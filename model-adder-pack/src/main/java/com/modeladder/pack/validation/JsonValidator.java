package com.modeladder.pack.validation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Validates JSON files (blockstates, models, font providers, pack.mcmeta).
 */
public final class JsonValidator {

    @NotNull
    public static ValidationResult validateJson(@NotNull File file) {
        ValidationResult result = ValidationResult.ok();

        if (!file.exists() || !file.isFile()) {
            result.addError(ErrorCode.JSON_PARSE_ERROR, "File does not exist: " + file.getPath(), file.getName());
            return result;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element == null || element.isJsonNull()) {
                result.addError(ErrorCode.JSON_PARSE_ERROR, "JSON file is empty or null", file.getPath());
            }
        } catch (JsonSyntaxException e) {
            result.addError(ErrorCode.JSON_PARSE_ERROR, "Syntax error in JSON: " + e.getMessage(), file.getPath());
        } catch (IOException e) {
            result.addError(ErrorCode.JSON_PARSE_ERROR, "I/O error reading JSON file: " + e.getMessage(), file.getPath());
        }

        return result;
    }

    @NotNull
    public static ValidationResult validateJsonContent(@NotNull String content, @NotNull String sourceIdentifier) {
        ValidationResult result = ValidationResult.ok();
        try {
            JsonElement element = JsonParser.parseString(content);
            if (element == null || element.isJsonNull()) {
                result.addError(ErrorCode.JSON_PARSE_ERROR, "JSON content is empty", sourceIdentifier);
            }
        } catch (JsonSyntaxException e) {
            result.addError(ErrorCode.JSON_PARSE_ERROR, "Syntax error in JSON: " + e.getMessage(), sourceIdentifier);
        }
        return result;
    }
}
