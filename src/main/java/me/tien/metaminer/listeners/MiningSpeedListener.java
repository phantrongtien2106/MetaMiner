package me.tien.metaminer.listeners;

import me.tien.metaminer.MetaMiner;
import me.tien.metaminer.data.PlayerDataManager;
import me.tien.metaminer.util.ExternalNftReader;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MiningSpeedListener implements Listener {

    private final MetaMiner plugin;

    public MiningSpeedListener(MetaMiner plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Chỉ áp dụng khi đang trong thế giới mine_<tên>
        if (!player.getWorld().getName().startsWith("mine_")) return;

        // Kiểm tra nếu inventory đã đầy (chỉ trong ô đã mở khóa)
        if (!InventoryManager.hasUnlockedSlot(player)) {
            event.setCancelled(true);
            int storageLevel = PlayerDataManager.getUpgrade(player.getUniqueId(), "storage");
            boolean isMaxed = storageLevel >= 3; // cấp độ tối đa là 3
            player.sendMessage(ChatColor.RED + "❌ Túi đồ của bạn đã đầy!");
            if (isMaxed) {
                player.sendMessage(ChatColor.YELLOW + "Bạn đã nâng cấp tối đa! Hãy dùng lệnh §e/claim §7để đổi block thành điểm.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Dùng lệnh §e/claim §7để đổi block thành điểm hoặc nâng cấp §aStorage§7.");
            }
            return;
        }
        int speedLevel = PlayerDataManager.getUpgrade(player.getUniqueId(), "speed");
        if (speedLevel > 0) {
            // Haste cấp độ = speedLevel (không giới hạn 4)
            PotionEffect current = player.getPotionEffect(PotionEffectType.FAST_DIGGING);
            int amplifier = speedLevel;
            if (current == null || current.getAmplifier() < amplifier) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.FAST_DIGGING,
                        Integer.MAX_VALUE,
                        amplifier,
                        false,
                        false,
                        true
                ));
            }
        }
        ExternalNftReader.tryDropNFTs(player, plugin);
    }
}
