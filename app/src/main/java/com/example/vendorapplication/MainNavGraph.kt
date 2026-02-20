//package com.example.vendorapplication.navigation
//
//import android.annotation.SuppressLint
//import android.util.Log
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import com.example.vendorapplication.*
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.mutableStateListOf
//
//
//@SuppressLint("SuspiciousIndentation")
//@Composable
//fun MainNavGraph(
//    navController: NavHostController,
//    innerPadding: PaddingValues
//) {
//
//    NavHost(
//        navController = navController,
//        startDestination = "wheels_main",
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(innerPadding)
//    ) {
//
//        // HOME
//        composable("wheels_main") {
//            Log.d("NavGraph", "Home: WheelsMain")
//            WheelsMainScreen(navController)
//        }
//
//        // LOAD SCREEN
//        composable(BottomNavItem.Load.route) {
//            Log.d("NavGraph", "Open: Load Screen")
//            LoadScreen(navController)
//        }
//
//        // MY TRIPS
//        composable(BottomNavItem.MyTrips.route) {
//            Log.d("NavGraph", "Open: MyTrips")
//            MyTripsScreen(navController)
//        }
//
//        // VIEW TRIPS
//        composable("view_trips") {
//            Log.d("NavGraph", "Open: ViewTrips")
//            ViewTripsApp()
//        }
//
//        // ================= VEHICLE KYC (FIXED) =================
//        composable(BottomNavItem.VehicleKYC.route) {
//
//            // 🔴 TEMPORARY HARDCODE TOKEN (TESTING ONLY)
//            val authToken =
//                "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTk5MDczNTQsInVzZXJJZCI6IjIyYWM1NjFkLWRhNWMtNDE2Ni1hYmJhLWQ5NzNlMmYwNDZhMyIsImVtYWlsIjoibmlraGlsLm1haGFyYUBjamRhcmNsLmNvbSIsIm1vYmlsZU51bWJlciI6Ijk2NTQ2MzI3NDQiLCJvcmdJZCI6ImVmMzAwNjgzLTkwMWItNDc3NC1iZjBlLTk1NWQ0OWU3OTZiYyIsIm5hbWUiOiJOaWtoaWwgU2luZ2ggTWFoYXJhIiwib3JnVHlwZSI6IkZMRUVUX09XTkVSIiwiaXNHb2QiOnRydWUsInBvcnRhbFR5cGUiOiJiYXNpYyJ9.MDDgj22r1Hlu4cbl_qySQpqLndzS1D_JAk0owgnQxJ8"
//
//            VehicleKycScreen(
//                navController = navController,
//                authToken = authToken,
//                onBack = {
//                    navController.navigate("load_screen") {
//                        popUpTo(BottomNavItem.VehicleKYC.route) {
//                            inclusive = true
//                        }
//                    }
//                }
//            )
//        }
//        // =======================================================
//
//        // HELP
//        composable("help") {
//            Log.d("NavGraph", "Sidebar -> HelpScreen")
//            HelpScreen()
//        }
//
//        // ================= VEHICLE UPLOAD =================
//        composable("vehicle_upload") {
//            Log.d("NavGraph", "Open: Vehicle Upload Screen")
//            VehicleUploadScreen(
//                vehicles = remember { mutableStateListOf() },
//                onNavigateToMyVehicles = {
//                    navController.navigate("my_vehicles") {
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }
//
//// ================= MY VEHICLES =================
//        composable("my_vehicles") {
//            Log.d("NavGraph", "Open: My Vehicles Screen")
//            MyVehiclesScreen(
//                vehicles = remember { mutableStateListOf() },
//                onBack = { navController.popBackStack() }
//            )
//        }
//
//
//        // MY ORDERS
//        composable("my_orders") {
//            Log.d("NavGraph", "Sidebar -> MyOrdersScreen")
//            MyOrdersScreen()
//        }
//
//        // DIESEL
//        composable("diesel_main") {
//            DiseleScreen()
//        }
//
//        // AUTO PAYMENTS
//        composable("auto_payments") {
//            Log.d("NavGraph", "Sidebar -> AutoPaymentsScreen")
//            AutoPaymentsScreen()
//        }
//
//        // SETTINGS
//        composable("settings") {
//            Log.d("NavGraph", "Open: Settings Screen")
//            SettingsScreen(
//                onBack = { navController.popBackStack() }
//            )
//        }
//
//        // WALLET
//        composable("wallet") {
//            Log.d("NavGraph", "Open: Wallet Screen")
//            WalletScreen(
//                onBack = { navController.popBackStack() }
//            )
//        }
//
//        // DEVICE CHANGE PAYMENT
//        composable("device_change") {
//            Log.d("NavGraph", "Open: Device Change Payment Screen")
//            DeviceChangePaymentScreen(
//                onBack = { navController.popBackStack() }
//            )
//        }
//
//        // MY LOAD DETAIL
//        composable("my_load_detail") {
//            Log.d("NavGraph", "Open: My Load Detail")
//            MyLoadDetailScreen()
//        }
//
//        // FASTAG
//        composable("fastag") {
//            Log.d("NavGraph", "Open: Fastag Screen")
//            YourScreenNameHere()
//        }
//
//        // EMPTY TRUCK
//        composable("empty_truck") {
//            Log.d("NavGraph", "Open: Empty Truck")
//            EmptyTruckScreen(
//                onBack = { navController.popBackStack() },
//                onNotAvailable = {},
//                onEmpty = {},
//                onBottomNav = {}
//            )
//        }
//
//        // MY LOAD PREVIEW
//        composable("my_load_preview") {
//            Log.d("NavGraph", "Open: My Load Preview")
//            MyLoadScreen(navController)
//        }
//
//        // NEW LOAD SCREEN
//        composable("NEWLOAD") {
//            Log.d("NavGraph", "Open: New Load Screen")
//            NewLoadScreenCompose(navController)
//        }
//
//        // PROFILE
//        composable(BottomNavItem.Profile.route) {
//            Log.d("NavGraph", "Open: Profile Screen")
//            ProfileScreen()
//        }
//    }
//}
package com.example.vendorapplication.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vendorapplication.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@SuppressLint("SuspiciousIndentation")
@Composable
fun MainNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {

    NavHost(
        navController = navController,
        // Start app from login screen. Change if you prefer to start elsewhere.
        startDestination = "login",
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        // LOGIN (single composable that contains three internal states)
        composable("login") {
            Log.d("NavGraph", "Open: LoginScreen")
            LoginScreen(
                onLoginSuccess = {
                    // navigate to main wheels screen and remove login from backstack
                    navController.navigate("wheels_main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignupSuccess = {
                    // after signup, go to main app
                    navController.navigate("wheels_main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // HOME
        composable("wheels_main") {
            Log.d("NavGraph", "Home: WheelsMain")
            WheelsMainScreen(navController)
        }

        // LOAD SCREEN
        composable(BottomNavItem.Load.route) {
            Log.d("NavGraph", "Open: Load Screen")
            LoadScreen(navController)
        }

        // MY TRIPS
        composable(BottomNavItem.MyTrips.route) {
            Log.d("NavGraph", "Open: MyTrips")
            MyTripsScreen(navController)
        }

        // VIEW TRIPS
        composable("view_trips") {
            Log.d("NavGraph", "Open: ViewTrips")
            ViewTripsApp()
        }
//

        // ================= VEHICLE KYC (FIXED) =================
        composable(BottomNavItem.VehicleKYC.route) {


            // 🔴 TEMPORARY HARDCODE TOKEN (TESTING ONLY)
            val authToken =
                "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTk5MDczNTQsInVzZXJJZCI6IjIyYWM1NjFkLWRhNWMtNDE2Ni1hYmJhLWQ5NzNlMmYwNDZhMyIsImVtYWlsIjoibmlraGlsLm1haGFyYUBjamRhcmNsLmNvbSIsIm1vYmlsZU51bWJlciI6Ijk2NTQ2MzI3NDQiLCJvcmdJZCI6ImVmMzAwNjgzLTkwMWItNDc3NC1iZjBlLTk1NWQ0OWU3OTZiYyIsIm5hbWUiOiJOaWtoaWwgU2luZ2ggTWFoYXJhIiwib3JnVHlwZSI6IkZMRUVUX09XTkVSIiwiaXNHb2QiOnRydWUsInBvcnRhbFR5cGUiOiJiYXNpYyJ9.MDDgj22r1Hlu4cbl_qySQpqLndzS1D_JAk0owgnQxJ8"

            VehicleKycScreen(
                navController = navController,
                authToken = authToken,
                onBack = {
                    navController.navigate("load_screen") {
                        popUpTo(BottomNavItem.VehicleKYC.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        // =======================================================

        // HELP
        composable("help") {
            Log.d("NavGraph", "Sidebar -> HelpScreen")
            HelpScreen()
        }

        // ================= VEHICLE UPLOAD =================
        composable("vehicle_upload") {
            Log.d("NavGraph", "Open: Vehicle Upload Screen")
            VehicleUploadScreen(
                vehicles = remember { mutableStateListOf() },
                onNavigateToMyVehicles = {
                    navController.navigate("my_vehicles") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ================= MY VEHICLES =================
        composable("my_vehicles") {
            Log.d("NavGraph", "Open: My Vehicles Screen")
            MyVehiclesScreen(
                vehicles = remember { mutableStateListOf() },
                onBack = { navController.popBackStack() }
            )
        }

        // MY ORDERS
        composable("my_orders") {
            Log.d("NavGraph", "Sidebar -> MyOrdersScreen")
            MyOrdersScreen()
        }

        // DIESEL
        composable("diesel_main") {
            DiseleScreen()
        }

        // AUTO PAYMENTS
        composable("auto_payments") {
            Log.d("NavGraph", "Sidebar -> AutoPaymentsScreen")
            AutoPaymentsScreen()
        }

        // SETTINGS
        composable("settings") {
            Log.d("NavGraph", "Open: Settings Screen")
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // WALLET
        composable("wallet") {
            Log.d("NavGraph", "Open: Wallet Screen")
            WalletScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // DEVICE CHANGE PAYMENT
        composable("device_change") {
            Log.d("NavGraph", "Open: Device Change Payment Screen")
            DeviceChangePaymentScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // MY LOAD DETAIL
        composable("my_load_detail") {
            Log.d("NavGraph", "Open: My Load Detail")
            MyLoadDetailScreen()
        }

        // FASTAG
        composable("fastag") {
            Log.d("NavGraph", "Open: Fastag Screen")
            YourScreenNameHere()
        }

        // EMPTY TRUCK
        composable("empty_truck") {
            Log.d("NavGraph", "Open: Empty Truck")
            EmptyTruckScreen(
                onBack = { navController.popBackStack() },
                onNotAvailable = {},
                onEmpty = {},
                onBottomNav = {}
            )
        }

        // MY LOAD PREVIEW
        composable("my_load_preview") {
            Log.d("NavGraph", "Open: My Load Preview")
            MyLoadScreen(navController)
        }

        // NEW LOAD SCREEN
        composable("NEWLOAD") {
            Log.d("NavGraph", "Open: New Load Screen")
            NewLoadScreenCompose(navController)
        }
        composable("main") {
            val screenWidth =
                LocalConfiguration.current.screenWidthDp.dp

            MainContentWithNav(
                screenWidth = screenWidth,
                navController = navController
            )
        }



        // PROFILE
        composable(BottomNavItem.Profile.route) {
            Log.d("NavGraph", "Open: Profile Screen")
            ProfileScreen()
        }
    }
}


