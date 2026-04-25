package com.pinkyudeer.wthaigd.network.handler;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;
import com.pinkyudeer.wthaigd.task.service.TeamService;

public final class NetTeamAction {

    private NetTeamAction() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(PacketIds.TEAM_ACTION, NetTeamAction::onServer);
    }

    public static void sendAction(NBTTagCompound payload) {
        PacketSender.INSTANCE.sendToServer(PacketIds.TEAM_ACTION, payload);
    }

    private static void onServer(NBTTagCompound payload, EntityPlayerMP sender) {
        if (sender == null) return;
        String action = payload.getString("action");
        try {
            UUID actorId = sender.getUniqueID();
            boolean op = isOp(sender);
            if ("create".equals(action)) {
                TeamService.createLocalTeam(payload.getString("name"), actorId, payload.getString("description"));
                NetTeamSync.sendSync(sender, true);
            } else if ("invite".equals(action)) {
                TeamService.invitePlayer(readUuid(payload, "teamId"), readUuid(payload, "playerId"), actorId);
                NetTeamSync.sendSync(sender, true);
            } else if ("request_join".equals(action)) {
                TeamService.requestJoin(readUuid(payload, "teamId"), actorId, payload.getString("reason"));
                NetTeamSync.sendSync(sender, true);
            } else if ("accept".equals(action)) {
                TeamService.acceptRequest(readUuid(payload, "requestId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("kick".equals(action)) {
                TeamService.kickMember(readUuid(payload, "teamId"), readUuid(payload, "playerId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("leave".equals(action)) {
                TeamService.leaveTeam(readUuid(payload, "teamId"), actorId);
                NetTeamSync.sendSync(sender, true);
            } else if ("transfer_owner".equals(action)) {
                TeamService.transferOwner(readUuid(payload, "teamId"), readUuid(payload, "playerId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("link_bq".equals(action)) {
                TeamService
                    .linkBetterQuestingParty(readUuid(payload, "teamId"), payload.getInteger("partyId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("sync_bq".equals(action)) {
                TeamService.syncBetterQuestingTeam(readUuid(payload, "teamId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("unlink_bq".equals(action)) {
                TeamService.unlinkBetterQuestingTeam(readUuid(payload, "teamId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("link_gtnh".equals(action)) {
                TeamService.linkGtnhLibTeam(readUuid(payload, "teamId"), payload.getString("teamKey"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("sync_gtnh".equals(action)) {
                TeamService.syncGtnhLibTeam(readUuid(payload, "teamId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else if ("unlink_gtnh".equals(action) || "unlink_external".equals(action)) {
                TeamService.unlinkExternalTeam(readUuid(payload, "teamId"), actorId, op);
                NetTeamSync.sendSync(sender, true);
            } else {
                NetError.send(sender, NetError.INVALID_ACTION, action);
            }
        } catch (SecurityException e) {
            NetError.send(sender, NetError.PERMISSION_DENIED, e.getMessage());
        } catch (IllegalArgumentException e) {
            NetError.send(sender, NetError.INVALID_PAYLOAD, e.getMessage());
        } catch (Exception e) {
            Wthaigd.LOG.error("Team action failed: {}", action, e);
            NetError.send(sender, NetError.SERVER_ERROR, e.getMessage());
        }
    }

    private static UUID readUuid(NBTTagCompound payload, String key) {
        if (!payload.hasKey(key) || payload.getString(key)
            .isEmpty()) return null;
        return UUID.fromString(payload.getString(key));
    }

    private static boolean isOp(EntityPlayerMP player) {
        return player.mcServer != null && player.mcServer.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }
}
