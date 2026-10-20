package com.modeladder.core.diagnostics;

import com.modeladder.api.pack.PackManager;
import com.modeladder.api.registry.RegistryManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Collects runtime diagnostics, performance metrics, and generates sanitized debug reports.
 */
public final class DiagnosticsService {

    private final String pluginVersion;
    private final RegistryManager registryManager;
    private final PackManager packManager;

    public DiagnosticsService(@NotNull String pluginVersion, @NotNull RegistryManager registryManager, @NotNull PackManager packManager) {
        this.pluginVersion = pluginVersion;
        this.registryManager = registryManager;
        this.packManager = packManager;
    }

    @NotNull
    public String generateSummary() {
        Runtime runtime = Runtime.getRuntime();
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;

        return String.format(
            """
            §6=== Model Adder Diagnostics ===
            §eVersion: §f%s (Target: 1.21.11)
            §eServer: §f%s (Bukkit: %s)
            §eJava: §f%s (%s)
            §eMemory: §f%dMB / %dMB (Max: %dMB)
            §eRegistered Content:
              §7- Items: §a%d
              §7- Blocks: §a%d
              §7- Furniture: §a%d
              §7- Crops: §a%d
              §7- Trees: §a%d
              §7- Mobs: §a%d
              §7- Recipes: §a%d
              §7- Abilities: §a%d
            §eResource Pack:
              §7- Current Build: §f%s
              §7- SHA-1: §f%s
              §7- Public URL: §f%s
            """,
            pluginVersion,
            Bukkit.getVersion(),
            Bukkit.getBukkitVersion(),
            System.getProperty("java.version"),
            System.getProperty("java.vendor"),
            usedMem, totalMem, maxMem,
            registryManager.getItems().size(),
            registryManager.getBlocks().size(),
            registryManager.getFurniture().size(),
            registryManager.getCrops().size(),
            registryManager.getTrees().size(),
            registryManager.getMobs().size(),
            registryManager.getRecipes().size(),
            registryManager.getAbilities().size(),
            packManager.getCurrentBuildId().orElse("None"),
            packManager.getCurrentSha1().orElse("None"),
            packManager.getPublicDownloadUrl().orElse("Not configured")
        );
    }

    @NotNull
    public Path exportSanitizedReport(@NotNull Path outputFolder) throws IOException {
        Files.createDirectories(outputFolder);
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        Path reportFile = outputFolder.resolve("debug-report-" + timestamp + ".txt");

        StringBuilder sb = new StringBuilder();
        sb.append("MODEL ADDER SANITIZED DEBUG REPORT\n");
        sb.append("Generated at: ").append(new Date()).append("\n\n");
        sb.append("--- Environment ---\n");
        sb.append("Plugin Version: ").append(pluginVersion).append("\n");
        sb.append("Server Version: ").append(Bukkit.getVersion()).append("\n");
        sb.append("Bukkit Version: ").append(Bukkit.getBukkitVersion()).append("\n");
        sb.append("Java: ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")\n");
        sb.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.arch")).append("\n\n");

        sb.append("--- Content Registries ---\n");
        sb.append("Items (").append(registryManager.getItems().size()).append("): ").append(registryManager.getItems().getAll().stream().map(i -> i.getKey().asString()).toList()).append("\n");
        sb.append("Blocks (").append(registryManager.getBlocks().size()).append("): ").append(registryManager.getBlocks().getAll().stream().map(b -> b.getKey().asString()).toList()).append("\n");
        sb.append("Furniture (").append(registryManager.getFurniture().size()).append("): ").append(registryManager.getFurniture().getAll().stream().map(f -> f.getKey().asString()).toList()).append("\n");
        sb.append("Mobs (").append(registryManager.getMobs().size()).append("): ").append(registryManager.getMobs().getAll().stream().map(m -> m.getKey().asString()).toList()).append("\n");
        sb.append("Recipes (").append(registryManager.getRecipes().size()).append("): ").append(registryManager.getRecipes().getAll().stream().map(r -> r.getKey().asString()).toList()).append("\n");
        sb.append("Abilities (").append(registryManager.getAbilities().size()).append("): ").append(registryManager.getAbilities().getAll().stream().map(a -> a.getKey().asString()).toList()).append("\n\n");

        sb.append("--- Resource Pack Status ---\n");
        sb.append("Current Build ID: ").append(packManager.getCurrentBuildId().orElse("None")).append("\n");
        sb.append("Current SHA-1: ").append(packManager.getCurrentSha1().orElse("None")).append("\n");
        sb.append("Published File Exists: ").append(packManager.getPublishedPackFile().isPresent()).append("\n");
        sb.append("Available Backups: ").append(packManager.getAvailableBackups()).append("\n");

        Files.writeString(reportFile, sb.toString(), StandardCharsets.UTF_8);
        return reportFile;
    }
}
