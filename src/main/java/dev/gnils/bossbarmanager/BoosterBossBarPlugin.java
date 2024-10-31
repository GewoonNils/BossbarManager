package dev.gnils.bossbarmanager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.clip.placeholderapi.PlaceholderAPI;

public class BoosterBossBarPlugin extends JavaPlugin implements Listener {

    private BossBar boosterBar;
    private long joinDelay;
    private String barTitle;
    private BarStyle barStyle;
    private BarColor barColor;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        boosterBar = Bukkit.createBossBar(barTitle, barColor, barStyle);
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                checkBoosterStatus();
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        if (boosterBar != null) {
            boosterBar.removeAll();
        }
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        joinDelay = config.getLong("join-delay", 40L);
        barTitle = config.getString("bossbar.title", "Actieve Booster: %booster%");
        try {
            barStyle = BarStyle.valueOf(config.getString("bossbar.style", "SOLID"));
        } catch (IllegalArgumentException e) {
            barStyle = BarStyle.SOLID;
            getLogger().warning("Invalid bossbar style in config, using SOLID");
        }
        try {
            barColor = BarColor.valueOf(config.getString("bossbar.color", "BLUE"));
        } catch (IllegalArgumentException e) {
            barColor = BarColor.BLUE;
            getLogger().warning("Invalid bossbar color in config, using BLUE");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                updateBoosterBarForPlayer(player);
            }
        }.runTaskLater(this, joinDelay);
    }

    private void checkBoosterStatus() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        Player anyPlayer = Bukkit.getOnlinePlayers().iterator().next();
        updateBoosterBar(anyPlayer);
    }

    private void updateBoosterBar(Player player) {
        String boosterStatus = PlaceholderAPI.setPlaceholders(player, "%axboosters_active_1_audience%");

        if (!boosterStatus.equals("---")) {
            String title = barTitle.replace("%booster%", boosterStatus);
            boosterBar.setTitle(title);
            boosterBar.setStyle(barStyle);
            boosterBar.setColor(barColor);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                boosterBar.addPlayer(onlinePlayer);
            }
        } else {
            boosterBar.removeAll();
        }
    }

    private void updateBoosterBarForPlayer(Player player) {
        String boosterStatus = PlaceholderAPI.setPlaceholders(player, "%axboosters_active_1_audience%");

        if (!boosterStatus.equals("---")) {
            boosterBar.addPlayer(player);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bossbarreload")) {
            if (sender.hasPermission("bossbarmanager.reload")) {
                loadConfig();
                sender.sendMessage("BoosterBossBar configuratie is herladen.");
                return true;
            } else {
                sender.sendMessage("Je hebt geen toestemming om dit commando uit te voeren.");
                return true;
            }
        }
        return false;
    }
}