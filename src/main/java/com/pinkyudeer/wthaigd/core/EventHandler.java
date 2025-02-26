package com.pinkyudeer.wthaigd.core;

import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.helper.SQLiteHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatisticsFile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

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
            if (!event.world.provider.isSurfaceWorld()) return;

            File currentWorldDir;
            if (FMLCommonHandler.instance()
                .getSide()
                .isServer()) {
                currentWorldDir = event.world.getSaveHandler()
                    .getWorldDirectory()
                    .getCanonicalFile();
            } else {
                // 如果是加入服务器，则返回
                if (Minecraft.getMinecraft()
                    .getIntegratedServer() == null) {
                    return;
                }

                currentWorldDir = new File(
                    ModFileHelper.getSavesDir(),
                    Minecraft.getMinecraft()
                        .getIntegratedServer()
                        .getFolderName());
            }
            ModFileHelper.updateModWorldDir(currentWorldDir);

            SQLiteHelper.initializeDatabases();
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (!event.world.provider.isSurfaceWorld()) return;

            ModFileHelper.updateModWorldDir(null);

            SQLiteHelper.close();
        }

        @SubscribeEvent
        public void onWorldSave(WorldEvent.Save event) {
            if (!event.world.provider.isSurfaceWorld()) return;

            Wthaigd.LOG.info("World save event triggered");

            SQLiteHelper.saveDataFromMemoryToFile();
        }
    }

    public static class playerHandler {

        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) throws NoSuchFieldException {
            Wthaigd.LOG.info("Player logged in: {}", event.player.getDisplayName());
            try {
                Class<? extends EntityPlayer> player = event.player.getClass();
                Field field = player.getDeclaredField("field_147103_bO");
                field.setAccessible(true);
                StatisticsFile statsFile = (StatisticsFile) field.get(event.player);
                Wthaigd.LOG.info("Player stats: {}", statsFile.toString());
                Class<? extends StatisticsFile> statsFileClass = statsFile.getClass();
                field = statsFileClass.getDeclaredField("field_150887_d");
                field.setAccessible(true);
                Wthaigd.LOG.info(
                    "Player stats file: {}",
                    field.get(statsFile)
                        .toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

        @SubscribeEvent
        public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Wthaigd.LOG.info("Player logged out: {}", event.player.getDisplayName());
        }
    }
}
