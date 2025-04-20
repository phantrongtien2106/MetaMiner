package me.tien.metaminer.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private Map<Material, Integer> oreRates = new HashMap<>();
    private Map<Material, Integer> pointValues = new HashMap<>();
    private int totalWeight = 0;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        // Load ore rates
        oreRates.clear();
        totalWeight = 0;

        if (config.isConfigurationSection("ores")) {
            for (String key : config.getConfigurationSection("ores").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    int weight = config.getInt("ores." + key);
                    oreRates.put(material, weight);
                    totalWeight += weight;
                    logger.info("Loaded ore rate: " + key + " = " + weight);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid material in config: " + key);
                }
            }
        }

        // Load point values
        pointValues.clear();

        if (config.isConfigurationSection("points")) {
            for (String key : config.getConfigurationSection("points").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    int points = config.getInt("points." + key);
                    pointValues.put(material, points);
                    logger.info("Loaded point value: " + key + " = " + points);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid material in config: " + key);
                }
            }
        }
        // Load NFT drop chances
        nftDropChances.clear();
        if (config.isConfigurationSection("nft_drops")) {
            for (String key : config.getConfigurationSection("nft_drops").getKeys(false)) {
                int chance = config.getInt("nft_drops." + key + ".chance", 0);
                nftDropChances.put(key, chance);
            }
        }

    }

    public Material getRandomOre() {
        if (totalWeight <= 0) return Material.STONE;

        int random = (int) (Math.random() * totalWeight);
        int currentWeight = 0;

        for (Map.Entry<Material, Integer> entry : oreRates.entrySet()) {
            currentWeight += entry.getValue();
            if (random < currentWeight) {
                return entry.getKey();
            }
        }

        return Material.STONE; // Default fallback
    }
    public Map<Material, Integer> getOreRates() {
        return oreRates;
    }
    private final Map<String, Integer> nftDropChances = new HashMap<>();
    public Map<String, Integer> getNftDropChances() {
        return nftDropChances;
    }

    public int getPointValue(Material material) {
        return pointValues.getOrDefault(material, 0);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }
}