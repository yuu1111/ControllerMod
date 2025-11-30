package com.github.yuu1111.controllermod;

import static com.github.yuu1111.controllermod.constants.Constants.MOD_ID;
import static com.github.yuu1111.controllermod.constants.Constants.MOD_NAME;
import static com.github.yuu1111.controllermod.constants.Constants.MOD_VERSION;

import com.github.yuu1111.controllermod.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = MOD_ID,
    version = MOD_VERSION,
    name = MOD_NAME,
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "com.github.yuu1111.controllermod.gui.GuiFactory")
public class ControllerMod {

    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    @SidedProxy(
        clientSide = "com.github.yuu1111.controllermod.proxy.ClientProxy",
        serverSide = "com.github.yuu1111.controllermod.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOG.info("ControllerMod PreInit");
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOG.info("ControllerMod Init");
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOG.info("ControllerMod PostInit");
        proxy.postInit(event);
    }
}
