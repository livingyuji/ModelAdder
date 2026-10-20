package com.modeladder.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player's resource pack delivery status updates (accepted, declined, downloaded, failed).
 */
public class ResourcePackDeliveryEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final DeliveryStatus status;

    public ResourcePackDeliveryEvent(@NotNull Player player, @NotNull DeliveryStatus status) {
        this.player = player;
        this.status = status;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public DeliveryStatus getStatus() {
        return status;
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

    public enum DeliveryStatus {
        ACCEPTED,
        DECLINED,
        FAILED_DOWNLOAD,
        SUCCESSFULLY_LOADED
    }
}
