package com.example.droidchiselploy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.droidchiselploy.ui.NavigationGraph
import com.example.droidchiselploy.ui.theme.droidchiselployTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            droidchiselployTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MainScreen()
                }
            }
        }
    }

}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold { paddingValues ->
        NavigationGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}
