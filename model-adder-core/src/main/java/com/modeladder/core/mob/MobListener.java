package com.modeladder.core.mob;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomBlock;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.content.CustomMob;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.core.item.ItemFactory;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles custom mob deaths and custom drop tables.
 */
public final class MobListener implements Listener {

    private final RegistryManager registryManager;
    private final MobManager mobManager;

    public MobListener(@NotNull RegistryManager registryManager, @NotNull MobManager mobManager) {
        this.registryManager = registryManager;
        this.mobManager = mobManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Optional<ContentKey> keyOpt = mobManager.getMobKey(entity);
        if (keyOpt.isEmpty()) return;

        Optional<CustomMob> mobDefOpt = registryManager.getMobs().get(keyOpt.get());
        if (mobDefOpt.isEmpty()) return;

        CustomMob mob = mobDefOpt.get();

        // Clear vanilla drops
        event.getDrops().clear();

        // Custom drops
        Location loc = entity.getLocation();
        for (CustomBlock.BlockDrop drop : mob.getDrops()) {
            if (ThreadLocalRandom.current().nextDouble() <= drop.chance()) {
                int count = ThreadLocalRandom.current().nextInt(drop.minAmount(), drop.maxAmount() + 1);
                Optional<CustomItem> dropItem = registryManager.getItems().get(drop.itemKey());
                if (dropItem.isPresent() && count > 0) {
                    event.getDrops().add(ItemFactory.create(dropItem.get(), count));
                }
            }
        }
    }
}
