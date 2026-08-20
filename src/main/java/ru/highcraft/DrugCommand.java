package ru.highcraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class DrugCommand implements CommandExecutor, TabCompleter {
    private final HighCraftPlugin plugin;
    private final ItemFactory items;
    private final DataStore data;
    private final RecipeManager recipes;

    public DrugCommand(HighCraftPlugin plugin, ItemFactory items, DataStore data, RecipeManager recipes) {
        this.plugin=plugin; this.items=items; this.data=data; this.recipes=recipes;
    }

    @Override public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        if (a.length==0 || a[0].equalsIgnoreCase("stats")) {
            if (!(s instanceof Player p)) { s.sendMessage("Только игрок."); return true; }
            DataStore.PlayerData d=data.player(p.getUniqueId());
            s.sendMessage(color(plugin.getConfig().getString("messages.prefix","")+
                    plugin.getConfig().getString("messages.stats","stats")
                    .replace("{count}",String.valueOf(d.count))
                    .replace("{health}",String.format(Locale.US,"%.1f",p.getHealth()))
                    .replace("{withdrawal}", "см. эффекты")));
            return true;
        }
        if (a[0].equalsIgnoreCase("reload")) {
            if (!s.hasPermission("highcraft.admin")) { s.sendMessage(color("&cНет прав.")); return true; }
            plugin.reloadHighCraft(); s.sendMessage(color("&aКонфигурация перезагружена.")); return true;
        }
        if (a[0].equalsIgnoreCase("give")) {
            if (!s.hasPermission("highcraft.admin") || a.length<3) { s.sendMessage("/drug give <игрок> <id> [количество]"); return true; }
            Player target=Bukkit.getPlayerExact(a[1]);
            if(target==null){s.sendMessage("&cИгрок не найден.");return true;}
            int amount=a.length>=4?parse(a[3],1):1;
            ItemStack item;
            String id=a[2];
            if(id.startsWith("crystal:")) item=items.crystal(id.substring(8),id.substring(8));
            else item=items.substance(id.replace("substance:",""),id.replace("substance:",""),org.bukkit.Material.PAPER);
            item.setAmount(Math.max(1,Math.min(64,amount)));
            target.getInventory().addItem(item);
            s.sendMessage(color("&aВыдано."));
            return true;
        }
        return true;
    }

    private int parse(String s,int d){try{return Integer.parseInt(s);}catch(Exception e){return d;}}
    private String color(String s){return ChatColor.translateAlternateColorCodes('&',s);}

    @Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if(args.length==1) return List.of("stats","give","reload");
        if(args.length==2 && args[0].equalsIgnoreCase("give"))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        return Collections.emptyList();
    }
}
