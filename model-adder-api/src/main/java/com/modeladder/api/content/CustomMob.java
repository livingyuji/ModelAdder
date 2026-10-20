package com.modeladder.api.content;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Custom mob definition with base entity attributes, equipment, and drop tables.
 */
public interface CustomMob extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.MOB;
    }

    /**
     * @return base vanilla entity type (e.g. ZOMBIE, SKELETON).
     */
    @NotNull
    EntityType getBaseEntityType();

    /**
     * @return maximum health attribute.
     */
    double getMaxHealth();

    /**
     * @return movement speed attribute.
     */
    double getMovementSpeed();

    /**
     * @return attack damage attribute.
     */
    double getAttackDamage();

    /**
     * @return item key held in main hand, or null.
     */
    @Nullable
    ContentKey getMainHandItem();

    /**
     * @return item key worn on helmet, or null.
     */
    @Nullable
    ContentKey getHelmetItem();

    /**
     * @return custom drop table on death.
     */
    @NotNull
    List<CustomBlock.BlockDrop> getDrops();

    /**
     * @return external model-engine model ID if an animation adapter is active.
     */
    @Nullable
    String getModelEngineId();
}
