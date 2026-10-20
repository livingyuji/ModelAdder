package com.modeladder.core.item;

import com.modeladder.api.content.ContentKey;
import com.modeladder.api.content.CustomAbility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles cooldown tracking and safe execution of custom item abilities.
 */
public final class AbilityExecutor {

    private final Map<UUID, Map<ContentKey, Long>> cooldowns = new ConcurrentHashMap<>();

    public boolean isOnCooldown(@NotNull Player player, @NotNull ContentKey abilityKey) {
        Map<ContentKey, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;
        Long expiry = playerCooldowns.get(abilityKey);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(abilityKey);
            return false;
        }
        return true;
    }

    public double getRemainingCooldownSeconds(@NotNull Player player, @NotNull ContentKey abilityKey) {
        Map<ContentKey, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0.0;
        Long expiry = playerCooldowns.get(abilityKey);
        if (expiry == null) return 0.0;
        long remainingMs = expiry - System.currentTimeMillis();
        return remainingMs > 0 ? remainingMs / 1000.0 : 0.0;
    }

    public void setCooldown(@NotNull Player player, @NotNull ContentKey abilityKey, double seconds) {
        if (seconds <= 0) return;
        long expiry = System.currentTimeMillis() + (long) (seconds * 1000.0);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>()).put(abilityKey, expiry);
    }

    public void executeAbility(@NotNull Player player, @NotNull CustomAbility ability) {
        ContentKey key = ability.getKey();

        // 1. Check cooldown
        if (isOnCooldown(player, key)) {
            double remaining = getRemainingCooldownSeconds(player, key);
            player.sendActionBar(Component.text("Ability on cooldown: " + String.format("%.1f", remaining) + "s", NamedTextColor.RED));
            return;
        }

        // 2. Sound effect
        if (ability.getSound() != null) {
            try {
                player.getWorld().playSound(player.getLocation(), ability.getSound(), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }

        // 3. Particle effect
        if (ability.getParticle() != null) {
            try {
                String pName = ability.getParticle().replace("minecraft:", "").toUpperCase();
                Particle particle = Particle.valueOf(pName);
                player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.0, 0), 15, 0.5, 0.5, 0.5, 0.05);
            } catch (Exception ignored) {}
        }

        // 4. Configured action effects
        Location loc = player.getLocation();
        for (String effect : ability.getEffects()) {
            String[] parts = effect.split(":");
            if (parts.length == 0) continue;
            String type = parts[0].toLowerCase();

            switch (type) {
                case "damage" -> {
                    // damage:nearby:<radius>:<amount>
                    if (parts.length >= 4 && "nearby".equalsIgnoreCase(parts[1])) {
                        double radius = Double.parseDouble(parts[2]);
                        double dmg = Double.parseDouble(parts[3]);
                        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                            if (e instanceof Damageable d && !e.equals(player)) {
                                d.damage(dmg, player);
                            }
                        }
                    }
                }
                case "launch" -> {
                    // launch:<power>
                    double power = parts.length >= 2 ? Double.parseDouble(parts[1]) : 1.0;
                    Vector dir = player.getLocation().getDirection().normalize().multiply(power);
                    dir.setY(Math.min(1.5, dir.getY() + 0.3 * power));
                    player.setVelocity(dir);
                }
                case "potion" -> {
                    // potion:<TYPE>:<seconds>:<amplifier>
                    if (parts.length >= 4) {
                        PotionEffectType pType = PotionEffectType.getByName(parts[1].toUpperCase());
                        if (pType != null) {
                            int sec = Integer.parseInt(parts[2]);
                            int amp = Integer.parseInt(parts[3]);
                            player.addPotionEffect(new PotionEffect(pType, sec * 20, amp, false, true, true));
                        }
                    }
                }
            }
        }

        // 5. Apply cooldown
        setCooldown(player, key, ability.getCooldownSeconds());
    }

    public void clearAll() {
        cooldowns.clear();
    }
}
