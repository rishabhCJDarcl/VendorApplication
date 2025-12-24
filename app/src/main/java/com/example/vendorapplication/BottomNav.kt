//package com.example.vendorapplication.navigation
//
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.currentBackStackEntryAsState
//
//sealed class BottomNavItem(
//    val route: String,
//    val title: String,
//    val icon: ImageVector
//) {
//    object Load : BottomNavItem("load", "Home", Icons.Default.Home)
//    object EmptyTruck : BottomNavItem("empty_truck", "खाली गाड़ी", Icons.Default.DirectionsCar)
//    object MyTrips : BottomNavItem("my_trips", "MyTrips", Icons.Default.Share) // kept in sealed class (not shown)
//    object ViewTrips : BottomNavItem("view_trips", "Trips", Icons.Default.ReceiptLong)
//    object VehicleKYC : BottomNavItem("kyc", "Help", Icons.Default.Help)
//    object Help : BottomNavItem("help", "Help2", Icons.Default.Info)
//    object Profile : BottomNavItem("profile", "Account", Icons.Default.Person)
//    object NewLoad : BottomNavItem("NEWLOAD", "New Load", Icons.Default.ReceiptLong)
//    object MyOrders : BottomNavItem("my_orders", "My Orders", Icons.Default.ShoppingBag)
//    object AutoPayments : BottomNavItem("auto_payments", "Auto Payments", Icons.Default.Payment)
//    object MyLoadDetail : BottomNavItem("my_load_detail", "Load Detail", Icons.Default.ReceiptLong)
//}
//
///**
// * Top purple banner shown in the screenshot.
// * Use this in Scaffold(topBar = { TopScoreBanner() }) or place it in the screen content.
// */
//@Composable
//fun TopScoreBanner(
//    modifier: Modifier = Modifier,
//    height: Dp = 52.dp
//) {
//    val purple = Color(0xFF6A2BD0) // close to screenshot purple
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(height)
//            .padding(horizontal = 12.dp, vertical = 8.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(12.dp))
//                .background(purple)
//                .padding(horizontal = 16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = "Click here to view your score",
//                color = Color.White,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                style = MaterialTheme.typography.bodyMedium
//            )
//
//            // circular avatar icon on the right
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF6E56E8)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Person,
//                    contentDescription = "profile",
//                    tint = Color.White,
//                    modifier = Modifier.size(18.dp)
//                )
//            }
//        }
//    }
//}
//
///**
// * Improved custom bottom navigation:
// * - Removed Trip, New Load and My Orders from visible items (kept in sealed class)
// * - Even spacing with Arrangement.SpaceEvenly to remove large empty margins
// * - Only selected item shows a small circular background + label; unselected show icon only
// * - Compact height and subtle elevation
// *
// * Use as: bottomBar = { BottomNav(navController) }
// */
//@Composable
//fun BottomNav(navController: NavHostController) {
//    Log.d("BottomNav", "Bottom bar visible")
//
//    // Items to display (removed Trip / NewLoad / MyOrders)
//    val items = listOf(
//        BottomNavItem.Load,
//        BottomNavItem.EmptyTruck,
//        BottomNavItem.ViewTrips,
////        BottomNavItem.VehicleKYC,
//        BottomNavItem.Help,
////        BottomNavItem.AutoPayments,
//        BottomNavItem.Profile
//    )
//
//    val barBackground = Color(0xFFDFF3FF)
//    val selectedIconBg = Color(0xFFF6E6F8)
//    val selectedTint = Color(0xFF6A2BD0)
//    val unselectedTint = Color(0xFF6A6D78)
//
//    // read current backstack entry to know which route is active
//    val navEntry by navController.currentBackStackEntryAsState()
//    val activeRoute = navEntry?.destination?.route
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp, vertical = 6.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Surface(
//            shape = RoundedCornerShape(18.dp),
//            tonalElevation = 1.5.dp,
//            shadowElevation = 6.dp,
//            color = barBackground,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(64.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                items.forEach { item ->
//                    // detect selected: exact match or startsWith in case of nested graphs
//                    val isSelected = when {
//                        activeRoute == null -> false
//                        activeRoute == item.route -> true
//                        else -> activeRoute.startsWith(item.route)
//                    }
//
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .fillMaxHeight()
//                            .clickable {
//                                if (activeRoute != item.route) {
//                                    navController.navigate(item.route) {
//                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
//                                        launchSingleTop = true
//                                        restoreState = true
//                                    }
//                                }
//                            },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center,
//                            modifier = Modifier.fillMaxHeight()
//                        ) {
//                            if (isSelected) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(36.dp)
//                                        .clip(CircleShape)
//                                        .background(selectedIconBg),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Icon(
//                                        imageVector = item.icon,
//                                        contentDescription = item.title,
//                                        tint = selectedTint,
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                }
//                                Spacer(modifier = Modifier.height(6.dp))
//                                Text(
//                                    text = item.title,
//                                    fontSize = 12.sp,
//                                    color = selectedTint,
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                            } else {
//                                Icon(
//                                    imageVector = item.icon,
//                                    contentDescription = item.title,
//                                    tint = unselectedTint,
//                                    modifier = Modifier.size(22.dp)
//                                )
//                                // Unselected: no label to keep bar clean (matches requested style)
//                                Spacer(modifier = Modifier.height(4.dp))
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
package com.example.vendorapplication.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Load : BottomNavItem("load", "Home", Icons.Default.Home)
    object EmptyTruck : BottomNavItem("empty_truck", "खाली गाड़ी", Icons.Default.DirectionsCar)
    object MyTrips : BottomNavItem("my_trips", "MyTrips", Icons.Default.Share)
    object ViewTrips : BottomNavItem("view_trips", "Trips", Icons.Default.ReceiptLong)
    object VehicleKYC : BottomNavItem("kyc", "Help", Icons.Default.Help)
    object Help : BottomNavItem("help", "Help2", Icons.Default.Info)
    object Profile : BottomNavItem("profile", "Account", Icons.Default.Person)
    object NewLoad : BottomNavItem("NEWLOAD", "New Load", Icons.Default.ReceiptLong)
    object MyOrders : BottomNavItem("my_orders", "My Orders", Icons.Default.ShoppingBag)
    object AutoPayments : BottomNavItem("auto_payments", "Auto Payments", Icons.Default.Payment)
    object MyLoadDetail : BottomNavItem("my_load_detail", "Load Detail", Icons.Default.ReceiptLong)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
    object Wallet : BottomNavItem("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object DeviceChangePayment : BottomNavItem("device_change_payment", "Payment", Icons.Default.Payment)
    object Diesel : BottomNavItem("diesel_main", "Diesel", Icons.Default.LocalGasStation)

}

/**
 * Top purple banner shown in the screenshot.
 * Use this in Scaffold(topBar = { TopScoreBanner() }) or place it in the screen content.
 */
@Composable
fun TopScoreBanner(
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    val purple = Color(0xFF6A2BD0) // close to screenshot purple
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(purple)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Click here to view your score",
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            // circular avatar icon on the right
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6E56E8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "profile",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Bottom navigation composable with the banner embedded above the navigation row.
 * Use BottomNav(navController)
 */
@Composable
fun BottomNav(navController: NavHostController) {
    Log.d("BottomNav", "Bottom bar visible")

    // Items to display
    val items = listOf(
        BottomNavItem.Load,
        BottomNavItem.EmptyTruck,
        BottomNavItem.ViewTrips,
        BottomNavItem.Diesel,
        BottomNavItem.Help,
        BottomNavItem.Profile
    )

    val barBackground = Color(0xFFDFF3FF)
    val selectedIconBg = Color(0xFFF6E6F8)
    val selectedTint = Color(0xFF6A2BD0)
    val unselectedTint = Color(0xFF6A6D78)

    // read current backstack entry to know which route is active
    val navEntry by navController.currentBackStackEntryAsState()
    val activeRoute = navEntry?.destination?.route

    // Column so the banner sits above the nav surface (both part of bottomBar)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner placed inside bottom bar (as requested)
        TopScoreBanner(modifier = Modifier.fillMaxWidth(), height = 68.dp)



        // The actual navigation surface
        Surface(
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 1.5.dp,
            shadowElevation = 6.dp,
            color = barBackground,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    // detect selected: exact match or startsWith in case of nested graphs
                    val isSelected = when {
                        activeRoute == null -> false
                        activeRoute == item.route -> true
                        else -> activeRoute.startsWith(item.route)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                if (activeRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(selectedIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = selectedTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    color = selectedTint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = unselectedTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * MainScreen acts as the app shell: the banner is embedded in BottomNav now,
 * so we don't set topBar. Put your NavGraph call inside the content lambda.
 *
 * Usage: in your Activity or NavHost composable call MainScreen(navController)
 */
//@Composable
//fun MainScreen(navController: NavHostController) {
//    Scaffold(
//        bottomBar = { BottomNav(navController) }
//    ) { paddingValues ->
//        // positional call to NavGraph for broader compatibility with different signatures
//        NavGraph(navController, paddingValues)
//    }
//}
