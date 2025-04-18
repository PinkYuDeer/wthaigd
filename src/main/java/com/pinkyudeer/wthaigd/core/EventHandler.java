package com.pinkyudeer.wthaigd.core;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;
import com.pinkyudeer.wthaigd.helper.network.NetWorkData;
import com.pinkyudeer.wthaigd.helper.network.NetWorkHelper;
import com.pinkyudeer.wthaigd.task.TaskSqlHelper;

import cpw.mods.fml.common.FMLCommonHandler;
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

            if (FMLCommonHandler.instance()
                .getSide()
                .isServer()) {
                ModFileHelper.updateModWorldDir(
                    event.world.getSaveHandler()
                        .getWorldDirectory()
                        .getCanonicalFile());
            } else {
                IntegratedServer IS = Minecraft.getMinecraft()
                    .getIntegratedServer();
                File saveFolderName = ModFileHelper.getSavesDir();
                if (IS == null) {
                    ModFileHelper.updateModWorldDir(saveFolderName);
                    return;
                }
                ModFileHelper.updateModWorldDir(
                    new File(
                        ModFileHelper.getSavesDir(),
                        Minecraft.getMinecraft()
                            .getIntegratedServer()
                            .getFolderName()));
            }
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

            NetWorkHelper.SendMessageToClient("welcome", event.player, NetWorkData.DataType.PLAYER_LOGIN, 200);
        }

        @SubscribeEvent
        public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Wthaigd.LOG.info("Player logged out: {}", event.player.getDisplayName());
        }
    }
}
