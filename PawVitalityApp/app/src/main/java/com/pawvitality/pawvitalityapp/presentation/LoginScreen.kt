package com.pawvitality.pawvitalityapp.presentation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


class LoginScreenState {
    var email: String = "";
    var password: String = "";
}

@Composable
fun LoginScreen(navController: NavController, authController: AuthController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            OutlinedTextField(value = email,
                onValueChange = { email = it; Log.d("EMAIL INPUT", it) },
                label = {
                    Text(text = "Email")
                })
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(text = "Password")
                },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                modifier = Modifier.width(300.dp),
                onClick = {
                    if (authController.authenticate(
                            email,
                            password
                        )
                    ) {
                        navController.navigate("start_screen")
                    }
                }
            ) {
                Text(text = "Sign up", fontSize = 20.sp)
            }
            Text(text = "Don't have an account?")
            Button(onClick = { navController.navigate("signup_screen") }) {
                Text(text = "Sign up")
            }
        }
    }
}