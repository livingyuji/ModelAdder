package com.modeladder.core.item;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomAbility;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.event.CustomItemAbilityEvent;
import com.modeladder.api.event.CustomItemUseEvent;
import com.modeladder.api.registry.RegistryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Listener handling interactions, ability execution, and durability tracking for custom items.
 */
public final class ItemListener implements Listener {

    private final RegistryManager registryManager;
    private final AbilityExecutor abilityExecutor;

    public ItemListener(@NotNull RegistryManager registryManager, @NotNull AbilityExecutor abilityExecutor) {
        this.registryManager = registryManager;
        this.abilityExecutor = abilityExecutor;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        Optional<ContentKey> keyOpt = ItemFactory.getItemKey(item);
        if (keyOpt.isEmpty()) return;

        Optional<CustomItem> customItemOpt = registryManager.getItems().get(keyOpt.get());
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();
        Player player = event.getPlayer();

        // 1. Fire CustomItemUseEvent
        CustomItemUseEvent useEvent = new CustomItemUseEvent(player, customItem, item, event.getAction());
        Bukkit.getPluginManager().callEvent(useEvent);
        if (useEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // 2. Check Abilities
        boolean isRightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;

        for (ContentKey abilityKey : customItem.getAbilities()) {
            Optional<CustomAbility> abilityOpt = registryManager.getAbilities().get(abilityKey);
            if (abilityOpt.isEmpty()) continue;

            CustomAbility ability = abilityOpt.get();
            if ((isRightClick && ability.getTrigger() == CustomAbility.AbilityTrigger.RIGHT_CLICK) ||
                (isLeftClick && ability.getTrigger() == CustomAbility.AbilityTrigger.LEFT_CLICK)) {

                CustomItemAbilityEvent abilityEvent = new CustomItemAbilityEvent(player, customItem, ability, item);
                Bukkit.getPluginManager().callEvent(abilityEvent);
                if (!abilityEvent.isCancelled()) {
                    abilityExecutor.executeAbility(player, ability);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        Optional<ContentKey> keyOpt = ItemFactory.getItemKey(item);
        if (keyOpt.isEmpty()) return;

        Optional<CustomItem> customItemOpt = registryManager.getItems().get(keyOpt.get());
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();

        for (ContentKey abilityKey : customItem.getAbilities()) {
            Optional<CustomAbility> abilityOpt = registryManager.getAbilities().get(abilityKey);
            if (abilityOpt.isEmpty()) continue;

            CustomAbility ability = abilityOpt.get();
            if (ability.getTrigger() == CustomAbility.AbilityTrigger.HIT_ENTITY) {
                CustomItemAbilityEvent abilityEvent = new CustomItemAbilityEvent(player, customItem, ability, item);
                Bukkit.getPluginManager().callEvent(abilityEvent);
                if (!abilityEvent.isCancelled()) {
                    abilityExecutor.executeAbility(player, ability);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Optional<ContentKey> keyOpt = ItemFactory.getItemKey(item);
        if (keyOpt.isEmpty()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer customDur = pdc.get(ItemFactory.KEY_DURABILITY, PersistentDataType.INTEGER);
        if (customDur == null) return;

        int newDur = customDur - event.getDamage();
        if (newDur <= 0) {
            // Item breaks
            event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            item.setAmount(item.getAmount() - 1);
            event.setCancelled(true);
        } else {
            pdc.set(ItemFactory.KEY_DURABILITY, PersistentDataType.INTEGER, newDur);
            item.setItemMeta(meta);
            event.setCancelled(true); // Prevent vanilla tool durability from overriding custom value
        }
    }
}
