package com.pawvitality.pawvitalityapp.presentation

import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.ktx.auth

class AuthController {
    var isAuthenticated: Boolean = false;

    fun authenticate(email: String, password: String): Boolean {
        isAuthenticated = email.isNotBlank() && password.isNotBlank()
        if (!isAuthenticated) {
            return false;
        }


        return isAuthenticated
    }

    fun isValidEmail(email : String): Boolean {
        return true
    }

    fun isValidPassword(password : String): Boolean {
        return password.length >= 6
    }

    fun signup(email: String, password: String) {

    }
}