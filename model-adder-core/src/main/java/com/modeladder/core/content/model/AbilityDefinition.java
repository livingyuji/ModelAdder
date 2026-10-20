package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomAbility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AbilityDefinition implements CustomAbility {

    private final ContentKey key;
    private final String displayName;
    private final AbilityTrigger trigger;
    private final double cooldownSeconds;
    private final String permission;
    private final String sound;
    private final String particle;
    private final List<String> effects;

    public AbilityDefinition(
        @NotNull ContentKey key,
        @NotNull String displayName,
        @NotNull AbilityTrigger trigger,
        double cooldownSeconds,
        @Nullable String permission,
        @Nullable String sound,
        @Nullable String particle,
        @NotNull List<String> effects
    ) {
        this.key = key;
        this.displayName = displayName;
        this.trigger = trigger;
        this.cooldownSeconds = Math.max(0.0, cooldownSeconds);
        this.permission = permission;
        this.sound = sound;
        this.particle = particle;
        this.effects = new ArrayList<>(effects);
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @NotNull @Override public AbilityTrigger getTrigger() { return trigger; }
    @Override public double getCooldownSeconds() { return cooldownSeconds; }
    @Nullable @Override public String getPermission() { return permission; }
    @Nullable @Override public String getSound() { return sound; }
    @Nullable @Override public String getParticle() { return particle; }
    @NotNull @Override public List<String> getEffects() { return Collections.unmodifiableList(effects); }
}
