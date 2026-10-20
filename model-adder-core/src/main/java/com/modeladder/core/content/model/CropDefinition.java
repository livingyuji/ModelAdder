package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomCrop;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CropDefinition implements CustomCrop {

    private final ContentKey key;
    private final String displayName;
    private final ContentKey seedItem;
    private final int maxStage;
    private final Map<Integer, String> stageModels;
    private final double growthRate;
    private final int minLightLevel;
    private final List<CustomBlock.BlockDrop> harvestDrops;
    private final boolean reusable;

    public CropDefinition(
        @NotNull ContentKey key,
        @NotNull String displayName,
        @NotNull ContentKey seedItem,
        int maxStage,
        @NotNull Map<Integer, String> stageModels,
        double growthRate,
        int minLightLevel,
        @NotNull List<CustomBlock.BlockDrop> harvestDrops,
        boolean reusable
    ) {
        this.key = key;
        this.displayName = displayName;
        this.seedItem = seedItem;
        this.maxStage = maxStage;
        this.stageModels = Map.copyOf(stageModels);
        this.growthRate = growthRate;
        this.minLightLevel = minLightLevel;
        this.harvestDrops = new ArrayList<>(harvestDrops);
        this.reusable = reusable;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @NotNull @Override public ContentKey getSeedItem() { return seedItem; }
    @Override public int getMaxStage() { return maxStage; }
    @NotNull @Override public String getStageModelAsset(int stage) { return stageModels.getOrDefault(stage, stageModels.getOrDefault(0, "")); }
    @Override public double getGrowthRate() { return growthRate; }
    @Override public int getMinLightLevel() { return minLightLevel; }
    @NotNull @Override public List<CustomBlock.BlockDrop> getHarvestDrops() { return Collections.unmodifiableList(harvestDrops); }
    @Override public boolean isReusable() { return reusable; }
}
