package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomFurniture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FurnitureDefinition implements CustomFurniture {

    private final ContentKey key;
    private final ContentKey itemKey;
    private final String displayName;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final boolean canSit;
    private final double sitHeightOffset;
    private final float rotationStep;
    private final String placeSound;
    private final String breakSound;

    public FurnitureDefinition(
        @NotNull ContentKey key,
        @NotNull ContentKey itemKey,
        @NotNull String displayName,
        float hitboxWidth,
        float hitboxHeight,
        boolean canSit,
        double sitHeightOffset,
        float rotationStep,
        @Nullable String placeSound,
        @Nullable String breakSound
    ) {
        this.key = key;
        this.itemKey = itemKey;
        this.displayName = displayName;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.canSit = canSit;
        this.sitHeightOffset = sitHeightOffset;
        this.rotationStep = rotationStep;
        this.placeSound = placeSound;
        this.breakSound = breakSound;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public ContentKey getItemKey() { return itemKey; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @Override public float getHitboxWidth() { return hitboxWidth; }
    @Override public float getHitboxHeight() { return hitboxHeight; }
    @Override public boolean canSit() { return canSit; }
    @Override public double getSitHeightOffset() { return sitHeightOffset; }
    @Override public float getRotationStep() { return rotationStep; }
    @Nullable @Override public String getPlaceSound() { return placeSound; }
    @Nullable @Override public String getBreakSound() { return breakSound; }
}
