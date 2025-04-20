package me.tien.metaminer.commands;

import me.tien.metaminer.util.ScoreboardDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineAreaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Lệnh này chỉ dành cho người chơi.");
            return false;
        }

        // Check if the player has used the /lobby command
        if (!LobbyCommand.hasUsedLobbyCommand(player.getName())) {
            player.sendMessage(ChatColor.RED + "Bạn phải sử dụng lệnh /lobby trước khi dùng lệnh này!");
            return true;
        }

        World currentWorld = player.getWorld();
        if (!currentWorld.getName().equalsIgnoreCase("mining_lobby")) {
            player.sendMessage(ChatColor.RED + "Bạn phải ở trong mining_lobby để sử dụng lệnh này!");
            return true;
        }

        // Get the player's mining world
        String worldName = "mine_" + player.getName();
        World mineWorld = Bukkit.getWorld(worldName);

        if (mineWorld == null) {
            player.sendMessage(ChatColor.RED + "Thế giới đào của bạn chưa được tạo. Vui lòng liên hệ quản trị viên!");
            return false;
        }

        // Teleport the player to their mining world
        Location spawnLocation = new Location(mineWorld, 8, 70, 8);
        player.teleport(spawnLocation);
        ScoreboardDisplay.show(player);
        player.sendMessage(ChatColor.GREEN + "Chào mừng đến với thế giới đào của bạn!");
        return true;
    }
}