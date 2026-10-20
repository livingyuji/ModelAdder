package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Custom crafting and smelting recipes.
 */
public interface CustomRecipe extends ContentDefinition {

    @Override
    default @NotNull ContentType getType() {
        return ContentType.RECIPE;
    }

    /**
     * @return type of recipe (SHAPED, SHAPELESS, FURNACE, BLAST_FURNACE, SMOKER, CAMPFIRE, SMITHING).
     */
    @NotNull
    RecipeType getRecipeType();

    /**
     * @return item key produced by this recipe.
     */
    @NotNull
    ContentKey getResultItem();

    /**
     * @return quantity of result produced.
     */
    int getResultAmount();

    /**
     * @return grid pattern rows for SHAPED recipe (e.g. [" R ", " R ", " S "]).
     */
    @NotNull
    List<String> getShapedShape();

    /**
     * @return map of character symbol to content/material key for SHAPED recipe.
     */
    @NotNull
    Map<Character, String> getShapedIngredients();

    /**
     * @return list of ingredient keys for SHAPELESS recipe.
     */
    @NotNull
    List<String> getShapelessIngredients();

    /**
     * @return cooking time in ticks for furnace/smoker/blast recipes.
     */
    int getCookingTimeTicks();

    /**
     * @return experience rewarded upon smelting.
     */
    float getSmeltExperience();

    enum RecipeType {
        SHAPED, SHAPELESS, FURNACE, BLAST_FURNACE, SMOKER, CAMPFIRE, SMITHING
    }
}
