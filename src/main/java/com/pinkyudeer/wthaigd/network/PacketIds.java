package com.pinkyudeer.wthaigd.network;

import net.minecraft.util.ResourceLocation;

import com.pinkyudeer.wthaigd.Wthaigd;

public final class PacketIds {

    public static final ResourceLocation MAIN_SYNC = id("main_sync");
    public static final ResourceLocation TASK_ACTION = id("task_action");
    public static final ResourceLocation TASK_SYNC = id("task_sync");
    public static final ResourceLocation TEAM_ACTION = id("team_action");
    public static final ResourceLocation TEAM_SYNC = id("team_sync");
    public static final ResourceLocation INVITE_SYNC = id("invite_sync");
    public static final ResourceLocation ERROR = id("error");

    private PacketIds() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Wthaigd.MODID + ":" + path);
    }
}
