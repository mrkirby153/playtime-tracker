package com.mrkirby153.playtime.util

import com.google.gson.JsonObject

interface JsonSerializable {

    /**
     * Write the object to the given json
     *
     * @param json The json object to write to
     */
    fun toJson(json: JsonObject)
}