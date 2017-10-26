package com.mrkirby153.playtime.repository

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.DimensionManager
import java.io.File
import java.nio.charset.Charset
import java.util.*

class UsernameRepository {

    private val dataDirectory = File(DimensionManager.getCurrentSaveRootDirectory(), "playtime")

    val repo = mutableMapOf<UUID, String>()

    fun update(player: EntityPlayer) {
        repo.put(player.uniqueID, player.name)
        save()
    }

    fun save() {
        val dataFile = File(dataDirectory, "uuidMaps.json")
        val jsonObject = JsonObject()
        repo.forEach {
            jsonObject.addProperty(it.key.toString(), it.value)
        }
        dataFile.writeText(jsonObject.toString())
    }

    fun load() {
        val dataFile = File(dataDirectory, "uuidMaps.json")
        if(!dataFile.exists()) {
            dataFile.createNewFile()
            dataFile.writeText("{}")
        }
        val `is` = dataFile.reader(Charset.defaultCharset())
        JsonParser().parse(`is`).asJsonObject.entrySet().forEach {
            repo.put(UUID.fromString(it.key), it.value.asString)
        }
    }

    fun getUUID(name: String): UUID? {
        repo.forEach {
            if (it.value.equals(name, true))
                return it.key
        }
        return null
    }

    fun getName(uuid: UUID) = repo[uuid]
}