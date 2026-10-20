# Model Adder — Compatibility & Quality Report

## Verified Platform Matrix

| Component | Target Version | Status | Verification Note |
|---|---|---|---|
| **Minecraft Java** | **1.21.11** | **VERIFIED** | Primary target baseline |
| **Server Platform** | **PaperMC / Purpur** | **VERIFIED** | Compiled with `io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT` |
| **Java Baseline** | **Java 21 LTS** | **VERIFIED** | Tested with Eclipse Adoptium OpenJDK 21.0.12.1+1 LTS |
| **Build Tool** | **Gradle 8.8** | **VERIFIED** | Multi-module build with local wrapper |

---

## 1.21.x Specific Conventions

- **Attribute Naming:** 1.21.2+ removed the legacy `GENERIC_` prefix. Model Adder uses `Attribute.MAX_HEALTH`, `Attribute.MOVEMENT_SPEED`, and `Attribute.ATTACK_DAMAGE`.
- **Entity Dismounting:** Uses modern Paper `org.bukkit.event.entity.EntityDismountEvent`.
- **Item Model Components:** Compatible with both `setCustomModelData` and modern 1.21.x `setItemModel`.
- **Resource Pack Format:** Generated with `pack_format: 34` and `supported_formats: {min_format: 34, max_format: 48}` to guarantee multi-client compatibility across 1.21.x releases.

---

## Limitations & Boundaries

1. **Vanilla Display Blocks:** Custom blocks use note blocks, mushroom stems, or barriers as physical anchors. Fully native arbitrary collision shapes without display entities are a vanilla client limitation.
2. **Mob Geometry:** Arbitrary multi-joint animated mob geometry requires client-side resource pack models or an external model-engine adapter.
3. **HTTP Server Port:** If using built-in hosting, the configured HTTP port (default 8085) must be open in server firewalls or forwarded behind a reverse proxy.
