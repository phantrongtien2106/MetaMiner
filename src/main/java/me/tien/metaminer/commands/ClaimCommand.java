package me.tien.metaminer.commands;

import me.tien.metaminer.MetaMiner;
import me.tien.metaminer.util.ScoreboardDisplay;
import me.tien.metaminer.data.PlayerDataManager;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import java.util.UUID;

public class ClaimCommand implements CommandExecutor {

    private final MetaMiner plugin;

    public ClaimCommand(MetaMiner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        UUID uuid = p.getUniqueId();
        int valueMultiplier = 1 + PlayerDataManager.getUpgrade(uuid, "value");
        int total = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;
            // Apply the value upgrade multiplier
            Material type = item.getType();
            int basePointValue = plugin.getConfigManager().getPointValue(type);
            int adjustedPointValue = basePointValue * valueMultiplier;
            if (adjustedPointValue > 0) {
                int val = adjustedPointValue * item.getAmount();
                total += val;
                p.getInventory().remove(item);
            }
        }

        if (total > 0) {
            PlayerDataManager.addPoints(p.getUniqueId(), total);
            p.sendMessage(ChatColor.GREEN + "Bạn đã nhận " + total + " điểm!");
        } else {
            p.sendMessage(ChatColor.RED + "Bạn không có block nào hợp lệ.");
        }

        ScoreboardDisplay.show(p);
        return true;
    }
}