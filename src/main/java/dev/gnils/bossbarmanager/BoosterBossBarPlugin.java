package dev.gnils.bossbarmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BoosterBossBarPlugin extends JavaPlugin implements Listener, TabCompleter {

    private BossBar boosterBar;
    private long joinDelay;
    private String barTitle;
    private BarStyle barStyle;
    private BarColor barColor;
    private String placeholderValue;
    private String placeholderInactive;
    private YamlConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        loadLangConfig();

        boosterBar = Bukkit.createBossBar(barTitle, barColor, barStyle);
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("bbm").setTabCompleter(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                checkBoosterStatus();
            }
        }.runTaskTimer(this, 0L, 10L);

        getLogger().info(getMessage("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        if (boosterBar != null) {
            boosterBar.removeAll();
        }
        getLogger().info(getMessage("plugin_disabled"));
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        joinDelay = config.getLong("join-delay", 10L);
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
        placeholderValue = config.getString("placeholder.value", "%axboosters_active_1_audience%");
        placeholderInactive = config.getString("placeholder.inactive", "---");
    }

    private void loadLangConfig() {
        saveResource("lang.yml", false);
        File langFile = new File(getDataFolder(), "lang.yml");
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private String getMessage(String path) {
        String message = langConfig.getString("messages." + path, "Message not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
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
        String boosterStatus = PlaceholderAPI.setPlaceholders(player, placeholderValue);

        if (!boosterStatus.equals(placeholderInactive)) {
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
        String boosterStatus = PlaceholderAPI.setPlaceholders(player, placeholderValue);

        if (!boosterStatus.equals(placeholderInactive)) {
            boosterBar.addPlayer(player);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bbm")) {
            if (args.length == 0) {
                sendHelpMenu(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("bossbarmanager.reload")) {
                    loadConfig();
                    loadLangConfig();
                    sender.sendMessage(getMessage("reload_success"));
                    return true;
                } else {
                    sender.sendMessage(getMessage("no_permission"));
                    return true;
                }
            } else {
                sendHelpMenu(sender);
                return true;
            }
        }
        return false;
    }

    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage(getMessage("help_menu.header"));
        sender.sendMessage(getMessage("help_menu.command_help"));
        sender.sendMessage(getMessage("help_menu.command_reload"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("bbm")) {
            if (args.length == 1) {
                completions.add("reload");
            }
        }
        return completions;
    }
}