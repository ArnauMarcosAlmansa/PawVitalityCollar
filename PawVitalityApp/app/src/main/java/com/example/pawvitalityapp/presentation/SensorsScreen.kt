package com.example.pawvitalityapp.presentation

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.Composable
import com.example.pawvitalityapp.presentation.permissions.SystemBroadcastReceiver

@Composable
fun SensorsScreen(
    onBluetoothStateChanged:()->Unit
) {

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED){ bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if(action == BluetoothAdapter.ACTION_STATE_CHANGED){
            onBluetoothStateChanged()
        }
    }

}