package ru.highcraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class CauldronManager implements Listener {
    private final HighCraftPlugin plugin;
    private final ItemFactory items;
    private final DataStore data;
    private final ConsequenceManager consequences;
    private final Map<String, Brew> brews = new HashMap<>();

    public CauldronManager(HighCraftPlugin plugin, ItemFactory items, DataStore data, ConsequenceManager consequences) {
        this.plugin = plugin;
        this.items = items;
        this.data = data;
        this.consequences = consequences;
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block b = e.getClickedBlock();
        if (b == null) return;
        if (b.getType() != Material.CAULDRON) return;
        
        ItemStack hand = e.getItem();
        if (hand == null) return;

        // Проверка: ведро с жидкостью (забор готовой жидкости)
        String liquid = items.liquidType(hand);
        if (liquid != null) {
            e.setCancelled(true);
            Block target = findAir(e.getPlayer(), b);
            if (target != null) {
                target.setType(Material.WATER);
                data.markWater(target.getLocation(), liquid);
                e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
                e.getPlayer().sendMessage(msg("special-water"));
                scheduleFreeze(target, liquid);
            }
            return;
        }

        // Проверка: ингредиент для котла
        String ingredientId = items.id(hand);
        if (ingredientId == null) return;
        if (!isCauldronIngredient(ingredientId)) return;

        // Проверка: есть ли вода в котле
        if (b.getBlockData() instanceof Levelled) {
            Levelled cauldron = (Levelled) b.getBlockData();
            if (cauldron.getLevel() < 3) {
                e.getPlayer().sendMessage(msg("cauldron-no-water"));
                return;
            }
        }

        e.setCancelled(true);
        
        // Уменьшаем количество ингредиента
        hand.setAmount(hand.getAmount() - 1);
        
        // Добавляем ингредиент в варку
        String key = locationKey(b.getLocation());
        Brew brew = brews.computeIfAbsent(key, k -> new Brew());
        brew.add(ingredientId);
        
        // Меняем цвет воды (здесь можно установить разные цвета для разных ингредиентов)
        // Это упрощённая версия — просто показываем, что котёл занят
        b.setBlockData(Bukkit.createBlockData(Material.WATER_CAULDRON));
        
        e.getPlayer().sendMessage(msg("cauldron-added").replace("{seconds}", 
            String.valueOf(plugin.getConfig().getInt("settings.cauldron.brew-seconds", 15))));

        // Запускаем таймер варки
        if (!brew.scheduled) {
            brew.scheduled = true;
            long ticks = plugin.getConfig().getLong("settings.cauldron.brew-seconds", 15) * 20L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                brew.ready = true;
                // Восстанавливаем обычный котёл
                b.setBlockData(Bukkit.createBlockData(Material.CAULDRON));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(b.getWorld()) && 
                        p.getLocation().distanceSquared(b.getLocation()) < 100) {
                        p.sendMessage(msg("cauldron-ready"));
                    }
                }
            }, ticks);
        }
    }

    private boolean isCauldronIngredient(String id) {
        return id.startsWith("substance:") || 
               id.startsWith("crystal:") || 
               id.startsWith("concentrate:");
    }

    private Block findAir(Player p, Block cauldron) {
        Block b = cauldron.getRelative(p.getFacing());
        return b.getType() == Material.AIR ? b : null;
    }

    private void scheduleFreeze(Block water, String type) {
        long ticks = plugin.getConfig().getLong("settings.cauldron.freeze-seconds", 60) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (water.getType() == Material.WATER && data.water(water.getLocation()) != null) {
                water.setType(Material.ICE);
                String crystalType = waterToCrystalType(type);
                data.removeWater(water.getLocation());
                data.markIce(water.getLocation(), crystalType);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(water.getWorld()) && 
                        p.getLocation().distanceSquared(water.getLocation()) < 36) {
                        p.sendMessage(msg("frozen"));
                    }
                }
            }
        }, ticks);
    }

    private String waterToCrystalType(String liquidType) {
        // Преобразуем тип жидкости в тип кристалла
        switch(liquidType) {
            case "бодрости": return "бодрости";
            case "сна": return "сна";
            case "яда": return "яда";
            case "хаоса": return "хаоса";
            case "тумана": return "тумана";
            case "фокуса": return "фокуса";
            case "эликсира": return "эликсира";
            case "кислоты": return "кислоты";
            case "тени": return "тени";
            case "радуги": return "радуги";
            default: return "бодрости";
        }
    }

    private String locationKey(Location l) {
        return l.getWorld().getUID() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    private String msg(String key) {
        return ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.prefix", "") + 
            plugin.getConfig().getString("messages." + key, key));
    }

    private static final class Brew {
        final List<String> ingredients = new ArrayList<>();
        boolean scheduled;
        boolean ready;
        void add(String s) { ingredients.add(s); }
    }
}
