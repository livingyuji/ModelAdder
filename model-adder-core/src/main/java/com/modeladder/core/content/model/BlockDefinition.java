package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlockDefinition implements CustomBlock {

    private final ContentKey key;
    private final Material baseMaterial;
    private final String displayName;
    private final String modelAsset;
    private final float hardness;
    private final float blastResistance;
    private final int lightLevel;
    private final List<BlockDrop> drops;
    private final String breakSound;
    private final String placeSound;

    public BlockDefinition(
        @NotNull ContentKey key,
        @NotNull Material baseMaterial,
        @NotNull String displayName,
        @Nullable String modelAsset,
        float hardness,
        float blastResistance,
        int lightLevel,
        @NotNull List<BlockDrop> drops,
        @Nullable String breakSound,
        @Nullable String placeSound
    ) {
        this.key = key;
        this.baseMaterial = baseMaterial;
        this.displayName = displayName;
        this.modelAsset = modelAsset;
        this.hardness = hardness;
        this.blastResistance = blastResistance;
        this.lightLevel = lightLevel;
        this.drops = new ArrayList<>(drops);
        this.breakSound = breakSound;
        this.placeSound = placeSound;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public Material getBaseMaterial() { return baseMaterial; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @Nullable @Override public String getModelAsset() { return modelAsset; }
    @Override public float getHardness() { return hardness; }
    @Override public float getBlastResistance() { return blastResistance; }
    @Override public int getLightLevel() { return lightLevel; }
    @NotNull @Override public List<BlockDrop> getDrops() { return Collections.unmodifiableList(drops); }
    @Nullable @Override public String getBreakSound() { return breakSound; }
    @Nullable @Override public String getPlaceSound() { return placeSound; }
}
