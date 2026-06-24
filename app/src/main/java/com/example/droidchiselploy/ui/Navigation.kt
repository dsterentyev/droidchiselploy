package com.example.droidchiselploy.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.droidchiselploy.ui.screens.DashboardScreen
import com.example.droidchiselploy.ui.screens.Screen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                paddingValues = paddingValues
            )
        }

    }
}
