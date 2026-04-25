package com.pinkyudeer.wthaigd.task.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.db.EntityHandler;
import com.pinkyudeer.wthaigd.db.SQLHelper;
import com.pinkyudeer.wthaigd.db.SQLiteManager;
import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.task.dao.TeamDao;
import com.pinkyudeer.wthaigd.task.dao.record.TeamMemberDao;
import com.pinkyudeer.wthaigd.task.dao.record.TeamRequestDao;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.entity.record.Notification;
import com.pinkyudeer.wthaigd.task.entity.record.TeamMember;
import com.pinkyudeer.wthaigd.task.entity.record.TeamRequest;
import com.pinkyudeer.wthaigd.task.team.TeamProvider;
import com.pinkyudeer.wthaigd.task.team.TeamProviders;

public final class TeamService {

    private TeamService() {}

    public static Team createLocalTeam(String name, UUID ownerId, String description) {
        if (isBlank(name)) throw new IllegalArgumentException("团队名称不能为空");
        return SQLiteManager.transaction(() -> {
            Team team = new Team(name.trim(), ownerId, description);
            team.setSyncSource(Team.SyncSource.LOCAL);
            team.setSyncStatus(Team.SyncStatus.ACTIVE);
            team.setTotalMembers(1);
            Integer result = TeamDao.insert(team);
            if (result == null || result <= 0) return null;

            TeamMember owner = new TeamMember(team.getId(), ownerId, ownerId);
            owner.setRole(Team.TeamRole.ADMIN);
            TeamMemberDao.insert(owner);
            return team;
        });
    }

    public static TeamRequest invitePlayer(UUID teamId, UUID targetPlayerId, UUID operatorId) {
        Team team = requireTeam(teamId);
        assertLocalEditable(team);
        assertCanManageMembers(team, operatorId, false);
        return SQLiteManager.transaction(() -> {
            TeamRequest request = new TeamRequest(
                TeamRequest.RequestType.INVITE,
                teamId,
                targetPlayerId,
                Notification.SourceType.PLAYER,
                operatorId);
            request.setInviterId(operatorId);
            request.setExpireTime(
                LocalDateTime.now()
                    .plusDays(7));
            TeamRequestDao.insert(request);
            team.setInvitationsCount(safeInt(team.getInvitationsCount()) + 1);
            SQLHelper.updateById(team)
                .execute();
            return request;
        });
    }

    public static TeamRequest requestJoin(UUID teamId, UUID applicantId, String reason) {
        Team team = requireTeam(teamId);
        assertLocalEditable(team);
        return SQLiteManager.transaction(() -> {
            TeamRequest request = new TeamRequest(
                TeamRequest.RequestType.JOIN,
                teamId,
                applicantId,
                Notification.SourceType.PLAYER,
                applicantId);
            request.setReason(reason);
            request.setExpireTime(
                LocalDateTime.now()
                    .plusDays(7));
            TeamRequestDao.insert(request);
            team.setJoinRequestsCount(safeInt(team.getJoinRequestsCount()) + 1);
            SQLHelper.updateById(team)
                .execute();
            return request;
        });
    }

    public static boolean acceptRequest(UUID requestId, UUID operatorId, boolean isOp) {
        TeamRequest request = TeamRequestDao.selectById(requestId);
        if (request == null || request.getStatus() != TeamRequest.RequestStatus.PENDING) return false;
        Team team = requireTeam(request.getTeamId());
        assertLocalEditable(team);

        if (request.getRequestType() == TeamRequest.RequestType.INVITE) {
            if (!isOp && !operatorId.equals(request.getApplicantId()) && !canManageMembers(team, operatorId)) {
                throw new SecurityException("无权接受此邀请");
            }
        } else {
            assertCanManageMembers(team, operatorId, isOp);
        }

        return SQLiteManager.transaction(() -> {
            TeamMember member = getMember(team.getId(), request.getApplicantId());
            if (member == null) {
                member = new TeamMember(team.getId(), request.getApplicantId(), operatorId);
                TeamMemberDao.insert(member);
                team.setTotalMembers(safeInt(team.getTotalMembers()) + 1);
            } else if (member.getStatus() != TeamMember.MemberStatus.ACTIVE) {
                TeamMember oldMember = UtilHelper.deepClone(member, TeamMember.class);
                member.setStatus(TeamMember.MemberStatus.ACTIVE);
                member.setLastOperatorId(operatorId);
                member.setLastOperationTime(LocalDateTime.now());
                TeamMemberDao.updateByIdByCompare(member, oldMember);
                team.setTotalMembers(safeInt(team.getTotalMembers()) + 1);
            }

            if (request.getRequestType() == TeamRequest.RequestType.INVITE) {
                team.setInvitationsCount(Math.max(0, safeInt(team.getInvitationsCount()) - 1));
            } else {
                team.setJoinRequestsCount(Math.max(0, safeInt(team.getJoinRequestsCount()) - 1));
            }

            TeamRequest oldRequest = UtilHelper.deepClone(request, TeamRequest.class);
            request.setStatus(TeamRequest.RequestStatus.APPROVED);
            request.setHandlerId(operatorId);
            request.setHandleTime(LocalDateTime.now());
            TeamRequestDao.updateByIdByCompare(request, oldRequest);
            SQLHelper.updateById(team)
                .execute();
            return true;
        });
    }

    public static boolean kickMember(UUID teamId, UUID targetPlayerId, UUID operatorId, boolean isOp) {
        Team team = requireTeam(teamId);
        assertLocalEditable(team);
        assertCanManageMembers(team, operatorId, isOp);
        if (targetPlayerId.equals(team.getOwnerId())) throw new SecurityException("不能踢出队长，请先转让队长");
        TeamMember target = getMember(teamId, targetPlayerId);
        if (target == null || target.getStatus() != TeamMember.MemberStatus.ACTIVE) return false;
        return SQLiteManager.transaction(() -> {
            TeamMember oldTarget = UtilHelper.deepClone(target, TeamMember.class);
            target.setStatus(TeamMember.MemberStatus.LEFT);
            target.setLastOperatorId(operatorId);
            target.setLastOperationTime(LocalDateTime.now());
            TeamMemberDao.updateByIdByCompare(target, oldTarget);
            updateMemberCount(team);
            return true;
        });
    }

    public static boolean leaveTeam(UUID teamId, UUID actorId) {
        Team team = requireTeam(teamId);
        assertLocalEditable(team);
        TeamMember member = getMember(teamId, actorId);
        if (member == null || member.getStatus() != TeamMember.MemberStatus.ACTIVE) return false;
        return SQLiteManager.transaction(() -> {
            if (actorId.equals(team.getOwnerId())) {
                UUID nextOwner = findNextOwner(teamId, actorId);
                if (nextOwner == null) {
                    Team oldTeam = UtilHelper.deepClone(team, Team.class);
                    team.setDisbandTime(LocalDateTime.now());
                    team.setSyncStatus(Team.SyncStatus.UNLINKED);
                    TeamDao.updateByIdByCompare(team, oldTeam);
                } else {
                    transferOwner(teamId, nextOwner, actorId, true);
                }
            }
            TeamMember oldMember = UtilHelper.deepClone(member, TeamMember.class);
            member.setStatus(TeamMember.MemberStatus.LEFT);
            member.setLastOperatorId(actorId);
            member.setLastOperationTime(LocalDateTime.now());
            TeamMemberDao.updateByIdByCompare(member, oldMember);
            updateMemberCount(team);
            return true;
        });
    }

    public static boolean transferOwner(UUID teamId, UUID targetPlayerId, UUID operatorId, boolean isOp) {
        Team team = requireTeam(teamId);
        assertLocalEditable(team);
        if (!isOp && !operatorId.equals(team.getOwnerId())) throw new SecurityException("只有队长可转让队长");
        TeamMember target = getMember(teamId, targetPlayerId);
        if (target == null || target.getStatus() != TeamMember.MemberStatus.ACTIVE) return false;

        return SQLiteManager.transaction(() -> {
            Team oldTeam = UtilHelper.deepClone(team, Team.class);
            team.setOwnerId(targetPlayerId);
            team.setUpdateTime(LocalDateTime.now());
            TeamDao.updateByIdByCompare(team, oldTeam);

            if (target.getRole() != Team.TeamRole.ADMIN) {
                TeamMember oldTarget = UtilHelper.deepClone(target, TeamMember.class);
                target.setRole(Team.TeamRole.ADMIN);
                target.setLastOperatorId(operatorId);
                target.setLastOperationTime(LocalDateTime.now());
                TeamMemberDao.updateByIdByCompare(target, oldTarget);
            }
            return true;
        });
    }

    public static boolean linkBetterQuestingParty(UUID teamId, int partyId, UUID operatorId, boolean isOp) {
        return linkExternalTeam(
            teamId,
            Team.SyncSource.BETTER_QUESTING,
            TeamProviders.betterQuesting(),
            Integer.toString(partyId),
            partyId,
            operatorId,
            isOp,
            "BetterQuesting");
    }

    public static boolean linkGtnhLibTeam(UUID teamId, String teamKey, UUID operatorId, boolean isOp) {
        return linkExternalTeam(
            teamId,
            Team.SyncSource.GTNH_LIB,
            TeamProviders.gtnhLib(),
            teamKey,
            -1,
            operatorId,
            isOp,
            "GTNHLib");
    }

    public static boolean syncBetterQuestingTeam(UUID teamId, UUID operatorId, boolean isOp) {
        return syncExternalTeam(
            teamId,
            Team.SyncSource.BETTER_QUESTING,
            TeamProviders.betterQuesting(),
            operatorId,
            isOp,
            "BetterQuesting");
    }

    public static boolean syncGtnhLibTeam(UUID teamId, UUID operatorId, boolean isOp) {
        return syncExternalTeam(teamId, Team.SyncSource.GTNH_LIB, TeamProviders.gtnhLib(), operatorId, isOp, "GTNHLib");
    }

    public static boolean unlinkBetterQuestingTeam(UUID teamId, UUID operatorId, boolean isOp) {
        return unlinkExternalTeam(teamId, operatorId, isOp);
    }

    public static boolean unlinkExternalTeam(UUID teamId, UUID operatorId, boolean isOp) {
        Team team = requireTeam(teamId);
        if (!isOp && (operatorId == null || !operatorId.equals(team.getOwnerId())))
            throw new SecurityException("只有队长可解除关联");
        return SQLiteManager.transaction(() -> {
            Team oldTeam = UtilHelper.deepClone(team, Team.class);
            team.setSyncSource(Team.SyncSource.LOCAL);
            team.setExternalPartyId(-1);
            team.setExternalTeamKey(null);
            team.setSyncStatus(Team.SyncStatus.UNLINKED);
            team.setLastSyncTime(LocalDateTime.now());
            team.setUpdateTime(LocalDateTime.now());
            TeamDao.updateByIdByCompare(team, oldTeam);
            return true;
        });
    }

    public static void syncLinkedTeamsForPlayer(UUID playerId, boolean isOp) {
        for (Team team : getAllTeams()) {
            TeamProvider provider = providerFor(team.getSyncSource());
            if (provider == null) continue;
            try {
                String teamKey = externalKey(team);
                if (isOp || isActiveMember(team.getId(), playerId)
                    || !provider.isAvailable()
                    || (!isBlank(teamKey) && provider.getMembers(teamKey)
                        .contains(playerId))) {
                    syncExternalTeam(
                        team.getId(),
                        team.getSyncSource(),
                        provider,
                        playerId,
                        isOp,
                        team.getSyncSource()
                            .name());
                }
            } catch (Exception e) {
                Wthaigd.LOG.warn("External team sync failed: {} {}", team.getSyncSource(), team.getId(), e);
            }
        }
    }

    public static TaskService.PermissionContext contextFor(UUID actorId, UUID teamId, boolean isOp) {
        Team.TeamRole role = null;
        if (teamId != null) {
            Team team = getTeam(teamId);
            TeamProvider provider = team == null ? null : providerFor(team.getSyncSource());
            if (team != null && provider != null) {
                try {
                    syncExternalTeam(
                        teamId,
                        team.getSyncSource(),
                        provider,
                        actorId,
                        isOp,
                        team.getSyncSource()
                            .name());
                } catch (Exception e) {
                    Wthaigd.LOG.warn("External team permission refresh failed: {} {}", team.getSyncSource(), teamId, e);
                }
            }
            TeamMember member = getMember(teamId, actorId);
            if (member != null && member.getStatus() == TeamMember.MemberStatus.ACTIVE) role = member.getRole();
        }
        return new TaskService.PermissionContext(actorId, teamId, role, isOp);
    }

    public static Team getTeam(UUID teamId) {
        return teamId == null ? null : SQLHelper.selectByPremiereKey(Team.class, teamId);
    }

    public static TeamMember getMember(UUID teamId, UUID playerId) {
        if (teamId == null || playerId == null) return null;
        return EntityHandler.handleSingle(
            SQLHelper.select(TeamMember.class)
                .where("team_id", SQLHelper.Operator.EQ, teamId)
                .where("player_id", SQLHelper.Operator.EQ, playerId)
                .limit(1)
                .execute(),
            TeamMember.class);
    }

    public static List<TeamMember> getMembers(UUID teamId) {
        if (teamId == null) return new ArrayList<>();
        return EntityHandler.handleList(
            SQLHelper.select(TeamMember.class)
                .where("team_id", SQLHelper.Operator.EQ, teamId)
                .execute(),
            TeamMember.class);
    }

    public static List<Team> getVisibleTeams(UUID playerId) {
        List<Team> teams = new ArrayList<>();
        for (Team team : getAllTeams()) {
            if (playerId.equals(team.getOwnerId())) {
                teams.add(team);
                continue;
            }
            TeamMember member = getMember(team.getId(), playerId);
            if (member != null && member.getStatus() == TeamMember.MemberStatus.ACTIVE) {
                teams.add(team);
            }
        }
        return teams;
    }

    public static List<Team> getAllTeams() {
        try {
            return TeamDao.selectAll();
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to load teams", e);
            return new ArrayList<>();
        }
    }

    private static boolean linkExternalTeam(UUID teamId, Team.SyncSource syncSource, TeamProvider provider,
        String teamKey, int legacyPartyId, UUID operatorId, boolean isOp, String providerName) {
        Team team = requireTeam(teamId);
        if (!isOp && (operatorId == null || !operatorId.equals(team.getOwnerId())))
            throw new SecurityException("只有队长可关联 " + providerName);

        if (provider == null || !provider.isAvailable()) throw new IllegalStateException(providerName + " 不可用");
        if (isBlank(teamKey) || !provider.hasTeam(teamKey)) throw new IllegalArgumentException(providerName + " 队伍不存在");
        if (!isOp && !provider.isOwner(teamKey, operatorId)) {
            throw new SecurityException("操作者必须是 " + providerName + " 队伍拥有者");
        }

        return SQLiteManager.transaction(() -> {
            Team oldTeam = UtilHelper.deepClone(team, Team.class);
            team.setSyncSource(syncSource);
            team.setExternalPartyId(legacyPartyId);
            team.setExternalTeamKey(teamKey.trim());
            team.setSyncStatus(Team.SyncStatus.ACTIVE);
            team.setLastSyncTime(LocalDateTime.now());
            team.setUpdateTime(LocalDateTime.now());
            TeamDao.updateByIdByCompare(team, oldTeam);
            return syncExternalTeam(teamId, syncSource, provider, operatorId, true, providerName);
        });
    }

    private static boolean syncExternalTeam(UUID teamId, Team.SyncSource syncSource, TeamProvider provider,
        UUID operatorId, boolean isOp, String providerName) {
        Team team = requireTeam(teamId);
        if (team.getSyncSource() != syncSource) return false;

        String teamKey = externalKey(team);
        if (isBlank(teamKey)) return false;

        if (provider == null || !provider.isAvailable()) {
            markStale(team);
            return false;
        }

        List<UUID> providerMembers = provider.getMembers(teamKey);
        if (providerMembers.isEmpty()) {
            markStale(team);
            return false;
        }

        if (!isOp
            && (operatorId == null || (!operatorId.equals(team.getOwnerId()) && !canManageMembers(team, operatorId)
                && !providerMembers.contains(operatorId)))) {
            throw new SecurityException("无权同步 " + providerName + " 队伍");
        }

        Set<UUID> memberSet = new HashSet<>(providerMembers);
        UUID ownerId = null;
        for (UUID memberId : providerMembers) {
            if (provider.isOwner(teamKey, memberId)) {
                ownerId = memberId;
                break;
            }
        }
        if (ownerId == null && memberSet.contains(team.getOwnerId())) ownerId = team.getOwnerId();
        if (ownerId == null) ownerId = providerMembers.get(0);

        UUID finalOwnerId = ownerId;
        return SQLiteManager.transaction(() -> {
            for (UUID memberId : providerMembers) {
                Team.TeamRole role = provider.getRole(teamKey, memberId);
                if (role == null) role = Team.TeamRole.MEMBER;
                upsertMember(teamId, memberId, role, operatorId);
            }

            for (TeamMember member : getMembers(teamId)) {
                if (member.getStatus() == TeamMember.MemberStatus.ACTIVE && !memberSet.contains(member.getPlayerId())) {
                    TeamMember oldMember = UtilHelper.deepClone(member, TeamMember.class);
                    member.setStatus(TeamMember.MemberStatus.LEFT);
                    member.setLastOperatorId(operatorId);
                    member.setLastOperationTime(LocalDateTime.now());
                    TeamMemberDao.updateByIdByCompare(member, oldMember);
                }
            }

            Team oldTeam = UtilHelper.deepClone(team, Team.class);
            team.setOwnerId(finalOwnerId);
            String externalName = provider.getName(teamKey);
            if (!isBlank(externalName)) team.setName(externalName);
            team.setSyncStatus(Team.SyncStatus.ACTIVE);
            team.setLastSyncTime(LocalDateTime.now());
            team.setTotalMembers(providerMembers.size());
            team.setUpdateTime(LocalDateTime.now());
            TeamDao.updateByIdByCompare(team, oldTeam);
            return true;
        });
    }

    private static TeamProvider providerFor(Team.SyncSource syncSource) {
        if (syncSource == Team.SyncSource.BETTER_QUESTING) return TeamProviders.betterQuesting();
        if (syncSource == Team.SyncSource.GTNH_LIB) return TeamProviders.gtnhLib();
        return null;
    }

    private static String externalKey(Team team) {
        if (team == null) return TeamProvider.NO_TEAM_KEY;
        if (!isBlank(team.getExternalTeamKey())) return team.getExternalTeamKey();
        Integer partyId = team.getExternalPartyId();
        return partyId == null || partyId < 0 ? TeamProvider.NO_TEAM_KEY : Integer.toString(partyId);
    }

    private static Team requireTeam(UUID teamId) {
        Team team = getTeam(teamId);
        if (team == null) throw new IllegalArgumentException("团队不存在");
        return team;
    }

    private static void assertLocalEditable(Team team) {
        if (team.getSyncSource() != null && team.getSyncSource() != Team.SyncSource.LOCAL) {
            throw new SecurityException("此团队成员由外部组队同步，不能在本地修改");
        }
    }

    private static void assertCanManageMembers(Team team, UUID operatorId, boolean isOp) {
        if (isOp || canManageMembers(team, operatorId)) return;
        throw new SecurityException("无权管理团队成员");
    }

    private static boolean canManageMembers(Team team, UUID operatorId) {
        if (operatorId == null) return false;
        if (operatorId.equals(team.getOwnerId())) return true;
        TeamMember member = getMember(team.getId(), operatorId);
        return member != null && member.getStatus() == TeamMember.MemberStatus.ACTIVE
            && member.getRole() == Team.TeamRole.ADMIN;
    }

    private static boolean isActiveMember(UUID teamId, UUID playerId) {
        TeamMember member = getMember(teamId, playerId);
        return member != null && member.getStatus() == TeamMember.MemberStatus.ACTIVE;
    }

    private static void upsertMember(UUID teamId, UUID playerId, Team.TeamRole role, UUID operatorId) {
        TeamMember member = getMember(teamId, playerId);
        if (member == null) {
            member = new TeamMember(teamId, playerId, operatorId);
            member.setRole(role);
            TeamMemberDao.insert(member);
            return;
        }
        TeamMember oldMember = UtilHelper.deepClone(member, TeamMember.class);
        member.setRole(role);
        member.setStatus(TeamMember.MemberStatus.ACTIVE);
        member.setLastOperatorId(operatorId);
        member.setLastOperationTime(LocalDateTime.now());
        TeamMemberDao.updateByIdByCompare(member, oldMember);
    }

    private static void updateMemberCount(Team team) {
        Team oldTeam = UtilHelper.deepClone(team, Team.class);
        int count = 0;
        for (TeamMember member : getMembers(team.getId())) {
            if (member.getStatus() == TeamMember.MemberStatus.ACTIVE) count++;
        }
        team.setTotalMembers(count);
        team.setUpdateTime(LocalDateTime.now());
        TeamDao.updateByIdByCompare(team, oldTeam);
    }

    private static UUID findNextOwner(UUID teamId, UUID oldOwnerId) {
        for (TeamMember member : getMembers(teamId)) {
            if (member.getStatus() == TeamMember.MemberStatus.ACTIVE && !oldOwnerId.equals(member.getPlayerId())) {
                return member.getPlayerId();
            }
        }
        return null;
    }

    private static void markStale(Team team) {
        Team oldTeam = UtilHelper.deepClone(team, Team.class);
        team.setSyncStatus(Team.SyncStatus.STALE);
        team.setLastSyncTime(LocalDateTime.now());
        team.setUpdateTime(LocalDateTime.now());
        TeamDao.updateByIdByCompare(team, oldTeam);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim()
            .isEmpty();
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
