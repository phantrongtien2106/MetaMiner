package me.tien.metaminer;

import me.tien.metaminer.commands.*;
import me.tien.metaminer.config.ConfigManager;
import me.tien.metaminer.data.PlayerDataManager;
import me.tien.metaminer.gui.UpgradeGUI;
import me.tien.metaminer.listeners.InventoryManager;
import me.tien.metaminer.listeners.MiningSpeedListener;
import me.tien.metaminer.util.ScoreboardDisplay;
import me.tien.metaminer.util.VoidChunkGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class MetaMiner extends JavaPlugin implements Listener, CommandExecutor {

    private ConfigManager configManager;
    private final Set<Integer> protectedGlassLevels = new HashSet<>();
    private boolean isGlassProtectionRegistered = false;

    @Override
    public void onEnable() {
        getLogger().info("MetaMiner đã được bật!");
        configManager = new ConfigManager(this);

        // Đăng ký lệnh và sự kiện
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new UpgradeGUI(), this);
        getServer().getPluginManager().registerEvents(new MiningSpeedListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryManager(), this);

        getCommand("claim").setExecutor(new ClaimCommand(this));
        getCommand("upgrade").setExecutor(new UpgradeCommand());
        getCommand("minearea").setExecutor(new MineAreaCommand());
        getCommand("lobby").setExecutor(new LobbyCommand());
        getCommand("resetmine").setExecutor(this);

        // Tạo thư mục dữ liệu
        if (!getDataFolder().exists()) getDataFolder().mkdir();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.load(player);

        String worldName = "mine_" + player.getName();
        World mineWorld = Bukkit.getWorld(worldName);

        if (mineWorld == null) {
            mineWorld = createPlayerMiningWorld(player.getName());
            if (mineWorld != null) {
                ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
                ItemMeta meta = diamondPickaxe.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.AQUA + "Cúp khởi đầu");
                    diamondPickaxe.setItemMeta(meta);
                }
                player.getInventory().addItem(diamondPickaxe);
                player.sendMessage(ChatColor.GREEN + "Bạn đã nhận được một cây cúp kim cương để bắt đầu đào!");
            }
        }

        // Chỉ cập nhật scoreboard nếu người chơi ở trong mining_lobby
        if (player.getWorld().getName().equals("mining_lobby")) {
            ScoreboardDisplay.show(player);
            InventoryManager.updateLockedSlots(player);
        }
    }

    private World createPlayerMiningWorld(String playerName) {
        String worldName = "mine_" + playerName;
        File worldsFolder = new File(getDataFolder().getParentFile(), "worlds");
        File worldFolder = new File(worldsFolder, worldName);

        // Đảm bảo thư mục "worlds" tồn tại
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs();
        }

        // Kiểm tra nếu thế giới đã tồn tại
        World mineWorld = Bukkit.getWorld(worldName);
        if (mineWorld == null && worldFolder.exists()) {
            mineWorld = Bukkit.createWorld(new WorldCreator(worldName));
        }

        // Tạo thế giới nếu nó chưa tồn tại
        if (mineWorld == null) {
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.generator(new VoidChunkGenerator());
            creator.type(WorldType.FLAT);
            creator.createWorld();

            // Di chuyển thế giới vào thư mục "worlds"
            if (worldFolder.exists()) {
                getLogger().info("Thư mục thế giới đã tồn tại: " + worldFolder.getPath());
            } else {
                getLogger().info("Đang tạo thư mục thế giới: " + worldFolder.getPath());
            }

            mineWorld = Bukkit.getWorld(worldName);
            if (mineWorld != null) {
                fillMineArea(mineWorld, 0, 64, 0);
                getLogger().info("Đã tạo thế giới đào cho người chơi: " + playerName);
            } else {
                getLogger().severe("Không thể tạo thế giới đào cho người chơi: " + playerName);
            }
        }
        return mineWorld;
    }

    private void fillMineArea(World world, int startX, int startY, int startZ) {
        if (world == null) {
            getLogger().severe("Không thể tạo khu mỏ: Thế giới không tồn tại!");
            return;
        }

        getLogger().info("Bắt đầu tạo khu mỏ tại vị trí (" + startX + ", " + startY + ", " + startZ + ")");

        List<BlockData> blockUpdateQueue = new ArrayList<>();

        for (int x = -1; x <= 16; x++) {
            for (int y = -1; y <= 26; y++) {
                for (int z = -1; z <= 16; z++) {
                    Material material;
                    if (y == -1 || ((x == -1 || x == 16 || z == -1 || z == 16) && y <= 26)) {
                        material = Material.BEDROCK;
                    } else if (y == 21) {
                        material = Material.AIR;
                    } else if (y <= 20) {
                        material = configManager.getRandomOre();
                    } else {
                        material = Material.AIR;
                    }
                    blockUpdateQueue.add(new BlockData(startX + x, startY + y, startZ + z, material));
                }
            }
        }

        int batchSize = 500;
        int totalBlocks = blockUpdateQueue.size();
        int batches = (int) Math.ceil((double) totalBlocks / batchSize);

        for (int i = 0; i < batches; i++) {
            final int batchIndex = i;
            Bukkit.getScheduler().runTaskLater(this, () -> {
                int start = batchIndex * batchSize;
                int end = Math.min(start + batchSize, totalBlocks);

                for (int j = start; j < end; j++) {
                    BlockData data = blockUpdateQueue.get(j);
                    world.getBlockAt(data.x, data.y, data.z).setType(data.material);
                }

                if (batchIndex == batches - 1) {
                    getLogger().info("Hoàn thành việc tạo khu mỏ!");
                }
            }, i * 2L);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Lệnh này chỉ dành cho người chơi.");
            return false;
        }

        if ("resetmine".equals(command.getName())) {
            World world = player.getWorld();
            String worldPrefix = "mine_";

            if (!world.getName().startsWith(worldPrefix)) {
                player.sendMessage(ChatColor.RED + "Bạn phải ở trong thế giới đào của mình để sử dụng lệnh này!");
                return true;
            }

            int startX = 0;
            int startY = 64;
            int startZ = 0;

            resetMineWithGlassBarrier(player, world, startX, startY, startZ);
            return true;
        }

        return false;
    }

    public void resetMineWithGlassBarrier(Player player, World world, int startX, int startY, int startZ) {
        int glassY = startY + 23;
        List<Block> glassBlocks = new ArrayList<>();

        player.sendMessage(ChatColor.GOLD + "Đang chuẩn bị reset khu đào...");

        int batchSize = 200;
        int totalBlocks = 16 * 16;
        int batches = (int) Math.ceil((double) totalBlocks / batchSize);

        for (int i = 0; i < batches; i++) {
            final int batchIndex = i;
            Bukkit.getScheduler().runTaskLater(this, () -> {
                int start = batchIndex * batchSize;
                int end = Math.min(start + batchSize, totalBlocks);

                for (int j = start; j < end; j++) {
                    int relX = j % 16;
                    int relZ = j / 16;
                    int x = startX + relX;
                    int z = startZ + relZ;

                    Block block = world.getBlockAt(x, glassY, z);
                    block.setType(Material.GLASS);
                    glassBlocks.add(block);
                }

                if (batchIndex == batches - 1) {
                    Location teleportLoc = new Location(world, startX + 8, glassY + 1, startZ + 8);
                    player.teleport(teleportLoc);
                    startMineReset(player, world, startX, startY, startZ, glassBlocks);
                }
            }, i);
        }
        registerGlassProtection(glassY);
    }

    private void startMineReset(Player player, World world, int startX, int startY, int startZ, List<Block> glassBlocks) {
        new BukkitRunnable() {
            int countdown = 10;
            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendTitle(
                            ChatColor.GOLD + "Reset trong " + countdown + "s",
                            ChatColor.YELLOW + "Vui lòng đợi...", 5, 10, 5
                    );
                    countdown--;
                } else {
                    this.cancel();
                    player.sendMessage(ChatColor.YELLOW + "Đang reset khu mỏ...");

                    resetMine(world, startX, startY, startZ, () -> {
                        removeGlassBarrier(glassBlocks);
                        Location mineCenter = new Location(world, startX + 8, startY + 21, startZ + 8);
                        player.teleport(mineCenter);
                        player.sendMessage(ChatColor.GREEN + "✅ Khu mỏ đã được reset!");
                    });
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void resetMine(World world, int startX, int startY, int startZ, Runnable onComplete) {
        fillMineArea(world, startX, startY, startZ);
        onComplete.run();
    }

    private void removeGlassBarrier(List<Block> glassBlocks) {
        Bukkit.getScheduler().runTask(this, () -> {
            for (Block block : glassBlocks) {
                block.setType(Material.AIR);
            }
        });
    }

    private void registerGlassProtection(int glassY) {
        protectedGlassLevels.add(glassY);

        if (!isGlassProtectionRegistered) {
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onBlockBreak(BlockBreakEvent event) {
                    Block block = event.getBlock();
                    if (block.getType() == Material.GLASS && protectedGlassLevels.contains(block.getY())) {
                        event.setCancelled(true);
                        Player player = event.getPlayer();
                        player.sendMessage(ChatColor.RED + "Không thể đập vỡ lớp kính bảo vệ!");
                    }
                }
            }, this);
            isGlassProtectionRegistered = true;
        }
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerDataManager.save(p);
        }
        getLogger().info("MetaMiner đã tắt.");
    }

    private static class BlockData {
        int x, y, z;
        Material material;

        public BlockData(int x, int y, int z, Material material) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}