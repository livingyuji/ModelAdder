package com.modeladder.editor.gui;

import com.modeladder.api.ModelAdderAPI;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.registry.RegistryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Paginated browser GUI displaying custom items with click-to-give functionality.
 */
public final class ItemBrowserGui implements InventoryHolder {

    private final Inventory inventory;
    private final List<CustomItem> items;
    private int page = 0;

    public ItemBrowserGui(@NotNull RegistryManager registryManager) {
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Model Adder — Items", NamedTextColor.GOLD));
        this.items = new ArrayList<>(registryManager.getItems().getAll());
        renderPage();
    }

    private void renderPage() {
        inventory.clear();
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            final int slot = i - startIndex;
            CustomItem itemDef = items.get(i);
            Optional<ItemStack> stackOpt = ModelAdderAPI.get().createItemStack(itemDef.getKey(), 1);
            stackOpt.ifPresent(itemStack -> inventory.setItem(slot, itemStack));
        }
    }

    public void open(@NotNull Player player) {
        player.openInventory(inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
