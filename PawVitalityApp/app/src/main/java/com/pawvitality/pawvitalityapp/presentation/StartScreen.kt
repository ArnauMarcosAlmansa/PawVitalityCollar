package com.pawvitality.pawvitalityapp.presentation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StartScreen(
    navController: NavController
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.Blue, CircleShape)
                .clickable {
                    // TODO: Uncomment
                    //navController.navigate(Screen.SensorsScreen.route){
                    //   popUpTo(Screen.StartScreen.route){
                    //      inclusive = true
                    //}
                    //}
                    sendDataToFirebase(7.2, 2, 5, true, true)
                },
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "Start",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun sendDataToFirebase(
    temperature: Double, heartRate: Int, breathRate: Int
    , moving: Boolean, barking: Boolean)
{
    val db = Firebase.firestore
    val data = hashMapOf(
        "temperature" to temperature,
        "heartRate" to heartRate,
        "breathRate" to breathRate,
        "moving" to moving,
        "barking" to barking
    )
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    val sensorsDataRef = db.collection("SensorsData")
    val usernameRef = sensorsDataRef.document(LoginScreenState.email)
    val dateRef = usernameRef.collection(date).document(time)
    dateRef
        .set(data)
        .addOnSuccessListener { Log.d(TAG, "Document successfully written!") }
        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
}





