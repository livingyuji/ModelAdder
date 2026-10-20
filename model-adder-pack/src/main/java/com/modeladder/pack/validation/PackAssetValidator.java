package com.modeladder.pack.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Validates an entire staged resource pack directory structure before building.
 */
public final class PackAssetValidator {

    @NotNull
    public static ValidationResult validatePackDirectory(@NotNull Path packRoot) {
        ValidationResult result = ValidationResult.ok();

        // 1. Validate pack.mcmeta
        Path mcmeta = packRoot.resolve("pack.mcmeta");
        if (!Files.exists(mcmeta)) {
            result.addError(ErrorCode.PACK_FORMAT_UNSUPPORTED, "pack.mcmeta is missing from the resource pack root");
        } else {
            ValidationResult jsonRes = JsonValidator.validateJson(mcmeta.toFile());
            if (!jsonRes.isValid()) {
                result.merge(jsonRes);
            } else {
                try (FileReader reader = new FileReader(mcmeta.toFile(), StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!obj.has("pack") || !obj.getAsJsonObject("pack").has("pack_format")) {
                        result.addError(ErrorCode.PACK_FORMAT_UNSUPPORTED, "pack.mcmeta does not declare pack.pack_format");
                    }
                } catch (Exception e) {
                    result.addError(ErrorCode.PACK_FORMAT_UNSUPPORTED, "Failed reading pack.mcmeta: " + e.getMessage());
                }
            }
        }

        // 2. Validate assets directory
        Path assetsDir = packRoot.resolve("assets");
        if (!Files.exists(assetsDir) || !Files.isDirectory(assetsDir)) {
            result.addWarning("No 'assets' directory found in pack staging root");
            return result;
        }

        // 3. Scan all model JSON files
        try (Stream<Path> stream = Files.walk(assetsDir)) {
            stream.filter(p -> p.toString().endsWith(".json") && p.toString().contains("models"))
                .forEach(modelPath -> {
                    ValidationResult modelRes = ModelValidator.validateModel(modelPath.toFile(), assetsDir);
                    result.merge(modelRes);
                });
        } catch (IOException e) {
            result.addError(ErrorCode.JSON_PARSE_ERROR, "Error reading assets directory: " + e.getMessage());
        }

        return result;
    }
}
