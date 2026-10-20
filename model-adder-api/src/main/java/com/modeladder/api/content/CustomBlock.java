package com.modeladder.api.content;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a custom block definition with placement, visual model, hardness, and drops.
 */
public interface CustomBlock extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.BLOCK;
    }

    /**
     * @return base vanilla block material used as anchor (e.g. NOTE_BLOCK, MUSHROOM_STEM, BARRIER).
     */
    @NotNull
    Material getBaseMaterial();

    /**
     * @return block model asset in resourcepack.
     */
    @Nullable
    String getModelAsset();

    /**
     * @return hardness for break speed calculation.
     */
    float getHardness();

    /**
     * @return blast resistance.
     */
    float getBlastResistance();

    /**
     * @return light emission level (0-15).
     */
    int getLightLevel();

    /**
     * @return items dropped when broken.
     */
    @NotNull
    List<BlockDrop> getDrops();

    /**
     * @return break sound key or null for default.
     */
    @Nullable
    String getBreakSound();

    /**
     * @return place sound key or null for default.
     */
    @Nullable
    String getPlaceSound();

    record BlockDrop(@NotNull ContentKey itemKey, int minAmount, int maxAmount, double chance) {}
}
