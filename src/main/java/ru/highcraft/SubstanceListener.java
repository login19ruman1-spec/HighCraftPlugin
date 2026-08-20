package ru.highcraft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class SubstanceListener implements Listener {
    private final HighCraftPlugin plugin;
    private final ItemFactory items;
    private final DataStore data;
    private final ConsequenceManager consequences;

    public SubstanceListener(HighCraftPlugin plugin, ItemFactory items, DataStore data, ConsequenceManager consequences) {
        this.plugin=plugin; this.items=items; this.data=data; this.consequences=consequences;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || !e.getAction().isRightClick()) return;
        ItemStack item=e.getItem();
        String id=items.id(item);
        if (id==null) return;

        if (id.startsWith("substance:") || id.startsWith("crystal:")) {
            e.setCancelled(true);
            Player p=e.getPlayer();
            String name=item.getItemMeta().getDisplayName();
            consumeOne(item,p);
            p.sendMessage(msg("used").replace("{item}", name));
        }
    }

    private void consumeOne(ItemStack item, Player p) {
        String id=items.id(item);
        if (id.startsWith("crystal:")) {
            applyCrystal(id.substring(8),p);
        } else {
            applySubstance(id.substring(10),p);
        }
        consequences.consume(p, id.startsWith("crystal:") ? 1.0 : 0.5);
        item.setAmount(item.getAmount()-1);
    }

    private void applySubstance(String id, Player p) {
        switch(id) {
            case "бодряк" -> { effect(p,PotionEffectType.SPEED,60,0); effect(p,PotionEffectType.HUNGER,30,0); }
            case "сонник" -> { effect(p,PotionEffectType.SLOWNESS,120,0); effect(p,PotionEffectType.RESISTANCE,60,0); }
            case "туман" -> { effect(p,PotionEffectType.BLINDNESS,60,0); effect(p,PotionEffectType.INVISIBILITY,30,0); }
            case "яд" -> { effect(p,PotionEffectType.POISON,60,0); effect(p,PotionEffectType.HUNGER,30,0); }
            case "распад" -> { effect(p,PotionEffectType.WITHER,20,0); effect(p,PotionEffectType.SLOW_FALLING,60,0); }
            default -> {}
        }
    }

    private void applyCrystal(String id, Player p) {
        switch(id) {
            case "бодрости" -> { effect(p,PotionEffectType.SPEED,120,0); effect(p,PotionEffectType.HASTE,60,0); }
            case "сна" -> { effect(p,PotionEffectType.SLOWNESS,180,0); effect(p,PotionEffectType.RESISTANCE,120,0); }
            case "яда" -> { effect(p,PotionEffectType.POISON,120,0); effect(p,PotionEffectType.HUNGER,60,0); }
            case "хаоса" -> { effect(p,PotionEffectType.WITHER,20,0); effect(p,PotionEffectType.SPEED,60,0); }
            case "тумана" -> { effect(p,PotionEffectType.BLINDNESS,120,0); effect(p,PotionEffectType.INVISIBILITY,60,0); }
            case "фокуса" -> { effect(p,PotionEffectType.NIGHT_VISION,300,0); effect(p,PotionEffectType.SPEED,120,0); }
            case "эликсира" -> { effect(p,PotionEffectType.ABSORPTION,240,0); effect(p,PotionEffectType.REGENERATION,30,0); }
            case "кислоты" -> { effect(p,PotionEffectType.POISON,60,0); effect(p,PotionEffectType.WITHER,20,0); effect(p,PotionEffectType.REGENERATION,30,0); }
            case "тени" -> { effect(p,PotionEffectType.INVISIBILITY,300,0); effect(p,PotionEffectType.STRENGTH,120,0); }
            case "радуги" -> { effect(p,PotionEffectType.LEVITATION,20,0); effect(p,PotionEffectType.REGENERATION,60,0); effect(p,PotionEffectType.DOLPHINS_GRACE,120,0); }
        }
    }

    private void effect(Player p, PotionEffectType type, int seconds, int amp) {
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(type, seconds*20, amp));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        String type=data.ice(e.getBlock().getLocation());
        if (type==null || e.getBlock().getType()!=Material.ICE) return;
        e.setDropItems(false);
        int amount=dropAmount();
        ItemStack crystal=items.crystal(type,type);
        crystal.setAmount(amount);
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), crystal);
        data.removeIce(e.getBlock().getLocation());
    }

    private int dropAmount() {
        double x=Math.random()*100;
        int one=plugin.getConfig().getInt("settings.drop.one",25);
        int two=one+plugin.getConfig().getInt("settings.drop.two",35);
        int three=two+plugin.getConfig().getInt("settings.drop.three",25);
        return x<one?1:x<two?2:x<three?3:4;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player p=e.getPlayer();
            if (p.getHealth() > 2.0) p.setHealth(2.0);
        });
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent e) {
        Player p=e.getPlayer();
        p.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    private String msg(String key) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix","") + plugin.getConfig().getString("messages."+key,key));
    }
}
