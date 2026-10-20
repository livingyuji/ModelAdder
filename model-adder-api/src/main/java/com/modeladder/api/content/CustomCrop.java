package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Custom crop definition with staged growth, harvest drops, and seed requirements.
 */
public interface CustomCrop extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.CROP;
    }

    /**
     * @return seed item key required to plant this crop.
     */
    @NotNull
    ContentKey getSeedItem();

    /**
     * @return total growth stages (e.g. 4 or 7).
     */
    int getMaxStage();

    /**
     * @return model asset for a specific stage index (0 to maxStage).
     */
    @NotNull
    String getStageModelAsset(int stage);

    /**
     * @return base growth chance per random tick (0.0 to 1.0).
     */
    double getGrowthRate();

    /**
     * @return minimum required light level to grow.
     */
    int getMinLightLevel();

    /**
     * @return drops produced on harvest.
     */
    @NotNull
    List<CustomBlock.BlockDrop> getHarvestDrops();

    /**
     * @return true if the crop resets to stage 0 after harvest instead of being destroyed.
     */
    boolean isReusable();
}
