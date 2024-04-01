package app.elizon.perms.pkg;

import app.elizon.perms.pkg.player.PermPlayer;
import co.plocki.asyncthread.AsyncThreadScheduler;
import co.plocki.json.JSONFile;
import co.plocki.json.JSONValue;
import co.plocki.mysql.*;
import org.json.JSONObject;

import java.util.HashMap;

public class Initializer {

    private static MySQLDriver driver;
    private static String discordBotToken;

    private static MySQLTable.fin playerTable;
    private static MySQLTable.fin groupTable;
    private static MySQLTable.fin playerRankTraces;
    private static MySQLTable.fin permGroupTime;
    private static MySQLTable.fin permGroupPrefixSuffix;
    private static MySQLTable.fin permGroupInherits;
    private static MySQLTable.fin permRankTracks;
    private static MySQLTable.fin permDiscordLink;
    private static MySQLTable.fin permFallbackGroup;

    public void init() {
        driver = new MySQLDriver();

        MySQLTable table = new MySQLTable();
        table.prepare("permPlayer", "uuid", "permissionsJson", "groupsJson");
        playerTable = table.build();

        MySQLTable table1 = new MySQLTable();
        table1.prepare("permGroups", "name", "permissionsJson");
        groupTable = table1.build();

        MySQLTable table2 = new MySQLTable();
        table2.prepare("permRankTraces", "uuid", "oldGroup", "newGroup", "addedForTime", "keepOldGroup", "changeDate");
        playerRankTraces = table2.build();

        MySQLTable table3 = new MySQLTable();
        table3.prepare("permGroupTime", "uuid", "targetGroup", "addedForTime");
        permGroupTime = table3.build();

        MySQLTable table4 = new MySQLTable();
        table4.prepare("permGroupPrefixSuffix", "targetGroup", "prefix", "suffix", "sortHeight");
        permGroupPrefixSuffix = table4.build();

        MySQLTable table5 = new MySQLTable();
        table5.prepare("permGroupRankTracks", "name", "groupsJson");
        permRankTracks = table5.build();

        MySQLTable table6 = new MySQLTable();
        table6.prepare("permDiscordLink", "uuid", "discordId");
        permDiscordLink = table6.build();

        MySQLTable table7 = new MySQLTable();
        table7.prepare("permGroupInherits", "targetGroup", "inheritedGroup");
        permGroupInherits = table7.build();

        MySQLTable table8 = new MySQLTable();
        table8.prepare("permFallbackGroup", "targetGroup");
        permFallbackGroup = table8.build();

        /***
         * Premium:
         * Adds functionality for rank traces, prefixes, suffixes, rank logs and rank & permission timings
         * Adds discord bot with rank request and mc-link
         * Adds rank priorities, sorting and permission inherits
         * Adds default ranks for players
         */

        AsyncThreadScheduler scheduler = new AsyncThreadScheduler(() -> {
            MySQLRequest request = new MySQLRequest();
            request.prepare(getPermGroupTime().getTableName());
            MySQLResponse response = new MySQLResponse();
            if(!response.isEmpty()) {
                for (HashMap<String, String> stringStringHashMap : response.rawAll()) {
                    if(Long.parseLong(stringStringHashMap.get("addedForTime"))>= System.currentTimeMillis()) {
                        new PermPlayer(stringStringHashMap.get("uuid")).removeGroup(stringStringHashMap.get("group"));
                    }
                }
            }
        });
        scheduler.scheduleAsyncTask(1000L, 1000L*300);

        JSONObject configObj = new JSONObject();
        configObj.put("discordBotToken", "TOKEN");

        JSONFile file = new JSONFile("plugins/ElizonPerms/config.json",
                new JSONValue() {
                    @Override
                    public JSONObject object() {
                        return configObj;
                    }

                    @Override
                    public String objectName() {
                        return "config";
                    }
                });

        if(file.isNew()) {
            System.out.println("Please configure the config.json.");
        }

        discordBotToken = file.get("config").getString("discordBotToken");

    }

    public static String getDiscordBotToken() {
        return discordBotToken;
    }

    public static MySQLTable.fin getPermDiscordLink() {
        return permDiscordLink;
    }

    public static MySQLTable.fin getPermFallbackGroup() {
        return permFallbackGroup;
    }

    public static MySQLTable.fin getPermGroupInherits() {
        return permGroupInherits;
    }

    public static MySQLTable.fin getPermGroupPrefixSuffix() {
        return permGroupPrefixSuffix;
    }

    public static MySQLTable.fin getPermGroupTime() {
        return permGroupTime;
    }

    public static MySQLTable.fin getPermRankTracks() {
        return permRankTracks;
    }

    public static MySQLTable.fin getPlayerRankTraces() {
        return playerRankTraces;
    }

    public static MySQLTable.fin getGroupTable() {
        return groupTable;
    }

    public static MySQLTable.fin getPlayerTable() {
        return playerTable;
    }

    public MySQLDriver getDriver() {
        return driver;
    }

}
