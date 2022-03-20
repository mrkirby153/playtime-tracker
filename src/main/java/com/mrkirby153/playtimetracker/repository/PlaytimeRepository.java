package com.mrkirby153.playtimetracker.repository;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;

public class PlaytimeRepository {

    public static void startSession(World world, PlayerEntity player) {
        loadFromOverworld(world).start(player);
    }

    public static void stopSession(World world, PlayerEntity player) {
        loadFromOverworld(world).stop(player);
    }

    private static PlaytimeData loadFromOverworld(World world) {
        MinecraftServer server = world.getServer();
        if(server != null) {
            ServerWorld overworld = world.getServer().overworld();
            DimensionSavedDataManager dataManager = overworld.getDataStorage();
            return dataManager.computeIfAbsent(PlaytimeData::new, PlaytimeData.ID);
        } else {
            throw new IllegalStateException("World was null");
        }
    }
}
