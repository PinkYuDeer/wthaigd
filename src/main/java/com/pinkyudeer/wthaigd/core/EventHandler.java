package com.pinkyudeer.wthaigd.core;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;
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
            }
        }

        @SubscribeEvent
        public void onWorldSave(WorldEvent.Save event) {
            // TODO: 测试其他世界暂停是否会保存主世界
            if (event.world.provider.dimensionId != 0) return;

            Wthaigd.LOG.info("World save event triggered");

            SQLiteManager.saveDataFromMemoryToFile();
        }
    }

    public static class playerHandler {

        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            Wthaigd.LOG.info("Player logged in: {}", event.player.getDisplayName());

            if (FMLCommonHandler.instance()
                .getSide()
                .isClient()) {
                // 如果是加入服务器，则返回
                if (Minecraft.getMinecraft()
                    .getIntegratedServer() == null) {
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

            TaskSqlHelper.player.login(event.player);
        }

        @SubscribeEvent
        public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Wthaigd.LOG.info("Player logged out: {}", event.player.getDisplayName());

            ModFileHelper.updateModWorldDir(null);

            SQLiteManager.close();
        }
    }
}
