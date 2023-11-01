package com.example.pawvitalityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pawvitalityapp.presentation.Navigation
import com.example.pawvitalityapp.ui.theme.PawVitalityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PawVitalityAppTheme {
                Navigation()
            }
        }
    }
}