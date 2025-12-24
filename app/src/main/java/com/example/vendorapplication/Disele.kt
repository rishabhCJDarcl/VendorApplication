package com.example.vendorapplication

/**
 * Disele.kt - single-file Diesel/Fuel module (Compose + Material3)
 *
 * - Corrected: Vehicle data class & consistent property names
 * - Avoids calling @Composable functions in default params
 * - Replaces SmallTopAppBar with a small in-file AppBar to avoid Material3-version issues
 * - Mock sensor flow using channelFlow and StateFlows
 *
 * Drop into your project and rebuild.
 */

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

/* ----------------------------
   Models
   ---------------------------- */

///** Vehicle model - ensure names used consistently across the file */
//data class Vehicle(
//    val id: String,
//    val numberPlate: String,
//    val model: String,
//    val fuelCapacityLiters: Float
//)

data class SensorPayload(
    val vehicleId: String,
    val fuelLevelPercent: Float,
    val fuelLiters: Float,
    val consumptionLph: Float,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class Transaction(
    val id: String,
    val vehicleId: String,
    val date: Long,
    val liters: Float,
    val amount: Double,
    val cashback: Double,
    val stationName: String,
    val isHpclPartner: Boolean
)

data class AlertEvent(
    val id: String,
    val vehicleId: String,
    val type: String,
    val message: String,
    val timestamp: Long,
    val resolved: Boolean
)

/* ----------------------------
   ViewModel + mock flow
   ---------------------------- */

class DieselViewModel : ViewModel() {

    private val _vehicles = MutableStateFlow(
        listOf(
            Vehicle(
                id = "TRK-1001",
                title = "Truck",
                spec = "Ashok Leyland 3700",
                distanceInfo = 1200f,
                numberPlate = "UP32AB1234",
                model = "Ashok Leyland 3700",
                fuelCapacityLiters = 400f
            ),
            Vehicle(
                id = "TRK-1002",
                title = "Truck",
                spec = "Tata LP 1512",
                distanceInfo = 980f,
                numberPlate = "MH12XY9876",
                model = "Tata LP 1512",
                fuelCapacityLiters = 320f
            ),
            Vehicle(
                id = "TRK-1003",
                title = "Mini Truck",
                spec = "Eicher Pro 1049",
                distanceInfo = 600f,
                numberPlate = "DL1PC3333",
                model = "Eicher Pro 1049",
                fuelCapacityLiters = 200f
            )
        )
    )

    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _selectedVehicle = MutableStateFlow<Vehicle?>(_vehicles.value.firstOrNull())
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()

    private val _sensorData = MutableStateFlow<SensorPayload?>(null)
    val sensorData: StateFlow<SensorPayload?> = _sensorData.asStateFlow()

    private val sampleTransactions = listOf(
        Transaction("TXN-2001", "TRK-1001", System.currentTimeMillis() - 86400000L * 2, 120f, 9000.0, 45.0, "HPCL Station A", true),
        Transaction("TXN-2002", "TRK-1001", System.currentTimeMillis() - 86400000L * 10, 200f, 15000.0, 0.0, "Local Pump", false),
        Transaction("TXN-2003", "TRK-1002", System.currentTimeMillis() - 86400000L * 5, 80f, 6000.0, 24.0, "HPCL Express", true)
    )
    private val _transactions = MutableStateFlow(sampleTransactions)
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _alerts = MutableStateFlow(
        listOf(
            AlertEvent("A-1001", "TRK-1001", "theft", "Possible fuel theft detected — sudden drop", System.currentTimeMillis() - 3600000L * 3, false),
            AlertEvent("A-1002", "TRK-1002", "refuel", "Refuel detected at HPCL Station A", System.currentTimeMillis() - 7200000L, false)
        )
    )
    val alerts: StateFlow<List<AlertEvent>> = _alerts.asStateFlow()

    // Mock sensor flow: emits synthetic payloads periodically
    private val _mockSensorFlow = channelFlow<SensorPayload> {
        val baseMap = _vehicles.value.associateBy({ it.id }, {
            val initialPercent = Random.nextDouble(30.0, 85.0)
            SampleSensorState(
                percent = initialPercent.toFloat(),
                liters = (it.fuelCapacityLiters * initialPercent / 100f),
                consumptionLph = Random.nextDouble(4.0, 8.0).toFloat(),
                lat = 28.7041 + Random.nextDouble(-0.1, 0.1),
                lon = 77.1025 + Random.nextDouble(-0.1, 0.1)
            )
        }).toMutableMap()

        while (true) {
            val vehicle = _vehicles.value.random()
            val state = baseMap[vehicle.id] ?: continue
            val change = Random.nextDouble(-1.5, 1.0)
            state.percent = (state.percent + change).coerceIn(1.0, 99.9).toFloat()
            state.liters = ((vehicle.fuelCapacityLiters * (state.percent / 100f)).toDouble())
            state.lat += Random.nextDouble(-0.0005, 0.0005)
            state.lon += Random.nextDouble(-0.0005, 0.0005)
            delay(Random.nextLong(3000L, 6000L))
            send(
                SensorPayload(
                    vehicleId = vehicle.id,
                    fuelLevelPercent = state.percent,
                    fuelLiters = state.liters.toFloat(),
                    consumptionLph = state.consumptionLph,
                    latitude = state.lat,
                    longitude = state.lon,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }.buffer(64).conflate()

    init {
        viewModelScope.launch {
            _mockSensorFlow.collect { payload ->
                // For demo keep a single latest payload; production: map per vehicleId
                _sensorData.value = payload
            }
        }
    }

    fun selectVehicle(id: String) {
        val v = _vehicles.value.find { it.id == id }
        _selectedVehicle.value = v
        viewModelScope.launch {
            _sensorData.value = SensorPayload(
                vehicleId = id,
                fuelLevelPercent = Random.nextDouble(25.0, 95.0).toFloat(),
                fuelLiters = (v?.fuelCapacityLiters ?: 250f) * Random.nextDouble(0.25, 0.95).toFloat(),
                consumptionLph = Random.nextDouble(3.5, 8.5).toFloat(),
                latitude = 28.7041 + Random.nextDouble(-0.05, 0.05),
                longitude = 77.1025 + Random.nextDouble(-0.05, 0.05),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun acknowledgeAlert(id: String) {
        _alerts.value = _alerts.value.map { if (it.id == id) it.copy(resolved = true) else it }
    }

    fun pairSensorForVehicle(id: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            delay(1200L)
            val success = Random.nextInt(100) > 6
            onComplete(success)
            _alerts.value = (if (success) listOf(
                AlertEvent("A-Pair-${System.currentTimeMillis()}", id, "info", "Sensor paired for vehicle $id", System.currentTimeMillis(), false)
            ) else listOf(
                AlertEvent("A-PAIR-ERR-${System.currentTimeMillis()}", id, "error", "Failed to pair sensor for $id", System.currentTimeMillis(), false)
            )) + _alerts.value
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            delay(700L)
            _sensorData.value = SensorPayload(
                vehicleId = _selectedVehicle.value?.id ?: "TRK-1001",
                fuelLevelPercent = Random.nextDouble(10.0, 95.0).toFloat(),
                fuelLiters = (_selectedVehicle.value?.fuelCapacityLiters ?: 300f) * Random.nextDouble(0.05, 0.95).toFloat(),
                consumptionLph = Random.nextDouble(3.0, 8.0).toFloat(),
                latitude = 28.7041 + Random.nextDouble(-0.05, 0.05),
                longitude = 77.1025 + Random.nextDouble(-0.05, 0.05),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private data class SampleSensorState(var percent: Float, var liters: Double, var consumptionLph: Float, var lat: Double, var lon: Double)
}

/* ----------------------------
   Navigation
   ---------------------------- */

private object Routes {
    const val DASHBOARD = "dashboard"
    const val VEHICLES = "vehicles"
    const val SENSOR = "sensor/{vehicleId}"
    const val TRANSACTIONS = "transactions"
    const val ALERTS = "alerts"
    const val SETTINGS = "settings"
    fun sensor(vehicleId: String) = "sensor/$vehicleId"
}

/* ----------------------------
   Utilities
   ---------------------------- */

private fun relativeTime(epochMillis: Long): String {
    val diff = System.currentTimeMillis() - epochMillis
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> formatDate(epochMillis)
    }
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

private fun estimateRangeKm(liters: Float, avgKmPerLiter: Double): Double = liters * avgKmPerLiter

/* ----------------------------
   Simple AppBar (avoid SmallTopAppBar version mismatches)
   ---------------------------- */

@Composable
fun AppBar(title: String, onNavBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavBack != null) {
                IconButton(onClick = onNavBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row { actions() }
        }
    }
}

/* ----------------------------
   Top-level Screen & NavHost
   ---------------------------- */

@Composable
fun DiseleScreen(viewModelParam: DieselViewModel? = null) {
    // call viewModel() inside a Composable context
    val vm = viewModelParam ?: androidx.lifecycle.viewmodel.compose.viewModel<DieselViewModel>()
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
        NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
            composable(Routes.DASHBOARD) {
                DieselDashboardScreen(
                    viewModel = vm,
                    onOpenVehicles = { navController.navigate(Routes.VEHICLES) },
                    onOpenTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                    onOpenAlerts = { navController.navigate(Routes.ALERTS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onPairSensor = { vm.pairSensorForVehicle(vm.selectedVehicle.value?.id ?: "") { } },
                    onSelectVehicle = { vid -> navController.navigate(Routes.sensor(vid)) }
                )
            }

            composable(Routes.VEHICLES) {
                VehicleListScreen(
                    vehicles = vm.vehicles.collectAsState().value,
                    onVehicleSelected = { id ->
                        vm.selectVehicle(id)
                        navController.navigate(Routes.sensor(id))
                    },
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Routes.SENSOR, arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })) { bse ->
                val vehicleId = bse.arguments?.getString("vehicleId") ?: ""
                SensorDetailsScreen(vehicleId = vehicleId, viewModel = vm, onBack = { navController.navigateUp() })
            }

            composable(Routes.TRANSACTIONS) {
                TransactionsScreen(transactions = vm.transactions.collectAsState().value, onBack = { navController.navigateUp() })
            }

            composable(Routes.ALERTS) {
                AlertsScreen(alerts = vm.alerts.collectAsState().value, onAcknowledge = { id -> vm.acknowledgeAlert(id) }, onBack = { navController.navigateUp() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(vehicles = vm.vehicles.collectAsState().value, onPair = { id -> vm.pairSensorForVehicle(id) }, onBack = { navController.navigateUp() })
            }
        }
    }
}

/* ----------------------------
   Screens & Components
   ---------------------------- */

@Composable
fun DieselDashboardScreen(
    viewModel: DieselViewModel,
    onOpenVehicles: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenSettings: () -> Unit,
    onPairSensor: () -> Unit,
    onSelectVehicle: (String) -> Unit
) {
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val sensorData by viewModel.sensorData.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        AppBar(title = "Diesel — Wheelseye", actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        })
        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth > 700.dp
            if (isWide) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Crossfade(targetState = sensorData) { sd ->
                                if (sd != null) {
                                    FuelGaugeCard(selectedVehicle, sd, onRefresh = { viewModel.refreshNow() }, onSelectVehicle = onOpenVehicles)
                                } else {
                                    Box(modifier = Modifier.height(220.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        AlertChipsRow(alerts = alerts, onOpenAlerts = onOpenAlerts, onPairSensor = onPairSensor)
                        Spacer(modifier = Modifier.height(12.dp))
                        MapPreviewCard(sensorData = sensorData, onOpenVehicles = onOpenVehicles)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        LiveStatsCard(sensorData = sensorData)
                        Spacer(modifier = Modifier.height(12.dp))
                        TransactionsPreview(transactions = transactions, onOpenTransactions = onOpenTransactions)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Crossfade(targetState = sensorData) { sd ->
                            if (sd != null) {
                                FuelGaugeCard(selectedVehicle, sd, onRefresh = { viewModel.refreshNow() }, onSelectVehicle = onOpenVehicles)
                            } else {
                                Box(modifier = Modifier.height(220.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            }
                        }
                    }
                    AlertChipsRow(alerts = alerts, onOpenAlerts = onOpenAlerts, onPairSensor = onPairSensor)
                    LiveStatsCard(sensorData = sensorData)
                    MapPreviewCard(sensorData = sensorData, onOpenVehicles = onOpenVehicles)
                    TransactionsPreview(transactions = transactions, onOpenTransactions = onOpenTransactions)
                }
            }
        }
    }
}

@Composable
fun FuelGaugeCard(vehicle: Vehicle?, payload: SensorPayload, onRefresh: () -> Unit, onSelectVehicle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vehicle?.numberPlate ?: payload.vehicleId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = vehicle?.model ?: "Vehicle", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onSelectVehicle, modifier = Modifier.semantics { contentDescription = "Select Vehicle" }) {
                Icon(Icons.Default.DirectionsCar, contentDescription = "Select Vehicle")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                FuelGauge(percent = payload.fuelLevelPercent.coerceIn(0f, 100f), liters = payload.fuelLiters, capacity = vehicle?.fuelCapacityLiters ?: payload.fuelLiters, modifier = Modifier.size(220.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.widthIn(min = 140.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fuel: ${"%.1f".format(payload.fuelLiters)} L", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("Level: ${"%.0f".format(payload.fuelLevelPercent)} %", style = MaterialTheme.typography.bodyMedium)
                val estRange = estimateRangeKm(payload.fuelLiters, 6.0)
                Text("Est. Range: ${"%.0f".format(estRange)} km", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Button(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh")
                    }
                }
            }
        }
    }
}

@Composable
fun FuelGauge(percent: Float, liters: Float, capacity: Float, modifier: Modifier = Modifier) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    val sweep = (animatedPercent / 100f) * 180f

    // Get color scheme INSIDE the composable
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.08f
            val radius = size.minDimension / 2f - stroke
            val center = Offset(size.width / 2f, size.height / 2f)
            val left = center.x - radius
            val top = center.y - radius
            val rectSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)

            drawArc(
                color = colorScheme.onSurface.copy(alpha = 0.12f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(left, top),
                size = rectSize
            )

            val arcColor = if (percent < 15f) colorScheme.error else colorScheme.primary
            drawArc(
                color = arcColor,
                startAngle = 180f,
                sweepAngle = sweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(left, top),
                size = rectSize
            )

            val angleDegrees = 180f + sweep
            val angleRad = Math.toRadians(angleDegrees.toDouble())
            val needleLength = radius - stroke * 0.8f
            val nx = center.x + (needleLength * cos(angleRad)).toFloat()
            val ny = center.y + (needleLength * sin(angleRad)).toFloat()
            drawLine(
                color = colorScheme.onSurface,
                strokeWidth = stroke * 0.22f,
                start = center,
                end = Offset(nx, ny),
                cap = StrokeCap.Round
            )
            drawCircle(color = colorScheme.onSurface, radius = stroke * 0.28f, center = center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${percent.toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${"%.1f".format(liters)} L",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { contentDescription = "Liters remaining" }
            )
            Text(
                text = "Capacity: ${"%.0f".format(capacity)} L",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun AlertChipsRow(alerts: List<AlertEvent>, onOpenAlerts: () -> Unit, onPairSensor: () -> Unit) {
    val scroll = rememberScrollState()
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ElevatedFilterChip(selected = false, onClick = onPairSensor, leadingIcon = { Icon(Icons.Default.Link, contentDescription = "Pair Sensor") }, label = { Text("Pair Sensor") }, shape = RoundedCornerShape(20.dp))
        ElevatedFilterChip(selected = false, onClick = onOpenAlerts, leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scan") }, label = { Text("Scan HPCL") }, shape = RoundedCornerShape(20.dp))
        ElevatedFilterChip(selected = false, onClick = onOpenAlerts, leadingIcon = { Icon(Icons.Default.Report, contentDescription = "Report") }, label = { Text("Report Theft") }, shape = RoundedCornerShape(20.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colorScheme.error))

        alerts.take(6).forEach { alert ->
            val pulse = rememberInfiniteTransition()
            val scale by pulse.animateFloat(initialValue = 1f, targetValue = if (!alert.resolved && alert.type != "info") 1.06f else 1f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(animation = tween(800), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse))
            Card(modifier = Modifier.scale(scale).clickable { onOpenAlerts() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (!alert.resolved && alert.type != "info") colorScheme.errorContainer else colorScheme.surface)) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (alert.type == "theft") Icons.Default.Warning else Icons.Default.Info, contentDescription = alert.type, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = alert.message, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        Text(text = relativeTime(alert.timestamp), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveStatsCard(sensorData: SensorPayload?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Live Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(label = "Inst. Consumption", value = sensorData?.consumptionLph?.let { "%.1f L/hr".format(it) } ?: "--")
                StatTile(label = "Avg Mileage", value = "6.0 km/L")
                StatTile(label = "Last Refill", value = sensorData?.timestamp?.let { relativeTime(it - 86400000L) } ?: "—")
            }
        }
    }
}

@Composable
fun StatTile(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MapPreviewCard(sensorData: SensorPayload?, onOpenVehicles: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onOpenVehicles() }, shape = RoundedCornerShape(12.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Map, contentDescription = "Map preview", modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Map preview (mock)", style = MaterialTheme.typography.bodyMedium)
                Text(text = sensorData?.let { "Lat: ${"%.4f".format(it.latitude)}, Lon: ${"%.4f".format(it.longitude)}" } ?: "No GPS", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text("(Tap to open full vehicle list)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun TransactionsPreview(transactions: List<Transaction>, onOpenTransactions: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenTransactions) { Text("View all") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val recent = transactions.take(3)
            recent.forEach { tx ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${tx.stationName} • ${"%.0f".format(tx.liters)} L", style = MaterialTheme.typography.bodyMedium)
                        Text("${formatDate(tx.date)} • Cashback: ₹${"%.1f".format(tx.cashback)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                    if (tx.isHpclPartner) Icon(Icons.Default.Star, contentDescription = "HPCL partner")
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

/* ----------------------------
   Vehicle list / details / other screens
   ---------------------------- */

@Composable
fun VehicleListScreen(vehicles: List<Vehicle>, onVehicleSelected: (String) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        AppBar(title = "Vehicles", onNavBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vehicles, key = { it.id }) { v ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onVehicleSelected(v.id) }, shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = "Vehicle", modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(v.numberPlate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(v.model, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${v.fuelCapacityLiters.toInt()} L", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun SensorDetailsScreen(vehicleId: String, viewModel: DieselViewModel, onBack: () -> Unit) {
    val vehicle = viewModel.vehicles.collectAsState().value.find { it.id == vehicleId }
    val sensor = viewModel.sensorData.collectAsState().value
    val alerts = viewModel.alerts.collectAsState().value.filter { it.vehicleId == vehicleId }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        AppBar(title = "Sensor Details", onNavBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(vehicle?.numberPlate ?: vehicleId, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(vehicle?.model ?: "", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                if (sensor != null && sensor.vehicleId == vehicleId) {
                    FuelGauge(percent = sensor.fuelLevelPercent, liters = sensor.fuelLiters, capacity = vehicle?.fuelCapacityLiters ?: sensor.fuelLiters)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Consumption: ${"%.2f".format(sensor.consumptionLph)} L/hr", style = MaterialTheme.typography.bodyMedium)
                    Text("Last update: ${relativeTime(sensor.timestamp)}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.refreshNow() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh sensor")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh")
                        }
                        OutlinedButton(onClick = { viewModel.pairSensorForVehicle(vehicleId) }) {
                            Icon(Icons.Default.Link, contentDescription = "Pair sensor")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pair Sensor")
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text("No sensor data available for this vehicle", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (alerts.isNotEmpty()) {
            Text("Alerts", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxHeight()) {
                items(alerts) { a ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(a.message, style = MaterialTheme.typography.bodyMedium)
                                Text(relativeTime(a.timestamp), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.acknowledgeAlert(a.id) }) {
                                Icon(Icons.Default.Check, contentDescription = "Acknowledge")
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(12.dp))
            Text("No alerts for this vehicle.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TransactionsScreen(transactions: List<Transaction>, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        AppBar(title = "Transactions", onNavBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions, key = { it.id }) { tx ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.stationName, style = MaterialTheme.typography.titleMedium)
                            Text(formatDate(tx.date), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${"%.2f".format(tx.amount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Cashback ₹${"%.1f".format(tx.cashback)}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            if (tx.isHpclPartner) Text("HPCL Partner", style = MaterialTheme.typography.labelSmall, color = colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsScreen(alerts: List<AlertEvent>, onAcknowledge: (String) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        AppBar(title = "Alerts & Events", onNavBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(alerts.sortedByDescending { it.timestamp }, key = { it.id }) { a ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.type.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelLarge, color = colorScheme.onSurfaceVariant)
                            Text(a.message, style = MaterialTheme.typography.bodyMedium)
                            Text(relativeTime(a.timestamp), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                        if (!a.resolved) Button(onClick = { onAcknowledge(a.id) }) { Text("Acknowledge") } else Text("Resolved", color = colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vehicles: List<Vehicle>, onPair: (String) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        AppBar(title = "Settings", onNavBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Sensor Pairing", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                vehicles.forEach { v ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(v.numberPlate)
                            Text(v.model, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { onPair(v.id) }) {
                            Icon(Icons.Default.Link, contentDescription = "Pair")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pair")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Refresh Rate", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sensor data refresh is simulated. In production choose a sensible polling/streaming interval (e.g., 2-10s).", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/* ----------------------------
   Previews
   ---------------------------- */

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Diesel Dashboard (Dark)")
@Composable
fun PreviewDieselDashboardDark() {
    val payload = SensorPayload("TRK-1001", 58.5f, 230f, 5.3f, 28.7041, 77.1025, System.currentTimeMillis())
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFFFFA000), secondary = androidx.compose.ui.graphics.Color(0xFF00BFA5), background = androidx.compose.ui.graphics.Color(0xFF0F1722), surface = androidx.compose.ui.graphics.Color(0xFF111827), onSurface = androidx.compose.ui.graphics.Color.White)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(12.dp)) {
                FuelGaugeCard(
                    vehicle = Vehicle(
                        id = "TRK-1001",
                        title = "Truck 1001",
                        spec = "Demo Spec",
                        distanceInfo = 400f,
                        numberPlate = "UP32AB1234",
                        model = "Demo Model",
                        fuelCapacityLiters = 400f
                    ),
                    payload = payload,
                    onRefresh = {},
                    onSelectVehicle = {}
                )

            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Fuel Gauge Preview")
@Composable
fun PreviewFuelGauge() {
    val payload = SensorPayload("TRK-1001", 76.0f, 310f, 4.8f, 28.7041, 77.1025, System.currentTimeMillis())
    MaterialTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                FuelGauge(percent = payload.fuelLevelPercent, liters = payload.fuelLiters, capacity = 400f, modifier = Modifier.size(260.dp))
            }
        }
    }
}
