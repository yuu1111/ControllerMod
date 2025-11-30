package com.github.yuu1111.controllermod.proxy;

import com.github.yuu1111.controllermod.ControllerMod;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import com.github.yuu1111.controllermod.config.ControllerConfig;
import com.github.yuu1111.controllermod.controller.ControllerHandler;
import com.github.yuu1111.controllermod.gui.VirtualCursor;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy {

    private ControllerHandler controllerHandler;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        // Config登録
        try {
            ConfigurationManager.registerConfig(ControllerConfig.class);
            ControllerMod.LOG.info("Config registered successfully");
        } catch (ConfigException e) {
            ControllerMod.LOG.error("Failed to register config", e);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // Initialize controller handler
        controllerHandler = new ControllerHandler();
        controllerHandler.init();

        // Register tick handler (FML events)
        FMLCommonHandler.instance()
            .bus()
            .register(this);

        // Register render handler (Forge events)
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && controllerHandler != null) {
            controllerHandler.update();
        }
    }

    /**
     * GUI描画後にバーチャルカーソルを描画する
     *
     * @param event GUI描画イベント
     */
    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (controllerHandler != null && controllerHandler.getInputHandler() != null) {
            VirtualCursor cursor = controllerHandler.getInputHandler()
                .getVirtualCursor();
            if (cursor != null && cursor.isActive()) {
                cursor.render();
            }
        }
    }

    public ControllerHandler getControllerHandler() {
        return controllerHandler;
    }
}
