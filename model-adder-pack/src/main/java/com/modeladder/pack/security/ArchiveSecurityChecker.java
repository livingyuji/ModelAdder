package com.modeladder.pack.security;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationError;
import com.modeladder.api.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Security validator for resource-pack zip archives to defend against Zip-Slip,
 * Zip-Bombs, path traversals, and excessive archive sizes.
 */
public final class ArchiveSecurityChecker {

    // Default safety limits
    public static final long DEFAULT_MAX_ARCHIVE_BYTES = 100 * 1024 * 1024; // 100 MB compressed
    public static final long DEFAULT_MAX_EXPANDED_BYTES = 250 * 1024 * 1024; // 250 MB extracted
    public static final int DEFAULT_MAX_ENTRIES = 10_000;
    public static final double DEFAULT_MAX_COMPRESSION_RATIO = 20.0; // 20:1 max ratio

    private final long maxArchiveBytes;
    private final long maxExpandedBytes;
    private final int maxEntries;
    private final double maxCompressionRatio;

    public ArchiveSecurityChecker(long maxArchiveBytes, long maxExpandedBytes, int maxEntries, double maxCompressionRatio) {
        this.maxArchiveBytes = maxArchiveBytes;
        this.maxExpandedBytes = maxExpandedBytes;
        this.maxEntries = maxEntries;
        this.maxCompressionRatio = maxCompressionRatio;
    }

    public ArchiveSecurityChecker() {
        this(DEFAULT_MAX_ARCHIVE_BYTES, DEFAULT_MAX_EXPANDED_BYTES, DEFAULT_MAX_ENTRIES, DEFAULT_MAX_COMPRESSION_RATIO);
    }

    /**
     * Inspects a ZIP archive file before extraction without writing any untrusted content to disk.
     */
    @NotNull
    public ValidationResult validateArchive(@NotNull File zipFile, @NotNull Path destinationDir) {
        ValidationResult result = ValidationResult.ok();

        if (!zipFile.exists() || !zipFile.isFile()) {
            result.addError(ErrorCode.PACK_INVALID_ZIP, "Target archive file does not exist: " + zipFile.getName());
            return result;
        }

        if (zipFile.length() > maxArchiveBytes) {
            result.addError(ErrorCode.PACK_ARCHIVE_TOO_LARGE,
                "Archive size (" + (zipFile.length() / (1024 * 1024)) + " MB) exceeds limit of " + (maxArchiveBytes / (1024 * 1024)) + " MB");
            return result;
        }

        Path canonicalDest = destinationDir.toAbsolutePath().normalize();
        long totalExtractedBytes = 0;
        int totalEntries = 0;

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                totalEntries++;

                if (totalEntries > maxEntries) {
                    result.addError(ErrorCode.PACK_TOO_MANY_ENTRIES,
                        "Archive contains more than " + maxEntries + " entries (possible denial-of-service attack)");
                    return result;
                }

                String entryName = entry.getName();

                // 1. Path traversal & Zip-Slip defense
                if (entryName.contains("../") || entryName.contains("..\\") || entryName.startsWith("/") || entryName.startsWith("\\")) {
                    result.addError(ErrorCode.PACK_UNSAFE_PATH,
                        "Archive contains prohibited path traversal entry: " + entryName, entryName);
                    return result;
                }

                Path resolvedPath = canonicalDest.resolve(entryName).normalize();
                if (!resolvedPath.startsWith(canonicalDest)) {
                    result.addError(ErrorCode.PACK_UNSAFE_PATH,
                        "Archive entry would extract outside destination target directory: " + entryName, entryName);
                    return result;
                }

                // 2. Uncompressed size tracking & Zip-Bomb check
                long entrySize = entry.getSize();
                if (entrySize > 0) {
                    totalExtractedBytes += entrySize;
                    if (totalExtractedBytes > maxExpandedBytes) {
                        result.addError(ErrorCode.PACK_EXPANDED_TOO_LARGE,
                            "Total extracted content exceeds safe limit of " + (maxExpandedBytes / (1024 * 1024)) + " MB");
                        return result;
                    }

                    long compressedSize = entry.getCompressedSize();
                    if (compressedSize > 0) {
                        double ratio = (double) entrySize / compressedSize;
                        if (ratio > maxCompressionRatio && entrySize > 1024 * 1024) {
                            result.addError(ErrorCode.PACK_INVALID_ZIP,
                                "Suspicious compression ratio (" + String.format("%.1f", ratio) + ":1) detected for entry: " + entryName);
                            return result;
                        }
                    }
                }
            }
        } catch (IOException e) {
            result.addError(ErrorCode.PACK_INVALID_ZIP, "Failed to read or parse ZIP file: " + e.getMessage());
        }

        return result;
    }
}
