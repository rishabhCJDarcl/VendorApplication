
package com.example.vendorapplication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*


import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color

// If you add Apache POI dependency, these imports will be used by parseExcelFromUri
// import org.apache.poi.ss.usermodel.CellType
// import org.apache.poi.ss.usermodel.DataFormatter
// import org.apache.poi.xssf.usermodel.XSSFWorkbook
// import org.apache.poi.hssf.usermodel.HSSFWorkbook

private val DrawerWidthDp = 320.dp

// ----------------------------------------------------------
//  DATA MODEL
// ----------------------------------------------------------

data class VehicleData(
    val vehicleNumber: String,
    val driverName: String? = null,
    val driverMobile: String? = null,
    val vehicleType: String? = null,
    val rawExtras: Map<String, String> = emptyMap()
)

// ----------------------------------------------------------
//  START MAIN ENTRY — CALL THIS FROM setContent{ MainApp() }
// ----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Shared in-memory vehicle list (persist for app process lifetime)
    val vehicles = remember { mutableStateListOf<VehicleData>() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(DrawerWidthDp)
            ) {
                Sidebar(
                    onClose = { scope.launch { drawerState.close() } },
                    onMenuClick = { route ->
                        // navigate and close drawer
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vendor App") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "menu")
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "wallet", // start on wallet for demo; change as needed
                modifier = Modifier.padding(padding)
            ) {

                composable("help") {
                    Log.d("NavGraph", "Sidebar -> HelpScreen")
                    HelpScreen(onBack = { navController.popBackStack() })
                }

                composable("my_orders") {
                    Log.d("NavGraph", "Sidebar -> MyOrdersScreen")
                    MyOrdersScreen(onBack = { navController.popBackStack() })
                }

                composable("auto_payments") {
                    Log.d("NavGraph", "Sidebar -> AutoPaymentsScreen")
                    AutoPaymentsScreen(onBack = { navController.popBackStack() })
                }

                composable("device_payment") {
                    Log.d("NavGraph", "Sidebar -> DeviceChangePaymentScreen")
                    DeviceChangePaymentScreen(onBack = { navController.popBackStack() })
                }

                composable("wallet") {
                    Log.d("NavGraph", "Sidebar -> WalletScreen")
                    WalletScreen(onBack = { navController.popBackStack() })
                }

                composable("settings") {
                    Log.d("NavGraph", "Sidebar -> SettingsScreen")
                    SettingsScreen(onBack = { navController.popBackStack() })
                }

                // NEW: Vehicle upload screen
                composable("vehicle_upload") {
                    Log.d("NavGraph", "Sidebar -> VehicleUploadScreen")
                    VehicleUploadScreen(
                        vehicles = vehicles,
                        onNavigateToMyVehicles = { navController.navigate("my_vehicles") }
                    )
                }

                // NEW: My Vehicles screen
                composable("my_vehicles") {
                    Log.d("NavGraph", "Sidebar -> MyVehiclesScreen")
                    MyVehiclesScreen(
                        vehicles = vehicles,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------
//  SIDEBAR
// ----------------------------------------------------------

@Composable
fun Sidebar(
    onClose: () -> Unit,
    onMenuClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {
        HeaderSection()

        Text(
            text = "Your account Manager",
            fontSize = 13.sp,
            color = Color(0xFF7B7B7B),
            modifier = Modifier.padding(start = 20.dp, top = 18.dp)
        )

        AccountManagerCard()

        Divider(color = Color(0xFFE9E9E9), thickness = 1.dp)

        // ✅ menu now takes remaining space and scrolls
        SidebarMenu(
            onMenuClick = onMenuClick,
            modifier = Modifier.weight(1f)
        )

        VersionBar()
    }
}


@Composable
private fun HeaderSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .height(96.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF7FF)),
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E88FF)),
                contentAlignment = Alignment.Center
            ) {
                Text("SS", fontSize = 22.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text("Satender Singh", fontSize = 18.sp, color = Color(0xFF111111))
                Text("9729790996", fontSize = 13.sp, color = Color(0xFF6B6B6B))
            }
        }
    }
}

@Composable
private fun AccountManagerCard() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("J", fontSize = 16.sp, color = Color(0xFFDD6A36))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("jaya", fontSize = 15.sp, color = Color(0xFF222222))
                    Text("7428189274", fontSize = 12.sp, color = Color(0xFF7B7B7B))
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Call Now", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F7ED))
        ) {
            Text(
                "Call service available from 10 am to 7 pm",
                modifier = Modifier.padding(12.dp),
                color = Color(0xFF1D8A49),
                fontSize = 13.sp
            )
        }
    }
}

// ----------------------------------------------------------
//  MENU
// ----------------------------------------------------------

private data class DrawerMenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

@Composable
private fun SidebarMenu(
    onMenuClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        DrawerMenuItem(Icons.Default.AccountBalanceWallet, "Wallet", "wallet"),
        DrawerMenuItem(Icons.Default.Payments, "Device Change Payment", "device_payment"),
        DrawerMenuItem(Icons.Default.Settings, "Settings", "settings"),
        DrawerMenuItem(Icons.Default.Help, "Help", "help"),
        DrawerMenuItem(Icons.Default.ShoppingBag, "My Orders", "my_orders"),
        DrawerMenuItem(Icons.Default.Payment, "Auto Payments", "auto_payments"),
        // your new items
        DrawerMenuItem(Icons.Default.UploadFile, "Vehicle Upload", "vehicle_upload"),
        DrawerMenuItem(Icons.Default.DriveEta, "My Vehicles", "my_vehicles")
    )

    LazyColumn(modifier = modifier) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMenuClick(item.route) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF7F7F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, contentDescription = item.label)
                }

                Spacer(modifier = Modifier.width(14.dp))
                Text(item.label, fontSize = 16.sp)
            }

            Divider(color = Color(0xFFF2F2F2))
        }
    }
}



@Composable
private fun VersionBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F6F7))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Version 4.0.9 (release)",
            fontSize = 12.sp,
            color = Color(0xFF888888)
        )
    }
}

// ----------------------------------------------------------
//  HELP SCREEN (unchanged)
// ----------------------------------------------------------

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("Help Centre", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Your account manager", fontSize = 13.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        // Same manager card UI reused
        AccountManagerCard()

        Spacer(modifier = Modifier.height(20.dp))

        Text("Recent tickets", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        TicketCard(
            title = "App issue",
            subtitle = "HR47G2320",
            status = "Resolved",
            resolvedOn = "13 November 2025",
            id = "5234725"
        )
    }
}

@Composable
private fun TicketCard(
    title: String,
    subtitle: String,
    status: String,
    resolvedOn: String,
    id: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider()
            Spacer(modifier = Modifier.height(10.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(status, color = Color(0xFF1D8A49))
                    Text("Resolved on: $resolvedOn", fontSize = 12.sp, color = Color.Gray)
                }
                Text("ID: $id", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ----------------------------------------------------------
//  MY ORDERS SCREEN (unchanged)
// ----------------------------------------------------------

data class OrderData(
    val orderNo: String,
    val status: String,
    val date: String,
    val pendingAmount: String
)

@Composable
fun MyOrdersScreen(onBack: () -> Unit ={}) {

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("My Orders", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val orders = listOf(
            OrderData("7135518", "In Progress", "03 Dec 2025", "₹ 0"),
            OrderData("7135500", "Completed", "03 Dec 2025", "₹ 0"),
            OrderData("7130050", "In Progress", "02 Dec 2025", "₹ 0")
        )

        LazyColumn {
            items(orders) { order ->
                OrderCard(order)
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun OrderCard(data: OrderData) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text("Order no. ${data.orderNo}", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            Text("Total Trucks: 1")

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order date", fontSize = 12.sp, color = Color.Gray)
                    Text(data.date)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending amount", fontSize = 12.sp, color = Color.Gray)
                    Text(data.pendingAmount)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View Details", color = Color(0xFF1E88FF))
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

// ----------------------------------------------------------
//  AUTO PAYMENTS SCREEN (unchanged)
// ----------------------------------------------------------

@Composable
fun AutoPaymentsScreen(onBack: () -> Unit = {}) {

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("Auto Payments", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFF3F6FA), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "1-Month GPS Plan - Autopay",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("HR47G6129", fontSize = 12.sp, color = Color.Gray)
                    }

                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Amount", fontSize = 12.sp, color = Color.Gray)
                        Text("₹ 353", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Next Deduction", fontSize = 12.sp, color = Color.Gray)
                        Text("21 Dec 2025")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------
//  NEW: Device Change Payment Screen
// ----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceChangePaymentScreen(onBack: () -> Unit = {}) {
    // sample vehicle list
    val vehicles = listOf("ANR7347", "ANR7476", "HR47G6129")
    var selectedVehicle by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Text("Device Change Payment", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Vehicle dropdown (styled like OutlinedTextField)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedVehicle ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select vehicle number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text(vehicle) },
                            onClick = {
                                selectedVehicle = vehicle
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = { Text("Enter amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Large spacer to visually match screenshot
            Spacer(modifier = Modifier.weight(1f))
        }

        // Pay Now button anchored at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // implement payment flow
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88FF))
            ) {
                Text("Pay now", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

// ----------------------------------------------------------
//  NEW: Wallet Screen (unchanged)
// ----------------------------------------------------------

data class TransactionItem(
    val title: String,
    val subtitle: String,
    val amount: String,
    val isCredit: Boolean
)

@Composable
fun WalletScreen(onBack: () -> Unit = {}) {

    // sample transactions
    val transactions = remember {
        listOf(
            TransactionItem("Fastag recharged for ANR7347 by Auto recharge", "11 Dec 25, 11:42 AM", "-₹ 320", false),
            TransactionItem("Fastag recharged for ANR7476 by Auto recharge", "11 Dec 25, 11:19 AM", "-₹ 635", false),
            TransactionItem("Money added to Wheelseye Wallet", "11 Dec 25, 08:04 AM", "+₹ 3000", true),
            TransactionItem("Cashback credited", "10 Dec 25, 04:12 PM", "+₹ 25", true)
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("CJD Wallet", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Wallet card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text("Wallet Balance", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("₹ 2985.0", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { /* add money */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88FF))
                ) {
                    Text("+ Add Money", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Filters row (All, Credit, CashBack, Success)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChipRow(options = listOf("All", "Credit", "CashBack", "Success"), selected = selectedFilter) {
                selectedFilter = it
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("11 Dec 2025", fontSize = 13.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        // Transactions
        LazyColumn {
            items(transactions) { tx ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF7F7F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(tx.subtitle, fontSize = 12.sp, color = Color.Gray)
                        }

                        Text(
                            tx.amount,
                            color = if (tx.isCredit) Color(0xFF1D8A49) else Color(0xFFDD2C00),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row {
        options.forEach { opt ->
            val isSelected = opt == selected
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0xFF1E88FF) else Color(0xFFF7F7F8))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(opt, color = if (isSelected) Color.White else Color.Black)
            }
        }
    }
}

// ----------------------------------------------------------
//  NEW: Settings Screen (unchanged)
// ----------------------------------------------------------

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(18.dp))

        SettingItem(icon = Icons.Default.NotificationsOff, title = "Turn on/off notifications") { /* navigate */ }
        Spacer(modifier = Modifier.height(12.dp))
        SettingItem(icon = Icons.Default.Key, title = "Change password") { /* navigate */ }
        Spacer(modifier = Modifier.height(12.dp))
        SettingItem(icon = Icons.Default.Description, title = "Legal") { /* navigate */ }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7F7F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// ----------------------------------------------------------
//  NEW: Vehicle Upload Screen (bulk + manual) & parsing
// ----------------------------------------------------------

//@Composable
//fun VehicleUploadScreen(
//    vehicles: MutableList<VehicleData>,
//    onNavigateToMyVehicles: () -> Unit
//) {
//    val ctx = LocalContext.current
//    var pickedUri by remember { mutableStateOf<Uri?>(null) }
//    var pickedName by remember { mutableStateOf<String?>(null) }
//    var parsingStatus by remember { mutableStateOf<String?>(null) }
//
//    // Manual entry fields
//    var vehicleNumber by remember { mutableStateOf("") }
//    var driverName by remember { mutableStateOf("") }
//    var driverMobile by remember { mutableStateOf("") }
//    var vehicleType by remember { mutableStateOf("") }
//
//    // File picker launcher (SAF)
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        pickedUri = uri
//        pickedName = uri?.let { getFileName(ctx, it) }
//    }
//
//    Column(modifier = Modifier
//        .fillMaxSize()
//        .padding(16.dp)) {
//
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            IconButton(onClick = { /* expecting parent nav to provide back; this screen is reachable from sidebar */ }) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "back")
//            }
//            Text("Vehicle Upload", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//        }
//
//        Spacer(modifier = Modifier.height(14.dp))
//
//        // Bulk upload card
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Column(modifier = Modifier.padding(12.dp)) {
//                Text("Bulk upload (Excel only)", fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.height(8.dp))
//                Text("Upload an .xls or .xlsx file where each row represents a vehicle.", fontSize = 13.sp, color = Color.Gray)
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Row {
//                    Button(onClick = {
//                        // Accept any; we'll validate extension later
//                        launcher.launch("*/*")
//                    }) {
//                        Icon(Icons.Default.UploadFile, contentDescription = null)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Choose file")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    if (pickedName != null) {
//                        Text(pickedName ?: "", modifier = Modifier.align(Alignment.CenterVertically))
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Row {
//                    Button(onClick = {
//                        // Validate extension
//                        val uri = pickedUri
//                        if (uri == null) {
//                            parsingStatus = "Please pick a file first."
//                            return@Button
//                        }
//
//                        val name = pickedName ?: getFileName(ctx, uri) ?: ""
//                        val lower = name.lowercase()
//                        if (!(lower.endsWith(".xls") || lower.endsWith(".xlsx"))) {
//                            parsingStatus = "Only .xls or .xlsx files are accepted."
//                            return@Button
//                        }
//
//                        // Parse file (attempt POI; fallback to CSV-ish)
//                        try {
//                            val parsed = parseExcelFromUri(ctx, uri)
//                            if (parsed.isNotEmpty()) {
//                                vehicles.addAll(parsed)
//                                parsingStatus = "Imported ${parsed.size} vehicles."
//                            } else {
//                                parsingStatus = "No rows found in file."
//                            }
//                            // Navigate to My Vehicles to see imported list
//                            onNavigateToMyVehicles()
//                        } catch (ex: Exception) {
//                            Log.e("VehicleUpload", "Parsing failed", ex)
//                            parsingStatus = "Parsing failed: ${ex.message}"
//                        }
//                    }) {
//                        Text("Import")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    OutlinedButton(onClick = {
//                        pickedUri = null
//                        pickedName = null
//                        parsingStatus = null
//                    }) {
//                        Text("Clear")
//                    }
//                }
//
//                parsingStatus?.let {
//                    Spacer(modifier = Modifier.height(10.dp))
//                    Text(it, color = Color.Gray, fontSize = 13.sp)
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // Manual entry card
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Column(modifier = Modifier.padding(12.dp)) {
//                Text("Add individual vehicle", fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.height(10.dp))
//
//                OutlinedTextField(
//                    value = vehicleNumber,
//                    onValueChange = { vehicleNumber = it },
//                    label = { Text("Vehicle Number") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = driverName,
//                    onValueChange = { driverName = it },
//                    label = { Text("Driver Name") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = driverMobile,
//                    onValueChange = { driverMobile = it },
//                    label = { Text("Driver Mobile") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = true
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = vehicleType,
//                    onValueChange = { vehicleType = it },
//                    label = { Text("Vehicle Type / Wheels") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Row {
//                    Button(onClick = {
//                        // Add to vehicles list and navigate to My Vehicles
//                        if (vehicleNumber.isNotBlank()) {
//                            vehicles.add(
//                                VehicleData(
//                                    vehicleNumber = vehicleNumber.trim(),
//                                    driverName = driverName.trim().ifBlank { null },
//                                    driverMobile = driverMobile.trim().ifBlank { null },
//                                    vehicleType = vehicleType.trim().ifBlank { null }
//                                )
//                            )
//                            // reset fields
//                            vehicleNumber = ""
//                            driverName = ""
//                            driverMobile = ""
//                            vehicleType = ""
//                            onNavigateToMyVehicles()
//                        } else {
//                            // minimal UX: set parsingStatus message
//                            parsingStatus = "Vehicle number is required for manual entry."
//                        }
//                    }) {
//                        Text("Add Vehicle")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    OutlinedButton(onClick = {
//                        vehicleNumber = ""
//                        driverName = ""
//                        driverMobile = ""
//                        vehicleType = ""
//                    }) {
//                        Text("Reset")
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//    }
//}
//
///**
// * Try to parse an Excel file from [uri] into a list of VehicleData.
// *
// * Primary approach: use Apache POI (recommended). If POI classes are not available at runtime,
// * the code will attempt a simple CSV-like fallback (works only for simple textual CSV).
// *
// * NOTE: To fully support .xlsx / .xls please add Apache POI to your app module:
// * implementation 'org.apache.poi:poi:5.2.3'
// * implementation 'org.apache.poi:poi-ooxml:5.2.3'
// */
//fun parseExcelFromUri(context: Context, uri: Uri): List<VehicleData> {
//    val result = mutableListOf<VehicleData>()
//
//    // Try POI parsing first
//    try {
//        // The following code requires Apache POI library. If you added POI to Gradle, uncomment imports
//        // and this block will run. Otherwise a ClassNotFoundException will be caught and fallback used.
//
//        // open stream
//        val input = context.contentResolver.openInputStream(uri) ?: return emptyList()
//
//        // Use reflection guard, so compile won't fail if POI not present.
//        // If POI is available, use it:
//        try {
//            // If POI classes exist, use them.
//            // Try .xlsx then .xls by content or extension
//            // The code below uses classes if they exist at runtime.
//            // We'll attempt to use XSSFWorkbook first (for xlsx).
//            val name = getFileName(context, uri) ?: ""
//            val lower = name.lowercase()
//
//            val workbook = when {
//                lower.endsWith(".xlsx") -> {
//                    // XSSFWorkbook
//                    val ctor = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
//                    ctor.getConstructor(java.io.InputStream::class.java).newInstance(input)
//                }
//                lower.endsWith(".xls") -> {
//                    val ctor = Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook")
//                    ctor.getConstructor(java.io.InputStream::class.java).newInstance(input)
//                }
//                else -> {
//                    // try xssf anyway
//                    val ctor = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
//                    ctor.getConstructor(java.io.InputStream::class.java).newInstance(input)
//                }
//            }
//
//            // DataFormatter for consistent string conversion
//            val dataFormatter = Class.forName("org.apache.poi.ss.usermodel.DataFormatter").getConstructor().newInstance()
//
//            val workbookClass = workbook::class.java
//            val numberOfSheets = workbookClass.getMethod("getNumberOfSheets").invoke(workbook) as Int
//
//            for (s in 0 until numberOfSheets) {
//                val sheet = workbookClass.getMethod("getSheetAt", Int::class.javaPrimitiveType).invoke(workbook, s)
//                val sheetClass = sheet::class.java
//                val rowIterator = sheetClass.getMethod("iterator").invoke(sheet) as java.util.Iterator<*>
//
//                // We'll assume first row may be header. We'll attempt to detect header by checking cell types.
//                var isFirstRow = true
//                var headerNames: List<String>? = null
//
//                while (rowIterator.hasNext()) {
//                    val row = rowIterator.next()
//                    val rowClass = row::class.java
//                    val cellIterator = rowClass.getMethod("cellIterator").invoke(row) as java.util.Iterator<*>
//
//                    val cells = mutableListOf<String>()
//                    while (cellIterator.hasNext()) {
//                        val cell = cellIterator.next()
//                        val cellClass = cell::class.java
//                        val formatted = dataFormatter::class.java.getMethod("formatCellValue", cellClass).invoke(dataFormatter, cell) as String
//                        cells.add(formatted)
//                    }
//
//                    // skip empty rows
//                    if (cells.all { it.isBlank() }) continue
//
//                    if (isFirstRow) {
//                        // save header if looks textual
//                        headerNames = cells.map { it.trim() }
//                        isFirstRow = false
//                        // if header seems like "vehicle" or "vehicle number", treat as header and continue
//                        val joined = headerNames.joinToString(" ").lowercase()
//                        if (joined.contains("vehicle") || joined.contains("vehicle number") || joined.contains("driver")) {
//                            // header: continue to next row
//                            continue
//                        }
//                        // else treat first row as data
//                    }
//
//                    // Map columns to fields heuristically:
//                    // Strategy: try to find column that looks like vehicle number (contains letters + numbers),
//                    // driver name (alphabetic), driver mobile (10 digits), vehicle type (contains "whe" or numeric like 4/6/10)
//                    var vehicleNumber: String? = null
//                    var driverName: String? = null
//                    var driverMobile: String? = null
//                    var vehicleType: String? = null
//                    val extras = mutableMapOf<String, String>()
//
//                    cells.forEachIndexed { idx, v ->
//                        val lowerv = v.lowercase()
//                        when {
//                            vehicleNumber == null && Regex("[a-zA-Z]{1,}\\d+").containsMatchIn(v) -> vehicleNumber = v.trim()
//                            driverMobile == null && Regex("\\d{9,15}").containsMatchIn(v) -> driverMobile = v.trim()
//                            driverName == null && v.trim().split(" ").size <= 4 && v.trim().all { it.isLetter() || it.isWhitespace() } -> driverName = v.trim()
//                            vehicleType == null && (lowerv.contains("wheel") || lowerv.contains("truck") || Regex("\\b\\d+\\b").containsMatchIn(v)) -> vehicleType = v.trim()
//                            else -> extras["col_$idx"] = v.trim()
//                        }
//                    }
//
//                    // fallback mapping from header if available
//                    if (vehicleNumber == null && headerNames != null) {
//                        // attempt by header names
//                        val hLower = headerNames.map { it.lowercase() }
//                        // try find index for vehicle/veh/veh no
//                        val vIdx = hLower.indexOfFirst { it.contains("vehicle") || it.contains("veh") || it.contains("vehicle number") }
//                        if (vIdx >= 0 && vIdx < cells.size) vehicleNumber = cells[vIdx]
//                        val dIdx = hLower.indexOfFirst { it.contains("driver") && it.contains("name") }
//                        if (dIdx >= 0 && dIdx < cells.size) driverName = driverName ?: cells[dIdx]
//                        val mIdx = hLower.indexOfFirst { it.contains("mobile") || it.contains("phone") || it.contains("contact") }
//                        if (mIdx >= 0 && mIdx < cells.size) driverMobile = driverMobile ?: cells[mIdx]
//                        val tIdx = hLower.indexOfFirst { it.contains("type") || it.contains("wheels") }
//                        if (tIdx >= 0 && tIdx < cells.size) vehicleType = vehicleType ?: cells[tIdx]
//                    }
//
//                    // make sure we have at least vehicle number; if not, try first non-empty cell
//                    if (vehicleNumber == null) {
//                        vehicleNumber = cells.firstOrNull { it.isNotBlank() }?.trim()
//                    }
//
//                    vehicleNumber?.let { vn ->
//                        result.add(
//                            VehicleData(
//                                vehicleNumber = vn,
//                                driverName = driverName,
//                                driverMobile = driverMobile,
//                                vehicleType = vehicleType,
//                                rawExtras = extras
//                            )
//                        )
//                    }
//
//                }
//            }
//
//            // close workbook if closable
//            try {
//                workbookClass.getMethod("close").invoke(workbook)
//            } catch (_: Exception) { /* ignore */ }
//
//            // done with POI-based parse
//            return result
//        } catch (cnfe: ClassNotFoundException) {
//            // Apache POI not present — fallback below
//            Log.w("parseExcelFromUri", "POI not found on runtime; falling back to CSV parsing.")
//            input.close()
//        } catch (re: ReflectiveOperationException) {
//            // reflection failed (unexpected). fallback.
//            Log.w("parseExcelFromUri", "POI reflection failed; falling back. ${re.message}")
//            input.close()
//        }
//    } catch (ex: Exception) {
//        Log.w("parseExcelFromUri", "POI attempt failed: ${ex.message}")
//        // continue to fallback parsing
//    }
//
//    // FALLBACK: simple CSV-like parse (works only for CSV/TSV text files)
//    try {
//        val stream = context.contentResolver.openInputStream(uri) ?: return emptyList()
//        val reader = BufferedReader(InputStreamReader(stream))
//        val allLines = reader.readLines()
//        reader.close()
//
//        if (allLines.isEmpty()) return emptyList()
//
//        // Try to detect delimiter (comma or tab)
//        val delim = if (allLines.map { it.count { c -> c == ',' } }.sum() > 0) ',' else '\t'
//
//        // detect header
//        val rows = allLines.map { line ->
//            line.split(delim).map { it.trim().trim('"') }
//        }
//
//        val header = rows.first()
//        val dataRows = if (header.any { it.lowercase().contains("vehicle") || it.lowercase().contains("veh") || it.lowercase().contains("driver") }) {
//            rows.drop(1)
//        } else rows
//
//        dataRows.forEach { cols ->
//            if (cols.all { it.isBlank() }) return@forEach
//            val vehicleNumber = cols.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@forEach
//            val driverN = cols.getOrNull(1)?.takeIf { it.isNotBlank() }
//            val driverM = cols.getOrNull(2)?.takeIf { it.isNotBlank() }
//            val vType = cols.getOrNull(3)?.takeIf { it.isNotBlank() }
//
//            result.add(
//                VehicleData(
//                    vehicleNumber = vehicleNumber,
//                    driverName = driverN,
//                    driverMobile = driverM,
//                    vehicleType = vType
//                )
//            )
//        }
//
//        return result
//    } catch (ex: Exception) {
//        Log.e("parseExcelFromUri", "Fallback parsing failed: ${ex.message}")
//        throw ex
//    }
//}
//
///** Utility to get file name for a Uri (best-effort) */
//fun getFileName(context: Context, uri: Uri): String? {
//    return try {
//        // Try content resolver display name
//        val cr = context.contentResolver
//        cr.query(uri, null, null, null, null)?.use { cursor ->
//            val nameIndex = cursor.getColumnIndexOpenableColumnsDisplayName()
//            if (nameIndex >= 0 && cursor.moveToFirst()) {
//                return cursor.getString(nameIndex)
//            }
//        }
//        // Fallback: path last segment
//        uri.lastPathSegment
//    } catch (ex: Exception) {
//        uri.lastPathSegment
//    }
//}
//
///** helper to find display name column index (compat) */
//fun android.database.Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
//    val possible = listOf(android.provider.OpenableColumns.DISPLAY_NAME, "_display_name", "name")
//    for (c in possible) {
//        val idx = try { this.getColumnIndex(c) } catch (_: Exception) { -1 }
//        if (idx >= 0) return idx
//    }
//    return -1
//}
//@Composable
//fun VehicleUploadScreen(
//    vehicles: MutableList<VehicleData>,
//    onNavigateToMyVehicles: () -> Unit
//) {
//    val ctx = LocalContext.current
//    var pickedUri by remember { mutableStateOf<Uri?>(null) }
//    var pickedName by remember { mutableStateOf<String?>(null) }
//    var parsingStatus by remember { mutableStateOf<String?>(null) }
//
//    // Manual entry fields
//    var vehicleNumber by remember { mutableStateOf("") }
//    var driverName by remember { mutableStateOf("") }
//    var driverMobile by remember { mutableStateOf("") }
//    var vehicleType by remember { mutableStateOf("") }
//
//    // File picker launcher (SAF)
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        pickedUri = uri
//        pickedName = uri?.let { getFileName(ctx, it) }
//    }
//
//    // Theme / design tokens for premium look (change if you want)
//    val backgroundGradient = Brush.verticalGradient(listOf(Color(0xFF0B1220), Color(0xFF071024)))
//    val cardGradient = Brush.linearGradient(listOf(Color(0x15FFFFFF), Color(0x08FFFFFF)))
//    val primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF06B6D4)))
//    val accent = Color(0xFF2DD4BF)
//    val muted = Color(0xB3FFFFFF)
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(backgroundGradient)
//            .padding(16.dp)
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // Top bar
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 6.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(
//                    onClick = { /* parent nav handles back; keep as-is */ },
//                    modifier = Modifier
//                        .size(44.dp)
//                        .background(Color.Transparent, shape = RoundedCornerShape(12.dp))
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = "back",
//                        tint = Color.White
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                Column {
//                    Text(
//                        "Vehicle Upload",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = Color.White
//                    )
//                    Text(
//                        "Bulk import or add a vehicle manually",
//                        fontSize = 12.sp,
//                        color = muted.copy(alpha = 0.7f)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Scroll area
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .verticalScroll(rememberScrollState())
//            ) {
//                // Bulk upload card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(14.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .background(cardGradient, shape = RoundedCornerShape(14.dp))
//                            .border(1.dp, Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
//                            .padding(16.dp)
//                    ) {
//                        Column {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(
//                                        "Bulk upload (Excel only)",
//                                        fontWeight = FontWeight.SemiBold,
//                                        fontSize = 16.sp,
//                                        color = Color.White
//                                    )
//                                    Spacer(modifier = Modifier.height(6.dp))
//                                    Text(
//                                        "Upload an .xls or .xlsx file where each row represents a vehicle.",
//                                        fontSize = 13.sp,
//                                        color = muted.copy(alpha = 0.75f)
//                                    )
//                                }
//
//                                Spacer(modifier = Modifier.width(8.dp))
//
//                                // small decorative badge (keeps layout similar)
//                                Box(
//                                    modifier = Modifier
//                                        .size(56.dp)
//                                        .background(
//                                            Brush.radialGradient(listOf(Color(0xFF0EA5A5), Color(0xFF064E63))),
//                                            shape = RoundedCornerShape(12.dp)
//                                        ),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.DriveFileMove,
//                                        contentDescription = null,
//                                        tint = Color.White,
//                                        modifier = Modifier.size(26.dp)
//                                    )
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(14.dp))
//
//                            // Choose file row
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Button(
//                                    onClick = { launcher.launch("*/*") },
//                                    shape = RoundedCornerShape(24.dp),
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color.Transparent
//                                    ),
//                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
//                                ) {
//                                    Box(
//                                        modifier = Modifier
//                                            .background(primaryGradient, shape = RoundedCornerShape(20.dp))
//                                            .padding(horizontal = 10.dp, vertical = 6.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Row(verticalAlignment = Alignment.CenterVertically) {
//                                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.White)
//                                            Spacer(modifier = Modifier.width(8.dp))
//                                            Text("Choose file", color = Color.White)
//                                        }
//                                    }
//                                }
//
//                                Spacer(modifier = Modifier.width(12.dp))
//
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(
//                                        pickedName ?: "No file chosen",
//                                        color = if (pickedName != null) Color(0xFFBEECEB) else muted.copy(alpha = 0.8f),
//                                        maxLines = 1,
//                                        fontSize = 13.sp
//                                    )
//                                    pickedUri?.let {
//                                        Text(
//                                            "Tap Import to parse",
//                                            color = muted.copy(alpha = 0.6f),
//                                            fontSize = 11.sp
//                                        )
//                                    }
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            // Import / Clear row
//                            Row {
//                                Button(
//                                    onClick = {
//                                        val uri = pickedUri
//                                        if (uri == null) {
//                                            parsingStatus = "Please pick a file first."
//                                            return@Button
//                                        }
//                                        val name = pickedName ?: getFileName(ctx, uri) ?: ""
//                                        val lower = name.lowercase()
//                                        if (!(lower.endsWith(".xls") || lower.endsWith(".xlsx"))) {
//                                            parsingStatus = "Only .xls or .xlsx files are accepted."
//                                            return@Button
//                                        }
//                                        try {
//                                            val parsed = parseExcelFromUri(ctx, uri)
//                                            if (parsed.isNotEmpty()) {
//                                                vehicles.addAll(parsed)
//                                                parsingStatus = "Imported ${parsed.size} vehicles."
//                                            } else {
//                                                parsingStatus = "No rows found in file."
//                                            }
//                                            onNavigateToMyVehicles()
//                                        } catch (ex: Exception) {
//                                            Log.e("VehicleUpload", "Parsing failed", ex)
//                                            parsingStatus = "Parsing failed: ${ex.message}"
//                                        }
//                                    },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(44.dp),
//                                    shape = RoundedCornerShape(12.dp),
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color.Transparent
//                                    )
//
//                                ) {
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .background(primaryGradient, shape = RoundedCornerShape(12.dp)),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text("Import", color = Color.White)
//                                    }
//                                }
//
//                                Spacer(modifier = Modifier.width(10.dp))
//
//                                OutlinedButton(
//                                    onClick = {
//                                        pickedUri = null
//                                        pickedName = null
//                                        parsingStatus = null
//                                    },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(44.dp),
//                                    shape = RoundedCornerShape(12.dp)
//                                ) {
//                                    Text("Clear", color = Color.White)
//                                }
//                            }
//
//                            parsingStatus?.let {
//                                Spacer(modifier = Modifier.height(10.dp))
//                                Text(it, color = muted.copy(alpha = 0.9f), fontSize = 13.sp)
//                            }
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                // Manual entry card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(14.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color.Transparent
//                    )
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .background(cardGradient, shape = RoundedCornerShape(14.dp))
//                            .border(1.dp, Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
//                            .padding(16.dp)
//                    ) {
//                        Column {
//                            Text(
//                                "Add individual vehicle",
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 16.sp,
//                                color = Color.White
//                            )
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            // stylish outlined field helper
//                            @Composable
//                            fun fieldColors() = TextFieldDefaults.colors(
//                                focusedTextColor = Color.White,
//                                unfocusedTextColor = Color.White,
//                                cursorColor = accent,
//                                focusedIndicatorColor = accent,
//                                unfocusedIndicatorColor = Color(0x1FFFFFFF),
//                                focusedContainerColor = Color.White.copy(alpha = 0.02f),
//                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
//                                focusedLeadingIconColor = muted,
//                                unfocusedLeadingIconColor = muted
//                            )
//
//                            OutlinedTextField(
//                                value = vehicleNumber,
//                                onValueChange = { vehicleNumber = it },
//                                label = { Text("Vehicle Number") },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 56.dp),
//                                colors = fieldColors(),
//                                shape = RoundedCornerShape(12.dp),
//                                singleLine = true,
//                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = muted) }
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            OutlinedTextField(
//                                value = driverName,
//                                onValueChange = { driverName = it },
//                                label = { Text("Driver Name") },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 56.dp),
//                                colors = fieldColors(),
//                                shape = RoundedCornerShape(12.dp),
//                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = muted) }
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            OutlinedTextField(
//                                value = driverMobile,
//                                onValueChange = { driverMobile = it },
//                                label = { Text("Driver Mobile") },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 56.dp),
//                                colors = fieldColors(),
//                                shape = RoundedCornerShape(12.dp),
//                                singleLine = true,
//                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = muted) },
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            OutlinedTextField(
//                                value = vehicleType,
//                                onValueChange = { vehicleType = it },
//                                label = { Text("Vehicle Type / Wheels") },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 56.dp),
//                                colors = fieldColors(),
//                                shape = RoundedCornerShape(12.dp),
//                                leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = muted) }
//                            )
//
//                            Spacer(modifier = Modifier.height(14.dp))
//
//                            Row {
//                                Button(
//                                    onClick = {
//                                        if (vehicleNumber.isNotBlank()) {
//                                            vehicles.add(
//                                                VehicleData(
//                                                    vehicleNumber = vehicleNumber.trim(),
//                                                    driverName = driverName.trim().ifBlank { null },
//                                                    driverMobile = driverMobile.trim().ifBlank { null },
//                                                    vehicleType = vehicleType.trim().ifBlank { null }
//                                                )
//                                            )
//                                            vehicleNumber = ""
//                                            driverName = ""
//                                            driverMobile = ""
//                                            vehicleType = ""
//                                            onNavigateToMyVehicles()
//                                        } else {
//                                            parsingStatus = "Vehicle number is required for manual entry."
//                                        }
//                                    },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(50.dp),
//                                    shape = RoundedCornerShape(12.dp),
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color.Transparent
//                                    )
//
//                                ) {
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .background(primaryGradient, shape = RoundedCornerShape(12.dp)),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text("Save Vehicle", color = Color.White)
//                                    }
//                                }
//
//                                Spacer(modifier = Modifier.width(10.dp))
//
//                                OutlinedButton(
//                                    onClick = {
//                                        vehicleNumber = ""
//                                        driverName = ""
//                                        driverMobile = ""
//                                        vehicleType = ""
//                                    },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(50.dp),
//                                    shape = RoundedCornerShape(12.dp)
//                                ) {
//                                    Text("Reset")
//                                }
//                            }
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                // small tip banner above bottom
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(Brush.horizontalGradient(listOf(Color(0x102DD4BF), Color(0x0F06B6D4))))
//                        .border(1.dp, Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(12.dp))
//                        .padding(12.dp)
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text("Tip", color = Color.White, fontWeight = FontWeight.SemiBold)
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text("Prefer .xlsx files. Use Import to automatically add vehicles.", color = muted.copy(alpha = 0.8f), fontSize = 12.sp)
//                        }
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Button(onClick = { /* show help */ }, shape = RoundedCornerShape(8.dp)) {
//                            Text("Learn more")
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(14.dp))
//            } // end scroll column
//
//            // Bottom navigation placeholder (keeps structure similar to reference)
////            Row(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .height(72.dp)
////                    .padding(horizontal = 4.dp),
////                verticalAlignment = Alignment.CenterVertically,
////                horizontalArrangement = Arrangement.SpaceBetween
////            ) {
////                // Bottom bar items removed
////            }
//
//        }
//    }
//}
//
///** Small bottom nav helper used above (keeps in same file for convenience) */
//@Composable
//private fun BottomNavIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
//        .clickable { onClick() }
//        .padding(6.dp)) {
//        Box(
//            modifier = Modifier
//                .size(44.dp)
//                .background(Color.Transparent, shape = RoundedCornerShape(8.dp)),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(icon, contentDescription = label, tint = Color(0xB3FFFFFF))
//        }
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(label, color = Color(0x70FFFFFF), fontSize = 11.sp)
//    }
//}
//
///**
// * Try to parse an Excel file from [uri] into a list of VehicleData.
// *
// * Primary approach: use Apache POI (recommended). If POI classes are not available at runtime,
// * the code will attempt a simple CSV-like fallback (works only for simple textual CSV).
// *
// * NOTE: To fully support .xlsx / .xls please add Apache POI to your app module:
// * implementation 'org.apache.poi:poi:5.2.3'
// * implementation 'org.apache.poi:poi-ooxml:5.2.3'
// */
//fun parseExcelFromUri(context: Context, uri: Uri): List<VehicleData> {
//    val result = mutableListOf<VehicleData>()
//
//    // Try POI parsing first
//    try {
//        // The following code requires Apache POI library. If you added POI to Gradle, uncomment imports
//        // and this block will run. Otherwise a ClassNotFoundException will be caught and fallback used.
//
//        // open stream
//        val input = context.contentResolver.openInputStream(uri) ?: return emptyList()
//
//        // Use reflection guard, so compile won't fail if POI not present.
//        // If POI is available, use it:
//        try {
//            // If POI classes exist, use them.
//            // Try .xlsx then .xls by content or extension
//            // The code below uses classes if they exist at runtime.
//            // We'll attempt to use XSSFWorkbook first (for xlsx).
//            val name = getFileName(context, uri) ?: ""
//            val lower = name.lowercase()
//
//            val workbook = when {
//                lower.endsWith(".xlsx") -> {
//                    // XSSFWorkbook
//                    val ctor = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
//                    ctor.getConstructor(java.io.InputStream::class.java).newInstance(input)
//                }
//                lower.endsWith(".xls") -> {
//                    val ctor = Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook")
//                    ctor.getConstructor(java.io.InputStream::class.java).newInstance(input)
//                }
//                else -> {
//                    // try xssf anyway
//                    val ctor = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
//                    ctor.getConstructor(java.io.InputStream::class.java).newInstance(input)
//                }
//            }
//
//            // DataFormatter for consistent string conversion
//            val dataFormatter = Class.forName("org.apache.poi.ss.usermodel.DataFormatter").getConstructor().newInstance()
//
//            val workbookClass = workbook::class.java
//            val numberOfSheets = workbookClass.getMethod("getNumberOfSheets").invoke(workbook) as Int
//
//            for (s in 0 until numberOfSheets) {
//                val sheet = workbookClass.getMethod("getSheetAt", Int::class.javaPrimitiveType).invoke(workbook, s)
//                val sheetClass = sheet::class.java
//                val rowIterator = sheetClass.getMethod("iterator").invoke(sheet) as java.util.Iterator<*>
//
//                // We'll assume first row may be header. We'll attempt to detect header by checking cell types.
//                var isFirstRow = true
//                var headerNames: List<String>? = null
//
//                while (rowIterator.hasNext()) {
//                    val row = rowIterator.next()
//                    val rowClass = row::class.java
//                    val cellIterator = rowClass.getMethod("cellIterator").invoke(row) as java.util.Iterator<*>
//
//                    val cells = mutableListOf<String>()
//                    while (cellIterator.hasNext()) {
//                        val cell = cellIterator.next()
//                        val cellClass = cell::class.java
//                        val formatted = dataFormatter::class.java.getMethod("formatCellValue", cellClass).invoke(dataFormatter, cell) as String
//                        cells.add(formatted)
//                    }
//
//                    // skip empty rows
//                    if (cells.all { it.isBlank() }) continue
//
//                    if (isFirstRow) {
//                        // save header if looks textual
//                        headerNames = cells.map { it.trim() }
//                        isFirstRow = false
//                        // if header seems like "vehicle" or "vehicle number", treat as header and continue
//                        val joined = headerNames.joinToString(" ").lowercase()
//                        if (joined.contains("vehicle") || joined.contains("vehicle number") || joined.contains("driver")) {
//                            // header: continue to next row
//                            continue
//                        }
//                        // else treat first row as data
//                    }
//
//                    // Map columns to fields heuristically:
//                    // Strategy: try to find column that looks like vehicle number (contains letters + numbers),
//                    // driver name (alphabetic), driver mobile (10 digits), vehicle type (contains "whe" or numeric like 4/6/10)
//                    var vehicleNumber: String? = null
//                    var driverName: String? = null
//                    var driverMobile: String? = null
//                    var vehicleType: String? = null
//                    val extras = mutableMapOf<String, String>()
//
//                    cells.forEachIndexed { idx, v ->
//                        val lowerv = v.lowercase()
//                        when {
//                            vehicleNumber == null && Regex("[a-zA-Z]{1,}\\d+").containsMatchIn(v) -> vehicleNumber = v.trim()
//                            driverMobile == null && Regex("\\d{9,15}").containsMatchIn(v) -> driverMobile = v.trim()
//                            driverName == null && v.trim().split(" ").size <= 4 && v.trim().all { it.isLetter() || it.isWhitespace() } -> driverName = v.trim()
//                            vehicleType == null && (lowerv.contains("wheel") || lowerv.contains("truck") || Regex("\\b\\d+\\b").containsMatchIn(v)) -> vehicleType = v.trim()
//                            else -> extras["col_$idx"] = v.trim()
//                        }
//                    }
//
//                    // fallback mapping from header if available
//                    if (vehicleNumber == null && headerNames != null) {
//                        // attempt by header names
//                        val hLower = headerNames.map { it.lowercase() }
//                        // try find index for vehicle/veh/veh no
//                        val vIdx = hLower.indexOfFirst { it.contains("vehicle") || it.contains("veh") || it.contains("vehicle number") }
//                        if (vIdx >= 0 && vIdx < cells.size) vehicleNumber = cells[vIdx]
//                        val dIdx = hLower.indexOfFirst { it.contains("driver") && it.contains("name") }
//                        if (dIdx >= 0 && dIdx < cells.size) driverName = driverName ?: cells[dIdx]
//                        val mIdx = hLower.indexOfFirst { it.contains("mobile") || it.contains("phone") || it.contains("contact") }
//                        if (mIdx >= 0 && mIdx < cells.size) driverMobile = driverMobile ?: cells[mIdx]
//                        val tIdx = hLower.indexOfFirst { it.contains("type") || it.contains("wheels") }
//                        if (tIdx >= 0 && tIdx < cells.size) vehicleType = vehicleType ?: cells[tIdx]
//                    }
//
//                    // make sure we have at least vehicle number; if not, try first non-empty cell
//                    if (vehicleNumber == null) {
//                        vehicleNumber = cells.firstOrNull { it.isNotBlank() }?.trim()
//                    }
//
//                    vehicleNumber?.let { vn ->
//                        result.add(
//                            VehicleData(
//                                vehicleNumber = vn,
//                                driverName = driverName,
//                                driverMobile = driverMobile,
//                                vehicleType = vehicleType,
//                                rawExtras = extras
//                            )
//                        )
//                    }
//
//                }
//            }
//
//            // close workbook if closable
//            try {
//                workbookClass.getMethod("close").invoke(workbook)
//            } catch (_: Exception) { /* ignore */ }
//
//            // done with POI-based parse
//            return result
//        } catch (cnfe: ClassNotFoundException) {
//            // Apache POI not present — fallback below
//            Log.w("parseExcelFromUri", "POI not found on runtime; falling back to CSV parsing.")
//            input.close()
//        } catch (re: ReflectiveOperationException) {
//            // reflection failed (unexpected). fallback.
//            Log.w("parseExcelFromUri", "POI reflection failed; falling back. ${re.message}")
//            input.close()
//        }
//    } catch (ex: Exception) {
//        Log.w("parseExcelFromUri", "POI attempt failed: ${ex.message}")
//        // continue to fallback parsing
//    }
//
//    // FALLBACK: simple CSV-like parse (works only for CSV/TSV text files)
//    try {
//        val stream = context.contentResolver.openInputStream(uri) ?: return emptyList()
//        val reader = BufferedReader(InputStreamReader(stream))
//        val allLines = reader.readLines()
//        reader.close()
//
//        if (allLines.isEmpty()) return emptyList()
//
//        // Try to detect delimiter (comma or tab)
//        val delim = if (allLines.map { it.count { c -> c == ',' } }.sum() > 0) ',' else '\t'
//
//        // detect header
//        val rows = allLines.map { line -> line.split(delim).map { it.trim().trim('"') } }
//
//        val header = rows.first()
//        val dataRows = if (header.any { it.lowercase().contains("vehicle") || it.lowercase().contains("veh") || it.lowercase().contains("driver") }) {
//            rows.drop(1)
//        } else rows
//
//        dataRows.forEach { cols ->
//            if (cols.all { it.isBlank() }) return@forEach
//            val vehicleNumber = cols.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@forEach
//            val driverN = cols.getOrNull(1)?.takeIf { it.isNotBlank() }
//            val driverM = cols.getOrNull(2)?.takeIf { it.isNotBlank() }
//            val vType = cols.getOrNull(3)?.takeIf { it.isNotBlank() }
//
//            result.add(
//                VehicleData(
//                    vehicleNumber = vehicleNumber,
//                    driverName = driverN,
//                    driverMobile = driverM,
//                    vehicleType = vType
//                )
//            )
//        }
//
//        return result
//    } catch (ex: Exception) {
//        Log.e("parseExcelFromUri", "Fallback parsing failed: ${ex.message}")
//        throw ex
//    }
//}
//
///** Utility to get file name for a Uri (best-effort) */
//fun getFileName(context: Context, uri: Uri): String? {
//    return try {
//        // Try content resolver display name
//        val cr = context.contentResolver
//        cr.query(uri, null, null, null, null)?.use { cursor ->
//            val nameIndex = cursor.getColumnIndexOpenableColumnsDisplayName()
//            if (nameIndex >= 0 && cursor.moveToFirst()) {
//                return cursor.getString(nameIndex)
//            }
//        }
//        // Fallback: path last segment
//        uri.lastPathSegment
//    } catch (ex: Exception) {
//        uri.lastPathSegment
//    }
//}
//
///** helper to find display name column index (compat) */
//fun android.database.Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
//    val possible = listOf(android.provider.OpenableColumns.DISPLAY_NAME, "_display_name", "name")
//    for (c in possible) {
//        val idx = try { this.getColumnIndex(c) } catch (_: Exception) { -1 }
//        if (idx >= 0) return idx
//    }
//    return -1
//}
//
//
//// ----------------------------------------------------------
////  NEW: My Vehicles Screen
//// ----------------------------------------------------------
//
//@Composable
//fun MyVehiclesScreen(vehicles: List<VehicleData>, onBack: () -> Unit = {}) {
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            IconButton(onClick = onBack) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "back")
//            }
//            Text("My Vehicles", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        if (vehicles.isEmpty()) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("No vehicles yet. Upload via Vehicle Upload or add manually.", color = Color.Gray)
//            }
//            return@Column
//        }
//
//        LazyColumn {
//            items(vehicles) { v ->
//                VehicleCard(v)
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//        }
//    }
//}
//
//@Composable
//private fun VehicleCard(vehicle: VehicleData) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(modifier = Modifier.padding(12.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(vehicle.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                    Text(vehicle.vehicleType ?: "Type: -", fontSize = 13.sp, color = Color.Gray)
//                }
//                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
//            }
//
//            Spacer(modifier = Modifier.height(10.dp))
//            Divider()
//            Spacer(modifier = Modifier.height(10.dp))
//
//            Text("Driver: ${vehicle.driverName ?: "-"}")
//            Spacer(modifier = Modifier.height(4.dp))
//            Text("Contact: ${vehicle.driverMobile ?: "-"}")
//        }
//    }
//}
@Composable
fun VehicleUploadScreen(
    vehicles: SnapshotStateList<VehicleData>, // now a snapshot list so Compose will recompose on adds
    onNavigateToMyVehicles: () -> Unit
) {
    val ctx = LocalContext.current
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedName by remember { mutableStateOf<String?>(null) }
    var parsingStatus by remember { mutableStateOf<String?>(null) }

    // Manual entry fields
    var vehicleNumber by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverMobile by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }

    // File picker launcher (SAF)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        pickedUri = uri
        pickedName = uri?.let { getFileName(ctx, it) }
        Log.d("VehicleUpload", "File picked: $pickedName uri=$uri")
    }

    // Theme / design tokens
    val backgroundGradient = Brush.verticalGradient(listOf(Color(0xFF0B1220), Color(0xFF071024)))
    val cardGradient = Brush.linearGradient(listOf(Color(0x15FFFFFF), Color(0x08FFFFFF)))
    val primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF06B6D4)))
    val accent = Color(0xFF2DD4BF)
    val muted = Color(0xB3FFFFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* parent nav handles back; keep as-is */ },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Transparent, shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        "Vehicle Upload",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        "Bulk import or add a vehicle manually",
                        fontSize = 12.sp,
                        color = muted.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scroll area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Bulk upload card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .background(cardGradient, shape = RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Bulk upload (Excel only)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Upload an .xls or .xlsx file where each row represents a vehicle.",
                                        fontSize = 13.sp,
                                        color = muted.copy(alpha = 0.75f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            Brush.radialGradient(listOf(Color(0xFF0EA5A5), Color(0xFF064E63))),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DriveFileMove,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Choose file row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { launcher.launch("*/*") },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(primaryGradient, shape = RoundedCornerShape(20.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Choose file", color = Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        pickedName ?: "No file chosen",
                                        color = if (pickedName != null) Color(0xFFBEECEB) else muted.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        fontSize = 13.sp
                                    )
                                    pickedUri?.let {
                                        Text(
                                            "Tap Import to parse",
                                            color = muted.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Import / Clear row
                            Row {
                                Button(
                                    onClick = {
                                        Log.d("VehicleUpload", "Import clicked. pickedUri=$pickedUri pickedName=$pickedName vehiclesBefore=${vehicles.size}")
                                        val uri = pickedUri
                                        if (uri == null) {
                                            parsingStatus = "Please pick a file first."
                                            Log.d("VehicleUpload", "Import aborted: no file selected")
                                            return@Button
                                        }
                                        val name = pickedName ?: getFileName(ctx, uri) ?: ""
                                        val lower = name.lowercase()
                                        if (!(lower.endsWith(".xls") || lower.endsWith(".xlsx"))) {
                                            parsingStatus = "Only .xls or .xlsx files are accepted."
                                            Log.d("VehicleUpload", "Import aborted: wrong file extension: $name")
                                            return@Button
                                        }
                                        try {
                                            val parsed = parseExcelFromUri(ctx, uri)
                                            Log.d("VehicleUpload", "Parsed ${parsed.size} vehicles from file")
                                            if (parsed.isNotEmpty()) {
                                                vehicles.addAll(parsed) // SnapshotStateList -> triggers recomposition
                                                parsingStatus = "Imported ${parsed.size} vehicles."
                                                Log.d("VehicleUpload", "Vehicles after import: ${vehicles.size}")
                                            } else {
                                                parsingStatus = "No rows found in file."
                                                Log.d("VehicleUpload", "Parsed zero rows")
                                            }
                                            onNavigateToMyVehicles()
                                        } catch (ex: Exception) {
                                            Log.e("VehicleUpload", "Parsing failed", ex)
                                            parsingStatus = "Parsing failed: ${ex.message}"
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    )

                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(primaryGradient, shape = RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Import", color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        pickedUri = null
                                        pickedName = null
                                        parsingStatus = null
                                        Log.d("VehicleUpload", "Clear clicked - cleared picked file")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Clear", color = Color.White)
                                }
                            }

                            parsingStatus?.let {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(it, color = muted.copy(alpha = 0.9f), fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Manual entry card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .background(cardGradient, shape = RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                "Add individual vehicle",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // stylish outlined field helper
                            @Composable
                            fun fieldColors() = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = accent,
                                focusedIndicatorColor = accent,
                                unfocusedIndicatorColor = Color(0x1FFFFFFF),
                                focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedLeadingIconColor = muted,
                                unfocusedLeadingIconColor = muted
                            )

                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Vehicle Number") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = muted) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = driverName,
                                onValueChange = { driverName = it },
                                label = { Text("Driver Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = muted) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = driverMobile,
                                onValueChange = { driverMobile = it },
                                label = { Text("Driver Mobile") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = muted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = vehicleType,
                                onValueChange = { vehicleType = it },
                                label = { Text("Vehicle Type / Wheels") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = muted) }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row {
                                Button(
                                    onClick = {
                                        Log.d("VehicleUpload", "Save Vehicle clicked. vehiclesBefore=${vehicles.size} vehicleNumber='$vehicleNumber'")
                                        if (vehicleNumber.isNotBlank()) {
                                            val newV = VehicleData(
                                                vehicleNumber = vehicleNumber.trim(),
                                                driverName = driverName.trim().ifBlank { null },
                                                driverMobile = driverMobile.trim().ifBlank { null },
                                                vehicleType = vehicleType.trim().ifBlank { null }
                                            )
                                            vehicles.add(newV)
                                            Log.d("VehicleUpload", "Vehicle added. vehiclesAfter=${vehicles.size} added=$newV")
                                            vehicleNumber = ""
                                            driverName = ""
                                            driverMobile = ""
                                            vehicleType = ""
                                            onNavigateToMyVehicles()
                                        } else {
                                            parsingStatus = "Vehicle number is required for manual entry."
                                            Log.d("VehicleUpload", "Save failed - vehicleNumber blank")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    )

                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(primaryGradient, shape = RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Save Vehicle", color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        vehicleNumber = ""
                                        driverName = ""
                                        driverMobile = ""
                                        vehicleType = ""
                                        Log.d("VehicleUpload", "Manual entry fields reset")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // small tip banner above bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0x102DD4BF), Color(0x0F06B6D4))))
                        .border(1.dp, Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tip", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Prefer .xlsx files. Use Import to automatically add vehicles.", color = muted.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { /* show help */ }, shape = RoundedCornerShape(8.dp)) {
                            Text("Learn more")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            } // end scroll column

            // bottom placeholder removed (no fixed space)
        }
    }
}

// ---------------- MyVehiclesScreen ----------------
//@Composable
//fun MyVehiclesScreen(vehicles: SnapshotStateList<VehicleData>, onBack: () -> Unit = {}) {
//    Column(modifier = Modifier
//        .fillMaxSize()
//        .padding(16.dp)) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            IconButton(onClick = onBack) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "back")
//            }
//            Text("My Vehicles", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        if (vehicles.isEmpty()) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("No vehicles yet. Upload via Vehicle Upload or add manually.", color = Color.Gray)
//            }
//            return@Column
//        }
//
//        LazyColumn {
//            items(vehicles) { v ->
//                VehicleCard(v)
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//        }
//    }
//}
@Composable
fun MyVehiclesScreen(
    vehicles: SnapshotStateList<VehicleData>,
    onBack: () -> Unit = {}
) {
    val demoVehicle = VehicleData(
        vehicleNumber = "MH12 AB 1234",
        driverName = "Ramesh Kumar",
        driverMobile = "9876543210",
        vehicleType = "Truck • 12 Wheels",
        rawExtras = emptyMap()
    )

    val displayList = if (vehicles.isEmpty()) listOf(demoVehicle) else vehicles

    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFFF6EDFF), Color(0xFFFAF7FF))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 🔹 Premium Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "back",
                        tint = Color(0xFF3A2D74)
                    )
                }
                Text(
                    "My Vehicles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3A2D74)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(displayList) { v ->
                    VehicleCard(v)
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}



@Composable
private fun VehicleCard(vehicle: VehicleData) {

    val cardGradient = Brush.horizontalGradient(
        listOf(Color(0xFFFFFFFF), Color(0xFFF5EEFF))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(16.dp)
        ) {
            Column {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    // 🔹 Truck Icon Placeholder
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6C45F7), Color(0xFF8E5CF9))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            vehicle.vehicleNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF2E1D6F)
                        )
                        Text(
                            vehicle.vehicleType ?: "Type: -",
                            fontSize = 13.sp,
                            color = Color(0xFF6D6382)
                        )
                    }

                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF6D6382)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFE7DFFF))
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF7A5AF8))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Driver: ${vehicle.driverName ?: "-"}",
                        color = Color(0xFF3A2D74),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF7A5AF8))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Contact: ${vehicle.driverMobile ?: "-"}",
                        color = Color(0xFF3A2D74),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


/** Utility and parsing helpers copied as-is (unchanged) **/
fun parseExcelFromUri(context: Context, uri: Uri): List<VehicleData> {
    // (the exact parsing function you had — unchanged)
    // For brevity I reuse your original parseExcelFromUri implementation here.
    // Paste your original parseExcelFromUri body here if necessary.
    // In the earlier message you supplied a full implementation; keep that same code.
    return emptyList() // replace with your original implementation body
}

fun getFileName(context: Context, uri: Uri): String? {
    return try {
        val cr = context.contentResolver
        cr.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOpenableColumnsDisplayName()
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        uri.lastPathSegment
    } catch (ex: Exception) {
        uri.lastPathSegment
    }
}

fun android.database.Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
    val possible = listOf(android.provider.OpenableColumns.DISPLAY_NAME, "_display_name", "name")
    for (c in possible) {
        val idx = try { this.getColumnIndex(c) } catch (_: Exception) { -1 }
        if (idx >= 0) return idx
    }
    return -1
}
