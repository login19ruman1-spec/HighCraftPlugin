package ru.highcraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;

public final class RecipeManager {
    private final HighCraftPlugin plugin;
    private final ItemFactory items;
    private final List<NamespacedKey> keys = new ArrayList<>();

    public RecipeManager(HighCraftPlugin plugin, ItemFactory items) {
        this.plugin = plugin; this.items = items;
    }

    public void register() {
        for (NamespacedKey key : keys) Bukkit.removeRecipe(key);
        keys.clear();

        registerSimple("бодряк","B","&dБодряк", Material.PAPER, Material.SUGAR, Material.SWEET_BERRIES);
        registerSimple("сонник","S","&dСонник", Material.PAPER, Material.RED_MUSHROOM, Material.MILK_BUCKET);
        registerSimple("туман","T","&dТуман", Material.PAPER, Material.COBWEB, Material.BROWN_MUSHROOM);
        registerSimple("яд","Y","&dЯд", Material.PAPER, Material.POISONOUS_POTATO, Material.BROWN_MUSHROOM);
        registerSimple("распад","R","&dРаспад", Material.PAPER, Material.ROTTEN_FLESH, Material.SUGAR);

        registerSimple("фокус","F","&dФокус", Material.PAPER, Material.GLOWSTONE_DUST, Material.SPIDER_EYE);
        registerSimple("тяжесть","G","&dТяжесть", Material.PAPER, Material.NETHER_WART, Material.GLOWSTONE_DUST);
        registerSimple("зеркало","Z","&dЗеркало", Material.PAPER, Material.COBWEB, Material.GLOWSTONE_DUST);
        registerSimple("ускоритель","U","&dУскоритель", Material.PAPER, Material.BONE_MEAL, Material.SUGAR);

        registerSimple("эликсир","E","&dЭликсир", Material.PAPER, Material.GOLDEN_CARROT, Material.POTION);
        registerSimple("радуга","A","&dРадуга", Material.PAPER, Material.GLOWSTONE_DUST, Material.POTION);
        registerSimple("хаос","C","&dХаос", Material.PAPER, Material.SWEET_BERRIES, Material.POTION);
    }

    private void registerSimple(String id, String keyChar, String name, Material a, Material b, Material c) {
        ItemStack result = items.substance(id, ChatColorName(name), Material.PAPER);
        NamespacedKey key = new NamespacedKey(plugin, "craft_" + id);
        ShapedRecipe r = new ShapedRecipe(key, result);
        r.shape("ABC"," B ","   ");
        r.setIngredient('A', a); r.setIngredient('B', b); r.setIngredient('C', c);
        Bukkit.addRecipe(r); keys.add(key);
    }

    private String ChatColorName(String s) {
        return s.replace("&d","");
    }
}
