package com.mrkirby153.playtime.listener

import com.mrkirby153.playtime.PlaytimeTracker
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent

object EventListener {

    @SubscribeEvent
    fun onJoin(event: PlayerEvent.PlayerLoggedInEvent) {

    }

    @SubscribeEvent
    fun onQuit(event: PlayerEvent.PlayerLoggedOutEvent) {

    }

    @SubscribeEvent
    fun onSave(event: WorldEvent.Save){
        PlaytimeTracker.instance.repository.save()
    }
}