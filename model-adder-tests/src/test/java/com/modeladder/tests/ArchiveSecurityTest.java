package com.modeladder.tests;

import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.pack.security.ArchiveSecurityChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Archive Security and Anti-ZipSlip Tests")
class ArchiveSecurityTest {

    @Test
    @DisplayName("Rejects Zip Slip path traversal entries containing '../'")
    void testZipSlipDetection(@TempDir Path tempDir) throws IOException {
        File zipFile = tempDir.resolve("malicious-slip.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("../evil.txt");
            zos.putNextEntry(entry);
            zos.write("malicious payload".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ArchiveSecurityChecker checker = new ArchiveSecurityChecker();
        ValidationResult result = checker.validateArchive(zipFile, tempDir.resolve("extract"));

        assertFalse(result.isValid(), "Archive with '../' must be rejected");
        assertTrue(result.getErrors().stream().anyMatch(e -> e.code() == ErrorCode.PACK_UNSAFE_PATH));
    }

    @Test
    @DisplayName("Rejects absolute path entries starting with '/'")
    void testAbsolutePathDetection(@TempDir Path tempDir) throws IOException {
        File zipFile = tempDir.resolve("malicious-abs.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("/etc/passwd");
            zos.putNextEntry(entry);
            zos.write("payload".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ArchiveSecurityChecker checker = new ArchiveSecurityChecker();
        ValidationResult result = checker.validateArchive(zipFile, tempDir.resolve("extract"));

        assertFalse(result.isValid(), "Archive with leading slash must be rejected");
        assertTrue(result.getErrors().stream().anyMatch(e -> e.code() == ErrorCode.PACK_UNSAFE_PATH));
    }

    @Test
    @DisplayName("Accepts normal, safe resource pack archive")
    void testSafeArchiveAllowed(@TempDir Path tempDir) throws IOException {
        File zipFile = tempDir.resolve("safe-pack.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("assets/modeladder/models/item/ruby_sword.json");
            zos.putNextEntry(entry);
            zos.write("{\"parent\": \"item/generated\"}".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ArchiveSecurityChecker checker = new ArchiveSecurityChecker();
        ValidationResult result = checker.validateArchive(zipFile, tempDir.resolve("extract"));

        assertTrue(result.isValid(), "Safe archive must pass security validation");
        assertTrue(result.getErrors().isEmpty());
    }
}
