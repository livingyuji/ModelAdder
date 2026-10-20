package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents custom furniture using Item Display and Interaction entities in Minecraft 1.21.x.
 */
public interface CustomFurniture extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.FURNITURE;
    }

    /**
     * @return item key used to render the 3D model via ItemDisplay.
     */
    @NotNull
    ContentKey getItemKey();

    /**
     * @return interaction hitbox width in blocks.
     */
    float getHitboxWidth();

    /**
     * @return interaction hitbox height in blocks.
     */
    float getHitboxHeight();

    /**
     * @return true if players can sit on this furniture (e.g. chairs, sofas).
     */
    boolean canSit();

    /**
     * @return vertical sitting height offset if canSit() is true.
     */
    double getSitHeightOffset();

    /**
     * @return rotation step in degrees (e.g. 45 or 90).
     */
    float getRotationStep();

    /**
     * @return sound played on placement.
     */
    @Nullable
    String getPlaceSound();

    /**
     * @return sound played on break.
     */
    @Nullable
    String getBreakSound();
}
