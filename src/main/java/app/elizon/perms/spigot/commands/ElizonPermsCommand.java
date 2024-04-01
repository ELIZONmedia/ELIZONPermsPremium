package app.elizon.perms.spigot.commands;

import app.elizon.perms.pkg.group.PermGroup;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.pkg.util.MultiState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ElizonPermsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("elizonperms.command.execute")) {
            sender.sendMessage("§f[§9EP§f] §aThis server is using the free ElizonPerms Lite permissions system by ELIZONMedia. Thanks for using!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§f[§9EP§f] §9Usage for groups: /epb group <name> <permission|info|create|delete|rename|clone> <add|remove|set|info> <string> <true|false>");
            player.sendMessage("§f[§9EP§f] §9Usage for users: /epb user <name> <group|permission|info> <add|remove|set|info> <string> <true|false|data>");
            player.sendMessage("§f[§9EP§f] §cDATA MultiState can be set by player, but must be modified via Plugin API.");
            return true;
        }

        String targetType = args[0];
        String targetName = args[1];
        String actionType = args[2];

        if (targetType.equalsIgnoreCase("group")) {
            handleGroupAction(player, targetName, actionType, args);
        } else if (targetType.equalsIgnoreCase("user")) {
            handleUserAction(player, targetName, actionType, args);
        } else {
            player.sendMessage("§f[§9EP§f] §cInvalid target type. Use 'group' or 'user'.");
        }

        return true;
    }

    private void handleGroupAction(Player player, String targetName, String actionType, String[] args) {
        PermGroup group = new PermGroup(targetName);

        if (!player.hasPermission("elizonperms.group." + actionType.toLowerCase())) {
            player.sendMessage("§f[§9EP§f] §cYou don't have permission to perform this action on groups.");
            return;
        }

        if (actionType.equalsIgnoreCase("permission")) {


            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }

            String action = args[3];

            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.group.permission.add")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to add permissions to groups.");
                        return;
                    }

                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    group.setMultiStatePermission(args[4].toLowerCase(), MultiState.TRUE);
                    player.sendMessage("§f[§9EP§f] §aAdding permission " + args[4] + " to group " + targetName);
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.group.permission.remove")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to remove permissions from groups.");
                        return;
                    }

                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    group.setMultiStatePermission(args[4].toLowerCase(), null);
                    player.sendMessage("§f[§9EP§f] §cRemoving permission " + args[4] + " from group " + targetName);
                    break;
                case "set":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.group.permission.set")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to set permissions for groups.");
                        return;
                    }
                    //value
                    if(args.length < 6) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    MultiState state = MultiState.valueOf(args[5].toUpperCase());
                    group.setMultiStatePermission(args[4].toLowerCase(), state);
                    player.sendMessage("§f[§9EP§f] §aSetting permission " + args[4] + " (" + args[5] + ") for group " + targetName);
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.group.permission.info")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to view permission info for groups.");
                        return;
                    }
                    player.sendMessage("§f[§9EP§f] §bGetting info about permission in group " + targetName + "...");
                    Map<String, JSONObject> dataVals = group.getAllDataSetPermissions();
                    Map<String, Boolean> stateVals = group.getAllSimpleSetPermissions();

                    StringBuilder builder = new StringBuilder("§f[§9EP§f] §bGroup Info §f(RAW)");
                    if (!stateVals.isEmpty()) {
                        stateVals.forEach((permission, value) -> builder.append("\n").append(permission).append(" (").append(value).append(")"));
                        builder.append("\n");
                    }
                    if (!dataVals.isEmpty()) {
                        dataVals.forEach((permission, data) -> builder.append("\n").append(permission).append(" (").append(data.toString()).append(")"));
                        builder.append("\n");
                    }

                    player.sendMessage(builder.toString());
                    break;
                default:
                    player.sendMessage(
                            "§f[§9EP§f] §bCommand Help §f(/epb group " + targetName + " permission ... <string> <true|false|data>) \n" +
                                    "§f[§9EP§f] §9> §aadd\n" +
                                    "§f[§9EP§f] §9> §cremove\n" +
                                    "§f[§9EP§f] §9> §aset\n" +
                                    "§f[§9EP§f] §9> §ainfo"
                    );
                    break;
            }
        } else if (actionType.equalsIgnoreCase("create")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to create groups.");
                return;
            }
            group.build();
            player.sendMessage("§f[§9EP§f] §aGroup " + targetName + " created successfully.");
        } else if (actionType.equalsIgnoreCase("delete")) {
            // Permission check for delete action
            if (!player.hasPermission("elizonperms.group.delete")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to delete groups.");
                return;
            }
            group.deleteGroup();
            player.sendMessage("§f[§9EP§f] §cGroup " + targetName + " deleted successfully.");
        } else if (actionType.equalsIgnoreCase("rename")) {
            // Permission check for rename action
            if (!player.hasPermission("elizonperms.group.rename")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to rename groups.");
                return;
            }
            String newName = args[5];
            group.renameGroup(newName);
            player.sendMessage("§f[§9EP§f] §bGroup " + targetName + " renamed to " + newName + ".");
        } else if (actionType.equalsIgnoreCase("info")) {
            // Permission check for info action
            if (!player.hasPermission("elizonperms.group.info")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to view general info about groups.");
                return;
            }
            int groupSize = group.getPlayersInGroup().size();
            player.sendMessage("§f[§9EP§f] §bGetting general info about group " + targetName + " (Size: " + groupSize + ")");
        } else if (actionType.equalsIgnoreCase("clone")) {
            // Permission check for clone action
            if (!player.hasPermission("elizonperms.group.clone")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to clone groups.");
                return;
            }
            group.cloneGroup(args[5], false);
            player.sendMessage("§f[§9EP§f] §aCloning group " + targetName + " to group " + args[5]);
        } else {
            // Help message
            player.sendMessage(
                    "§f[§9EP§f] §bCommand Help §f(/epb group " + targetName + " <action>)\n" +
                            "§f[§9EP§f] §9> §apermission\n" +
                            "§f[§9EP§f] §9> §ainfo\n" +
                            "§f[§9EP§f] §9> §aclone"
            );
        }
    }


    private void handleUserAction(Player player, String targetName, String actionType, String[] args) {
        PermPlayer permPlayer = new PermPlayer(Bukkit.getOfflinePlayer(targetName).getUniqueId().toString());

        if (!player.hasPermission("elizonperms.user." + actionType.toLowerCase())) {
            player.sendMessage("§f[§9EP§f] §cYou don't have permission to perform this action on users.");
            return;
        }

        if (actionType.equalsIgnoreCase("group")) {

            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }

            String action = args[3];
            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.user.group.add")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to add users to groups.");
                        return;
                    }
                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    permPlayer.addGroup(args[4].toLowerCase());
                    player.sendMessage("§f[§9EP§f] §aAdding user " + targetName + " to group " + args[4]);
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.user.group.remove")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to remove users from groups.");
                        return;
                    }
                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    permPlayer.removeGroup(args[4].toLowerCase());
                    player.sendMessage("§f[§9EP§f] §cRemoving user " + targetName + " from group " + args[4]);
                    break;
                case "set":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.user.group.set")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to set user groups.");
                        return;
                    }
                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    permPlayer.setGroup(args[4].toLowerCase());
                    player.sendMessage("§f[§9EP§f] §aSetting group for user " + targetName + " to " + args[4]);
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.user.group.info")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to view group info for users.");
                        return;
                    }
                    List<String> groups = permPlayer.getGroups();
                    player.sendMessage("§f[§9EP§f] §bGroups of player: " + groups);
                    break;
                default:
                    player.sendMessage(
                            "§f[§9EP§f] §bCommand Help §f(/epb user " + targetName + " group ... <group>)\n" +
                                    "§f[§9EP§f] §9> §aadd\n" +
                                    "§f[§9EP§f] §9> §cremove\n" +
                                    "§f[§9EP§f] §9> §aset\n" +
                                    "§f[§9EP§f] §9> §ainfo"
                    );
                    break;
            }
        } else if (actionType.equalsIgnoreCase("permission")) {

            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }

            String action = args[3];

            //permission

            if(args.length < 5) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }
            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.user.permission.add")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to add permissions to users.");
                        return;
                    }
                    permPlayer.setMultiStatePermission(args[4].toLowerCase(), MultiState.TRUE);
                    player.sendMessage("§f[§9EP§f] §aAdding permission " + args[4] + " to user " + targetName);
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.user.permission.remove")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to remove permissions from users.");
                        return;
                    }
                    permPlayer.setMultiStatePermission(args[4].toLowerCase(), null);
                    player.sendMessage("§f[§9EP§f] §cRemoving permission " + args[4] + " from user " + targetName);
                    break;
                case "set":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.user.permission.set")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to set permissions for users.");
                        return;
                    }
                    //value
                    if(args.length < 6) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    permPlayer.setMultiStatePermission(args[4].toLowerCase(), MultiState.valueOf(args[5].toUpperCase()));
                    player.sendMessage("§f[§9EP§f] §aSetting permission " + args[4] + " (" + args[5] + ") for user " + targetName);
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.user.permission.info")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to view permission info for users.");
                        return;
                    }
                    Map<String, JSONObject> dataVals = permPlayer.getAllDataSetPermissions();
                    Map<String, Boolean> stateVals = permPlayer.getAllSimpleSetPermissions();

                    StringBuilder builder = new StringBuilder("§f[§9EP§f] §bPermission Info for user ").append(targetName).append("§f: \n");

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

                    player.sendMessage(builder.toString());
                    break;
                default:
                    player.sendMessage(
                            "§f[§9EP§f] §bCommand Help §f(/epb user " + targetName + " permission ... <string> <true|false|data>)\n" +
                                    "§f[§9EP§f] §9> §aadd\n" +
                                    "§f[§9EP§f] §9> §cremove\n" +
                                    "§f[§9EP§f] §9> §aset\n" +
                                    "§f[§9EP§f] §9> §ainfo"
                    );
                    break;
            }
        } else if (actionType.equalsIgnoreCase("info")) {
            // Permission check for info action
            if (!player.hasPermission("elizonperms.user.info")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to view general info about users.");
                return;
            }
            List<String> groups = permPlayer.getGroups();
            player.sendMessage("§f[§9EP§f] §bGetting general info about user " + targetName);
            player.sendMessage("§f[§9EP§f] §bGroups: " + groups);
        } else {
            player.sendMessage(
                    "§f[§9EP§f] §bCommand Help §f(/epb user " + targetName + " <action>)\n" +
                            "§f[§9EP§f] §9> §apermission\n" +
                            "§f[§9EP§f] §9> §ainfo"
            );
        }
    }



    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("elizonperms.autocomplete")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // Complete target type (group/user)
            return List.of("group", "user");
        } else if (args.length == 2) {
            // Complete target name (players/groups)
            if (args[0].equalsIgnoreCase("group")) {
                // Complete group names
                // You can fetch group names from your data source
                PermGroup group = new PermGroup(null);
                return group.getAllGroups();
            } else if (args[0].equalsIgnoreCase("user")) {
                // Complete online player names
                List<String> names = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                names.addFirst("uuid:");
                return names;
            }
        } else if (args.length == 3) {
            // Complete action type (permission/info/create/delete/rename/clone)
            return List.of("permission", "info", "create", "delete", "rename", "clone");
        } else if (args.length == 4) {
            // Complete action (add/remove/set/info)
            if (args[0].equalsIgnoreCase("group")) {
                return List.of("add", "remove", "set", "info");
            }
            return List.of("add", "remove", "set", "info");
        } else if (args.length == 5) {
            // Complete permission/group name for actions that require it
            if (args[2].equalsIgnoreCase("permission") || args[2].equalsIgnoreCase("group")) {
                // Provide your list of permissions/groups here
                List<String> perms = new ArrayList<>();
                for (Permission permission : Bukkit.getPluginManager().getPermissions().stream().toList()) {
                    perms.add(permission.getName());
                }
                return perms;
            } else if (args[2].equalsIgnoreCase("rename") || args[2].equalsIgnoreCase("clone")) {
                PermGroup group = new PermGroup(null);
                return group.getAllGroups();
            } else if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("create")) {
                // Command ends after this point, no further completions needed
                return List.of();
            }
        } else if (args.length == 6) {
            // Complete true/false for the 6th argument
            return List.of("true", "false");
        } else if (args.length == 7 && args[4].equalsIgnoreCase("clone")) {
            // Complete group name for clone action
            // You can fetch group names from your data source
            PermGroup group = new PermGroup(null);
            return group.getAllGroups();
        }
        return List.of();
    }
}
