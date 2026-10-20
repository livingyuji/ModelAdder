package com.modeladder.pack.validation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Validates Minecraft model JSON files: parent hierarchy and texture references.
 */
public final class ModelValidator {

    private static final Set<String> BUILTIN_PARENTS = Set.of(
        "builtin/generated", "builtin/entity",
        "minecraft:builtin/generated", "minecraft:builtin/entity",
        "item/generated", "item/handheld", "item/handheld_rod",
        "minecraft:item/generated", "minecraft:item/handheld", "minecraft:item/handheld_rod",
        "block/block", "block/cube", "block/cube_all", "block/cube_column", "block/cube_directional",
        "minecraft:block/block", "minecraft:block/cube", "minecraft:block/cube_all", "minecraft:block/cube_column"
    );

    /**
     * Validates a model file against the resource pack asset root.
     */
    @NotNull
    public static ValidationResult validateModel(@NotNull File modelFile, @NotNull Path assetsDir) {
        ValidationResult result = JsonValidator.validateJson(modelFile);
        if (!result.isValid()) {
            return result;
        }

        try (FileReader reader = new FileReader(modelFile, StandardCharsets.UTF_8)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            // 1. Check parent reference
            if (obj.has("parent")) {
                String parent = obj.get("parent").getAsString();
                if (!BUILTIN_PARENTS.contains(parent)) {
                    // Custom parent reference: check if file exists
                    Path parentFile = resolveAssetPath(assetsDir, parent, "models", ".json");
                    if (parentFile == null || !parentFile.toFile().exists()) {
                        result.addError(ErrorCode.MODEL_PARENT_MISSING,
                            "Parent model '" + parent + "' referenced in " + modelFile.getName() + " was not found.",
                            modelFile.getPath(), "parent");
                    }
                }
            }

            // 2. Check texture references
            if (obj.has("textures") && obj.get("textures").isJsonObject()) {
                JsonObject textures = obj.getAsJsonObject("textures");
                for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                    String texturePath = entry.getValue().getAsString();
                    if (texturePath.startsWith("#")) {
                        // Variable reference (e.g. #all or #layer0)
                        continue;
                    }
                    if (texturePath.startsWith("minecraft:")) {
                        // Vanilla texture reference - allowed
                        continue;
                    }

                    Path textureFile = resolveAssetPath(assetsDir, texturePath, "textures", ".png");
                    if (textureFile == null || !textureFile.toFile().exists()) {
                        result.addError(ErrorCode.TEXTURE_NOT_FOUND,
                            "Texture '" + texturePath + "' referenced in " + modelFile.getName() + " was not found.",
                            modelFile.getPath(), "textures." + entry.getKey());
                    }
                }
            }
        } catch (Exception e) {
            result.addError(ErrorCode.JSON_PARSE_ERROR, "Failed to analyze model " + modelFile.getName() + ": " + e.getMessage(), modelFile.getPath());
        }

        return result;
    }

    /**
     * Resolves namespaced asset paths (e.g. "modeladder:item/ruby_sword" in "textures" -> assets/modeladder/textures/item/ruby_sword.png).
     */
    public static Path resolveAssetPath(@NotNull Path assetsDir, @NotNull String rawPath, @NotNull String category, @NotNull String extension) {
        String namespace = "modeladder";
        String path = rawPath;

        int colon = rawPath.indexOf(':');
        if (colon != -1) {
            namespace = rawPath.substring(0, colon);
            path = rawPath.substring(colon + 1);
        }

        return assetsDir.resolve(namespace).resolve(category).resolve(path + extension);
    }
}
