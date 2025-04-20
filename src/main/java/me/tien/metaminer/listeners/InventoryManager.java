package me.tien.metaminer.listeners;

import me.tien.metaminer.data.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InventoryManager implements Listener {

    // Define how many slots are unlocked per storage level
    public static final int[] SLOTS_PER_LEVEL = {9, 18, 27, 36};

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null || clickedInv.getType() != InventoryType.PLAYER) return;

        // Áp dụng cả ở mining_lobby và mine_<tên>
        String worldName = player.getWorld().getName();
        if (!worldName.equals("mining_lobby") && !worldName.startsWith("mine_")) return;

        int storageLevel = PlayerDataManager.getUpgrade(player.getUniqueId(), "storage");
        int allowedSlots = SLOTS_PER_LEVEL[Math.min(storageLevel, SLOTS_PER_LEVEL.length - 1)];

        int slot = event.getSlot();
        if (slot >= allowedSlots && slot < 36) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Nâng cấp Storage để mở khóa ô này!");
        }
    }
    // Method to visualize locked slots
    public static void updateLockedSlots(Player player) {
        int storageLevel = PlayerDataManager.getUpgrade(player.getUniqueId(), "storage");
        int allowedSlots = SLOTS_PER_LEVEL[Math.min(storageLevel, SLOTS_PER_LEVEL.length - 1)];

        ItemStack lockedItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = lockedItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Ô bị khóa");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Nâng cấp Storage để mở khóa"));
        lockedItem.setItemMeta(meta);

        for (int i = allowedSlots; i < 36; i++) {
            if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) {
                player.getInventory().setItem(i, lockedItem);
            }
        }
    }
    // Method to get allowed slot count
    public static int getAllowedSlots(int storageLevel) {
        return SLOTS_PER_LEVEL[Math.min(storageLevel, SLOTS_PER_LEVEL.length - 1)];
    }
    // Check if player has at least one unlocked empty slot
    public static boolean hasUnlockedSlot(Player player) {
        int storageLevel = PlayerDataManager.getUpgrade(player.getUniqueId(), "storage");
        int allowed = getAllowedSlots(storageLevel);

        for (int i = 0; i < allowed; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR || item.getType() == Material.BARRIER) {
                return true;
            }
        }
        return false;
    }
}
