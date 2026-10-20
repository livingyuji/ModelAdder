package com.modeladder.integrations.papi;

import com.modeladder.api.ModelAdderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion providing placeholders for Model Adder content and resource packs.
 */
public final class ModelAdderExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "modeladder";
    }

    @Override
    public @NotNull String getAuthor() {
        return "LivingYuji";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        ModelAdderAPI api;
        try {
            api = ModelAdderAPI.get();
        } catch (IllegalStateException e) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "items_count" -> String.valueOf(api.getRegistries().getItems().size());
            case "blocks_count" -> String.valueOf(api.getRegistries().getBlocks().size());
            case "furniture_count" -> String.valueOf(api.getRegistries().getFurniture().size());
            case "total_content" -> String.valueOf(api.getRegistries().getTotalContentCount());
            case "pack_build" -> api.getPackManager().getCurrentBuildId().orElse("None");
            case "pack_sha1" -> api.getPackManager().getCurrentSha1().orElse("None");
            case "pack_url" -> api.getPackManager().getPublicDownloadUrl().orElse("");
            default -> null;
        };
    }
}
