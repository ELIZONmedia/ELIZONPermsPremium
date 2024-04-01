package app.elizon.perms.pkg.group;

import app.elizon.perms.pkg.Initializer;
import app.elizon.perms.pkg.exception.IllegalMultiStateException;
import app.elizon.perms.pkg.group.trace.GroupTrace;
import app.elizon.perms.pkg.group.track.PermGroupTrack;
import app.elizon.perms.pkg.player.PermPlayer;
import app.elizon.perms.pkg.util.MultiState;
import co.plocki.mysql.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class PermGroup {

    private String name;

    public PermGroup(String name) {
        if(name != null) {
            name = name.toLowerCase();
        }

        this.name = name;
    }

    public void setAsDefaultGroup() {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermFallbackGroup().getTableName());
        MySQLResponse response = request.execute();
        if(!response.isEmpty()) {
            MySQLPush push = new MySQLPush();
            push.prepare(Initializer.getPermFallbackGroup().getTableName(), "targetGroup", name);
            push.execute();
        } else {
            MySQLInsert insert = new MySQLInsert();
            insert.prepare(Initializer.getPermFallbackGroup(), name);
            insert.execute();
        }
    }

    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getGroupTable().getTableName());

        MySQLResponse response = request.execute();

        if(!response.isEmpty()) {
            for (HashMap<String, String> stringStringHashMap : response.rawAll()) {
                if(stringStringHashMap.get("name") == null) {
                    groups.add("no groups available");
                    break;
                }
                groups.add(stringStringHashMap.get("name"));
            }
        }
        return groups;
    }

    public boolean build() {
        // Check if the group already exists
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        MySQLResponse response = request.execute();

        if (response.isEmpty()) {
            // Group does not exist, proceed with creating it
            MySQLInsert insert = new MySQLInsert();
            insert.prepare(Initializer.getGroupTable(), name, "{\"permissions\": {}}");
            insert.execute();
            return true;
        } else {
            return false;
        }
    }

    public void setGroupPrefixSuffix(@Nullable String prefix, @Nullable String suffix, int sortHeight) {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermGroupPrefixSuffix().getTableName());
        request.addRequirement("targetGroup", name);
        MySQLResponse response = request.execute();

        if(response.isEmpty()) {
            MySQLInsert insert = new MySQLInsert();
            insert.prepare(Initializer.getPermGroupPrefixSuffix(), name, prefix, suffix, sortHeight);
            insert.execute();
        } else {
            MySQLPush push = new MySQLPush();
            push.prepare(Initializer.getPermGroupPrefixSuffix().getTableName(), "prefix", prefix);
            push.addRequirement("targetGroup", name);
            push.execute();
            push = new MySQLPush();
            push.prepare(Initializer.getPermGroupPrefixSuffix().getTableName(), "suffix", suffix);
            push.addRequirement("targetGroup", name);
            push.execute();
            push = new MySQLPush();
            push.prepare(Initializer.getPermGroupPrefixSuffix().getTableName(), "sortHeight", sortHeight);
            push.addRequirement("targetGroup", name);
            push.execute();
        }
    }

    public String getPrefix() {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermGroupPrefixSuffix().getTableName());
        request.addRequirement("targetGroup", name);
        MySQLResponse response = request.execute();

        if(!response.isEmpty()) {
            return response.getString("prefix");
        }
        return null;
    }

    public String getSuffix() {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermGroupPrefixSuffix().getTableName());
        request.addRequirement("targetGroup", name);
        MySQLResponse response = request.execute();

        if(!response.isEmpty()) {
            return response.getString("suffix");
        }
        return null;
    }

    public int getSortHeight() {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermGroupPrefixSuffix().getTableName());
        request.addRequirement("targetGroup", name);
        MySQLResponse response = request.execute();

        if(!response.isEmpty()) {
            return response.getInt("sortHeight");
        }
        return -1;
    }

    public void deleteGroup() {

        for (String s : getPlayersInGroup()) {
            PermPlayer player = new PermPlayer(s);
            player.removeGroup(name);
        }

        MySQLDelete delete = new MySQLDelete();
        delete.prepare(Initializer.getGroupTable().getTableName());
        delete.addRequirement("name", name);
        delete.execute();
    }

    public List<String> getPlayersInGroup() {
        List<String> players = new ArrayList<>();
        MySQLRequest request1 = new MySQLRequest();
        request1.prepare(Initializer.getPlayerTable().getTableName());
        for (HashMap<String, String> stringStringHashMap : request1.execute().rawAll()) {
            String uuid = stringStringHashMap.get("uuid");
            JSONObject groupsJson = new JSONObject(stringStringHashMap.get("groupsJson"));
            JSONArray groupsArray = groupsJson.getJSONArray("groups");

            for (int i = 0; i < groupsArray.length(); i++) {
                String groupName = groupsArray.getString(i);
                if (groupName.equals(name)) {
                    players.add(uuid);
                    break;
                }
            }
        }
        return players;
    }

    public Map<String, Boolean> getAllSimpleSetPermissions() {
        MySQLRequest request = new MySQLRequest();
        request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
        JSONObject permsObject = obj.getJSONObject("permissions");

        Map<String, Boolean> permissions = new HashMap<>();
        for (String permission : permsObject.keySet()) {
            JSONObject permObj = permsObject.getJSONObject(permission);
            MultiState state = MultiState.valueOf(permObj.getString("state"));
            if (state == MultiState.DATA) {
                JSONObject data = permObj.getJSONObject("data");
                if (data.has("simple")) {
                    permissions.put(permission, Boolean.parseBoolean(data.getString("simple")));
                }
            } else {
                permissions.put(permission, Boolean.parseBoolean(state.name()));
            }
        }

        return permissions;
    }

    public Map<String, JSONObject> getAllDataSetPermissions() {
        MySQLRequest request = new MySQLRequest();
        request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
        JSONObject permsObject = obj.getJSONObject("permissions");

        Map<String, JSONObject> permissions = new HashMap<>();
        for (String permission : permsObject.keySet()) {
            JSONObject permObj = permsObject.getJSONObject(permission);
            MultiState state = MultiState.valueOf(permObj.getString("state"));
            if (state == MultiState.DATA) {
                permissions.put(permission, permObj.getJSONObject("data"));
            }
        }

        return permissions;
    }

    public PermGroup cloneGroup(String nameOfClone, boolean addAlreadyContainingPlayers) {
        nameOfClone = nameOfClone.toLowerCase();
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        MySQLResponse response = request.execute();

        String permissionsJson = response.getString("permissionsJson");

        MySQLInsert insert = new MySQLInsert();

        insert.prepare(Initializer.getGroupTable(), List.of(nameOfClone, permissionsJson));
        insert.execute();

        if(addAlreadyContainingPlayers) {
            MySQLRequest request1 = new MySQLRequest();
            request1.prepare(Initializer.getPlayerTable().getTableName());
            for (HashMap<String, String> stringStringHashMap : request1.execute().rawAll()) {
                String uuid = stringStringHashMap.get("uuid");
                JSONObject groups = new JSONObject(stringStringHashMap.get("groupsJson"));

                String finalNameOfClone = nameOfClone;
                groups.getJSONArray("groups").forEach(group -> {
                    if(group.equals(name)) {
                        groups.getJSONArray("groups").put(finalNameOfClone);
                    }
                });

                MySQLPush push = new MySQLPush();
                push.prepare(Initializer.getPlayerTable().getTableName(), "groupsJson", groups.toString());
                push.addRequirement("uuid", uuid);
                push.execute();

                MySQLInsert insert2 = new MySQLInsert();
                insert2.prepare(Initializer.getPlayerRankTraces(), uuid, name, nameOfClone.toLowerCase(), "false", "true", System.currentTimeMillis());
                insert2.execute();
            }
        }

        return new PermGroup(nameOfClone);
    }

    public void addGroupInherit(String groupToInherit) {
        MySQLInsert insert = new MySQLInsert();
        insert.prepare(Initializer.getPermGroupInherits(), name, groupToInherit.toLowerCase());
        insert.execute();
    }

    public void removeGroupInherit(String groupBeingInherited) {
        MySQLDelete delete = new MySQLDelete();
        delete.prepare(Initializer.getPermGroupInherits().getTableName());
        delete.addRequirement("targetGroup", name);
        delete.addRequirement("inheritedGroup", groupBeingInherited.toLowerCase());
        delete.execute();
    }

    public List<String> getInheritedGroups() {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermGroupInherits().getTableName());
        request.addRequirement("targetGroup", name);
        MySQLResponse response = request.execute();

        List<String> groups = new ArrayList<>();
        if(!response.isEmpty()) {
            for (HashMap<String, String> stringStringHashMap : response.rawAll()) {
                groups.add(stringStringHashMap.get("inheritedGroup"));
            }
        }
        if(!groups.isEmpty()) {
            groups.sort(Comparator.comparingInt(value -> new PermGroup(value).getSortHeight()));
        }
        return groups;
    }

    public PermGroup renameGroup(String newName) {
        MySQLPush push = new MySQLPush();
        push.prepare(Initializer.getGroupTable().getTableName(), "name", newName.toLowerCase());
        push.addRequirement("name", name);

        push.execute();

        List<String> players = getPlayersInGroup();
        for (String player : players) {
            PermPlayer permPlayer = new PermPlayer(player);

            HashMap<String, Long> times = permPlayer.getCurrentGroupTimeStamps();
            long time = (times == null ? -1 : (!times.containsKey(name) ? -1 : times.get(name)-System.currentTimeMillis()));

            permPlayer.removeGroup(name);
            permPlayer.addGroup(newName.toLowerCase(), time);

            MySQLInsert insert = new MySQLInsert();
            insert.prepare(Initializer.getPlayerRankTraces(), player, name, newName.toLowerCase(), "false", "false", System.currentTimeMillis());
            insert.execute();
        }

        push = new MySQLPush();
        push.prepare(Initializer.getPermGroupInherits().getTableName(), "targetGroup", newName.toLowerCase());
        push.addRequirement("targetGroup", name);
        push.execute();

        push = new MySQLPush();
        push.prepare(Initializer.getPermGroupInherits().getTableName(), "inheritedGroup", newName.toLowerCase());
        push.addRequirement("inheritedGroup", name);
        push.execute();


        push = new MySQLPush();
        push.prepare(Initializer.getPermGroupPrefixSuffix().getTableName(), "targetGroup", newName.toLowerCase());
        push.addRequirement("targetGroup", name);
        push.execute();


        push = new MySQLPush();
        push.prepare(Initializer.getPermGroupTime().getTableName(), "targetGroup", newName.toLowerCase());
        push.addRequirement("targetGroup", name);
        push.execute();

        push = new MySQLPush();
        push.prepare(Initializer.getPlayerRankTraces().getTableName(), "oldGroup", newName.toLowerCase());
        push.addRequirement("oldGroup", name);
        push.execute();

        push = new MySQLPush();
        push.prepare(Initializer.getPlayerRankTraces().getTableName(), "newGroup", newName.toLowerCase());
        push.addRequirement("newGroup", name);
        push.execute();

        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermFallbackGroup().getTableName());
        if(request.execute().getString("targetGroup").equalsIgnoreCase(name)) {
            push = new MySQLPush();
            push.prepare(Initializer.getPermFallbackGroup().getTableName(), "targetGroup", newName.toLowerCase());
            push.execute();
        }

        for (String allTrack : new PermGroupTrack().getAllTracks()) {
            new PermGroupTrack().renameGroupInTrack(allTrack, name, newName.toLowerCase());
        }

        name = newName.toLowerCase();
        return this;
    }

    public void setMultiStatePermission(String permission, @Nullable MultiState state) {
        if(state == MultiState.DATA) {
            new IllegalMultiStateException("Trying to set DATA Permission in simple state").printStackTrace();
            return;
        } else if(state == null) {
            //remove permission
            MySQLRequest request = new MySQLRequest();
            request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
            request.addRequirement("name", name);
            JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
            JSONObject object = obj.getJSONObject("permissions");
            object.remove(permission.toLowerCase());

            obj.put("permissions", object);

            MySQLPush push = new MySQLPush();
            push.prepare(Initializer.getGroupTable().getTableName(), "permissionsJson", obj.toString());
            push.addRequirement("name", name);
            push.execute();
        }

        MySQLRequest request = new MySQLRequest();
        request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
        JSONObject object = obj.getJSONObject("permissions");

        JSONObject permObj = new JSONObject();
        assert state != null;
        permObj.put("state", state.name());

        object.put(permission.toLowerCase(), permObj);

        obj.put("permissions", object);

        MySQLPush push = new MySQLPush();
        push.prepare(Initializer.getGroupTable().getTableName(), "permissionsJson", obj.toString());
        push.addRequirement("name", name);
        push.execute();
    }

    public void setMultiStatePermission(String permission, @NotNull MultiState state, JSONObject dataObj) {
        MySQLRequest request = new MySQLRequest();
        request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
        JSONObject object = obj.getJSONObject("permissions");

        JSONObject permObj = new JSONObject();
        permObj.put("state", state.name());
        permObj.put("data", dataObj);

        object.put(permission.toLowerCase(), permObj);

        obj.put("permissions", object);

        MySQLPush push = new MySQLPush();
        push.prepare(Initializer.getGroupTable().getTableName(), "permissionsJson", obj.toString());
        push.addRequirement("name", name);
        push.execute();
    }

    public boolean simpleHasPermission(String permission) {

        permission = permission.toLowerCase();
        // Check if the permission string itself is *
        if(getAllSimpleSetPermissions().containsKey("*")) {
            if(getAllSimpleSetPermissions().get("*")) {
                return true;
            }
        }

        if(permission.contains(".")) {
            // Split the permission string at each dot (.)
            String[] sections = permission.split("\\.");

            // Iterate over the sections to build wildcard patterns
            StringBuilder wildcardBuilder = new StringBuilder();
            for (String section : sections) {
                wildcardBuilder.append(section).append(".");
                String wildcardPermission = wildcardBuilder + "*";

                // Check if the wildcard permission matches any permission in the database
                for (String perm : getAllSimpleSetPermissions().keySet()) {
                    if (perm.startsWith(wildcardPermission)) {
                        return getAllSimpleSetPermissions().get(perm);
                    }
                }
            }
        }

        // Check if the permission is partially in the database
        for (String perm : getAllSimpleSetPermissions().keySet()) {
            // If the permission is found in the database
            if (perm.equals(permission)) {
                return getAllSimpleSetPermissions().get(permission);
            }
        }

        for (String inheritedGroup : getInheritedGroups()) {
            PermGroup group = new PermGroup(inheritedGroup);
            if(group.getAllSimpleSetPermissions().containsKey(permission)) {
                return group.simpleHasPermission(permission);
            }
        }

        return false;
    }


    public @Nullable JSONObject getPermissionData(String permission) {
        MySQLRequest request = new MySQLRequest();
        request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
        JSONObject object = obj.getJSONObject("permissions");

        MultiState result = MultiState.valueOf(object.getJSONObject(permission).getString("state"));

        if(result == MultiState.DATA) {
            return object.getJSONObject(permission).getJSONObject("data");
        }
        return null;
    }

    public boolean permissionHasData(String permission) {
        MySQLRequest request = new MySQLRequest();
        request.prepare("permissionsJson", Initializer.getGroupTable().getTableName());
        request.addRequirement("name", name);
        JSONObject obj = new JSONObject((String) request.execute().get("permissionsJson"));
        JSONObject object = obj.getJSONObject("permissions");

        MultiState result = MultiState.valueOf(object.getJSONObject(permission).getString("state"));

        return result == MultiState.DATA;
    }

}
