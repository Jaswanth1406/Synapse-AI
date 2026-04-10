package com.example.synapseai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.synapseai.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToDialer = { navController.navigate(Screen.Dialer.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToSchedule = { navController.navigate(Screen.ScheduleCall.route) }
            )
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(
                onCallContact = { phoneNumber ->
                    navController.navigate(Screen.DialerWithNumber.createRoute(phoneNumber))
                }
            )
        }

        composable(Screen.CallHistory.route) {
            CallHistoryScreen()
        }

        composable(Screen.Dialer.route) {
            DialerScreen()
        }

        composable(
            route = Screen.DialerWithNumber.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber")
            DialerScreen(initialPhoneNumber = phoneNumber)
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }

        composable(Screen.ScheduleCall.route) {
            ScheduleCallScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
