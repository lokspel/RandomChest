package me.lokspel.randomchest.util;

import me.lokspel.randomchest.RandomChest;
import me.lokspel.randomchest.task.RespawnTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChestUtil {

    private final RandomChest plugin;
    private final ChestDatabase database;
    private final Random random = new Random();

    private final Map<String, RefillData> refillDelays = new HashMap<>();
    private final Map<Location, Long> respawnDelays = new HashMap<>();
    private final Map<Player, String> selectedTypes = new HashMap<>();

    public ChestUtil(RandomChest plugin) {
        this.plugin = plugin;
        this.database = new ChestDatabase(plugin);
    }

    public void reloadChestDB() {
        database.reload();
    }

    public void restoreAllChests() {
        for (String key : database.getAllKeys()) {
            Block block = database.getBlock(key);

            if (block != null) {
                block.setType(Material.CHEST);
            }
        }
    }

    public void addChest(Player player, Block block) {
        database.setChestType(block, getSelectedType(player));
        database.save();
    }

    public void removeChest(Block block) {
        database.removeChest(block);
        database.save();
        respawnDelays.remove(block.getLocation());
    }

    public String getChestType(Block block) {
        return database.getChestType(block);
    }

    public void selectType(Player player, String type) {
        selectedTypes.put(player, type);
    }

    public String getSelectedType(Player player) {
        return selectedTypes.get(player);
    }

    public boolean haveSelectedType(Player player) {
        return selectedTypes.containsKey(player);
    }

    public void removeSelectedType(Player player) {
        selectedTypes.remove(player);
    }

    public void addRefillDelay(Chest chest, long delay) {
        refillDelays.put(
                toKey(chest.getBlock()),
                new RefillData(System.currentTimeMillis(), delay)
        );
    }

    public boolean canRefill(Chest chest) {
        String key = toKey(chest.getBlock());
        RefillData data = refillDelays.get(key);

        if (data == null) {
            return true;
        }

        if (System.currentTimeMillis() - data.createdAt() > data.delay()) {
            refillDelays.remove(key);
            return true;
        }

        return false;
    }

    public void addRespawnDelay(Chest chest, long delay) {
        respawnDelays.put(
                chest.getBlock().getLocation(),
                System.currentTimeMillis() + delay
        );
    }

    public int getRespawnDelay(String type) {
        List<Integer> delays = plugin.getConfig().getIntegerList(
                chestSetPath(type) + ".respawn"
        );

        return delays.isEmpty() ? 30 : randomFrom(delays);
    }

    public boolean isProtected(String type) {
        return !plugin.getConfig().getBoolean(
                chestSetPath(type) + ".break",
                false
        );
    }

    public void forceChestsRespawn() {
        respawnDelays.keySet().forEach(location ->
                location.getBlock().setType(Material.CHEST)
        );

        respawnDelays.clear();
    }

    public void startChestsRespawn() {
        for (Location location : respawnDelays.keySet()) {
            plugin.getFoliaLib().getScheduler().runAtLocationTimer(
                    location,
                    new RespawnTask(respawnDelays, location),
                    20L,
                    20L
            );
        }
    }

    public void fill(Chest chest, String type) {
        String path = chestSetPath(type);

        int min = plugin.getConfig().getInt(path + ".min", 1);
        int max = plugin.getConfig().getInt(path + ".max", 4);

        List<Map<?, ?>> items = plugin.getConfig().getMapList(
                path + ".items"
        );

        Inventory inventory = chest.getInventory();
        inventory.clear();

        for (int i = 0; i < random(min, max); i++) {
            ItemStack item = buildRandomItem(items);

            if (item != null) {
                addItemToRandomSlot(inventory, item);
            }
        }
    }

    private ItemStack buildRandomItem(List<Map<?, ?>> items) {
        if (items.isEmpty()) {
            return null;
        }

        Map<?, ?> config = randomFrom(items);
        Material material = getMaterial(config);

        if (material == null) {
            return null;
        }

        int amount = Math.max(
                ItemUtil.getInt(config, "amount"),
                1
        );

        if (MaterialUtil.isPotion(material)) {
            return buildPotion(config, material, amount);
        }

        ItemStack item = new ItemStack(material, amount);

        applyItemData(item, config);

        return item;
    }

    private void applyItemData(ItemStack item, Map<?, ?> config) {
        applyDamage(item, ItemUtil.getInt(config, "data"));
        applyEnchantments(item, config);
        applyDurability(item, config);
        applyMeta(item, config);
    }

    private Material getMaterial(Map<?, ?> config) {
        Object material = config.get("material");

        if (material == null) {
            material = config.get("type");
        }

        if (material == null) {
            material = config.get("id");
        }

        return MaterialUtil.getMaterial(material);
    }

    private void applyDamage(ItemStack item, int damage) {
        if (damage > 0) {
            ReflectionUtil.setDamage(item, damage);
        }
    }

    private void applyEnchantments(ItemStack item, Map<?, ?> config) {
        if (ItemUtil.getBoolean(config, "random-enchant")) {
            applyRandomEnchant(item);
            return;
        }

        applyConfiguredEnchants(
                item,
                getList(config, "enchantments")
        );
    }

    private void applyDurability(ItemStack item, Map<?, ?> config) {
        List<?> durability = getList(config, "durability");

        if (durability.size() != 2) {
            return;
        }

        Integer min = toInt(durability.get(0));
        Integer max = toInt(durability.get(1));

        if (min == null || max == null) {
            return;
        }

        applyRandomDurability(item, min, max);
    }

    private void applyMeta(ItemStack item, Map<?, ?> config) {
        applyMeta(
                item,
                (String) config.get("name"),
                getList(config, "lore"),
                (String) config.get("skull")
        );
    }

    private ItemStack buildPotion(
            Map<?, ?> config,
            Material material,
            int amount
    ) {
        String type = (String) config.get("potion-type");

        int level = Math.max(
                ItemUtil.getInt(config, "potion-level"),
                1
        );

        boolean splash = ItemUtil.getBoolean(config, "potion-splash")
                || MaterialUtil.isSplashPotion(material);

        ItemStack legacy = PotionUtil.buildLegacy(
                type,
                splash,
                level,
                amount
        );

        return legacy != null
                ? legacy
                : PotionUtil.buildMeta(type, splash, amount);
    }

    private void applyRandomEnchant(ItemStack item) {
        if (ItemUtil.isBow(item)) {
            randomEnchant(
                    item,
                    "POWER",
                    "PUNCH",
                    "FLAME",
                    "INFINITY"
            );
            return;
        }

        if (ItemUtil.isSword(item)) {
            randomEnchant(
                    item,
                    "SHARPNESS",
                    "SMITE",
                    "BANE_OF_ARTHROPODS",
                    "KNOCKBACK",
                    "FIRE_ASPECT"
            );
            return;
        }

        if (ItemUtil.isHelmet(item)) {
            randomEnchant(
                    item,
                    "RESPIRATION",
                    "AQUA_AFFINITY"
            );
            return;
        }

        if (ItemUtil.isBoots(item)) {
            randomEnchant(
                    item,
                    "FEATHER_FALLING"
            );
            return;
        }

        if (ItemUtil.isChestplate(item)) {
            randomEnchant(
                    item,
                    "PROTECTION",
                    "FIRE_PROTECTION",
                    "BLAST_PROTECTION",
                    "PROJECTILE_PROTECTION"
            );
        }
    }

    private void randomEnchant(ItemStack item, String... pool) {
        if (pool.length == 0) {
            return;
        }

        int count = random(1, pool.length);

        for (int i = 0; i < count; i++) {
            Enchantment enchantment = EnchantmentUtil.getByName(
                    pool[random(0, pool.length - 1)]
            );

            if (enchantment == null) {
                continue;
            }

            int level = random(
                    enchantment.getStartLevel(),
                    enchantment.getMaxLevel()
            );

            item.addUnsafeEnchantment(enchantment, level);
        }
    }

    private void applyConfiguredEnchants(
            ItemStack item,
            List<?> enchantments
    ) {
        for (Object object : enchantments) {
            if (!(object instanceof Map<?, ?> config)) {
                continue;
            }

            String name = (String) config.get("name");

            int level = Math.max(
                    ItemUtil.getInt(config, "level"),
                    1
            );

            Enchantment enchantment = EnchantmentUtil.getByName(name);

            if (enchantment == null) {
                continue;
            }

            level = Math.max(
                    level,
                    enchantment.getStartLevel()
            );

            level = Math.min(
                    level,
                    enchantment.getMaxLevel()
            );

            item.addUnsafeEnchantment(enchantment, level);
        }
    }

    private void applyRandomDurability(
            ItemStack item,
            int minPercent,
            int maxPercent
    ) {
        int maxDurability = ReflectionUtil.getMaxDurability(item);

        if (maxDurability <= 0) {
            return;
        }

        int from = (int) (maxDurability * minPercent / 100.0);
        int to = (int) (maxDurability * maxPercent / 100.0);

        ReflectionUtil.setDamage(
                item,
                random(from, to)
        );
    }

    @SuppressWarnings("deprecation")
    private void applyMeta(
            ItemStack item,
            String name,
            List<?> lore,
            String skull
    ) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        if (name != null) {
            meta.setDisplayName(name);
        }

        if (!lore.isEmpty()) {
            List<String> lines = new ArrayList<>();

            for (Object line : lore) {
                lines.add(String.valueOf(line));
            }

            meta.setLore(lines);
        }

        if (skull != null && meta instanceof SkullMeta skullMeta) {
            skullMeta.setPlayerProfile(
                    Bukkit.createProfile(skull)
            );
        }

        item.setItemMeta(meta);
    }

    private void addItemToRandomSlot(
            Inventory inventory,
            ItemStack item
    ) {
        List<Integer> emptySlots = new ArrayList<>();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                emptySlots.add(slot);
            }
        }

        if (emptySlots.isEmpty()) {
            return;
        }

        inventory.setItem(
                randomFrom(emptySlots),
                item
        );
    }

    public Material getToolMaterial(String path) {
        return MaterialUtil.getMaterial(
                plugin.getConfig().get(path)
        );
    }

    public boolean isSetExist(String type) {
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("chestset");

        return section != null
                && section.getKeys(false).contains(type.toLowerCase());
    }

    public int random(int min, int max) {
        if (min == max) {
            return min;
        }

        if (min > max) {
            throw new IllegalArgumentException(
                    "min cannot be greater than max"
            );
        }

        return random.nextInt(max - min + 1) + min;
    }

    private <T> T randomFrom(List<T> list) {
        return list.get(random(0, list.size() - 1));
    }

    private String chestSetPath(String type) {
        return "chestset." + type.toLowerCase();
    }

    private String toKey(Block block) {
        return block.getWorld().getName()
                + "," + block.getX()
                + "," + block.getY()
                + "," + block.getZ();
    }

    private List<?> getList(Map<?, ?> map, String key) {
        Object value = map.get(key);

        return value instanceof List<?>
                ? (List<?>) value
                : Collections.emptyList();
    }

    private Integer toInt(Object value) {
        return value instanceof Number
                ? ((Number) value).intValue()
                : null;
    }

    private record RefillData(long createdAt, long delay) {
    }
}