package com.modeladder.api.pack;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Public interface for managing resource pack builds, hosting, and player delivery.
 */
public interface PackManager {

    /**
     * Triggers a new build of the resource pack asynchronously.
     *
     * @return future containing build outcome
     */
    @NotNull
    CompletableFuture<PackBuildResult> buildPack();

    /**
     * @return current active pack build ID, or empty if never built.
     */
    @NotNull
    Optional<String> getCurrentBuildId();

    /**
     * @return SHA-1 hash of the currently published pack, or empty if not available.
     */
    @NotNull
    Optional<String> getCurrentSha1();

    /**
     * @return the currently published pack file, if available.
     */
    @NotNull
    Optional<File> getPublishedPackFile();

    /**
     * @return the reachable URL from which clients can download the pack.
     */
    @NotNull
    Optional<String> getPublicDownloadUrl();

    /**
     * Sends the current resource pack to a player.
     *
     * @param player target player
     * @return true if pack prompt was dispatched
     */
    boolean sendPack(@NotNull Player player);

    /**
     * Rolls back to a previously saved valid build ID.
     *
     * @param buildId build identifier
     * @return true if rollback succeeded
     */
    boolean rollbackTo(@NotNull String buildId);

    /**
     * @return list of stored backup build IDs.
     */
    @NotNull
    List<String> getAvailableBackups();

    record PackBuildResult(boolean success, @Nullable String buildId, @Nullable String sha1, long sizeBytes, @Nullable String errorMessage) {}
}
