package com.example.vendorapplication


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vendorapplication.navigation.BottomNavItem
import com.google.gson.JsonArray
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.round

@Composable
fun MyTripsScreen(parentNavController: NavHostController) {

    val TAG = "MyTripsScreen"

    // ✅ INNER NavController for nested navigation
    val innerNavController = rememberNavController()

    SampleVendorTheme {
        Surface(
            color = Color(0xFFF6F7FB),
            modifier = Modifier.fillMaxSize()
        ) {

            Log.d(TAG, "Composed MyTripsScreen with inner NavHost")

            NavHost(
                navController = innerNavController,
                startDestination = "my_trips",
                modifier = Modifier.fillMaxSize()
            ) {

                composable("my_trips") {
                    Log.d(TAG, "Open: my_trips (inner)")
                    BoxWithConstraints {
                        val screenWidth = maxWidth
                        MainContentWithNav(
                            screenWidth = screenWidth,
                            navController = parentNavController // use parent for global nav
                        )
                    }
                }

                composable("select_vehicle") {
                    Log.d(TAG, "Open: select_vehicle (inner)")
                    SelectVehicleScreen()
                }

                composable("my_load_detail") {
                    Log.d(TAG, "Open: my_load_detail (inner)")
                    MyLoadDetailScreen()
                }

                composable("my_load_preview") {
                    Log.d(TAG, "Open: my_load_preview (inner)")
                    MyLoadScreen(parentNavController)
                }

                composable(BottomNavItem.Load.route) {
                    Log.d(TAG, "Open: Load Screen (parent route called from inner)")
                    LoadScreen(parentNavController)
                }

                composable("NEWLOAD") {
                    Log.d(TAG, "Open: New Load Screen (parent route called from inner)")
                    NewLoadScreenCompose(parentNavController)
                }

                composable("new_load_screen") {
                    Log.d(TAG, "Open: new_load_screen (parent route called from inner)")
                    NewLoadScreenCompose(parentNavController)
                }

                // ✅ vehicle_kyc inside inner graph
                composable("vehicle_kyc") {

                    Log.d(TAG, "Open: vehicle_kyc (inner)")

                    val authToken =
                        "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTk5MDczNTQsInVzZXJJZCI6IjIyYWM1NjFkLWRhNWMtNDE2Ni1hYmJhLWQ5NzNlMmYwNDZhMyIsImVtYWlsIjoibmlraGlsLm1haGFyYUBjamRhcmNsLmNvbSIsIm1vYmlsZU51bWJlciI6Ijk2NTQ2MzI3NDQiLCJvcmdJZCI6ImVmMzAwNjgzLTkwMWItNDc3NC1iZjBlLTk1NWQ0OWU3OTZiYyIsIm5hbWUiOiJOaWtoaWwgU2luZ2ggTWFoYXJhIiwib3JnVHlwZSI6IkZMRUVUX09XTkVSIiwiaXNHb2QiOnRydWUsInBvcnRhbFR5cGUiOiJiYXNpYyJ9.MDDgj22r1Hlu4cbl_qySQpqLndzS1D_JAk0owgnQxJ8"

                    VehicleKycScreen(
                        navController = innerNavController,
                        authToken = authToken,
                        onBack = {
                            Log.d(TAG, "Back from vehicle_kyc")
                            innerNavController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}




@Composable
fun MainContentWithNav(screenWidth: Dp, navController: NavHostController) {

    val selectedTab = remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {  },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopRow()
            Spacer(modifier = Modifier.height(12.dp))
            StatusCard()
            Spacer(modifier = Modifier.height(18.dp))
            PromoBanner(screenWidth = screenWidth)
            Spacer(modifier = Modifier.height(14.dp))


            TabsWithContent(
                navController = navController,
                selectedIndexState = selectedTab
            )

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TabsWithContent(
    navController: NavHostController,
    selectedIndexState: MutableState<Int>
) {
    val tabs = listOf<TabItem>(
        TabItem.Screen("My Load(1)") {
            MyLoadScreen(navController)
        },
        TabItem.Route("Load(4)", BottomNavItem.Load.route),
        TabItem.Route("New Load(40)", "NEWLOAD")
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { idx, tab ->
            val selected = idx == selectedIndexState.value

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Color.Black else Color.White)
                    .clickable {
                        Log.d("TabsWithContent", "Tab clicked: ${tabTitle(tab)}")
                        when (tab) {
                            is TabItem.Screen -> {
                                // Screen-type tab: content shown below in this composable
                                Log.d("TabsWithContent", "Open composable: ${tab.title}")
                            }
                            is TabItem.Route -> {
                                val route = tab.route
                                if (route == null) {
                                    Log.e("TabsWithContent", "ERROR: Route is null, cannot navigate!")
                                    return@clickable
                                }

                                Log.d("TabsWithContent", "Navigate to route: $route")
                                navController.navigate(route)
                            }

                            else -> {}
                        }
                        selectedIndexState.value = idx
                    }
            ) {
                Text(
                    text = tabTitle(tab),
                    color = if (selected) Color.White else Color.Gray,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))


    when (val tab = tabs[selectedIndexState.value]) {
        is TabItem.Screen -> {
            tab.content()
        }
        is TabItem.Route -> {

        }
    }
}

private fun tabTitle(tab: TabItem): String {
    return when (tab) {
        is TabItem.Screen -> tab.title
        is TabItem.Route -> tab.title
    }
}


@Composable
fun MyLoadScreen(navController: NavHostController) {

    fun safeNavigate(route: String) {
        Log.d("MyLoadScreen", "Attempting navigation to '$route'")

        val currentRoute = navController.currentDestination?.route
        if (currentRoute != route) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
            Log.d("MyLoadScreen", "Navigation to '$route' executed.")
        } else {
            Log.d("MyLoadScreen", "Already on '$route' — skipping navigation.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        LargeLoadCard(
            navController = navController,

            // ENTER VEHICLE DETAILS NAVIGATION
            onEnterDetails = {
                safeNavigate("select_vehicle")
            },

            // CONFIRM NAVIGATION
            onConfirm = {
                safeNavigate("my_load_detail")
            }
        )
    }
}



@Composable
fun LargeLoadCard(
    navController: NavHostController,
    onEnterDetails: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // --- Pickup & Drop (two rows with markers) ---
            Row(modifier = Modifier.fillMaxWidth()) {
                // left markers column (dot, dashed line, square)
                Column(
                    modifier = Modifier
                        .width(28.dp)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // pickup dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2ECC71))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // dashed line between
                    Canvas(
                        modifier = Modifier
                            .height(36.dp)
                            .width(2.dp)
                    ) {
                        val stroke = 3.dp.toPx()
                        drawLine(
                            color = Color(0xFFCDCDD1),
                            start = Offset(x = size.width / 2, y = 0f),
                            end = Offset(x = size.width / 2, y = size.height),
                            strokeWidth = stroke,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // drop square
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFB91C1C), shape = RoundedCornerShape(2.dp))
                    )
                }

                // addresses column
                Column(modifier = Modifier.weight(1f)) {
                    // pickup address
                    Text(
                        text = "I.A. Surajpur, Gautam Buddha Nagar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "201306  •  UP",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // divider between pickup & drop addresses
                    Divider(color = Color(0xFFE6E6E9), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // drop address
                    Text(
                        text = "Dabra, Gwalior",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "475110  •  MP",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- small info row (icons + texts) ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Container info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "container",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Container | 32 Ft | Goods 4.5 Ton", style = MaterialTheme.typography.bodySmall)
                    }

                    // Loading time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "loading time",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Loading today evening at 4", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // purple info chip
                Surface(
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF3E8FF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "distance",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF7C3AED)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your truck is 27.0 km away from loading",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6D28D9))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Token card with centered timer overlap ---
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                // Token card (placed lower so timer can overlap visually)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp), // leave space for the timer overlap
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp)) // extra spacing to account for timer
                        Text(
                            text = "Token received",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Enter vehicle details button (full width)
                        Button(
                            onClick = onEnterDetails,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = "Enter vehicle details",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Timer badge overlapping (positioned at top center of the card)
                Box(
                    modifier = Modifier
                        .offset(y = 0.dp)
                        .background(Color(0xFFFFFF00), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFF1C40F), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "02 : 07",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF2B2B2B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Party rate + Confirm button (keep position as you requested) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Party's Rate",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹ 29300",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .width(110.dp)
                ) {
                    Text(text = "Confirm", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}



@Composable
private fun IconRowItem(icon: ImageVector, primaryText: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(22.dp)
                .padding(end = 8.dp),
            tint = Color(0xFF111827)
        )
        Text(
            text = primaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151)
        )
    }
}

@Composable
private fun TopRow(
    navController: NavController? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            // Priority: NavController -> provided onBack lambda -> activity fallback
            val handledByNav = navController?.let {
                // popBackStack returns true if successful (but may return false if nothing to pop)
                it.popBackStack()
            } ?: false

            if (!handledByNav) {
                when {
                    onBack != null -> onBack()
                    else -> {
                        // safe activity fallback (no ClassCastException)
                        (context as? ComponentActivity)
                            ?.onBackPressedDispatcher
                            ?.onBackPressed()
                    }
                }
            }
        }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF111827)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFEEEEEE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("A1", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Load alert",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var switchOn by remember { mutableStateOf(true) }
        Switch(
            checked = switchOn,
            onCheckedChange = { switchOn = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2E7D32),
                uncheckedTrackColor = Color(0xFFBDBDBD)
            )
        )
    }
}

@Composable
private fun StatusCard() {
    val TAG = "StatusCard"
    var showPreview by remember { mutableStateOf(false) }
    var showUnloadingPreview by remember { mutableStateOf(false) }
    val green = Color(0xFF23A455)


    val loadingCities = listOf(
        "All NCR", "Central Delhi", "East Delhi", "New Delhi",
        "North Delhi", "North East Delhi", "North West Delhi",
        "Shahdara", "South Delhi", "South East Delhi", "South West Delhi"
    )

    val unloadingCities = listOf(
        "Rajasthan", "Himachal Pradesh", "Nagaland", "Uttarakhand",
        "Andhra Pradesh", "Madhya Pradesh", "Lakshadweep", "Meghalaya",
        "Sikkim", "Kerala", "Chhattisgarh", "Tamil Nadu"
    )


    val loadingChecked =
        remember { mutableStateListOf<Boolean>().apply { repeat(loadingCities.size) { add(false) } } }
    val unloadingChecked =
        remember { mutableStateListOf<Boolean>().apply { repeat(unloadingCities.size) { add(false) } } }


    var loadingPlaceholder by remember { mutableStateOf("कृपया शहर चुनें") }
    var unloadingPlaceholder by remember { mutableStateOf("कृपया शहर चुनें") }

    Log.d(TAG, "Composed — showPreview=$showPreview showUnloadingPreview=$showUnloadingPreview")


    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Loading
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Loading",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loadingPlaceholder,
                        fontSize = 13.sp,
                        color = Color(0xFF6B6B6B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        showPreview = true
                        Log.d(TAG, "Open preview clicked (Loading)")
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open preview"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Unloading",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = unloadingPlaceholder,
                        fontSize = 13.sp,
                        color = Color(0xFF6B6B6B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        showUnloadingPreview = true
                        Log.d(TAG, "Open preview clicked (Unloading)")
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open preview"
                    )
                }
            }
        }
    }


    if (showPreview) {
        Log.d(TAG, "Loading Preview opened (Dialog shown)")
        Dialog(
            onDismissRequest = {
                showPreview = false
                Log.d(TAG, "Loading Preview dismissed (dialog onDismiss)")
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))

                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Top bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
                        ) {
                            IconButton(onClick = {
                                showPreview = false
                                Log.d(TAG, "Loading Preview closed (Top close clicked)")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                            Text(
                                text = "Select Loading",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 68.dp, bottom = 88.dp)
                        ) {
                            val states = listOf(
                                "NCR" to "NCR",
                                "DL" to "DELHI",
                                "GJ" to "GUJARAT",
                                "HR" to "HARYANA",
                                "KA" to "KARNATAKA",
                                "MH" to "MAHARASHTRA",
                                "RJ" to "RAJASTHAN"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(0.28f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF6F6F8))
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(states) { item ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = item.first,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = item.second,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                                    }
                                }
                            }


                            Column(
                                modifier = Modifier
                                    .weight(0.72f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "शहर चुने",
                                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                                    fontWeight = FontWeight.Medium
                                )

                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(loadingCities) { idx, city ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp)
                                        ) {
                                            Checkbox(
                                                checked = loadingChecked[idx],
                                                onCheckedChange = {
                                                    loadingChecked[idx] = it
                                                    Log.d(
                                                        TAG,
                                                        "Loading city checked: index=$idx, city='$city', checked=$it"
                                                    )
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = green,
                                                    uncheckedColor = Color.Gray,
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = city)
                                        }
                                        Divider(color = Color(0xFFFAFAFA), thickness = 1.dp)
                                    }
                                }
                            }
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 18.dp, vertical = 18.dp)
                        ) {
                            Button(
                                onClick = {
                                    val selected =
                                        loadingCities.mapIndexedNotNull { i, c -> if (loadingChecked[i]) c else null }
                                    loadingPlaceholder =
                                        if (selected.isEmpty()) "None selected" else selected.joinToString(
                                            ", "
                                        )
                                    showPreview = false
                                    Log.d(TAG, "Loading View Loads clicked — selected=$selected")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green)
                            ) {
                                Text(
                                    text = "View Loads",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    if (showUnloadingPreview) {
        Log.d(TAG, "Unloading Preview opened (Dialog shown)")
        Dialog(
            onDismissRequest = {
                showUnloadingPreview = false
                Log.d(TAG, "Unloading Preview dismissed (dialog onDismiss)")
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))


                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(vertical = 12.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Top bar: Close + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            IconButton(onClick = {
                                showUnloadingPreview = false
                                Log.d(TAG, "Unloading Preview closed (Top close clicked)")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                            Text(
                                text = "Select Unloading",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Middle content: scrolling list
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 8.dp
                                    )
                            ) {
                                itemsIndexed(unloadingCities) { idx, city ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp)
                                    ) {
                                        Checkbox(
                                            checked = unloadingChecked[idx],
                                            onCheckedChange = {
                                                unloadingChecked[idx] = it
                                                Log.d(
                                                    TAG,
                                                    "Unloading city checked: index=$idx, city='$city', checked=$it"
                                                )
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = green,
                                                uncheckedColor = Color.Gray,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = city, fontSize = 16.sp)
                                    }
                                    Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                                }
                            }
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    val selected =
                                        unloadingCities.mapIndexedNotNull { i, c -> if (unloadingChecked[i]) c else null }
                                    unloadingPlaceholder =
                                        if (selected.isEmpty()) "None selected" else selected.joinToString(
                                            ", "
                                        )
                                    Log.d(TAG, "Unloading View Loads clicked. Selected: $selected")
                                    showUnloadingPreview = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green)
                            ) {
                                Text(
                                    text = "View Loads",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PromoBanner(screenWidth: Dp) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF6CE)),
        elevation = cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Need load for your truck?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E3A2F)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Mark truck empty now",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF18310E),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B63FF)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Update now", color = Color.White)
                }
            }


        }
    }
}

@Composable
fun SelectVehicleScreen(onSubmit: ((String) -> Unit)? = null) {
    SampleVendorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFFFFF)) {
            var selectedReg by remember { mutableStateOf<String?>(null) }
            val scrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TopBar()
                    LocationCard()

                    Text(
                        text = "Select Vehicle",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var query by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = { Text("Search vehicle") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "search"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFFE8E8F3),
                            unfocusedIndicatorColor = Color(0xFFE8E8F3),
                            focusedContainerColor = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFFFFFFF),
                            cursorColor = Color(0xFF111827)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        VehicleCard(
                            reg = "HR47G2320",
                            specs = "Container · 33.0 Ft · 10.0 Ton",
                            info = "Vehicle is 30 km near loading",
                            selected = selectedReg == "HR47G2320",
                            onSelect = { selectedReg = "HR47G2320" }
                        )
                        VehicleCard(
                            reg = "HR47E9396",
                            specs = "Container · 32.0 Ft · 10.0 Ton",
                            info = "Vehicle is 65 km near loading",
                            selected = selectedReg == "HR47E9396",
                            onSelect = { selectedReg = "HR47E9396" }
                        )
                        VehicleCard(
                            reg = "HR47F0106",
                            specs = "Container · 32.0 Ft · 10.0 Ton",
                            info = "Vehicle is 75 km near loading",
                            selected = selectedReg == "HR47F0106",
                            onSelect = { selectedReg = "HR47F0106" }
                        )
                        VehicleCard(
                            reg = "HR47E7551",
                            specs = "Container · 32.0 Ft · 9.5 Ton",
                            info = "Vehicle is 105 km near loading",
                            selected = selectedReg == "HR47E7551",
                            onSelect = { selectedReg = "HR47E7551" }
                        )
                        VehicleCard(
                            reg = "HR47F5121",
                            specs = "Container · 32.0 Ft · 6.0 Ton",
                            info = "Vehicle is 105 km near loading",
                            selected = selectedReg == "HR47F5121",
                            onSelect = { selectedReg = "HR47F5121" }
                        )
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }

                val showSubmit = selectedReg != null

                AnimatedVisibility(
                    visible = showSubmit,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(250)
                    ) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Button(
                        onClick = {
                            selectedReg?.let { reg ->
                                onSubmit?.invoke(reg)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9F0DD)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Submit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFFFFF).copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { /* back pressed */ }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun LocationCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F3))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gautam Buddha Nagar, Uttar Pradesh",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gwalior, MADHYA PRADESH",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFAF0D9))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fill details on time to confirm",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF6C84E))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "04:11",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    reg: String,
    specs: String,
    info: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEF0FF))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .clickable { onSelect() }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            if (selected) Color(0xFF10B981) else Color(0xFFBFC6D6),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reg,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = specs,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = Color(0xFF6B6B6B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F5FF))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "info",
                        tint = Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

//data class LoadItem(
//    val from: String,
//    val fromPinState: String,
//    val to: String,
//    val toPinState: String,
//    val containerInfo: String?,
//    val loadingTime: String?,
//    val distanceInfo: String?,
//    val partyRate: String?,
//    val material: String?,
//    val hauler: String?,
//    val maxLoadMT: Float?,    // parsed numeric
//    val wheels: Int?,         // parsed numeric
//    val vehicleCategory: String?,
//    val orderId: String? = null,
//    val lineItemId: String? = null
//)
//
//data class Vehicle(
//    val id: String,
//    val title: String,
//    val spec: String,
//    val distanceInfo: Float,
//    val numberPlate: String,
//    val model: String,
//    val fuelCapacityLiters: Float
//)
//
//// ------------------------ Config (change RAW_TOKEN with your token) ------------------------
//private const val BASE_URL = "https://tms-test.cjdarcl.com:8002/"
//private const val RAW_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTk5MDczNTQsInVzZXJJZCI6IjIyYWM1NjFkLWRhNWMtNDE2Ni1hYmJhLWQ5NzNlMmYwNDZhMyIsImVtYWlsIjoibmlraGlsLm1haGFyYUBjamRhcmNsLmNvbSIsIm1vYmlsZU51bWJlciI6Ijk2NTQ2MzI3NDQiLCJvcmdJZCI6ImVmMzAwNjgzLTkwMWItNDc3NC1iZjBlLTk1NWQ0OWU3OTZiYyIsIm5hbWUiOiJOaWtoaWwgU2luZ2ggTWFoYXJhIiwib3JnVHlwZSI6IkZMRUVUX09XTkVSIiwiaXNHb2QiOnRydWUsInBvcnRhbFR5cGUiOiJiYXNpYyJ9.MDDgj22r1Hlu4cbl_qySQpqLndzS1D_JAk0owgnQxJ8" // keep safe
//
//// ------------------------ Retrofit API ------------------------
//interface ApiService {
//    @GET
//    suspend fun getOrders(@Url url: String): Response<JsonElement>
//}
//
//private fun createApiService(): ApiService {
//    val logging = HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }
//    logging.level = HttpLoggingInterceptor.Level.BODY
//
//    val okHttpClient = OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(35, TimeUnit.SECONDS)
//        .writeTimeout(30, TimeUnit.SECONDS)
//        .addInterceptor { chain ->
//            val original = chain.request()
//            val newReq = original.newBuilder()
//                .header("Authorization", "Bearer $RAW_TOKEN")
//                .header("Accept", "application/json")
//                .method(original.method, original.body)
//                .build()
//            Log.d("Network", "Sending request to ${newReq.url}")
//            chain.proceed(newReq)
//        }
//        .addInterceptor(logging)
//        .build()
//
//    val retrofit = Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .client(okHttpClient)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    return retrofit.create(ApiService::class.java)
//}
//
//// ------------------------ helper to build encoded API URL ------------------------
//fun buildOrdersApi(origin: String, destination: String): String {
//    val filtersJson = """
//        {
//          "origin": ["$origin"],
//          "destination": ["$destination"],
//          "lineItems.expectedPickupDate": {
//            "from": null,
//            "till": null
//          },
//          "orderType": ["Order", "MTROrder", "MarketOrder"]
//        }
//    """.trimIndent()
//    val encodedFilters = try {
//        URLEncoder.encode(filtersJson, "UTF-8")
//    } catch (t: Throwable) {
//        Log.w("Orders", "URLEncoder failed: ${t.message}")
//        filtersJson.replace(" ", "%20")
//    }
//    val apiUrl = "https://tms-test.cjdarcl.com:8002/shipment-view/sales/v2/orders?limit=50&filters=$encodedFilters"
//    Log.d("Orders", "Built orders API URL (filters encoded) for origin='$origin' destination='$destination' -> $apiUrl")
//    return apiUrl
//}
//
//// ------------------------ Repository ------------------------
//class LoadsRepository(private val api: ApiService) {
//
//    suspend fun fetchOrdersWithFilters(origin: String, destination: String): Response<JsonElement> {
//        val apiUrl = buildOrdersApi(origin, destination)
//        Log.d("Repository", "fetchOrdersWithFilters calling: $apiUrl")
//        return api.getOrders(apiUrl)
//    }
//}
//
//// ------------------------ UI State ------------------------
//sealed class LoadUiState {
//    object Idle : LoadUiState()
//    object Loading : LoadUiState()
//    data class Success(val items: List<LoadItem>) : LoadUiState()
//    object Empty : LoadUiState()
//    data class Error(val message: String) : LoadUiState()
//}
//
//// ------------------------ ViewModel ------------------------
//class LoadViewModel(private val repository: LoadsRepository) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<LoadUiState>(LoadUiState.Idle)
//    val uiState: StateFlow<LoadUiState> = _uiState
//
//    private val _selectedOrigin = MutableStateFlow<String?>(null)
//    private val _selectedDestination = MutableStateFlow<String?>(null)
//
//    fun setOrigin(value: String?) {
//        _selectedOrigin.value = value
//        Log.d("LoadViewModel", "Origin set to: $value")
//    }
//
//    fun setDestination(value: String?) {
//        _selectedDestination.value = value
//        Log.d("LoadViewModel", "Destination set to: $value")
//    }
//
//    fun fetchLoadsIfReady() {
//        val ori = _selectedOrigin.value
//        val dest = _selectedDestination.value
//
//        if (ori.isNullOrBlank() || dest.isNullOrBlank()) {
//            Log.d("LoadViewModel", "fetchLoadsIfReady: origin or destination missing -> not fetching")
//            return
//        }
//
//        fetchLoads(ori, dest)
//    }
//
//    private fun fetchLoads(originSelection: String, destinationSelection: String) {
//        viewModelScope.launch {
//            _uiState.value = LoadUiState.Loading
//            Log.d("LoadViewModel", "Starting fetchLoads for selection '$originSelection' -> '$destinationSelection'")
//
//            try {
//                val response = repository.fetchOrdersWithFilters(
//                    origin = originSelection,
//                    destination = destinationSelection
//                )
//
//                val statusCode = response.code()
//                val isSuccessful = response.isSuccessful
//
//                Log.d(
//                    "LoadViewModel",
//                    "Response received: code=$statusCode, successful=$isSuccessful"
//                )
//
//                if (!isSuccessful) {
//                    val errBody = try { response.errorBody()?.string() } catch (t: Throwable) { null }
//                    Log.e("LoadViewModel", "API ERROR code=$statusCode body=${errBody?.take(300) ?: "no body"}")
//                    _uiState.value = LoadUiState.Error("Server error: $statusCode - ${errBody?.take(300) ?: "no body"}")
//                    return@launch
//                }
//
//                val body = response.body()
//
//                if (body == null) {
//                    Log.w("LoadViewModel", "Empty response body")
//                    _uiState.value = LoadUiState.Empty
//                    return@launch
//                }
//
//                val items = parseJsonToLoadItems(body)
//
//                Log.d("LoadViewModel", "Parsed ${items.size} items before filtering by UI selections")
//
//                val filtered = items.filter { item ->
//                    matchesSelection(item.from, originSelection) && matchesSelection(item.to, destinationSelection)
//                }
//
//                Log.d("LoadViewModel", "After filtering by selection: ${filtered.size} items")
//
//                _uiState.value = if (filtered.isEmpty()) LoadUiState.Empty else LoadUiState.Success(filtered)
//
//            } catch (e: java.io.IOException) {
//                Log.e("LoadViewModel", "Network IO exception", e)
//                _uiState.value = LoadUiState.Error("Network error: ${e.message ?: "IO error"}")
//
//            } catch (e: Exception) {
//                Log.e("LoadViewModel", "Unknown exception", e)
//                _uiState.value = LoadUiState.Error("Unknown error: ${e.message ?: "error"}")
//            }
//        }
//    }
//
//    private fun matchesSelection(itemValue: String?, selection: String?): Boolean {
//        if (selection.isNullOrBlank()) return true
//        if (itemValue.isNullOrBlank()) return false
//
//        val selParts = selection.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
//        val itemLower = itemValue.lowercase()
//        return selParts.any { part -> itemLower == part || itemLower.contains(part) }
//    }
//
//    // ---------------- JSON parsing & Party Rate calculation ----------------
//    private fun parseJsonToLoadItems(json: JsonElement): List<LoadItem> {
//        val result = mutableListOf<LoadItem>()
//
//        try {
//            val jsonElement = json
//            val array: JsonArray? = when {
//                jsonElement.isJsonArray -> jsonElement.asJsonArray
//                jsonElement.isJsonObject -> {
//                    val obj = jsonElement.asJsonObject
//                    when {
//                        obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
//                        obj.has("orders") && obj.get("orders").isJsonArray -> obj.getAsJsonArray("orders")
//                        obj.has("items") && obj.get("items").isJsonArray -> obj.getAsJsonArray("items")
//                        else -> obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
//                    }
//                }
//                else -> null
//            }
//
//            if (array == null) {
//                Log.w("LoadParser", "No JSON array found in response root")
//                return emptyList()
//            }
//
//            Log.d("LoadParser", "Array length = ${array.size()}")
//
//            for ((index, orderEl) in array.withIndex()) {
//                if (!orderEl.isJsonObject) {
//                    Log.w("LoadParser", "Skipping non-object element at index $index")
//                    continue
//                }
//                val orderObj = orderEl.asJsonObject
//
//                // order id for logging
//                val orderId = try {
//                    when {
//                        orderObj.has("id") && !orderObj.get("id").isJsonNull -> orderObj.get("id").asString
//                        orderObj.has("orderNumber") && !orderObj.get("orderNumber").isJsonNull -> orderObj.get("orderNumber").asString
//                        else -> null
//                    }
//                } catch (_: Exception) { null }
//
//                if (!orderObj.has("lineItems") || !orderObj.get("lineItems").isJsonArray) {
//                    Log.d("PartyRate", "OrderId=${orderId ?: "unknown"} PartyRate not calculable — no lineItems")
//                    continue
//                }
//
//                val lineItems = orderObj.getAsJsonArray("lineItems")
//                for (liIdx in 0 until lineItems.size()) {
//                    val liEl = lineItems.get(liIdx)
//                    if (!liEl.isJsonObject) continue
//                    val liObj = liEl.asJsonObject
//
//                    val lineItemId = try { if (liObj.has("id") && !liObj.get("id").isJsonNull) liObj.get("id").asString else null } catch (_: Exception) { null }
//
//                    // Extract mandatory fields required for Party Rate calculation
//                    // distance
//                    val distance = try {
//                        if (liObj.has("distance") && !liObj.get("distance").isJsonNull) {
//                            val v = liObj.get("distance")
//                            when {
//                                v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> v.asFloat
//                                v.isJsonPrimitive -> v.asString.toFloatOrNull()
//                                else -> null
//                            }
//                        } else null
//                    } catch (_: Exception) { null }
//
//                    // freightChargeType
//                    val freightChargeType = try { if (liObj.has("freightChargeType") && !liObj.get("freightChargeType").isJsonNull) liObj.get("freightChargeType").asString else null } catch (_: Exception) { null }
//
//                    // freightChargeRate -> parse as Double (supports numbers and numeric strings with commas)
//                    val freightChargeRate = try {
//                        if (liObj.has("freightChargeRate") && !liObj.get("freightChargeRate").isJsonNull) {
//                            val v = liObj.get("freightChargeRate")
//                            when {
//                                v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> v.asDouble
//                                v.isJsonPrimitive -> v.asString.replace(",", "").trim().toDoubleOrNull()
//                                else -> null
//                            }
//                        } else null
//                    } catch (_: Exception) { null }
//
//                    // netQuantity: remainingPlannedQuantity.weight.netQuantity
//                    val netQuantity = try {
//                        if (liObj.has("remainingPlannedQuantity") && liObj.get("remainingPlannedQuantity").isJsonObject) {
//                            val rem = liObj.getAsJsonObject("remainingPlannedQuantity")
//                            if (rem.has("weight") && rem.get("weight").isJsonObject) {
//                                val wt = rem.getAsJsonObject("weight")
//                                val nq = when {
//                                    wt.has("netQuantity") && !wt.get("netQuantity").isJsonNull -> {
//                                        val v = wt.get("netQuantity")
//                                        when {
//                                            v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> v.asFloat
//                                            v.isJsonPrimitive -> v.asString.toFloatOrNull()
//                                            else -> null
//                                        }
//                                    }
//                                    else -> null
//                                }
//                                nq
//                            } else null
//                        } else null
//                    } catch (_: Exception) { null }
//
//                    // material -> loadInfo.material
//                    val material = try {
//                        if (liObj.has("loadInfo") && liObj.get("loadInfo").isJsonObject) {
//                            val li = liObj.getAsJsonObject("loadInfo")
//                            if (li.has("material") && !li.get("material").isJsonNull) li.get("material").asString else null
//                        } else null
//                    } catch (_: Exception) { null }
//
//                    // consigner state
//                    val consignerState = try {
//                        if (liObj.has("consigner") && liObj.get("consigner").isJsonObject) {
//                            val con = liObj.getAsJsonObject("consigner")
//                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
//                                val p0 = con.getAsJsonArray("places").get(0)
//                                if (p0.isJsonObject && p0.asJsonObject.has("state") && !p0.asJsonObject.get("state").isJsonNull) p0.asJsonObject.get("state").asString else null
//                            } else if (con.has("state") && !con.get("state").isJsonNull) con.get("state").asString else null
//                        } else null
//                    } catch (_: Exception) { null }
//
//                    // consignee state
//                    val consigneeState = try {
//                        if (liObj.has("consignee") && liObj.get("consignee").isJsonObject) {
//                            val con = liObj.getAsJsonObject("consignee")
//                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
//                                val p0 = con.getAsJsonArray("places").get(0)
//                                if (p0.isJsonObject && p0.asJsonObject.has("state") && !p0.asJsonObject.get("state").isJsonNull) p0.asJsonObject.get("state").asString else null
//                            } else if (con.has("state") && !con.get("state").isJsonNull) con.get("state").asString else null
//                        } else null
//                    } catch (_: Exception) { null }
//
//                    // allowedCustomerLoadTypes -> chassisTypes[] and other attributes
//                    var truckTypeDisplay: String? = null
//                    var wheelsDerived: Int? = null
//                    var vehicleCategory: String? = null
//                    var haulerName: String? = null
//                    var maxLoadMT: Float? = null
//
//                    try {
//                        if (liObj.has("allowedCustomerLoadTypes") && liObj.get("allowedCustomerLoadTypes").isJsonArray) {
//                            val actArr = liObj.getAsJsonArray("allowedCustomerLoadTypes")
//                            if (actArr.size() > 0) {
//                                val ac0 = actArr.get(0)
//                                if (ac0.isJsonObject) {
//                                    val acObj = ac0.asJsonObject
//                                    haulerName = acObj.get("name")?.takeIf { !it.isJsonNull }?.let { if (it.isJsonPrimitive) it.asString else it.toString() }
//                                    vehicleCategory = acObj.get("vehicleCategory")?.takeIf { !it.isJsonNull }?.let { if (it.isJsonPrimitive) it.asString else it.toString() }
//
//                                    try {
//                                        val pmt = acObj.get("passingCapacityMT")
//                                        if (pmt != null && !pmt.isJsonNull && pmt.isJsonPrimitive && pmt.asJsonPrimitive.isNumber) {
//                                            maxLoadMT = try { pmt.asFloat } catch (_: Throwable) { pmt.asString.toFloatOrNull() }
//                                        } else {
//                                            maxLoadMT = acObj.get("passingCapacityMT")?.asString?.toFloatOrNull()
//                                        }
//                                    } catch (_: Exception) { }
//
//                                    try {
//                                        val nw = acObj.get("numberOfWheels")
//                                        if (nw != null && !nw.isJsonNull && nw.isJsonPrimitive && nw.asJsonPrimitive.isNumber) {
//                                            wheelsDerived = try { nw.asInt } catch (_: Throwable) { nw.asString.toIntOrNull() }
//                                        } else {
//                                            wheelsDerived = acObj.get("numberOfWheels")?.asString?.toIntOrNull()
//                                        }
//                                    } catch (_: Exception) { }
//
//                                    // chassisTypes[]
//                                    try {
//                                        if (acObj.has("chassisTypes") && acObj.get("chassisTypes").isJsonArray) {
//                                            val chArr = acObj.getAsJsonArray("chassisTypes")
//                                            if (chArr.size() > 0) {
//                                                val ch0 = chArr.get(0)
//                                                val chStr = if (ch0.isJsonPrimitive) ch0.asString else ch0.toString()
//                                                truckTypeDisplay = chStr
//                                                // derive wheels by regex like TRUCK-14W or 14W
//                                                val regex = Regex("(\\d+)")
//                                                val match = regex.find(chStr)
//                                                if (match != null) {
//                                                    wheelsDerived = match.groupValues[1].toIntOrNull() ?: wheelsDerived
//                                                }
//                                            }
//                                        }
//                                    } catch (_: Exception) { }
//                                }
//                            }
//                        }
//                    } catch (_: Exception) { }
//
//                    // Validation: mandatory fields
//                    // declare upfront so it's visible for the rest of the loop
//                    var partyRateString: String = "Rate on Request"
//
//                    val mandatoryMissing =
//                        distance == null ||
//                                freightChargeType.isNullOrBlank() ||
//                                freightChargeRate == null ||
//                                consignerState.isNullOrBlank() ||
//                                consigneeState.isNullOrBlank()
//
//                    val compositeId = listOfNotNull(orderId, lineItemId).joinToString(":")
//
//                    if (mandatoryMissing) {
//                        val missingReasons = mutableListOf<String>()
//
//                        if (distance == null) missingReasons.add("distance")
//                        if (freightChargeType.isNullOrBlank()) missingReasons.add("freightChargeType")
//                        if (freightChargeRate == null) missingReasons.add("freightChargeRate")
//                        if (consignerState.isNullOrBlank()) missingReasons.add("consignerState")
//                        if (consigneeState.isNullOrBlank()) missingReasons.add("consigneeState")
//                        if (truckTypeDisplay.isNullOrBlank()) missingReasons.add("truckType / chassisType")
//
//                        Log.d(
//                            "PartyRate",
//                            """
//❌ Party Rate NOT calculated
//LoadId : ${if (compositeId.isNotEmpty()) compositeId else "unknown"}
//Missing : ${missingReasons.joinToString(", ")}
//Raw Values →
//    distance = $distance
//    freightChargeType = $freightChargeType
//    freightChargeRate = $freightChargeRate
//    netQuantity = $netQuantity
//    consignerState = $consignerState
//    consigneeState = $consigneeState
//    truckType = $truckTypeDisplay
//""".trimIndent()
//                        )
//
//                        // partyRateString remains the fallback "Rate on Request"
//                    } else {
//                        // preliminary partyRateString (will be overwritten below if we successfully compute perMT)
//                        partyRateString = when {
//                            freightChargeType?.equals("perMT", true) == true &&
//                                    netQuantity != null &&
//                                    netQuantity > 0 &&
//                                    freightChargeRate != null -> {
//                                val amount = freightChargeRate * netQuantity
//                                "₹ ${amount.toLong()}"
//                            }
//
//                            freightChargeType?.equals("perVehicle", true) == true &&
//                                    freightChargeRate != null -> {
//                                "₹ ${freightChargeRate.toLong()}"
//                            }
//
//                            else -> "Rate on Request"
//                        }
//                    }
//
//// Only handle perMT per your rules
//                    if (freightChargeType?.equals("perMT", ignoreCase = true) != true) {
//                        Log.d(
//                            "PartyRate",
//                            "LoadId=${compositeId.ifEmpty { "unknown" }} PartyRate not calculable — freightChargeType=$freightChargeType"
//                        )
//                        continue
//                    }
//
//// compute party rate: freightChargeRate * netQuantity
//                    val computed = try {
//                        val netQty = netQuantity ?: 0f
//                        val rate = freightChargeRate ?: 0.0
//                        val value = (rate * netQty.toDouble())
//                        if (value.isFinite()) value else null
//                    } catch (t: Throwable) {
//                        null
//                    }
//
//                    if (computed == null || computed <= 0.0) {
//                        Log.d(
//                            "PartyRate",
//                            "LoadId=${compositeId.ifEmpty { "unknown" }} PartyRate not calculable — zero or invalid computed value"
//                        )
//                        continue
//                    }
//
//// Format in INR with no decimals
//                    val amountLong = round(computed).toLong()
//                    val formattedNumber = NumberFormat.getNumberInstance(Locale("en", "IN")).format(amountLong)
//                    partyRateString = "₹ $formattedNumber"
//
////                    Log.d("PartyRate", "LoadId=${compositeId.ifEmpty { "unknown" }} PartyRate=$partyRateString")
//                    Log.d(
//                        "PartyRate",
//                        """
//    LoadId=${compositeId.ifEmpty { "unknown" }}
//    Calculation:
//    freightChargeType=perMT
//    freightChargeRate=$freightChargeRate
//    netQuantityMT=$netQuantity
//    Formula=freightChargeRate × netQuantity
//    PartyRate=$partyRateString
//    """.trimIndent()
//                    )
//
//
//// build display strings for distance and others
//                    val distanceInfo = distance?.let { "${String.format("%.0f", it)} km" }
//
//// fallback for names: try to pick first place name from consigner/consignee or salesOffice/customer
//                    val fromName = try {
//                        if (liObj.has("consigner") && liObj.get("consigner").isJsonObject) {
//                            val con = liObj.getAsJsonObject("consigner")
//                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
//                                val p0 = con.getAsJsonArray("places").get(0)
//                                if (p0.isJsonObject && p0.asJsonObject.has("name") && !p0.asJsonObject.get("name").isJsonNull) p0.asJsonObject.get("name").asString else null
//                            } else null
//                        } else null
//                    } catch (_: Exception) { null } ?: orderObj.getAsJsonObject("salesOffice")?.get("name")?.asString ?: "N/A"
//
//                    val toName = try {
//                        if (liObj.has("consignee") && liObj.get("consignee").isJsonObject) {
//                            val con = liObj.getAsJsonObject("consignee")
//                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
//                                val p0 = con.getAsJsonArray("places").get(0)
//                                if (p0.isJsonObject && p0.asJsonObject.has("name") && !p0.asJsonObject.get("name").isJsonNull) p0.asJsonObject.get("name").asString else null
//                            } else null
//                        } else null
//                    } catch (_: Exception) { null } ?: orderObj.getAsJsonObject("customer")?.getAsJsonArray("places")?.get(0)?.asJsonObject?.get("name")?.asString ?: "N/A"
//
//                    val item = LoadItem(
//                        from = fromName,
//                        fromPinState = consignerState ?: "",
//                        to = toName,
//                        toPinState = consigneeState ?: "",
//                        containerInfo = try { liObj.get("containerInfo")?.asString } catch (_: Exception) { null },
//                        loadingTime = try { liObj.get("loadingTime")?.asString } catch (_: Exception) { null },
//                        distanceInfo = distanceInfo,
//                        partyRate = partyRateString,
//
//                        material = material ?: "-",
//                        hauler = haulerName ?: "-",
//                        maxLoadMT = maxLoadMT,
//                        wheels = wheelsDerived,
//                        vehicleCategory = vehicleCategory ?: "-",
//
//                        orderId = orderId,
//                        lineItemId = lineItemId
//                    )
//
//                    result.add(item)
//
//                }
//            }
//
//        } catch (t: Throwable) {
//            Log.e("LoadParser", "Error parsing JSON", t)
//        }
//
//        return result
//    }
//}
//
//// Simple ViewModelFactory
//class LoadViewModelFactory(private val repo: LoadsRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(LoadViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return LoadViewModel(repo) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//// ------------------------ Composables: integrate ViewModel ------------------------
//@Composable
//fun LoadScreen(navController: NavHostController) {
//    val apiService = remember { createApiService() }
//    val repo = remember { LoadsRepository(apiService) }
//    val vm: LoadViewModel = viewModel(factory = LoadViewModelFactory(repo))
//
//    val uiState by vm.uiState.collectAsState()
//
//    Scaffold(
//        topBar = { LoadTopBar(navController) },
//        bottomBar = {  },
//        containerColor = Color(0xFFF6F7FB)
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(
//                    start = 16.dp,
//                    end = 16.dp,
//                    top = 8.dp,
//                    bottom = 12.dp
//                )
//        ) {
//            StatusRows(
//                onApplySelection = { originSelection: String?, destSelection: String? ->
//                    vm.setOrigin(originSelection)
//                    vm.setDestination(destSelection)
//                    vm.fetchLoadsIfReady()
//                }
//            )
//
//            Spacer(modifier = Modifier.height(10.dp))
//            BannerCard()
//            Spacer(modifier = Modifier.height(12.dp))
//            SegmentedTabs(navController)
//
//            Spacer(modifier = Modifier.height(10.dp))
//
//            when (uiState) {
//                is LoadUiState.Idle -> {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("Choose origin & destination then tap View Loads", color = Color(0xFF9B9B9B))
//                    }
//                }
//                is LoadUiState.Loading -> {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator()
//                    }
//                }
//                is LoadUiState.Empty -> {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF9B9B9B), modifier = Modifier.size(48.dp))
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text("No loads found for the selected route.", color = Color(0xFF6B6B6B))
//                        }
//                    }
//                }
//                is LoadUiState.Error -> {
//                    val msg = (uiState as LoadUiState.Error).message
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text("Something went wrong", fontWeight = FontWeight.SemiBold)
//                            Spacer(modifier = Modifier.height(6.dp))
//                            Text(msg, color = Color(0xFF6B6B6B))
//                        }
//                    }
//                }
//                is LoadUiState.Success -> {
//                    val items = (uiState as LoadUiState.Success).items
//
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.spacedBy(16.dp),
//                        contentPadding = PaddingValues(bottom = 160.dp)
//                    ) {
//                        items(items) { load ->
//                            LoadCard(
//                                item = load,
//                                navController = navController, // ✅ REQUIRED FIX
//                                onConfirm = {
//                                    val currentRoute = navController.currentBackStackEntry?.destination?.route
//                                    if (currentRoute != "select_vehicle") {
//                                        navController.navigate("select_vehicle") {
//                                            launchSingleTop = true
//                                            restoreState = true
//                                        }
//                                    }
//                                },
//                                onCardClick = {
//                                    navController.navigate(BottomNavItem.VehicleKYC.route)
//                                }
//
//                            )
//
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ------------------------ StatusRows and other UI components (unchanged) ------------------------
//@Composable
//fun StatusRows(
//    onApplySelection: (originSelection: String?, destSelection: String?) -> Unit
//) {
//    val TAG = "StatusRows"
//    val green = Color(0xFF23A455)
//    var showPreview by remember { mutableStateOf(false) }
//    var showUnloadingPreview by remember { mutableStateOf(false) }
//
//    var loadingPlaceholder by remember { mutableStateOf<String?>(null) }
//    var unloadingPlaceholder by remember { mutableStateOf<String?>(null) }
//
//    val loadingCities = listOf(
//        "All NCR", "Central Delhi", "East Delhi", "New Delhi",
//        "North Delhi", "North East Delhi", "North West Delhi",
//        "Shahdara", "South Delhi", "South East Delhi", "South West Delhi" , "AMBALA"
//    )
//    val unloadingCities = listOf(
//        "Rajasthan", "Himachal Pradesh", "Nagaland", "Uttarakhand",
//        "Andhra Pradesh", "Madhya Pradesh", "Lakshadweep", "Meghalaya",
//        "Sikkim", "Kerala", "Chhattisgarh", "Tamil Nadu" , "KOLKATA"
//    )
//    val loadingChecked = remember { mutableStateListOf<Boolean>().apply { repeat(loadingCities.size) { add(false) } } }
//    val unloadingChecked = remember { mutableStateListOf<Boolean>().apply { repeat(unloadingCities.size) { add(false) } } }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 4.dp)
//            .shadow(4.dp, RoundedCornerShape(16.dp))
//            .background(Color.White, RoundedCornerShape(16.dp))
//            .padding(vertical = 16.dp, horizontal = 20.dp)
//    ) {
//        StatusRowSelectable(
//            label = "Loading",
//            dotColor = Color(0xFF1EB980),
//            placeholder = loadingPlaceholder ?: "कृपया शहर चुनें",
//            onClick = { showPreview = true }
//        )
//
//        Spacer(modifier = Modifier.height(10.dp))
//
//        StatusRowSelectable(
//            label = "Unloading",
//            dotColor = Color(0xFFED5565),
//            placeholder = unloadingPlaceholder ?: "कृपया शहर चुनें",
//            onClick = { showUnloadingPreview = true }
//        )
//
//        Spacer(modifier = Modifier.height(10.dp))
//    }
//
//    if (showPreview) {
//        Dialog(
//            onDismissRequest = { showPreview = false },
//            properties = DialogProperties(
//                usePlatformDefaultWidth = false,
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true
//            )
//        ) {
//            Box(modifier = Modifier.fillMaxSize()) {
//                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))
//
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = Color.White
//                ) {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
//                        ) {
//                            IconButton(onClick = { showPreview = false }) {
//                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
//                            }
//                            Text(
//                                text = "Select Loading",
//                                modifier = Modifier.padding(start = 8.dp),
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        }
//                        LazyColumn(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(top = 68.dp, bottom = 88.dp, start = 12.dp, end = 12.dp)
//                        ) {
//                            itemsIndexed(loadingCities) { idx, city ->
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 10.dp)
//                                ) {
//                                    Checkbox(
//                                        checked = loadingChecked[idx],
//                                        onCheckedChange = { loadingChecked[idx] = it },
//                                        colors = CheckboxDefaults.colors(
//                                            checkedColor = green,
//                                            uncheckedColor = Color.Gray,
//                                            checkmarkColor = Color.White
//                                        )
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(city)
//                                }
//                                Divider(color = Color(0xFFFAFAFA), thickness = 1.dp)
//                            }
//                        }
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .align(Alignment.BottomCenter)
//                                .padding(horizontal = 18.dp, vertical = 18.dp)
//                        ) {
//                            Button(
//                                onClick = {
//                                    val selected = loadingCities.mapIndexedNotNull { i, c -> if (loadingChecked[i]) c else null }
//                                    loadingPlaceholder = if (selected.isEmpty()) null else selected.joinToString(", ")
//                                    Log.d(TAG, "Loading selected: $loadingPlaceholder")
//                                    showPreview = false
//                                    onApplySelection(loadingPlaceholder, unloadingPlaceholder)
//                                },
//                                modifier = Modifier.fillMaxWidth().height(54.dp),
//                                shape = RoundedCornerShape(12.dp),
//                                colors = ButtonDefaults.buttonColors(containerColor = green)
//                            ) {
//                                Text(text = "View Loads", fontWeight = FontWeight.SemiBold, color = Color.White)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    if (showUnloadingPreview) {
//        Dialog(
//            onDismissRequest = { showUnloadingPreview = false },
//            properties = DialogProperties(
//                usePlatformDefaultWidth = false,
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true
//            )
//        ) {
//            Box(modifier = Modifier.fillMaxSize()) {
//                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))
//
//                Surface(
//                    modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
//                    color = Color.White,
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Column(modifier = Modifier.fillMaxSize()) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.fillMaxWidth().padding(8.dp)
//                        ) {
//                            IconButton(onClick = { showUnloadingPreview = false }) {
//                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
//                            }
//                            Text(
//                                text = "Select Unloading",
//                                modifier = Modifier.padding(start = 8.dp),
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        }
//
//                        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
//
//                        LazyColumn(
//                            modifier = Modifier
//                                .weight(1f)
//                                .fillMaxWidth()
//                                .padding(horizontal = 8.dp, vertical = 8.dp)
//                        ) {
//                            itemsIndexed(unloadingCities) { idx, city ->
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 10.dp)
//                                ) {
//                                    Checkbox(
//                                        checked = unloadingChecked[idx],
//                                        onCheckedChange = { unloadingChecked[idx] = it },
//                                        colors = CheckboxDefaults.colors(
//                                            checkedColor = green,
//                                            uncheckedColor = Color.Gray,
//                                            checkmarkColor = Color.White
//                                        )
//                                    )
//                                    Spacer(modifier = Modifier.width(12.dp))
//                                    Text(text = city, fontSize = 16.sp)
//                                }
//                                Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
//                            }
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 18.dp, vertical = 16.dp)
//                        ) {
//                            Button(
//                                onClick = {
//                                    val selected = unloadingCities.mapIndexedNotNull { i, c -> if (unloadingChecked[i]) c else null }
//                                    unloadingPlaceholder = if (selected.isEmpty()) null else selected.joinToString(", ")
//                                    Log.d(TAG, "Unloading selected: $unloadingPlaceholder")
//                                    showUnloadingPreview = false
//                                    onApplySelection(loadingPlaceholder, unloadingPlaceholder)
//                                },
//                                modifier = Modifier.fillMaxWidth().height(52.dp),
//                                shape = RoundedCornerShape(10.dp),
//                                colors = ButtonDefaults.buttonColors(containerColor = green)
//                            ) {
//                                Text(text = "View Loads", fontWeight = FontWeight.SemiBold, color = Color.White)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun StatusRowSelectable(
//    label: String,
//    dotColor: Color,
//    placeholder: String,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Box(
//                modifier = Modifier
//                    .size(12.dp)
//                    .clip(CircleShape)
//                    .background(dotColor)
//            )
//            Spacer(modifier = Modifier.width(10.dp))
//            Column {
//                Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = placeholder,
//                    fontSize = 13.sp,
//                    color = Color(0xFF6B6B6B),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//
//        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
//            Icon(
//                imageVector = Icons.Default.ArrowForward,
//                contentDescription = null,
//                tint = Color.Gray,
//                modifier = Modifier.size(22.dp)
//            )
//        }
//    }
//}
//
//// ------------------------ rest of UI components (BannerCard, Tabs, TopBar, LoadCard) ------------------------
//@Composable
//fun BannerCard() {
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(120.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(Color(0xFFDAEFB8), Color(0xFFBDE07B))
//                    )
//                )
//                .padding(16.dp)
//        ) {
//            Column(modifier = Modifier.align(Alignment.CenterStart)) {
//                Text("Need load for your truck?", fontSize = 14.sp, color = Color(0xFF2E2E2E))
//                Text(
//                    "Mark truck empty now",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.ExtraBold,
//                    color = Color(0xFF1E1E1E)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    onClick = { /* update */ },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1157FF))
//                ) {
//                    Text("Update now", color = Color.White)
//                }
//            }
//        }
//    }
//}
//
//sealed class TabItem {
//    data class Screen(val title: String, val content: @Composable () -> Unit) : TabItem()
//    data class Route(val title: String, val route: String?) : TabItem()
//}
//
//@Composable
//fun SegmentedTabs(navController: NavHostController) {
//    val selectedState = remember { mutableStateOf(1) }
//    val tabs = listOf<TabItem>(
//        TabItem.Route("My Load(1)", "my_trips"),
//        TabItem.Route("Load(4)", null),
//        TabItem.Route("New load(33)", "NEWLOAD")
//    )
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        val tabModifier = Modifier
//            .width(110.dp)
//            .height(44.dp)
//            .shadow(6.dp, RoundedCornerShape(50))
//            .clip(RoundedCornerShape(50))
//
//        fun cardColor(isSelected: Boolean) =
//            if (isSelected) Color.Black else Color(0xFFF6F6F7)
//
//        fun textColor(isSelected: Boolean) =
//            if (isSelected) Color.White else Color(0xFF9B9B9B)
//
//        tabs.forEachIndexed { index, tab ->
//            val title = when (tab) { is TabItem.Screen -> tab.title; is TabItem.Route -> tab.title }
//            val isSelected = selectedState.value == index
//
//            Card(
//                modifier = tabModifier.clickable {
//                    selectedState.value = index
//                    when (tab) {
//                        is TabItem.Screen -> {}
//                        is TabItem.Route -> {
//                            val route = tab.route
//                            if (route == null) return@clickable
//                            if (navController.graph.findNode(route) != null) navController.navigate(route)
//                        }
//                    }
//                },
//                shape = RoundedCornerShape(50),
//                colors = CardDefaults.cardColors(containerColor = cardColor(isSelected)),
//                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE6E6E6))
//            ) {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(text = title, color = textColor(isSelected), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
//                }
//            }
//        }
//    }
//
//    Spacer(modifier = Modifier.height(12.dp))
//    when (val current = tabs[selectedState.value]) {
//        is TabItem.Screen -> current.content()
//        is TabItem.Route -> {}
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LoadTopBar(navController: NavController) {
//    TopAppBar(
//        title = { },
//        navigationIcon = {
//            IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back") }
//        },
//        actions = {
//            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
//                Text(text = "Load alert", fontSize = 14.sp, fontWeight = FontWeight.Medium)
//                Spacer(modifier = Modifier.width(8.dp))
//                var checked by remember { mutableStateOf(true) }
//                Switch(checked = checked, onCheckedChange = { checked = it }, modifier = Modifier.scale(0.85f))
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
//        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).height(56.dp)
//    )
//}
//
//val GreenDot = Color(0xFF22C55E)
//val RedDot = Color(0xFFEF4444)
//val LightGrayText = Color(0xFF9CA3AF)
//val TimerYellow = Color(0xFFFACC15)
//val PurplePillBg = Color(0xFFE9D5FF)
//val PurpleText = Color(0xFF7C3AED)
//val ConfirmGreen = Color(0xFF16A34A)
//val ConfirmGreenText = Color(0xFF15803D)
//
//@Composable
//fun LoadCard(
//    item: LoadItem,
//    navController: NavHostController,
//    modifier: Modifier = Modifier,
//    onConfirm: () -> Unit = {},
//    onCardClick: () -> Unit = {}
//) {
//    fun hasText(s: String?) = !s.isNullOrBlank()
//    fun formatFloat(f: Float?) = f?.let { String.format("%.2f", it) }
//
//    // ================= SAFE NAVIGATION WITH FULL LOGGING & PAYLOAD =================
//    fun safeNavigate(
//        navController: NavHostController?,
//        targetRoute: String,
//        source: String,
//        payload: Map<String, Any?> = emptyMap()
//    ) {
//        val TAG = "LOAD_CARD_NAV"
//        try {
//            Log.d(TAG, "================ NAVIGATION START ================")
//            Log.d(TAG, "Source: $source")
//            Log.d(TAG, "Target Route: \"$targetRoute\"")
//
//            Log.d(TAG, "Params -> from=${item.from}, to=${item.to}, rate=${item.partyRate}")
//            Log.d(TAG, "Params -> material=${item.material}, hauler=${item.hauler}, wheels=${item.wheels}")
//            Log.d(TAG, "Params -> maxLoadMT=${formatFloat(item.maxLoadMT)}, vehicleCategory=${item.vehicleCategory}")
//
//            if (navController == null) {
//                Log.w(TAG, "Navigation skipped: navController is NULL")
//                Log.d(TAG, "================ NAVIGATION END ==================")
//                return
//            }
//
//            val currentRoute = try {
//                navController.currentBackStackEntry?.destination?.route
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to read current route: ${e.message}")
//                null
//            }
//
//            Log.d(TAG, "Current Route Before Navigation: \"${currentRoute ?: "UNKNOWN"}\"")
//
//            if (currentRoute == targetRoute) {
//                Log.w(TAG, "Navigation blocked: currentRoute == targetRoute")
//                Log.d(TAG, "================ NAVIGATION END ==================")
//                return
//            }
//
//            // Put payload into SavedStateHandle of the current back stack entry
//            try {
//                val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
//                if (savedStateHandle != null) {
//                    for ((k, v) in payload) {
//                        // Setting each value separately — destination will read by key
//                        savedStateHandle.set(k, v)
//                        Log.d(TAG, "SavedStateHandle set -> $k = $v")
//                    }
//                } else {
//                    Log.w(TAG, "No savedStateHandle available on currentBackStackEntry")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to set payload on SavedStateHandle: ${e.message}")
//            }
//
//            Log.d(TAG, "Executing navController.navigate(\"$targetRoute\")")
//            navController.navigate(targetRoute)
//            Log.d(TAG, "navigate() call executed")
//
//            val afterRoute = try {
//                navController.currentBackStackEntry?.destination?.route
//            } catch (_: Exception) {
//                null
//            }
//
//            Log.d(TAG, "Current Route After Navigation: \"${afterRoute ?: "UNKNOWN"}\"")
//            Log.d(TAG, "================ NAVIGATION END ==================")
//
//        } catch (t: Throwable) {
//            Log.e(TAG, "Navigation failed with exception: ${t.message}", t)
//        }
//    }
//    // ====================================================================
//
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        onClick = {
//            Log.d("LOAD_CARD_NAV", "Card clicked")
//
//            try { onCardClick() } catch (_: Exception) {}
//
//            // Build payload from the item - only primitive/serializable primitives (String, Int, Float)
//            val payload = mapOf<String, Any?>(
//                "from" to item.from,
//                "fromPinState" to item.fromPinState,
//                "to" to item.to,
//                "toPinState" to item.toPinState,
//                "material" to item.material,
//                "hauler" to item.hauler,
//                "maxLoadMT" to item.maxLoadMT,
//                "wheels" to item.wheels,
//                "vehicleCategory" to item.vehicleCategory,
//                "partyRate" to item.partyRate,
//                "distanceInfo" to item.distanceInfo
//            )
//
//            safeNavigate(
//                navController = navController,
//                targetRoute = BottomNavItem.VehicleKYC.route,
//                source = "LoadCard → Card Click",
//                payload = payload
//            )
//
//        },
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
//            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
//                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GreenDot))
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Box(modifier = Modifier.width(2.dp).height(36.dp).background(Color(0xFFECECEC)))
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(RedDot))
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Column(modifier = Modifier.weight(1f)) {
//                    if (hasText(item.from)) {
//                        Text(
//                            text = item.from,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = Color(0xFF111111),
//                            maxLines = 2,
//                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                        )
//                        if (hasText(item.fromPinState)) {
//                            Text(text = item.fromPinState ?: "", fontSize = 12.sp, color = Color(0xFF6B6B6B))
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(10.dp))
//
//                    if (hasText(item.to)) {
//                        Text(
//                            text = item.to,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF111111),
//                            maxLines = 2,
//                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                        )
//                        if (hasText(item.toPinState)) {
//                            Text(text = item.toPinState ?: "", fontSize = 12.sp, color = Color(0xFF6B6B6B))
//                        }
//                    }
//                }
//
//                Icon(
//                    imageVector = Icons.Default.KeyboardArrowRight,
//                    contentDescription = null,
//                    tint = Color(0xFF9E9E9E),
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            if (hasText(item.distanceInfo)) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(PurplePillBg)
//                        .padding(horizontal = 12.dp, vertical = 10.dp)
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.FlashOn,
//                            contentDescription = null,
//                            modifier = Modifier.size(18.dp),
//                            tint = Color(0xFFFFC85A)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = item.distanceInfo ?: "",
//                            fontSize = 13.sp,
//                            color = PurpleText,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//
//            Divider(color = Color(0xFFF1F1F1), thickness = 1.dp)
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Column(modifier = Modifier.fillMaxWidth()) {
//                @Composable
//                fun DataRow(label: String, value: String) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 6.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = label,
//                            fontSize = 13.sp,
//                            color = Color(0xFF8E8E8E),
//                            modifier = Modifier.weight(0.45f)
//                        )
//                        Text(
//                            text = value,
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF111111),
//                            modifier = Modifier.weight(0.55f),
//                            maxLines = 1,
//                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                        )
//                    }
//                }
//
//                if (hasText(item.material)) DataRow("Material", item.material!!)
//                if (hasText(item.hauler)) DataRow("Hauler", item.hauler!!)
//                if (item.maxLoadMT != null) DataRow("Max Load (MT)", formatFloat(item.maxLoadMT)!!)
//                if (item.wheels != null) DataRow("Wheels", item.wheels.toString())
//                if (hasText(item.vehicleCategory)) DataRow("Vehicle Category", item.vehicleCategory!!)
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text("Party's Rate", fontSize = 12.sp, color = Color(0xFF8E8E8E))
//                    Spacer(modifier = Modifier.height(6.dp))
//                    if (hasText(item.partyRate)) {
//                        Text(
//                            text = item.partyRate!!,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = Color(0xFF111111)
//                        )
//                    } else Spacer(modifier = Modifier.height(0.dp))
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Button(
//                    onClick = {
//                        Log.d("LOAD_CARD_NAV", "Confirm button clicked")
//
//                        try { onConfirm() } catch (_: Exception) {}
//
//                        val payload = mapOf<String, Any?>(
//                            "from" to item.from,
//                            "fromPinState" to item.fromPinState,
//                            "to" to item.to,
//                            "toPinState" to item.toPinState,
//                            "material" to item.material,
//                            "hauler" to item.hauler,
//                            "maxLoadMT" to item.maxLoadMT,
//                            "wheels" to item.wheels,
//                            "vehicleCategory" to item.vehicleCategory,
//                            "partyRate" to item.partyRate,
//                            "distanceInfo" to item.distanceInfo
//                        )
//
//                        safeNavigate(
//                            navController = navController,
//                            targetRoute = "vehicle_kyc",
//                            source = "LoadCard → Confirm Button",
//                            payload = payload
//                        )
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = ConfirmGreen),
//                    shape = RoundedCornerShape(12.dp),
//                    modifier = Modifier.height(46.dp).widthIn(min = 120.dp)
//                ) {
//                    Text(
//                        text = "Confirm",
//                        color = ConfirmGreenText,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//            }
//        }
//    }
//}
//@Composable
//fun LoadListScreen(navController: NavHostController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF8F8F9))
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            IconButton(onClick = { navController.popBackStack() }) {
//                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
//            }
//            Text("Loads", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//        }
//
//        // <-- IMPORTANT: local list variable (empty for now, no hardcode)
//        val loadItems = remember { emptyList<LoadItem>() }
//
//        if (loadItems.isEmpty()) {
//            // Empty state (friendly message)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(text = "No loads available", fontSize = 14.sp)
//            }
//        } else {
//            // When data arrives, this will display properly
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .padding(horizontal = 16.dp),
//                verticalArrangement = Arrangement.spacedBy(14.dp),
//                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
//            ) {
//                items(loadItems) { item ->
//                    LoadCard(
//                        item = item,
//                        navController = navController
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//    }
//}
data class LoadItem(
    val from: String,
    val fromPinState: String,
    val to: String,
    val toPinState: String,
    val containerInfo: String?,
    val loadingTime: String?,
    val distanceInfo: String?,
    val partyRate: String?,
    val material: String?,
    val hauler: String?,
    val maxLoadMT: Float?,    // parsed numeric
    val wheels: Int?,         // parsed numeric
    val vehicleCategory: String?,
    val orderId: String? = null,
    val lineItemId: String? = null,

    // NEW fields requested by you: orderNumber and status
    val orderNumber: String? = null,
    val status: String? = null
)

data class Vehicle(
    val id: String,
    val title: String,
    val spec: String,
    val distanceInfo: Float,
    val numberPlate: String,
    val model: String,
    val fuelCapacityLiters: Float
)

// ------------------------ Config (change RAW_TOKEN with your token) ------------------------
private const val BASE_URL = "https://tms-test.cjdarcl.com:8002/"
private const val RAW_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTk5MDczNTQsInVzZXJJZCI6IjIyYWM1NjFkLWRhNWMtNDE2Ni1hYmJhLWQ5NzNlMmYwNDZhMyIsImVtYWlsIjoibmlraGlsLm1haGFyYUBjamRhcmNsLmNvbSIsIm1vYmlsZU51bWJlciI6Ijk2NTQ2MzI3NDQiLCJvcmdJZCI6ImVmMzAwNjgzLTkwMWItNDc3NC1iZjBlLTk1NWQ0OWU3OTZiYyIsIm5hbWUiOiJOaWtoaWwgU2luZ2ggTWFoYXJhIiwib3JnVHlwZSI6IkZMRUVUX09XTkVSIiwiaXNHb2QiOnRydWUsInBvcnRhbFR5cGUiOiJiYXNpYyJ9.MDDgj22r1Hlu4cbl_qySQpqLndzS1D_JAk0owgnQxJ8" // keep safe

// ------------------------ Retrofit API ------------------------
interface ApiService {
    @GET
    suspend fun getOrders(@Url url: String): Response<JsonElement>
}

private fun createApiService(): ApiService {
    val logging = HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }
    logging.level = HttpLoggingInterceptor.Level.BODY

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(35, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val newReq = original.newBuilder()
                .header("Authorization", "Bearer $RAW_TOKEN")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            Log.d("Network", "Sending request to ${newReq.url}")
            chain.proceed(newReq)
        }
        .addInterceptor(logging)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(ApiService::class.java)
}

// ------------------------ helper to build encoded API URL ------------------------
fun buildOrdersApi(origin: String, destination: String): String {
    val filtersJson = """
        {
          "origin": ["$origin"],
          "destination": ["$destination"],
          "lineItems.expectedPickupDate": {
            "from": null,
            "till": null
          },
          "orderType": ["Order", "MTROrder", "MarketOrder"]
        }
    """.trimIndent()
    val encodedFilters = try {
        URLEncoder.encode(filtersJson, "UTF-8")
    } catch (t: Throwable) {
        Log.w("Orders", "URLEncoder failed: ${t.message}")
        filtersJson.replace(" ", "%20")
    }
    val apiUrl = "https://tms-test.cjdarcl.com:8002/shipment-view/sales/v2/orders?limit=50&filters=$encodedFilters"
    Log.d("Orders", "Built orders API URL (filters encoded) for origin='$origin' destination='$destination' -> $apiUrl")
    return apiUrl
}

// ------------------------ Repository ------------------------
class LoadsRepository(private val api: ApiService) {

    suspend fun fetchOrdersWithFilters(origin: String, destination: String): Response<JsonElement> {
        val apiUrl = buildOrdersApi(origin, destination)
        Log.d("Repository", "fetchOrdersWithFilters calling: $apiUrl")
        return api.getOrders(apiUrl)
    }
}

// ------------------------ UI State ------------------------
sealed class LoadUiState {
    object Idle : LoadUiState()
    object Loading : LoadUiState()
    data class Success(val items: List<LoadItem>) : LoadUiState()
    object Empty : LoadUiState()
    data class Error(val message: String) : LoadUiState()
}

// ------------------------ ViewModel ------------------------
class LoadViewModel(private val repository: LoadsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadUiState>(LoadUiState.Idle)
    val uiState: StateFlow<LoadUiState> = _uiState

    private val _selectedOrigin = MutableStateFlow<String?>(null)
    private val _selectedDestination = MutableStateFlow<String?>(null)

    fun setOrigin(value: String?) {
        _selectedOrigin.value = value
        Log.d("LoadViewModel", "Origin set to: $value")
    }

    fun setDestination(value: String?) {
        _selectedDestination.value = value
        Log.d("LoadViewModel", "Destination set to: $value")
    }

    fun fetchLoadsIfReady() {
        val ori = _selectedOrigin.value
        val dest = _selectedDestination.value

        if (ori.isNullOrBlank() || dest.isNullOrBlank()) {
            Log.d("LoadViewModel", "fetchLoadsIfReady: origin or destination missing -> not fetching")
            return
        }

        fetchLoads(ori, dest)
    }

    private fun fetchLoads(originSelection: String, destinationSelection: String) {
        viewModelScope.launch {
            _uiState.value = LoadUiState.Loading
            Log.d("LoadViewModel", "Starting fetchLoads for selection '$originSelection' -> '$destinationSelection'")

            try {
                val response = repository.fetchOrdersWithFilters(
                    origin = originSelection,
                    destination = destinationSelection
                )

                val statusCode = response.code()
                val isSuccessful = response.isSuccessful

                Log.d(
                    "LoadViewModel",
                    "Response received: code=$statusCode, successful=$isSuccessful"
                )

                if (!isSuccessful) {
                    val errBody = try { response.errorBody()?.string() } catch (t: Throwable) { null }
                    Log.e("LoadViewModel", "API ERROR code=$statusCode body=${errBody?.take(300) ?: "no body"}")
                    _uiState.value = LoadUiState.Error("Server error: $statusCode - ${errBody?.take(300) ?: "no body"}")
                    return@launch
                }

                val body = response.body()

                if (body == null) {
                    Log.w("LoadViewModel", "Empty response body")
                    _uiState.value = LoadUiState.Empty
                    return@launch
                }

                val items = parseJsonToLoadItems(body)

                Log.d("LoadViewModel", "Parsed ${items.size} items before filtering by UI selections")

                val filtered = items.filter { item ->
                    matchesSelection(item.from, originSelection) && matchesSelection(item.to, destinationSelection)
                }

                Log.d("LoadViewModel", "After filtering by selection: ${filtered.size} items")

                _uiState.value = if (filtered.isEmpty()) LoadUiState.Empty else LoadUiState.Success(filtered)

            } catch (e: java.io.IOException) {
                Log.e("LoadViewModel", "Network IO exception", e)
                _uiState.value = LoadUiState.Error("Network error: ${e.message ?: "IO error"}")

            } catch (e: Exception) {
                Log.e("LoadViewModel", "Unknown exception", e)
                _uiState.value = LoadUiState.Error("Unknown error: ${e.message ?: "error"}")
            }
        }
    }

    private fun matchesSelection(itemValue: String?, selection: String?): Boolean {
        if (selection.isNullOrBlank()) return true
        if (itemValue.isNullOrBlank()) return false

        val selParts = selection.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val itemLower = itemValue.lowercase()
        return selParts.any { part -> itemLower == part || itemLower.contains(part) }
    }

    // ---------------- JSON parsing & Party Rate calculation ----------------
    private fun parseJsonToLoadItems(json: JsonElement): List<LoadItem> {
        val result = mutableListOf<LoadItem>()

        try {
            val jsonElement = json
            val array: JsonArray? = when {
                jsonElement.isJsonArray -> jsonElement.asJsonArray
                jsonElement.isJsonObject -> {
                    val obj = jsonElement.asJsonObject
                    when {
                        obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                        obj.has("orders") && obj.get("orders").isJsonArray -> obj.getAsJsonArray("orders")
                        obj.has("items") && obj.get("items").isJsonArray -> obj.getAsJsonArray("items")
                        else -> obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
                    }
                }
                else -> null
            }

            if (array == null) {
                Log.w("LoadParser", "No JSON array found in response root")
                return emptyList()
            }

            Log.d("LoadParser", "Array length = ${array.size()}")

            for ((index, orderEl) in array.withIndex()) {
                if (!orderEl.isJsonObject) {
                    Log.w("LoadParser", "Skipping non-object element at index $index")
                    continue
                }
                val orderObj = orderEl.asJsonObject

                // order id for logging
                val orderId = try {
                    when {
                        orderObj.has("id") && !orderObj.get("id").isJsonNull -> orderObj.get("id").asString
                        orderObj.has("orderNumber") && !orderObj.get("orderNumber").isJsonNull -> orderObj.get("orderNumber").asString
                        else -> null
                    }
                } catch (_: Exception) { null }

                // NEW: Try to extract explicit orderNumber (if present separately)
                val orderNumberFromOrder = try {
                    if (orderObj.has("orderNumber") && !orderObj.get("orderNumber").isJsonNull) orderObj.get("orderNumber").asString else null
                } catch (_: Exception) { null }

                // NEW: Try to extract top-level status (order status). We'll also fallback to other keys later.
                val topLevelStatus = try {
                    when {
                        orderObj.has("status") && !orderObj.get("status").isJsonNull -> orderObj.get("status").asString
                        orderObj.has("orderStatus") && !orderObj.get("orderStatus").isJsonNull -> orderObj.get("orderStatus").asString
                        else -> null
                    }
                } catch (_: Exception) { null }

                if (!orderObj.has("lineItems") || !orderObj.get("lineItems").isJsonArray) {
                    Log.d("PartyRate", "OrderId=${orderId ?: "unknown"} PartyRate not calculable — no lineItems")
                    continue
                }

                val lineItems = orderObj.getAsJsonArray("lineItems")
                for (liIdx in 0 until lineItems.size()) {
                    val liEl = lineItems.get(liIdx)
                    if (!liEl.isJsonObject) continue
                    val liObj = liEl.asJsonObject

                    val lineItemId = try { if (liObj.has("id") && !liObj.get("id").isJsonNull) liObj.get("id").asString else null } catch (_: Exception) { null }

                    // Extract mandatory fields required for Party Rate calculation
                    // distance
                    val distance = try {
                        if (liObj.has("distance") && !liObj.get("distance").isJsonNull) {
                            val v = liObj.get("distance")
                            when {
                                v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> v.asFloat
                                v.isJsonPrimitive -> v.asString.toFloatOrNull()
                                else -> null
                            }
                        } else null
                    } catch (_: Exception) { null }

                    // freightChargeType
                    val freightChargeType = try { if (liObj.has("freightChargeType") && !liObj.get("freightChargeType").isJsonNull) liObj.get("freightChargeType").asString else null } catch (_: Exception) { null }

                    // freightChargeRate -> parse as Double (supports numbers and numeric strings with commas)
                    val freightChargeRate = try {
                        if (liObj.has("freightChargeRate") && !liObj.get("freightChargeRate").isJsonNull) {
                            val v = liObj.get("freightChargeRate")
                            when {
                                v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> v.asDouble
                                v.isJsonPrimitive -> v.asString.replace(",", "").trim().toDoubleOrNull()
                                else -> null
                            }
                        } else null
                    } catch (_: Exception) { null }

                    // netQuantity: remainingPlannedQuantity.weight.netQuantity
                    val netQuantity = try {
                        if (liObj.has("remainingPlannedQuantity") && liObj.get("remainingPlannedQuantity").isJsonObject) {
                            val rem = liObj.getAsJsonObject("remainingPlannedQuantity")
                            if (rem.has("weight") && rem.get("weight").isJsonObject) {
                                val wt = rem.getAsJsonObject("weight")
                                val nq = when {
                                    wt.has("netQuantity") && !wt.get("netQuantity").isJsonNull -> {
                                        val v = wt.get("netQuantity")
                                        when {
                                            v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> v.asFloat
                                            v.isJsonPrimitive -> v.asString.toFloatOrNull()
                                            else -> null
                                        }
                                    }
                                    else -> null
                                }
                                nq
                            } else null
                        } else null
                    } catch (_: Exception) { null }

                    // material -> loadInfo.material
                    val material = try {
                        if (liObj.has("loadInfo") && liObj.get("loadInfo").isJsonObject) {
                            val li = liObj.getAsJsonObject("loadInfo")
                            if (li.has("material") && !li.get("material").isJsonNull) li.get("material").asString else null
                        } else null
                    } catch (_: Exception) { null }

                    // consigner state
                    val consignerState = try {
                        if (liObj.has("consigner") && liObj.get("consigner").isJsonObject) {
                            val con = liObj.getAsJsonObject("consigner")
                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
                                val p0 = con.getAsJsonArray("places").get(0)
                                if (p0.isJsonObject && p0.asJsonObject.has("state") && !p0.asJsonObject.get("state").isJsonNull) p0.asJsonObject.get("state").asString else null
                            } else if (con.has("state") && !con.get("state").isJsonNull) con.get("state").asString else null
                        } else null
                    } catch (_: Exception) { null }

                    // consignee state
                    val consigneeState = try {
                        if (liObj.has("consignee") && liObj.get("consignee").isJsonObject) {
                            val con = liObj.getAsJsonObject("consignee")
                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
                                val p0 = con.getAsJsonArray("places").get(0)
                                if (p0.isJsonObject && p0.asJsonObject.has("state") && !p0.asJsonObject.get("state").isJsonNull) p0.asJsonObject.get("state").asString else null
                            } else if (con.has("state") && !con.get("state").isJsonNull) con.get("state").asString else null
                        } else null
                    } catch (_: Exception) { null }

                    // allowedCustomerLoadTypes -> chassisTypes[] and other attributes
                    var truckTypeDisplay: String? = null
                    var wheelsDerived: Int? = null
                    var vehicleCategory: String? = null
                    var haulerName: String? = null
                    var maxLoadMT: Float? = null

                    try {
                        if (liObj.has("allowedCustomerLoadTypes") && liObj.get("allowedCustomerLoadTypes").isJsonArray) {
                            val actArr = liObj.getAsJsonArray("allowedCustomerLoadTypes")
                            if (actArr.size() > 0) {
                                val ac0 = actArr.get(0)
                                if (ac0.isJsonObject) {
                                    val acObj = ac0.asJsonObject
                                    haulerName = acObj.get("name")?.takeIf { !it.isJsonNull }?.let { if (it.isJsonPrimitive) it.asString else it.toString() }
                                    vehicleCategory = acObj.get("vehicleCategory")?.takeIf { !it.isJsonNull }?.let { if (it.isJsonPrimitive) it.asString else it.toString() }

                                    try {
                                        val pmt = acObj.get("passingCapacityMT")
                                        if (pmt != null && !pmt.isJsonNull && pmt.isJsonPrimitive && pmt.asJsonPrimitive.isNumber) {
                                            maxLoadMT = try { pmt.asFloat } catch (_: Throwable) { pmt.asString.toFloatOrNull() }
                                        } else {
                                            maxLoadMT = acObj.get("passingCapacityMT")?.asString?.toFloatOrNull()
                                        }
                                    } catch (_: Exception) { }

                                    try {
                                        val nw = acObj.get("numberOfWheels")
                                        if (nw != null && !nw.isJsonNull && nw.isJsonPrimitive && nw.asJsonPrimitive.isNumber) {
                                            wheelsDerived = try { nw.asInt } catch (_: Throwable) { nw.asString.toIntOrNull() }
                                        } else {
                                            wheelsDerived = acObj.get("numberOfWheels")?.asString?.toIntOrNull()
                                        }
                                    } catch (_: Exception) { }

                                    // chassisTypes[]
                                    try {
                                        if (acObj.has("chassisTypes") && acObj.get("chassisTypes").isJsonArray) {
                                            val chArr = acObj.getAsJsonArray("chassisTypes")
                                            if (chArr.size() > 0) {
                                                val ch0 = chArr.get(0)
                                                val chStr = if (ch0.isJsonPrimitive) ch0.asString else ch0.toString()
                                                truckTypeDisplay = chStr
                                                // derive wheels by regex like TRUCK-14W or 14W
                                                val regex = Regex("(\\d+)")
                                                val match = regex.find(chStr)
                                                if (match != null) {
                                                    wheelsDerived = match.groupValues[1].toIntOrNull() ?: wheelsDerived
                                                }
                                            }
                                        }
                                    } catch (_: Exception) { }
                                }
                            }
                        }
                    } catch (_: Exception) { }

                    // Validation: mandatory fields
                    // declare upfront so it's visible for the rest of the loop
                    var partyRateString: String = "Rate on Request"

                    val mandatoryMissing =
                        distance == null ||
                                freightChargeType.isNullOrBlank() ||
                                freightChargeRate == null ||
                                consignerState.isNullOrBlank() ||
                                consigneeState.isNullOrBlank()

                    val compositeId = listOfNotNull(orderId, lineItemId).joinToString(":")

                    if (mandatoryMissing) {
                        val missingReasons = mutableListOf<String>()

                        if (distance == null) missingReasons.add("distance")
                        if (freightChargeType.isNullOrBlank()) missingReasons.add("freightChargeType")
                        if (freightChargeRate == null) missingReasons.add("freightChargeRate")
                        if (consignerState.isNullOrBlank()) missingReasons.add("consignerState")
                        if (consigneeState.isNullOrBlank()) missingReasons.add("consigneeState")
                        if (truckTypeDisplay.isNullOrBlank()) missingReasons.add("truckType / chassisType")

                        Log.d(
                            "PartyRate",
                            """
❌ Party Rate NOT calculated
LoadId : ${if (compositeId.isNotEmpty()) compositeId else "unknown"}
Missing : ${missingReasons.joinToString(", ")}
Raw Values →
    distance = $distance
    freightChargeType = $freightChargeType
    freightChargeRate = $freightChargeRate
    netQuantity = $netQuantity
    consignerState = $consignerState
    consigneeState = $consigneeState
    truckType = $truckTypeDisplay
""".trimIndent()
                        )

                        // partyRateString remains the fallback "Rate on Request"
                    } else {
                        // preliminary partyRateString (will be overwritten below if we successfully compute perMT)
                        partyRateString = when {
                            freightChargeType?.equals("perMT", true) == true &&
                                    netQuantity != null &&
                                    netQuantity > 0 &&
                                    freightChargeRate != null -> {
                                val amount = freightChargeRate * netQuantity
                                "₹ ${amount.toLong()}"
                            }

                            freightChargeType?.equals("perVehicle", true) == true &&
                                    freightChargeRate != null -> {
                                "₹ ${freightChargeRate.toLong()}"
                            }

                            else -> "Rate on Request"
                        }
                    }

                    // Only handle perMT per your rules
                    if (freightChargeType?.equals("perMT", ignoreCase = true) != true) {
                        Log.d(
                            "PartyRate",
                            "LoadId=${compositeId.ifEmpty { "unknown" }} PartyRate not calculable — freightChargeType=$freightChargeType"
                        )
                        continue
                    }

                    // compute party rate: freightChargeRate * netQuantity
                    val computed = try {
                        val netQty = netQuantity ?: 0f
                        val rate = freightChargeRate ?: 0.0
                        val value = (rate * netQty.toDouble())
                        if (value.isFinite()) value else null
                    } catch (t: Throwable) {
                        null
                    }

                    if (computed == null || computed <= 0.0) {
                        Log.d(
                            "PartyRate",
                            "LoadId=${compositeId.ifEmpty { "unknown" }} PartyRate not calculable — zero or invalid computed value"
                        )
                        continue
                    }

                    // Format in INR with no decimals
                    val amountLong = round(computed).toLong()
                    val formattedNumber = NumberFormat.getNumberInstance(Locale("en", "IN")).format(amountLong)
                    partyRateString = "₹ $formattedNumber"

                    Log.d(
                        "PartyRate",
                        """
    LoadId=${compositeId.ifEmpty { "unknown" }}
    Calculation:
    freightChargeType=perMT
    freightChargeRate=$freightChargeRate
    netQuantityMT=$netQuantity
    Formula=freightChargeRate × netQuantity
    PartyRate=$partyRateString
    """.trimIndent()
                    )


                    // build display strings for distance and others
                    val distanceInfo = distance?.let { "${String.format("%.0f", it)} km" }

                    // fallback for names: try to pick first place name from consigner/consignee or salesOffice/customer
                    val fromName = try {
                        if (liObj.has("consigner") && liObj.get("consigner").isJsonObject) {
                            val con = liObj.getAsJsonObject("consigner")
                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
                                val p0 = con.getAsJsonArray("places").get(0)
                                if (p0.isJsonObject && p0.asJsonObject.has("name") && !p0.asJsonObject.get("name").isJsonNull) p0.asJsonObject.get("name").asString else null
                            } else null
                        } else null
                    } catch (_: Exception) { null } ?: orderObj.getAsJsonObject("salesOffice")?.get("name")?.asString ?: "N/A"

                    val toName = try {
                        if (liObj.has("consignee") && liObj.get("consignee").isJsonObject) {
                            val con = liObj.getAsJsonObject("consignee")
                            if (con.has("places") && con.get("places").isJsonArray && con.getAsJsonArray("places").size() > 0) {
                                val p0 = con.getAsJsonArray("places").get(0)
                                if (p0.isJsonObject && p0.asJsonObject.has("name") && !p0.asJsonObject.get("name").isJsonNull) p0.asJsonObject.get("name").asString else null
                            } else null
                        } else null
                    } catch (_: Exception) { null } ?: orderObj.getAsJsonObject("customer")?.getAsJsonArray("places")?.get(0)?.asJsonObject?.get("name")?.asString ?: "N/A"

                    // NEW: determine status to put on the LoadItem
                    val statusFromLineItem = try {
                        if (liObj.has("status") && !liObj.get("status").isJsonNull) liObj.get("status").asString else null
                    } catch (_: Exception) { null }

                    val statusFinal = statusFromLineItem ?: topLevelStatus

                    // NEW: determine orderNumber to put on the LoadItem (prefer explicit orderNumber, else orderId)
                    val orderNumberFinal = orderNumberFromOrder ?: orderId

                    val item = LoadItem(
                        from = fromName,
                        fromPinState = consignerState ?: "",
                        to = toName,
                        toPinState = consigneeState ?: "",
                        containerInfo = try { liObj.get("containerInfo")?.asString } catch (_: Exception) { null },
                        loadingTime = try { liObj.get("loadingTime")?.asString } catch (_: Exception) { null },
                        distanceInfo = distanceInfo,
                        partyRate = partyRateString,

                        material = material ?: "-",
                        hauler = haulerName ?: "-",
                        maxLoadMT = maxLoadMT,
                        wheels = wheelsDerived,
                        vehicleCategory = vehicleCategory ?: "-",

                        orderId = orderId,
                        lineItemId = lineItemId,

                        // NEW fields
                        orderNumber = orderNumberFinal,
                        status = statusFinal
                    )

                    result.add(item)

                }
            }

        } catch (t: Throwable) {
            Log.e("LoadParser", "Error parsing JSON", t)
        }

        return result
    }
}

// Simple ViewModelFactory
class LoadViewModelFactory(private val repo: LoadsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoadViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ------------------------ Composables: integrate ViewModel ------------------------
@Composable
fun LoadScreen(navController: NavHostController) {
    val apiService = remember { createApiService() }
    val repo = remember { LoadsRepository(apiService) }
    val vm: LoadViewModel = viewModel(factory = LoadViewModelFactory(repo))

    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = { LoadTopBar(navController) },
        bottomBar = {  },
        containerColor = Color(0xFFF6F7FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 12.dp
                )
        ) {
            StatusRows(
                onApplySelection = { originSelection: String?, destSelection: String? ->
                    vm.setOrigin(originSelection)
                    vm.setDestination(destSelection)
                    vm.fetchLoadsIfReady()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            BannerCard()
            Spacer(modifier = Modifier.height(12.dp))
            SegmentedTabs(navController)

            Spacer(modifier = Modifier.height(10.dp))

            when (uiState) {
                is LoadUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Choose origin & destination then tap View Loads", color = Color(0xFF9B9B9B))
                    }
                }
                is LoadUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF9B9B9B), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No loads found for the selected route.", color = Color(0xFF6B6B6B))
                        }
                    }
                }
                is LoadUiState.Error -> {
                    val msg = (uiState as LoadUiState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Something went wrong", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(msg, color = Color(0xFF6B6B6B))
                        }
                    }
                }
                is LoadUiState.Success -> {
                    val items = (uiState as LoadUiState.Success).items

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 160.dp)
                    ) {
                        items(items) { load ->
                            LoadCard(
                                item = load,
                                navController = navController, // ✅ REQUIRED FIX
                                onConfirm = {
                                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                                    if (currentRoute != "select_vehicle") {
                                        navController.navigate("select_vehicle") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onCardClick = {
                                    navController.navigate(BottomNavItem.VehicleKYC.route)
                                }

                            )

                        }
                    }
                }
            }
        }
    }
}

// ------------------------ StatusRows and other UI components (unchanged) ------------------------
@Composable
fun StatusRows(
    onApplySelection: (originSelection: String?, destSelection: String?) -> Unit
) {
    val TAG = "StatusRows"
    val green = Color(0xFF23A455)
    var showPreview by remember { mutableStateOf(false) }
    var showUnloadingPreview by remember { mutableStateOf(false) }

    var loadingPlaceholder by remember { mutableStateOf<String?>(null) }
    var unloadingPlaceholder by remember { mutableStateOf<String?>(null) }

    val loadingCities = listOf(
        "All NCR", "Central Delhi", "East Delhi", "New Delhi",
        "North Delhi", "North East Delhi", "North West Delhi",
        "Shahdara", "South Delhi", "South East Delhi", "South West Delhi" , "AMBALA"
    )
    val unloadingCities = listOf(
        "Rajasthan", "Himachal Pradesh", "Nagaland", "Uttarakhand",
        "Andhra Pradesh", "Madhya Pradesh", "Lakshadweep", "Meghalaya",
        "Sikkim", "Kerala", "Chhattisgarh", "Tamil Nadu" , "KOLKATA"
    )
    val loadingChecked = remember { mutableStateListOf<Boolean>().apply { repeat(loadingCities.size) { add(false) } } }
    val unloadingChecked = remember { mutableStateListOf<Boolean>().apply { repeat(unloadingCities.size) { add(false) } } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        StatusRowSelectable(
            label = "Loading",
            dotColor = Color(0xFF1EB980),
            placeholder = loadingPlaceholder ?: "कृपया शहर चुनें",
            onClick = { showPreview = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        StatusRowSelectable(
            label = "Unloading",
            dotColor = Color(0xFFED5565),
            placeholder = unloadingPlaceholder ?: "कृपया शहर चुनें",
            onClick = { showUnloadingPreview = true }
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showPreview) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
                        ) {
                            IconButton(onClick = { showPreview = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                            Text(
                                text = "Select Loading",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 68.dp, bottom = 88.dp, start = 12.dp, end = 12.dp)
                        ) {
                            itemsIndexed(loadingCities) { idx, city ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                ) {
                                    Checkbox(
                                        checked = loadingChecked[idx],
                                        onCheckedChange = { loadingChecked[idx] = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = green,
                                            uncheckedColor = Color.Gray,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(city)
                                }
                                Divider(color = Color(0xFFFAFAFA), thickness = 1.dp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 18.dp, vertical = 18.dp)
                        ) {
                            Button(
                                onClick = {
                                    val selected = loadingCities.mapIndexedNotNull { i, c -> if (loadingChecked[i]) c else null }
                                    loadingPlaceholder = if (selected.isEmpty()) null else selected.joinToString(", ")
                                    Log.d(TAG, "Loading selected: $loadingPlaceholder")
                                    showPreview = false
                                    onApplySelection(loadingPlaceholder, unloadingPlaceholder)
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green)
                            ) {
                                Text(text = "View Loads", fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnloadingPreview) {
        Dialog(
            onDismissRequest = { showUnloadingPreview = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))

                Surface(
                    modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            IconButton(onClick = { showUnloadingPreview = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                            Text(
                                text = "Select Unloading",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            itemsIndexed(unloadingCities) { idx, city ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                ) {
                                    Checkbox(
                                        checked = unloadingChecked[idx],
                                        onCheckedChange = { unloadingChecked[idx] = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = green,
                                            uncheckedColor = Color.Gray,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = city, fontSize = 16.sp)
                                }
                                Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp)
                        ) {
                            Button(
                                onClick = {
                                    val selected = unloadingCities.mapIndexedNotNull { i, c -> if (unloadingChecked[i]) c else null }
                                    unloadingPlaceholder = if (selected.isEmpty()) null else selected.joinToString(", ")
                                    Log.d(TAG, "Unloading selected: $unloadingPlaceholder")
                                    showUnloadingPreview = false
                                    onApplySelection(loadingPlaceholder, unloadingPlaceholder)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green)
                            ) {
                                Text(text = "View Loads", fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusRowSelectable(
    label: String,
    dotColor: Color,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = Color(0xFF6B6B6B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ------------------------ rest of UI components (BannerCard, Tabs, TopBar, LoadCard) ------------------------
@Composable
fun BannerCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFDAEFB8), Color(0xFFBDE07B))
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text("Need load for your truck?", fontSize = 14.sp, color = Color(0xFF2E2E2E))
                Text(
                    "Mark truck empty now",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* update */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1157FF))
                ) {
                    Text("Update now", color = Color.White)
                }
            }
        }
    }
}

sealed class TabItem {
    data class Screen(val title: String, val content: @Composable () -> Unit) : TabItem()
    data class Route(val title: String, val route: String?) : TabItem()
}

@Composable
fun SegmentedTabs(navController: NavHostController) {

    val TAG = "SegmentedTabs"

    val selectedState = remember { mutableStateOf(1) }

    val tabs = listOf<TabItem>(
        TabItem.Route("My Load(1)", "my_trips"),
        TabItem.Route("Load(4)", null),
        TabItem.Route("New load(33)", "NEWLOAD")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val tabModifier = Modifier
            .width(110.dp)
            .height(44.dp)
            .shadow(6.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))

        fun cardColor(isSelected: Boolean) =
            if (isSelected) Color.Black else Color(0xFFF6F6F7)

        fun textColor(isSelected: Boolean) =
            if (isSelected) Color.White else Color(0xFF9B9B9B)

        tabs.forEachIndexed { index, tab ->

            val title = when (tab) {
                is TabItem.Screen -> tab.title
                is TabItem.Route -> tab.title
            }

            val isSelected = selectedState.value == index

            Card(
                modifier = tabModifier.clickable {

                    Log.d(TAG, "---------------- TAB CLICK ----------------")
                    Log.d(TAG, "Clicked tab index=$index title='$title'")
                    Log.d(TAG, "Previously selected=${selectedState.value}")

                    selectedState.value = index
                    Log.d(TAG, "POSITIVE: Tab selected -> index=$index")

                    when (tab) {
                        is TabItem.Screen -> {
                            Log.d(TAG, "POSITIVE: Screen tab clicked, rendering local content only")
                        }

                        is TabItem.Route -> {
                            val route = tab.route

                            if (route == null) {
                                Log.e(TAG, "NEGATIVE: Route is NULL for tab '$title'. Navigation skipped.")
                                return@clickable
                            }

                            Log.d(TAG, "Attempting navigation to route='$route'")

                            val node = navController.graph.findNode(route)
                            if (node == null) {
                                Log.e(
                                    TAG,
                                    "NEGATIVE: Route '$route' NOT FOUND in NavGraph. Available start=${navController.graph.startDestinationRoute}"
                                )
                            } else {
                                try {
                                    navController.navigate(route)
                                    Log.d(TAG, "POSITIVE: Navigation SUCCESS to '$route'")
                                } catch (e: Exception) {
                                    Log.e(
                                        TAG,
                                        "NEGATIVE: Navigation FAILED to '$route' -> ${e.message}",
                                        e
                                    )
                                }
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = cardColor(isSelected)),
                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE6E6E6))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor(isSelected),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (val current = tabs[selectedState.value]) {
        is TabItem.Screen -> {
            Log.d(TAG, "Rendering Screen content for tab index=${selectedState.value}")
            current.content()
        }

        is TabItem.Route -> {
            Log.d(TAG, "Route tab selected index=${selectedState.value}, content handled by NavHost")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadTopBar(navController: NavController) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back") }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                Text(text = "Load alert", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                var checked by remember { mutableStateOf(true) }
                Switch(checked = checked, onCheckedChange = { checked = it }, modifier = Modifier.scale(0.85f))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).height(56.dp)
    )
}

val GreenDot = Color(0xFF22C55E)
val RedDot = Color(0xFFEF4444)
val LightGrayText = Color(0xFF9CA3AF)
val TimerYellow = Color(0xFFFACC15)
val PurplePillBg = Color(0xFFE9D5FF)
val PurpleText = Color(0xFF7C3AED)
val ConfirmGreen = Color(0xFF16A34A)
val ConfirmGreenText = Color(0xFF15803D)

//    @Composable
//    fun LoadCard(
//        item: LoadItem,
//        navController: NavHostController,
//        modifier: Modifier = Modifier,
//        onConfirm: () -> Unit = {},
//        onCardClick: () -> Unit = {}
//    ) {
//        fun hasText(s: String?) = !s.isNullOrBlank()
//        fun formatFloat(f: Float?) = f?.let { String.format("%.2f", it) }
//
//        // ================= SAFE NAVIGATION WITH FULL LOGGING & PAYLOAD =================
//        fun safeNavigate(
//            navController: NavHostController?,
//            targetRoute: String,
//            source: String,
//            payload: Map<String, Any?> = emptyMap()
//        ) {
//            val TAG = "LOAD_CARD_NAV"
//            try {
//                Log.d(TAG, "================ NAVIGATION START ================")
//                Log.d(TAG, "Source: $source")
//                Log.d(TAG, "Target Route: \"$targetRoute\"")
//
//                Log.d(TAG, "Params -> from=${item.from}, to=${item.to}, rate=${item.partyRate}")
//                Log.d(TAG, "Params -> material=${item.material}, hauler=${item.hauler}, wheels=${item.wheels}")
//                Log.d(TAG, "Params -> maxLoadMT=${formatFloat(item.maxLoadMT)}, vehicleCategory=${item.vehicleCategory}")
//
//                if (navController == null) {
//                    Log.w(TAG, "Navigation skipped: navController is NULL")
//                    Log.d(TAG, "================ NAVIGATION END ==================")
//                    return
//                }
//
//                val currentRoute = try {
//                    navController.currentBackStackEntry?.destination?.route
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to read current route: ${e.message}")
//                    null
//                }
//
//                Log.d(TAG, "Current Route Before Navigation: \"${currentRoute ?: "UNKNOWN"}\"")
//
//                if (currentRoute == targetRoute) {
//                    Log.w(TAG, "Navigation blocked: currentRoute == targetRoute")
//                    Log.d(TAG, "================ NAVIGATION END ==================")
//                    return
//                }
//
//                // Put payload into SavedStateHandle of the current back stack entry
//                try {
//                    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
//                    if (savedStateHandle != null) {
//                        for ((k, v) in payload) {
//                            // Setting each value separately — destination will read by key
//                            savedStateHandle.set(k, v)
//                            Log.d(TAG, "SavedStateHandle set -> $k = $v")
//                        }
//                    } else {
//                        Log.w(TAG, "No savedStateHandle available on currentBackStackEntry")
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to set payload on SavedStateHandle: ${e.message}")
//                }
//
//                Log.d(TAG, "Executing navController.navigate(\"$targetRoute\")")
//                navController.navigate(targetRoute)
//                Log.d(TAG, "navigate() call executed")
//
//                val afterRoute = try {
//                    navController.currentBackStackEntry?.destination?.route
//                } catch (_: Exception) {
//                    null
//                }
//
//                Log.d(TAG, "Current Route After Navigation: \"${afterRoute ?: "UNKNOWN"}\"")
//                Log.d(TAG, "================ NAVIGATION END ==================")
//
//            } catch (t: Throwable) {
//                Log.e(TAG, "Navigation failed with exception: ${t.message}", t)
//            }
//        }
//        // ====================================================================
//
//        Card(
//            modifier = modifier.fillMaxWidth(),
//            onClick = {
//                Log.d("LOAD_CARD_NAV", "Card clicked")
//
//                try { onCardClick() } catch (_: Exception) {}
//
//                // Build payload from the item - only primitive/serializable primitives (String, Int, Float)
//                val payload = mapOf<String, Any?>(
//                    "from" to item.from,
//                    "fromPinState" to item.fromPinState,
//                    "to" to item.to,
//                    "toPinState" to item.toPinState,
//                    "material" to item.material,
//                    "hauler" to item.hauler,
//                    "maxLoadMT" to item.maxLoadMT,
//                    "wheels" to item.wheels,
//                    "vehicleCategory" to item.vehicleCategory,
//                    "partyRate" to item.partyRate,
//                    "distanceInfo" to item.distanceInfo,
//                    // include new fields in payload as well
//                    "orderNumber" to item.orderNumber,
//                    "status" to item.status
//                )
//
//                safeNavigate(
//                    navController = navController,
//                    targetRoute = BottomNavItem.VehicleKYC.route,
//                    source = "LoadCard → Card Click",
//                    payload = payload
//                )
//
//            },
//            shape = RoundedCornerShape(16.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
//                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
//                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GreenDot))
//                        Spacer(modifier = Modifier.height(6.dp))
//                        Box(modifier = Modifier.width(2.dp).height(36.dp).background(Color(0xFFECECEC)))
//                        Spacer(modifier = Modifier.height(6.dp))
//                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(RedDot))
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Column(modifier = Modifier.weight(1f)) {
//                        if (hasText(item.from)) {
//                            Text(
//                                text = item.from,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color(0xFF111111),
//                                maxLines = 2,
//                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                            )
//                            if (hasText(item.fromPinState)) {
//                                Text(text = item.fromPinState ?: "", fontSize = 12.sp, color = Color(0xFF6B6B6B))
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(10.dp))
//
//                        if (hasText(item.to)) {
//                            Text(
//                                text = item.to,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF111111),
//                                maxLines = 2,
//                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                            )
//                            if (hasText(item.toPinState)) {
//                                Text(text = item.toPinState ?: "", fontSize = 12.sp, color = Color(0xFF6B6B6B))
//                            }
//                        }
//                    }
//
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = null,
//                        tint = Color(0xFF9E9E9E),
//                        modifier = Modifier.size(28.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                if (hasText(item.distanceInfo)) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(PurplePillBg)
//                            .padding(horizontal = 12.dp, vertical = 10.dp)
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(
//                                imageVector = Icons.Default.FlashOn,
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp),
//                                tint = Color(0xFFFFC85A)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = item.distanceInfo ?: "",
//                                fontSize = 13.sp,
//                                color = PurpleText,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(12.dp))
//                }
//
//                Divider(color = Color(0xFFF1F1F1), thickness = 1.dp)
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Column(modifier = Modifier.fillMaxWidth()) {
//                    @Composable
//                    fun DataRow(label: String, value: String) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 6.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = label,
//                                fontSize = 13.sp,
//                                color = Color(0xFF8E8E8E),
//                                modifier = Modifier.weight(0.45f)
//                            )
//                            Text(
//                                text = value,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF111111),
//                                modifier = Modifier.weight(0.55f),
//                                maxLines = 1,
//                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                            )
//                        }
//                    }
//
//                    // ===== ADDED: Show Order Number and Status fetched from backend =====
//                    if (hasText(item.orderNumber)) {
//                        DataRow("Order Number", item.orderNumber!!)
//                    } else if (hasText(item.orderId)) {
//                        // fallback to orderId if explicit orderNumber missing
//                        DataRow("Order Number", item.orderId!!)
//                    }
//
//                    if (hasText(item.status)) {
//                        DataRow("Status", item.status!!)
//                    }
//
//                    // existing fields
//                    if (hasText(item.material)) DataRow("Material", item.material!!)
//                    if (hasText(item.hauler)) DataRow("Hauler", item.hauler!!)
//                    if (item.maxLoadMT != null) DataRow("Max Load (MT)", formatFloat(item.maxLoadMT)!!)
//                    if (item.wheels != null) DataRow("Wheels", item.wheels.toString())
//                    if (hasText(item.vehicleCategory)) DataRow("Vehicle Category", item.vehicleCategory!!)
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text("Party's Rate", fontSize = 12.sp, color = Color(0xFF8E8E8E))
//                        Spacer(modifier = Modifier.height(6.dp))
//                        if (hasText(item.partyRate)) {
//                            Text(
//                                text = item.partyRate!!,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color(0xFF111111)
//                            )
//                        } else Spacer(modifier = Modifier.height(0.dp))
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Button(
//                        onClick = {
//                            Log.d("LOAD_CARD_NAV", "Confirm button clicked")
//
//                            try { onConfirm() } catch (_: Exception) {}
//
//                            val payload = mapOf<String, Any?>(
//                                "from" to item.from,
//                                "fromPinState" to item.fromPinState,
//                                "to" to item.to,
//                                "toPinState" to item.toPinState,
//                                "material" to item.material,
//                                "hauler" to item.hauler,
//                                "maxLoadMT" to item.maxLoadMT,
//                                "wheels" to item.wheels,
//                                "vehicleCategory" to item.vehicleCategory,
//                                "partyRate" to item.partyRate,
//                                "distanceInfo" to item.distanceInfo,
//                                // include new fields in payload as well
//                                "orderNumber" to item.orderNumber,
//                                "status" to item.status
//                            )
//
//                            safeNavigate(
//                                navController = navController,
//                                targetRoute = "vehicle_kyc",
//                                source = "LoadCard → Confirm Button",
//                                payload = payload
//                            )
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = ConfirmGreen),
//                        shape = RoundedCornerShape(12.dp),
//                        modifier = Modifier.height(46.dp).widthIn(min = 120.dp)
//                    ) {
//                        Text(
//                            text = "Confirm",
//                            color = ConfirmGreenText,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            }
//        }
//    }
private val gson = Gson()
@Composable
fun LoadCard(
    item: LoadItem,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    fun hasText(s: String?) = !s.isNullOrBlank()
    fun formatFloat(f: Float?) = f?.let { String.format("%.2f", it) }

    // ================= SAFE NAVIGATION WITH FULL LOGGING & PAYLOAD =================
    fun safeNavigate(
        navController: NavHostController?,
        targetRoute: String,
        source: String,
        payload: Map<String, Any?> = emptyMap()
    ) {
        val TAG = "LOAD_CARD_NAV"
        try {
            Log.d(TAG, "================ NAVIGATION START ================")
            Log.d(TAG, "Source: $source")
            Log.d(TAG, "Target Route: \"$targetRoute\"")

            Log.d(TAG, "Params -> from=${item.from}, to=${item.to}, rate=${item.partyRate}")
            Log.d(TAG, "Params -> material=${item.material}, hauler=${item.hauler}, wheels=${item.wheels}")
            Log.d(TAG, "Params -> maxLoadMT=${formatFloat(item.maxLoadMT)}, vehicleCategory=${item.vehicleCategory}")

            if (navController == null) {
                Log.w(TAG, "Navigation skipped: navController is NULL")
                Log.d(TAG, "================ NAVIGATION END ==================")
                return
            }

            val currentRoute = try {
                navController.currentBackStackEntry?.destination?.route
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read current route: ${e.message}")
                null
            }

            Log.d(TAG, "Current Route Before Navigation: \"${currentRoute ?: "UNKNOWN"}\"")

            if (currentRoute == targetRoute) {
                Log.w(TAG, "Already on $targetRoute. Updating current savedStateHandle instead of navigating.")

                try {
                    val handle = navController.currentBackStackEntry?.savedStateHandle
                    if (handle != null) {
                        for ((k, v) in payload) {
                            handle.set(k, v?.toString())
                            Log.d(TAG, "Updated current.savedStateHandle -> $k = ${v?.toString()}")
                        }
                    } else {
                        Log.w(TAG, "currentBackStackEntry.savedStateHandle is NULL")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed updating current savedStateHandle: ${e.message}", e)
                }

                Log.d(TAG, "================ NAVIGATION END ==================")
                return
            }


            // Put payload into SavedStateHandle of the current back stack entry
            try {
                val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                if (savedStateHandle != null) {
                    for ((k, v) in payload) {
                        // Setting each value separately — destination will read by key
                        savedStateHandle.set(k, v)
                        Log.d(TAG, "SavedStateHandle set -> $k = $v")
                    }
                } else {
                    Log.w(TAG, "No savedStateHandle available on currentBackStackEntry")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set payload on SavedStateHandle: ${e.message}")
            }

            Log.d(TAG, "Executing navController.navigate(\"$targetRoute\")")
            navController.navigate(targetRoute)
            Log.d(TAG, "navigate() call executed")

            val afterRoute = try {
                navController.currentBackStackEntry?.destination?.route
            } catch (_: Exception) {
                null
            }

            Log.d(TAG, "Current Route After Navigation: \"${afterRoute ?: "UNKNOWN"}\"")
            Log.d(TAG, "================ NAVIGATION END ==================")

        } catch (t: Throwable) {
            Log.e(TAG, "Navigation failed with exception: ${t.message}", t)
        }
    }
    // ====================================================================

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            Log.d("LOAD_CARD_NAV", "Card clicked")

            try { onCardClick() } catch (_: Exception) {}

            // Serialize entire item to JSON so we can pass the whole object to the next screen
            val itemJson = try {
                gson.toJson(item)
            } catch (e: Exception) {
                Log.e("LOAD_CARD_NAV", "Failed to serialize LoadItem to JSON: ${e.message}")
                null
            }

            // Log the full item (JSON) right here so you can see it in Logcat
            Log.d("LOAD_CARD_NAV", "Selected LoadItem JSON (on click): $itemJson")
            Log.d("LOAD_CARD_NAV", "Selected LoadItem toString (on click): $item")

            // Build payload from the item - include both primitive fields and the full JSON
            val payload = mapOf<String, Any?>(
                "from" to item.from,
                "fromPinState" to item.fromPinState,
                "to" to item.to,
                "toPinState" to item.toPinState,
                "material" to item.material,
                "hauler" to item.hauler,
                "maxLoadMT" to item.maxLoadMT,
                "wheels" to item.wheels,
                "vehicleCategory" to item.vehicleCategory,
                "partyRate" to item.partyRate,
                "distanceInfo" to item.distanceInfo,
                // include new fields in payload as well
                "orderNumber" to item.orderNumber,
                "status" to item.status,
                // full serialized item
                "selectedItemJson" to itemJson
            )

            safeNavigate(
                navController = navController,
                targetRoute = BottomNavItem.VehicleKYC.route,
                source = "LoadCard → Card Click",
                payload = payload
            )

        },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GreenDot))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.width(2.dp).height(36.dp).background(Color(0xFFECECEC)))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(RedDot))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (hasText(item.from)) {
                        Text(
                            text = item.from,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111111),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (hasText(item.fromPinState)) {
                            Text(text = item.fromPinState ?: "", fontSize = 12.sp, color = Color(0xFF6B6B6B))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (hasText(item.to)) {
                        Text(
                            text = item.to,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111111),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (hasText(item.toPinState)) {
                            Text(text = item.toPinState ?: "", fontSize = 12.sp, color = Color(0xFF6B6B6B))
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasText(item.distanceInfo)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurplePillBg)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFFFC85A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.distanceInfo ?: "",
                            fontSize = 13.sp,
                            color = PurpleText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = Color(0xFFF1F1F1), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                @Composable
                fun DataRow(label: String, value: String) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E8E),
                            modifier = Modifier.weight(0.45f)
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111111),
                            modifier = Modifier.weight(0.55f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                // ===== ADDED: Show Order Number and Status fetched from backend =====
                if (hasText(item.orderNumber)) {
                    DataRow("Order Number", item.orderNumber!!)
                } else if (hasText(item.orderId)) {
                    // fallback to orderId if explicit orderNumber missing
                    DataRow("Order Number", item.orderId!!)
                }

                if (hasText(item.status)) {
                    DataRow("Status", item.status!!)
                }

                // existing fields
                if (hasText(item.material)) DataRow("Material", item.material!!)
                if (hasText(item.hauler)) DataRow("Hauler", item.hauler!!)
                if (item.maxLoadMT != null) DataRow("Max Load (MT)", formatFloat(item.maxLoadMT)!!)
                if (item.wheels != null) DataRow("Wheels", item.wheels.toString())
                if (hasText(item.vehicleCategory)) DataRow("Vehicle Category", item.vehicleCategory!!)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Party's Rate", fontSize = 12.sp, color = Color(0xFF8E8E8E))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (hasText(item.partyRate)) {
                        Text(
                            text = item.partyRate!!,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111111)
                        )
                    } else Spacer(modifier = Modifier.height(0.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        Log.d("LOAD_CARD_NAV", "Confirm button clicked")

                        try { onConfirm() } catch (_: Exception) {}

                        val itemJson = try {
                            gson.toJson(item)
                        } catch (e: Exception) {
                            Log.e("LOAD_CARD_NAV", "Failed to serialize LoadItem to JSON: ${e.message}")
                            null
                        }

                        val payload = mapOf<String, Any?>(
                            "from" to item.from,
                            "fromPinState" to item.fromPinState,
                            "to" to item.to,
                            "toPinState" to item.toPinState,
                            "material" to item.material,
                            "hauler" to item.hauler,
                            "maxLoadMT" to item.maxLoadMT,
                            "wheels" to item.wheels,
                            "vehicleCategory" to item.vehicleCategory,
                            "partyRate" to item.partyRate,
                            "distanceInfo" to item.distanceInfo,
                            // include new fields in payload as well
                            "orderNumber" to item.orderNumber,
                            "status" to item.status,
                            "selectedItemJson" to itemJson
                        )

                        safeNavigate(
                            navController = navController,
                            targetRoute = BottomNavItem.VehicleKYC.route, // align with card-click route
                            source = "LoadCard → Confirm Button",
                            payload = payload
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConfirmGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp).widthIn(min = 120.dp)
                ) {
                    Text(
                        text = "Confirm",
                        color = ConfirmGreenText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
@Composable
fun LoadListScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F9))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Loads", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // <-- IMPORTANT: local list variable (empty for now, no hardcode)
        val loadItems = remember { emptyList<LoadItem>() }

        if (loadItems.isEmpty()) {
            // Empty state (friendly message)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No loads available", fontSize = 14.sp)
            }
        } else {
            // When data arrives, this will display properly
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
            ) {
                items(loadItems) { item ->
                    LoadCard(
                        item = item,
                        navController = navController
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectionScreen(nav: NavController) {
    // sample vehicles - replace with your real data
    val allVehicles = remember {
        listOf(
            Vehicle(
                id = "v1",
                title = "Truck",
                spec = "Container · 33.0 Ft · 10.0 Ton",
                distanceInfo = 30f,
                numberPlate = "HR47G2320",
                model = "Container Truck",
                fuelCapacityLiters = 300f
            ),
            Vehicle(
                id = "v2",
                title = "Truck",
                spec = "Container · 32.0 Ft · 10.0 Ton",
                distanceInfo = 65f,
                numberPlate = "HR47E9396",
                model = "Container Truck",
                fuelCapacityLiters = 300f
            ),
            Vehicle(
                id = "v3",
                title = "Truck",
                spec = "Container · 32.0 Ft · 10.0 Ton",
                distanceInfo = 75f,
                numberPlate = "HR47F0106",
                model = "Container Truck",
                fuelCapacityLiters = 300f
            ),
            Vehicle(
                id = "v4",
                title = "Truck",
                spec = "Container · 32.0 Ft · 9.5 Ton",
                distanceInfo = 105f,
                numberPlate = "HR47E7551",
                model = "Container Truck",
                fuelCapacityLiters = 300f
            ),
            Vehicle(
                id = "v5",
                title = "Truck",
                spec = "Container · 32.0 Ft · 6.0 Ton",
                distanceInfo = 105f,
                numberPlate = "HR47F5121",
                model = "Container Truck",
                fuelCapacityLiters = 300f
            )
        )
    }


    var query by remember { mutableStateOf(TextFieldValue("")) }
    var selectedId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(query.text, allVehicles) {
        if (query.text.isBlank()) allVehicles
        else allVehicles.filter {
            it.title.contains(query.text, ignoreCase = true) ||
                    it.spec.contains(query.text, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text("Select Vehicle")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search vehicle") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filtered) { v ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .clickable { selectedId = v.id }
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = selectedId == v.id,
                                            onClick = { selectedId = v.id }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                v.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(v.spec, fontSize = 13.sp, color = LightGrayText)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PurplePillBg)
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.FlashOn,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color(0xFFFFC85A)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "${v.distanceInfo.toInt()} km near loading",
                                                fontSize = 13.sp,
                                                color = PurpleText,
                                                fontWeight = FontWeight.Medium
                                            )

                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {

                            nav.popBackStack()
                        },
                        enabled = selectedId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFECCE))
                    ) {
                        Text(
                            text = "Submit",
                            color = if (selectedId != null) ConfirmGreenText else Color.White.copy(
                                alpha = 0.6f
                            ),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    )
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list_screen"
    ) {

        composable("list_screen") {
            LoadListScreen(
                navController = navController
            )
        }


        composable("detail_screen") {
            VehicleSelectionScreen(
                nav = navController
            )
        }
    }
}


class LoadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadList() {
    MaterialTheme {
        AppNavigation()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLoadScreen(navController: NavHostController) {

    Scaffold(
        containerColor = Color(0xFFF6F6F9),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "View Loads",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
//                        Icon(
//                            painterResource(id = R.drawable.ic_filter),
//                            tint = Color.Black,
//                            contentDescription = null
//                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C4CFF)
                            )
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF2EBF84), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Loading",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .alpha(0.25f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFFF5A44), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Unloading",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(22.dp)
                ) {
                    Text(
                        "Load confirm in 30 min\nor get token back",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3A1F8D)
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabPill("My Load(0)", false)
                TabPill("Load(4)", false)
                TabPill("New load(33)", true)
            }

            Spacer(Modifier.height(20.dp))

            LoadListItem(
                title1 = "Bhiwari Ind. Area. Alwar, Alwar",
                title2 = "Malsian, Jalandhar",
                onClick = { navController.navigate("loadDetail") }
            )
        }
    }
}

@Composable
fun TabPill(text: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color.Black else Color.White,
        border = if (!selected) BorderStroke(1.dp, Color(0xFFDCDCDC)) else null
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (selected) Color.White else Color.Black
            )
        )
    }
}

@Composable
fun LoadListItem(title1: String, title2: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF2EBF84), CircleShape)
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    title1,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp).alpha(0.2f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFFF5A44), CircleShape)
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    title2,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

private const val TAG = "BottomNav"

private data class NavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavItem(route = "wheels_main", icon = Icons.Default.Home, label = "View Load"),
        NavItem(route = "empty_truck", icon = Icons.Default.LocalShipping, label = "खाली गाड़ी"),
        NavItem(route = "view_trips", icon = Icons.Default.Map, label = "Trip"),
        NavItem(route = "help", icon = Icons.Default.Help, label = "Help"),
        NavItem(route = "profile", icon = Icons.Default.Person, label = "Account")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Log.d("BottomNav", "Render BottomNav → currentRoute = $currentRoute")



    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    Log.d(TAG, "BottomNav click -> requested route = ${item.route}, currentRoute = $currentRoute")

                    if (currentRoute == item.route) {
                        // The user reselected the same item
                        Log.d(TAG, "BottomNav: item reselected (no navigation). route=${item.route}")
                        // Optionally you can pop to start or refresh; here we simply log.
                        return@NavigationBarItem
                    }

                    // Try to navigate; wrap with try-catch to log failures
                    try {
                        navController.navigate(item.route) {
                            // Pop up to the start to avoid large back stacks and restore state
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        Log.d(TAG, "BottomNav: navigate() called -> ${item.route}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "BottomNav: navigate FAILED for route=${item.route}", ex)
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadDetailScreen(navController: NavHostController) {

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2EBF84)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        "Enter your rate",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Route card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF2EBF84), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Bhiwari Ind. Area. Alwar, Alwar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowUp, null)
                    }

                    Divider(Modifier.padding(vertical = 12.dp).alpha(0.2f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFFF5A44), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Malsian, Jalandhar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Spacer(Modifier.height(20.dp))

            // Market Rates
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "View 5 Market Rates",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "View All",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFFC7904),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            repeat(3) {
                MarketRateItem()
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun InfoRow(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            painterResource(id = icon),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF3D3D3D)
            )
        )
    }
}

@Composable
fun MarketRateItem() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFC107))
                Spacer(Modifier.width(12.dp))
                Text("₹XX,000", fontWeight = FontWeight.Bold)
            }
            Text("Rating: NA/100", color = Color.Gray)
        }
    }
}


// =========================================================
// Theme / Typography
// =========================================================
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0B63FF),
    onPrimary = Color.White,
    secondary = Color(0xFF6B2EBE),
    background = Color(0xFFF6F7FB),
    surface = Color.White,
    onSurface = Color(0xFF111827)
)

@Composable
private fun SampleVendorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = TypographyDefaults(),
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(18.dp)
        ),
        content = content
    )
}

@Composable
private fun TypographyDefaults() = androidx.compose.material3.Typography(
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
    bodySmall = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
)

@Preview(showBackground = true)
@Composable
private fun PreviewMyTripsScreen() {
    val navController = rememberNavController()
    MyTripsScreen(navController)
}

