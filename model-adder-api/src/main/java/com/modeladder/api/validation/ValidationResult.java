package com.modeladder.api.validation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of any validation routine, holding collected errors and non-fatal warnings.
 */
public final class ValidationResult {

    private final List<ValidationError> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    private ValidationResult() {}

    @NotNull
    public static ValidationResult ok() {
        return new ValidationResult();
    }

    @NotNull
    public static ValidationResult ofError(@NotNull ErrorCode code, @NotNull String message) {
        ValidationResult result = new ValidationResult();
        result.addError(code, message);
        return result;
    }

    public void addError(@NotNull ErrorCode code, @NotNull String message) {
        errors.add(new ValidationError(code, message));
    }

    public void addError(@NotNull ErrorCode code, @NotNull String message, @NotNull String sourcePath) {
        errors.add(new ValidationError(code, message, sourcePath));
    }

    public void addError(@NotNull ErrorCode code, @NotNull String message, @NotNull String sourcePath, @NotNull String field) {
        errors.add(new ValidationError(code, message, sourcePath, field));
    }

    public void addError(@NotNull ValidationError error) {
        errors.add(error);
    }

    public void addWarning(@NotNull String warning) {
        warnings.add(warning);
    }

    public void merge(@NotNull ValidationResult other) {
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @NotNull
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @NotNull
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return warnings.isEmpty() ? "Validation OK" : "Validation OK with " + warnings.size() + " warnings";
        }
        return "Validation FAILED with " + errors.size() + " errors: " + errors;
    }
}
