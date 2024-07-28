package com.imam.Doktor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import com.imam.Doktor.presentation.SplashScreen
import com.imam.Doktor.ui.theme.MaterialChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialChatTheme {
                Navigator(screen = SplashScreen()) {
                    FadeTransition(navigator = it)
                }
            }
        }
    }
}














