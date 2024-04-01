package app.elizon.perms.pkg.discord;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.discord.listener.LinkCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class DiscordBot {

    private JDA bot;

    public void start() {
        bot = JDABuilder.createDefault(Initializer.getDiscordBotToken())
                .addEventListeners(new LinkCommand())
                .setActivity(Activity.playing("Minecraft"))
                .build();

        bot.updateCommands().addCommands(
                Commands.slash("link", "Link your Minecraft account").setGuildOnly(true),
                Commands.slash("getlink", "Get a minecraft account link information").setGuildOnly(true).addOption(OptionType.USER, "user", "The user to get link information about", true)
        ).queue();
    }

    public void stop() {
        //bot.shutdown();
    }
}
