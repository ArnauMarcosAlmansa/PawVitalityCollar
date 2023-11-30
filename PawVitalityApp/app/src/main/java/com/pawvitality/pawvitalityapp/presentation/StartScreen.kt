package com.pawvitality.pawvitalityapp.presentation

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
    val database = Firebase.database("https://pawvitality-default-rtdb.europe-west1.firebasedatabase.app")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timestamp = dateFormat.format(Date())
    val dbRef = database.reference.child(LoginScreenState.email).child(timestamp)
    val data = mapOf(
        "temperature" to temperature,
        "heartRate" to heartRate,
        "breathRate" to breathRate,
        "moving" to moving,
        "barking" to barking
    )
    dbRef.setValue(data)
}





