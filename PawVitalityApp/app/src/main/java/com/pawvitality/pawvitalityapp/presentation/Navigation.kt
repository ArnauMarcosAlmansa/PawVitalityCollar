package com.pawvitality.pawvitalityapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember


@Composable
fun Navigation(
    onBluetoothStateChanged:()->Unit
) {

    val navController = rememberNavController()
    val authController by remember { mutableStateOf(AuthController()) }
    
    NavHost(navController = navController, startDestination = Screen.LoginScreen.route){
        composable(Screen.StartScreen.route){
            StartScreen(navController = navController)
        }

        composable(Screen.SensorsScreen.route){
            SensorsScreen(
                onBluetoothStateChanged
            )
        }

        composable(Screen.LoginScreen.route){
            LoginScreen(
                navController = navController,
                authController = authController
            )
        }

        composable(Screen.SignupScreen.route){
            SignupScreen(
                navController = navController,
                authController = authController
            )
        }
    }

}

sealed class Screen(val route:String){
    object StartScreen:Screen("start_screen")
    object SensorsScreen:Screen("sensors_screen")
    object LoginScreen:Screen("login_screen")
    object SignupScreen:Screen("signup_screen")
}