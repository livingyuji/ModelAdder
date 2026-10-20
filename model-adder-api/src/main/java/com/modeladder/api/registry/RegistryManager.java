package com.modeladder.api.registry;

import com.modeladder.api.content.*;
import org.jetbrains.annotations.NotNull;

/**
 * Access point to all content registries.
 */
public interface RegistryManager {

    @NotNull
    <T extends ContentDefinition> ContentRegistry<T> getRegistry(@NotNull ContentType type);

    @NotNull
    ContentRegistry<CustomItem> getItems();

    @NotNull
    ContentRegistry<CustomBlock> getBlocks();

    @NotNull
    ContentRegistry<CustomFurniture> getFurniture();

    @NotNull
    ContentRegistry<CustomCrop> getCrops();

    @NotNull
    ContentRegistry<CustomTree> getTrees();

    @NotNull
    ContentRegistry<CustomMob> getMobs();

    @NotNull
    ContentRegistry<CustomRecipe> getRecipes();

    @NotNull
    ContentRegistry<CustomAbility> getAbilities();

    int getTotalContentCount();
}
