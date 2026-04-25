package com.pinkyudeer.wthaigd.task.team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.task.entity.Team;

public interface TeamProvider {

    int NO_PARTY = -1;
    String NO_TEAM_KEY = "";

    boolean isAvailable();

    int getPartyForPlayer(UUID playerId);

    List<UUID> getMembers(int partyId);

    Team.TeamRole getRole(int partyId, UUID playerId);

    String getName(int partyId);

    default boolean isOwner(int partyId, UUID playerId) {
        return false;
    }

    default String getTeamKeyForPlayer(UUID playerId) {
        int partyId = getPartyForPlayer(playerId);
        return partyId == NO_PARTY ? NO_TEAM_KEY : Integer.toString(partyId);
    }

    default List<UUID> getMembers(String teamKey) {
        int partyId = parsePartyId(teamKey);
        return partyId == NO_PARTY ? new ArrayList<>() : getMembers(partyId);
    }

    default Team.TeamRole getRole(String teamKey, UUID playerId) {
        int partyId = parsePartyId(teamKey);
        return partyId == NO_PARTY ? null : getRole(partyId, playerId);
    }

    default String getName(String teamKey) {
        int partyId = parsePartyId(teamKey);
        return partyId == NO_PARTY ? "" : getName(partyId);
    }

    default boolean isOwner(String teamKey, UUID playerId) {
        int partyId = parsePartyId(teamKey);
        return partyId != NO_PARTY && isOwner(partyId, playerId);
    }

    default boolean hasTeam(String teamKey) {
        return !getMembers(teamKey).isEmpty();
    }

    static int parsePartyId(String teamKey) {
        if (teamKey == null || teamKey.trim()
            .isEmpty()) return NO_PARTY;
        try {
            return Integer.parseInt(teamKey.trim());
        } catch (NumberFormatException e) {
            return NO_PARTY;
        }
    }
}
