package com.example.vendorapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// -----------------------------
// Data model
// -----------------------------
data class WheelsVehicle(
    val plate: String,
    val smallPlate: String,
    val subtitle: String,
    val status: String,
    val speed: String,
    val extra: String,
    val fastagBalance: String,
    val isGps: Boolean
)

private fun sampleWheelsVehicles() = listOf(
    WheelsVehicle(
        plate = "RJ27GE4...",
        smallPlate = "–",
        subtitle = "3 Dec 25, 4:1...",
        status = "Non Wheelseye GPS",
        speed = "0 Kmph",
        extra = "",
        fastagBalance = "₹3700.61",
        isGps = false
    ),
    WheelsVehicle(
        plate = "HR47G7891",
        smallPlate = "0",
        subtitle = "Today, 04:18 PM",
        status = "OFF",
        speed = "0 Kmph",
        extra = "308 M From Ramphal Sinhmar Ujhana, Nh 52, Ujhana, Narwana, Jind District,...",
        fastagBalance = "—",
        isGps = true
    )
)

// -----------------------------
// Colors & theme
// -----------------------------
private val WheelsBackground = Color(0xFFF1F6FA)
private val WheelsAppBar = Color(0xFFF5F7FA)
private val WheelsPillGray = Color(0xFFE9EEF5)
private val WheelsPrimaryBlue = Color(0xFF1E6BFF)
private val WheelsCardWhite = Color(0xFFFFFFFF)
private val WheelsSubtleText = Color(0xFF6B7280)
private val WheelsMuted = Color(0xFF9AA3B2)
private val WheelsLightBorder = Color(0xFFE6ECF3)


// -----------------------------
// Theme (Opt-in for Material3 drawer APIs)
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelsTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = WheelsPrimaryBlue,
        onPrimary = Color.White,
        background = WheelsBackground,
        surface = WheelsCardWhite,
        onSurface = Color.Black
    )
    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography()
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
            content()
        }
    }
}

// -----------------------------
// Entry / preview
// -----------------------------
@Preview(showBackground = true, backgroundColor = 0xFFF1F6FA)
@Composable
fun WheelsPreviewScreen() {
    WheelsTheme {
        val navController = rememberNavController()
        WheelsMainScreen(navController)
    }
}

// -----------------------------
// Main screen with ModalNavigationDrawer at root
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelsMainScreen(navController: NavHostController) {
    val vehicles = remember { sampleWheelsVehicles() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp),
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
            ) {
                // Pass a close callback to DrawerContent so items can close the drawer
                DrawerContent(
                    navController = navController, // pass your NavHostController here
                    onClose = { scope.launch { drawerState.close() } }
                )

            }
        }
    ) {
        Scaffold(
            topBar = {
                // Toggle drawer: open if closed, close if open
                WheelsTopBar(
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                        }
                    },
                    onCallClick = { /* handle call */ },
                    onSearchClick = { /* handle search */ }
                )
            },
//            bottomBar = {
//                WheelsBottomBar(navController)
//            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WheelsTabsRow(navController)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { WheelsMyVehiclesHeader() }
                        item { WheelsFilterChipsRow() }
                        item { WheelsAddVehicleCard() }
                        items(vehicles) { vehicle -> WheelsVehicleCard(vehicle) }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // keep last item visible above bottom bar
                    }
                }
            }
        )
    }
}


//    @Composable
//    fun DrawerContent(
//        navController: NavHostController,
//        onClose: () -> Unit = {}
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//
//            // ------------------ TOP USER CARD ------------------
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//                    .clip(RoundedCornerShape(18.dp))
//                    .background(Color(0xFFE8F0FB))
//                    .padding(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Box(
//                        modifier = Modifier
//                            .size(50.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF4B7BE5)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "SS",
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                fontWeight = FontWeight.Bold,
//                                color = Color.White
//                            )
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Column {
//                        Text(
//                            "Satender",
//                            style = MaterialTheme.typography.titleLarge.copy(
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                        )
//                        Text(
//                            "9729790996",
//                            style = MaterialTheme.typography.bodyMedium.copy(
//                                color = Color.DarkGray
//                            )
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // ------------------ ACCOUNT MANAGER BOX ------------------
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(Color.White)
//                    .padding(12.dp)
//            ) {
//                Text(
//                    "Your account Manager",
//                    style = MaterialTheme.typography.labelLarge.copy(
//                        color = Color.Gray
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(6.dp))
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Box(
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFFFFE3C8)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            Icons.Default.Person,
//                            contentDescription = null,
//                            tint = Color(0xFFB85C00)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(10.dp))
//
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            "Jaya",
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                fontWeight = FontWeight.W500
//                            )
//                        )
//                        Text(
//                            "7428189274",
//                            style = MaterialTheme.typography.bodySmall.copy(
//                                color = Color.DarkGray
//                            )
//                        )
//                    }
//
//                    Button(
//                        onClick = {},
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF1678FF),
//                            contentColor = Color.White
//                        ),
//                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
//                    ) {
//                        Text("Call Now")
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(10.dp))
//                        .background(Color(0xFFE4F5E4))
//                        .padding(8.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        "Call service available from 10 am to 7 pm",
//                        style = MaterialTheme.typography.bodySmall.copy(
//                            color = Color(0xFF1B7D1B)
//                        )
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Divider()
//
//
//            // ---------------------------------------------
//            //       DRAWER ITEMS WITH NAVIGATION
//            // ---------------------------------------------
//
//            DrawerItem(Icons.Default.Help, "Help") {
//                onClose()
//                navController.navigate("help")
//            }
//
//            DrawerItem(Icons.Default.ShoppingCart, "My Orders") {
//                onClose()
//                navController.navigate("my_orders")
//            }
//
//            DrawerItem(Icons.Default.Autorenew, "Auto Payments") {
//                onClose()
//                navController.navigate("auto_payments")
//            }
//
//            DrawerItem(Icons.Default.LocationOn, "GPS Transactions") {
//                onClose()
//                navController.navigate("gps_transactions")
//            }
//
//            DrawerItem(Icons.Default.PhoneAndroid, "Device Change") {
//                onClose()
//                navController.navigate("device_change")
//            }
//
//            DrawerItem(Icons.Default.AccountBalanceWallet, "Wallet") {
//                onClose()
//                navController.navigate("wallet")
//            }
//
//            DrawerItem(Icons.Default.Settings, "Settings") {
//                onClose()
//                navController.navigate("settings")
//            }
//
//            DrawerItem(Icons.Default.LocalShipping, "Find Load") {
//                onClose()
//                navController.navigate("find_load")
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Text(
//                "Version 4.0.9 (release)",
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(12.dp),
//                style = MaterialTheme.typography.bodySmall.copy(
//                    color = Color.Gray
//                )
//            )
//        }
//    }
@Composable
fun DrawerContent(
    navController: NavHostController,
    onClose: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ------------------ TOP USER CARD ------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE8F0FB))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4B7BE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Satender",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Text(
                        "9729790996",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.DarkGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ------------------ ACCOUNT MANAGER BOX ------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(
                "Your account Manager",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE3C8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFB85C00)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Jaya",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500
                        )
                    )
                    Text(
                        "7428189274",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.DarkGray
                        )
                    )
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1678FF),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
                ) {
                    Text("Call Now")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE4F5E4))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Call service available from 10 am to 7 pm",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF1B7D1B)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        // ------------------ DRAWER ITEMS ------------------

//        DrawerItem(Icons.Default.Help, "Help") {
//            onClose()
//            navController.navigate("help")
//        }
//
//        DrawerItem(Icons.Default.ShoppingCart, "My Orders") {
//            onClose()
//            navController.navigate("my_orders")
//        }

//        DrawerItem(Icons.Default.Autorenew, "Auto Payments") {
//            onClose()
//            navController.navigate("auto_payments")
//        }
//
//        DrawerItem(Icons.Default.LocationOn, "GPS Transactions") {
//            onClose()
//            navController.navigate("gps_transactions")
//        }

//        DrawerItem(Icons.Default.PhoneAndroid, "Device Change") {
//            onClose()
//            navController.navigate("device_change")
//        }
//
//        DrawerItem(Icons.Default.AccountBalanceWallet, "Wallet") {
//            onClose()
//            navController.navigate("wallet")
//        }
//
//        DrawerItem(Icons.Default.Settings, "Settings") {
//            onClose()
//            navController.navigate("settings")
//        }

//        DrawerItem(Icons.Default.LocalShipping, "Find Load") {
//            onClose()
//            navController.navigate("find_load")
//        }

        // ✅✅ NEW ITEMS ADDED HERE
        DrawerItem(Icons.Default.UploadFile, "Vehicle Upload") {
            Log.d("DrawerNav", "➡ Clicked: Vehicle Upload")

            try {
                onClose()
                Log.d("DrawerNav", "➡ Navigating to: vehicle_upload")

                navController.navigate("vehicle_upload") {
                    launchSingleTop = true
                }

                Log.d("DrawerNav", "✅ Navigation call done: vehicle_upload")
            } catch (e: Exception) {
                Log.e("DrawerNav", "❌ Navigation failed: vehicle_upload", e)
            }
        }

        DrawerItem(Icons.Default.DirectionsCar, "My Vehicles") {
            Log.d("DrawerNav", "➡ Clicked: My Vehicles")

            try {
                onClose()
                Log.d("DrawerNav", "➡ Navigating to: my_vehicles")

                navController.navigate("my_vehicles") {
                    launchSingleTop = true
                }

                Log.d("DrawerNav", "✅ Navigation call done: my_vehicles")
            } catch (e: Exception) {
                Log.e("DrawerNav", "❌ Navigation failed: my_vehicles", e)
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        Text(
            "Version 4.0.9 (release)",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(12.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray
            )
        )
    }
}


@Composable
fun WheelsTopBar(
    onMenuClick: () -> Unit,
    onCallClick: () -> Unit,
    onSearchClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WheelsAppBar)
            .zIndex(5f) // ensure always on top
            .padding(start = 12.dp, end = 12.dp, top = 26.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -------------------------
        // MENU BUTTON (working touch like TopRow)
        // -------------------------
        IconButton(
            onClick = {
                Log.d("WheelsTopBar", "Menu Clicked")
                onMenuClick()
            },
            modifier = Modifier
                .size(48.dp)
                .clickable( // force clickable so no layout blocks it
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onMenuClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF111827),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {}

        // -------------------------
        // CALL BUTTON
        // -------------------------
        IconButton(
            onClick = {
                Log.d("WheelsTopBar", "Call Clicked")
                onCallClick()
            },
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCallClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = Color(0xFF111827),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // -------------------------
        // SEARCH BUTTON
        // -------------------------
        IconButton(
            onClick = {
                Log.d("WheelsTopBar", "Search Clicked")
                onSearchClick()
            },
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSearchClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF111827),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}



// -----------------------------
// Drawer item helper (now accepts click lambda)
// -----------------------------
@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // keep clickable first so entire row is tappable
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF4F4F4F), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black
            )
        )
    }
}

// -----------------------------
// Tabs / pills
// -----------------------------
@Composable
fun WheelsTabsRow(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.wrapContentSize().clickable {
                navController.navigate("fastag") {
                    launchSingleTop = true
                }
            }
        ) {
            WheelsPillTab("FASTAG", selected = false)
        }


        Box(modifier = Modifier.wrapContentSize()) { WheelsPillTab("GPS", selected = true) }

        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable {
                    navController.navigate("diesel_main") {   // 👈 navigate to diesel
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    }
                }
        ) {
            WheelsPillTab("DIESEL", selected = false)
        }


        Box(modifier = Modifier.wrapContentSize().clickable {
            navController.navigate("load") {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            }
        }) { WheelsPillTab("LOAD", selected = false) }
    }
}

@Composable
fun WheelsPillTab(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else WheelsPillGray)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (selected) Color(0xFF0B2E68) else Color(0xFF6B7280),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// -----------------------------
// Header, chips, add vehicle, vehicle card
// -----------------------------
@Composable
fun WheelsMyVehiclesHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Vehicles",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(WheelsPillGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF6B7280))
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("New", color = Color(0xFFDC2626), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reports", fontSize = 12.sp, color = Color(0xFF0B2E68))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Card(
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("GPS alerts", fontSize = 12.sp, color = Color(0xFF0B2E68))
                }
            }
        }
    }
}

@Composable
fun WheelsFilterChipsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelsChipWithCount("All", 16, selected = true)
        Spacer(modifier = Modifier.width(8.dp))
        WheelsChipWithCount("Running", 3, selected = false)
        Spacer(modifier = Modifier.width(8.dp))
        WheelsChipWithCount("Stopped", 12, selected = false)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(WheelsPillGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6B7280))
        }
    }
}

@Composable
fun WheelsChipWithCount(label: String, count: Int, selected: Boolean) {
    val bg = if (selected) Color(0xFFEAF2FF) else Color.White
    val borderColor = if (selected) Color(0xFFBEE3FF) else WheelsLightBorder
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$label ($count)", fontSize = 13.sp, color = Color(0xFF0B2E68))
    }
}

@Composable
fun WheelsAddVehicleCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(WheelsCardWhite)
            .padding(14.dp)) {

            Text(
                text = "Add your other vehicle",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var plate by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    placeholder = { Text("HR26CT5405", color = WheelsMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color(0xFFE5E7EB),
                        cursorColor = Color(0xFF111827),
                        focusedPlaceholderColor = WheelsMuted,
                        unfocusedPlaceholderColor = WheelsMuted
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { /* action */ },
                    modifier = Modifier
                        .height(46.dp)
                        .width(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0870FF)
                    )
                ) {
                    Text(
                        text = "Add",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        }
    }
}

@Composable
fun WheelsVehicleCard(data: WheelsVehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(WheelsCardWhite)
            .padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F6FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.smallPlate,
                        fontSize = 18.sp,
                        color = WheelsMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = data.plate,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF3F6FA))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = data.status, fontSize = 12.sp, color = WheelsSubtleText)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = data.subtitle, fontSize = 12.sp, color = WheelsSubtleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WheelsPillGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⋮", color = Color(0xFF6B7280), fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (data.isGps) "Live GPS available" else "+ Buy GPS to track your vehicle", fontSize = 13.sp, color = Color(0xFF1565C0))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDADAF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("F", fontSize = 12.sp, color = Color(0xFF3B2EB8))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "FASTag Balance: ${data.fastagBalance}", fontSize = 13.sp, color = Color(0xFF111827))
                    }

                    Text(text = "Recharge", fontSize = 13.sp, color = Color(0xFF1160FF))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (data.extra.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = data.extra, fontSize = 13.sp, color = WheelsSubtleText, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "4 km Today | Stopped since 16 hr", fontSize = 12.sp, color = WheelsMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Relay is OFF", fontSize = 12.sp, color = WheelsMuted)
                    }
                }
            }
        }
    }
}

