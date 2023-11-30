package com.pawvitality.pawvitalityapp.presentation

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(): ViewModel() {
    val email: MutableState<String> = mutableStateOf("")
    val password: MutableState<String> = mutableStateOf("")

    var auth = Firebase.auth;
    var errorMessage: MutableState<String?> = mutableStateOf(null)

    fun login(email: String, password: String, goToStart : () -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("LOGIN", "login ok")
                            goToStart()
                        } else {
                            Log.d("LOGIN", "login fail")
                            errorMessage.value = "Invalid email or password"
                        }
                    }
            } catch (ex: Exception) {
                Log.d("LOGIN", "login exception")
                errorMessage.value = "Unexpected error"
            }
        }
    }

    fun signup(email: String, password: String, goToStart : () -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("SIGNUP", "signup ok")
                            goToStart()
                        } else {
                            Log.d("SIGNUP", "signup fail")
                            errorMessage.value = "Invalid email or password"
                        }
                    }
            } catch (ex: Exception) {
                Log.d("SIGNUP", "signup exception")
                errorMessage.value = "Unexpected error"
            }
        }
    }


}