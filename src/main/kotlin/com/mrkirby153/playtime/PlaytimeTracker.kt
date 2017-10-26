package com.mrkirby153.playtime

import com.mrkirby153.playtime.listener.EventListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(modid = Constants.MODID, version = Constants.VERSION, name = Constants.NAME, acceptableRemoteVersions = "*")
class PlaytimeTracker {

    val logger: Logger = LogManager.getLogger(Constants.MODID)

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger.info("Calling pre-init")
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        if (event.side == Side.SERVER) {
            logger.info("Calling init")
            MinecraftForge.EVENT_BUS.register(EventListener)
        } else {
            logger.info("Skipping init. Wrong side")
        }
    }

    companion object {
        @Mod.Instance(Constants.MODID)
        lateinit var instance: PlaytimeTracker
    }

}