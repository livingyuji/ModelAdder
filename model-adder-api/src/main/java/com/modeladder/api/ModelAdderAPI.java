package com.modeladder.api;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.pack.PackManager;
import com.modeladder.api.registry.RegistryManager;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Public Developer API gateway for Model Adder.
 */
public interface ModelAdderAPI {

    /**
     * @return active ModelAdderAPI instance.
     * @throws IllegalStateException if the plugin is not yet loaded.
     */
    @NotNull
    static ModelAdderAPI get() {
        ModelAdderAPI instance = Holder.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("ModelAdderAPI is not initialized. Is ModelAdder plugin enabled?");
        }
        return instance;
    }

    /**
     * Internal method to set the singleton instance upon startup.
     */
    static void setInstance(@Nullable ModelAdderAPI api) {
        Holder.INSTANCE = api;
    }

    @NotNull
    RegistryManager getRegistries();

    @NotNull
    PackManager getPackManager();

    boolean isCustomItem(@Nullable ItemStack item);

    @NotNull
    Optional<ContentKey> getItemKey(@Nullable ItemStack item);

    @NotNull
    Optional<CustomItem> getCustomItem(@Nullable ItemStack item);

    @NotNull
    Optional<ItemStack> createItemStack(@NotNull ContentKey key, int amount);

    @NotNull
    default Optional<ItemStack> createItemStack(@NotNull String rawKey, int amount) {
        if (!ContentKey.isValid(rawKey)) {
            return Optional.empty();
        }
        return createItemStack(ContentKey.of(rawKey), amount);
    }

    boolean isCustomBlock(@Nullable Block block);

    @NotNull
    Optional<ContentKey> getBlockKey(@Nullable Block block);

    @NotNull
    Optional<CustomBlock> getCustomBlock(@Nullable Block block);

    final class Holder {
        private static volatile ModelAdderAPI INSTANCE;
    }
}
