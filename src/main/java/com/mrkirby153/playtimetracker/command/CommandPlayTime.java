package com.mrkirby153.playtimetracker.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mrkirby153.playtimetracker.repository.PlaySession;
import com.mrkirby153.playtimetracker.repository.PlaytimeData;
import com.mrkirby153.playtimetracker.repository.PlaytimeRepository;
import me.mrkirby153.kcutils.Time;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandPlayTime {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("playtime").requires((source) -> source.hasPermission(0))
                .then(Commands.literal("get").then(
                    Commands.argument("target", GameProfileArgument.gameProfile())
                        .suggests((sender, suggest) -> {
                            PlaytimeData data = PlaytimeRepository.loadFromServer(
                                sender.getSource().getServer());
                            return ISuggestionProvider.suggest(data.getAllUsernames(), suggest);
                        }).executes(sender -> {
                            try {
                                List<GameProfile> profiles = new ArrayList<>(
                                    GameProfileArgument.getGameProfiles(sender, "target"));
                                GameProfile p = profiles.get(0);
                                CommandSource source = sender.getSource();
                                List<PlaySession> sessions = PlaytimeRepository.loadFromServer(
                                    source.getServer()).get(p.getId());

                                Long totalPlayTime = sessions.stream().map(PlaySession::duration)
                                    .reduce(0L, Long::sum);
                                float avg = totalPlayTime / (float) sessions.size();

                                source.sendSuccess(new StringTextComponent(
                                    String.format("Play time for %s", p.getName())), false);
                                source.sendSuccess(new StringTextComponent(
                                    String.format("Time Played: %s over %d sessions",
                                        Time.formatLong(totalPlayTime), sessions.size())), false);
                                source.sendSuccess(new StringTextComponent(
                                    String.format("Average Time Played: %s", Time.formatLong(
                                        (long) avg))), false);
                            } catch (Exception e) {
                                sender.getSource()
                                    .sendFailure(new StringTextComponent("Failed: " + e.getMessage()));
                                return 0;
                            }
                            return 1;
                        })
                ))
                .then(Commands.literal("list").executes(sender -> {
                    Map<UUID, Long> playtime = new HashMap<>();
                    PlaytimeData playtimeData = PlaytimeRepository.loadFromServer(
                        sender.getSource().getServer());
                    playtimeData.getAll()
                        .forEach((uuid, playSessions) -> {
                            playtime.put(uuid,
                                playSessions.stream().map(PlaySession::duration)
                                    .reduce(0L, Long::sum));
                        });

                    LinkedHashMap<UUID, Long> sorted = sortByValue(playtime);

                    AtomicInteger num = new AtomicInteger(1);
                    boolean displayed = false;
                    Entity sourceEntity = sender.getSource().getEntity();
                    for (Map.Entry<UUID, Long> entry : sorted.entrySet()) {
                        if (num.get() > 5 && sourceEntity instanceof PlayerEntity) {
                            break;
                        }
                        if (sourceEntity instanceof PlayerEntity) {
                            LOGGER.debug("Player: {} {}", entry.getKey(), sourceEntity.getUUID());
                            if (entry.getKey().equals(sourceEntity.getUUID())) {
                                LOGGER.debug("Displayed!");
                                displayed = true;
                            }
                        }
                        sender.getSource().sendSuccess(
                            new StringTextComponent(
                                String.format("#%d: %s - %s", num.getAndIncrement(),
                                    playtimeData.getUsername(entry.getKey()),
                                    Time.formatLong(entry.getValue()))), false);
                    }

                    if (sourceEntity instanceof PlayerEntity && !displayed) {
                        int place = 1;
                        for (Map.Entry<UUID, Long> e : sorted.entrySet()) {
                            if (e.getKey() == sourceEntity.getUUID()) {
                                break;
                            }
                            place++;
                        }
                        sender.getSource().sendSuccess(new StringTextComponent(""), false);
                        long time = sorted.get(sourceEntity.getUUID());
                        sender.getSource()
                            .sendSuccess(new StringTextComponent(
                                    String.format("#%d: %s - %s", place,
                                        sourceEntity.getName().getString(), Time.formatLong(time))),
                                false);
                    }
                    return 1;
                })));
    }


    private static <K, V extends Comparable<V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
        LinkedList<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        LinkedHashMap<K, V> sorted = new LinkedHashMap<>();
        list.forEach(e -> {
            sorted.put(e.getKey(), e.getValue());
        });
        return sorted;
    }
}
