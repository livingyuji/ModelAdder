package com.modeladder.api.event;

import com.modeladder.api.content.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a custom block is broken.
 */
public class CustomBlockBreakEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Block block;
    private final CustomBlock customBlock;
    private final Player player;
    private boolean cancelled = false;
    private boolean dropItems = true;

    public CustomBlockBreakEvent(@NotNull Block block, @NotNull CustomBlock customBlock, @Nullable Player player) {
        this.block = block;
        this.customBlock = customBlock;
        this.player = player;
    }

    @NotNull
    public Block getBlock() {
        return block;
    }

    @NotNull
    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public boolean isDropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
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
