package com.example.droidchiselploy.ui.screens

sealed class Screen(val route: String) {

    data object Dashboard: Screen("dashboard_screen")
}
