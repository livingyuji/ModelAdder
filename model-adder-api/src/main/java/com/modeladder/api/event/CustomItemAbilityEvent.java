package com.modeladder.api.event;

import com.modeladder.api.content.CustomAbility;
import com.modeladder.api.content.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before a custom ability is executed from an item.
 */
public class CustomItemAbilityEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final CustomItem customItem;
    private final CustomAbility ability;
    private final ItemStack itemStack;
    private boolean cancelled = false;

    public CustomItemAbilityEvent(@NotNull Player player, @NotNull CustomItem customItem, @NotNull CustomAbility ability, @NotNull ItemStack itemStack) {
        this.player = player;
        this.customItem = customItem;
        this.ability = ability;
        this.itemStack = itemStack;
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
    public CustomAbility getAbility() {
        return ability;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
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
