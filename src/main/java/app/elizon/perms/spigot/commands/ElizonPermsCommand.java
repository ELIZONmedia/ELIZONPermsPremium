package app.elizon.perms.spigot.commands;

import app.elizon.perms.pkg.group.PermGroup;
import app.elizon.perms.pkg.group.track.PermGroupTrack;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.pkg.util.MultiState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;

public class ElizonPermsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!sender.hasPermission("elizonperms.command.execute")) {
            sender.sendMessage("§f[§9EP§f] §aThis server is using the free ElizonPerms Lite permissions system by ELIZONMedia. Thanks for using!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§f[§9EP§f] §9Usage for groups:\n" +
                    "  /epb group <name> info\n" +
                    "  /epb group <name> permission info\n" +
                    "  /epb group <name> create\n" +
                    "  /epb group <name> delete\n" +
                    "  /epb group <name> rename <new name>\n" +
                    "  /epb group <name> clone <name>\n" +
                    "  /epb group <name> permission add <permission>\n" +
                    "  /epb group <name> permission remove <permission>\n" +
                    "  /epb group <name> permission set <permission> true\n" +
                    "  /epb group <name> permission set <permission> false\n" +
                    "  /epb group <name> permission set <permission> data (Initialize data via API)\n" +
                    "  /epb group <name> inherit info\n" +
                    "  /epb group <name> inherit add <group to inherit>\n" +
                    "  /epb group <name> inherit remove <group>\n" +
                    "  /epb group <name> setdefault\n" +
                    "  /epb group <name> setprefix <prefix>\n" +
                    "  /epb group <name> setsuffix <suffix>\n" +
                    "  /epb group <name> setheight <height>\n" +
                    "§f[§9EP§f] §9Usage for users:\n" +
                    "  /epb user <name> info\n" +
                    "  /epb user <name> permission info\n" +
                    "  /epb user <name> group info\n" +
                    "  /epb user <name> group add <group>\n" +
                    "  /epb user <name> group addtimed <group> <time in hours>\n" +
                    "  /epb user <name> group set <group> true\n" +
                    "  /epb user <name> group set <group> false\n" +
                    "  /epb user <name> group set <group> data (Initialize data via API)\n" +
                    "  /epb user <name> group settimed <group> true <time in hours>\n" +
                    "  /epb user <name> group settimed <group> false <time in hours>\n" +
                    "  /epb user <name> group settimed <group> data <time in hours> (Initialize data via API)\n" +
                    "  /epb user <name> permission add <permission>\n" +
                    "  /epb user <name> permission remove <permission>\n" +
                    "  /epb user <name> permission set <permission> true\n" +
                    "  /epb user <name> permission set <permission> false\n" +
                    "  /epb user <name> permission set <permission> data (Initialize data via API)\n" +
                    "§f[§9EP§f] §9Usage for tracks:\n" +
                    "  /epb track <name> list\n" +
                    "  /epb track <name> delete\n" +
                    "  /epb track <name> create <group> <group> <group>...\n" +
                    "  /epb track <name> insertbefore <group to insert> <next higher group>\n" +
                    "  /epb track <name> add <new highest group>\n" +
                    "  /epb track <name> remove <group>\n" +
                    "  /epb track <name> next <group>\n" +
                    "  /epb track <name> rankup <username>");

            sender.sendMessage("§f[§9EP§f] §cDATA MultiState can be set by player, but must be modified via Plugin API.");
            return true;
        }

        String targetType = args[0];
        String targetName = args[1];
        String actionType = args[2];

        if (targetType.equalsIgnoreCase("group")) {
            handleGroupAction(sender, targetName, actionType, args);
        } else if (targetType.equalsIgnoreCase("user")) {
            handleUserAction(sender, targetName, actionType, args);
        } else if (targetType.equalsIgnoreCase("track")) {
            handleTrackAction(sender, targetName, actionType, args);
        } else {
            sender.sendMessage("§f[§9EP§f] §cInvalid target type. Use 'group' or 'user'.");
        }
        return true;
    }

    private void handleTrackAction(CommandSender player, String targetName, String actionType, String[] args) {
        PermGroupTrack groupTrack = new PermGroupTrack();

        if (!player.hasPermission("elizonperms.track." + actionType.toLowerCase())) {
            player.sendMessage("§f[§9EP§f] §cYou don't have permission to perform this action on tracks.");
            return;
        }

        switch (actionType.toLowerCase()) {
            case "insertbefore":
                // Insert group before next
                if (args.length < 5) {
                    player.sendMessage("§f[§9EP§f] §cMissing arguments.");
                    return;
                }
                String groupToInsert = args[3];
                String nextGroup = args[4];
                groupTrack.insertGroupBeforeNext(targetName, groupToInsert, nextGroup);
                player.sendMessage("§f[§9EP§f] §aInserted group " + groupToInsert + " before " + nextGroup + " in track " + targetName);
                break;
            case "create":
                // Add group to end
                if (args.length < 3) {
                    player.sendMessage("§f[§9EP§f] §cMissing arguments.");
                    return;
                }

                List<String> str = new ArrayList<>();

                for (int i = 3; i < args.length; i++) {
                    if (args[i] != null) {
                        str.add(args[i].toLowerCase());
                    }
                }

                groupTrack.createTrack(targetName, str);
                player.sendMessage("§f[§9EP§f] §aCreated track " + targetName + " with the following groups: " + str);
                break;

            case "add":
                // Add group to end
                if (args.length < 4) {
                    player.sendMessage("§f[§9EP§f] §cMissing arguments.");
                    return;
                }
                String groupToAdd = args[3];
                groupTrack.addGroupToEnd(targetName, groupToAdd);
                player.sendMessage("§f[§9EP§f] §aAdded group " + groupToAdd + " to the end of track " + targetName);
                break;
            case "remove":
                // Add group to end
                if (args.length < 4) {
                    player.sendMessage("§f[§9EP§f] §cMissing arguments.");
                    return;
                }
                String groupToRemove = args[3];
                groupTrack.removeGroupFromTrack(targetName, groupToRemove);
                player.sendMessage("§f[§9EP§f] §cRemoved group " + groupToRemove + " from the track " + targetName);
                break;
            case "delete":
                // Delete track
                groupTrack.deleteTrack(targetName);
                player.sendMessage("§f[§9EP§f] §cDeleted track " + targetName);
                break;
            case "next":
                // Get next group
                if (args.length < 4) {
                    player.sendMessage("§f[§9EP§f] §cMissing arguments.");
                    return;
                }
                String currentGroup = args[3];
                String nextGroup2 = groupTrack.getNextGroup(targetName, currentGroup);
                if (nextGroup2 != null) {
                    player.sendMessage("§f[§9EP§f] §bNext group after " + currentGroup + " in track " + targetName + ": " + nextGroup2);
                } else {
                    player.sendMessage("§f[§9EP§f] §cNo next group found after " + currentGroup + " in track " + targetName);
                }
                break;
            case "list":
                // List groups in track
                List<String> groups = groupTrack.getGroups(targetName);
                if (groups != null && !groups.isEmpty()) {
                    player.sendMessage("§f[§9EP§f] §bGroups in track " + targetName + ": " + String.join(", ", groups));
                } else {
                    player.sendMessage("§f[§9EP§f] §cNo groups found in track " + targetName);
                }
                break;
            case "rankup":
                // Rank up player
                if (args.length < 4) {
                    player.sendMessage("§f[§9EP§f] §cMissing arguments.");
                    return;
                }

                if (!player.hasPermission("elizonperms.user.group.set")) {
                    player.sendMessage("§f[§9EP§f] §cYou don't have permission to perform this action on groups.");
                    return;
                }

                String playerName = args[3].toLowerCase();

                PermPlayer target;
                if (playerName.startsWith("uuid:")) {
                    target = new PermPlayer(playerName.replaceFirst("uuid:", ""));
                } else {
                    if (Bukkit.getPlayer(playerName) == null) {
                        player.sendMessage("§f[§9EP§f] §cThe target player is offline. Use the uuid like this instead: \"uuid:UUID HERE\"");
                        return;
                    }
                    target = new PermPlayer(Objects.requireNonNull(Bukkit.getPlayer(playerName)).getUniqueId().toString());
                }

                if (target.getGroups().size() != 1) {
                    player.sendMessage("§f[§9EP§f] §cThere is no rank to rankup. Please note, that the player must have only one rank.");
                } else {
                    String next = groupTrack.getNextGroup(targetName, target.getGroups().getFirst());
                    if (next != null) {
                        target.setGroup(next, -1);
                        player.sendMessage("§f[§9EP§f] §aRanked up player from group " + target.getGroups().getFirst() + " to " + next + ".");
                    } else {
                        player.sendMessage("§f[§9EP§f] §cThere is no rank to rankup. Please note, that the player must have only one rank.");
                    }
                }

                break;
            default:
                player.sendMessage("§f[§9EP§f] §cInvalid action type.");
                break;
        }
    }



    private void handleGroupAction(CommandSender player, String targetName, String actionType, String[] args) {
        PermGroup group = new PermGroup(targetName);

        if (!player.hasPermission("elizonperms.group." + actionType.toLowerCase())) {
            player.sendMessage("§f[§9EP§f] §cYou don't have permission to perform this action on groups.");
            return;
        }

        if (actionType.equalsIgnoreCase("permission")) {
            if (args.length < 3) {
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

                    if (args.length < 5) {
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

                    if (args.length < 5) {
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
                    if (args.length < 6) {
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
                    StringBuilder builder = getStringBuilder(group);

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
        } else if (actionType.equalsIgnoreCase("inherit")) {


            if(args.length < 3) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }

            String action = args[3];

            switch (action.toLowerCase()) {
                case "add":
                    // Permission check for add action
                    if (!player.hasPermission("elizonperms.group.inherit.add")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to add inherit groups to groups.");
                        return;
                    }

                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    group.addGroupInherit(args[4].toLowerCase());
                    player.sendMessage("§f[§9EP§f] §aNow inheriting group " + args[4] + " at group " + targetName);
                    break;
                case "remove":
                    // Permission check for remove action
                    if (!player.hasPermission("elizonperms.group.inherit.remove")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to remove group inherits from groups.");
                        return;
                    }

                    if(args.length < 5) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }
                    group.removeGroupInherit(args[4].toLowerCase());
                    player.sendMessage("§f[§9EP§f] §cRemoving inherit of group " + args[4] + " from group " + targetName);
                    break;
                case "info":
                    // Permission check for info action
                    if (!player.hasPermission("elizonperms.group.inherit.info")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to view inherit info for groups.");
                        return;
                    }
                    player.sendMessage("§f[§9EP§f] §bGetting info about inherits of group " + targetName + "...");
                    List<String> inheritVals = group.getInheritedGroups();

                    StringBuilder builder = new StringBuilder("§f[§9EP§f] §bGroup Inherit Info §f(RAW)");
                    if (!inheritVals.isEmpty()) {
                        inheritVals.forEach((value) -> builder.append("\n").append(value));
                        builder.append("\n");
                    }

                    player.sendMessage(builder.toString());
                    break;
                default:
                    player.sendMessage("§f[§9EP§f] §bCommand Help §f(/epb group " + targetName + " permission ... <string> <true|false|data>) \n" +
                                    "§f[§9EP§f] §9> §aadd\n" +
                                    "§f[§9EP§f] §9> §cremove\n" +
                                    "§f[§9EP§f] §9> §aset\n" +
                                    "§f[§9EP§f] §9> §ainfo"
                    );
                    break;
            }
        } else if (actionType.equalsIgnoreCase("setdefault")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to create groups.");
                return;
            }

            group.setAsDefaultGroup();
            player.sendMessage("§f[§9EP§f] §aGroup " + targetName + " is now the default group.");
        } else if (actionType.equalsIgnoreCase("setprefix")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to create groups.");
                return;
            }

            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }
            group.setGroupPrefixSuffix(args[3].replaceAll("&", "§"), null, -1);
            player.sendMessage("§f[§9EP§f] §aGroup " + targetName + " is now the default group.");
        } else if (actionType.equalsIgnoreCase("setsuffix")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to create groups.");
                return;
            }

            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }
            group.setGroupPrefixSuffix(null, args[3].replaceAll("&", "§"), -1);
            player.sendMessage("§f[§9EP§f] §aGroup " + targetName + " is now the default group.");
        } else if (actionType.equalsIgnoreCase("setheight")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to create groups.");
                return;
            }

            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }
            group.setGroupPrefixSuffix(null, null, Integer.parseInt(args[3]));
            player.sendMessage("§f[§9EP§f] §aGroup " + targetName + " is now the default group.");
        } else if (actionType.equalsIgnoreCase("create")) {
            // Permission check for create action
            if (!player.hasPermission("elizonperms.group.create")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to create groups.");
                return;
            }
            player.sendMessage("§f[§9EP§f] §aGroup " + targetName + " ( " + group.build() + " ) created.");
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
            if(args.length < 4) {
                player.sendMessage("§f[§9EP§f] §cMissing argument.");
                return;
            }
            String newName = args[3];
            group.renameGroup(newName);
            player.sendMessage("§f[§9EP§f] §bGroup " + targetName + " renamed to " + newName + ".");
        } else if (actionType.equalsIgnoreCase("info")) {
            // Permission check for info action
            if (!player.hasPermission("elizonperms.group.info")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to view general info about groups.");
                return;
            }
            int groupSize = group.getPlayersInGroup().size();
            String prefix = group.getPrefix();
            String suffix = group.getSuffix();
            int height = group.getSortHeight();
            List<String> inheritedGroups = group.getInheritedGroups();
            player.sendMessage("§f[§9EP§f] §bGeneral Information for Group: " + targetName + "\n" +
                    "§f[§9EP§f] §bSize: " + groupSize + "\n" +
                    "§f[§9EP§f] §bPrefix: " + prefix + "\n" +
                    "§f[§9EP§f] §bSuffix: " + suffix + "\n" +
                    "§f[§9EP§f] §bSort Height: " + height + "\n" +
                    "§f[§9EP§f] §bInherited Groups: " + inheritedGroups);
        } else if (actionType.equalsIgnoreCase("clone")) {
            // Permission check for clone action
            if (!player.hasPermission("elizonperms.group.clone")) {
                player.sendMessage("§f[§9EP§f] §cYou don't have permission to clone groups.");
                return;
            }
            group.cloneGroup(args[3], false);
            player.sendMessage("§f[§9EP§f] §aCloning group " + targetName + " to group " + args[3]);
        } else {
            // Help message
            player.sendMessage("§f[§9EP§f] §bCommand Help §f(/epb group " + targetName + " <action>)\n" +
                            "§f[§9EP§f] §9> §apermission\n" +
                            "§f[§9EP§f] §9> §ainfo\n" +
                            "§f[§9EP§f] §9> §aclone\n" +
                            "§f[§9EP§f] §9> §arename\n" +
                            "§f[§9EP§f] §9> §adelete\n" +
                            "§f[§9EP§f] §9> §acreate\n" +
                            "§f[§9EP§f] §9> §asetdefault\n" +
                            "§f[§9EP§f] §9> §asetprefix\n" +
                            "§f[§9EP§f] §9> §asetsuffix\n" +
                            "§f[§9EP§f] §9> §asetheight"
            );
        }
    }

    private static @NotNull StringBuilder getStringBuilder(PermGroup group) {
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
        return builder;
    }


    private void handleUserAction(CommandSender player, String targetName, String actionType, String[] args) {
        PermPlayer permPlayer;
        if(targetName.toLowerCase().startsWith("uuid:")) {
            permPlayer = new PermPlayer(targetName.toLowerCase().replaceFirst("uuid:", ""));
        } else {
            if(Bukkit.getPlayer(targetName) == null) {
                player.sendMessage("§f[§9EP§f] §cThe target player is offline. Use uuid: with the uuid of the target player instead.");
                return;
            }
            permPlayer = new PermPlayer(Objects.requireNonNull(Bukkit.getPlayer(targetName)).getUniqueId().toString());
        }

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
                    permPlayer.addGroup(args[4].toLowerCase(), -1);
                    player.sendMessage("§f[§9EP§f] §aAdding user " + targetName + " to group " + args[4]);
                    break;
                case "addtimed":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.user.group.set")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to set user groups.");
                        return;
                    }
                    if(args.length < 6) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }

                    long time = 1000*60*60*Long.parseLong(args[5]);

                    permPlayer.addGroup(args[4].toLowerCase(), time);
                    player.sendMessage("§f[§9EP§f] §aSetting group for user " + targetName + " to " + args[4]);
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
                    permPlayer.setGroup(args[4].toLowerCase(), -1);
                    player.sendMessage("§f[§9EP§f] §aSetting group for user " + targetName + " to " + args[4]);
                    break;
                case "settimed":
                    // Permission check for set action
                    if (!player.hasPermission("elizonperms.user.group.set")) {
                        player.sendMessage("§f[§9EP§f] §cYou don't have permission to set user groups.");
                        return;
                    }
                    if(args.length < 6) {
                        player.sendMessage("§f[§9EP§f] §cMissing argument.");
                        return;
                    }

                    long time2 = 1000*60*60*Long.parseLong(args[5]);

                    permPlayer.setGroup(args[4].toLowerCase(), time2);
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
                    player.sendMessage("§f[§9EP§f] §bCommand Help §f(/epb user " + targetName + " group ... <group>)\n" +
                                    "§f[§9EP§f] §9> §aadd\n" +
                                    "§f[§9EP§f] §9> §aaddtimed\n" +
                                    "§f[§9EP§f] §9> §cremove\n" +
                                    "§f[§9EP§f] §9> §aset\n" +
                                    "§f[§9EP§f] §9> §asettimed\n" +
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
                    player.sendMessage("§f[§9EP§f] §bCommand Help §f(/epb user " + targetName + " permission ... <string> <true|false|data>)\n" +
                            "§f[§9EP§f] §9> §aadd\n" +
                            "§f[§9EP§f] §9> §cremove\n" +
                            "§f[§9EP§f] §9> §aset\n" +
                            "§f[§9EP§f] §9> §ainfo");
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
            player.sendMessage("§f[§9EP§f] §bCommand Help §f(/epb user " + targetName + " <action>)\n" +
                            "§f[§9EP§f] §9> §apermission\n" +
                            "§f[§9EP§f] §9> §ainfo"
            );
        }
    }



    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("elizonperms.autocomplete")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete target type (group/user)
            completions.add("group");
            completions.add("user");
            completions.add("track");
        } else if (args.length == 2) {
            // Complete target name (players/groups)
            if ("group".equalsIgnoreCase(args[0])) {
                // Complete group names
                // You can fetch group names from your data source
                PermGroup group = new PermGroup(null);
                completions.addAll(group.getAllGroups());
            } else if ("user".equalsIgnoreCase(args[0])) {
                // Complete online player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                completions.add("uuid:");
            } else if ("track".equalsIgnoreCase(args[0])) {
                // Complete online player names
                return new PermGroupTrack().getAllTracks();
            }
        } else if (args.length == 3) {
            // Complete action type (permission/info/create/delete/rename/clone/track)
            if ("group".equalsIgnoreCase(args[0])) {
                completions.add("create");
                completions.add("delete");
                completions.add("clone");
                completions.add("rename");
                completions.add("permission");
                completions.add("info");
                completions.add("setdefault");
                completions.add("setprefix");
                completions.add("setsuffix");
                completions.add("setheight");
            } else if ("user".equalsIgnoreCase(args[0])) {
                completions.add("group");
                completions.add("permission");
                completions.add("info");
            } else if ("track".equalsIgnoreCase(args[0])) {
                completions.add("insertbefore");
                completions.add("add");
                completions.add("delete");
                completions.add("next");
                completions.add("list");
                completions.add("rankup");
            }
        } else if (args.length == 4) {
            // Complete action (add/remove/set/info)
            completions.add("add");
            completions.add("remove");
            completions.add("set");
            completions.add("info");
            if("rankup".equalsIgnoreCase(args[2])) {
                completions.clear();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if ("group".equalsIgnoreCase(args[0])) {
                if("create".equalsIgnoreCase(args[2])) {
                    return Collections.emptyList();
                }
            }
            if ("group".equalsIgnoreCase(args[2])) {
                completions.add("addtimed");
                completions.add("settimed");
            } else if ("delete".equalsIgnoreCase(args[2])) {
                return Collections.emptyList();
            } else if ("track".equalsIgnoreCase(args[0])) {
                completions.clear();
                if ("remove".equalsIgnoreCase(args[2])) {
                    PermGroupTrack track = new PermGroupTrack();
                    completions.addAll(track.getGroups(args[1].toLowerCase()));
                } else if ("add".equalsIgnoreCase(args[2])) {
                    PermGroup group = new PermGroup(null);
                    completions.addAll(group.getAllGroups());
                }
            }
        } else if (args.length == 5) {
            // Complete permission/group name for actions that require it
            if ("permission".equalsIgnoreCase(args[2])) {
                return Collections.emptyList();
            } else if ("group".equalsIgnoreCase(args[2])) {
                // Provide your list of permissions/groups here
                PermGroup group = new PermGroup(null);
                completions.addAll(group.getAllGroups());
                // Add more permissions/groups as needed
            } else if ("delete".equalsIgnoreCase(args[2])) {
                return Collections.emptyList();
            } else if ("insertbefore".equalsIgnoreCase(args[2])) {
                PermGroupTrack track = new PermGroupTrack();
                completions.addAll(track.getGroups(args[1].toLowerCase()));
            } else if("track".equalsIgnoreCase(args[0])) {
                if("insertbefore".equalsIgnoreCase(args[2])) {
                    PermGroup group = new PermGroup(null);
                    completions.addAll(group.getAllGroups());
                }
            }
        } else if (args.length == 6) {
            // Complete true/false for the 6th argument
            if ("permission".equalsIgnoreCase(args[2])) {
                completions.add("data");
                completions.add("true");
                completions.add("false");
            } else if ("group".equalsIgnoreCase(args[2])) {
                completions.add("time in hours");
            }
        }

        return completions;
    }
}
