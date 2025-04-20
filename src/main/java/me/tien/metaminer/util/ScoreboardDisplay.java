package me.tien.metaminer.util;

import me.tien.metaminer.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class ScoreboardDisplay {

    public static void show(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("stats", "dummy", ChatColor.GOLD + "§lĐÀO NFT");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        UUID uuid = player.getUniqueId();

        int points = PlayerDataManager.getPoints(uuid);
        int speed = PlayerDataManager.getUpgrade(uuid, "speed");
        int value = PlayerDataManager.getUpgrade(uuid, "value");
        int storage = PlayerDataManager.getUpgrade(uuid, "storage");

        objective.getScore(ChatColor.YELLOW + "Tên: " + ChatColor.GREEN + player.getName()).setScore(6);
        objective.getScore(ChatColor.YELLOW + "Điểm: " + ChatColor.AQUA + points).setScore(5);
        objective.getScore(ChatColor.GREEN + "⛏ Speed: " + ChatColor.WHITE + speed).setScore(4);
        objective.getScore(ChatColor.GREEN + "💰 Value: " + ChatColor.WHITE + value).setScore(3);
        objective.getScore(ChatColor.GREEN + "📦 Storage: " + ChatColor.WHITE + storage).setScore(2);
        objective.getScore(ChatColor.GRAY + "Thế giới: " + ChatColor.WHITE + "mine_" + player.getName()).setScore(1);

        player.setScoreboard(board);
    }
}
