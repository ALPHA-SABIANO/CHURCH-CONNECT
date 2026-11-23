package com.example.churchconnect

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onStart = {
                if (ChurchRepository.currentUid() != null) {
                    nav.navigate("home") { popUpTo("splash") { inclusive = true } }
                } else {
                    nav.navigate("auth")
                }
            })
        }

        composable("auth") {
            AuthScreen(
                onBack = { nav.popBackStack() },
                onSuccess = { nav.navigate("home") { popUpTo("splash") { inclusive = true } } }
            )
        }

        composable("home") {
            HomeScreen(
                onManage = { nav.navigate("manage") },
                onOpenDetail = { collection, id -> nav.navigate("detail/$collection/$id") },
                onNotificationClick = { nav.navigate("notifications") },
                onLogout = {
                    ChurchRepository.logout()
                    nav.navigate("splash") { popUpTo("home") { inclusive = true } }
                }
            )
        }

        composable("notifications") {
            NotificationsScreen(
                onBack = { nav.popBackStack() },
                onOpenDetail = { collection, id -> nav.navigate("detail/$collection/$id") }
            )
        }

        composable("pending_questions") {
            PendingQuestionsScreen(
                onBack = { nav.popBackStack() },
                onOpenDetail = { collection, id -> nav.navigate("detail/$collection/$id") }
            )
        }

        composable("detail/{collection}/{id}") { entry ->
            DetailScreen(
                collection = entry.arguments?.getString("collection") ?: "announcements",
                itemId = entry.arguments?.getString("id") ?: "",
                onBack = { nav.popBackStack() }
            )
        }

        composable("manage") {
            ManageHomeScreen(
                onBack = { nav.popBackStack() },
                onOpen = { route -> nav.navigate(route) },
                onLogout = {
                    ChurchRepository.logout()
                    nav.navigate("splash") { popUpTo("home") { inclusive = true } }
                }
            )
        }

        composable("manage/{section}") { entry ->
            ManageListScreen(
                section = entry.arguments?.getString("section") ?: "ann",
                onBack = { nav.popBackStack() }
            )
        }

        composable("members") { MembersScreen(onBack = { nav.popBackStack() }) }

        composable("attendance") { AttendanceScreen(onBack = { nav.popBackStack() }) }
    }
}
