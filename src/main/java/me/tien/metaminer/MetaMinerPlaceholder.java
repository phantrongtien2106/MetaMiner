package me.tien.metaminer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.tien.metaminer.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetaMinerPlaceholder extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "metaminer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "tien";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        String mineWorld = "mine_" + player.getName();
        if (!player.getWorld().getName().equalsIgnoreCase(mineWorld)) return "";

        switch (identifier.toLowerCase()) {
            case "points":
                return String.valueOf(PlayerDataManager.getPoints(player.getUniqueId()));
            case "speed":
                return String.valueOf(PlayerDataManager.getUpgrade(player.getUniqueId(), "speed"));
            case "value":
                return String.valueOf(PlayerDataManager.getUpgrade(player.getUniqueId(), "value"));
            case "storage":
                return String.valueOf(PlayerDataManager.getUpgrade(player.getUniqueId(), "storage"));
            default:
                return null;
        }
    }
}
