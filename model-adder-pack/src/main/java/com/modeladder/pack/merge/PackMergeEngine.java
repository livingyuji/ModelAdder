package com.modeladder.pack.merge;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Merges imported assets and plugin-generated assets into a unified staging tree.
 */
public final class PackMergeEngine {

    public enum ConflictPolicy {
        OVERWRITE,
        KEEP_EXISTING,
        FAIL_ON_CONFLICT
    }

    private final ConflictPolicy policy;
    private final List<String> detectedConflicts = new ArrayList<>();

    public PackMergeEngine(@NotNull ConflictPolicy policy) {
        this.policy = policy;
    }

    public PackMergeEngine() {
        this(ConflictPolicy.OVERWRITE);
    }

    /**
     * Merges source directory into destination directory.
     */
    @NotNull
    public ValidationResult merge(@NotNull Path sourceDir, @NotNull Path destinationDir) {
        ValidationResult result = ValidationResult.ok();
        detectedConflicts.clear();

        if (!Files.exists(sourceDir)) {
            return result;
        }

        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream.forEach(sourcePath -> {
                if (Files.isDirectory(sourcePath)) {
                    return;
                }

                Path relative = sourceDir.relativize(sourcePath);
                Path target = destinationDir.resolve(relative);

                try {
                    if (Files.exists(target)) {
                        detectedConflicts.add(relative.toString());
                        if (policy == ConflictPolicy.FAIL_ON_CONFLICT) {
                            result.addError(ErrorCode.ASSET_CONFLICT,
                                "Asset conflict detected on path: " + relative, relative.toString());
                            return;
                        } else if (policy == ConflictPolicy.KEEP_EXISTING) {
                            result.addWarning("Preserved existing asset: " + relative);
                            return;
                        }
                    }

                    Path parent = target.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }

                    Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    result.addError(ErrorCode.ASSET_CONFLICT,
                        "Failed merging file " + relative + ": " + e.getMessage(), relative.toString());
                }
            });
        } catch (IOException e) {
            result.addError(ErrorCode.ASSET_CONFLICT, "Merge engine failed walking directory: " + e.getMessage());
        }

        return result;
    }

    @NotNull
    public List<String> getDetectedConflicts() {
        return List.copyOf(detectedConflicts);
    }
}
