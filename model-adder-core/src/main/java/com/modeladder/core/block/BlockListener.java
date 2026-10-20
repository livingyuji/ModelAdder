package com.modeladder.core.block;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.event.CustomBlockBreakEvent;
import com.modeladder.api.event.CustomBlockPlaceEvent;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.core.item.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles placing and breaking of custom blocks, custom drops, and sound effects.
 */
public final class BlockListener implements Listener {

    private final RegistryManager registryManager;
    private final BlockStorage blockStorage;

    public BlockListener(@NotNull RegistryManager registryManager, @NotNull BlockStorage blockStorage) {
        this.registryManager = registryManager;
        this.blockStorage = blockStorage;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Optional<ContentKey> keyOpt = ItemFactory.getItemKey(item);
        if (keyOpt.isEmpty()) return;

        ContentKey key = keyOpt.get();
        // Check if there is a matching custom block definition
        Optional<CustomBlock> blockDefOpt = registryManager.getBlocks().get(key);
        if (blockDefOpt.isEmpty()) return;

        CustomBlock customBlock = blockDefOpt.get();
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        CustomBlockPlaceEvent placeEvent = new CustomBlockPlaceEvent(block, customBlock, player);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        blockStorage.setBlock(block, key);

        if (customBlock.getPlaceSound() != null) {
            try {
                block.getWorld().playSound(block.getLocation(), customBlock.getPlaceSound(), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        ContentKey key = blockStorage.getBlockKey(block);
        if (key == null) return;

        Optional<CustomBlock> blockDefOpt = registryManager.getBlocks().get(key);
        if (blockDefOpt.isEmpty()) {
            blockStorage.removeBlock(block);
            return;
        }

        CustomBlock customBlock = blockDefOpt.get();
        Player player = event.getPlayer();

        CustomBlockBreakEvent breakEvent = new CustomBlockBreakEvent(block, customBlock, player);
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        blockStorage.removeBlock(block);

        // Cancel vanilla drops
        event.setDropItems(false);

        // Custom drops
        if (breakEvent.isDropItems() && (player == null || player.getGameMode() != GameMode.CREATIVE)) {
            Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
            for (CustomBlock.BlockDrop drop : customBlock.getDrops()) {
                if (ThreadLocalRandom.current().nextDouble() <= drop.chance()) {
                    int count = (drop.minAmount() == drop.maxAmount())
                        ? drop.minAmount()
                        : ThreadLocalRandom.current().nextInt(drop.minAmount(), drop.maxAmount() + 1);

                    if (count > 0) {
                        Optional<CustomItem> dropItem = registryManager.getItems().get(drop.itemKey());
                        if (dropItem.isPresent()) {
                            ItemStack item = ItemFactory.create(dropItem.get(), count);
                            block.getWorld().dropItemNaturally(dropLoc, item);
                        }
                    }
                }
            }
        }

        if (customBlock.getBreakSound() != null) {
            try {
                block.getWorld().playSound(block.getLocation(), customBlock.getBreakSound(), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }
}
