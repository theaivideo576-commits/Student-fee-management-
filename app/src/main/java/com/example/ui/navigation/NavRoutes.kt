package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoute(val route: String, val titleEn: String, val titleHi: String, val icon: ImageVector) {
    object Dashboard : NavRoute("dashboard", "Dashboard", "डैशबोर्ड", Icons.Default.Dashboard)
    object Students : NavRoute("students", "Students", "विद्यार्थी", Icons.Default.Group)
    object Payments : NavRoute("payments", "Payments", "भुगतान log", Icons.Default.Payments)
    object PendingFees : NavRoute("pending_fees", "Pending Fees", "बकाया फीस", Icons.Default.PendingActions)
    object Reports : NavRoute("reports", "Reports", "रिपोर्ट्स", Icons.Default.Assessment)
    object Settings : NavRoute("settings", "Settings", "सेटिंग्स", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    NavRoute.Dashboard,
    NavRoute.Students,
    NavRoute.Payments,
    NavRoute.PendingFees,
    NavRoute.Reports,
    NavRoute.Settings
)
