package com.pinkyudeer.wthaigd.core;

import com.pinkyudeer.wthaigd.Tags;
import com.pinkyudeer.wthaigd.helper.ConfigHelper;
import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.loader.BlockLoader;
import com.pinkyudeer.wthaigd.loader.CommandLoader;
import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;
import com.pinkyudeer.wthaigd.loader.ItemLoader;
import com.pinkyudeer.wthaigd.loader.RecipeLoader;

import cpw.mods.fml.common.FMLCommonHandler;
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

@SuppressWarnings("EmptyMethod")
public class CommonProxy {

    // 在模组主类实例化后触发，用于极早期的初始化（如反射操作）。
    public void construct(FMLConstructionEvent event) {}

    // 注册物品/方块、加载配置文件、设置日志（最常见入口）。
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHelper.init(event.getSuggestedConfigurationFile());
        Wthaigd.LOG.info(ConfigHelper.getStringConfig("greeting"));
        Wthaigd.LOG.info("wthaigd version {}", Tags.VERSION);
        FMLCommonHandler.instance()
            .bus()
            .register(new ConfigHelper());
        ModFileHelper.init();
        CreativeTabsLoader.init();
        BlockLoader.init();
        ItemLoader.init();
    }

    // 注册合成配方、网络通信、事件监听器。
    public void init(FMLInitializationEvent event) {
        RecipeLoader.init();
        EventHandler.registerCommonEvents();
        FMLCommonHandler.instance()
            .bus()
            .register(new EventHandler.playerHandler());
    }

    // 模组间交互（如获取其他模组内容）、覆盖原版逻辑。
    public void postInit(FMLPostInitializationEvent event) {}

    // 模组加载完成后的操作, 执行最终全局调整（如修改原版生物生成规则）。
    public void LoadComplete(FMLLoadCompleteEvent event) {}

    // 服务器即将启动前的操作
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        CommandLoader.init(event);
    }

    // 服务端启动后的后续操作
    public void afterServerStarting(FMLServerStartedEvent event) {}

    // 服务器关闭前的操作
    public void preServerStopping(FMLServerStoppingEvent event) {}

    // 服务器关闭后的操作
    public void afterServerStopped(FMLServerStoppedEvent event) {}

}
