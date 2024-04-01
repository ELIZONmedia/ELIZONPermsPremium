package app.elizon.perms.velocity.commands;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.discord.listener.LinkCommand;
import co.plocki.mysql.MySQLInsert;
import co.plocki.mysql.MySQLRequest;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class VelocityLinkCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        String[] strings = invocation.arguments();

        if(commandSource instanceof Player player) {
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

                        player.sendMessage(Component.text("§f[§9EP§f] §aYour account has been linked successfully."));
                    } else {
                        player.sendMessage(Component.text("§f[§9EP§f] §cThe discord account is already linked."));
                    }
                } else {
                    player.sendMessage(Component.text("§f[§9EP§f] §cThe code doesn't exist (anymore). Request one on discord using /link."));
                }
            } else {
                player.sendMessage(Component.text("§f[§9EP§f] §cYou didn't add a code. Request one on discord using /link."));
            }
        }
    }
}
