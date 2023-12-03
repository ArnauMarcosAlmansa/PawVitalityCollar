package com.pawvitality.pawvitalityapp.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.pawvitality.pawvitalityapp.presentation.HeartRateData
import com.pawvitality.pawvitalityapp.presentation.LoginScreenState
import com.pawvitality.pawvitalityapp.presentation.TemperatureData

class CloudFunctionsService {
    val functions = Firebase.functions
    val callSetupDatabase = functions.getHttpsCallable("setupDatabase")
    val callGetDataFeedback = functions.getHttpsCallable("getDataFeedback")

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

                val resultTemperature = result["temperature"] as Map<String, Float>
                val tmp = TemperatureData()
                tmp.current = (resultTemperature["current"] as Float)
                tmp.resting = (resultTemperature["resting"] as Float)
                tmp.high = (resultTemperature["high"] as Float)

                val resultHeartRate = result["heartRate"] as Map<String, Float>
                val hrt = HeartRateData()
                hrt.current = (resultHeartRate["current"] as Float)
                hrt.resting = (resultHeartRate["resting"] as Float)
                hrt.high = (resultHeartRate["high"] as Float)

                Log.d("FEEDBACK", "success")

                Pair(tmp, hrt)
            }.addOnFailureListener {
                Log.e("FEEDBACK", "exception", it)
            }
    }
}