package com.github.yuu1111.controllermod;

import com.github.yuu1111.controllermod.controller.ControllerHandler;

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
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // Initialize controller handler
        controllerHandler = new ControllerHandler();
        controllerHandler.init();

        // Register tick handler
        FMLCommonHandler.instance()
            .bus()
            .register(this);
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

    public ControllerHandler getControllerHandler() {
        return controllerHandler;
    }
}
