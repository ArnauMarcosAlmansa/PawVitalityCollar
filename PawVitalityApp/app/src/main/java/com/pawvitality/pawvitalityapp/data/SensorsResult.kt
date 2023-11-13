package com.pawvitality.pawvitalityapp.data

data class SensorsResult(
    val temperature:Float,
    val heartRate: Int,
    val breathRate: Int,
    val moving: Boolean,
    val barking: Boolean,
    val connectionState: ConnectionState,
)
