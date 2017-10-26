package com.mrkirby153.playtime.listener

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent

object EventListener {

    @SubscribeEvent
    fun onJoin(event: PlayerEvent.PlayerLoggedInEvent) {

    }

    @SubscribeEvent
    fun onQuit(event: PlayerEvent.PlayerLoggedOutEvent) {

    }
}