package com.modeladder.pack;

import com.modeladder.api.pack.PackManager;
import com.modeladder.pack.builder.HashUtil;
import com.modeladder.pack.builder.PackBuilder;
import com.modeladder.pack.hosting.BuiltInHttpServer;
import com.modeladder.pack.hosting.ExternalHostingAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of PackManager orchestrating building, hosting, rollback, and player delivery.
 */
public final class DefaultPackManager implements PackManager {

    private final Path pluginDataFolder;
    private final Path stagingDir;
    private final Path buildsDir;
    private final Path backupsDir;
    private final File publishedPackFile;
    private final Logger logger;

    private boolean builtInHostingEnabled = true;
    private String bindAddress = "0.0.0.0";
    private int httpPort = 8085;
    private String publicUrl = "";

    private boolean externalHostingEnabled = false;
    private String externalUrl = "";
    private String externalExpectedSha1 = "";

    private BuiltInHttpServer httpServer;
    private ExternalHostingAdapter externalAdapter;

    private volatile String currentBuildId;
    private volatile String currentSha1;

    public DefaultPackManager(@NotNull Path pluginDataFolder, @NotNull Logger logger) {
        this.pluginDataFolder = pluginDataFolder;
        this.logger = logger;

        Path packBase = pluginDataFolder.resolve("resourcepack");
        this.stagingDir = packBase.resolve("staging");
        this.buildsDir = packBase.resolve("builds");
        this.backupsDir = packBase.resolve("backups");
        this.publishedPackFile = packBase.resolve("published").resolve("pack.zip").toFile();

        // Check if a published pack exists on startup
        if (publishedPackFile.exists()) {
            try {
                this.currentSha1 = HashUtil.calculateSha1(publishedPackFile);
                this.currentBuildId = "existing-" + Long.toHexString(publishedPackFile.lastModified());
            } catch (IOException ignored) {}
        }
    }

    public void configureHosting(
        boolean builtInEnabled,
        @NotNull String bindAddress,
        int httpPort,
        @NotNull String publicUrl,
        boolean externalEnabled,
        @NotNull String externalUrl,
        @Nullable String externalSha1
    ) {
        this.builtInHostingEnabled = builtInEnabled;
        this.bindAddress = bindAddress;
        this.httpPort = httpPort;
        this.publicUrl = publicUrl;

        this.externalHostingEnabled = externalEnabled;
        this.externalUrl = externalUrl;
        this.externalExpectedSha1 = (externalSha1 != null) ? externalSha1 : "";

        if (builtInEnabled) {
            if (httpServer != null) {
                httpServer.stop();
            }
            httpServer = new BuiltInHttpServer(bindAddress, httpPort, publishedPackFile, logger);
            httpServer.start();
        } else if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }

        if (externalEnabled && !externalUrl.isBlank()) {
            externalAdapter = new ExternalHostingAdapter(externalUrl, externalSha1);
        } else {
            externalAdapter = null;
        }
    }

    public void shutdown() {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }
    }

    @Override
    @NotNull
    public CompletableFuture<PackBuildResult> buildPack() {
        return CompletableFuture.supplyAsync(() -> {
            PackBuilder builder = new PackBuilder(stagingDir, buildsDir, backupsDir, publishedPackFile, 10);
            PackBuildResult result = builder.build();

            if (result.success()) {
                this.currentBuildId = result.buildId();
                this.currentSha1 = result.sha1();
                logger.info("[ModelAdder] Resource pack build succeeded: " + result.buildId() + " (SHA1: " + result.sha1() + ")");
            } else {
                logger.warning("[ModelAdder] Resource pack build failed: " + result.errorMessage());
            }

            return result;
        });
    }

    @Override
    @NotNull
    public Optional<String> getCurrentBuildId() {
        return Optional.ofNullable(currentBuildId);
    }

    @Override
    @NotNull
    public Optional<String> getCurrentSha1() {
        return Optional.ofNullable(currentSha1);
    }

    @Override
    @NotNull
    public Optional<File> getPublishedPackFile() {
        return (publishedPackFile.exists() && publishedPackFile.isFile()) ? Optional.of(publishedPackFile) : Optional.empty();
    }

    @Override
    @NotNull
    public Optional<String> getPublicDownloadUrl() {
        if (externalHostingEnabled && !externalUrl.isBlank()) {
            return Optional.of(externalUrl);
        }
        if (builtInHostingEnabled && !publicUrl.isBlank()) {
            String url = publicUrl.endsWith("/") ? publicUrl + "pack.zip" : publicUrl;
            return Optional.of(url);
        }
        return Optional.empty();
    }

    @Override
    public boolean sendPack(@NotNull Player player) {
        Optional<String> downloadUrlOpt = getPublicDownloadUrl();
        if (downloadUrlOpt.isEmpty()) {
            return false;
        }

        String downloadUrl = downloadUrlOpt.get();
        String sha1 = (externalHostingEnabled && !externalExpectedSha1.isBlank()) ? externalExpectedSha1 : currentSha1;

        if (sha1 != null && sha1.length() == 40) {
            byte[] hashBytes = new byte[20];
            for (int i = 0; i < 40; i += 2) {
                hashBytes[i / 2] = (byte) ((Character.digit(sha1.charAt(i), 16) << 4)
                    + Character.digit(sha1.charAt(i + 1), 16));
            }
            player.setResourcePack(downloadUrl, hashBytes);
        } else {
            player.setResourcePack(downloadUrl);
        }

        return true;
    }

    @Override
    public boolean rollbackTo(@NotNull String buildId) {
        Path backupFile = backupsDir.resolve(buildId.endsWith(".zip") ? buildId : buildId + ".zip");
        if (!Files.exists(backupFile)) {
            return false;
        }

        try {
            Files.copy(backupFile, publishedPackFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.currentSha1 = HashUtil.calculateSha1(publishedPackFile);
            this.currentBuildId = buildId;
            logger.info("[ModelAdder] Successfully rolled back resource pack to build: " + buildId);
            return true;
        } catch (IOException e) {
            logger.severe("[ModelAdder] Failed rollback to build " + buildId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    @NotNull
    public List<String> getAvailableBackups() {
        if (!Files.exists(backupsDir)) {
            return Collections.emptyList();
        }

        try (Stream<Path> stream = Files.list(backupsDir)) {
            return stream.filter(p -> p.toString().endsWith(".zip"))
                .map(p -> p.getFileName().toString().replace(".zip", ""))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Path getStagingDir() {
        return stagingDir;
    }
}
