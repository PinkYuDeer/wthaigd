package com.pinkyudeer.wthaigd.integration.gtnhlib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.team.TeamProvider;

import cpw.mods.fml.common.Loader;

public class GtnhLibTeamProvider implements TeamProvider {

    private static final String TEAM_MANAGER = "com.gtnewhorizon.gtnhlib.teams.TeamManager";

    @Override
    public boolean isAvailable() {
        if (!isGtnhLibLoaded()) return false;
        try {
            Class.forName(TEAM_MANAGER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public int getPartyForPlayer(UUID playerId) {
        return NO_PARTY;
    }

    @Override
    public String getTeamKeyForPlayer(UUID playerId) {
        Object team = getTeamByPlayer(playerId);
        String name = readName(team);
        return name == null ? NO_TEAM_KEY : name;
    }

    @Override
    public List<UUID> getMembers(int partyId) {
        return new ArrayList<>();
    }

    @Override
    public List<UUID> getMembers(String teamKey) {
        Object team = getTeam(teamKey);
        if (team == null) return new ArrayList<>();
        return readUuidCollection(invoke(team, "getMembers"));
    }

    @Override
    public Team.TeamRole getRole(int partyId, UUID playerId) {
        return null;
    }

    @Override
    public Team.TeamRole getRole(String teamKey, UUID playerId) {
        Object team = getTeam(teamKey);
        if (team == null || playerId == null) return null;
        if (invokeBoolean(team, "isOwner", playerId) || invokeBoolean(team, "isOfficer", playerId)) {
            return Team.TeamRole.ADMIN;
        }
        if (invokeBoolean(team, "isMember", playerId)) return Team.TeamRole.MEMBER;
        return null;
    }

    @Override
    public String getName(int partyId) {
        return "";
    }

    @Override
    public String getName(String teamKey) {
        Object team = getTeam(teamKey);
        String name = readName(team);
        return name == null ? "" : name;
    }

    @Override
    public boolean isOwner(int partyId, UUID playerId) {
        return false;
    }

    @Override
    public boolean isOwner(String teamKey, UUID playerId) {
        Object team = getTeam(teamKey);
        return team != null && playerId != null && invokeBoolean(team, "isOwner", playerId);
    }

    @Override
    public boolean hasTeam(String teamKey) {
        return getTeam(teamKey) != null;
    }

    private Object getTeamByPlayer(UUID playerId) {
        if (!isAvailable() || playerId == null) return null;
        try {
            return Class.forName(TEAM_MANAGER)
                .getMethod("getTeamByPlayer", UUID.class)
                .invoke(null, playerId);
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read GTNHLib team for player: {}", playerId, e);
            return null;
        }
    }

    private Object getTeam(String teamKey) {
        if (!isAvailable() || teamKey == null
            || teamKey.trim()
                .isEmpty())
            return null;
        try {
            Class<?> manager = Class.forName(TEAM_MANAGER);
            Method method = findMethod(manager, "getTeamByName", String.class);
            if (method == null) method = findMethod(manager, "getTeam", String.class);
            if (method == null) return null;
            return method.invoke(null, teamKey.trim());
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to read GTNHLib team: {}", teamKey, e);
            return null;
        }
    }

    private Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
        try {
            return owner.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object invoke(Object target, String methodName) {
        if (target == null) return null;
        try {
            return target.getClass()
                .getMethod(methodName)
                .invoke(target);
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to invoke GTNHLib team method: {}", methodName, e);
            return null;
        }
    }

    private boolean invokeBoolean(Object target, String methodName, UUID playerId) {
        if (target == null || playerId == null) return false;
        try {
            Object value = target.getClass()
                .getMethod(methodName, UUID.class)
                .invoke(target, playerId);
            return Boolean.TRUE.equals(value);
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to invoke GTNHLib team method: {}", methodName, e);
            return false;
        }
    }

    private String readName(Object team) {
        Object name = invoke(team, "getTeamName");
        return name == null ? null : name.toString();
    }

    private List<UUID> readUuidCollection(Object value) {
        List<UUID> uuids = new ArrayList<>();
        if (!(value instanceof Collection<?>)) return uuids;
        for (Object entry : (Collection<?>) value) {
            if (entry instanceof UUID) uuids.add((UUID) entry);
        }
        return uuids;
    }

    private boolean isGtnhLibLoaded() {
        return Loader.isModLoaded("gtnhlib") || Loader.isModLoaded("GTNHLib");
    }
}
