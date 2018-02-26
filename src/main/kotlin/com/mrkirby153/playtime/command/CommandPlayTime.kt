package com.mrkirby153.playtime.command

import com.mrkirby153.playtime.PlaytimeTracker
import com.mrkirby153.playtime.Time
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Collections
import java.util.LinkedList
import java.util.Locale
import java.util.UUID

class CommandPlayTime : CommandBase() {
    override fun getCommandName(): String {
        return "playtime"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/playtime"
    }

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            // Display the play times for everyone
            val playTimes = mutableMapOf<UUID, Long>()
            PlaytimeTracker.instance.repository.repo.forEach { (key, value) ->
                playTimes[key] = value.getTotalPlaytime()
            }

            val sorted = sortByValue(playTimes)
            var num = 1

            var displayed = false

            sorted.forEach { k, v ->
                if (num > 5 && sender is EntityPlayer)
                    return@forEach
                if (sender is EntityPlayer) {
                    if (k == sender.uniqueID)
                        displayed = true
                }
                sender.addChatMessage(TextComponentString(
                        "#${num++}: ${PlaytimeTracker.instance.usernameRepo.getName(
                                k)} - ${Time.format(1, v, Time.TimeUnit.FIT)}"))
            }
            var place = 1
            sorted.forEach { k, _ ->
                if (sender is EntityPlayer) {
                    if (sender.uniqueID == k)
                        return@forEach
                    else
                        place++
                }
            }
            if (sender is EntityPlayer && !displayed) {
                sender.addChatMessage(TextComponentString(""))
                sender.addChatComponentMessage(TextComponentString(
                        "#$place: ${sender.name} = ${Time.format(1, playTimes[sender.uniqueID]!!,
                                Time.TimeUnit.FIT)}"))
            }
        } else if (args.size == 1) {
            val user = args[0]
            val uuid = PlaytimeTracker.instance.usernameRepo.getUUID(user)
            if (uuid == null) {
                sender.addChatMessage(TextComponentString("That user does not exist!").apply {
                    style = Style().apply { color = TextFormatting.RED }
                })
                return
            }

            val playTime = PlaytimeTracker.instance.repository.repo[uuid] ?: return

            val sessions = playTime.sessions

            var sessionCount = sessions.size

            var average = 0.0
            sessions.forEach {
                average += it.duration
            }
            if (playTime.currentSession != null) {
                sessionCount++
                average += playTime.currentSession!!.duration
            }
            average /= sessionCount

            var sd = 0.0
            sessions.forEach {
                sd += Math.pow((it.duration) / -average, 2.0) / sessionCount
            }
            sd = Math.sqrt(sd)

            sender.addChatMessage(TextComponentString("Playtime for $user").apply {
                style = Style().apply { color = TextFormatting.GREEN }
            })

            sender.addChatMessage(TextComponentString(
                    "Time Played: ${Time.format(1, playTime.getTotalPlaytime(),
                            Time.TimeUnit.FIT)}"))
            sender.addChatMessage(TextComponentString(
                    "Average Time Played: ${Time.format(1, average.toLong(), Time.TimeUnit.FIT)}"))
            sender.addChatMessage(TextComponentString("Std. Dev: ${formatDouble(sd)}"))
        }
    }

    fun formatDouble(double: Double): Double = DecimalFormat("#.##",
            DecimalFormatSymbols(Locale.US)).format(double).toDouble()

    fun <K, V : Comparable<V>> sortByValue(map: Map<K, V>): Map<K, V> {
        val list = LinkedList<Map.Entry<K, V>>(map.entries)

        Collections.sort(list) { o1, o2 ->
            o2.value.compareTo(o1.value)
        }

        val sortedMap = LinkedHashMap<K, V>()

        list.forEach {
            sortedMap.put(it.key, it.value)
        }
        return sortedMap
    }

    override fun checkPermission(server: MinecraftServer?, sender: ICommandSender?): Boolean {
        return true
    }


}