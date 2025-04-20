package me.tien.metaminer.gui;

import me.tien.metaminer.listeners.InventoryManager;
import me.tien.metaminer.util.ScoreboardDisplay;
import me.tien.metaminer.data.PlayerDataManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UpgradeGUI implements Listener {

    public static void openUpgradeMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Nâng cấp NFT đào");

        UUID uuid = p.getUniqueId();
        int speedLevel = PlayerDataManager.getUpgrade(uuid, "speed");
        int valueLevel = PlayerDataManager.getUpgrade(uuid, "value");
        int storageLevel = PlayerDataManager.getUpgrade(uuid, "storage");

        // Calculate scaled costs based on current levels
        int speedCost = 50 * (speedLevel + 1);
        int valueCost = 75 * (valueLevel + 1);
        int storageCost = 100 * (storageLevel + 1);

        inv.setItem(11, createUpgradeItem(Material.GOLDEN_PICKAXE, "Speed", speedLevel, speedCost));
        inv.setItem(13, createUpgradeItem(Material.EMERALD, "Value", valueLevel, valueCost));
        inv.setItem(15, createUpgradeItem(Material.CHEST, "Storage", storageLevel, storageCost));

        // Add information items
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Thông tin");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Điểm hiện tại: " + PlayerDataManager.getPoints(uuid),
                ChatColor.GRAY + "Speed: Tăng tốc độ đào",
                ChatColor.GRAY + "Value: Tăng giá trị khi đào",
                ChatColor.GRAY + "Storage: Mở khóa hàng trong túi đồ"
        ));
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        // Add border decoration
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }

        p.openInventory(inv);
    }

    private static ItemStack createUpgradeItem(Material material, String name, int level, int cost) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name + " (Cấp " + level + ")");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Giá: " + cost + " điểm");
        lore.add(ChatColor.AQUA + "Click để nâng cấp lên cấp " + (level + 1));

        if (name.equals("Storage")) {
            lore.add(ChatColor.YELLOW + "Mở khóa " + getStorageRowsForLevel(level) + " hàng trong túi đồ");
            if (level < 3) {
                lore.add(ChatColor.YELLOW + "Nâng cấp tiếp để mở khóa " + getStorageRowsForLevel(level + 1) + " hàng");
            } else {
                lore.add(ChatColor.GOLD + "Đã đạt cấp tối đa!");
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static int getStorageRowsForLevel(int level) {
        switch (level) {
            case 0: return 1; // Base level: 1 row
            case 1: return 2; // Level 1: 2 rows
            case 2: return 3; // Level 2: 3 rows
            case 3: return 4; // Level 3: 4 rows (max)
            default: return 4;
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(ChatColor.GOLD + "Nâng cấp NFT đào")) return;

        e.setCancelled(true);
        int slot = e.getSlot();

        String type = null;
        int basePrice = 0;

        if (slot == 11) { type = "speed"; basePrice = 50; }
        else if (slot == 13) { type = "value"; basePrice = 75; }
        else if (slot == 15) { type = "storage"; basePrice = 100; }
        else return;

        UUID uuid = p.getUniqueId();
        int currentLevel = PlayerDataManager.getUpgrade(uuid, type);

        // Maximum storage level check
        if (type.equals("storage") && currentLevel >= 3) {
            p.sendMessage(ChatColor.RED + "Đã đạt cấp tối đa!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        // Calculate actual cost based on current level
        int actualCost = basePrice * (currentLevel + 1);

        if (PlayerDataManager.getPoints(uuid) >= actualCost) {
            PlayerDataManager.removePoints(uuid, actualCost);
            PlayerDataManager.incrementUpgrade(uuid, type);

            // Update inventory slots if storage was upgraded
            if (type.equals("storage")) {
                Bukkit.getScheduler().runTaskLater(
                        Bukkit.getPluginManager().getPlugin("MetaMiner"),
                        () -> InventoryManager.updateLockedSlots(p),
                        1L
                );
            }

            p.sendMessage(ChatColor.GREEN + "Đã nâng cấp " + type + " lên cấp " + (currentLevel + 1) + "!");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            openUpgradeMenu(p);
            ScoreboardDisplay.show(p);
        } else {
            p.sendMessage(ChatColor.RED + "Không đủ điểm! Cần " + actualCost + " điểm.");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }
}