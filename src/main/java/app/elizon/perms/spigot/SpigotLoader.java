package app.elizon.perms.spigot;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.spigot.commands.ElizonLatencyCommand;
import app.elizon.perms.spigot.commands.ElizonPermsCommand;
import app.elizon.perms.spigot.handler.SpigotPermHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpigotLoader extends JavaPlugin implements Listener {

    private String version;

    @Override
    public void onEnable() {
        new Initializer().init();
        version = Bukkit.getBukkitVersion();

        Map<String, String> versionToClassMap = new HashMap<>();
        versionToClassMap.put("1.16.5", "v1_16_R3");
        versionToClassMap.put("1.17", "v1_17_R1");
        versionToClassMap.put("1.18.2", "v1_18_R2");
        versionToClassMap.put("1.18", "v1_18_R1");
        versionToClassMap.put("1.19.3", "v1_19_R2");
        versionToClassMap.put("1.19.4", "v1_19_R3");
        versionToClassMap.put("1.19", "v1_19_R1");
        versionToClassMap.put("1.20.2", "v1_20_R2");
        versionToClassMap.put("1.20.4", "v1_20_R3");
        versionToClassMap.put("1.20", "v1_20_R1");

        for (Map.Entry<String, String> entry : versionToClassMap.entrySet()) {
            String version = entry.getKey();
            String className = entry.getValue();
            if (this.version.contains(version)) {
                this.version = className;
                break;
            }
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("ep").setExecutor(new ElizonPermsCommand());
        this.getCommand("el").setExecutor(new ElizonLatencyCommand());
        this.getCommand("ep").setTabCompleter(new ElizonPermsCommand());
        this.getCommand("ep").setAliases(List.of("elizonperms", "perms", "elizonp"));
        this.getCommand("el").setAliases(List.of("elizonlatency", "latency", "elizonpl"));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        new PermPlayer(event.getPlayer().getUniqueId().toString());
        try {
            Field field = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity").getDeclaredField("perm");
            field.setAccessible(true);
            field.set(event.getPlayer(), new SpigotPermHandler(event.getPlayer()));
            field.setAccessible(false);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
