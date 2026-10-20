# Model Adder

**Model Adder** is a high-performance, original Minecraft custom content engine, item model system, and automated resource-pack delivery pipeline for Minecraft Java Edition **1.21.11** (Paper / Purpur).

---

## Key Features

- **Custom Items & Models:** Namespaced IDs (`modeladder:ruby_sword`), PersistentDataContainer identities, custom model data, modern 1.21.x item models, custom durability, and ability triggers with cooldowns.
- **Custom Blocks & Furniture:** Hardness, custom drops, block persistence across chunk unloads and restarts. Modern 1.21 `ItemDisplay` and `Interaction` furniture with yaw snapping and sitting mechanics.
- **Crops & Trees:** Staged crop growth, seeds, reusable harvesting, and tree generation.
- **Custom Mobs & Drops:** Custom mob attributes (health, attack damage, speed), equipment, and drop tables.
- **Crafting & Smelting:** Custom shaped and shapeless crafting recipes with exact ingredient choices.
- **Resource-Pack Engine:**
  - Automated staging, validation (JSON syntax, model parents, texture resolution), and deterministic ZIP generation.
  - Reproducible SHA-1 cryptographic integrity hashing.
  - Built-in embedded HTTP server (serving on configurable port, e.g. 8085) with memory-safe streaming and path restriction.
  - External HTTPS hosting adapter with SHA-1 verification.
  - Atomic publishing, backup retention, and one-command rollback (`/ma pack rollback <build-id>`).
- **Archive Security:** Complete defense against Zip-Slip, path traversal (`../`), absolute paths, and zip bombs.
- **Extensible Architecture:** Developer API (`ModelAdderAPI`), custom Bukkit events, PlaceholderAPI expansion, in-game GUIs, and sanitized debug reports.

---

## Quick Start

### 1. Requirements
- Server: **Paper** or **Purpur** for **Minecraft Java 1.21.11**
- Runtime: **Java 21+** (e.g. Eclipse Adoptium OpenJDK 21 LTS)

### 2. Installation
1. Download `ModelAdder-1.21.11.jar` from `release/`.
2. Place the JAR into your server's `plugins/` directory.
3. Start or restart the server.
4. Default configuration and sample YAML content will be automatically generated in `plugins/ModelAdder/`.

### 3. Basic Commands
- `/modeladder help` — List all available commands.
- `/modeladder version` — Display target compatibility and build version.
- `/modeladder status` — View runtime diagnostics and resource pack status.
- `/modeladder reload` — Safely reload YAML content and configs.
- `/modeladder item give <player> <item-id> [amount]` — Give custom items.
- `/modeladder pack build` — Build and publish the resource pack.
- `/modeladder pack send <player>` — Dispatches pack prompt to player.

---

## Module Layout

- [`model-adder-api`](model-adder-api/): Public API contracts, content keys, registries, and custom Bukkit events.
- [`model-adder-pack`](model-adder-pack/): Resource pack pipeline, security, validation, deterministic builder, HTTP hosting, backups.
- [`model-adder-core`](model-adder-core/): Main plugin lifecycle, configs, item/block/furniture/mob mechanics, recipe registration, commands.
- [`model-adder-editor`](model-adder-editor/): In-game studio GUIs for item and pack administration.
- [`model-adder-integrations`](model-adder-integrations/): PlaceholderAPI and soft integration hooks.
- [`model-adder-tests`](model-adder-tests/): Automated test suite covering security, JSON validation, determinism, and registries.

---

## Building from Source

To compile the project and run all automated tests:

```powershell
.\gradlew.bat clean build
```

To produce the release JAR in `release/ModelAdder-1.21.11.jar`:

```powershell
.\gradlew.bat packageRelease
```

---

## Documentation

Full guides are available in the [`docs/`](docs/) directory:
- [Installation Guide](docs/INSTALLATION.md)
- [Configuration Reference](docs/CONFIGURATION.md)
- [Commands & Permissions](docs/COMMANDS.md)
- [Resource Pack Pipeline](docs/RESOURCE_PACKS.md)
- [Custom Items Guide](docs/CUSTOM_ITEMS.md)
- [Custom Blocks & Furniture](docs/CUSTOM_BLOCKS.md)
- [Developer API Guide](docs/DEVELOPER_API.md)
- [Compatibility & Quality Report](docs/COMPATIBILITY.md)
- [Release Notes](docs/RELEASE_NOTES.md)
