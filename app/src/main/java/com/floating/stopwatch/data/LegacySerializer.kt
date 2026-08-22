package com.floating.stopwatch.data

import com.floating.stopwatch.domain.Legacy
import org.json.JSONArray
import org.json.JSONObject

object LegacySerializer {

    fun toJson(legacies: List<Legacy>): String {
        val array = JSONArray()
        for (legacy in legacies) {
            val obj = JSONObject().apply {
                put("id", legacy.id)
                put("name", legacy.name)
                put("targetDurationMs", legacy.targetDurationMs)
                put("totalDays", legacy.totalDays)
                put("dailyTargetMs", legacy.dailyTargetMs)
                put("startDateMs", legacy.startDateMs)
                put("targetDateMs", legacy.targetDateMs)
                put("accumulatedTimeMs", legacy.accumulatedTimeMs)
                put("manualTimeMs", legacy.manualTimeMs)
                put("postponedDays", legacy.postponedDays)
                put("isCompleted", legacy.isCompleted)

                val mapObj = JSONObject()
                legacy.dailyProgressMap.forEach { (dateKey, valMs) ->
                    mapObj.put(dateKey, valMs)
                }
                put("dailyProgressMap", mapObj)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToLegacies(json: String): List<Legacy> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<Legacy>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val name = obj.optString("name", "Legacy")
                val targetDurationMs = obj.optLong("targetDurationMs", 0L)
                val totalDays = obj.optInt("totalDays", 1)
                val dailyTargetMs = obj.optLong("dailyTargetMs", 0L)
                val startDateMs = obj.optLong("startDateMs", System.currentTimeMillis())
                val targetDateMs = obj.optLong("targetDateMs", System.currentTimeMillis())
                val accumulatedTimeMs = obj.optLong("accumulatedTimeMs", 0L)
                val manualTimeMs = obj.optLong("manualTimeMs", 0L)
                val postponedDays = obj.optInt("postponedDays", 0)
                val isCompleted = obj.optBoolean("isCompleted", false)

                val dailyMap = mutableMapOf<String, Long>()
                if (obj.has("dailyProgressMap")) {
                    val mapObj = obj.getJSONObject("dailyProgressMap")
                    val keys = mapObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        dailyMap[key] = mapObj.getLong(key)
                    }
                }

                if (id.isNotBlank()) {
                    list.add(
                        Legacy(
                            id = id,
                            name = name,
                            targetDurationMs = targetDurationMs,
                            totalDays = totalDays,
                            dailyTargetMs = dailyTargetMs,
                            startDateMs = startDateMs,
                            targetDateMs = targetDateMs,
                            accumulatedTimeMs = accumulatedTimeMs,
                            manualTimeMs = manualTimeMs,
                            postponedDays = postponedDays,
                            isCompleted = isCompleted,
                            dailyProgressMap = dailyMap
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
