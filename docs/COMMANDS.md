# Model Adder — Commands & Permissions Reference

## Command Structure

The main command is `/modeladder` (alias: `/ma`).

| Command | Permission | Description |
|---|---|---|
| `/ma help` | *(all)* | Shows command help guide |
| `/ma version` | *(all)* | Shows version and target Minecraft compatibility (1.21.11) |
| `/ma status` | `modeladder.status` | Displays memory diagnostics, registry counts, and pack build info |
| `/ma reload` | `modeladder.reload` | Safely reloads configuration and YAML content without restart |
| `/ma debug [export]` | `modeladder.debug` | Generates sanitized diagnostic log in `plugins/ModelAdder/logs/` |
| `/ma item list` | `modeladder.item.list` | Lists all registered custom items |
| `/ma item give <player> <item-id> [amount]` | `modeladder.item.give` | Gives custom items to an online player |
| `/ma item info <item-id>` | `modeladder.item.info` | Displays attributes, durability, and abilities of an item |
| `/ma block list` | `modeladder.block.list` | Lists all registered custom blocks |
| `/ma block info <block-id>` | `modeladder.block.info` | Shows block hardness, blast resistance, and drop table |
| `/ma mob list` | `modeladder.mob.list` | Lists all registered custom mobs |
| `/ma mob spawn <mob-id>` | `modeladder.mob.spawn` | Spawns a custom mob at the player's location |
| `/ma pack validate` | `modeladder.pack.validate` | Validates pack.mcmeta, model parent hierarchy, and textures |
| `/ma pack build` | `modeladder.pack.build` | Builds the resource pack deterministically, generates SHA-1, and publishes |
| `/ma pack status` | `modeladder.pack.status` | Shows active build ID, SHA-1 hash, and hosting URL |
| `/ma pack list` | `modeladder.pack.list` | Lists saved backup builds available for rollback |
| `/ma pack rollback <build-id>` | `modeladder.pack.rollback` | Rolls back the published resource pack to a prior valid build |
| `/ma pack send <player>` | `modeladder.pack.send` | Sends resource pack download prompt with SHA-1 hash to player |
| `/ma pack import <file.zip>` | `modeladder.pack.import` | Safely imports an archive from `resourcepack/imports/` with Zip-Slip checks |

---

## Permissions Hierarchy

- `modeladder.admin`: Gives full administrative control over Model Adder.
- `modeladder.*`: Wildcard access to all plugin sub-permissions.
