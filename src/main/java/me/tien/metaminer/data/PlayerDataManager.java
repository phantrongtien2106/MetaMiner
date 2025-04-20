package me.tien.metaminer.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private static final Map<UUID, Integer> playerPoints = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerUpgrades = new HashMap<>();
    private static final File dataFolder = new File(Bukkit.getPluginManager().getPlugin("MetaMiner").getDataFolder(), "data");
    private static final Map<UUID, Integer> storageLevels = new HashMap<>();

    public static void load(Player player) {
        UUID uuid = player.getUniqueId();
        File file = new File(dataFolder, uuid + ".yml");

        if (!file.exists()) {
            save(player); // Tạo file nếu chưa có
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        playerPoints.put(uuid, config.getInt("points", 0));
        storageLevels.putIfAbsent(uuid, 0); // Default level is 0
        Map<String, Integer> upgrades = new HashMap<>();
        if (config.isConfigurationSection("upgrades")) {
            for (String key : config.getConfigurationSection("upgrades").getKeys(false)) {
                upgrades.put(key, config.getInt("upgrades." + key, 0));
            }
        }
        playerUpgrades.put(uuid, upgrades);
    }

    public static void save(Player player) {
        UUID uuid = player.getUniqueId();
        File file = new File(dataFolder, uuid + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("points", getPoints(uuid));

        Map<String, Integer> upgrades = playerUpgrades.getOrDefault(uuid, new HashMap<>());
        for (Map.Entry<String, Integer> entry : upgrades.entrySet()) {
            config.set("upgrades." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getPoints(UUID uuid) {
        return playerPoints.getOrDefault(uuid, 0);
    }

    public static void addPoints(UUID uuid, int amount) {
        playerPoints.put(uuid, getPoints(uuid) + amount);
    }

    public static void removePoints(UUID uuid, int amount) {
        playerPoints.put(uuid, Math.max(0, getPoints(uuid) - amount));
    }

    public static int getUpgrade(UUID playerId, String type) {
        if ("storage".equalsIgnoreCase(type)) {
            return storageLevels.getOrDefault(playerId, 0); // Default level is 0
        }
        return 0;
    }
    public static void setUpgrade(UUID playerId, String type, int level) {
        if ("storage".equalsIgnoreCase(type)) {
            storageLevels.put(playerId, level);
        }
    }
    public static void incrementUpgrade(UUID uuid, String type) {
        Map<String, Integer> upgrades = playerUpgrades.computeIfAbsent(uuid, k -> new HashMap<>());
        upgrades.put(type, upgrades.getOrDefault(type, 0) + 1);
    }
}
