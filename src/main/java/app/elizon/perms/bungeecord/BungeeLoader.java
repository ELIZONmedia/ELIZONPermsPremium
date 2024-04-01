package app.elizon.perms.bungeecord;

import app.elizon.perms.bungeecord.commands.BungeeLinkCommand;
import app.elizon.perms.bungeecord.commands.ElizonPermsBungeeCommand;
import app.elizon.perms.bungeecord.handler.BungeePermHandler;
import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.discord.DiscordBot;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLoader extends Plugin {

    private DiscordBot bot;

    @Override
    public void onEnable() {
        new Initializer().init();
        this.getProxy().getPluginManager().registerCommand(this, new ElizonPermsBungeeCommand());
        this.getProxy().getPluginManager().registerCommand(this, new BungeeLinkCommand());
        this.getProxy().getPluginManager().registerListener(this, new BungeePermHandler());

        bot = new DiscordBot();
        bot.start();
    }

    @Override
    public void onDisable() {
        bot.stop();
    }
}
