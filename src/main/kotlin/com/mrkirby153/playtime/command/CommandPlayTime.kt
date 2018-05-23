package com.mrkirby153.playtime.command

import com.mrkirby153.playtime.PlaytimeTracker
import me.mrkirby153.kcutils.Time
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.LinkedHashMap

class CommandPlayTime : CommandBase() {
    override fun getName(): String {
        return "playtime"
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
                sender.sendMessage(TextComponentString("#${num++}: ${PlaytimeTracker.instance.usernameRepo.getName(k)} - ${Time.formatLong(v)}"))
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
                sender.sendMessage(TextComponentString(""))
                val time = playTimes[sender.uniqueID]
                sender.sendMessage(TextComponentString("#$place: ${sender.name} = ${if (time != null) Time.formatLong(time) else "Unknown"}"))
            }
        } else if (args.size == 1) {
            val user = args[0]
            val uuid = PlaytimeTracker.instance.usernameRepo.getUUID(user)
            if (uuid == null) {
                sender.sendMessage(TextComponentString("That user does not exist!").apply {
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

            sender.sendMessage(TextComponentString("Playtime for $user").apply {
                style = Style().apply { color = TextFormatting.GREEN }
            })

            sender.sendMessage(TextComponentString(
                    "Time Played: ${Time.formatLong(playTime.getTotalPlaytime())}"))

            sender.sendMessage(TextComponentString("Current Session: ${Time.formatLong(playTime.getCurrentPlaytime())}"))
            sender.sendMessage(TextComponentString("Average Time Played: ${Time.formatLong(average.toLong())}"))
            sender.sendMessage(TextComponentString("Std. Dev: ${formatDouble(sd)}"))
        }
    }

    override fun getUsage(sender: ICommandSender?): String {
        return "/playtime"
    }

    fun formatDouble(double: Double): Double = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US)).format(double).toDouble()

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