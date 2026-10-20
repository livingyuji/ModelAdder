# Model Adder v1.0.0 — Release Notes

**Target Minecraft Version:** Minecraft Java Edition 1.21.11  
**Target Server:** Paper / Purpur 1.21.11  
**Java Runtime:** Java 21 LTS  
**Release Artifact:** `release/ModelAdder-1.21.11.jar`  

---

## What's New

### 1. Multi-Module Architecture
- Complete modular architecture separating API, Resource Pack Engine, Core, In-game Editor, Integrations, and Tests.
- Zero proprietary code or unlicensed assets; 100% original implementation.

### 2. Resource Pack Pipeline & Dual Hosting
- Automatic deterministic resource pack packaging with normalized timestamps.
- Exact SHA-1 hash calculation and client dispatch.
- Built-in embedded HTTP daemon restricted exclusively to serving the published pack.
- External HTTPS hosting adapter with accessibility validation.
- Atomic file publishing preventing corrupted overwrites on failure.
- Versioned backups and instant `/ma pack rollback <build-id>` command.

### 3. Archive Security
- Rigorous security checking rejecting Zip-Slip traversal (`../`), leading slashes, zip bombs, and oversized archives.

### 4. Custom Content Systems
- Custom items with PDC identities, custom model data, item models, custom durability, and ability triggers with cooldowns.
- Custom blocks with configurable hardness, blast resistance, sounds, and drop tables.
- Modern 3D furniture using `ItemDisplay` and `Interaction` entities with sit mechanics.
- Crops with staged growth, harvest drops, and reusable harvesting.
- Custom mobs with custom health, damage, speed, equipment, and drops.
- Custom crafting and smelting recipes.

### 5. Diagnostics & Tooling
- In-game studio GUI for items and pack management.
- Sanitized debug exporter (`/ma debug export`) for safe troubleshooting without exposing credentials.
- PlaceholderAPI expansion hook.
- 100% automated test pass across all unit, security, determinism, and merge test suites.
