package com.modeladder.integrations;

import com.modeladder.integrations.papi.ModelAdderExpansion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * Coordinates discovery and activation of optional integrations (PlaceholderAPI, Skript, model adapters).
 */
public final class IntegrationManager {

    private final Logger logger;
    private ModelAdderExpansion papiExpansion;

    public IntegrationManager(@NotNull Logger logger) {
        this.logger = logger;
    }

    public void init() {
        // PlaceholderAPI check
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                this.papiExpansion = new ModelAdderExpansion();
                papiExpansion.register();
                logger.info("[ModelAdder] Successfully registered PlaceholderAPI expansion.");
            } catch (Throwable t) {
                logger.warning("[ModelAdder] Failed registering PlaceholderAPI expansion: " + t.getMessage());
            }
        }
    }

    public void shutdown() {
        if (papiExpansion != null) {
            try {
                papiExpansion.unregister();
            } catch (Throwable ignored) {}
            papiExpansion = null;
        }
    }
}
