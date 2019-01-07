package com.mrkirby153.playtime.repository

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrkirby153.playtime.PlaytimeTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.DimensionManager
import java.io.File
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.TimeUnit

class PlayTimeRepository {

    val repo = mutableMapOf<UUID, PlayTime>()

    private val dataDirectory = File(DimensionManager.getCurrentSaveRootDirectory(), "playtime")

    init {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
    }

    fun save() {
        repo.forEach {
            save(it.key, it.value)
        }
    }

    fun save(uuid: UUID) {
        repo[uuid]?.let {
            save(uuid, it)
        }
    }

    fun save(uuid: UUID, playtime: PlayTime) {
        val dataFile = File(dataDirectory, "$uuid.json")
        val jsonArray = JsonArray()
        if (!dataFile.exists())
            dataFile.createNewFile()
        val obj = JsonObject()
        val currentSession = playtime.currentSession
        obj.add("current", if (currentSession != null) JsonObject().apply {
            currentSession.toJson(this)
            addProperty("heartbeat", playtime.sessionHeartbeat)
        } else null)
        playtime.sessions.forEach {
            jsonArray.add(JsonObject().apply {
                it.toJson(this)
            })
        }
        obj.add("sessions", jsonArray)
        dataFile.writeText(obj.toString())
    }

    fun load() {
        PlaytimeTracker.instance.logger.info("Loading playtime repository")
        repo.clear()
        dataDirectory.listFiles().forEach { file ->
            if (file.nameWithoutExtension == "uuidMaps")
                return@forEach
            try {
                val uuid = UUID.fromString(file.nameWithoutExtension)
                val parser = JsonParser()
                val inputStream = file.reader(Charset.defaultCharset())
                val obj = parser.parse(inputStream).asJsonObject
                val playtime = PlayTime(uuid)

                val cur = obj.get("current")
                if(!cur.isJsonNull) {
                    playtime.currentSession = deserializeSession(cur.asJsonObject)
                    playtime.sessionHeartbeat = cur.asJsonObject.get("heartbeat").asLong
                }
                obj.get("sessions").asJsonArray.map { it.asJsonObject }.forEach { sessionJson ->
                    playtime.sessions.add(deserializeSession(sessionJson))
                }
                repo[uuid] = playtime
                inputStream.close()
            } catch (e: IllegalArgumentException) {
                // Ignore
            }
        }
    }

    fun discardActiveSessions(duration: Long = 5L, period: TimeUnit = TimeUnit.MINUTES) {
        PlaytimeTracker.instance.logger.info("Discarding stale active sessions")
        val time = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(duration, period)
        this.repo.values.forEach {
            if (it.sessionHeartbeat < time && it.currentSession != null) { // Last heartbeat was too long ago, discard
                PlaytimeTracker.instance.logger.info("Discarding session for ${it.player} - Heartbeat timeout")
                it.currentSession = null
            }
        }
        save()
    }

    fun get(player: EntityPlayer): PlayTime {
        if (!repo.containsKey(player.uniqueID)) {
            repo[player.uniqueID] = PlayTime(player.uniqueID)
        }
        return repo[player.uniqueID]!!
    }

    private fun deserializeSession(obj: JsonObject) = Session(obj.get("id").asString,
            obj.get("login").asLong, obj.get("logout").asLong)
}