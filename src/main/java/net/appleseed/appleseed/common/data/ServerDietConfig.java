package net.appleseed.appleseed.common.data;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.data.group.DietGroup;
import net.appleseed.appleseed.network.SyncDietConfigPacket;

import java.util.*;

public class ServerDietConfig {

    private static final List<IDietGroup> serverGroups = new ArrayList<>();
    private static final Map<String, Map<String, Float>> serverFoodData = new HashMap<>();
    private static boolean connectedToDedicatedServer = false;

    public static void setServerGroups(List<SyncDietConfigPacket.GroupData> groupsData) {
        serverGroups.clear();
        for (SyncDietConfigPacket.GroupData data : groupsData) {
            DietGroup group = DietGroup.fromSyncData(data.toMap());
            if (group != null) {
                serverGroups.add(group);
            }
        }
        connectedToDedicatedServer = true;
    }

    public static void setServerFoodData(Map<String, Map<String, Float>> data) {
        serverFoodData.clear();
        serverFoodData.putAll(data);
    }

    public static List<IDietGroup> getServerGroups() {
        return serverGroups;
    }

    public static Map<String, Float> getServerFoodNutrition(String foodId) {
        return serverFoodData.getOrDefault(foodId, Collections.emptyMap());
    }

    public static boolean isConnectedToDedicatedServer() {
        return connectedToDedicatedServer && !serverGroups.isEmpty();
    }

    public static void clear() {
        serverGroups.clear();
        serverFoodData.clear();
        connectedToDedicatedServer = false;
    }
}
