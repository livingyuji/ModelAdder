package com.modeladder.core.permission;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Central registry of all Model Adder permission nodes.
 */
public final class Permissions {

    private Permissions() {}

    public static final String ADMIN = "modeladder.admin";
    public static final String RELOAD = "modeladder.reload";
    public static final String STATUS = "modeladder.status";
    public static final String DEBUG = "modeladder.debug";

    // Item permissions
    public static final String ITEM_LIST = "modeladder.item.list";
    public static final String ITEM_GIVE = "modeladder.item.give";
    public static final String ITEM_INFO = "modeladder.item.info";
    public static final String ITEM_EDITOR = "modeladder.editor.items";

    // Block & Furniture permissions
    public static final String BLOCK_LIST = "modeladder.block.list";
    public static final String BLOCK_INFO = "modeladder.block.info";
    public static final String BLOCK_EDITOR = "modeladder.editor.blocks";
    public static final String FURNITURE_EDITOR = "modeladder.editor.furniture";

    // Recipe & Mob permissions
    public static final String RECIPE_LIST = "modeladder.recipe.list";
    public static final String RECIPE_EDITOR = "modeladder.editor.recipes";
    public static final String MOB_LIST = "modeladder.mob.list";
    public static final String MOB_SPAWN = "modeladder.mob.spawn";
    public static final String MOB_EDITOR = "modeladder.editor.mobs";

    // Resource pack permissions
    public static final String PACK_IMPORT = "modeladder.pack.import";
    public static final String PACK_VALIDATE = "modeladder.pack.validate";
    public static final String PACK_BUILD = "modeladder.pack.build";
    public static final String PACK_STATUS = "modeladder.pack.status";
    public static final String PACK_LIST = "modeladder.pack.list";
    public static final String PACK_ROLLBACK = "modeladder.pack.rollback";
    public static final String PACK_SEND = "modeladder.pack.send";

    public static boolean has(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(ADMIN) || sender.hasPermission("modeladder.*") || sender.hasPermission(permission);
    }
}
