package app.elizon.perms.pkg.group.track;

import app.elizon.perms.pkg.Initializer;
import co.plocki.mysql.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PermGroupTrack {

    public void createTrack(String name, List<String> groups) {
        name = name.toLowerCase();

        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermRankTracks().getTableName());
        request.addRequirement("name", name);
        MySQLResponse response = request.execute();

        if (response.isEmpty()) {
            JSONObject json = new JSONObject();

            // Add groups to the JSON object
            JSONArray jsonArray = new JSONArray(Collections.singleton(groups));
            json.put("groups", jsonArray);

            MySQLInsert insert = new MySQLInsert();
            insert.prepare(Initializer.getPermRankTracks(), name, json.toString());
            insert.execute(); // Assuming execute() method exists

        }

    }

    public List<String> getAllTracks() {
        List<String> tracks = new ArrayList<>();

        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermRankTracks().getTableName());
        MySQLResponse response = request.execute();

        if(!response.isEmpty()) {
            for (HashMap<String, String> stringStringHashMap : response.rawAll()) {
                String trackName = stringStringHashMap.get("name");
                tracks.add(trackName);
            }
        } else {
            tracks.add("no tracks found");
        }

        return tracks;
    }

    private JSONObject getTrackJson(String track) {
        MySQLRequest request = new MySQLRequest();
        request.prepare(Initializer.getPermRankTracks().getTableName());
        request.addRequirement("name", track);
        MySQLResponse response = request.execute();

        if(!response.isEmpty()) {
            return new JSONObject(response.getString("groupsJson"));
        }
        return new JSONObject("{\"groups\":[]}");
    }

    private void updateTrackJson(String name, JSONObject json) {
        MySQLPush push = new MySQLPush();
        push.prepare(Initializer.getPermRankTracks().getTableName(), "groupsJson", json.toString());
        push.addRequirement("name", name);
        push.execute();
    }

    public void addGroupToTrack(String trackName, String groupName) {
        JSONObject json = getTrackJson(trackName); // Implement this method to retrieve track JSON
        JSONArray groupsArray = json.getJSONArray("groups");
        groupsArray.put(groupName);
        updateTrackJson(trackName, json); // Implement this method to update track JSON
    }

    public void removeGroupFromTrack(String trackName, String groupName) {
        JSONObject json = getTrackJson(trackName);
        JSONArray groupsArray = json.getJSONArray("groups");

        // Find the index of the group to remove
        int indexToRemove = -1;
        for (int i = 0; i < groupsArray.length(); i++) {
            if (groupsArray.getString(i).equals(groupName)) {
                indexToRemove = i;
                break;
            }
        }

        // If the group is found, remove it from the JSON array
        if (indexToRemove != -1) {
            groupsArray.remove(indexToRemove);

            // Update the groups in the JSON object and database
            json.put("groups", groupsArray);
            updateTrackJson(trackName, json);
        }
    }

    public void deleteTrack(String trackName) {
        MySQLDelete delete = new MySQLDelete();
        delete.prepare(Initializer.getPermRankTracks().getTableName());
        delete.addRequirement("name", trackName);
        delete.execute();
    }

    public String getNextGroup(String trackName, String currentGroup) {
        JSONObject json = getTrackJson(trackName);
        JSONArray groupsArray = json.getJSONArray("groups");
        for (int i = 0; i < groupsArray.length(); i++) {
            if (groupsArray.getString(i).equals(currentGroup)) {
                if (i + 1 < groupsArray.length()) {
                    return groupsArray.getString(i + 1);
                } else {
                    return null; // No next group
                }
            }
        }
        return null; // Current group not found
    }

    public List<String> getGroups(String trackName) {
        JSONObject json = getTrackJson(trackName);
        JSONArray groupsArray = json.getJSONArray("groups");
        List<String> groups = new ArrayList<>();
        if(!groupsArray.isEmpty()) {
            for (int i = 0; i < groupsArray.length(); i++) {
                groups.add(groupsArray.getString(i));
            }
        } else {
            return null;
        }
        return groups;
    }

    public void insertGroupBeforeNext(String trackName, String groupToInsert, String nextGroup) {
        JSONObject json = getTrackJson(trackName);
        JSONArray groupsArray = json.getJSONArray("groups");
        int insertIndex = -1;

        // Find the index of the next group
        for (int i = 0; i < groupsArray.length(); i++) {
            if (groupsArray.getString(i).equals(nextGroup)) {
                insertIndex = i;
                break;
            }
        }

        // If the next group is found, insert the new group before it
        if (insertIndex != -1) {
            List<String> updatedGroups = new ArrayList<>();
            for (int i = 0; i < groupsArray.length(); i++) {
                if (i == insertIndex) {
                    updatedGroups.add(groupToInsert);
                }
                updatedGroups.add(groupsArray.getString(i));
            }

            // Update the groups in the JSON object and database
            json.put("groups", new JSONArray(updatedGroups));
            updateTrackJson(trackName, json);
        } else {
            // If the next group is not found, simply add the new group to the end
            addGroupToTrack(trackName, groupToInsert);
        }
    }

    public void addGroupToEnd(String trackName, String groupToAdd) {
        JSONObject json = getTrackJson(trackName);
        JSONArray groupsArray = json.getJSONArray("groups");

        List<String> updatedGroups = new ArrayList<>();
        for (int i = 0; i < groupsArray.length(); i++) {
            updatedGroups.add(groupsArray.getString(i));
        }
        updatedGroups.add(groupToAdd);

        // Update the groups in the JSON object and database
        json.put("groups", new JSONArray(updatedGroups));
        updateTrackJson(trackName, json);
    }

    public PermGroupTrack renameGroupInTrack(String trackName, String oldGroupName, String newGroupName) {
        // Get the JSON object of the track
        JSONObject trackJson = getTrackJson(trackName);

        // Get the array of groups from the track JSON
        JSONArray groupsArray = trackJson.getJSONArray("groups");

        // Iterate through the groups array to find and replace the old group name with the new one
        for (int i = 0; i < groupsArray.length(); i++) {
            if (groupsArray.getString(i).equals(oldGroupName)) {
                groupsArray.put(i, newGroupName);
                break;
            }
        }

        // Update the track JSON with the modified groups array
        trackJson.put("groups", groupsArray);

        // Update the track in the database
        updateTrackJson(trackName, trackJson);

        return this;
    }
}
