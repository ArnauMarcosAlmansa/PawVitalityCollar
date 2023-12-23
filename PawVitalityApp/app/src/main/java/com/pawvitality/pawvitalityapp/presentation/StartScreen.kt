package com.pawvitality.pawvitalityapp.presentation

import android.bluetooth.BluetoothAdapter
import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions
import com.pawvitality.pawvitalityapp.data.ConnectionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pawvitality.pawvitalityapp.data.CloudFunctionsService
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.pawvitality.pawvitalityapp.presentation.permissions.PermissionUtils
import com.pawvitality.pawvitalityapp.presentation.permissions.SystemBroadcastReceiver


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartScreen(
    navController: NavController,
    onBluetoothStateChanged:()->Unit,
    cloudFunctions: CloudFunctionsService,
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    viewModel: SensorsViewModel = hiltViewModel()
) {
    val mainHandler by remember { mutableStateOf(Handler(Looper.getMainLooper())) }
    LaunchedEffect(key1 = LoginScreenState.email) {
        mainHandler.post(object : Runnable {
            override fun run() {
                cloudFunctions.getDataFeedback(LoginScreenState.email)
                    .continueWith {
                        val (tmp, hrt) = it.result
                        feedbackViewModel.temperature = tmp
                        feedbackViewModel.heartRate = hrt
                    }

                cloudFunctions.getLastHourDataData(LoginScreenState.email)
                    .continueWith {
                        feedbackViewModel.lastHourEntries = it.result
                        Log.d("LASTHOUR", it.result.toString())
                    }

                mainHandler.postDelayed(this, 10000)
            }
        })
    }

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED){ bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if(action == BluetoothAdapter.ACTION_STATE_CHANGED){
            onBluetoothStateChanged()
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState = viewModel.connectionState


    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver{_, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionState.launchMultiplePermissionRequest()
                if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
                    viewModel.reconnect()
                }
            }

            if (event == Lifecycle.Event.ON_STOP) {
                if (bleConnectionState == ConnectionState.Connected) {
                    viewModel.disconnect()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    } )

    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            if (bleConnectionState == ConnectionState.Uninitialized) {
                viewModel.initializeConnection()
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(0.dp, 150.dp, 0.dp, 0.dp),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Heart Rate Box
            Box(
                modifier = Modifier
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Heart Rate",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Column (
                    modifier = Modifier
                        .padding(20.dp)
                ){
                    Text(
                        text = "Resting: ${String.format("%.1f", feedbackViewModel.heartRate.resting)}", // Calcular la mitjana quan dels HR quan moving=False
                        color = Color.White
                    )
                    Text(
                        text = "Current: ${String.format("%.1f", feedbackViewModel.heartRate.current)}",
                        color = Color.White
                    )
                    Text(
                        text = "High: ${String.format("%.1f", feedbackViewModel.heartRate.high)}", // Pillar el max HR
                        color = Color.White
                    )
                }
            }

            // Spacer to create some space between the two boxes
            Spacer(modifier = Modifier.height(16.dp))

            // Temperature Box
            Box(
                modifier = Modifier
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Temperature",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Column (
                    modifier = Modifier
                        .padding(20.dp)
                ){
                    Text(
                        text = "Resting: ${String.format("%.1f", feedbackViewModel.temperature.resting)} ºC",
                        color = Color.White
                    )
                    Text(
                        text = "Current: ${String.format("%.1f", feedbackViewModel.temperature.current)} ºC",
                        color = Color.White
                    )
                    Text(
                        text = "High: ${String.format("%.1f", feedbackViewModel.temperature.high)} ºC",
                        color = Color.White
                    )
                }
            }

            key(feedbackViewModel.lastHourEntries) {
                AndroidView(factory = {
                    val chart = LineChart(it)
                    val values = LineDataSet(feedbackViewModel.lastHourEntries.mapIndexed { index, dataEntry ->  Entry(index.toFloat(), dataEntry.temp)}, "Temperature")
                    val series = listOf(values)
                    val data = LineData(series)
                    chart.data = data
                    chart
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp))
            }
        }
    }
    // Header
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ){
        if (bleConnectionState == ConnectionState.CurrentlyInitializing) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                if (viewModel.initializingMessage != null) {
                    Text(
                        text = viewModel.initializingMessage!!
                    )
                }
            }
        } else if (!permissionState.allPermissionsGranted) {
            Text(
                text = "Go to the app settings and allow the missing permissions",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(10.dp),
                textAlign = TextAlign.Center
            )
        } else if (viewModel.errorMessage != null) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.errorMessage!!
                )
                Button (onClick = {
                    if (permissionState.allPermissionsGranted) {
                        viewModel.initializeConnection()
                    }
                }) {
                    Text(
                        text = "Try again"
                    )
                }
            }
        } else if (bleConnectionState == ConnectionState.Connected) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bluetooth Feed",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Heart Rate: ${viewModel.heartRate} bpm",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Temperature: ${viewModel.temperature} ºC",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Breath rate: ${viewModel.breathRate}",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Barking: ${yesNo(viewModel.barking)}",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Moving: ${yesNo(viewModel.moving)}",
                    style = MaterialTheme.typography.h6
                )
            }
        } else if (bleConnectionState == ConnectionState.Disconnected) {
            Button(onClick = {
                viewModel.initializeConnection()
            }) {
                Text(text = "Initialize again")
            }
        }
        Divider(
            color = Color.Blue,
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 0.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

private fun sendDataToFirebase(cloudFunctions: CloudFunctionsService,
    temperature: Float, heartRate: Int, breathRate: Int
    , moving: Boolean, barking: Boolean)
{
    cloudFunctions.sendData(temperature, heartRate, breathRate, moving, barking)

    return

    val db = Firebase.firestore
    val data = hashMapOf(
        "temperature" to temperature,
        "heartRate" to heartRate,
        "breathRate" to breathRate,
        "moving" to moving,
        "barking" to barking
    )
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    val sensorsDataRef = db.collection("SensorsData")
    val usernameRef = sensorsDataRef.document(LoginScreenState.email)
    val dateRef = usernameRef.collection(date).document(time)
    dateRef
        .set(data)
        .addOnSuccessListener { Log.d(TAG, "Document successfully written!") }
        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
}
