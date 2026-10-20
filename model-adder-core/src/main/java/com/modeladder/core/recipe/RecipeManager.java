package com.modeladder.core.recipe;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.content.CustomRecipe;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.core.item.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

/**
 * Registers custom crafting and smelting recipes into the Bukkit server.
 */
public final class RecipeManager {

    private final RegistryManager registryManager;
    private final Logger logger;
    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();

    public RecipeManager(@NotNull RegistryManager registryManager, @NotNull Logger logger) {
        this.registryManager = registryManager;
        this.logger = logger;
    }

    public void registerAll() {
        unregisterAll();

        for (CustomRecipe recipe : registryManager.getRecipes().getAll()) {
            Optional<CustomItem> resultItemOpt = registryManager.getItems().get(recipe.getResultItem());
            if (resultItemOpt.isEmpty()) continue;

            ItemStack resultStack = ItemFactory.create(resultItemOpt.get(), recipe.getResultAmount());
            NamespacedKey recipeKey = new NamespacedKey(recipe.getKey().getNamespace(), recipe.getKey().getKey());

            try {
                if (recipe.getRecipeType() == CustomRecipe.RecipeType.SHAPED && !recipe.getShapedShape().isEmpty()) {
                    ShapedRecipe shaped = new ShapedRecipe(recipeKey, resultStack);
                    shaped.shape(recipe.getShapedShape().toArray(new String[0]));

                    for (Map.Entry<Character, String> entry : recipe.getShapedIngredients().entrySet()) {
                        RecipeChoice choice = resolveRecipeChoice(entry.getValue());
                        if (choice != null) {
                            shaped.setIngredient(entry.getKey(), choice);
                        }
                    }

                    Bukkit.addRecipe(shaped);
                    registeredRecipes.add(recipeKey);

                } else if (recipe.getRecipeType() == CustomRecipe.RecipeType.SHAPELESS) {
                    ShapelessRecipe shapeless = new ShapelessRecipe(recipeKey, resultStack);
                    for (String ing : recipe.getShapelessIngredients()) {
                        RecipeChoice choice = resolveRecipeChoice(ing);
                        if (choice != null) {
                            shapeless.addIngredient(choice);
                        }
                    }

                    Bukkit.addRecipe(shapeless);
                    registeredRecipes.add(recipeKey);
                }
            } catch (Exception e) {
                logger.warning("[ModelAdder] Could not register recipe " + recipe.getKey() + ": " + e.getMessage());
            }
        }
    }

    public void unregisterAll() {
        for (NamespacedKey key : registeredRecipes) {
            try {
                Bukkit.removeRecipe(key);
            } catch (Exception ignored) {}
        }
        registeredRecipes.clear();
    }

    private RecipeChoice resolveRecipeChoice(String rawKey) {
        if (ContentKey.isValid(rawKey)) {
            Optional<CustomItem> customItem = registryManager.getItems().get(ContentKey.of(rawKey));
            if (customItem.isPresent()) {
                return new RecipeChoice.ExactChoice(ItemFactory.create(customItem.get(), 1));
            }
        }

        Material mat = Material.matchMaterial(rawKey.toUpperCase());
        if (mat != null) {
            return new RecipeChoice.MaterialChoice(mat);
        }

        return null;
    }
}
