package com.pinkyudeer.wthaigd.task.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.entity.record.TeamMember;
import com.pinkyudeer.wthaigd.task.service.TeamService;

public class LocalTeamProvider implements TeamProvider {

    private final Map<Integer, UUID> teamAliases = new HashMap<>();

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPartyForPlayer(UUID playerId) {
        List<Team> teams = TeamService.getVisibleTeams(playerId);
        return teams.isEmpty() ? NO_PARTY : aliasFor(teams.get(0)
            .getId());
    }

    @Override
    public List<UUID> getMembers(int partyId) {
        UUID teamId = teamAliases.get(partyId);
        if (teamId == null) return new ArrayList<>();
        List<UUID> members = new ArrayList<>();
        for (TeamMember member : TeamService.getMembers(teamId)) {
            if (member.getStatus() == TeamMember.MemberStatus.ACTIVE) {
                members.add(member.getPlayerId());
            }
        }
        return members;
    }

    @Override
    public Team.TeamRole getRole(int partyId, UUID playerId) {
        UUID teamId = teamAliases.get(partyId);
        TeamMember member = teamId == null ? null : TeamService.getMember(teamId, playerId);
        return member == null ? null : member.getRole();
    }

    @Override
    public String getName(int partyId) {
        UUID teamId = teamAliases.get(partyId);
        Team team = teamId == null ? null : TeamService.getTeam(teamId);
        return team == null ? "" : team.getName();
    }

    @Override
    public boolean isOwner(int partyId, UUID playerId) {
        UUID teamId = teamAliases.get(partyId);
        Team team = teamId == null ? null : TeamService.getTeam(teamId);
        return team != null && playerId.equals(team.getOwnerId());
    }

    private synchronized int aliasFor(UUID teamId) {
        int alias = teamId.hashCode();
        while (teamAliases.containsKey(alias) && !teamId.equals(teamAliases.get(alias))) {
            alias++;
        }
        teamAliases.put(alias, teamId);
        return alias;
    }
}
