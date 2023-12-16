package com.pawvitality.pawvitalityapp.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ktx.database
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.pawvitality.pawvitalityapp.presentation.HeartRateData
import com.pawvitality.pawvitalityapp.presentation.LoginScreenState
import com.pawvitality.pawvitalityapp.presentation.TemperatureData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CloudFunctionsService @Inject constructor() {
    val functions = Firebase.functions
    val callSetupDatabase = functions.getHttpsCallable("setupDatabase")
    val callGetDataFeedback = functions.getHttpsCallable("getDataFeedback")
    val callSendData = functions.getHttpsCallable("sendData")

    fun setupDatabase(username: String): Task<HttpsCallableResult> {
        val data = hashMapOf(
            "username" to username,
        )

        return callSetupDatabase
            .call(data)
    }

    fun getDataFeedback(username: String): Task<Pair<TemperatureData, HeartRateData>> {
        val data = hashMapOf(
            "username" to username,
        )

        return callGetDataFeedback
            .call(data)
            .continueWith { task ->
                val result = task.result?.data as Map<*, *>

                val resultTemperature = result["temperature"] as Map<String, *>
                val tmp = parseTemperature(resultTemperature)
//                tmp.resting = (resultTemperature["resting"]?.toFloat() ?: 0.0f)
//                tmp.high = (resultTemperature["high"]?.toFloat() ?: 0.0f)

                val resultHeartRate = result["heartRate"] as Map<String, Float>
                val hrt = parseHeartRate(resultHeartRate)

                Log.d("FEEDBACK", "success")

                Pair(tmp, hrt)
            }.addOnFailureListener {
                Log.e("FEEDBACK", "exception", it)
            }
    }

    fun parseTemperature(resultTemperature: Map<String, *>): TemperatureData {
        val tmp = TemperatureData()

        Log.d("FEEDBACK", resultTemperature.toString())
        if (resultTemperature["current"] == null) {
            tmp.current = 0.0f;
        } else if (resultTemperature["current"] is Int?) {
            tmp.current = (resultTemperature["current"] as Int).toFloat()
        } else {
            tmp.current = (resultTemperature["current"] as Double).toFloat()
        }

        if (resultTemperature["high"] == null) {
            tmp.high = 0.0f;
        } else if (resultTemperature["high"] is Int?) {
            tmp.high = (resultTemperature["high"] as Int).toFloat()
        } else {
            tmp.high = (resultTemperature["high"] as Double).toFloat()
        }

        if (resultTemperature["resting"] == null) {
            tmp.resting = 0.0f;
        } else if (resultTemperature["resting"] is Int?) {
            tmp.resting = (resultTemperature["resting"] as Int).toFloat()
        } else {
            tmp.resting = (resultTemperature["resting"] as Double).toFloat()
        }

        return tmp
    }

    fun parseHeartRate(resultHeartRate: Map<String, *>): HeartRateData {
        val hr = HeartRateData()

        Log.d("FEEDBACK", resultHeartRate.toString())
        if (resultHeartRate["current"] == null) {
            hr.current = 0.0f;
        } else if (resultHeartRate["current"] is Int?) {
            hr.current = (resultHeartRate["current"] as Int).toFloat()
        } else {
            hr.current = (resultHeartRate["current"] as Double).toFloat()
        }

        if (resultHeartRate["high"] == null) {
            hr.high = 0.0f;
        } else if (resultHeartRate["high"] is Int?) {
            hr.high = (resultHeartRate["high"] as Int).toFloat()
        } else {
            hr.high = (resultHeartRate["high"] as Double).toFloat()
        }

        if (resultHeartRate["resting"] == null) {
            hr.resting = 0.0f;
        } else if (resultHeartRate["resting"] is Int?) {
            hr.resting = (resultHeartRate["resting"] as Int).toFloat()
        } else {
            hr.resting = (resultHeartRate["resting"] as Double).toFloat()
        }

        return hr
    }

    fun sendData(temperature: Float, heartRate: Int, breathRate: Int, moving: Boolean, barking: Boolean) {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val data = mapOf(
            "email" to LoginScreenState.email,
            "temperature" to temperature,
            "heartRate" to heartRate,
            "breathRate" to breathRate,
            "moving" to moving,
            "barking" to barking,
            "day" to dateFormat.format(Date()),
            "timestamp" to dateTimeFormat.format(Date()),
        )

        callSendData.call(data)
    }
}