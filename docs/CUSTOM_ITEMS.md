# Model Adder — Custom Items & Abilities Guide

Custom items are defined in `plugins/ModelAdder/content/items/<name>.yml`.

---

## Example Item Definition: `ruby_sword.yml`

```yaml
id: "modeladder:ruby_sword"
display:
  name: "<red><bold>Ruby Sword</bold></red>"
  lore:
    - "<gray>A forged crystal blade of ancient crimson fire."
    - "<yellow>Ability: <gold>Ruby Slash <gray>(Right-Click)"

material: "DIAMOND_SWORD"

model:
  asset: "modeladder:item/ruby_sword"
  texture: "modeladder:item/ruby_sword"
  custom-model-data: 10001

properties:
  stack-size: 1
  durability: 1800
  unbreakable: false

abilities:
  - "modeladder:ruby_slash"
```

---

## Persistent Item Identity

Model Adder tags custom items using Bukkit's `PersistentDataContainer` (PDC):
- `modeladder:id` (STRING): Unique namespaced key (e.g. `modeladder:ruby_sword`).
- `modeladder:durability` (INTEGER): Current custom durability.
- `modeladder:max_durability` (INTEGER): Configured maximum durability.

This guarantees that renaming, modifying lore, or enchanting custom items on anvils will **never** cause the item to lose its custom identity.

---

## Custom Ability Definition: `ruby_slash.yml`

Located in `plugins/ModelAdder/content/abilities/ruby_slash.yml`:

```yaml
id: "modeladder:ruby_slash"
display: "Ruby Slash"
trigger: "RIGHT_CLICK"
cooldown-seconds: 4.5
sound: "minecraft:entity.player.attack.sweep"
particle: "minecraft:flame"

effects:
  - "damage:nearby:4:6.0"
  - "launch:0.8"
```

Supported Triggers:
- `RIGHT_CLICK`
- `LEFT_CLICK`
- `HIT_ENTITY`
- `KILL_ENTITY`
- `TAKE_DAMAGE`
- `SNEAK`
- `CONSUME`
