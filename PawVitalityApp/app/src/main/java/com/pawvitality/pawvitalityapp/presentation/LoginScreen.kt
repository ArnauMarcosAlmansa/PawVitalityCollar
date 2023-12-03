package com.pawvitality.pawvitalityapp.presentation

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.play.integrity.internal.l


object LoginScreenState {
    var email: String = "";
    var password: String = "";
}

@Composable
fun LoginScreen(
    navController: NavController, authController: AuthController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // TODO: e
//    if (viewModel.auth.currentUser != null) {
//        LoginScreenState.email = viewModel.auth.currentUser!!.email.toString()
//        navController.navigate("start_screen")
//    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            OutlinedTextField(value = viewModel.email.value,
                onValueChange = { viewModel.email.value = it; Log.d("EMAIL INPUT", it) },
                label = {
                    Text(text = "Email")
                })
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.value = it },
                label = {
                    Text(text = "Password")
                },
                visualTransformation = PasswordVisualTransformation()
            )
            viewModel.errorMessage.value?.let {
                Text(text = it, color = Color.Red)
            }
            Button(
                modifier = Modifier.width(300.dp),
                onClick = {
                    viewModel.login(
                        viewModel.email.value,
                        viewModel.password.value
                    ) {
                        navController.navigate("start_screen")
                        LoginScreenState.email = viewModel.email.value
                        LoginScreenState.password = viewModel.password.value
                    }
                }
            ) {
                Text(text = "Login", fontSize = 20.sp)
            }
            Text(text = "Don't have an account?")
            Button(onClick = { navController.navigate("signup_screen") }) {
                Text(text = "Sign up")
            }
        }
    }
}