package com.example.pawvitalityapp.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawvitalityapp.data.ConnectionState
import com.example.pawvitalityapp.data.SensorsReceiveManager
import com.example.pawvitalityapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    private val sensorsReceiveManager: SensorsReceiveManager
) : ViewModel(){

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var temperature by mutableStateOf(0f)
        private set

    var heartRate by mutableStateOf(0)
        private set

    var breathRate by mutableStateOf(0)
        private set

    var barking by mutableStateOf(false)
        private set

    var moving by mutableStateOf(false)
        private set


    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    private fun subscribeToChanges(){
        viewModelScope.launch {
            sensorsReceiveManager.data.collect{ result ->
                when (result) {
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        temperature = result.data.temperature
                        heartRate = result.data.heartRate
                        breathRate = result.data.breathRate
                        moving = result.data.moving
                        barking = result.data.barking

                        Log.d("APP SUCCESS", "${temperature}")
                    }
                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing

                        Log.d("APP LOADING", initializingMessage!!)
                    }
                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized

                        Log.e("APP ERROR", errorMessage!!)
                    }
                }
            }
        }
    }

    fun disconnect() {
        sensorsReceiveManager.disconnect()
    }

    fun reconnect() {
        sensorsReceiveManager.reconnect()
    }

    fun initializeConnection() {
        errorMessage = null
        subscribeToChanges()
        sensorsReceiveManager.startReceiving()
    }

    override fun onCleared() {
        super.onCleared()
        sensorsReceiveManager.closeConnection()
    }
}