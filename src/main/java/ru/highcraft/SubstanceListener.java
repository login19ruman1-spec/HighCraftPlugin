package ru.highcraft;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public final class SubstanceListener implements Listener {
    private final HighCraftPlugin plugin;
    private final ItemFactory items;
    private final DataStore data;
    private final ConsequenceManager consequences;
    private final Random random = new Random();

    public SubstanceListener(HighCraftPlugin plugin, ItemFactory items, DataStore data, ConsequenceManager consequences) {
        this.plugin = plugin;
        this.items = items;
        this.data = data;
        this.consequences = consequences;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!e.getAction().isRightClick()) return;
        
        ItemStack item = e.getItem();
        if (item == null) return;
        
        String id = items.id(item);
        if (id == null) return;

        if (id.startsWith("substance:") || id.startsWith("crystal:")) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            String name = item.getItemMeta().getDisplayName();
            
            // Проверка: если игрок в ломке — снимаем ломку и не даём эффектов
            DataStore.PlayerData d = data.player(p.getUniqueId());
            long now = System.currentTimeMillis();
            long withdrawalMs = plugin.getConfig().getLong("settings.consequences.withdrawal-minutes", 40) * 60_000L;
            
            if (now - d.lastUse >= withdrawalMs && d.count > 0) {
                consequences.clearWithdrawal(p);
                d.count = 0;
                d.health = 20.0;
                if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                }
                p.setHealth(20.0);
                p.sendMessage(msg("withdrawal_cleared"));
                data.save();
                item.setAmount(item.getAmount() - 1);
                return;
            }
            
            // Обычное употребление
            consumeOne(item, p);
            p.sendMessage(msg("used").replace("{item}", name));
        }
    }

    private void consumeOne(ItemStack item, Player p) {
        String id = items.id(item);
        double baseDamage = 0.5;
        
        if (id.startsWith("crystal:")) {
            String crystalType = id.substring(8);
            applyCrystal(crystalType, p);
            baseDamage = getCrystalDamage(crystalType);
        } else {
            String substanceType = id.substring(10);
            applySubstance(substanceType, p);
            baseDamage = getSubstanceDamage(substanceType);
        }
        
        consequences.consume(p, baseDamage);
        item.setAmount(item.getAmount() - 1);
    }

    private double getSubstanceDamage(String id) {
        switch(id) {
            case "эликсир":
            case "радуга":
            case "хаос":
                return 1.5;
            case "тень":
            case "цветной_дым":
            case "кислота":
                return 1.0;
            default:
                return 0.5;
        }
    }

    private double getCrystalDamage(String id) {
        switch(id) {
            case "кислоты":
                return 2.0;
            case "эликсира":
            case "тени":
            case "радуги":
                return 1.5;
            case "яда":
            case "хаоса":
            case "фокуса":
                return 1.0;
            default:
                return 0.5;
        }
    }

    private void applySubstance(String id, Player p) {
        switch(id) {
            case "бодряк" -> {
                effect(p, PotionEffectType.SPEED, 60, 0);
                effect(p, PotionEffectType.HUNGER, 30, 0);
            }
            case "сонник" -> {
                effect(p, PotionEffectType.SLOWNESS, 120, 0);
                effect(p, PotionEffectType.RESISTANCE, 60, 0);
            }
            case "туман" -> {
                effect(p, PotionEffectType.BLINDNESS, 60, 0);
                effect(p, PotionEffectType.INVISIBILITY, 30, 0);
            }
            case "яд" -> {
                effect(p, PotionEffectType.POISON, 60, 0);
                effect(p, PotionEffectType.HUNGER, 30, 0);
            }
            case "распад" -> {
                effect(p, PotionEffectType.WITHER, 20, 0);
                effect(p, PotionEffectType.SLOW_FALLING, 60, 0);
            }
            case "фокус" -> {
                effect(p, PotionEffectType.NIGHT_VISION, 120, 0);
                effect(p, PotionEffectType.BLINDNESS, 60, 0);
            }
            case "тяжесть" -> {
                effect(p, PotionEffectType.WEAKNESS, 120, 0);
                effect(p, PotionEffectType.RESISTANCE, 30, 0);
            }
            case "зеркало" -> {
                effect(p, PotionEffectType.NAUSEA, 60, 0);
                effect(p, PotionEffectType.DOLPHINS_GRACE, 30, 0);
            }
            case "ускоритель" -> {
                effect(p, PotionEffectType.HASTE, 60, 0);
                effect(p, PotionEffectType.SLOWNESS, 30, 0);
            }
            case "эликсир" -> {
                effect(p, PotionEffectType.ABSORPTION, 120, 0);
                effect(p, PotionEffectType.BLINDNESS, 30, 0);
            }
            case "радуга" -> {
                effect(p, PotionEffectType.LEVITATION, 10, 0);
                effect(p, PotionEffectType.REGENERATION, 20, 0);
            }
            case "хаос" -> {
                effect(p, PotionEffectType.WITHER, 10, 0);
                effect(p, PotionEffectType.SPEED, 30, 0);
            }
            case "тень" -> {
                effect(p, PotionEffectType.INVISIBILITY, 120, 0);
                effect(p, PotionEffectType.WEAKNESS, 60, 0);
            }
            case "цветной_дым" -> {
                effect(p, PotionEffectType.CONFUSION, 120, 0);
                effect(p, PotionEffectType.JUMP_BOOST, 60, 0);
            }
            case "кислота" -> {
                effect(p, PotionEffectType.POISON, 30, 0);
                effect(p, PotionEffectType.REGENERATION, 10, 0);
            }
        }
    }

    private void applyCrystal(String id, Player p) {
        switch(id) {
            case "бодрости" -> {
                effect(p, PotionEffectType.SPEED, 120, 0);
                effect(p, PotionEffectType.HASTE, 60, 0);
            }
            case "сна" -> {
                effect(p, PotionEffectType.SLOWNESS, 180, 0);
                effect(p, PotionEffectType.RESISTANCE, 120, 0);
            }
            case "яда" -> {
                effect(p, PotionEffectType.POISON, 120, 0);
                effect(p, PotionEffectType.HUNGER, 60, 0);
            }
            case "хаоса" -> {
                effect(p, PotionEffectType.WITHER, 20, 0);
                effect(p, PotionEffectType.SPEED, 60, 0);
            }
            case "тумана" -> {
                effect(p, PotionEffectType.BLINDNESS, 120, 0);
                effect(p, PotionEffectType.INVISIBILITY, 60, 0);
            }
            case "фокуса" -> {
                effect(p, PotionEffectType.NIGHT_VISION, 300, 0);
                effect(p, PotionEffectType.SPEED, 120, 0);
            }
            case "эликсира" -> {
                effect(p, PotionEffectType.ABSORPTION, 240, 0);
                effect(p, PotionEffectType.REGENERATION, 30, 0);
            }
            case "кислоты" -> {
                effect(p, PotionEffectType.POISON, 60, 0);
                effect(p, PotionEffectType.WITHER, 20, 0);
                effect(p, PotionEffectType.REGENERATION, 30, 0);
            }
            case "тени" -> {
                effect(p, PotionEffectType.INVISIBILITY, 300, 0);
                effect(p, PotionEffectType.STRENGTH, 120, 0);
            }
            case "радуги" -> {
                effect(p, PotionEffectType.LEVITATION, 20, 0);
                effect(p, PotionEffectType.REGENERATION, 60, 0);
                effect(p, PotionEffectType.DOLPHINS_GRACE, 120, 0);
            }
        }
    }

    private void effect(Player p, PotionEffectType type, int seconds, int amp) {
        if (type != null) {
            p.addPotionEffect(new PotionEffect(type, seconds * 20, amp));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.ICE) return;
        
        String type = data.ice(e.getBlock().getLocation());
        if (type == null) return;
        
        e.setDropItems(false);
        int amount = dropAmount();
        ItemStack crystal = items.crystal(type, type);
        crystal.setAmount(amount);
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), crystal);
        data.removeIce(e.getBlock().getLocation());
        
        Player p = e.getPlayer();
        p.sendMessage(msg("crystal_drop").replace("{amount}", String.valueOf(amount)));
    }

    private int dropAmount() {
        double x = random.nextDouble() * 100;
        int one = plugin.getConfig().getInt("settings.drop.one", 25);
        int two = one + plugin.getConfig().getInt("settings.drop.two", 35);
        int three = two + plugin.getConfig().getInt("settings.drop.three", 25);
        if (x < one) return 1;
        if (x < two) return 2;
        if (x < three) return 3;
        return 4;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player p = e.getPlayer();
            consequences.resetPlayerHealth(p);
            p.sendMessage(msg("respawn"));
        });
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent e) {
        Player p = e.getPlayer();
        // Снимаем слепоту от веществ (но не от ломки)
        if (p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            // Проверяем, не ломка ли это (ломка имеет бесконечную длительность)
            for (PotionEffect effect : p.getActivePotionEffects()) {
                if (effect.getType() == PotionEffectType.BLINDNESS && effect.getDuration() < Integer.MAX_VALUE - 10) {
                    p.removePotionEffect(PotionEffectType.BLINDNESS);
                    p.sendMessage(msg("blindness_cured"));
                    break;
                }
            }
        }
    }

    private String msg(String key) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.prefix", "") +
            plugin.getConfig().getString("messages." + key, key));
    }
}
