package ru.highcraft;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class DataStore {
    private final HighCraftPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Map<String, String> specialIce = new HashMap<>();
    private final Map<String, String> specialWater = new HashMap<>();

    public DataStore(HighCraftPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            yaml = new YamlConfiguration();
            return;
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getConfigurationSection("players") == null ? Collections.emptySet()
                : yaml.getConfigurationSection("players").getKeys(false)) {
            UUID uuid;
            try { uuid = UUID.fromString(key); } catch (Exception ignored) { continue; }
            String path = "players." + key;
            PlayerData d = new PlayerData();
            d.count = yaml.getInt(path + ".count", 0);
            d.health = yaml.getDouble(path + ".health", 20.0);
            d.lastUse = yaml.getLong(path + ".lastUse", System.currentTimeMillis());
            d.lastDegradation = yaml.getLong(path + ".lastDegradation", d.lastUse);
            players.put(uuid, d);
        }
        loadMap("ice", specialIce);
        loadMap("water", specialWater);
    }

    private void loadMap(String section, Map<String,String> map) {
        if (yaml.getConfigurationSection(section) == null) return;
        for (String k : yaml.getConfigurationSection(section).getKeys(false))
            map.put(k, yaml.getString(section + "." + k));
    }

    public PlayerData player(UUID uuid) { return players.computeIfAbsent(uuid, k -> new PlayerData()); }

    public void markIce(Location l, String type) { specialIce.put(key(l), type); }
    public String ice(Location l) { return specialIce.get(key(l)); }
    public void removeIce(Location l) { specialIce.remove(key(l)); }

    public void markWater(Location l, String type) { specialWater.put(key(l), type); }
    public String water(Location l) { return specialWater.get(key(l)); }
    public void removeWater(Location l) { specialWater.remove(key(l)); }

    private String key(Location l) {
        return l.getWorld().getUID() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public void save() {
        if (yaml == null) yaml = new YamlConfiguration();
        yaml.set("players", null);
        for (Map.Entry<UUID,PlayerData> e : players.entrySet()) {
            String p = "players." + e.getKey();
            PlayerData d = e.getValue();
            yaml.set(p + ".count", d.count);
            yaml.set(p + ".health", d.health);
            yaml.set(p + ".lastUse", d.lastUse);
            yaml.set(p + ".lastDegradation", d.lastDegradation);
        }
        yaml.set("ice", null);
        for (var e : specialIce.entrySet()) yaml.set("ice." + e.getKey(), e.getValue());
        yaml.set("water", null);
        for (var e : specialWater.entrySet()) yaml.set("water." + e.getKey(), e.getValue());
        try { yaml.save(file); } catch (IOException ex) { plugin.getLogger().warning("Cannot save data.yml: " + ex.getMessage()); }
    }

    public static final class PlayerData {
        public int count;
        public double health = 20.0;
        public long lastUse = System.currentTimeMillis();
        public long lastDegradation = System.currentTimeMillis();
    }
}
