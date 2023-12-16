package com.pawvitality.pawvitalityapp.presentation

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.viewinterop.AndroidView
import com.pawvitality.pawvitalityapp.data.CloudFunctionsService
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


@Composable
fun StartScreen(
    navController: NavController,
    cloudFunctions: CloudFunctionsService,
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
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
                        text = "Resting: ${feedbackViewModel.heartRate.resting}", // Calcular la mitjana quan dels HR quan moving=False
                        color = Color.White
                    )
                    Text(
                        text = "Current: ${feedbackViewModel.heartRate.current}",
                        color = Color.White
                    )
                    Text(
                        text = "High: ${feedbackViewModel.heartRate.high}", // Pillar el max HR
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
                        text = "Resting: ${feedbackViewModel.temperature.resting} ºC",
                        color = Color.White
                    )
                    Text(
                        text = "Current: ${feedbackViewModel.temperature.current} ºC",
                        color = Color.White
                    )
                    Text(
                        text = "High: ${feedbackViewModel.temperature.high} ºC",
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
                    .height(300.dp))
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
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(75.dp)
                .clip(CircleShape)
                .background(Color.Blue, CircleShape)
                .clickable {
                    // TODO: Uncomment
                    navController.navigate(Screen.SensorsScreen.route) {
                        popUpTo(Screen.StartScreen.route) {
                            inclusive = true
                        }
                    }
//                    sendDataToFirebase(cloudFunctions, 7.2f, 2, 5, true, true)
                },
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "Start",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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
