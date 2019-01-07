package com.mrkirby153.playtime.listener

import com.mrkirby153.playtime.PlaytimeTracker
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent

object EventListener {

    @SubscribeEvent
    fun onJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        PlaytimeTracker.instance.repository.get(event.player).startNewSession()
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
}