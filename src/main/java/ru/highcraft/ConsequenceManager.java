package ru.highcraft;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

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
        long degradationMs = plugin.getConfig().getLong("settings.consequences.degradation-minutes", 200) * 60_000L;
        long withdrawalMs = plugin.getConfig().getLong("settings.consequences.withdrawal-minutes", 40) * 60_000L;

        if (now - d.lastUse >= withdrawalMs) {
            d.count = 0;
            d.health = 20.0;
            p.setHealth(Math.min(20.0, p.getHealth()));
            clearWithdrawal(p);
            p.sendMessage(msg("withdrawal"));
        }

        if (now - d.lastDegradation >= degradationMs) {
            d.count++;
            d.lastDegradation = now;
        }
        d.count++;
        d.lastUse = now;

        double extraHearts = Math.min(
                plugin.getConfig().getDouble("settings.consequences.max-dose-damage-hearts", 2.0),
                Math.max(0, d.count / 10) * plugin.getConfig().getDouble("settings.consequences.degradation-step-hearts", 0.5)
        );
        double damageHp = Math.min(4.0, (baseDamageHearts + extraHearts) * 2.0);
        double minHp = plugin.getConfig().getDouble("settings.consequences.minimum-health-hearts", 1.0) * 2.0;
        p.setHealth(Math.max(minHp, p.getHealth() - damageHp));
        // ИСПРАВЛЕНО: MAX_HEALTH → GENERIC_MAX_HEALTH
        d.health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        long withdrawalMs = plugin.getConfig().getLong("settings.consequences.withdrawal-minutes", 40) * 60_000L;
        long resetMs = plugin.getConfig().getLong("settings.consequences.effect-reset-minutes", 80) * 60_000L;

        for (Player p : Bukkit.getOnlinePlayers()) {
            DataStore.PlayerData d = data.player(p.getUniqueId());
            long idle = now - d.lastUse;
            if (idle >= withdrawalMs) applyWithdrawal(p);
            if (idle >= resetMs) clearSubstanceEffects(p);
        }
    }

    private void applyWithdrawal(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 50, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 50, 2));
        // ИСПРАВЛЕНО: CONFUSION → NAUSEA
        p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 50, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
    }

    public void clearWithdrawal(Player p) {
        // ИСПРАВЛЕНО: CONFUSION → NAUSEA
        for (PotionEffectType t : new PotionEffectType[]{PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS,
                PotionEffectType.HUNGER, PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS})
            p.removePotionEffect(t);
    }

    private void clearSubstanceEffects(Player p) {
        clearWithdrawal(p);
        for (PotionEffectType t : PotionEffectType.values()) {
            if (t != null) p.removePotionEffect(t);
        }
    }

    private String msg(String key) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix","") + plugin.getConfig().getString("messages."+key,key));
    }
}
