package app.elizon.perms.pkg.discord.listener;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.player.PermPlayer;
import co.plocki.mysql.MySQLRequest;
import co.plocki.mysql.MySQLResponse;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Objects;

public class LinkCommand extends ListenerAdapter {

    public static HashMap<String, Long> ids = new HashMap<>();

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            code.append(randomChar);
        }

        return code.toString();
    }

    public String getName(String uuid) {
        try {
            String url = "https://api.mojang.com/user/profile/" + uuid;
            String nameJson = IOUtils.toString(new URI(url), StandardCharsets.UTF_8);
            JSONObject nameArray = new JSONObject(nameJson);
            return nameArray.getString("name");
        } catch (IOException | JSONException e) {
            e.printStackTrace(); // Handle the exception appropriately (logging, error dialog, etc.)
            return "Error occurred while fetching name";
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equalsIgnoreCase("link")) {
            if(!ids.containsValue(event.getMember().getIdLong())) {
                event.deferReply().queue();

                MySQLRequest request = new MySQLRequest();
                request.prepare("uuid", Initializer.getPermDiscordLink().getTableName());
                request.addRequirement("discordId", Objects.requireNonNull(event.getMember()).getIdLong());
                MySQLResponse response = request.execute();

                if(response.isEmpty()) {
                    String code = generateCode(6);
                    ids.put(code, Objects.requireNonNull(event.getMember()).getIdLong());
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("This is your code: ||" + code + "||")).queue();
                    event.getHook().editOriginal("Your link has been sent to your via direct messages. - Type /link " + code + " on the server to link your account.\nPlease note, that you **can't** unlink your account and this is a **permanent** action. Use within 5 Minutes. Don't share your code.")
                            .queue();

                    new Thread(() -> {
                        try {
                            Thread.sleep(1000*300);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        ids.remove(code);
                        event.getHook().deleteOriginal().queue();
                    }).start();
                } else {
                    event.reply("Your account is already linked.").setEphemeral(true).queue();
                }
            } else {
                event.reply("You already have a running code. Look into your direct messages.").setEphemeral(true).queue();
            }
        } else if(event.getName().equalsIgnoreCase("getlink")) {
            MySQLRequest request = new MySQLRequest();
            request.prepare("uuid", Initializer.getPermDiscordLink().getTableName());
            request.addRequirement("discordId", Objects.requireNonNull(event.getMember()).getIdLong());
            MySQLResponse response = request.execute();
            if(!response.isEmpty()) {
                if(new PermPlayer(response.getString("uuid")).simpleHasPermission("elizonperms.discord.command.getlink")) {
                    long targetID = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsUser)).getIdLong();

                    request = new MySQLRequest();
                    request.prepare("uuid", Initializer.getPermDiscordLink().getTableName());
                    request.addRequirement("discordId", targetID);
                    response = request.execute();
                    if(!response.isEmpty()) {
                        String minecraftName = getName(response.getString("uuid"));

                        event.reply("The account is linked to " + minecraftName + " (" + response.getString("uuid") + ").").setEphemeral(true).queue();
                    } else {
                        event.reply("The user has not linked his minecraft account.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("You don't have enough rights to perform this command.").setEphemeral(true).queue();
                }
            } else {
                event.reply("You must link your account first. Use /link.").setEphemeral(true).queue();
            }
        }
    }
}
