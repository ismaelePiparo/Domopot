package com.example.domopotapp

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
    var lastWatering: Int,

    var commandMode: String = "Humidity",   //accepted values: Humidity, Immediate, Program
    var humidityThreshold: Int,
    var programTiming: Int = -1,
)

data class PlantTypeData(
    val id: String,
    val name: String,
    val img: String,
    val difficulty: Int,
    val humidityThreshold: Int,
    val description: String
)
