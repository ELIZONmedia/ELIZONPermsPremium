package app.elizon.perms.bungeecord.commands;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.discord.listener.LinkCommand;
import co.plocki.mysql.MySQLInsert;
import co.plocki.mysql.MySQLRequest;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.bukkit.entity.Player;

public class BungeeLinkCommand extends Command {

    public BungeeLinkCommand() {
        super("link");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if(commandSender instanceof Player player) {
            if (strings.length == 1) {
                if (LinkCommand.ids.containsKey(strings[0].toUpperCase())) {
                    //link

                    MySQLRequest request = new MySQLRequest();
                    request.prepare("uuid", Initializer.getPermDiscordLink().getTableName());
                    request.addRequirement("discordId", LinkCommand.ids.get(strings[0].toUpperCase()));

                    if(request.execute().isEmpty()) {
                        MySQLInsert insert = new MySQLInsert();
                        insert.prepare(Initializer.getPermDiscordLink(), player.getUniqueId().toString(), LinkCommand.ids.get(strings[0].toUpperCase()));
                        insert.execute();
                        LinkCommand.ids.remove(strings[0].toUpperCase());

                        commandSender.sendMessage("§f[§9EP§f] §aYour account has been linked successfully.");
                    } else {
                        commandSender.sendMessage("§f[§9EP§f] §cThe discord account is already linked.");
                    }
                } else {
                    commandSender.sendMessage("§f[§9EP§f] §cThe code desn't exist (anymore). Request one on discord using /link.");
                }
            } else {
                commandSender.sendMessage("§f[§9EP§f] §cYou didn't add a code. Request one on discord using /link.");
            }
        }
    }

}
