package com.exemplo.oficina.data.remote

import com.google.gson.JsonParser

object ErrorJsonParser {
    fun extractErrorMessage(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val root = JsonParser.parseString(json).asJsonObject
            root.get("erro")?.takeIf { !it.isJsonNull }?.asString
        } catch (_: Exception) {
            null
        }
    }
}
