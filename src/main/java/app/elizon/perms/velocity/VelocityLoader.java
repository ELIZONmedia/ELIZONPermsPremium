package app.elizon.perms.velocity;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.discord.DiscordBot;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.velocity.commands.ElizonPermsVelocityCommand;
import app.elizon.perms.velocity.commands.VelocityLinkCommand;
import app.elizon.perms.velocity.handler.VelocityPermHandler;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(authors = "ELIZONMedia", id = "elizonperms", name = "ELIZONPerms", version = "0.5-beta")
public class VelocityLoader {

    private final ProxyServer server;
    private final Logger logger;
    private DiscordBot bot;

    @Inject
    public VelocityLoader(ProxyServer server, Logger logger) {
        new Initializer().init();
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onJoin(LoginEvent event) {
        new PermPlayer(event.getPlayer().getUniqueId().toString());
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        CommandManager manager = server.getCommandManager();
        CommandMeta meta = manager.metaBuilder("epv").aliases("elizonpermsvelocity", "elizonpermsv", "elizonpv").plugin(this).build();
        CommandMeta meta2 = manager.metaBuilder("link").plugin(this).build();

        server.getEventManager().register(this, this);
        server.getCommandManager().register(meta, new ElizonPermsVelocityCommand(server));
        server.getCommandManager().register(meta2, new VelocityLinkCommand());
        bot = new DiscordBot();
        bot.start();
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        bot.stop();
    }

    @Subscribe
    public void onPermSetup(PermissionsSetupEvent event) {

        if(!(event.getSubject() instanceof Player)) {
            return;
        }

        event.setProvider(new VelocityPermHandler(event.getSubject()));
    }

}
