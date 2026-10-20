package com.modeladder.editor.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game administrative main menu GUI.
 */
public final class MainMenuGui implements InventoryHolder {

    private final Inventory inventory;

    public MainMenuGui() {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Model Adder — Studio", NamedTextColor.GOLD));
        initialize();
    }

    private void initialize() {
        // Items button
        ItemStack itemsBtn = createButton(Material.DIAMOND_SWORD, "&6Custom Items", List.of("&7Browse, obtain, and inspect", "&7all registered custom items."));
        inventory.setItem(11, itemsBtn);

        // Blocks button
        ItemStack blocksBtn = createButton(Material.NOTE_BLOCK, "&6Custom Blocks & Furniture", List.of("&7Inspect registered blocks,", "&7furniture, and hitboxes."));
        inventory.setItem(13, blocksBtn);

        // Resource pack button
        ItemStack packBtn = createButton(Material.CHEST, "&6Resource Pack Control", List.of("&7Trigger pack builds, backups,", "&7and check download status."));
        inventory.setItem(15, packBtn);
    }

    public static ItemStack createButton(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(l));
            }
            meta.lore(compLore);
            item.setItemMeta(meta);
        }
        return item;
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
