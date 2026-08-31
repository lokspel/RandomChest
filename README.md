# RandomChest

Random loot chest plugin for Spigot. Register chests, fill them with random items from configurable loot tables, and respawn them on a timer. Cross-version API differences are handled via reflection, no runtime dependencies.

## Features

- Multiple chest types, each with its own loot table
- Random loot with adjustable item count per chest (min/max)
- Enchantments — random per item type or explicitly configured
- Potions, player skulls, custom names and lore
- Per-type respawn timer, chests refill after the delay elapses
- Per-type option to let chests be destroyed by players
- Item durability ranges (as percentage of max durability)

## Commands

| Command | Aliases | Description |
|---|---|---|
| `/randomchest select <type>` | `/rs select` | Select a chest type to place |
| `/randomchest unselect` | `/rs unselect` | Clear selection |
| `/randomchest reload` | `/rs reload` | Reload config and database |
| `/randomchest restore` | `/rs restore` | Restore all registered chests |

## How to use

1. `/randomchest select example` — select a chest type (must match a key in `chestset`)
2. Hold the select tool (`GUNPOWDER` / id 289) in hand
3. Right-click a chest in creative mode — chest is registered
4. `/randomchest unselect` — clear selection

Registered chests auto-fill with random loot when opened and respawn after a delay.

To remove a chest, hold the remove tool (`SUGAR` / id 353) and right-click the chest in creative mode.

## Configuration

### `config.yml`

```yaml
# MODERN config: items use material names.
# For legacy servers (1.8-1.12) replace the names with numeric ids:
#   IRON_SWORD  -> id: 268
#   POTION      -> id: 373
#   RED_WOOL    -> id: 35  (color via data: 14)
#   PLAYER_HEAD -> id: 397
# select-tool / remove-tool accept a name or a legacy id too.
select-tool: GUNPOWDER #powder (legacy id: 289)
remove-tool: SUGAR #sugar (legacy id: 353)
chestset:
  example: #Type name
    min: 1 #The minimum number of possible loot
    max: 3 #Maximum possible loot
    customname: "Example"
    powered: false #Turn on redstone control
    break: false #Include the destruction of chests
    respawn: [15, 30, 45] #Time in seconds through which to put things respawn
    items:
      - material: IRON_SWORD #legacy id: 268
        random-enchant: false #Include random enchantment
        enchantments:
          - name: KNOCKBACK #Type of enchantment
            level: 1 #Enchantment level
        name: "My Sword" #A new name for things
        lore: ["Line 1", "Line 2", "Line 3"] #Description of things
        amount: 1 #amount
      - material: POTION #legacy id: 373
        potion-type: INSTANT_HEAL #Potion type
        potion-level: 2 #Potion level
        potion-splash: false #To make an explosive potion
      - material: RED_WOOL #legacy id: 35 + data 14
        data: 14 #Thing data (legacy; ignored on modern)
        amount: 5
      - material: PLAYER_HEAD #legacy id: 397
        skull: "Notch" #Nickname of the owner of the head
```

### Item fields

| Field | Description |
|---|---|
| `material` | Material name (modern servers) |
| `id` | Material ID, used when `material` is absent (legacy servers) |
| `amount` | Stack size |
| `data` | Material data value (legacy meta) |
| `durability` | Durability range `[min, max]`, percent of max durability (applied only when both values are set) |
| `random-enchant` | Apply a random enchantment (pool depends on item type) |
| `enchantments` | List of `name` / `level` — used when `random-enchant` is false |
| `name` | Custom display name |
| `lore` | Lore lines |
| `skull` | Player name for skull owner |
| `potion-type` | Potion effect (only for potion materials) |
| `potion-level` | Potion level |
| `potion-splash` | Make it a splash potion |

### Chest type fields

| Field | Description |
|---|---|
| `min` | Minimum items per chest |
| `max` | Maximum items per chest |
| `customname` | **Not implemented yet**, reserved |
| `powered` | Redstone control — **not implemented yet**, reserved |
| `break` | Allow chests to be destroyed by players |
| `respawn` | Respawn delay in seconds (random value from the list) |

### `messages.yml`

```yaml
prefix: "&7[&eRandomChest&7] "
only-game: "&cThis command is not available in the console."
no-permission: "&cInsufficient permissions."
reload: "&aConfiguration and database has been reloaded."
selected: "&7Selected ''&e%type%&7''. Hold &e%item% &7and right click a chest."
type-not-found: "&7Type ''&e%type%&7'' does not exist."
unselect: "&7Cleared."
restore: "&7Chests restored."
add: "&7Chest added to base."
remove: "&7Chest removed from base."
usage: "&cUsage: /randomchest <reload|select|unselect|restore>"
```

All message values support `&` color codes; use `%prefix%` to insert the prefix.
Available placeholders: `%type%`, `%item%`.

## Building

Requires Java 21 and Maven.

```sh
mvn clean package
```

The output jar is `target/RandomChest-1.0-SNAPSHOT.jar`.

## Requirements

- Spigot/Paper 1.8 – 26.2
- Java 21+ runtime

> Note: numeric material IDs only resolve on servers where `Material.getId()`
> still exists; on newer Paper versions prefer `material`/`type` names in the
> config.
