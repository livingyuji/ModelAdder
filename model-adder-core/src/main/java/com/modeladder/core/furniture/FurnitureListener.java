package com.modeladder.core.furniture;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomFurniture;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.event.CustomFurnitureBreakEvent;
import com.modeladder.api.event.CustomFurniturePlaceEvent;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.core.item.ItemFactory;
import com.modeladder.core.permission.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.entity.EntityDismountEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles placing, sitting, and breaking for modern 3D ItemDisplay furniture.
 */
public final class FurnitureListener implements Listener {

    public static final NamespacedKey KEY_FURNITURE_ID = new NamespacedKey("modeladder", "furniture_id");
    public static final NamespacedKey KEY_SEAT = new NamespacedKey("modeladder", "temp_seat");

    private final RegistryManager registryManager;

    public FurnitureListener(@NotNull RegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlaceFurniture(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        ItemStack item = event.getItem();
        Optional<ContentKey> keyOpt = ItemFactory.getItemKey(item);
        if (keyOpt.isEmpty()) return;

        ContentKey key = keyOpt.get();
        Optional<CustomFurniture> furnOpt = registryManager.getFurniture().get(key);
        if (furnOpt.isEmpty()) return;

        CustomFurniture furniture = furnOpt.get();
        Player player = event.getPlayer();

        Location placeLoc = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);

        // Yaw snapping according to rotation step
        float yaw = Math.round(player.getLocation().getYaw() / furniture.getRotationStep()) * furniture.getRotationStep();
        placeLoc.setYaw(yaw);

        CustomFurniturePlaceEvent placeEvent = new CustomFurniturePlaceEvent(placeLoc, furniture, player);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // Spawn ItemDisplay
        ItemDisplay display = (ItemDisplay) placeLoc.getWorld().spawnEntity(placeLoc, EntityType.ITEM_DISPLAY);
        Optional<CustomItem> modelItem = registryManager.getItems().get(furniture.getItemKey());
        if (modelItem.isPresent()) {
            display.setItemStack(ItemFactory.create(modelItem.get(), 1));
        }
        display.getPersistentDataContainer().set(KEY_FURNITURE_ID, PersistentDataType.STRING, key.asString());

        // Spawn Interaction Entity for precise hitboxes
        Interaction interaction = (Interaction) placeLoc.getWorld().spawnEntity(placeLoc, EntityType.INTERACTION);
        interaction.setInteractionWidth(furniture.getHitboxWidth());
        interaction.setInteractionHeight(furniture.getHitboxHeight());
        interaction.setResponsive(true);
        interaction.getPersistentDataContainer().set(KEY_FURNITURE_ID, PersistentDataType.STRING, key.asString());

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        if (furniture.getPlaceSound() != null) {
            try {
                placeLoc.getWorld().playSound(placeLoc, furniture.getPlaceSound(), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractFurniture(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Interaction interaction)) return;

        PersistentDataContainer pdc = interaction.getPersistentDataContainer();
        String rawKey = pdc.get(KEY_FURNITURE_ID, PersistentDataType.STRING);
        if (rawKey == null || !ContentKey.isValid(rawKey)) return;

        Optional<CustomFurniture> furnOpt = registryManager.getFurniture().get(ContentKey.of(rawKey));
        if (furnOpt.isEmpty()) return;

        CustomFurniture furniture = furnOpt.get();
        Player player = event.getPlayer();

        if (furniture.canSit() && interaction.getPassengers().isEmpty()) {
            // Spawn temporary invisible armor stand seat
            Location seatLoc = interaction.getLocation().add(0, furniture.getSitHeightOffset(), 0);
            ArmorStand seat = (ArmorStand) seatLoc.getWorld().spawnEntity(seatLoc, EntityType.ARMOR_STAND);
            seat.setVisible(false);
            seat.setGravity(false);
            seat.setMarker(true);
            seat.setSmall(true);
            seat.getPersistentDataContainer().set(KEY_SEAT, PersistentDataType.BYTE, (byte) 1);

            seat.addPassenger(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageFurniture(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Interaction interaction)) return;

        PersistentDataContainer pdc = interaction.getPersistentDataContainer();
        String rawKey = pdc.get(KEY_FURNITURE_ID, PersistentDataType.STRING);
        if (rawKey == null || !ContentKey.isValid(rawKey)) return;

        Optional<CustomFurniture> furnOpt = registryManager.getFurniture().get(ContentKey.of(rawKey));
        if (furnOpt.isEmpty()) return;

        CustomFurniture furniture = furnOpt.get();
        Player player = (event.getDamager() instanceof Player p) ? p : null;

        CustomFurnitureBreakEvent breakEvent = new CustomFurnitureBreakEvent(interaction.getLocation(), furniture, player);
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        Location loc = interaction.getLocation();

        // Drop item
        if (breakEvent.isDropItem() && (player == null || player.getGameMode() != GameMode.CREATIVE)) {
            Optional<CustomItem> itemDef = registryManager.getItems().get(furniture.getItemKey());
            if (itemDef.isPresent()) {
                loc.getWorld().dropItemNaturally(loc, ItemFactory.create(itemDef.get(), 1));
            }
        }

        // Clean up display entity nearby
        for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
            if (nearby instanceof ItemDisplay id) {
                if (rawKey.equals(id.getPersistentDataContainer().get(KEY_FURNITURE_ID, PersistentDataType.STRING))) {
                    id.remove();
                }
            }
        }

        interaction.remove();

        if (furniture.getBreakSound() != null) {
            try {
                loc.getWorld().playSound(loc, furniture.getBreakSound(), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        Entity vehicle = event.getDismounted();
        if (vehicle instanceof ArmorStand stand) {
            if (stand.getPersistentDataContainer().has(KEY_SEAT, PersistentDataType.BYTE)) {
                stand.remove();
            }
        }
    }
}
