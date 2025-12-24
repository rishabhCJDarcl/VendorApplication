///* ViewTrips.kt
//   Single-file Jetpack Compose + Navigation implementation.
//   Fixes crash on View Detail by avoiding direct painterResource(...) calls
//   when drawable ids are not present.
//*/
//
//package com.example.vendorapplication
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.Call
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//
//// -----------------------------
//// Main entry composable to host the NavHost
//// -----------------------------
//@Composable
//fun ViewTripsApp() {
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = "list") {
//        composable("list") {
//            ListScreen(
//                onViewDetails = { navController.navigate("detail_default") },
//                onViewOngoingDetails = { navController.navigate("detail_ongoing") }
//            )
//        }
//        composable("detail_default") {
//            DetailDefaultScreen(onBack = { navController.navigateUp() })
//        }
//        composable("detail_ongoing") {
//            DetailOngoingScreen(onBack = { navController.navigateUp() })
//        }
//    }
//}
//
//// -----------------------------
//// Data models (simple placeholders)
//// -----------------------------
//data class Trip(
//    val id: String,
//    val route: String,
//    val netAmount: String,
//    val badge: String? = null,
//    val isOngoing: Boolean = false
//)
//
//private val sampleTrips = listOf(
//    Trip(id = "HR47G2320", route = "Delhi - Hosur", netAmount = "₹ 90000"),
//    Trip(id = "HR47G2320", route = "Bahadurgarh - Bengaluru", netAmount = "₹ 90000", badge = "Cancelled trip"),
//    Trip(id = "HR47G2935", route = "Nav Sari District, Gujarat - Bengaluru", netAmount = "₹ 68001", badge = "Vehicle is at loading"),
//    Trip(id = "HR47G6129", route = "Delhi - Upper Subansiri, Arunachal Pradesh", netAmount = "₹ 68001", badge = "Vehicle is going for unloading", isOngoing = true)
//)
//
//// -----------------------------
//// List / Default Screen (screenshot 1)
//// -----------------------------
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ListScreen(onViewDetails: () -> Unit, onViewOngoingDetails: () -> Unit) {
//    var selectedTab by remember { mutableStateOf(0) } // 0 = Upcoming (default), 1 = Ongoing, 2 = Cancelled
//    val tabs = listOf("Upcoming", "Ongoing", "Cancelled")
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("View Trips", fontWeight = FontWeight.SemiBold) },
//                navigationIcon = {
//                    IconButton(onClick = { /* handle back */ }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(text = "Load alert", fontSize = 13.sp)
//                        Spacer(Modifier.width(6.dp))
//                        var checked by remember { mutableStateOf(false) }
//                        Switch(checked = checked, onCheckedChange = { checked = it })
//                    }
//                }
//            )
//        },
//        content = { inner ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(inner)
//                    .verticalScroll(rememberScrollState())
//            ) {
//                // Tab row
//                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
//                    tabs.forEachIndexed { index, label ->
//                        val selected = index == selectedTab
//                        Box(
//                            modifier = Modifier
//                                .padding(end = 12.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(if (selected) Color.Black else Color(0xFFF3F4F6))
//                        ) {
//                            Text(
//                                text = label,
//                                modifier = Modifier
//                                    .padding(horizontal = 18.dp, vertical = 10.dp)
//                                    .clickableNoRipple { selectedTab = index },
//                                color = if (selected) Color.White else Color.DarkGray,
//                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Content changes based on selected tab
//                when (selectedTab) {
//                    0 -> { // Upcoming: show first screenshot style list
//                        sampleTrips.take(3).forEachIndexed { _, t ->
//                            TripCard(trip = t, onViewDetails = onViewDetails)
//                        }
//                    }
//                    1 -> { // Ongoing (screenshot 3)
//                        sampleTrips.filter { it.isOngoing || it.badge?.contains("Vehicle") == true }
//                            .forEach { t ->
//                                OngoingTripCard(trip = t, onViewDetails = onViewOngoingDetails)
//                            }
//                    }
//                    2 -> { // Cancelled
//                        sampleTrips.filter { it.badge?.contains("Cancelled") == true }.forEach { t ->
//                            TripCard(trip = t, onViewDetails = onViewDetails)
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(80.dp))
//            }
//        }
//    )
//}
//
//// -----------------------------
//// click without ripple helper (no @Composable, safe)
//// -----------------------------
//private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
//    this.then(Modifier.clickable(interactionSource = MutableInteractionSource(), indication = null) { onClick() })
//
//// -----------------------------
//// Trip card used in default list (screenshot 1)
//// -----------------------------
//@Composable
//fun TripCard(trip: Trip, onViewDetails: () -> Unit) {
//    Column(modifier = Modifier
//        .fillMaxWidth()
//        .padding(horizontal = 16.dp, vertical = 8.dp)) {
//
//        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier
//            .fillMaxWidth()
//            .shadow(8.dp, RoundedCornerShape(14.dp))) {
//
//            Column(modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White)
//                .padding(16.dp)) {
//
//                trip.badge?.let { b ->
//                    Box(modifier = Modifier
//                        .background(Color(0xFFFFD966), RoundedCornerShape(12.dp))
//                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
//                        Text(text = b, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
//                    }
//                    Spacer(modifier = Modifier.height(12.dp))
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(text = trip.id, fontWeight = FontWeight.Bold, fontSize = 20.sp)
//                        Spacer(modifier = Modifier.height(6.dp))
//                        Text(text = trip.route, color = Color.Gray)
//                    }
//                    Icon(Icons.Default.ArrowForward, contentDescription = "open")
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//                Divider()
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = "Net Amount", modifier = Modifier.weight(1f))
//                    Text(text = trip.netAmount, fontWeight = FontWeight.Bold)
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                OutlinedButton(onClick = onViewDetails, modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp), shape = RoundedCornerShape(12.dp)) {
//                    Text(text = "View Detail", color = Color(0xFF0B8F3D))
//                }
//            }
//        }
//    }
//}
//
//// -----------------------------
//// Ongoing style card (screenshot 3)
//// -----------------------------
//@Composable
//private fun OngoingTripCard(trip: Trip, onViewDetails: () -> Unit) {
//    Column(modifier = Modifier
//        .fillMaxWidth()
//        .padding(horizontal = 16.dp, vertical = 8.dp)) {
//
//        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier
//            .fillMaxWidth()
//            .shadow(6.dp, RoundedCornerShape(14.dp))) {
//
//            Column(modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White)
//                .padding(16.dp)) {
//
//                trip.badge?.let { b ->
//                    Box(modifier = Modifier
//                        .background(Color(0xFFFFD966), RoundedCornerShape(12.dp))
//                        .padding(horizontal = 8.dp, vertical = 6.dp)) {
//                        Text(text = b, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
//                    }
//                    Spacer(modifier = Modifier.height(10.dp))
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(text = trip.id, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
//                        Spacer(modifier = Modifier.height(6.dp))
//                        Text(text = trip.route, color = Color.Gray)
//                    }
//                    Icon(Icons.Default.ArrowForward, contentDescription = "open")
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//                Divider()
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = "Net Amount", modifier = Modifier.weight(1f))
//                    Text(text = trip.netAmount, fontWeight = FontWeight.Bold)
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                OutlinedButton(onClick = onViewDetails, modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp), shape = RoundedCornerShape(12.dp)) {
//                    Text(text = "View Detail", color = Color(0xFF0B8F3D))
//                }
//            }
//        }
//    }
//}
//
//// -----------------------------
//// Detail A (screenshot 2)
//// -----------------------------
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DetailDefaultScreen(onBack: () -> Unit) {
//    Scaffold(topBar = {
//        TopAppBar(
//            title = {
//                Column {
//                    Text("HR47G2320", fontWeight = FontWeight.SemiBold)
//                    Text("Delhi - Hosur", fontSize = 12.sp, color = Color.Gray)
//                }
//            },
//            navigationIcon = {
//                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
//            }
//        )
//    }) { inner ->
//
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(inner)
//            .verticalScroll(rememberScrollState())) {
//
//            // Trip ledger card
//            Card(modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp), shape = RoundedCornerShape(8.dp)) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(text = "Trip ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                    Spacer(modifier = Modifier.height(12.dp))
//                    LedgerRow(label = "Freight", value = "₹ 90000")
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Divider()
//                    Spacer(modifier = Modifier.height(8.dp))
//                    LedgerRow(label = "Net Amount", value = "₹ 90000", bold = true)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    LedgerRow(label = "Balance", value = "₹ 90000")
//                }
//            }
//
//            // Trip Summary area
//            Text(text = "Trip Summary", modifier = Modifier.padding(start = 18.dp), fontWeight = FontWeight.SemiBold)
//            Spacer(modifier = Modifier.height(10.dp))
//
//            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
//                Row(verticalAlignment = Alignment.Top) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Box(modifier = Modifier
//                            .size(10.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF9EE9B1)))
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Box(modifier = Modifier
//                            .width(2.dp)
//                            .height(40.dp)
//                            .background(Color(0xFFDDDDDD)))
//                    }
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(text = "Reached loading - NORTH DISTRICT, DELHI", modifier = Modifier.weight(1f))
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Row(verticalAlignment = Alignment.Top) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Box(modifier = Modifier
//                            .size(10.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF9EE9B1)))
//                    }
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(text = "Reached at unloading - KRISHNAGIRI DISTRICT, TAMIL NADU", modifier = Modifier.weight(1f))
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Decorative area mimic - SAFE painter usage (won't crash if drawable missing)
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(140.dp)
//                        .padding(16.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(Color(0xFFF6F6F7)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    // Safe attempt to fetch a painter; fall back to placeholder if not found
//                    val painterOrNull = runCatching { painterResource(id = R.drawable.ss2) }.getOrNull()
//                    if (painterOrNull != null) {
//                        Image(painter = painterOrNull, contentDescription = "watermark", modifier = Modifier.size(96.dp), contentScale = ContentScale.Fit)
//                    } else {
//                        // placeholder (no crash)
//                        Text(text = "watermark placeholder", color = Color.Gray)
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(60.dp))
//            }
//        }
//    }
//}
//
//@Composable
//private fun LedgerRow(label: String, value: String, bold: Boolean = false) {
//    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        Text(text = label, modifier = Modifier.weight(1f))
//        Text(text = value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
//    }
//}
//
//// -----------------------------
//// Detail B (screenshot 4) - ongoing trip detail
//// -----------------------------
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DetailOngoingScreen(onBack: () -> Unit) {
//    Scaffold(topBar = {
//        TopAppBar(
//            title = {
//                Column {
//                    Text("HR47G2935", fontWeight = FontWeight.SemiBold)
//                    Text("Nav Sari District, Gujarat - Bengaluru", fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
//                }
//            },
//            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
//            actions = {
//                IconButton(onClick = { /* call action */ }) { Icon(Icons.Default.Call, contentDescription = "call") }
//            }
//        )
//    }) { inner ->
//
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(inner)) {
//
//            // map placeholder: safe painter usage (no crash if drawable missing)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(260.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                val painterOrNull = runCatching { painterResource(id = R.drawable.ss3) }.getOrNull()
//                if (painterOrNull != null) {
//                    Image(painter = painterOrNull, contentDescription = "map", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
//                } else {
//                    // fallback placeholder
//                    Box(modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color(0xFFF0F0F0)),
//                        contentAlignment = Alignment.Center) {
//                        Text(text = "map placeholder", color = Color.Gray)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // floating attention card (mimic)
//            Card(modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp), shape = RoundedCornerShape(12.dp)) {
//                Column(modifier = Modifier.padding(14.dp)) {
//                    Text(text = "Vehicle is at loading", fontSize = 12.sp, color = Color.Gray)
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(text = "Check OTP From the bilty to start the trip", fontWeight = FontWeight.Medium)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Addresses card mimic
//            Card(modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Box(modifier = Modifier
//                            .size(14.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF16A34A)))
//                        Spacer(modifier = Modifier.width(10.dp))
//                        Column(Modifier.weight(1f)) {
//                            Text(text = "Loading address", fontWeight = FontWeight.SemiBold)
//                            Spacer(modifier = Modifier.height(6.dp))
//                            Text(text = "Buyerfox, NAVSARI, GUJARAT, 396427", color = Color.Gray)
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(14.dp))
//
//                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                        TextButton(onClick = { /* send to driver */ }) { Text(text = "Send to driver") }
//                        TextButton(onClick = { /* check map */ }) { Text(text = "Check map") }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(14.dp))
//
//            // bottom CTA like green button
//            Box(modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
//                Button(onClick = { /* enter OTP */ }, modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B8F3D)), shape = RoundedCornerShape(10.dp)) {
//                    Text(text = "Enter OTP and start the trip", color = Color.White, fontWeight = FontWeight.SemiBold)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//        }
//    }
//}
//
//// -----------------------------
//// Dummy drawable resource references
//// Replace R.drawable.ss1..ss4 with the actual images/screenshots placed in res/drawable
//// e.g. ss1.png, ss2.png, ss3.png, ss4.png
//// -----------------------------
//object R {
//    object drawable {
//        // set these to real drawable resource ids in your project (or add the files named ss2, ss3)
//        const val ss1 = 0
//        const val ss2 = 0
//        const val ss3 = 0
//        const val ss4 = 0
//    }
//}
// ViewTrips_Fix_NoDrawableCrashes.kt
package com.example.vendorapplication

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Single-file ViewTrips with placeholders (no painterResource usage)

data class Trip(
    val id: String,
    val route: String,
    val netAmount: String,
    val badge: String? = null,
    val isOngoing: Boolean = false
)

private val sampleTrips = listOf(
    Trip(id = "HR47G2320", route = "Delhi - Hosur", netAmount = "₹ 90000"),
    Trip(id = "HR47G2320", route = "Bahadurgarh - Bengaluru", netAmount = "₹ 90000", badge = "Cancelled trip"),
    Trip(id = "HR47G2935", route = "Nav Sari District, Gujarat - Bengaluru", netAmount = "₹ 68001", badge = "Vehicle is at loading"),
    Trip(id = "HR47G6129", route = "Delhi - Upper Subansiri, Arunachal Pradesh", netAmount = "₹ 68001", badge = "Vehicle is going for unloading", isOngoing = true)
)

@Composable
fun ViewTripsApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ListScreen(
                onViewDetails = { navController.navigate("detail_default") },
                onViewOngoingDetails = { navController.navigate("detail_ongoing") }
            )
        }
        composable("detail_default") { DetailDefaultScreen(onBack = { navController.navigateUp() }) }
        composable("detail_ongoing") { DetailOngoingScreen(onBack = { navController.navigateUp() }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(onViewDetails: () -> Unit, onViewOngoingDetails: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 Upcoming,1 Ongoing,2 Cancelled
    val tabs = listOf("Upcoming", "Ongoing", "Cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("View Trips", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = { /* back */ }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                tabs.forEachIndexed { index, label ->
                    val selected = index == selectedTab
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Color.Black else Color(0xFFF3F4F6))
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                                .clickableNoRipple { selectedTab = index },
                            color = if (selected) Color.White else Color.DarkGray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> sampleTrips.take(3).forEach { TripCard(it, onViewDetails) }
                1 -> sampleTrips.filter { it.isOngoing || it.badge?.contains("Vehicle") == true }
                    .forEach { OngoingTripCard(it, onViewOngoingDetails) }
                2 -> sampleTrips.filter { it.badge?.contains("Cancelled") == true }.forEach { TripCard(it, onViewDetails) }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(interactionSource = MutableInteractionSource(), indication = null) { onClick() })

@Composable
fun TripCard(trip: Trip, onViewDetails: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(14.dp))) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)) {

                trip.badge?.let { b ->
                    Box(modifier = Modifier
                        .background(Color(0xFFFFD966), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = b, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = trip.id, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = trip.route, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = "open")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Net Amount", modifier = Modifier.weight(1f))
                    Text(text = trip.netAmount, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = onViewDetails, modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(text = "View Detail", color = Color(0xFF0B8F3D))
                }
            }
        }
    }
}

@Composable
private fun OngoingTripCard(trip: Trip, onViewDetails: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(14.dp))) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)) {

                trip.badge?.let { b ->
                    Box(modifier = Modifier
                        .background(Color(0xFFFFD966), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Text(text = b, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = trip.id, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = trip.route, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = "open")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Net Amount", modifier = Modifier.weight(1f))
                    Text(text = trip.netAmount, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = onViewDetails, modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(text = "View Detail", color = Color(0xFF0B8F3D))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailDefaultScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Column {
                    Text("HR47G2320", fontWeight = FontWeight.SemiBold)
                    Text("Delhi - Hosur", fontSize = 12.sp, color = Color.Gray)
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
    }) { inner ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(rememberScrollState())) {

            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Trip ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    LedgerRow(label = "Freight", value = "₹ 90000")
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    LedgerRow(label = "Net Amount", value = "₹ 90000", bold = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    LedgerRow(label = "Balance", value = "₹ 90000")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Trip Summary", modifier = Modifier.padding(start = 18.dp), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            // Basic summary timeline (placeholder visuals)
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                TimelineItem("Reached loading - NORTH DISTRICT, DELHI", "2 Dec 2025, 03:44", done = true)
                TimelineItem("Reached at unloading - KRISHNAGIRI DISTRICT, TAMIL NADU", "3 Dec 2025, 06:30", done = true)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun LedgerRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        Text(text = value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RememberReturnType")
@Composable
fun DetailOngoingScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("HR47G2935", fontWeight = FontWeight.SemiBold)
                        Text("Nav Sari District, Gujarat - Bengaluru", fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = { /* call action */ }) { Icon(Icons.Default.Call, contentDescription = "call") } }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                // MAP PLACEHOLDER (no drawable)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "map placeholder", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Vehicle is at loading", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Check OTP From the bilty to start the trip", fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A)))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(text = "Loading address", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Buyerfox, NAVSARI, GUJARAT, 396427", color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { /* send to driver */ }) { Text(text = "Send to driver") }
                            TextButton(onClick = { /* check map */ }) { Text(text = "Check map") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedButton(
                        onClick = { /* send bilty */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B8F3D))
                    ) {
                        Text(text = "Send bilty to the party", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Trip ledger", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(12.dp))
                            LedgerRow(label = "Freight", value = "₹ 69000")
                            Spacer(modifier = Modifier.height(8.dp))
                            LedgerRow(label = "Platform", value = "- ₹ 999")
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            LedgerRow(label = "Net Amount", value = "₹ 68001", bold = true)
                            Spacer(modifier = Modifier.height(10.dp))
                            LedgerRow(label = "Balance", value = "₹ 68001")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(text = "Trip Summary", modifier = Modifier.padding(start = 18.dp), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    TimelineItem("Program confirmed", "2 Dec 2025, 03:44", done = true)
                    TimelineItem("Reached loading - NAVSARI DISTRICT, GUJARAT", "3 Dec 2025, 06:30", done = true)
                    TimelineItem("Reached at unloading - BENGALURU URBAN DISTRICT, KARNATAKA", "--", done = false)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Watermark area (placeholder)
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF6F6F7)), contentAlignment = Alignment.Center) {
                    Text(text = "100% Safe (placeholder)", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

            // Floating help pill
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                Card(modifier = Modifier.padding(end = 18.dp, bottom = 120.dp).wrapContentSize(), shape = RoundedCornerShape(24.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Get help")
                    }
                }
            }

            // Bottom CTA pinned
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 16.dp)) {
                Button(
                    onClick = { /* enter OTP */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B8F3D)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Enter OTP and start the trip", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(title: String, subtitle: String, done: Boolean) {
    Row(modifier = Modifier.padding(vertical = 10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (done) Color(0xFF16A34A) else Color(0xFFEDEDED)))
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(if (done) Color(0xFF16A34A) else Color(0xFFEDEDED)))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
