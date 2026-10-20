package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomArmor;
import com.modeladder.api.content.CustomItem;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ItemDefinition implements CustomItem {

    private final ContentKey key;
    private final Material baseMaterial;
    private final String displayName;
    private final List<String> lore;
    private final Integer customModelData;
    private final String modelAsset;
    private final String textureAsset;
    private final int maxStackSize;
    private final Integer maxDurability;
    private final boolean unbreakable;
    private final List<ContentKey> abilities;
    private final CustomArmor armorDefinition;

    public ItemDefinition(
        @NotNull ContentKey key,
        @NotNull Material baseMaterial,
        @NotNull String displayName,
        @NotNull List<String> lore,
        @Nullable Integer customModelData,
        @Nullable String modelAsset,
        @Nullable String textureAsset,
        int maxStackSize,
        @Nullable Integer maxDurability,
        boolean unbreakable,
        @NotNull List<ContentKey> abilities,
        @Nullable CustomArmor armorDefinition
    ) {
        this.key = key;
        this.baseMaterial = baseMaterial;
        this.displayName = displayName;
        this.lore = new ArrayList<>(lore);
        this.customModelData = customModelData;
        this.modelAsset = modelAsset;
        this.textureAsset = textureAsset;
        this.maxStackSize = maxStackSize;
        this.maxDurability = maxDurability;
        this.unbreakable = unbreakable;
        this.abilities = new ArrayList<>(abilities);
        this.armorDefinition = armorDefinition;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public Material getBaseMaterial() { return baseMaterial; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @NotNull @Override public List<String> getLore() { return Collections.unmodifiableList(lore); }
    @Nullable @Override public Integer getCustomModelData() { return customModelData; }
    @Nullable @Override public String getModelAsset() { return modelAsset; }
    @Nullable @Override public String getTextureAsset() { return textureAsset; }
    @Override public int getMaxStackSize() { return maxStackSize; }
    @Nullable @Override public Integer getMaxDurability() { return maxDurability; }
    @Override public boolean isUnbreakable() { return unbreakable; }
    @NotNull @Override public List<ContentKey> getAbilities() { return Collections.unmodifiableList(abilities); }
    @Nullable @Override public CustomArmor getArmorDefinition() { return armorDefinition; }
}
