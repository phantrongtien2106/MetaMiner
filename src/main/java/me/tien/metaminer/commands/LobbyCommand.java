package me.tien.metaminer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class LobbyCommand implements CommandExecutor {

    private static final Set<String> playersInLobby = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Lệnh này chỉ dành cho người chơi.");
            return true;
        }

        World lobbyWorld = Bukkit.getWorld("mining_lobby");
        if (lobbyWorld == null) {
            player.sendMessage(ChatColor.RED + "Thế giới mining_lobby không khả dụng.");
            return false;
        }
        Location spawnLocation = new Location(lobbyWorld, 0, 70, 0);
        player.teleport(spawnLocation);
        playersInLobby.add(player.getName()); // Add player to the tracking set
        player.sendMessage(ChatColor.GREEN + "Chào mừng đến với khu vực đào!");
        return true;
    }

    public static boolean hasUsedLobbyCommand(String playerName) {
        return playersInLobby.contains(playerName);
    }
}