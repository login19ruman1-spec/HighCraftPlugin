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
    private final List<NamespacedKey> registeredKeys = new ArrayList<>();

    public RecipeManager(HighCraftPlugin plugin, ItemFactory items) {
        this.plugin = plugin;
        this.items = items;
    }

    public void register() {
        // Удаляем старые рецепты (безопасно для всех версий)
        for (NamespacedKey key : registeredKeys) {
            try {
                Bukkit.removeRecipe(key);
            } catch (Exception ignored) {}
        }
        registeredKeys.clear();

        // РЕЦЕПТЫ УРОВНЯ 1 (Верстак) — 5 штук
        registerShaped("bodyak", "&5Бодряк",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.SUGAR, 'C', Material.SWEET_BERRIES));

        registerShaped("sonnik", "&5Сонник",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.RED_MUSHROOM, 'C', Material.MILK_BUCKET));

        registerShaped("tuman", "&5Туман",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.COBWEB, 'C', Material.BROWN_MUSHROOM));

        registerShaped("yad", "&5Яд",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.POISONOUS_POTATO, 'C', Material.BROWN_MUSHROOM));

        registerShaped("raspad", "&5Распад",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.ROTTEN_FLESH, 'C', Material.SUGAR));

        // РЕЦЕПТЫ УРОВНЯ 2 (4 штуки)
        registerShaped("fokus", "&5Фокус",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.GLOWSTONE_DUST, 'C', Material.SPIDER_EYE));

        registerShaped("tyazhest", "&5Тяжесть",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.NETHER_WART, 'C', Material.GLOWSTONE_DUST));

        registerShaped("zerkalo", "&5Зеркало",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.COBWEB, 'C', Material.GLOWSTONE_DUST));

        registerShaped("uskoritel", "&5Ускоритель",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.BONE_MEAL, 'C', Material.SUGAR));

        // РЕЦЕПТЫ УРОВНЯ 3 (3 штуки)
        registerShaped("eliksir", "&5Эликсир",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.GOLDEN_CARROT, 'C', Material.POTION));

        registerShaped("raduga", "&5Радуга",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.GLOWSTONE_DUST, 'C', Material.POTION));

        registerShaped("khaos", "&5Хаос",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.SWEET_BERRIES, 'C', Material.POTION));

        // ДОП. КОМБИНАЦИИ (3 штуки)
        registerShaped("ten", "&5Тень",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.NETHER_BRICK, 'C', Material.RED_MUSHROOM));

        registerShaped("cvetnoy_dym", "&5Цветной дым",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.GLOWSTONE_DUST, 'C', Material.SPIDER_EYE));

        registerShaped("kislota", "&5Кислота",
            new String[]{" A ", " B ", " C "},
            Map.of('A', Material.PAPER, 'B', Material.CACTUS, 'C', Material.POTION));

        plugin.getLogger().info("§aЗарегистрировано 15 рецептов!");
    }

    private void registerShaped(String id, String name, String[] shape, Map<Character, Material> ingredients) {
        ItemStack result = items.substance(id, name, Material.PAPER);
        NamespacedKey key = new NamespacedKey(plugin, "craft_" + id);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape[0], shape[1], shape[2]);
        for (var entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        try {
            Bukkit.addRecipe(recipe);
            registeredKeys.add(key);
            plugin.getLogger().info("Рецепт зарегистрирован: " + id);
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось зарегистрировать рецепт " + id + ": " + e.getMessage());
        }
    }
}
