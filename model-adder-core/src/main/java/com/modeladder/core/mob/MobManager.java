package com.modeladder.core.mob;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomItem;
import com.modeladder.api.content.CustomMob;
import com.modeladder.api.registry.RegistryManager;
import com.modeladder.core.item.ItemFactory;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Spawns and configures custom mobs with attributes, equipment, and identities.
 */
public final class MobManager {

    public static final NamespacedKey KEY_MOB_ID = new NamespacedKey("modeladder", "mob_id");

    private final RegistryManager registryManager;

    public MobManager(@NotNull RegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    @Nullable
    public LivingEntity spawn(@NotNull Location location, @NotNull CustomMob definition) {
        Entity entity = location.getWorld().spawnEntity(location, definition.getBaseEntityType());
        if (!(entity instanceof LivingEntity living)) {
            entity.remove();
            return null;
        }

        // 1. Tag with PDC identity
        living.getPersistentDataContainer().set(KEY_MOB_ID, PersistentDataType.STRING, definition.getKey().asString());

        // 2. Custom name
        living.customName(ItemFactory.parseFormattedText(definition.getDisplayName()));
        living.setCustomNameVisible(true);

        // 3. Set attributes
        AttributeInstance maxHealth = living.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(definition.getMaxHealth());
            living.setHealth(definition.getMaxHealth());
        }

        AttributeInstance speed = living.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(definition.getMovementSpeed());
        }

        AttributeInstance damage = living.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(definition.getAttackDamage());
        }

        // 4. Equip items if mob is Mob
        if (living instanceof Mob mob) {
            if (definition.getMainHandItem() != null) {
                Optional<CustomItem> handItem = registryManager.getItems().get(definition.getMainHandItem());
                handItem.ifPresent(customItem -> mob.getEquipment().setItemInMainHand(ItemFactory.create(customItem, 1)));
            }
            if (definition.getHelmetItem() != null) {
                Optional<CustomItem> helmetItem = registryManager.getItems().get(definition.getHelmetItem());
                helmetItem.ifPresent(customItem -> mob.getEquipment().setHelmet(ItemFactory.create(customItem, 1)));
            }
        }

        return living;
    }

    @NotNull
    public Optional<ContentKey> getMobKey(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity living)) return Optional.empty();
        String rawKey = living.getPersistentDataContainer().get(KEY_MOB_ID, PersistentDataType.STRING);
        if (rawKey == null || !ContentKey.isValid(rawKey)) return Optional.empty();
        return Optional.of(ContentKey.of(rawKey));
    }
}
