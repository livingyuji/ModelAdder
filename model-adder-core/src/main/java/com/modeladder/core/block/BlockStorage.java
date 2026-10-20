package com.modeladder.core.block;

import com.modeladder.api.content.ContentKey;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persists placed custom block locations across chunk unloads and server restarts.
 */
public final class BlockStorage {

    private final Path storageFile;
    private final Map<String, ContentKey> blockLocations = new ConcurrentHashMap<>();

    public BlockStorage(@NotNull Path dataFolder) {
        this.storageFile = dataFolder.resolve("data").resolve("blocks.yml");
        load();
    }

    public void setBlock(@NotNull Block block, @NotNull ContentKey key) {
        blockLocations.put(toLocationKey(block.getLocation()), key);
    }

    public void removeBlock(@NotNull Block block) {
        blockLocations.remove(toLocationKey(block.getLocation()));
    }

    @Nullable
    public ContentKey getBlockKey(@NotNull Block block) {
        return blockLocations.get(toLocationKey(block.getLocation()));
    }

    public boolean isCustomBlock(@NotNull Block block) {
        return blockLocations.containsKey(toLocationKey(block.getLocation()));
    }

    public synchronized void save() {
        try {
            Files.createDirectories(storageFile.getParent());
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<String, ContentKey> entry : blockLocations.entrySet()) {
                yaml.set("blocks." + entry.getKey(), entry.getValue().asString());
            }
            yaml.save(storageFile.toFile());
        } catch (IOException ignored) {}
    }

    public synchronized void load() {
        blockLocations.clear();
        if (!Files.exists(storageFile)) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(storageFile.toFile());
        if (!yaml.contains("blocks")) return;

        for (String locKey : yaml.getConfigurationSection("blocks").getKeys(false)) {
            String rawKey = yaml.getString("blocks." + locKey);
            if (ContentKey.isValid(rawKey)) {
                blockLocations.put(locKey, ContentKey.of(rawKey));
            }
        }
    }

    @NotNull
    private String toLocationKey(@NotNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public int getStoredBlockCount() {
        return blockLocations.size();
    }
}
