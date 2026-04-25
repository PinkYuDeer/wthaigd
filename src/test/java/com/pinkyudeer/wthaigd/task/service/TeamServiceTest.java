package com.pinkyudeer.wthaigd.task.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.entity.record.TeamMember;
import com.pinkyudeer.wthaigd.task.entity.record.TeamRequest;
import com.pinkyudeer.wthaigd.test.SqliteTestSupport;

public class TeamServiceTest extends SqliteTestSupport {

    @Test
    public void localTeamInviteAcceptKickAndTransferFlowWorks() {
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID nextOwner = UUID.randomUUID();

        Team team = TeamService.createLocalTeam("alpha", owner, "main team");
        assertNotNull(team);
        assertEquals(Team.SyncSource.LOCAL, team.getSyncSource());
        assertEquals(
            Team.TeamRole.ADMIN,
            TeamService.getMember(team.getId(), owner)
                .getRole());

        TeamRequest invite = TeamService.invitePlayer(team.getId(), member, owner);
        assertNotNull(invite);
        assertTrue(TeamService.acceptRequest(invite.getId(), member, false));
        TeamMember acceptedMember = TeamService.getMember(team.getId(), member);
        assertNotNull(acceptedMember);
        assertEquals(TeamMember.MemberStatus.ACTIVE, acceptedMember.getStatus());

        TeamRequest secondInvite = TeamService.invitePlayer(team.getId(), nextOwner, owner);
        assertTrue(TeamService.acceptRequest(secondInvite.getId(), nextOwner, false));
        assertTrue(TeamService.transferOwner(team.getId(), nextOwner, owner, false));
        assertEquals(
            nextOwner,
            TeamService.getTeam(team.getId())
                .getOwnerId());

        assertTrue(TeamService.kickMember(team.getId(), member, nextOwner, false));
        assertEquals(
            TeamMember.MemberStatus.LEFT,
            TeamService.getMember(team.getId(), member)
                .getStatus());
    }

    @Test(expected = SecurityException.class)
    public void linkedTeamCannotBeEditedLocally() {
        UUID owner = UUID.randomUUID();
        Team team = TeamService.createLocalTeam("linked", owner, null);
        team.setSyncSource(Team.SyncSource.GTNH_LIB);
        TeamService.unlinkExternalTeam(team.getId(), owner, true);
        Team linked = TeamService.getTeam(team.getId());
        linked.setSyncSource(Team.SyncSource.GTNH_LIB);
        com.pinkyudeer.wthaigd.db.SQLHelper.updateById(linked)
            .execute();

        TeamService.invitePlayer(team.getId(), UUID.randomUUID(), owner);
    }
}
