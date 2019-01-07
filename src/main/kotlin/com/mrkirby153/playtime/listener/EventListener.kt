package com.mrkirby153.playtime.listener

import com.mrkirby153.playtime.PlaytimeTracker
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.TimeUnit

object EventListener {

    private var lastSessionHeartbeat = 0L

    private val heartbeatInterval = 5000 // Every 5 seconds

    @SubscribeEvent
    fun onJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val pt = PlaytimeTracker.instance.repository.get(event.player)
        if (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(5,
                        TimeUnit.MINUTES) > pt.sessionHeartbeat && pt.currentSession != null) {
            PlaytimeTracker.instance.logger.info(
                    "Invalid session for ${event.player.uniqueID}, starting a new one")
            pt.currentSession = null
            pt.sessionHeartbeat = 0
        }
        pt.startNewSession()
        PlaytimeTracker.instance.usernameRepo.update(event.player)
    }

    @SubscribeEvent
    fun onQuit(event: PlayerEvent.PlayerLoggedOutEvent) {
        PlaytimeTracker.instance.repository.get(event.player).endCurrentSession()
    }

    @SubscribeEvent
    fun onSave(event: WorldEvent.Save) {
        PlaytimeTracker.instance.repository.save()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.WorldTickEvent) {
        if (event.phase != TickEvent.Phase.START && event.side != Side.SERVER)
            return // Ensure we only heartbeat once per tick on the server
        if (lastSessionHeartbeat + heartbeatInterval < System.currentTimeMillis()) {
            PlaytimeTracker.instance.repository.repo.entries.filter {
                event.world.minecraftServer?.playerList?.getPlayerByUUID(it.key) != null
            }.forEach {
                it.value.sessionHeartbeat = System.currentTimeMillis()
            }
            lastSessionHeartbeat = System.currentTimeMillis()
        }
    }
}