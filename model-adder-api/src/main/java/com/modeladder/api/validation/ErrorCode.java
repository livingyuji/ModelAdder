package com.modeladder.api.validation;

import org.jetbrains.annotations.NotNull;

/**
 * Standard error codes across Model Adder validation, resource packs, and content definitions.
 */
public enum ErrorCode {
    // Resource Pack & Archive Errors
    PACK_INVALID_ZIP("Malformed or corrupted ZIP archive"),
    PACK_UNSAFE_PATH("Unsafe entry path in archive (e.g. traversal attempt with '../')"),
    PACK_ARCHIVE_TOO_LARGE("Archive exceeds maximum allowed byte size"),
    PACK_EXPANDED_TOO_LARGE("Extracted content exceeds safe uncompressed size"),
    PACK_TOO_MANY_ENTRIES("Archive exceeds maximum allowed entry count"),
    PACK_FORMAT_UNSUPPORTED("pack.mcmeta format is invalid or unsupported"),
    PACK_HOST_UNREACHABLE("Resource-pack hosting server or external URL is unreachable"),
    PACK_HASH_MISMATCH("Generated pack SHA-1 hash does not match expected value"),

    // JSON & Model Validation Errors
    JSON_PARSE_ERROR("Failed to parse JSON file"),
    MODEL_PARENT_MISSING("Referenced parent model does not exist"),
    TEXTURE_NOT_FOUND("Referenced texture file does not exist in pack assets"),
    ASSET_CONFLICT("Conflicting asset definitions found with same target path"),

    // Content Definition Errors
    INVALID_ID_FORMAT("Namespaced ID does not match valid namespace:key format"),
    DUPLICATE_ID("Content with this ID is already registered"),
    INVALID_MATERIAL("Referenced Bukkit Material does not exist"),
    MISSING_REQUIRED_FIELD("Mandatory field in YAML definition is missing"),
    INVALID_COOLDOWN("Cooldown seconds must be non-negative"),
    INVALID_RECIPE_INGREDIENT("Ingredient key in recipe is not a valid material or registered item"),
    UNRESOLVED_REFERENCE("Referenced content ID does not exist in registry"),
    CONFIG_LOAD_ERROR("Error loading or reading configuration file");

    private final String description;

    ErrorCode(@NotNull String description) {
        this.description = description;
    }

    @NotNull
    public String getDescription() {
        return description;
    }
}
