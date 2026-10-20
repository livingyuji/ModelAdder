package com.modeladder.core.world;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomCrop;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.core.item.ItemFactory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles custom crop planting, growth stages, harvesting, and replanting.
 */
public final class CropListener implements Listener {

    private final RegistryManager registryManager;
    private final Map<String, CropInstance> plantedCrops = new ConcurrentHashMap<>();

    public CropListener(@NotNull RegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlantCrop(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.FARMLAND) return;

        Block above = clicked.getRelative(0, 1, 0);
        if (above.getType() != Material.AIR) return;

        ItemStack item = event.getItem();
        Optional<ContentKey> keyOpt = ItemFactory.getItemKey(item);
        if (keyOpt.isEmpty()) return;

        ContentKey seedKey = keyOpt.get();

        // Check if any crop uses this seed
        for (CustomCrop crop : registryManager.getCrops().getAll()) {
            if (crop.getSeedItem().equals(seedKey)) {
                // Plant crop (using vanilla WHEAT or CARROTS as anchor block)
                above.setType(Material.WHEAT);
                BlockData data = above.getBlockData();
                if (data instanceof Ageable ageable) {
                    ageable.setAge(0);
                    above.setBlockData(ageable);
                }

                plantedCrops.put(locKey(above), new CropInstance(crop.getKey(), 0));
                if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    item.setAmount(item.getAmount() - 1);
                }
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHarvestCrop(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        CropInstance instance = plantedCrops.get(locKey(block));
        if (instance == null) return;

        Optional<CustomCrop> cropOpt = registryManager.getCrops().get(instance.cropKey);
        if (cropOpt.isEmpty()) return;

        CustomCrop crop = cropOpt.get();

        // Check if fully grown
        if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() >= ageable.getMaximumAge()) {
            // Drop harvest items
            Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
            for (CustomBlock.BlockDrop drop : crop.getHarvestDrops()) {
                if (ThreadLocalRandom.current().nextDouble() <= drop.chance()) {
                    int amount = ThreadLocalRandom.current().nextInt(drop.minAmount(), drop.maxAmount() + 1);
                    Optional<CustomItem> dropItem = registryManager.getItems().get(drop.itemKey());
                    if (dropItem.isPresent() && amount > 0) {
                        block.getWorld().dropItemNaturally(dropLoc, ItemFactory.create(dropItem.get(), amount));
                    }
                }
            }

            if (crop.isReusable()) {
                ageable.setAge(0);
                block.setBlockData(ageable);
                instance.currentStage = 0;
            } else {
                block.setType(Material.AIR);
                plantedCrops.remove(locKey(block));
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreakCrop(BlockBreakEvent event) {
        Block block = event.getBlock();
        CropInstance instance = plantedCrops.remove(locKey(block));
        if (instance != null) {
            Optional<CustomCrop> cropOpt = registryManager.getCrops().get(instance.cropKey);
            if (cropOpt.isPresent()) {
                CustomCrop crop = cropOpt.get();
                // If broken before mature, return 1 seed
                Optional<CustomItem> seed = registryManager.getItems().get(crop.getSeedItem());
                if (seed.isPresent()) {
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), ItemFactory.create(seed.get(), 1));
                }
                event.setDropItems(false);
            }
        }
    }

    private String locKey(Block block) {
        return block.getWorld().getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }

    private static class CropInstance {
        final ContentKey cropKey;
        int currentStage;

        CropInstance(ContentKey cropKey, int currentStage) {
            this.cropKey = cropKey;
            this.currentStage = currentStage;
        }
    }
}
