package com.pawvitality.pawvitalityapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


class HeartRateData {
    var resting: Float = 0.0f
    var current: Float = 0.0f
    var high: Float = 0.0f
}


class TemperatureData {
    var resting: Float = 0.0f
    var current: Float = 0.0f
    var high: Float = 0.0f
}



@HiltViewModel
class FeedbackViewModel @Inject constructor(): ViewModel() {
    var temperature by mutableStateOf(TemperatureData())
    var heartRate by mutableStateOf(HeartRateData())
}
