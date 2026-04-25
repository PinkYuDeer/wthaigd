package com.pinkyudeer.wthaigd.core;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.db.SQLiteManager;
import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.network.handler.NetMainSync;
import com.pinkyudeer.wthaigd.task.TaskSqlHelper;
import com.pinkyudeer.wthaigd.task.service.TeamService;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class EventHandler {

    public static void registerCommonEvents() {
        MinecraftForge.EVENT_BUS.register(new WorldHandler());
    }

    /**
     * 世界事件处理
     */
    public static class WorldHandler {

        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event) throws IOException {
            if (event.world.provider.dimensionId != 0) return;

            ModFileHelper.updateModWorldDir(Wthaigd.proxy.getCurrentWorldDir(event.world));
            SQLiteManager.initSqlite();
        }

        @SubscribeEvent
        public void onWorldSave(WorldEvent.Save event) {
            // TODO: 测试其他世界暂停是否会保存主世界
            if (event.world.provider.dimensionId != 0) return;

            Wthaigd.LOG.info("World save event triggered");

            SQLiteManager.saveDataFromMemoryToFile();
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (event.world.provider.dimensionId != 0) return;

            Wthaigd.LOG.info("World unload event triggered");

            ModFileHelper.updateModWorldDir(null);

            SQLiteManager.close();
        }
    }

    public static class serverHandler {

        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            Wthaigd.LOG.info("Player logged in: {}", event.player.getDisplayName());

            TaskSqlHelper.player.login(event.player);

            if (event.player instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) event.player;
                TeamService.syncLinkedTeamsForPlayer(player.getUniqueID(), isOp(player));
                NetMainSync.sendReset(player, true, true);
            }
        }

        @SubscribeEvent
        public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Wthaigd.LOG.info("Player logged out: {}", event.player.getDisplayName());
        }

        private boolean isOp(EntityPlayerMP player) {
            return player.mcServer != null && player.mcServer.getConfigurationManager()
                .func_152596_g(player.getGameProfile());
        }
    }
}
