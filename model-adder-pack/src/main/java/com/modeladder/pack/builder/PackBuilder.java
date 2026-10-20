package com.modeladder.pack.builder;

import com.google.gson.JsonObject;
import com.modeladder.api.pack.PackManager.PackBuildResult;
import com.modeladder.api.validation.ErrorCode;
import com.modeladder.api.validation.ValidationResult;
import com.modeladder.pack.security.ArchiveSecurityChecker;
import com.modeladder.pack.validation.PackAssetValidator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Deterministic resource pack builder for Minecraft 1.21.x / 1.21.11.
 * Ensures reproducible output, atomic publishing, and safety backups.
 */
public final class PackBuilder {

    private final Path stagingDir;
    private final Path buildsDir;
    private final Path backupsDir;
    private final File publishedPackFile;
    private final int maxBackups;

    public PackBuilder(
        @NotNull Path stagingDir,
        @NotNull Path buildsDir,
        @NotNull Path backupsDir,
        @NotNull File publishedPackFile,
        int maxBackups
    ) {
        this.stagingDir = stagingDir;
        this.buildsDir = buildsDir;
        this.backupsDir = backupsDir;
        this.publishedPackFile = publishedPackFile;
        this.maxBackups = maxBackups;
    }

    /**
     * Executes the complete pack build pipeline atomically.
     */
    @NotNull
    public synchronized PackBuildResult build() {
        String buildId = "build-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());

        try {
            Files.createDirectories(stagingDir);
            Files.createDirectories(buildsDir);
            Files.createDirectories(backupsDir);

            // 1. Ensure pack.mcmeta exists
            ensurePackMcmeta();

            // 2. Validate the staging directory tree
            ValidationResult validation = PackAssetValidator.validatePackDirectory(stagingDir);
            if (!validation.isValid()) {
                String firstErr = validation.getErrors().isEmpty() ? "Validation failed" : validation.getErrors().get(0).toString();
                return new PackBuildResult(false, buildId, null, 0, firstErr);
            }

            // 3. Collect and sort all files lexicographically for deterministic packaging
            List<Path> fileList;
            try (Stream<Path> stream = Files.walk(stagingDir)) {
                fileList = stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
            }

            if (fileList.isEmpty()) {
                return new PackBuildResult(false, buildId, null, 0, "No assets found in staging directory to build.");
            }

            // 4. Build ZIP file deterministically
            Path targetBuildZip = buildsDir.resolve(buildId + ".zip");
            try (OutputStream fos = Files.newOutputStream(targetBuildZip);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 ZipOutputStream zos = new ZipOutputStream(bos)) {

                byte[] buffer = new byte[8192];

                for (Path file : fileList) {
                    Path relative = stagingDir.relativize(file);
                    String zipPath = relative.toString().replace('\\', '/');

                    ZipEntry entry = new ZipEntry(zipPath);
                    // Fixed epoch time for deterministic output
                    entry.setTime(0L);
                    zos.putNextEntry(entry);

                    try (InputStream fis = Files.newInputStream(file)) {
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
            }

            // 5. Calculate SHA-1 hash
            String sha1 = HashUtil.calculateSha1(targetBuildZip.toFile());
            long packSize = Files.size(targetBuildZip);

            // 6. Test archive safety
            ArchiveSecurityChecker checker = new ArchiveSecurityChecker();
            ValidationResult secCheck = checker.validateArchive(targetBuildZip.toFile(), stagingDir);
            if (!secCheck.isValid()) {
                Files.deleteIfExists(targetBuildZip);
                return new PackBuildResult(false, buildId, null, 0, "Security validation on built pack failed: " + secCheck.getErrors());
            }

            // 7. Atomic publication: copy to temp file, then rename/replace published file
            File parentDir = publishedPackFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            Path tempPublished = publishedPackFile.toPath().resolveSibling(publishedPackFile.getName() + ".tmp");
            Files.copy(targetBuildZip, tempPublished, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempPublished, publishedPackFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            // 8. Create backup copy and rotate old backups
            Path backupTarget = backupsDir.resolve(buildId + ".zip");
            Files.copy(targetBuildZip, backupTarget, StandardCopyOption.REPLACE_EXISTING);
            rotateBackups();

            return new PackBuildResult(true, buildId, sha1, packSize, null);

        } catch (Exception e) {
            return new PackBuildResult(false, buildId, null, 0, "Build exception: " + e.getMessage());
        }
    }

    private void ensurePackMcmeta() throws IOException {
        Path mcmeta = stagingDir.resolve("pack.mcmeta");
        if (!Files.exists(mcmeta)) {
            JsonObject packObj = new JsonObject();
            JsonObject packMeta = new JsonObject();
            // Primary target Minecraft 1.21.x / 1.21.11: format 34 (with wide range 34-48)
            packMeta.addProperty("pack_format", 34);

            JsonObject supportedFormats = new JsonObject();
            supportedFormats.addProperty("min_format", 34);
            supportedFormats.addProperty("max_format", 48);
            packMeta.add("supported_formats", supportedFormats);

            packMeta.addProperty("description", "Model Adder Custom Content Resource Pack");
            packObj.add("pack", packMeta);

            Files.writeString(mcmeta, packObj.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void rotateBackups() {
        try (Stream<Path> stream = Files.list(backupsDir)) {
            List<Path> backups = stream.filter(p -> p.toString().endsWith(".zip"))
                .sorted(Comparator.comparingLong(p -> {
                    try {
                        return Files.getLastModifiedTime(p).toMillis();
                    } catch (IOException e) {
                        return 0L;
                    }
                }))
                .collect(Collectors.toList());

            while (backups.size() > maxBackups) {
                Path oldest = backups.remove(0);
                Files.deleteIfExists(oldest);
            }
        } catch (IOException ignored) {}
    }
}
