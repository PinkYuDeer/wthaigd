package com.pinkyudeer.wthaigd.network.handler;

import java.time.LocalDateTime;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.pinkyudeer.wthaigd.client.TaskClientStore;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.entity.record.TeamMember;
import com.pinkyudeer.wthaigd.task.service.TeamService;

public final class NetTeamSync {

    private NetTeamSync() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(PacketIds.TEAM_SYNC, NetTeamSync::onServer);
        PacketTypeRegistry.INSTANCE.registerClientHandler(PacketIds.TEAM_SYNC, NetTeamSync::onClient);
    }

    public static void requestSync() {
        PacketSender.INSTANCE.sendToServer(PacketIds.TEAM_SYNC, new NBTTagCompound());
    }

    public static void sendSync(EntityPlayerMP player, boolean merge) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setBoolean("merge", merge);
        NBTTagList list = new NBTTagList();
        for (Team team : TeamService.getVisibleTeams(player.getUniqueID())) {
            list.appendTag(writeTeam(team, TeamService.getMembers(team.getId())));
        }
        payload.setTag("data", list);
        PacketSender.INSTANCE.sendToPlayers(PacketIds.TEAM_SYNC, payload, player);
    }

    private static void onServer(NBTTagCompound payload, EntityPlayerMP sender) {
        if (sender != null) sendSync(sender, false);
    }

    private static void onClient(NBTTagCompound payload) {
        TaskClientStore.INSTANCE.acceptTeamSync(payload.getTagList("data", 10), payload.getBoolean("merge"));
    }

    private static NBTTagCompound writeTeam(Team team, List<TeamMember> members) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", team.getId() == null ? "" : team.getId().toString());
        tag.setString("name", team.getName() == null ? "" : team.getName());
        tag.setString("description", team.getDescription() == null ? "" : team.getDescription());
        tag.setString("ownerId", team.getOwnerId() == null ? "" : team.getOwnerId().toString());
        tag.setInteger("totalMembers", team.getTotalMembers() == null ? 0 : team.getTotalMembers());
        tag.setString("syncSource", team.getSyncSource() == null ? "" : team.getSyncSource().name());
        tag.setInteger("externalPartyId", team.getExternalPartyId() == null ? -1 : team.getExternalPartyId());
        tag.setString("externalTeamKey", team.getExternalTeamKey() == null ? "" : team.getExternalTeamKey());
        tag.setString("syncStatus", team.getSyncStatus() == null ? "" : team.getSyncStatus().name());
        tag.setString("lastSyncTime", writeTime(team.getLastSyncTime()));

        NBTTagList memberList = new NBTTagList();
        for (TeamMember member : members) {
            NBTTagCompound memberTag = new NBTTagCompound();
            memberTag.setString("playerId", member.getPlayerId() == null ? "" : member.getPlayerId().toString());
            memberTag.setString("role", member.getRole() == null ? "" : member.getRole().name());
            memberTag.setString("status", member.getStatus() == null ? "" : member.getStatus().name());
            memberList.appendTag(memberTag);
        }
        tag.setTag("members", memberList);
        return tag;
    }

    private static String writeTime(LocalDateTime time) {
        return time == null ? "" : time.toString();
    }
}
