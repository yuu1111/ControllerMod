package com.github.yuu1111.controllermod.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        // Server-side pre-initialization
    }

    public void init(FMLInitializationEvent event) {
        // Server-side initialization
    }

    public void postInit(FMLPostInitializationEvent event) {
        // Server-side post-initialization
    }
}
