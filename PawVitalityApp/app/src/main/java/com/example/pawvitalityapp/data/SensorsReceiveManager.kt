package com.example.pawvitalityapp.data

import com.example.pawvitalityapp.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface SensorsReceiveManager {

    val data: MutableSharedFlow<Resource<SensorsResult>>


    fun reconnect()


    fun disconnect()


    fun startReceiving()


    fun closeConnection()

}