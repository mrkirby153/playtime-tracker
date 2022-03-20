package com.mrkirby153.playtimetracker.repository;

import com.mrkirby153.playtimetracker.PlaytimeTracker;
import me.mrkirby153.kcutils.Time;
import me.mrkirby153.kcutils.Time.TimeUnit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PlaytimeData extends WorldSavedData {

    private static final Logger LOGGER = LogManager.getLogger();

    public static String ID = "playtime";

    private Map<UUID, List<PlaySession>> playtime = new HashMap<>();
    private Map<UUID, String> uuidToNameMap = new HashMap<>();

    public PlaytimeData() {
        super(ID);
    }

    @Override
    public void load(CompoundNBT nbt) {
        LOGGER.debug("Loading playtime data");
        nbt.getAllKeys().forEach(key -> {
            LOGGER.debug("Loading key {}", key);
            INBT compound = nbt.get(key);
            if (compound instanceof CompoundNBT) {
                String name = ((CompoundNBT) compound).getString("name");
                uuidToNameMap.put(UUID.fromString(key), name);
                LOGGER.debug("Mapping {} -> {}", key, name);
                INBT sessions = ((CompoundNBT) compound).get("sessions");
                if (sessions instanceof CompoundNBT) {
                    List<PlaySession> playSessions = new ArrayList<>();
                    ((CompoundNBT) sessions).getAllKeys().forEach(session -> {
                        LOGGER.debug("Loading session {}", session);
                        INBT sessionCompound = ((CompoundNBT) sessions).get(session);
                        if (sessionCompound instanceof CompoundNBT) {
                            CompoundNBT casted = (CompoundNBT) sessionCompound;
                            playSessions.add(new PlaySession(session, casted.getLong("start"),
                                casted.getLong("end")));
                        }
                    });
                    playtime.put(UUID.fromString(key), playSessions);
                }
            }
        });
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        playtime.forEach((uuid, playSessions) -> {
            LOGGER.debug("Saving sessions for {}", uuid.toString());
            CompoundNBT root = new CompoundNBT();
            root.putString("name", uuidToNameMap.getOrDefault(uuid, "Unknown"));

            CompoundNBT sessions = new CompoundNBT();

            AtomicInteger i = new AtomicInteger(0);
            playSessions.forEach(session -> {
                CompoundNBT sessionCompound = new CompoundNBT();
                sessionCompound.putLong("start", session.getStart());
                sessionCompound.putLong("end", session.getEnd());
                sessions.put(session.getId(), sessionCompound);
                LOGGER.debug("Saved session {}", session.getId());
                i.incrementAndGet();
            });

            root.put("sessions", sessions);
            nbt.put(uuid.toString(), root);
            LOGGER.debug("Saved {} sessions for {}", i.get(), uuid.toString());
        });
        return nbt;
    }

    public void start(PlayerEntity entity) {
        // Check if there already is a started session, if so, discard
        List<PlaySession> sessions = playtime.computeIfAbsent(entity.getUUID(),
            uuid -> new ArrayList<>());
        String prev = uuidToNameMap.put(entity.getUUID(), entity.getName().getString());
        if (!entity.getName().getString().equals(prev)) {
            this.setDirty();
        }
        if (sessions.stream().anyMatch(PlaySession::isActive)) {
            return;
        }
        sessions.add(new PlaySession(UUID.randomUUID().toString().replaceAll("-", ""),
            System.currentTimeMillis(), -1));
        this.setDirty();
    }

    public void stop(PlayerEntity entity) {
        PlaytimeTracker.LOGGER.debug("Stopping session for {}", entity.getUUID());
        List<PlaySession> sessions = playtime.computeIfAbsent(entity.getUUID(),
            uuid -> new ArrayList<>());
        Optional<PlaySession> currentSession = sessions.stream().filter(PlaySession::isActive)
            .findFirst();
        currentSession.ifPresent(session -> {
            sessions.remove(session);
            sessions.add(
                new PlaySession(session.getId(), session.getStart(), System.currentTimeMillis()));
            this.setDirty();
            long duration = System.currentTimeMillis() - session.getStart();
            PlaytimeTracker.LOGGER.debug("Stopped session {} ({})", session.getId(),
                Time.formatLong(duration, TimeUnit.SECONDS));
        });
    }

    public List<PlaySession> get(PlayerEntity entity) {
        return get(entity.getUUID());
    }

    public List<PlaySession> get(UUID uuid) {
        return playtime.computeIfAbsent(uuid, u -> new ArrayList<>());
    }

    public Map<UUID, List<PlaySession>> getAll() {
        return playtime;
    }

    public void endAllRunningSessions() {
        PlaytimeTracker.LOGGER.info("Ending all running sessions");
        AtomicLong amount = new AtomicLong(0);
        playtime.values().forEach(sessions -> {
            Optional<PlaySession> currentSession = sessions.stream().filter(PlaySession::isActive)
                .findFirst();
            currentSession.ifPresent(session -> {
                PlaytimeTracker.LOGGER.info("Ending session id: {}", session.getId());
                sessions.remove(session);
                sessions.add(new PlaySession(session.getId(), session.getStart(),
                    System.currentTimeMillis()));
                this.setDirty();
                amount.incrementAndGet();
            });
        });
        PlaytimeTracker.LOGGER.info("Ended {} running sessions", amount.get());
    }

    public Collection<String> getAllUsernames() {
        return uuidToNameMap.values();
    }

    public String getUsername(UUID uuid) {
        return uuidToNameMap.getOrDefault(uuid, "Unknown");
    }
}
