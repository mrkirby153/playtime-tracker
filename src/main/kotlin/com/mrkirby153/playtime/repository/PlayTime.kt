package com.mrkirby153.playtime.repository

import com.google.gson.JsonObject
import com.mrkirby153.playtime.PlaytimeTracker
import com.mrkirby153.playtime.util.JsonSerializable
import java.util.UUID

class PlayTime(val player: UUID) : Comparable<PlayTime> {
    val sessions = mutableListOf<Session>()

    var currentSession: Session? = null


    fun startNewSession() {
        if (currentSession != null) {
            PlaytimeTracker.instance.logger.warn(
                    "Attempting to start a new session while one is already active for $player")
        }
        currentSession = Session(PlaytimeTracker.instance.generateId(10),
                System.currentTimeMillis(), -1)
        PlaytimeTracker.instance.repository.save(player)
    }

    fun endCurrentSession() {
        if (currentSession == null)
            PlaytimeTracker.instance.logger.warn(
                    "Attempting to end a session when one hasn't started for $player")
        currentSession?.logout = System.currentTimeMillis()
        if (currentSession != null)
            sessions.add(currentSession!!)
        currentSession = null
        PlaytimeTracker.instance.repository.save(player)
    }

    fun getTotalPlaytime(): Long {
        var time = 0L
        currentSession?.let {
            time += it.duration
        }
        sessions.forEach {
            time += it.duration
        }
        return time
    }

    fun getCurrentPlaytime(): Long {
        if (currentSession == null)
            return -1
        return System.currentTimeMillis() - currentSession!!.login
    }

    override fun compareTo(other: PlayTime): Int {
        return other.getTotalPlaytime().compareTo(getTotalPlaytime())
    }

}


class Session(val id: String, val login: Long, var logout: Long) : JsonSerializable {
    val duration: Long
        get() = if (logout == -1L) System.currentTimeMillis() - login else logout - login

    override fun toJson(json: JsonObject) {
        json.addProperty("id", this.id)
        json.addProperty("login", this.login)
        json.addProperty("logout", this.logout)
    }
}