package com.kisansethu.app.data

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

class LocationRepository(private val context: Context) {

    private val locationsMap: Map<String, List<String>> by lazy {
        loadLocationsFromAssets()
    }

    private fun loadLocationsFromAssets(): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        try {
            context.assets.open("locations.json").use { inputStream ->
                val jsonString = InputStreamReader(inputStream).readText()
                val jsonObject = JSONObject(jsonString)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val state = keys.next()
                    val districtsArray = jsonObject.getJSONArray(state)
                    val districtsList = mutableListOf<String>()
                    for (i in 0 until districtsArray.length()) {
                        districtsList.add(districtsArray.getString(i))
                    }
                    map[state] = districtsList.sorted()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map.toSortedMap()
    }

    fun getStates(): List<String> {
        return locationsMap.keys.toList()
    }

    fun getDistrictsForState(state: String): List<String> {
        return locationsMap[state] ?: emptyList()
    }
}
