package ru.highcraft;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ConsequenceManager {
    private final HighCraftPlugin plugin;
    private final DataStore data;

    public ConsequenceManager(HighCraftPlugin plugin, DataStore data) {
        this.plugin = plugin;
        this.data = data;
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L * 60L, 20L * 60L);
    }

    public void consume(Player p, double baseDamageHearts) {
        DataStore.PlayerData d = data.player(p.getUniqueId());
        long now = System.currentTimeMillis();
        long withdrawalMs = plugin.getConfig().getLong("settings.consequences.withdrawal-minutes", 40) * 60_000L;
        long degradationMs = plugin.getConfig().getLong("settings.consequences.degradation-minutes", 200) * 60_000L;

        // Если прошло 40 минут — снимаем ломку и восстанавливаем здоровье
        if (now - d.lastUse >= withdrawalMs && d.count > 0) {
            d.count = 0;
            d.health = 20.0;
            if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }
            p.setHealth(20.0);
            clearWithdrawal(p);
            p.sendMessage(msg("withdrawal_cleared"));
            data.save();
        }

        // Проверка деградации (каждые 200 минут)
        if (now - d.lastDegradation >= degradationMs) {
            d.count++;
            d.lastDegradation = now;
            data.save();
        }

        d.count++;
        d.lastUse = now;

        // Расчёт урона
        double extraHearts = Math.min(
            plugin.getConfig().getDouble("settings.consequences.max-dose-damage-hearts", 2.0),
            Math.max(0, d.count / 10) * plugin.getConfig().getDouble("settings.consequences.degradation-step-hearts", 0.5)
        );
        double totalDamage = Math.min(4.0, (baseDamageHearts + extraHearts) * 2.0);
        double minHp = plugin.getConfig().getDouble("settings.consequences.minimum-health-hearts", 1.0) * 2.0;

        // Применяем урон
        double newMaxHealth = Math.max(minHp, d.health - totalDamage);
        d.health = newMaxHealth;
        if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
        }
        p.setHealth(Math.min(p.getHealth(), newMaxHealth));

        data.save();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        long withdrawalMs = plugin.getConfig().getLong("settings.consequences.withdrawal-minutes", 40) * 60_000L;
        long resetMs = plugin.getConfig().getLong("settings.consequences.effect-reset-minutes", 80) * 60_000L;

        for (Player p : Bukkit.getOnlinePlayers()) {
            DataStore.PlayerData d = data.player(p.getUniqueId());
            long idle = now - d.lastUse;
            
            // Если прошло 40 минут — ломка
            if (idle >= withdrawalMs && d.count > 0) {
                applyWithdrawal(p);
                d.count = 0;
                data.save();
            }
            
            // Если прошло 80 минут — сброс эффектов
            if (idle >= resetMs && d.count > 0) {
                clearSubstanceEffects(p);
                p.sendMessage(msg("effects_reset"));
            }
        }
    }

    private void applyWithdrawal(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, Integer.MAX_VALUE, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
        p.sendMessage(msg("withdrawal_start"));
    }

    public void clearWithdrawal(Player p) {
        p.removePotionEffect(PotionEffectType.SLOWNESS);
        p.removePotionEffect(PotionEffectType.WEAKNESS);
        p.removePotionEffect(PotionEffectType.HUNGER);
        p.removePotionEffect(PotionEffectType.CONFUSION);
        p.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    private void clearSubstanceEffects(Player p) {
        clearWithdrawal(p);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.HASTE);
        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.removePotionEffect(PotionEffectType.POISON);
        p.removePotionEffect(PotionEffectType.WITHER);
        p.removePotionEffect(PotionEffectType.REGENERATION);
        p.removePotionEffect(PotionEffectType.ABSORPTION);
        p.removePotionEffect(PotionEffectType.LEVITATION);
        p.removePotionEffect(PotionEffectType.STRENGTH);
        p.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
        p.removePotionEffect(PotionEffectType.JUMP_BOOST);
        p.removePotionEffect(PotionEffectType.RESISTANCE);
        p.removePotionEffect(PotionEffectType.WEAKNESS);
        p.removePotionEffect(PotionEffectType.CONFUSION);
        p.removePotionEffect(PotionEffectType.SLOW_FALLING);
    }

    public void resetPlayerHealth(Player p) {
        DataStore.PlayerData d = data.player(p.getUniqueId());
        if (d.health < 2.0) {
            d.health = 2.0;
        }
        if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(d.health);
        }
        p.setHealth(Math.min(p.getHealth(), d.health));
        data.save();
    }

    private String msg(String key) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.prefix", "") +
            plugin.getConfig().getString("messages." + key, key));
    }
}
