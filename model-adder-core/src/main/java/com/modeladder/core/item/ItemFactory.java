package com.modeladder.core.item;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Creates custom ItemStacks with PersistentDataContainer identities, custom model assignments, and lore.
 */
public final class ItemFactory {

    public static final NamespacedKey KEY_ITEM_ID = new NamespacedKey("modeladder", "id");
    public static final NamespacedKey KEY_DURABILITY = new NamespacedKey("modeladder", "durability");
    public static final NamespacedKey KEY_MAX_DURABILITY = new NamespacedKey("modeladder", "max_durability");

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static Method setItemModelMethod;

    static {
        try {
            setItemModelMethod = ItemMeta.class.getMethod("setItemModel", NamespacedKey.class);
        } catch (NoSuchMethodException ignored) {
            setItemModelMethod = null;
        }
    }

    private ItemFactory() {}

    @NotNull
    public static ItemStack create(@NotNull CustomItem definition, int amount) {
        int count = Math.max(1, Math.min(definition.getMaxStackSize(), amount));
        ItemStack item = new ItemStack(definition.getBaseMaterial(), count);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // 1. Persistent Identity
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_ITEM_ID, PersistentDataType.STRING, definition.getKey().asString());

        // 2. Durability
        if (definition.getMaxDurability() != null) {
            pdc.set(KEY_DURABILITY, PersistentDataType.INTEGER, definition.getMaxDurability());
            pdc.set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, definition.getMaxDurability());
        }

        // 3. Unbreakable flag
        if (definition.isUnbreakable()) {
            meta.setUnbreakable(true);
        }

        // 4. Display Name
        Component nameComp = parseFormattedText(definition.getDisplayName());
        meta.displayName(nameComp);

        // 5. Lore
        if (!definition.getLore().isEmpty()) {
            List<Component> loreList = new ArrayList<>();
            for (String line : definition.getLore()) {
                loreList.add(parseFormattedText(line));
            }
            meta.lore(loreList);
        }

        // 6. Custom Model Data (1.14 - 1.21.x standard)
        if (definition.getCustomModelData() != null) {
            meta.setCustomModelData(definition.getCustomModelData());
        }

        // 7. Modern Item Model component (1.21.2+ / 1.21.11 item_model)
        if (definition.getModelAsset() != null && setItemModelMethod != null) {
            try {
                String asset = definition.getModelAsset();
                NamespacedKey modelKey = asset.contains(":") ? NamespacedKey.fromString(asset) : new NamespacedKey("modeladder", asset);
                if (modelKey != null) {
                    setItemModelMethod.invoke(meta, modelKey);
                }
            } catch (Exception ignored) {}
        }

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isCustomItem(@Nullable ItemStack item) {
        return getItemKey(item).isPresent();
    }

    @NotNull
    public static Optional<ContentKey> getItemKey(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return Optional.empty();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String rawKey = pdc.get(KEY_ITEM_ID, PersistentDataType.STRING);
        if (rawKey == null || !ContentKey.isValid(rawKey)) {
            return Optional.empty();
        }
        return Optional.of(ContentKey.of(rawKey));
    }

    @NotNull
    public static Component parseFormattedText(@NotNull String text) {
        if (text.contains("<") && text.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(text);
            } catch (Exception ignored) {}
        }
        // Fallback to legacy color codes &
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
