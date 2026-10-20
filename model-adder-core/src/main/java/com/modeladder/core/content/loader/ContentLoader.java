package com.modeladder.core.content.loader;

import com.modeladder.api.content.*;
import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.core.content.model.*;
import com.modeladder.core.registry.DefaultRegistryManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Reads, validates, and populates all custom content YAML definitions.
 */
public final class ContentLoader {

    private final Path contentRoot;
    private final DefaultRegistryManager registryManager;
    private final Logger logger;

    public ContentLoader(@NotNull Path contentRoot, @NotNull DefaultRegistryManager registryManager, @NotNull Logger logger) {
        this.contentRoot = contentRoot;
        this.registryManager = registryManager;
        this.logger = logger;
    }

    @NotNull
    public ValidationResult loadAll() {
        ValidationResult result = ValidationResult.ok();
        registryManager.clearAll();

        try {
            ensureDirectoriesAndExamples();

            // 1. Load abilities first (items depend on them)
            loadAbilities(result);

            // 2. Load items & armor
            loadItems(result);

            // 3. Load blocks & furniture
            loadBlocks(result);
            loadFurniture(result);

            // 4. Load crops & trees
            loadCrops(result);
            loadTrees(result);

            // 5. Load mobs & recipes
            loadMobs(result);
            loadRecipes(result);

        } catch (Exception e) {
            result.addError(ErrorCode.CONFIG_LOAD_ERROR, "Unexpected error loading content definitions: " + e.getMessage());
        }

        return result;
    }

    private void ensureDirectoriesAndExamples() throws IOException {
        for (ContentType type : ContentType.values()) {
            Path folder = contentRoot.resolve(type.getFolderName());
            Files.createDirectories(folder);
        }

        // Example Item: ruby_sword
        Path itemExample = contentRoot.resolve("items").resolve("ruby_sword.yml");
        if (!Files.exists(itemExample)) {
            String yaml = """
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
                properties:
                  stack-size: 1
                  durability: 1800
                  unbreakable: false
                abilities:
                  - "modeladder:ruby_slash"
                """;
            Files.writeString(itemExample, yaml);
        }

        // Example Ability: ruby_slash
        Path abilityExample = contentRoot.resolve("abilities").resolve("ruby_slash.yml");
        if (!Files.exists(abilityExample)) {
            String yaml = """
                id: "modeladder:ruby_slash"
                display: "Ruby Slash"
                trigger: "RIGHT_CLICK"
                cooldown-seconds: 4.5
                sound: "minecraft:entity.player.attack.sweep"
                particle: "minecraft:flame"
                effects:
                  - "damage:nearby:4:6.0"
                  - "launch:0.8"
                """;
            Files.writeString(abilityExample, yaml);
        }

        // Example Block: ruby_ore
        Path blockExample = contentRoot.resolve("blocks").resolve("ruby_ore.yml");
        if (!Files.exists(blockExample)) {
            String yaml = """
                id: "modeladder:ruby_ore"
                display: "Ruby Ore"
                material: "NOTE_BLOCK"
                model: "modeladder:block/ruby_ore"
                properties:
                  hardness: 3.5
                  blast-resistance: 15.0
                  light-level: 3
                drops:
                  - item: "modeladder:ruby_sword"
                    min: 1
                    max: 1
                    chance: 1.0
                """;
            Files.writeString(blockExample, yaml);
        }

        // Example Furniture: oak_chair
        Path furnitureExample = contentRoot.resolve("furniture").resolve("oak_chair.yml");
        if (!Files.exists(furnitureExample)) {
            String yaml = """
                id: "modeladder:oak_chair"
                display: "Oak Chair"
                item: "modeladder:ruby_sword"
                hitbox:
                  width: 0.8
                  height: 0.9
                seating:
                  can-sit: true
                  sit-height-offset: -0.2
                rotation-step: 90
                """;
            Files.writeString(furnitureExample, yaml);
        }
    }

    private void loadAbilities(ValidationResult result) {
        scanYamlFiles("abilities", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) {
                result.addError(ErrorCode.INVALID_ID_FORMAT, "Invalid ability ID: " + rawId, file.getPath(), "id");
                return;
            }
            ContentKey key = ContentKey.of(rawId);
            String display = config.getString("display", key.getKey());
            String triggerStr = config.getString("trigger", "RIGHT_CLICK").toUpperCase();
            CustomAbility.AbilityTrigger trigger;
            try {
                trigger = CustomAbility.AbilityTrigger.valueOf(triggerStr);
            } catch (IllegalArgumentException e) {
                trigger = CustomAbility.AbilityTrigger.RIGHT_CLICK;
            }
            double cooldown = config.getDouble("cooldown-seconds", 0.0);
            String sound = config.getString("sound");
            String particle = config.getString("particle");
            List<String> effects = config.getStringList("effects");

            AbilityDefinition def = new AbilityDefinition(key, display, trigger, cooldown, null, sound, particle, effects);
            registryManager.getAbilities().register(def);
        }, result);
    }

    private void loadItems(ValidationResult result) {
        scanYamlFiles("items", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) {
                result.addError(ErrorCode.INVALID_ID_FORMAT, "Invalid item ID: " + rawId, file.getPath(), "id");
                return;
            }
            ContentKey key = ContentKey.of(rawId);
            String matStr = config.getString("material", "STICK").toUpperCase();
            Material material = Material.matchMaterial(matStr);
            if (material == null) {
                result.addError(ErrorCode.INVALID_MATERIAL, "Invalid material '" + matStr + "' in " + file.getName(), file.getPath(), "material");
                material = Material.STICK;
            }

            String name = config.getString("display.name", key.getKey());
            List<String> lore = config.getStringList("display.lore");
            Integer cmd = config.contains("model.custom-model-data") ? config.getInt("model.custom-model-data") : null;
            String modelAsset = config.getString("model.asset");
            String textureAsset = config.getString("model.texture");

            int stackSize = Math.max(1, Math.min(99, config.getInt("properties.stack-size", material.getMaxStackSize())));
            Integer maxDurability = config.contains("properties.durability") ? config.getInt("properties.durability") : null;
            boolean unbreakable = config.getBoolean("properties.unbreakable", false);

            List<String> abilityKeys = config.getStringList("abilities");
            List<ContentKey> parsedAbilities = new ArrayList<>();
            for (String ak : abilityKeys) {
                if (ContentKey.isValid(ak)) {
                    parsedAbilities.add(ContentKey.of(ak));
                }
            }

            ItemDefinition def = new ItemDefinition(key, material, name, lore, cmd, modelAsset, textureAsset, stackSize, maxDurability, unbreakable, parsedAbilities, null);
            registryManager.getItems().register(def);
        }, result);
    }

    private void loadBlocks(ValidationResult result) {
        scanYamlFiles("blocks", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) {
                result.addError(ErrorCode.INVALID_ID_FORMAT, "Invalid block ID: " + rawId, file.getPath(), "id");
                return;
            }
            ContentKey key = ContentKey.of(rawId);
            String matStr = config.getString("material", "NOTE_BLOCK").toUpperCase();
            Material material = Material.matchMaterial(matStr);
            if (material == null) {
                material = Material.NOTE_BLOCK;
            }

            String display = config.getString("display", key.getKey());
            String model = config.getString("model");
            float hardness = (float) config.getDouble("properties.hardness", 2.0);
            float blastRes = (float) config.getDouble("properties.blast-resistance", 10.0);
            int light = config.getInt("properties.light-level", 0);

            List<CustomBlock.BlockDrop> drops = parseDrops(config.getMapList("drops"));
            BlockDefinition def = new BlockDefinition(key, material, display, model, hardness, blastRes, light, drops, null, null);
            registryManager.getBlocks().register(def);
        }, result);
    }

    private void loadFurniture(ValidationResult result) {
        scanYamlFiles("furniture", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) return;
            ContentKey key = ContentKey.of(rawId);

            String rawItem = config.getString("item", rawId);
            ContentKey itemKey = ContentKey.isValid(rawItem) ? ContentKey.of(rawItem) : key;
            String display = config.getString("display", key.getKey());

            float width = (float) config.getDouble("hitbox.width", 0.8);
            float height = (float) config.getDouble("hitbox.height", 0.8);
            boolean canSit = config.getBoolean("seating.can-sit", false);
            double sitOffset = config.getDouble("seating.sit-height-offset", 0.0);
            float rotStep = (float) config.getDouble("rotation-step", 90.0);

            FurnitureDefinition def = new FurnitureDefinition(key, itemKey, display, width, height, canSit, sitOffset, rotStep, null, null);
            registryManager.getFurniture().register(def);
        }, result);
    }

    private void loadCrops(ValidationResult result) {
        scanYamlFiles("crops", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) return;
            ContentKey key = ContentKey.of(rawId);
            String display = config.getString("display", key.getKey());
            ContentKey seed = ContentKey.of(config.getString("seed", rawId));
            int maxStage = config.getInt("max-stage", 3);
            double growthRate = config.getDouble("growth-rate", 0.2);
            int minLight = config.getInt("min-light", 9);
            boolean reusable = config.getBoolean("reusable", false);

            List<CustomBlock.BlockDrop> drops = parseDrops(config.getMapList("drops"));
            CropDefinition def = new CropDefinition(key, display, seed, maxStage, Collections.emptyMap(), growthRate, minLight, drops, reusable);
            registryManager.getCrops().register(def);
        }, result);
    }

    private void loadTrees(ValidationResult result) {
        scanYamlFiles("trees", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) return;
            ContentKey key = ContentKey.of(rawId);
            String display = config.getString("display", key.getKey());
            ContentKey sapling = ContentKey.of(config.getString("sapling", rawId));
            ContentKey log = ContentKey.of(config.getString("log", "modeladder:oak_log"));
            ContentKey leaves = ContentKey.of(config.getString("leaves", "modeladder:oak_leaves"));

            TreeDefinition def = new TreeDefinition(key, display, sapling, log, leaves, 4, 7, 2);
            registryManager.getTrees().register(def);
        }, result);
    }

    private void loadMobs(ValidationResult result) {
        scanYamlFiles("mobs", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) return;
            ContentKey key = ContentKey.of(rawId);
            String display = config.getString("display", key.getKey());
            EntityType entityType = EntityType.valueOf(config.getString("entity-type", "ZOMBIE").toUpperCase());
            double health = config.getDouble("health", 20.0);
            double speed = config.getDouble("speed", 0.23);
            double damage = config.getDouble("damage", 3.0);

            MobDefinition def = new MobDefinition(key, display, entityType, health, speed, damage, null, null, Collections.emptyList(), null);
            registryManager.getMobs().register(def);
        }, result);
    }

    private void loadRecipes(ValidationResult result) {
        scanYamlFiles("recipes", (file, config) -> {
            String rawId = config.getString("id");
            if (!ContentKey.isValid(rawId)) return;
            ContentKey key = ContentKey.of(rawId);
            String display = config.getString("display", key.getKey());
            ContentKey resItem = ContentKey.of(config.getString("result.item", "modeladder:ruby_sword"));
            int amount = config.getInt("result.amount", 1);

            RecipeDefinition def = new RecipeDefinition(key, display, CustomRecipe.RecipeType.SHAPED, resItem, amount,
                Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(), 200, 0.7f);
            registryManager.getRecipes().register(def);
        }, result);
    }

    private List<CustomBlock.BlockDrop> parseDrops(List<Map<?, ?>> list) {
        List<CustomBlock.BlockDrop> drops = new ArrayList<>();
        if (list == null) return drops;
        for (Map<?, ?> map : list) {
            String item = String.valueOf(map.get("item"));
            if (ContentKey.isValid(item)) {
                int min = map.containsKey("min") ? ((Number) map.get("min")).intValue() : 1;
                int max = map.containsKey("max") ? ((Number) map.get("max")).intValue() : 1;
                double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 1.0;
                drops.add(new CustomBlock.BlockDrop(ContentKey.of(item), min, max, chance));
            }
        }
        return drops;
    }

    private void scanYamlFiles(String subfolder, YamlConsumer consumer, ValidationResult result) {
        Path dir = contentRoot.resolve(subfolder);
        if (!Files.exists(dir)) return;

        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                .forEach(path -> {
                    try {
                        File file = path.toFile();
                        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                        consumer.accept(file, config);
                    } catch (Exception e) {
                        result.addError(ErrorCode.CONFIG_LOAD_ERROR, "Failed parsing " + path.getFileName() + ": " + e.getMessage(), path.toString());
                    }
                });
        } catch (IOException e) {
            result.addError(ErrorCode.CONFIG_LOAD_ERROR, "Failed scanning folder " + subfolder + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface YamlConsumer {
        void accept(File file, FileConfiguration config);
    }
}
