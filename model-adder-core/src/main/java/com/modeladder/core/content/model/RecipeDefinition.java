package com.modeladder.core.content.model;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class RecipeDefinition implements CustomRecipe {

    private final ContentKey key;
    private final String displayName;
    private final RecipeType recipeType;
    private final ContentKey resultItem;
    private final int resultAmount;
    private final List<String> shapedShape;
    private final Map<Character, String> shapedIngredients;
    private final List<String> shapelessIngredients;
    private final int cookingTimeTicks;
    private final float smeltExperience;

    public RecipeDefinition(
        @NotNull ContentKey key,
        @NotNull String displayName,
        @NotNull RecipeType recipeType,
        @NotNull ContentKey resultItem,
        int resultAmount,
        @NotNull List<String> shapedShape,
        @NotNull Map<Character, String> shapedIngredients,
        @NotNull List<String> shapelessIngredients,
        int cookingTimeTicks,
        float smeltExperience
    ) {
        this.key = key;
        this.displayName = displayName;
        this.recipeType = recipeType;
        this.resultItem = resultItem;
        this.resultAmount = resultAmount;
        this.shapedShape = new ArrayList<>(shapedShape);
        this.shapedIngredients = new HashMap<>(shapedIngredients);
        this.shapelessIngredients = new ArrayList<>(shapelessIngredients);
        this.cookingTimeTicks = cookingTimeTicks;
        this.smeltExperience = smeltExperience;
    }

    @NotNull @Override public ContentKey getKey() { return key; }
    @NotNull @Override public String getDisplayName() { return displayName; }
    @NotNull @Override public RecipeType getRecipeType() { return recipeType; }
    @NotNull @Override public ContentKey getResultItem() { return resultItem; }
    @Override public int getResultAmount() { return resultAmount; }
    @NotNull @Override public List<String> getShapedShape() { return Collections.unmodifiableList(shapedShape); }
    @NotNull @Override public Map<Character, String> getShapedIngredients() { return Collections.unmodifiableMap(shapedIngredients); }
    @NotNull @Override public List<String> getShapelessIngredients() { return Collections.unmodifiableList(shapelessIngredients); }
    @Override public int getCookingTimeTicks() { return cookingTimeTicks; }
    @Override public float getSmeltExperience() { return smeltExperience; }
}
