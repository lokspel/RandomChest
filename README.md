# RandomChest

Random loot chest plugin for Spigot 1.8.8. Register chests, fill them with random items from configurable loot tables, and respawn them on a timer.

## Features

- Multiple chest types with configurable loot tables
- Random item selection with min/max item count per chest
- Enchantment support (random or configured)
- Potion, skull, custom name, and lore support
- Configurable respawn delay per chest type
- Chest destruction toggle per type
- Redstone power control per type
- Item durability ranges
- Locale system with `.properties` files

## Commands

| Command | Aliases | Description |
|---|---|---|
| `/randomchest select <type>` | `/rs select` | Select a chest type to place |
| `/randomchest unselect` | `/rs unselect` | Clear selection |
| `/randomchest reload` | `/rs reload` | Reload config and database |
| `/randomchest restore` | `/rs restore` | Restore all registered chests |

## How to use

1. `/randomchest select 1` — select chest type
2. Hold the select tool (289 / powder) in hand
3. Right-click a chest in creative mode — chest is registered
4. `/randomchest unselect` — clear selection

Registered chests auto-fill with random loot when opened and respawn after a delay.

## Configuration

### `config.yml`

```yaml
# Locale file name (without .properties)
locale: en_US

# Select tool item ID (289 = powder)
select-tool: 289
# Remove tool item ID (353 = sugar)
remove-tool: 353

# Chest types
chestset:
  1: # Type name
    min: 1 # The minimum number of possible loot
    max: 2 # Maximum possible loot
    customname: "&e[&0&l Сундук&e ]"
    powered: false # Turn on redstone control
    break: false # Include the destruction of chests
    respawn: [45, 60, 75, 80] # Time in seconds through which to put things respawn
    items:
      - id: 310
        amount: 1
        random-enchant: true
      - id: 310
        amount: 1
      - id: 261
        amount: 1
        random-enchant: true
      - id: 262
        amount: 30
      - id: 322
        data: 1
        amount: 10
      - id: 384
        amount: 30
```

### Item fields

| Field | Description |
|---|---|
| `id` | Material ID |
| `amount` | Stack size |
| `data` | Material data value |
| `durability` | Durability range `[min, max]` as percentages |
| `random-enchant` | Apply random enchantments |
| `enchantments` | List of specific enchantments |
| `name` | Custom display name |
| `lore` | Custom lore lines |
| `skull` | Player name for skull owner |
| `potion-type` | Potion type name |
| `potion-level` | Potion level |
| `potion-splash` | Make it a splash potion |

### Chest type fields

| Field | Description |
|---|---|
| `min` | Minimum items per chest |
| `max` | Maximum items per chest |
| `customname` | Chest custom name |
| `powered` | Redstone control |
| `break` | Allow chest destruction |
| `respawn` | Respawn delay in seconds (random from list) |

### `messages.yml`

All messages support `&` color codes. Insert the prefix with `%prefix%`. Placeholders: `%type%`, `%item%`.

## Building

Requires Java 8 and Maven.

```sh
mvn clean package
```

The output jar is `target/RandomChest-1.0-SNAPSHOT.jar`.

## Requirements

- Spigot / PandaSpigot 1.8.8
- Java 8
