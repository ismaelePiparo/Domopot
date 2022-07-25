package com.example.domopotapp

import java.time.LocalDateTime

data class PotData(
    var id: String,
    var name: String,
    var type: String? = null,
    var image: String,

    var manualMode: Boolean = false,
    var connectionStatus: Boolean = false,

    var humidity: Int,
    var waterLevel: Int,
    var temperature: Int,
    var lastWatering: LocalDateTime,

    var commandMode: String = "Humidity",   //accepted values: Humidity, Immediate, Program
    var humidityThreshold: Int,
    var programTiming: Long = -1,
    var waterQuantity: Int = 50
) {
    override fun equals(other: Any?): Boolean {
        val o = other as PotData
        //Log.i("PotData", this.toString() + "\n" + o.toString())
        return when {
            id != o.id -> false
            name != o.name -> false
            type != o.type -> false
            image != o.image -> false
            manualMode != o.manualMode -> false
            connectionStatus != o.connectionStatus -> false
            humidity != o.humidity -> false
            waterLevel != o.waterLevel -> false
            temperature != o.temperature -> false
            lastWatering != o.lastWatering -> false
            commandMode != o.commandMode -> false
            humidityThreshold != o.humidityThreshold -> false
            programTiming != o.programTiming -> false
            waterQuantity != o.waterQuantity -> false
            else -> true
        }
    }

    override fun toString(): String {
        return "$id\n" +
                "$name\n" +
                "$type\n" +
                "$image\n" +
                "$manualMode\n" +
                "$connectionStatus\n" +
                "$humidity\n" +
                "$waterLevel\n" +
                "$temperature\n" +
                "$lastWatering\n" +
                "$commandMode\n" +
                "$humidityThreshold\n" +
                "$programTiming\n" +
                "$waterQuantity"
    }
}

data class PlantTypeData(
    val id: String,
    val name: String,
    val img: String,
    val difficulty: Int,
    val humidityThreshold: Int,
    val description: String
)
