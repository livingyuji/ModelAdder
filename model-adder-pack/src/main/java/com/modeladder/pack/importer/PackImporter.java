package com.modeladder.pack.importer;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.pack.security.ArchiveSecurityChecker;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Safely extracts imported resource pack ZIP archives into the staging/assets directory.
 */
public final class PackImporter {

    private final ArchiveSecurityChecker securityChecker;

    public PackImporter(@NotNull ArchiveSecurityChecker securityChecker) {
        this.securityChecker = securityChecker;
    }

    public PackImporter() {
        this(new ArchiveSecurityChecker());
    }

    /**
     * Imports and extracts a zip file into the target directory after rigorous security checks.
     */
    @NotNull
    public ValidationResult importArchive(@NotNull File zipFile, @NotNull Path targetDirectory, boolean overwrite) {
        ValidationResult securityResult = securityChecker.validateArchive(zipFile, targetDirectory);
        if (!securityResult.isValid()) {
            return securityResult;
        }

        Path canonicalTarget = targetDirectory.toAbsolutePath().normalize();

        try {
            Files.createDirectories(canonicalTarget);
        } catch (IOException e) {
            return ValidationResult.ofError(ErrorCode.PACK_INVALID_ZIP, "Failed to create destination directory: " + e.getMessage());
        }

        try (FileInputStream fis = new FileInputStream(zipFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                Path entryPath = canonicalTarget.resolve(entryName).normalize();

                // Re-verify canonical destination boundary
                if (!entryPath.startsWith(canonicalTarget)) {
                    return ValidationResult.ofError(ErrorCode.PACK_UNSAFE_PATH, "Target path escape prevented: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    if (Files.exists(entryPath) && !overwrite) {
                        zis.closeEntry();
                        continue;
                    }

                    Path parent = entryPath.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }

                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            return ValidationResult.ofError(ErrorCode.PACK_INVALID_ZIP, "Failed extracting archive: " + e.getMessage());
        }

        return ValidationResult.ok();
    }
}
