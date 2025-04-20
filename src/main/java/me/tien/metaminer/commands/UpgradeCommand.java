package me.tien.metaminer.commands;

import me.tien.metaminer.gui.UpgradeGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class UpgradeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            UpgradeGUI.openUpgradeMenu(player);
        }
        return true;
    }
}