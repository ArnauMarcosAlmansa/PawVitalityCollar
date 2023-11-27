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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class LoginScreenState(val navController: NavController): ViewModel() {
    var auth = Firebase.auth;

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("LOGIN", "login ok")
                            navController.navigate("start_screen")
                        } else {
                            Log.d("LOGIN", "login fail")
                        }
                    }
            } catch (ex: Exception) {
                Log.d("LOGIN", "login exception")
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, authController: AuthController) {
    val state by remember {
        mutableStateOf(LoginScreenState(navController))
    }

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
                    state.login(
                        email,
                        password
                    )
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