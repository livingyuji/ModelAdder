package com.modeladder.editor.listener;

import com.modeladder.editor.gui.ItemBrowserGui;
import com.modeladder.editor.gui.MainMenuGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Handles interactions inside Model Adder administrative GUIs.
 */
public final class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MainMenuGui) {
            event.setCancelled(true);
            // Main menu navigation
        } else if (holder instanceof ItemBrowserGui) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && !clicked.getType().isAir() && event.getWhoClicked() instanceof Player player) {
                // Give copy of item
                player.getInventory().addItem(clicked.clone());
                player.sendMessage("§aObtained " + clicked.getType().name());
            }
        }
    }
}
