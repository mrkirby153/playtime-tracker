package com.mrkirby153.playtime

import com.mrkirby153.playtime.command.CommandPlayTime
import com.mrkirby153.playtime.listener.EventListener
import com.mrkirby153.playtime.repository.PlayTimeRepository
import com.mrkirby153.playtime.repository.UsernameRepository
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.Random

@Mod(modid = Constants.MODID, version = Constants.VERSION, name = Constants.NAME, acceptableRemoteVersions = "*")
class PlaytimeTracker {

    val logger: Logger = LogManager.getLogger(Constants.MODID)

    lateinit var repository: PlayTimeRepository
    lateinit var usernameRepo: UsernameRepository

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

    @Mod.EventHandler
    fun serverStart(event: FMLServerStartingEvent) {
        if (event.side != Side.SERVER)
            return
        event.registerServerCommand(CommandPlayTime())
        this.repository = PlayTimeRepository()
        this.usernameRepo = UsernameRepository()
        repository.load()
        usernameRepo.load()
    }


    fun generateId(size: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        var id = ""
        val random = Random()
        for (i in 0 until size) {
            id += chars[random.nextInt(chars.length)]
        }
        return id
    }

    companion object {
        @Mod.Instance(Constants.MODID)
        lateinit var instance: PlaytimeTracker
    }

}