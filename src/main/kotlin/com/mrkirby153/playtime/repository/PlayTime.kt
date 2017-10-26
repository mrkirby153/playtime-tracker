package com.mrkirby153.playtime.repository

import com.mrkirby153.playtime.PlaytimeTracker
import java.util.*

class PlayTime(val player: UUID) {
    val sessions = mutableListOf<Session>()

    var currentSession: Session? = null


    fun startNewSession() {
        if (currentSession != null) {
            throw IllegalStateException("Attempting to start a new session while one is already active!")
        }
        currentSession = Session(PlaytimeTracker.instance.generateId(10), System.currentTimeMillis(), -1)
    }

    fun endCurrentSession() {
        if (currentSession == null)
            throw IllegalStateException("Attempting to end a session when one hasn't started!")
        currentSession?.logout = System.currentTimeMillis()
        if (currentSession != null)
            sessions.add(currentSession!!)
        currentSession = null
    }

    fun getTotalPlaytime(): Long {
        var time = 0L
        sessions.forEach {
            time += it.duration
        }
        return time
    }
}


class Session(val id: String, val login: Long, var logout: Long) {
    val duration: Long
        get() = if (logout == -1L) System.currentTimeMillis() - login else logout - login
}