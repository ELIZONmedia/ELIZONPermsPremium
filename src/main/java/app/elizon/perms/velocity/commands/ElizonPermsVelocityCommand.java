package app.elizon.perms.velocity.commands;

import app.elizon.perms.pkg.group.PermGroup;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.pkg.util.MultiState;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;

public class ElizonPermsVelocityCommand implements SimpleCommand {

    private final ProxyServer proxy;

    public ElizonPermsVelocityCommand(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Only players can use this command."));
            return;
        }

        if (!player.hasPermission("elizonperms.command.execute")) {
            player.sendMessage(Component.text("§f[§9EP§f] §aThis server is using the free ElizonPerms Lite permissions system by ELIZONMedia. Thanks for using!"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("§f[§9EP§f] §9Usage for groups: /epb group <name> <permission|info|create|delete|rename|clone> <add|remove|set|info> <string> <true|false>"));
            player.sendMessage(Component.text("§f[§9EP§f] §9Usage for users: /epb user <name> <group|permission|info> <add|remove|set|info> <string> <true|false|data>"));
            player.sendMessage(Component.text("§f[§9EP§f] §cDATA MultiState can be set by player, but must be modified via Plugin API."));
            return;
        }

        String targetType = args[0];
        String targetName = args[1];
        String actionType = args[2];

        if (targetType.equalsIgnoreCase("group")) {
            handleGroupAction(player, targetName, actionType, args);
        } else if (targetType.equalsIgnoreCase("user")) {
            handleUserAction(player, targetName, actionType, args);
        } else {
            player.sendMessage(Component.text("§f[§9EP§f] §cInvalid target type. Use 'group' or 'user'."));
        }
    }

    private void handleGroupAction(Player player, String targetName, String actionType, String[] args) {
        PermGroup group = new PermGroup(targetName);

        if (!player.hasPermission("elizonperms.group." + actionType.toLowerCase())) {
            player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to perform this action on groups.").build());
            return;
        }

        if (actionType.equalsIgnoreCase("permission")) {

            if(args.length < 4) {
                player.sendMessage(Component.text().content("§f[§9EP§f] §cMissing argument.").build());
                return;
            }

            String action = args[3];

            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.group.permission.add")) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to add permissions to groups.").build());
                        return;
                    }

                    if(args.length < 5) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cMissing argument.").build());
                        return;
                    }
                    group.setMultiStatePermission(args[4].toLowerCase(), MultiState.TRUE);
                    player.sendMessage(Component.text().content("§f[§9EP§f] §aAdding permission " + args[4] + " to group " + targetName).build());
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.group.permission.remove")) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to remove permissions from groups.").build());
                        return;
                    }

                    if(args.length < 5) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cMissing argument.").build());
                        return;
                    }
                    group.setMultiStatePermission(args[4].toLowerCase(), null);
                    player.sendMessage(Component.text().content("§f[§9EP§f] §cRemoving permission " + args[4] + " from group " + targetName).build());
                    break;
                case "set":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.group.permission.set")) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to set permissions for groups.").build());
                        return;
                    }
                    //value
                    if(args.length < 6) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cMissing argument.").build());
                        return;
                    }
                    MultiState state = MultiState.valueOf(args[5].toUpperCase());
                    group.setMultiStatePermission(args[4].toLowerCase(), state);
                    player.sendMessage(Component.text().content("§f[§9EP§f] §aSetting permission " + args[4] + " (" + args[5] + ") for group " + targetName).build());
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.group.permission.info")) {
                        player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to view permission info for groups.").build());
                        return;
                    }
                    player.sendMessage(Component.text().content("§f[§9EP§f] §bGetting info about permission in group " + targetName + "...").build());
                    Map<String, JSONObject> dataVals = group.getAllDataSetPermissions();
                    Map<String, Boolean> stateVals = group.getAllSimpleSetPermissions();

                    StringBuilder builder = new StringBuilder().append("§f[§9EP§f] §bGroup Info §f(RAW)");

                    if (!stateVals.isEmpty()) {
                        stateVals.forEach((permission, value) -> builder.append(Component.text().content("\n" + permission + " (" + value + ")")));
                        builder.append(Component.text().content("\n"));
                    }
                    if (!dataVals.isEmpty()) {
                        stateVals.forEach((permission, value) -> builder.append(Component.text().content("\n" + permission + " (" + value + ")")));
                        builder.append(Component.text().content("\n"));
                    }

                    player.sendMessage(Component.text().content(builder.toString()));
                    break;
                default:
                    player.sendMessage(Component.text().content(
                            "§f[§9EP§f] §bCommand Help §f(/epb group " + targetName + " permission ... <string> <true|false|data>) \n" +
                                    "§f[§9EP§f] §9> §aadd\n" +
                                    "§f[§9EP§f] §9> §cremove\n" +
                                    "§f[§9EP§f] §9> §aset\n" +
                                    "§f[§9EP§f] §9> §ainfo"
                    ).build());
                    break;
            }
        } else if (actionType.equalsIgnoreCase("create")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to create groups.").build());
                return;
            }
            group.build();
            player.sendMessage(Component.text().content("§f[§9EP§f] §aGroup " + targetName + " created successfully.").build());
        } else if (actionType.equalsIgnoreCase("delete")) {
            // Permission check for delete action
            if (!player.hasPermission("elizonperms.group.delete")) {
                player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to delete groups.").build());
                return;
            }
            group.deleteGroup();
            player.sendMessage(Component.text().content("§f[§9EP§f] §cGroup " + targetName + " deleted successfully.").build());
        } else if (actionType.equalsIgnoreCase("rename")) {
            // Permission check for rename action
            if (!player.hasPermission("elizonperms.group.rename")) {
                player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to rename groups.").build());
                return;
            }
            String newName = args[5];
            group.renameGroup(newName);
            player.sendMessage(Component.text().content("§f[§9EP§f] §bGroup " + targetName + " renamed to " + newName + ".").build());
        } else if (actionType.equalsIgnoreCase("info")) {
            // Permission check for info action
            if (!player.hasPermission("elizonperms.group.info")) {
                player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to view general info about groups.").build());
                return;
            }
            int groupSize = group.getPlayersInGroup().size();
            player.sendMessage(Component.text().content("§f[§9EP§f] §bGetting general info about group " + targetName + " (Size: " + groupSize + ")").build());
        } else if (actionType.equalsIgnoreCase("clone")) {
            // Permission check for clone action
            if (!player.hasPermission("elizonperms.group.clone")) {
                player.sendMessage(Component.text().content("§f[§9EP§f] §cYou don't have permission to clone groups.").build());
                return;
            }
            group.cloneGroup(args[5], false);
            player.sendMessage(Component.text().content("§f[§9EP§f] §aCloning group " + targetName + " to group " + args[5]).build());
        } else {
            // Help message
            player.sendMessage(Component.text().content(
                    "§f[§9EP§f] §bCommand Help §f(/epb group " + targetName + " <action>)\n" +
                            "§f[§9EP§f] §9> §apermission\n" +
                            "§f[§9EP§f] §9> §ainfo\n" +
                            "§f[§9EP§f] §9> §aclone"
            ).build());
        }
    }

    private void handleUserAction(Player player, String targetName, String actionType, String[] args) {
        PermPlayer permPlayer = new PermPlayer(net.md_5.bungee.api.ProxyServer.getInstance().getPlayer(targetName).getUniqueId().toString());

        if (!player.hasPermission("elizonperms.user." + actionType.toLowerCase())) {
            player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to perform this action on users.")));
            return;
        }

        if (actionType.equalsIgnoreCase("group")) {

            if(args.length < 4) {
                player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                return;
            }

            String action = args[3];
            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.user.group.add")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to add users to groups.")));
                        return;
                    }
                    if(args.length < 5) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                        return;
                    }
                    permPlayer.addGroup(args[4].toLowerCase());
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§aAdding user " + targetName + " to group " + args[4])));
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.user.group.remove")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to remove users from groups.")));
                        return;
                    }
                    if(args.length < 5) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                        return;
                    }
                    permPlayer.removeGroup(args[4].toLowerCase());
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cRemoving user " + targetName + " from group " + args[4])));
                    break;
                case "set":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.user.group.set")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to set user groups.")));
                        return;
                    }
                    if(args.length < 5) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                        return;
                    }
                    permPlayer.setGroup(args[4].toLowerCase());
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§aSetting group for user " + targetName + " to " + args[4])));
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.user.group.info")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to view group info for users.")));
                        return;
                    }
                    List<String> groups = permPlayer.getGroups();
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§bGroups of player: " + groups)));
                    break;
                default:
                    player.sendMessage(
                            Component.text("§f[§9EP§f] ").append(Component.text("§bCommand Help §f(/epb user " + targetName + " group ... <group>)\n"))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §aadd\n")))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §cremove\n")))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §aset\n")))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §ainfo")))
                    );
                    break;
            }
        } else if (actionType.equalsIgnoreCase("permission")) {

            if(args.length < 4) {
                player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                return;
            }

            String action = args[3];

            //permission

            if(args.length < 5) {
                player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                return;
            }
            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.user.permission.add")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to add permissions to users.")));
                        return;
                    }
                    permPlayer.setMultiStatePermission(args[4].toLowerCase(), MultiState.TRUE);
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§aAdding permission " + args[4] + " to user " + targetName)));
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.user.permission.remove")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to remove permissions from users.")));
                        return;
                    }
                    permPlayer.setMultiStatePermission(args[4].toLowerCase(), null);
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cRemoving permission " + args[4] + " from user " + targetName)));
                    break;
                case "set":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.user.permission.set")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to set permissions for users.")));
                        return;
                    }
                    //value
                    if(args.length < 6) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cMissing argument.")));
                        return;
                    }
                    permPlayer.setMultiStatePermission(args[4].toLowerCase(), MultiState.valueOf(args[5].toUpperCase()));
                    player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§aSetting permission " + args[4] + " (" + args[5] + ") for user " + targetName)));
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.user.permission.info")) {
                        player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to view permission info for users.")));
                        return;
                    }
                    Map<String, JSONObject> dataVals = permPlayer.getAllDataSetPermissions();
                    Map<String, Boolean> stateVals = permPlayer.getAllSimpleSetPermissions();

                    StringBuilder builder = new StringBuilder("§f[§9EP§f] ").append(Component.text("§bPermission Info for user ")).append(targetName).append("§f: \n");

                    // Append simple set permissions
                    builder.append("§aSimple Set Permissions§f: \n");
                    for (Map.Entry<String, Boolean> entry : stateVals.entrySet()) {
                        builder.append("§9- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }

                    // Append data set permissions
                    builder.append("§aData Set Permissions§f: \n");
                    for (Map.Entry<String, JSONObject> entry : dataVals.entrySet()) {
                        builder.append("§9- ").append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
                    }

                    player.sendMessage(Component.text().content(builder.toString()));
                    break;
                default:
                    player.sendMessage(
                            Component.text("§f[§9EP§f] ").append(Component.text("§bCommand Help §f(/epb user " + targetName + " permission ... <string> <true|false|data>)\n"))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §aadd\n")))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §cremove\n")))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §aset\n")))
                                    .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §ainfo")))
                    );
                    break;
            }
        } else if (actionType.equalsIgnoreCase("info")) {
            // Permission check for info action
            if (!player.hasPermission("elizonperms.user.info")) {
                player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§cYou don't have permission to view general info about users.")));
                return;
            }
            List<String> groups = permPlayer.getGroups();
            player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§bGetting general info about user " + targetName)));
            player.sendMessage(Component.text("§f[§9EP§f] ").append(Component.text("§bGroups: " + groups)));
        } else {
            player.sendMessage(
                    Component.text("§f[§9EP§f] ").append(Component.text("§bCommand Help §f(/epb user " + targetName + " <action>)\n"))
                            .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §apermission\n")))
                            .append(Component.text("§f[§9EP§f] ").append(Component.text("§9> §ainfo")))
            );
        }
    }

    @Nullable
    @Override
    public List<String> suggest(Invocation invocation) {

        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission("elizonperms.autocomplete")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete target type (group/user)
            completions.add("group");
            completions.add("user");
        } else if (args.length == 2) {
            // Complete target name (players/groups)
            if ("group".equalsIgnoreCase(args[0])) {
                // Complete group names
                // You can fetch group names from your data source
                PermGroup group = new PermGroup(null);
                completions.addAll(group.getAllGroups());
            } else if ("user".equalsIgnoreCase(args[0])) {
                // Complete online player names
                for (Player player : proxy.getAllPlayers()) {
                    completions.add(player.getUsername());
                }
                completions.add("uuid:");
            }
        } else if (args.length == 3) {
            // Complete action type (permission/info/create/delete/rename/clone)
            completions.add("permission");
            completions.add("info");
            completions.add("create");
            completions.add("delete");
            completions.add("rename");
            completions.add("clone");
        } else if (args.length == 4) {
            // Complete action (add/remove/set/info)
            completions.add("add");
            completions.add("remove");
            completions.add("set");
            completions.add("info");
        } else if (args.length == 5) {
            // Complete permission/group name for actions that require it
            if ("permission".equalsIgnoreCase(args[2])) {
                return Collections.emptyList();
            } else if ("group".equalsIgnoreCase(args[2])) {
                // Provide your list of permissions/groups here
                PermGroup group = new PermGroup(null);
                completions.addAll(group.getAllGroups());
                // Add more permissions/groups as needed
            } else if ("rename".equalsIgnoreCase(args[2]) || "clone".equalsIgnoreCase(args[2])) {
                return Collections.emptyList();
            } else if ("delete".equalsIgnoreCase(args[2]) || "create".equalsIgnoreCase(args[2])) {
                // Command ends after this point, no further completions needed
                return Collections.emptyList();
            }
        } else if (args.length == 6) {
            // Complete true/false for the 6th argument
            completions.add("true");
            completions.add("false");
            if ("permission".equalsIgnoreCase(args[2])) {
                completions.add("data");
            }
        }

        return completions;
    }

}
