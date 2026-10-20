package com.modeladder.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a resource pack build completes or fails.
 */
public class ResourcePackBuildEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String buildId;
    private final boolean success;
    private final String sha1Hash;
    private final long packSizeBytes;
    private final String errorMessage;

    public ResourcePackBuildEvent(@NotNull String buildId, boolean success, @Nullable String sha1Hash, long packSizeBytes, @Nullable String errorMessage) {
        super(true); // Asynchronous event since pack generation occurs off the main thread
        this.buildId = buildId;
        this.success = success;
        this.sha1Hash = sha1Hash;
        this.packSizeBytes = packSizeBytes;
        this.errorMessage = errorMessage;
    }

    @NotNull
    public String getBuildId() {
        return buildId;
    }

    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public String getSha1Hash() {
        return sha1Hash;
    }

    public long getPackSizeBytes() {
        return packSizeBytes;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
