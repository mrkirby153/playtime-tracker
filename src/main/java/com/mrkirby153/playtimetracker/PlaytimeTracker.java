package com.mrkirby153.playtimetracker;

import com.mrkirby153.playtimetracker.repository.PlaytimeRepository;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("playtimetracker")
public class PlaytimeTracker {

    public static final Logger LOGGER = LogManager.getLogger();

    public PlaytimeTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("Server is starting!");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        LOGGER.info("Player logged in, starting session");
        PlaytimeRepository.startSession(event.getEntity().level, event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        LOGGER.info("Player logged out, stopping session");
        PlaytimeRepository.stopSession(event.getEntity().level, event.getPlayer());
    }
}