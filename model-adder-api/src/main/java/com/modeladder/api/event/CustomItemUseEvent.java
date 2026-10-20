package com.modeladder.api.event;

import com.modeladder.api.content.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player uses or interacts with a custom item.
 */
public class CustomItemUseEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final CustomItem customItem;
    private final ItemStack itemStack;
    private final Action action;
    private boolean cancelled = false;

    public CustomItemUseEvent(@NotNull Player player, @NotNull CustomItem customItem, @NotNull ItemStack itemStack, @NotNull Action action) {
        this.player = player;
        this.customItem = customItem;
        this.itemStack = itemStack;
        this.action = action;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public CustomItem getCustomItem() {
        return customItem;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    @NotNull
    public Action getAction() {
        return action;
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
