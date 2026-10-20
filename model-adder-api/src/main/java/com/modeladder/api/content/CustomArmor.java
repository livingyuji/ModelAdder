package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom armor definition defining wearable textures and defense stats.
 */
public interface CustomArmor {

    /**
     * @return armor slot (HEAD, CHEST, LEGS, FEET).
     */
    @NotNull
    ArmorSlot getSlot();

    /**
     * @return path to layer 1 texture in resourcepack (e.g. "modeladder:textures/models/armor/ruby_layer_1.png").
     */
    @NotNull
    String getLayer1Texture();

    /**
     * @return path to layer 2 texture (for leggings) or null.
     */
    @Nullable
    String getLayer2Texture();

    /**
     * @return defense points added by this armor.
     */
    double getDefense();

    /**
     * @return armor toughness points.
     */
    double getToughness();

    /**
     * @return knockback resistance (0.0 to 1.0).
     */
    double getKnockbackResistance();

    enum ArmorSlot {
        HEAD, CHEST, LEGS, FEET
    }
}
