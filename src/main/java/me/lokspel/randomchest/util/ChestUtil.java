package me.lokspel.randomchest.util;

import me.lokspel.randomchest.RandomChest;
import me.lokspel.randomchest.task.RespawnTask;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class ChestUtil {

    private final RandomChest plugin;
    private final ChestDatabase database;
    private final Random rnd = new Random();

    private final HashMap<String, Long[]> refillDelay = new HashMap<>();
    private final ConcurrentHashMap<String, Long[]> respawnDelay = new ConcurrentHashMap<>();
    private final HashMap<Player, String> selectType = new HashMap<>();

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
        respawnDelay.remove(toKey(block));
    }

    public String getChestType(Block block) {
        return database.getChestType(block);
    }

    public void selectType(Player player, String type) {
        selectType.put(player, type);
    }

    public String getSelectedType(Player player) {
        return selectType.get(player);
    }

    public boolean haveSelectedType(Player player) {
        return selectType.containsKey(player);
    }

    public void removeSelectedType(Player player) {
        selectType.remove(player);
    }

    public void addRefillDelay(Chest chest, long delay) {
        refillDelay.put(toKey(chest.getBlock()), new Long[]{System.currentTimeMillis(), delay});
    }

    public boolean canRefill(Chest chest) {
        String key = toKey(chest.getBlock());
        if (!refillDelay.containsKey(key)) {
            return true;
        }
        Long[] data = refillDelay.get(key);
        if (System.currentTimeMillis() - data[0] > data[1]) {
            refillDelay.remove(key);
            return true;
        }
        return false;
    }

    public void addRespawnDelay(Chest chest, long delay) {
        respawnDelay.put(toKey(chest.getBlock()), new Long[]{System.currentTimeMillis(), delay});
    }

    public int getRespawnDelay(String type) {
        List<Integer> list = plugin.getConfig().getIntegerList("chestset." + type.toLowerCase() + ".respawn");
        if (list != null && !list.isEmpty()) {
            return list.get(random(0, list.size() - 1));
        }
        return 30;
    }

    public boolean isProtected(String type) {
        return !plugin.getConfig().getBoolean("chestset." + type.toLowerCase() + ".break", false);
    }

    public void forceChestsRespawn() {
        Iterator<Map.Entry<String, Long[]>> iterator = respawnDelay.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long[]> entry = iterator.next();
            iterator.remove();
            String[] parts = entry.getKey().split(",");
            org.bukkit.World world = plugin.getServer().getWorld(parts[0]);
            if (world != null) {
                Block block = world.getBlockAt(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3])
                );
                block.setType(Material.CHEST);
            }
        }
    }

    public void startChestsRespawn() {
        plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                new RespawnTask(respawnDelay),
                20L,
                20L
        );
    }

    public void fill(Chest chest, String type) {
        int min = plugin.getConfig().getInt("chestset." + type + ".min", 1);
        int max = plugin.getConfig().getInt("chestset." + type + ".max", 4);
        List<Map<?, ?>> items = plugin.getConfig().getMapList("chestset." + type + ".items");

        Inventory inv = chest.getInventory();
        inv.clear();

        int count = random(min, max);
        for (int i = 0; i < count; i++) {
            ItemStack item = buildRandomItem(items);
            if (item != null) {
                addItemToRandomSlot(inv, item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ItemStack buildRandomItem(List<Map<?, ?>> items) {
        Map<?, ?> map = items.get(random(0, items.size() - 1));

        int id = getInt(map, "id");
        if (id <= 0) return null;

        int data = getInt(map, "data");
        int amount = Math.max(getInt(map, "amount"), 1);
        String name = (String) map.get("name");
        List<?> lore = getList(map, "lore");
        String skull = (String) map.get("skull");

        Object durabilityObj = map.get("durability");
        List<Integer> durabilityRange = null;
        if (durabilityObj instanceof List) {
            durabilityRange = (List<Integer>) durabilityObj;
        }

        boolean randomEnchant = getBoolean(map, "random-enchant");
        List<?> enchantments = getList(map, "enchantments");

        Material mat = getMaterialById(id);
        if (mat == null) return null;

        if (mat.equals(Material.POTION)) {
            return buildPotion(map, amount);
        }

        ItemStack item = new ItemStack(mat, amount);
        if (data != 0) {
            item.setDurability((short) data);
        }

        if (randomEnchant) {
            applyRandomEnchant(item);
        } else {
            applyConfiguredEnchants(item, enchantments);
        }

        if (durabilityRange != null && durabilityRange.size() == 2) {
            applyRandomDurability(item, durabilityRange.get(0), durabilityRange.get(1));
        }

        applyMeta(item, name, lore, skull);
        return item;
    }

    private ItemStack buildPotion(Map<?, ?> map, int amount) {
        PotionType type = PotionType.valueOf((String) map.get("potion-type"));
        int level = Math.max(getInt(map, "potion-level"), 1);
        boolean splash = getBoolean(map, "potion-splash");

        Potion potion = new Potion(PotionType.WATER);
        potion.setType(type);
        if (!potion.getType().equals(PotionType.WATER)) {
            potion.setSplash(splash);
            potion.setLevel(Math.min(level, potion.getType().getMaxLevel()));
        }
        return potion.toItemStack(amount);
    }

    private void applyRandomEnchant(ItemStack item) {
        if (isBow(item)) {
            randomEnchant(item, Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK,
                    Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE);
        } else if (isSword(item)) {
            randomEnchant(item, Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD,
                    Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT);
        } else if (isHelmet(item)) {
            randomEnchant(item, Enchantment.OXYGEN, Enchantment.WATER_WORKER);
        } else if (isBoots(item)) {
            randomEnchant(item, Enchantment.PROTECTION_FALL);
        } else if (isChestplate(item)) {
            randomEnchant(item, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE,
                    Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE);
        }
    }

    private void randomEnchant(ItemStack item, Enchantment... pool) {
        int count = random(1, pool.length);
        for (int i = 0; i < count; i++) {
            Enchantment ench = pool[random(0, pool.length - 1)];
            int level = random(ench.getStartLevel(), ench.getMaxLevel());
            item.addUnsafeEnchantment(ench, level);
        }
    }

    private void applyConfiguredEnchants(ItemStack item, List<?> enchantments) {
        for (Object obj : enchantments) {
            Map<?, ?> enchMap = (Map<?, ?>) obj;
            String enchName = (String) enchMap.get("name");
            int enchLevel = Math.max(getInt(enchMap, "level"), 1);

            Enchantment ench = Enchantment.getByName(enchName);
            if (ench != null) {
                enchLevel = Math.max(enchLevel, ench.getStartLevel());
                enchLevel = Math.min(enchLevel, ench.getMaxLevel());
                item.addUnsafeEnchantment(ench, enchLevel);
            }
        }
    }

    private void applyRandomDurability(ItemStack item, int minPercent, int maxPercent) {
        short maxDur = item.getType().getMaxDurability();
        if (maxDur == 0) return;
        int from = (int) (maxDur / 100.0 * minPercent);
        int to = (int) (maxDur / 100.0 * maxPercent);
        item.setDurability((short) random(from, to));
    }

    private void applyMeta(ItemStack item, String name, List<?> lore, String skull) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (name != null) {
            meta.setDisplayName(name);
        }

        if (lore != null && !lore.isEmpty()) {
            List<String> loreStrings = new ArrayList<>();
            for (Object line : lore) {
                loreStrings.add(String.valueOf(line));
            }
            meta.setLore(loreStrings);
        }

        if (skull != null && meta instanceof SkullMeta) {
            item.setDurability((short) 3);
            ((SkullMeta) meta).setOwner(skull);
        }

        item.setItemMeta(meta);
    }

    private void addItemToRandomSlot(Inventory inventory, ItemStack item) {
        int slot = random(0, inventory.getSize() - 1);
        if (inventory.getItem(slot) != null) {
            addItemToRandomSlot(inventory, item);
        } else {
            inventory.setItem(slot, item);
        }
    }

    public Material getMaterialById(int id) {
        for (Material mat : Material.values()) {
            if (mat.getId() == id) {
                return mat;
            }
        }
        return null;
    }

    public boolean isSetExist(String type) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("chestset");
        return section != null && section.getKeys(false).contains(type.toLowerCase());
    }

    public int random(int min, int max) {
        if (min == 0 && max == 0) return 0;
        return rnd.nextInt(max - min + 1) + min;
    }

    private String toKey(Block block) {
        return block.getWorld().getName()
                + "," + block.getX()
                + "," + block.getY()
                + "," + block.getZ();
    }

    private boolean isChestplate(ItemStack item) {
        Material m = item.getType();
        return m.equals(Material.LEATHER_CHESTPLATE) || m.equals(Material.IRON_CHESTPLATE)
                || m.equals(Material.DIAMOND_CHESTPLATE);
    }

    private boolean isBoots(ItemStack item) {
        Material m = item.getType();
        return m.equals(Material.LEATHER_BOOTS) || m.equals(Material.IRON_BOOTS)
                || m.equals(Material.DIAMOND_BOOTS);
    }

    private boolean isHelmet(ItemStack item) {
        Material m = item.getType();
        return m.equals(Material.LEATHER_HELMET) || m.equals(Material.IRON_HELMET)
                || m.equals(Material.DIAMOND_HELMET);
    }

    private boolean isSword(ItemStack item) {
        Material m = item.getType();
        return m.equals(Material.WOOD_SWORD) || m.equals(Material.STONE_SWORD)
                || m.equals(Material.IRON_SWORD) || m.equals(Material.DIAMOND_SWORD);
    }

    private boolean isBow(ItemStack item) {
        return item.getType().equals(Material.BOW);
    }

    private int getInt(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private boolean getBoolean(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof Boolean && (Boolean) val;
    }

    private List<?> getList(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val instanceof List ? (List<?>) val : Collections.emptyList();
    }
}
