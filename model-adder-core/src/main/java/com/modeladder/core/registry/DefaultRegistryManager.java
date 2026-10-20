package com.modeladder.core.registry;

import com.modeladder.api.content.*;
import com.modeladder.api.registry.ContentRegistry;
import com.modeladder.api.registry.RegistryManager;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Default implementation of RegistryManager maintaining typed registries for all content types.
 */
public final class DefaultRegistryManager implements RegistryManager {

    private final Map<ContentType, SimpleContentRegistry<? extends ContentDefinition>> registries = new EnumMap<>(ContentType.class);

    private final SimpleContentRegistry<CustomItem> items = new SimpleContentRegistry<>(ContentType.ITEM);
    private final SimpleContentRegistry<CustomBlock> blocks = new SimpleContentRegistry<>(ContentType.BLOCK);
    private final SimpleContentRegistry<CustomFurniture> furniture = new SimpleContentRegistry<>(ContentType.FURNITURE);
    private final SimpleContentRegistry<CustomCrop> crops = new SimpleContentRegistry<>(ContentType.CROP);
    private final SimpleContentRegistry<CustomTree> trees = new SimpleContentRegistry<>(ContentType.TREE);
    private final SimpleContentRegistry<CustomMob> mobs = new SimpleContentRegistry<>(ContentType.MOB);
    private final SimpleContentRegistry<CustomRecipe> recipes = new SimpleContentRegistry<>(ContentType.RECIPE);
    private final SimpleContentRegistry<CustomAbility> abilities = new SimpleContentRegistry<>(ContentType.ABILITY);

    public DefaultRegistryManager() {
        registries.put(ContentType.ITEM, items);
        registries.put(ContentType.BLOCK, blocks);
        registries.put(ContentType.FURNITURE, furniture);
        registries.put(ContentType.CROP, crops);
        registries.put(ContentType.TREE, trees);
        registries.put(ContentType.MOB, mobs);
        registries.put(ContentType.RECIPE, recipes);
        registries.put(ContentType.ABILITY, abilities);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T extends ContentDefinition> ContentRegistry<T> getRegistry(@NotNull ContentType type) {
        SimpleContentRegistry<? extends ContentDefinition> reg = registries.get(type);
        if (reg == null) {
            throw new IllegalArgumentException("No registry found for type: " + type);
        }
        return (ContentRegistry<T>) reg;
    }

    @NotNull
    @Override
    public ContentRegistry<CustomItem> getItems() { return items; }

    @NotNull
    @Override
    public ContentRegistry<CustomBlock> getBlocks() { return blocks; }

    @NotNull
    @Override
    public ContentRegistry<CustomFurniture> getFurniture() { return furniture; }

    @NotNull
    @Override
    public ContentRegistry<CustomCrop> getCrops() { return crops; }

    @NotNull
    @Override
    public ContentRegistry<CustomTree> getTrees() { return trees; }

    @NotNull
    @Override
    public ContentRegistry<CustomMob> getMobs() { return mobs; }

    @NotNull
    @Override
    public ContentRegistry<CustomRecipe> getRecipes() { return recipes; }

    @NotNull
    @Override
    public ContentRegistry<CustomAbility> getAbilities() { return abilities; }

    @Override
    public int getTotalContentCount() {
        return items.size() + blocks.size() + furniture.size() + crops.size() + trees.size() + mobs.size() + recipes.size() + abilities.size();
    }

    public void clearAll() {
        registries.values().forEach(SimpleContentRegistry::clear);
    }
}
