package com.pinkyudeer.wthaigd.integration.betterquesting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.team.TeamProvider;

import cpw.mods.fml.common.Loader;

public class BetterQuestingTeamProvider implements TeamProvider {

    @Override
    public boolean isAvailable() {
        return Loader.isModLoaded("betterquesting") && getPartyDatabase() != null;
    }

    @Override
    public int getPartyForPlayer(UUID playerId) {
        Object database = getPartyDatabase();
        if (database == null) return NO_PARTY;
        try {
            Object entry = database.getClass()
                .getMethod("getParty", UUID.class)
                .invoke(database, playerId);
            if (entry == null) return NO_PARTY;
            Object id = entry.getClass()
                .getMethod("getID")
                .invoke(entry);
            return ((Number) id).intValue();
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read BetterQuesting party for {}", playerId, e);
            return NO_PARTY;
        }
    }

    @Override
    public List<UUID> getMembers(int partyId) {
        Object party = getParty(partyId);
        if (party == null) return new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<UUID> members = (List<UUID>) party.getClass()
                .getMethod("getMembers")
                .invoke(party);
            return new ArrayList<>(members);
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read BetterQuesting party members: {}", partyId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Team.TeamRole getRole(int partyId, UUID playerId) {
        String status = getStatusName(partyId, playerId);
        if ("OWNER".equals(status) || "ADMIN".equals(status)) return Team.TeamRole.ADMIN;
        if ("MEMBER".equals(status)) return Team.TeamRole.MEMBER;
        return null;
    }

    @Override
    public String getName(int partyId) {
        Object party = getParty(partyId);
        if (party == null) return "";
        try {
            Object properties = party.getClass()
                .getMethod("getProperties")
                .invoke(party);
            Object nameProp = Class.forName("betterquesting.api.properties.NativeProps")
                .getField("NAME")
                .get(null);
            Class<?> propertyType = Class.forName("betterquesting.api.properties.IPropertyType");
            Object name = properties.getClass()
                .getMethod("getProperty", propertyType)
                .invoke(properties, nameProp);
            return name == null ? "" : name.toString();
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read BetterQuesting party name: {}", partyId, e);
            return "";
        }
    }

    @Override
    public boolean isOwner(int partyId, UUID playerId) {
        return "OWNER".equals(getStatusName(partyId, playerId));
    }

    private String getStatusName(int partyId, UUID playerId) {
        Object party = getParty(partyId);
        if (party == null) return null;
        try {
            Object status = party.getClass()
                .getMethod("getStatus", UUID.class)
                .invoke(party, playerId);
            return status == null ? null : status.toString();
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read BetterQuesting party role: {} {}", partyId, playerId, e);
            return null;
        }
    }

    private Object getParty(int partyId) {
        Object database = getPartyDatabase();
        if (database == null || partyId < 0) return null;
        try {
            return database.getClass()
                .getMethod("getValue", int.class)
                .invoke(database, partyId);
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read BetterQuesting party: {}", partyId, e);
            return null;
        }
    }

    private Object getPartyDatabase() {
        if (!Loader.isModLoaded("betterquesting")) return null;
        try {
            Class<?> apiKeyClass = Class.forName("betterquesting.api.api.ApiKey");
            Object partyDbKey = Class.forName("betterquesting.api.api.ApiReference")
                .getField("PARTY_DB")
                .get(null);
            Method getApi = Class.forName("betterquesting.api.api.QuestingAPI")
                .getMethod("getAPI", apiKeyClass);
            return getApi.invoke(null, partyDbKey);
        } catch (Exception e) {
            return null;
        }
    }
}
