package com.modeladder.api.event;

import com.modeladder.api.content.CustomFurniture;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when custom furniture is placed in the world.
 */
public class CustomFurniturePlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Location location;
    private final CustomFurniture furniture;
    private final Player player;
    private boolean cancelled = false;

    public CustomFurniturePlaceEvent(@NotNull Location location, @NotNull CustomFurniture furniture, @Nullable Player player) {
        this.location = location;
        this.furniture = furniture;
        this.player = player;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    @NotNull
    public CustomFurniture getFurniture() {
        return furniture;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
