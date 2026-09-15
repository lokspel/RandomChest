# RandomChest

Random loot chest plugin for Spigot.

## » About

Adds configurable loot chests with multiple chest types and randomized contents. Each chest type can have its own loot table, item limits, respawn settings, and player interaction rules.

## » Features

- Multiple chest types with separate loot tables
- Random item selection with configurable minimum and maximum amounts
- Random or explicitly configured enchantments
- Potions and custom potion effects
- Player skulls
- Custom item names and lore
- Per-item durability ranges
- Configurable chest respawn delays
- Optional player destruction per chest type
- Persistent registered chest locations
- Automatic loot refills after the configured delay

## » Commands

| Command | Aliases | Description |
| --- | --- | --- |
| `/randomchest select <type>` | `/rs select` | Select a chest type to place |
| `/randomchest unselect` | `/rs unselect` | Clear the current selection |
| `/randomchest reload` | `/rs reload` | Reload configuration and database |
| `/randomchest restore` | `/rs restore` | Restore all registered chests |

## » Installation

1. Install the plugin on your Spigot server
2. Start the server once to generate the configuration
3. Configure your chest types and loot tables
4. Use `/randomchest select <type>` to select a chest type
5. Place and register your chests

## » How to use

1. Run `/randomchest select example`
2. Hold the selection tool (`GUNPOWDER`)
3. Right-click a chest in creative mode
4. The chest will be registered with the selected type
5. Run `/randomchest unselect` when finished

Registered chests are automatically filled with random loot when opened and refilled after their configured respawn delay.

To remove a registered chest, hold the removal tool (`SUGAR`) and right-click the chest in creative mode.

## » Item Fields

| Field | Description |
| --- | --- |
| `material` | Material name on modern Minecraft versions |
| `id` | Material ID, used when `material` is not specified on legacy versions |
| `amount` | Item stack size |
| `data` | Material data value for legacy item metadata |
| `durability` | Durability range `[min, max]` as a percentage of maximum durability |
| `random-enchant` | Applies a random enchantment based on the item type |
| `enchantments` | List of configured `name` / `level` enchantments |
| `name` | Custom display name |
| `lore` | Custom lore lines |
| `skull` | Player name used as the skull owner |
| `potion-type` | Potion effect type |
| `potion-level` | Potion effect level |
| `potion-splash` | Makes the potion a splash potion |

## » Chest Type Fields

| Field | Description |
| --- | --- |
| `min` | Minimum number of items generated |
| `max` | Maximum number of items generated |
| `customname` | Reserved for future use |
| `powered` | Reserved for future redstone control |
| `break` | Allows players to destroy the chest |
| `respawn` | Respawn delay in seconds; a random value is selected from the configured list |
