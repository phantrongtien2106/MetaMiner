package me.tien.metaminer;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MineResetManager {

    private final Plugin plugin;
    private final Location origin;
    private final int size = 16;
    private final int height = 3;

    public MineResetManager(Plugin plugin, Location origin) {
        this.plugin = plugin;
        this.origin = origin;
    }

    public void startCountdownAndReset(Player player) {
        new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Khu mỏ sẽ reset trong " + countdown + " giây...");
                    countdown--;
                } else {
                    resetMineArea();
                    player.sendMessage(ChatColor.GREEN + "✅ Khu mỏ đã được reset!");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void resetMineArea() {
        World world = origin.getWorld();
        if (world == null) return;

        Random random = new Random();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < size; z++) {
                    Location loc = origin.clone().add(x, y, z);
                    Material blockType = getRandomOre(random);
                    world.getBlockAt(loc).setType(blockType);
                }
            }
        }
    }

    private Material getRandomOre(Random random) {
        int chance = random.nextInt(100);
        if (chance < 60) return Material.STONE;
        if (chance < 75) return Material.COAL_ORE;
        if (chance < 85) return Material.IRON_ORE;
        if (chance < 93) return Material.GOLD_ORE;
        return Material.DIAMOND_ORE;
    }
}
