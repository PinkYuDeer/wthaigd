package com.pinkyudeer.wthaigd.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinkyudeer.wthaigd.Tags;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = Wthaigd.MODID, version = Tags.VERSION, name = Wthaigd.NAME, acceptedMinecraftVersions = "[1.7.10]")
public class Wthaigd {

    public static final String MODID = "wthaigd";
    public static final String NAME = "what the hell am I gonna do";
    // 定义一个log4j的日志记录器，其中
    // debug为调试信息，开发阶段的详细输出，
    // info为普通信息，用户可见的基础日志
    // warn为警告信息，潜在信息，不影响运行
    // error为错误信息，功能异常，但模组仍然可以运行
    // fatal为严重错误，模组无法继续运行
    // trace为跟踪信息，通常用于调试
    // 应该使用 {} 占位符动态插入变量，避免字符串拼接： LOG.info("成功注册了 {} 个物品", itemCount);
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.pinkyudeer.wthaigd.core.ClientProxy",
        serverSide = "com.pinkyudeer.wthaigd.core.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // 模组主类实例化后（最早阶段）: 底层初始化（如反射操作），普通模组极少使用。
    public void construct(FMLConstructionEvent event) {
        proxy.construct(event);
    }

    @Mod.EventHandler
    // 模组配置加载前:注册物品/方块、加载配置文件、设置日志。
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // 所有模组完成 preInit 后: 注册合成配方、发送 IMC（模组间通信） 消息、初始化网络通信。
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // 所有模组完成 init 后: 模组间交互（如获取其他模组 API）、覆盖原版逻辑。
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // 所有模组加载完成后（最终阶段）: 执行最终全局调整（如修改原版生物生成规则）。
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.LoadComplete(event);
    }

    @Mod.EventHandler
    // 服务器实例创建前: 修改服务器配置或预加载数据（极少使用）。
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.serverAboutToStart(event);
    }

    @Mod.EventHandler
    // 服务器启动时: 注册服务端命令、调整服务器设置。
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    // 服务器启动后: 执行启动后逻辑（如广播通知）。
    public void afterServerStarting(FMLServerStartedEvent event) {
        proxy.afterServerStarting(event);
    }

    @Mod.EventHandler
    // 服务器关闭前: 保存数据、清理临时资源。
    public void preServerStopping(FMLServerStoppingEvent event) {
        proxy.preServerStopping(event);
    }

    @Mod.EventHandler
    // 服务器关闭后: 释放长期资源（如关闭数据库连接）。
    public void afterServerStopped(FMLServerStoppedEvent event) {
        proxy.afterServerStopped(event);
    }
}
