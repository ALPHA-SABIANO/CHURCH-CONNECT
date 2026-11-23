package com.example.churchconnect.ui.navigation

sealed class ScreenRoutes(val route: String) {
    object Splash : ScreenRoutes("splash")
    object SelectChurch : ScreenRoutes("select_church")
    object ChurchSpace : ScreenRoutes("church_space")
    object AdminAuth : ScreenRoutes("admin_auth")
    object ManageChurch : ScreenRoutes("manage_church")
}
