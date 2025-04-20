package me.tien.metaminer.util;

import me.tien.metaminer.MetaMiner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class ExternalNftReader {

    private static final File nftPluginFolder = new File("plugins/NFTPlugin/metadata");

    public static void tryDropNFTs(Player player, MetaMiner plugin) {
        FileConfiguration config = plugin.getConfig();
        if (!nftPluginFolder.exists()) return;

        File[] files = nftPluginFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            String name = file.getName().replace(".json", "");
            double chance = config.getDouble("nft_drops." + name, 0.0);

            if (Math.random() * 100 < chance) {
                ItemStack item = loadNft(file);
                if (item != null) {
                    player.getInventory().addItem(item);
                    player.sendMessage("§aBạn vừa nhận được NFT: §6" + item.getItemMeta().getDisplayName());
                }
            }
        }
    }

    private static ItemStack loadNft(File file) {
        try {
            String raw = new String(Files.readAllBytes(file.toPath()));
            JSONObject json = new JSONObject(raw);
            JSONObject reward = json.getJSONObject("quest").getJSONObject("reward");

            Material mat = Material.valueOf(reward.getString("item"));
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(reward.getString("name"));

            if (reward.has("lore")) {
                JSONArray loreArray = reward.getJSONArray("lore");
                List<String> lore = new ArrayList<>();
                for (int i = 0; i < loreArray.length(); i++) {
                    lore.add(loreArray.getString(i));
                }
                meta.setLore(lore);
            }

            if (reward.has("custom_model_data")) {
                meta.setCustomModelData(reward.getInt("custom_model_data"));
            }

            if (reward.optBoolean("unbreakable", false)) {
                meta.setUnbreakable(true);
            }

            item.setItemMeta(meta);

            if (reward.has("enchantments")) {
                JSONArray ench = reward.getJSONArray("enchantments");
                for (int i = 0; i < ench.length(); i++) {
                    String[] parts = ench.getString(i).split(":");
                    Enchantment enchant = Enchantment.getByName(parts[0]);
                    if (enchant != null) {
                        item.addUnsafeEnchantment(enchant, Integer.parseInt(parts[1]));
                    }
                }
            }

            return item;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[MetaMiner] Không thể đọc NFT từ " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
}
