package com.example.synapseai.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Contacts : Screen("contacts")
    object Campaign : Screen("campaign")
    object Dialer : Screen("dialer")
    object Analytics : Screen("analytics")
    object ScheduleCall : Screen("schedule_call")

    // Dialer with optional phone number argument
    object DialerWithNumber : Screen("dialer/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "dialer/$phoneNumber"
    }
}
