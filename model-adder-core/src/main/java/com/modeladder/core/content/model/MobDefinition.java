package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomMob;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MobDefinition implements CustomMob {

    private final ContentKey key;
    private final String displayName;
    private final EntityType baseEntityType;
    private final double maxHealth;
    private final double movementSpeed;
    private final double attackDamage;
    private final ContentKey mainHandItem;
    private final ContentKey helmetItem;
    private final List<CustomBlock.BlockDrop> drops;
    private final String modelEngineId;

    public MobDefinition(
        @NotNull ContentKey key,
        @NotNull String displayName,
        @NotNull EntityType baseEntityType,
        double maxHealth,
        double movementSpeed,
        double attackDamage,
        @Nullable ContentKey mainHandItem,
        @Nullable ContentKey helmetItem,
        @NotNull List<CustomBlock.BlockDrop> drops,
        @Nullable String modelEngineId
    ) {
        this.key = key;
        this.displayName = displayName;
        this.baseEntityType = baseEntityType;
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.attackDamage = attackDamage;
        this.mainHandItem = mainHandItem;
        this.helmetItem = helmetItem;
        this.drops = new ArrayList<>(drops);
        this.modelEngineId = modelEngineId;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @NotNull @Override public EntityType getBaseEntityType() { return baseEntityType; }
    @Override public double getMaxHealth() { return maxHealth; }
    @Override public double getMovementSpeed() { return movementSpeed; }
    @Override public double getAttackDamage() { return attackDamage; }
    @Nullable @Override public ContentKey getMainHandItem() { return mainHandItem; }
    @Nullable @Override public ContentKey getHelmetItem() { return helmetItem; }
    @NotNull @Override public List<CustomBlock.BlockDrop> getDrops() { return Collections.unmodifiableList(drops); }
    @Nullable @Override public String getModelEngineId() { return modelEngineId; }
}
