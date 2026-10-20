package com.modeladder.api.content;

import org.jetbrains.annotations.NotNull;

/**
 * Types of custom content supported by Model Adder.
 */
public enum ContentType {
    ITEM("items", "Item"),
    BLOCK("blocks", "Block"),
    FURNITURE("furniture", "Furniture"),
    ARMOR("armor", "Armor"),
    CROP("crops", "Crop"),
    TREE("trees", "Tree"),
    LIQUID("liquids", "Liquid"),
    MOB("mobs", "Mob"),
    RECIPE("recipes", "Recipe"),
    ABILITY("abilities", "Ability"),
    SOUND("sounds", "Sound"),
    FONT("fonts", "Font"),
    HUD("huds", "HUD");

    private final String folderName;
    private final String friendlyName;

    ContentType(@NotNull String folderName, @NotNull String friendlyName) {
        this.folderName = folderName;
        this.friendlyName = friendlyName;
    }

    @NotNull
    public String getFolderName() {
        return folderName;
    }

    @NotNull
    public String getFriendlyName() {
        return friendlyName;
    }
}
