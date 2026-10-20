package com.modeladder.api.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates an error discovered during validation of archives, JSON models, or content definitions.
 */
public record ValidationError(
    @NotNull ErrorCode code,
    @NotNull String message,
    @Nullable String sourcePath,
    @Nullable String field
) {
    public ValidationError(@NotNull ErrorCode code, @NotNull String message) {
        this(code, message, null, null);
    }

    public ValidationError(@NotNull ErrorCode code, @NotNull String message, @Nullable String sourcePath) {
        this(code, message, sourcePath, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(code.name()).append("] ").append(message);
        if (sourcePath != null) {
            sb.append(" (at ").append(sourcePath);
            if (field != null) {
                sb.append(" # ").append(field);
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
