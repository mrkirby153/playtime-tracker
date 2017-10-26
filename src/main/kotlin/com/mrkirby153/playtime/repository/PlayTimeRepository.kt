package com.mrkirby153.playtime.repository

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrkirby153.playtime.PlaytimeTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.DimensionManager
import java.io.File
import java.nio.charset.Charset
import java.util.*

class PlayTimeRepository {

    private val repo = mutableMapOf<UUID, PlayTime>()

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
        val sessions = playtime.sessions
        sessions.forEach {
            jsonArray.add(JsonObject().apply {
                addProperty("login", it.login)
                addProperty("logout", it.logout)
                addProperty("id", it.id)
            })
        }
        dataFile.writeText(jsonArray.toString())
    }

    fun load() {
        repo.clear()
        dataDirectory.listFiles().forEach {
            val uuid = UUID.fromString(it.nameWithoutExtension)
            val parser = JsonParser()
            val inputStream = it.reader(Charset.defaultCharset())
            val array = parser.parse(inputStream).asJsonArray

            val playtime = PlayTime(uuid)

            array.map { it.asJsonObject }.forEach {
                val id = it.get("id").asString
                PlaytimeTracker.instance.logger.info("Loading session $id")
                val session = Session(id, it.get("login").asLong, it.get("logout").asLong)
                playtime.sessions.add(session)
            }
            repo[uuid] = playtime
            inputStream.close()
        }
    }

    fun get(player: EntityPlayer): PlayTime {
        if (!repo.containsKey(player.uniqueID)) {
            repo[player.uniqueID] = PlayTime(player.uniqueID)
        }
        return repo[player.uniqueID]!!
    }
}