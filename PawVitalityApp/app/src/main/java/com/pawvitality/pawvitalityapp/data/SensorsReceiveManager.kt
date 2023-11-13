package com.pawvitality.pawvitalityapp.data

import com.pawvitality.pawvitalityapp.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface SensorsReceiveManager {

    val data: MutableSharedFlow<Resource<SensorsResult>>


    fun reconnect()


    fun disconnect()


    fun startReceiving()


    fun closeConnection()

}