package app.elizon.perms.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ElizonLatencyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        long now = System.currentTimeMillis();

        if(sender.hasPermission("elizonperms.command.latency")) {
            sender.sendMessage("§f[§9EP§f] §aLatency: " + (System.currentTimeMillis()-now) + "ms");
        } else {
            sender.sendMessage("§f[§9EP§f] §cYou don't have the permission to do that.");
        }

        return true;
    }
}
