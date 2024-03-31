package app.elizon.perms.velocity;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.velocity.commands.ElizonPermsVelocityCommand;
import app.elizon.perms.velocity.handler.VelocityPermHandler;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(authors = "ELIZONMedia", id = "elizonperms", name = "ELIZONPerms", version = "v0.1-alpha")
public class VelocityLoader {

    private final ProxyServer server;
    private final Logger logger;

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
        CommandMeta meta = manager.metaBuilder("elizonpermsvelocity").aliases("epv", "elizonpermsv", "elizonpv").plugin(this).build();

        server.getEventManager().register(this, this);
        server.getCommandManager().register(meta, new ElizonPermsVelocityCommand(server));
    }

    @Subscribe
    public void onPermSetup(PermissionsSetupEvent event) {

        if(!(event.getSubject() instanceof Player)) {
            return;
        }

        event.setProvider(new VelocityPermHandler(event.getSubject()));
    }

}
