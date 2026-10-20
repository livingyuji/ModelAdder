package com.modeladder.api.content;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Custom item ability definition with configurable trigger, cooldown, and actions.
 */
public interface CustomAbility extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.ABILITY;
    }

    /**
     * @return interaction event that triggers this ability.
     */
    @NotNull
    AbilityTrigger getTrigger();

    /**
     * @return cooldown duration in seconds before ability can be re-triggered.
     */
    double getCooldownSeconds();

    /**
     * @return permission node required to use this ability, or null if open.
     */
    @Nullable
    String getPermission();

    /**
     * @return sound key played when ability triggers.
     */
    @Nullable
    String getSound();

    /**
     * @return particle effect played on activation.
     */
    @Nullable
    String getParticle();

    /**
     * @return list of configured ability effects (e.g. "potion:SPEED:10:1", "damage:nearby:5:4.0", "launch:1.5").
     */
    @NotNull
    List<String> getEffects();

    enum AbilityTrigger {
        RIGHT_CLICK,
        LEFT_CLICK,
        HIT_ENTITY,
        KILL_ENTITY,
        TAKE_DAMAGE,
        SNEAK,
        CONSUME
    }
}
