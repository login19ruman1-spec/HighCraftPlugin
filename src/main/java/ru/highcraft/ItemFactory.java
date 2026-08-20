package ru.highcraft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ItemFactory {
    private final HighCraftPlugin plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey crystalKey;
    private final NamespacedKey cauldronLiquidKey;

    public ItemFactory(HighCraftPlugin plugin) {
        this.plugin = plugin;
        idKey = new NamespacedKey(plugin, "item_id");
        crystalKey = new NamespacedKey(plugin, "crystal_type");
        cauldronLiquidKey = new NamespacedKey(plugin, "cauldron_liquid");
    }

    public ItemStack custom(Material material, String id, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack substance(String id, String name, Material base) {
        return custom(base, "substance:" + id, "&d" + name);
    }

    public ItemStack crystal(String id, String name) {
        ItemStack item = custom(Material.AMETHYST_SHARD, "crystal:" + id, "&bКристалл " + name);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(crystalKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack liquidBucket(String type) {
        ItemStack item = custom(Material.WATER_BUCKET, "liquid:" + type, "&bВедро с эликсиром");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(cauldronLiquidKey, PersistentDataType.STRING, type);
        item.setItemMeta(meta);
        return item;
    }

    public String id(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public String crystalType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(crystalKey, PersistentDataType.STRING);
    }

    public String liquidType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(cauldronLiquidKey, PersistentDataType.STRING);
    }
}
