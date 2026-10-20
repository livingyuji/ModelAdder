package com.modeladder.api.content;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a custom item definition with models, display, attributes, and abilities.
 */
public interface CustomItem extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.ITEM;
    }

    /**
     * @return the base vanilla item material.
     */
    @NotNull
    Material getBaseMaterial();

    /**
     * @return lore lines with formatting/minimessage support.
     */
    @NotNull
    List<String> getLore();

    /**
     * @return custom model data integer if configured, or null for item_model component.
     */
    @Nullable
    Integer getCustomModelData();

    /**
     * @return resource pack model asset path (e.g. "modeladder:item/ruby_sword").
     */
    @Nullable
    String getModelAsset();

    /**
     * @return texture asset path (e.g. "modeladder:item/ruby_sword.png").
     */
    @Nullable
    String getTextureAsset();

    /**
     * @return maximum stack size (1 - 99 for 1.20.5+ / 1.21.x).
     */
    int getMaxStackSize();

    /**
     * @return custom durability or null if vanilla or unbreakable.
     */
    @Nullable
    Integer getMaxDurability();

    /**
     * @return true if item cannot take damage.
     */
    boolean isUnbreakable();

    /**
     * @return list of ability keys assigned to this item.
     */
    @NotNull
    List<ContentKey> getAbilities();

    /**
     * @return armor definition if this item is wearable armor.
     */
    @Nullable
    CustomArmor getArmorDefinition();
}
