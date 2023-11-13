package com.pawvitality.pawvitalityapp.data.ble;

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context;
import android.util.Log
import com.pawvitality.pawvitalityapp.data.ConnectionState
import com.pawvitality.pawvitalityapp.data.SensorsReceiveManager
import com.pawvitality.pawvitalityapp.data.SensorsResult
import com.pawvitality.pawvitalityapp.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject;

@SuppressLint("MissingPermission")
class SensorsBLEReceiveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context:Context
) : SensorsReceiveManager {

    // MIRAR AL USER MANUAL DEL DEVICE ELS CODIS
    private val DEVICE_NAME = "PawVitality"
//    private val TEMP_SERVICE_UUID = "0000xxxx-0000-1000-8000-00805f9b34fb"
    private val TEMP_SERVICE_UUID = "0000bde2-081b-4f83-bde2-753e72b34f84"
//    private val TEMP_CHARACTERISTICS_UUID = "0000xxxx-0000-1000-8000-00805f9b34fb"
    private val TEMP_CHARACTERISTICS_UUID = "0000bde2-081b-4f83-bde2-753e72b34f84"

    override val data: MutableSharedFlow<Resource<SensorsResult>> = MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.device.name == DEVICE_NAME){
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device...") )
                }
                if(isScanning){
                    result.device.connectGatt(context,false, gattCallback)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private var MAXIMUM_CONNECTION_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services..."))
                    }
                    gatt.discoverServices()
                    this@SensorsBLEReceiveManager.gatt = gatt
                }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = SensorsResult(0f, 0, 0, false, false, ConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            }else{
                gatt.close()
                currentConnectionAttempt+=1
                coroutineScope.launch {
                    data.emit(
                        Resource.Loading(
                            message = "Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"
                        )
                    )
                }
                if(currentConnectionAttempt<=MAXIMUM_CONNECTION_ATTEMPTS){
                    startReceiving()
                }else{
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to ble device"))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()
//                coroutineScope.launch {
//                    data.emit(Resource.Loading(message = "Adjusting MTU space..."))
//                }
//                gatt.requestMtu(20)

                val characteristic = findCharacteristics(TEMP_SERVICE_UUID, TEMP_CHARACTERISTICS_UUID)
                if(characteristic == null){
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not find sensors publisher"))
                    }
                    return
                } else {
                    coroutineScope.launch {
                        data.emit(Resource.Success(
                            SensorsResult(
                                0.0f,
                                0,
                                0,
                                false,
                                false,
                                ConnectionState.Connected
                            )
                        ))
                    }
                }

                enableNotification(characteristic)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val characteristic = findCharacteristics(TEMP_SERVICE_UUID, TEMP_CHARACTERISTICS_UUID)
            if(characteristic == null){
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find temp publisher"))
                }
                return
            }
            enableNotification(characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            with(characteristic){
                when(uuid){
                    UUID.fromString(TEMP_CHARACTERISTICS_UUID) -> {

                        Log.d("VALUE[0]", value[0].toString())
                        Log.d("VALUE[1]", value[1].toString())
                        Log.d("VALUE[2]", value[2].toString())
                        Log.d("VALUE[3]", value[3].toString())
                        Log.d("VALUE[4]", value[4].toString())
                        Log.d("VALUE[5]", value[5].toString())
                        Log.d("VALUE[6]", value[6].toString())


                        // MIRAR AL MANUAL EL FORMAT DE LA TEMPERATURA
                        val multiplicator = if(value.first().toInt()> 0) -1 else 1
                        val temperature = value[1].toInt() + value[2].toInt() / 10f
                        val heartRate = value[3].toInt()
                        val breathRate = value[4].toInt()
                        val moving = value[5].toInt() > 0
                        val barking = value[6].toInt() > 0

                        val sensorsResult = SensorsResult(
                            multiplicator * temperature,
                            heartRate,
                            breathRate,
                            moving,
                            barking,
                            ConnectionState.Connected
                        )
                        coroutineScope.launch {
                            data.emit(
                                Resource.Success(data = sensorsResult)
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun enableNotification(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, true) == false){
                Log.d("BLEReceiveManager", "set characteristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        gatt?.let { gatt ->
            gatt.writeDescriptor(descriptor, payload)
        }?: error("Not connected to a BLE device!")
    }

    private fun findCharacteristics(serviceUUID:String, characteristicUUID:String):BluetoothGattCharacteristic?{
        return gatt?.services?.find { service ->
            Log.d("service UUID", service.uuid.toString())
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            Log.d("characteristics UUID", characteristics.uuid.toString())
            characteristics.uuid.toString() == characteristicUUID
        }

    }

    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning for devices..."))
        }
        isScanning = true
        bleScanner.startScan(null,scanSettings,scanCallback)
    }


    override fun reconnect() {
        gatt?.connect()
    }


    override fun disconnect() {
        gatt?.disconnect()
    }


    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        val characteristic = findCharacteristics(TEMP_SERVICE_UUID, TEMP_CHARACTERISTICS_UUID)
        if(characteristic != null){
            disconnectCharacteristic(characteristic)
        }
        gatt?.close()
    }

    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, false) == false){
                Log.d("TempReceiveManager", "set characteristics notification faile")
                return
            }
            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

}
