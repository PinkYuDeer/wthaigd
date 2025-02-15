package com.pinkyudeer.wthaigd.core;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.helper.SQLiteHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    private static final WorldHandler WORLD_HANDLER = new WorldHandler();

    public static void registerCommonEvents() {
        MinecraftForge.EVENT_BUS.register(WORLD_HANDLER);
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

            SQLiteHelper.saveDataFromMemoryToFile();
        }

        @SubscribeEvent
        public void onWorldSave(WorldEvent.Save event) {
            if (!event.world.provider.isSurfaceWorld()) return;

            Wthaigd.LOG.info("World save event triggered");

            SQLiteHelper.close();
        }
    }
}
