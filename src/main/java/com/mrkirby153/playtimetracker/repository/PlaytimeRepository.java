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

    public static PlaytimeData loadFromServer(MinecraftServer server) {
        ServerWorld overworld = server.overworld();
        DimensionSavedDataManager dataManager = overworld.getDataStorage();
        return dataManager.computeIfAbsent(PlaytimeData::new, PlaytimeData.ID);
    }

    public static PlaytimeData loadFromOverworld(World world) {
        MinecraftServer server = world.getServer();
        if (server == null) {
            throw new IllegalArgumentException("Server was null");
        }
        return loadFromServer(server);
    }
}
