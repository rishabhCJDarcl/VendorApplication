
package com.example.vendorapplication

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/* ---- Colors and small design tokens ---- */
private val BackgroundGray = Color(0xFFF6F6F9)
private val CardBorderPurple = Color(0xFFECEBFF)
private val LightPurpleBg = Color(0xFFF4EEFF)

private val RedSquare = Color(0xFFDE3B2C)
private val InfoBeige = Color(0xFFF7E9CA)
private val TimerBg = Color(0xFFFFE09B)
private val InfoTextGray = Color(0xFF6B6B6B)
private val SubmitGreen = Color(0xFFBFEAD0)
private val SubmitText = Color(0xFF7A7A7A)
private val CardInnerBg = Color(0xFFFFFFFF)
private val SearchFieldBorder = Color(0xFFE8E8F1)


/* ---- Models ---- */
data class VehicleItem(
    val id: String,
    val type: String,
    val lengthFt: Double,
    val capacityTon: Double,
    val distanceKm: Int
)



/* ---- ViewModel: fetches, parses, logs loads ---- */
class LoadsViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loads by mutableStateOf<List<LoadItem>>(emptyList())
        private set

    var lastOrigin by mutableStateOf<String?>(null)
        private set
    var lastDestination by mutableStateOf<String?>(null)
        private set

    fun fetchLoads(authToken: String, origin: String, destination: String) {
        if (origin.isBlank() || destination.isBlank()) {
            errorMessage = "Origin or destination missing"
            loads = emptyList()
            return
        }

        lastOrigin = origin
        lastDestination = destination

        isLoading = true
        errorMessage = null
        loads = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val urlStr = buildOrdersApi(origin, destination)
                Log.d("LoadsFetch", "REQUEST -> URL=$urlStr")
                val url = URL(urlStr)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                    if (authToken.isNotBlank()) {
                        // Example header — replace with whatever your Centrum API expects:
                        setRequestProperty("Authorization", "Bearer $authToken")
                        setRequestProperty("Centrum-API-KEY", "a9jTk7Vguygrf545gfygyDpUgEFWzhAnRYcpkGXPv")
                    } else {
                        Log.w("LoadsFetch", "Auth token is blank; request may fail")
                    }
                }

                val code = conn.responseCode
                val inputStream = if (code in 200..299) conn.inputStream else conn.errorStream
                val reader = BufferedReader(InputStreamReader(inputStream ?: conn.inputStream))
                val sb = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = reader.readLine()
                }
                reader.close()
                val respStr = sb.toString()
                Log.d("LoadsFetch", "HTTP $code response length=${respStr.length}")

                if (code !in 200..299) {
                    errorMessage = "API error: HTTP $code"
                    isLoading = false
                    Log.e("LoadsFetch", "API failure: HTTP $code - $respStr")
                    return@launch
                }

                Log.d("LoadsFetch", "Raw JSON response: ${respStr.take(1000)}") // truncated log if long
                val parsed = parseLoadsFromJson(respStr)

                // Log parsed results for debugging
                Log.d("LoadsFetch", "Parsed loads count = ${parsed.size}")
                parsed.take(10).forEachIndexed { idx, it ->
                    Log.d("LoadsFetch", "Parsed[$idx] from='${it.from}' to='${it.to}' partyRate='${it.partyRate}' material='${it.material}' maxLoadMT='${it.maxLoadMT}' distanceInfo='${it.distanceInfo}'")
                }

                // Filter: show only loads where partyRate is null/empty/"NA"/"0" (meaning not calculated)
                val shown = mutableListOf<LoadItem>()
                for (li in parsed) {
                    val raw = li.partyRate?.trim()
                    val show = shouldShowLoadBasedOnPartyRate(raw)
                    val lid = "${li.from}_${li.to}_${li.partyRate ?: "NA"}"

                    if (show) {
                        Log.d("PartyRateCheck", "LoadId=$lid PartyRate=${raw ?: "MISSING"} → SHOWN")
                        shown.add(li)
                    } else {
                        Log.d("PartyRateCheck", "LoadId=$lid PartyRate=${raw ?: "MISSING"} → SKIPPED (calculated)")
                    }
                }

                // If you want to temporarily disable the partyRate filter for debugging, uncomment:
                // loads = parsed

                loads = shown
                if (shown.isEmpty()) {
                    Log.d("LoadsFetch", "No loads to show after partyRate filter (parsedCount=${parsed.size})")
                } else {
                    Log.d("LoadsFetch", "Shown loads count=${shown.size}")
                }
                isLoading = false
            } catch (t: Throwable) {
                isLoading = false
                errorMessage = "Failed to fetch loads: ${t.message}"
                Log.e("LoadsFetch", "Exception while fetching loads", t)
            }
        }
    }

    private fun shouldShowLoadBasedOnPartyRate(partyRateRaw: String?): Boolean {
        if (partyRateRaw == null) return true
        val normalized = partyRateRaw.trim()
        if (normalized.isEmpty()) return true
        if (normalized.equals("NA", ignoreCase = true)) return true
        return try {
            val d = normalized.replace(",", "").toDouble()
            d == 0.0
        } catch (ex: Exception) {
            false
        }
    }

    private fun parseLoadsFromJson(jsonStr: String): List<LoadItem> {
        val out = mutableListOf<LoadItem>()
        try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    parseLoadObject(obj)?.let { out.add(it) }
                }
            } else {
                val root = JSONObject(trimmed)
                val candidateKeys = listOf("orders", "data", "results", "hits", "items")
                var arr: JSONArray? = null
                for (k in candidateKeys) {
                    if (root.has(k)) {
                        val value = root.opt(k)
                        if (value is JSONArray) {
                            arr = value
                            break
                        } else if (value is JSONObject) {
                            arr = JSONArray().put(value)
                            break
                        }
                    }
                }
                if (arr == null) {
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = root.opt(k)
                        if (v is JSONArray) {
                            arr = v
                            break
                        }
                    }
                }
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        parseLoadObject(obj)?.let { out.add(it) }
                    }
                } else {
                    parseLoadObject(root)?.let { out.add(it) }
                }
            }
        } catch (t: Throwable) {
            Log.e("ParseLoads", "JSON parse error: ${t.message}", t)
        }
        return out
    }

    private fun parseLoadObject(obj: JSONObject): LoadItem? {
        var partyRate: String? = null
        if (obj.has("partyRate")) partyRate = obj.optString("partyRate", null)
        if (partyRate == null && obj.has("rate")) partyRate = obj.optString("rate", null)
        if (partyRate == null && obj.has("charges")) {
            val charges = obj.optJSONObject("charges")
            partyRate = charges?.optString("partyRate", null)
        }

        val from = obj.optString("origin", null) ?: obj.optString("from", null) ?: ""
        val fromPinState = obj.optString("fromPinState", null) ?: obj.optString("originPin", null)
        val to = obj.optString("destination", null) ?: obj.optString("to", null) ?: ""
        val toPinState = obj.optString("toPinState", null) ?: obj.optString("destinationPin", null)

        val material = obj.optString("material", null)
        val hauler = obj.optString("hauler", null)
        val vehicleCategory = obj.optString("vehicleCategory", null)

        var maxLoadMT: Float? = null
        if (obj.has("maxLoadMT")) {
            val d = obj.optDouble("maxLoadMT", Double.NaN)
            if (!d.isNaN()) maxLoadMT = d.toFloat()
        } else if (obj.has("maxLoad")) {
            val d = obj.optDouble("maxLoad", Double.NaN)
            if (!d.isNaN()) maxLoadMT = d.toFloat()
        } else if (obj.has("capacityTon")) {
            val d = obj.optDouble("capacityTon", Double.NaN)
            if (!d.isNaN()) maxLoadMT = d.toFloat()
        }

        var wheels: Int? = null
        if (obj.has("wheels")) {
            val w = obj.optInt("wheels", -1)
            if (w >= 0) wheels = w
        }

        val distanceInfo = obj.optString("distanceInfo", null) ?: obj.optString("distance", null)

        return LoadItem(
            from = from,
            fromPinState = fromPinState,
            to = to,
            toPinState = toPinState,
            containerInfo = obj.optString("containerInfo", null),
            loadingTime = obj.optString("loadingTime", null),
            distanceInfo = distanceInfo,
            partyRate = partyRate,
            material = material,
            hauler = hauler,
            maxLoadMT = maxLoadMT,
            wheels = wheels,
            vehicleCategory = vehicleCategory
        )
    }

    private fun buildOrdersApi(origin: String, destination: String): String {
        // Replace with your real endpoint
        val base = "https://api.example.com/orders"
        val qOrigin = java.net.URLEncoder.encode(origin, "utf-8")
        val qDest = java.net.URLEncoder.encode(destination, "utf-8")
        return "$base?origin=$qOrigin&destination=$qDest"
    }
}

/* ---- Navigation helper ----
   Use this from the LoadList when a card is clicked.
   It writes origin/to into the currentBackStackEntry.savedStateHandle and navigates to 'kyc'.
*/
fun navigateToKycOrUpdate(navController: NavHostController, from: String, to: String) {
    val targetRoute = "kyc"

    // Set origin/destination on the currentBackStackEntry so the destination can pick them up.
    navController.currentBackStackEntry?.savedStateHandle?.set("from", from)
    navController.currentBackStackEntry?.savedStateHandle?.set("to", to)

    // Also set a refreshKey to force re-fetch if already on the route
    navController.currentBackStackEntry?.savedStateHandle?.set("refreshKey", UUID.randomUUID().toString())

    val currentRoute = navController.currentDestination?.route
    Log.d("LOAD_CARD_NAV", "navigateToKycOrUpdate: currentRoute=$currentRoute targetRoute=$targetRoute from=$from to=$to")
    if (currentRoute != targetRoute) {
        navController.navigate(targetRoute)
    } else {
        Log.d("LOAD_CARD_NAV", "Already on kyc route — updated savedStateHandle for current entry.")
    }
}

/* ---- Main App entry composable: NavHost with LoadList and VehicleKycScreen ---- */
@Composable
fun MainApp(authToken: String = "") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "load_list") {
        composable("load_list") {
            LoadListScreen(navController = navController)
        }
        composable("kyc") {
            // Pass authToken here
            VehicleKycScreen(navController = navController, authToken = authToken, onBack = {
                navController.popBackStack()
            })
        }
    }
}

/* ---- Sample LoadList screen with clickable cards that navigate to VehicleKycScreen ---- */


/* ---- VehicleKycScreen (reads origin/destination reactively and logs everything) ---- */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun VehicleKycScreen(
//    navController: NavHostController,
//    authToken: String,
//    onBack: () -> Unit = {},
//    viewModel: LoadsViewModel = viewModel()
//) {
//    BackHandler { onBack() }
//
//    // Reactive read of savedStateHandle from the current back stack entry
//    val savedState = navController.currentBackStackEntry?.savedStateHandle
//
//    val originStateFlow: StateFlow<String?>? = savedState?.getStateFlow("from", null as String?)
//    val destinationStateFlow: StateFlow<String?>? = savedState?.getStateFlow("to", null as String?)
//    val refreshKeyStateFlow: StateFlow<String?>? = savedState?.getStateFlow("refreshKey", null as String?)
//
//    val origin by (originStateFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
//    val destination by (destinationStateFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
//    val refreshKey by (refreshKeyStateFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
//
//    LaunchedEffect(origin, destination, refreshKey) {
//        val o = origin
//        val d = destination
//        Log.d("VehicleKycScreen", "Observed origin=$o destination=$d refresh=$refreshKey")
//        if (!o.isNullOrBlank() && !d.isNullOrBlank()) {
//            // Clear previous errors (optional)
//            viewModel.fetchLoads(authToken = authToken, origin = o, destination = d)
//        } else {
//            Log.d("VehicleKycScreen", "Origin or Destination missing - waiting")
//        }
//    }
//
//    // UI state
//    var search by remember { mutableStateOf("") }
//    var selectedId by remember { mutableStateOf<String?>(null) }
//
//    Scaffold(topBar = {
//        TopAppBar(
//            title = {},
//            navigationIcon = {
//                IconButton(onClick = onBack) {
//                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(64.dp)
//        )
//    }, containerColor = BackgroundGray) { innerPadding ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//                    .padding(horizontal = 16.dp),
//                contentPadding = PaddingValues(bottom = 110.dp, top = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                item {
//                    RouteCardDynamic(origin = origin, destination = destination)
//                    Spacer(modifier = Modifier.height(20.dp))
//                    Text(
//                        text = "Select Vehicle",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 18.sp
//                        )
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    OutlinedTextField(
//                        value = search,
//                        onValueChange = { search = it },
//                        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search") },
//                        placeholder = { Text(text = "Search vehicle", color = InfoTextGray) },
//                        singleLine = true,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp)
//                            .border(BorderStroke(1.dp, SearchFieldBorder), shape = RoundedCornerShape(12.dp))
//                            .background(Color.White, shape = RoundedCornerShape(12.dp)),
//                        colors = TextFieldDefaults.colors(
//                            focusedContainerColor = Color.Transparent,
//                            unfocusedContainerColor = Color.Transparent,
//                            disabledContainerColor = Color.Transparent,
//                            errorContainerColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            disabledIndicatorColor = Color.Transparent,
//                            errorIndicatorColor = Color.Transparent
//                        ),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                }
//
//                if (viewModel.isLoading) {
//                    item {
//                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
//                            CircularProgressIndicator()
//                        }
//                    }
//                } else if (!viewModel.errorMessage.isNullOrBlank()) {
//                    item {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = CardDefaults.cardColors(containerColor = Color.White),
//                            border = BorderStroke(1.dp, Color(0xFFF1F1F6))
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                Text(text = "Failed to load data", fontWeight = FontWeight.Bold)
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Text(text = viewModel.errorMessage ?: "Unknown error")
//                            }
//                        }
//                    }
//                } else {
//                    val mappedVehicles = viewModel.loads.map { load ->
//                        val id = "${load.from}_${load.to}_${load.material ?: "unk"}"
//                        val type = load.vehicleCategory ?: load.material ?: "Unknown"
//                        val length = (load.maxLoadMT?.toDouble() ?: 0.0)
//                        val capacity = (load.maxLoadMT?.toDouble() ?: 0.0)
//                        val dist = parseDistanceKm(load.distanceInfo)
//                        VehicleItem(id = id, type = type, lengthFt = length, capacityTon = capacity, distanceKm = dist)
//                    }
//
//                    if (mappedVehicles.isEmpty()) {
//                        item {
//                            Card(
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(12.dp),
//                                colors = CardDefaults.cardColors(containerColor = Color.White),
//                                border = BorderStroke(1.dp, Color(0xFFF1F1F6))
//                            ) {
//                                Column(modifier = Modifier.padding(16.dp)) {
//                                    Text(text = "No loads available", fontWeight = FontWeight.Bold)
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                    Text(text = "No loads without party rate were returned by the API.")
//                                }
//                            }
//                        }
//                    } else {
//                        val filtered = if (search.isBlank()) mappedVehicles else mappedVehicles.filter {
//                            it.id.contains(search, ignoreCase = true)
//                        }
//                        items(filtered) { vehicle ->
//                            VehicleCard(vehicle = vehicle, selected = vehicle.id == selectedId, onSelect = { selectedId = vehicle.id })
//                        }
//                    }
//                }
//            }
//
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Button(
//                    onClick = { /* submit */ },
//                    enabled = selectedId != null,
//                    modifier = Modifier.fillMaxWidth().height(56.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = SubmitGreen,
//                        disabledContainerColor = SubmitGreen.copy(alpha = 0.6f),
//                        contentColor = SubmitText
//                    ),
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Text(text = "Submit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
//                }
//            }
//        }
//    }
//}
private const val TAG = "VehicleKycScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleKycScreen(
    navController: NavHostController,
    authToken: String,
    onBack: () -> Unit = {},
    viewModel: LoadsViewModel = viewModel()
) {
    // Keep back handling from original
    BackHandler { onBack() }

    // ----- Robust reader that searches multiple locations (and logs where value was found) -----
    fun readRawSingle(key: String): String? {
        val currEntry = navController.currentBackStackEntry
        val prevEntry = navController.previousBackStackEntry

        // 1) previous savedStateHandle (common pattern when previous screen does savedStateHandle.set)
        try {
            prevEntry?.savedStateHandle?.get<String>(key)?.let { if (!it.isNullOrBlank()) {
                Log.d(TAG, "readRaw: key='$key' found in previousEntry.savedStateHandle (route=${prevEntry.destination.route})")
                return it
            } }
        } catch (_: Exception) { /* ignore type issues */ }

        // 2) current arguments (nav args)
        try {
            currEntry?.arguments?.getString(key)?.let { if (!it.isNullOrBlank()) {
                Log.d(TAG, "readRaw: key='$key' found in currentEntry.arguments (route=${currEntry.destination.route})")
                return it
            } }
        } catch (_: Exception) { /* ignore */ }

        // 3) current savedStateHandle
        try {
            currEntry?.savedStateHandle?.get<String>(key)?.let { if (!it.isNullOrBlank()) {
                Log.d(TAG, "readRaw: key='$key' found in currentEntry.savedStateHandle (route=${currEntry.destination.route})")
                return it
            } }
        } catch (_: Exception) { /* ignore */ }

        // 4) Search whole back stack (most robust) - search from newest to oldest
//        try {
//            navController.backQueue.asReversed().forEach { entry ->
//                // savedStateHandle search
//                try {
//                    entry.savedStateHandle.get<String>(key)?.let { if (!it.isNullOrBlank()) {
//                        Log.d(TAG, "readRaw: key='$key' found in backQueue.savedStateHandle (route=${entry.destination.route})")
//                        return it
//                    } }
//                } catch (_: Exception) {}
//
//                // arguments search
//                try {
//                    entry.arguments?.getString(key)?.let { if (!it.isNullOrBlank()) {
//                        Log.d(TAG, "readRaw: key='$key' found in backQueue.arguments (route=${entry.destination.route})")
//                        return it
//                    } }
//                } catch (_: Exception) {}
//            }
//        } catch (_: Exception) {}


        return null
    }

    // Accept aliases for keys (example: some callers set "rate" while screen expects "partyRate")
    fun readRaw(vararg keys: String): String? {
        val prev = navController.previousBackStackEntry
        val curr = navController.currentBackStackEntry

        keys.forEach { key ->

            // 1) previous savedStateHandle (best when set before navigate)
            try {
                val v = prev?.savedStateHandle?.get<String>(key)
                if (!v.isNullOrBlank()) {
                    Log.d(TAG, "readRaw: '$key' from previous.savedStateHandle = $v")
                    return v
                }
            } catch (_: Exception) {}

            // 2) current savedStateHandle
            try {
                val v = curr?.savedStateHandle?.get<String>(key)
                if (!v.isNullOrBlank()) {
                    Log.d(TAG, "readRaw: '$key' from current.savedStateHandle = $v")
                    return v
                }
            } catch (_: Exception) {}

            // 3) current arguments (nav args)
            try {
                val v = curr?.arguments?.getString(key)
                if (!v.isNullOrBlank()) {
                    Log.d(TAG, "readRaw: '$key' from current.arguments = $v")
                    return v
                }
            } catch (_: Exception) {}
        }

        return null
    }


    // Read all raw values (attempt several alias keys where appropriate)
    val rawFrom = readRaw("from", "origin", "originName")
    val rawFromPinState = readRaw("fromPinState", "from_pin_state", "fromPin")
    val rawTo = readRaw("to", "destination", "destinationName")
    val rawToPinState = readRaw("toPinState", "to_pin_state", "toPin")
    val rawContainerInfo = readRaw("containerInfo", "container_info")
    val rawLoadingTime = readRaw("loadingTime", "loading_time", "time")
    val rawDistanceInfo = readRaw("distanceInfo", "distance", "dist")
    val rawPartyRate = readRaw("partyRate", "rate", "party_rate")
    val rawMaterial = readRaw("material", "goods", "shipmentMaterial")
    val rawHauler = readRaw("hauler", "vehicle", "vehicleCategory")
    val rawMaxLoadMT = readRaw("maxLoadMT", "maxLoad", "max_load_mt")
    val rawWheels = readRaw("wheels", "wheelCount", "noOfWheels")

    // For collecting missing / parse errors to include in final summary
    val missingOrErrors = mutableListOf<String>()

    // For each field, attempt parse / presence check and emit exactly one log entry (keeps your original approach)
    val fromDisplay: String = if (!rawFrom.isNullOrBlank()) {
        Log.d(TAG, "from = ${rawFrom} — SUCCESS")
        rawFrom
    } else {
        Log.d(TAG, "from = '' — EMPTY (key missing or null)")
        missingOrErrors.add("from (EMPTY)")
        "-"
    }

    val fromPinStateDisplay: String = if (!rawFromPinState.isNullOrBlank()) {
        Log.d(TAG, "fromPinState = ${rawFromPinState} — SUCCESS")
        rawFromPinState
    } else {
        Log.d(TAG, "fromPinState = '' — EMPTY (key missing or null)")
        missingOrErrors.add("fromPinState (EMPTY)")
        "-"
    }

    val toDisplay: String = if (!rawTo.isNullOrBlank()) {
        Log.d(TAG, "to = ${rawTo} — SUCCESS")
        rawTo
    } else {
        Log.d(TAG, "to = '' — EMPTY (key missing or null)")
        missingOrErrors.add("to (EMPTY)")
        "-"
    }

    val toPinStateDisplay: String = if (!rawToPinState.isNullOrBlank()) {
        Log.d(TAG, "toPinState = ${rawToPinState} — SUCCESS")
        rawToPinState
    } else {
        Log.d(TAG, "toPinState = '' — EMPTY (key missing or null)")
        missingOrErrors.add("toPinState (EMPTY)")
        "-"
    }

    val containerInfoDisplay: String = if (!rawContainerInfo.isNullOrBlank()) {
        Log.d(TAG, "containerInfo = ${rawContainerInfo} — SUCCESS")
        rawContainerInfo
    } else {
        Log.d(TAG, "containerInfo = '' — EMPTY (key missing or null)")
        missingOrErrors.add("containerInfo (EMPTY)")
        "-"
    }

    val loadingTimeDisplay: String = if (!rawLoadingTime.isNullOrBlank()) {
        Log.d(TAG, "loadingTime = ${rawLoadingTime} — SUCCESS")
        rawLoadingTime
    } else {
        Log.d(TAG, "loadingTime = '' — EMPTY (key missing or null)")
        missingOrErrors.add("loadingTime (EMPTY)")
        "-"
    }

    val distanceInfoDisplay: String = if (!rawDistanceInfo.isNullOrBlank()) {
        Log.d(TAG, "distanceInfo = ${rawDistanceInfo} — SUCCESS")
        rawDistanceInfo
    } else {
        Log.d(TAG, "distanceInfo = '' — EMPTY (key missing or null)")
        missingOrErrors.add("distanceInfo (EMPTY)")
        "-"
    }

    // partyRate: try parse numeric (Float) but keep original string as display if non-numeric
    var partyRateParsed: Float? = null
    val partyRateDisplay: String = if (!rawPartyRate.isNullOrBlank()) {
        try {
            val cleaned = rawPartyRate.replace("[^0-9.+-]".toRegex(), "").ifBlank { rawPartyRate }
            try {
                partyRateParsed = cleaned.toFloat()
                Log.d(TAG, "partyRate = '${rawPartyRate}' — SUCCESS (parsed=${partyRateParsed})")
            } catch (nf: Exception) {
                Log.d(TAG, "partyRate = '${rawPartyRate}' — SUCCESS (non-numeric string, raw kept)")
            }
            rawPartyRate
        } catch (e: Exception) {
            Log.d(TAG, "partyRate = '${rawPartyRate}' — PARSE_ERROR (${e::class.simpleName}: ${e.message})")
            missingOrErrors.add("partyRate (PARSE_ERROR)")
            "-"
        }
    } else {
        Log.d(TAG, "partyRate = '' — EMPTY (key missing or null)")
        missingOrErrors.add("partyRate (EMPTY)")
        "-"
    }

    val materialDisplay: String = if (!rawMaterial.isNullOrBlank()) {
        Log.d(TAG, "material = ${rawMaterial} — SUCCESS")
        rawMaterial
    } else {
        Log.d(TAG, "material = '' — EMPTY (key missing or null)")
        missingOrErrors.add("material (EMPTY)")
        "-"
    }

    val haulerDisplay: String = if (!rawHauler.isNullOrBlank()) {
        Log.d(TAG, "hauler = ${rawHauler} — SUCCESS")
        rawHauler
    } else {
        Log.d(TAG, "hauler = '' — EMPTY (key missing or null)")
        missingOrErrors.add("hauler (EMPTY)")
        "-"
    }

    // maxLoadMT: expected Float
    var maxLoadMTParsed: Float? = null
    val maxLoadMTDisplay: String = if (!rawMaxLoadMT.isNullOrBlank()) {
        try {
            val cleaned = rawMaxLoadMT.replace("[^0-9.+-]".toRegex(), "")
            maxLoadMTParsed = cleaned.toFloat()
            Log.d(TAG, "maxLoadMT = ${maxLoadMTParsed} — SUCCESS")
            String.format("%.2f", maxLoadMTParsed)
        } catch (e: Exception) {
            val fallback = rawMaxLoadMT.toFloatOrNull()
            if (fallback != null) {
                maxLoadMTParsed = fallback
                Log.d(TAG, "maxLoadMT = ${maxLoadMTParsed} — SUCCESS (fallback parse)")
                String.format("%.2f", maxLoadMTParsed)
            } else {
                Log.d(TAG, "maxLoadMT = '${rawMaxLoadMT}' — PARSE_ERROR (${e::class.simpleName}: ${e.message})")
                missingOrErrors.add("maxLoadMT (PARSE_ERROR)")
                "-"
            }
        }
    } else {
        Log.d(TAG, "maxLoadMT = '' — EMPTY (key missing or null)")
        missingOrErrors.add("maxLoadMT (EMPTY)")
        "-"
    }

    // wheels: expected Int
    var wheelsParsed: Int? = null
    val wheelsDisplay: String = if (!rawWheels.isNullOrBlank()) {
        try {
            val cleaned = rawWheels.replace("[^0-9+-]".toRegex(), "")
            wheelsParsed = cleaned.toInt()
            Log.d(TAG, "wheels = ${wheelsParsed} — SUCCESS")
            wheelsParsed.toString()
        } catch (e: Exception) {
            val fb = rawWheels.toIntOrNull()
            if (fb != null) {
                wheelsParsed = fb
                Log.d(TAG, "wheels = ${wheelsParsed} — SUCCESS (fallback parse)")
                wheelsParsed.toString()
            } else {
                Log.d(TAG, "wheels = '${rawWheels}' — PARSE_ERROR (${e::class.simpleName}: ${e.message})")
                missingOrErrors.add("wheels (PARSE_ERROR)")
                "-"
            }
        }
    } else {
        Log.d(TAG, "wheels = '' — EMPTY (key missing or null)")
        missingOrErrors.add("wheels (EMPTY)")
        "-"
    }

    // Final summary log: if any missingOrErrors then MISSING_FIELDS else ALL_FIELDS_OK
    if (missingOrErrors.isEmpty()) {
        Log.d(TAG, "Summary: ALL_FIELDS_OK")
    } else {
        Log.d(TAG, "Summary: MISSING_FIELDS => ${missingOrErrors.joinToString(", ")}")
    }

    // Reactive read of savedStateHandle for origin/destination so previous fetch logic still works
    val savedState = navController.currentBackStackEntry?.savedStateHandle
    val originStateFlow: StateFlow<String?>? = savedState?.getStateFlow("from", null as String?)
    val destinationStateFlow: StateFlow<String?>? = savedState?.getStateFlow("to", null as String?)
    val refreshKeyStateFlow: StateFlow<String?>? = savedState?.getStateFlow("refreshKey", null as String?)

    val origin by (originStateFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
    val destination by (destinationStateFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
    val refreshKey by (refreshKeyStateFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })

    LaunchedEffect(origin, destination, refreshKey) {
        val o = origin
        val d = destination
        Log.d(TAG, "Observed origin=$o destination=$d refresh=$refreshKey")
        if (!o.isNullOrBlank() && !d.isNullOrBlank()) {
            viewModel.fetchLoads(authToken = authToken, origin = o, destination = d)
        } else {
            Log.d(TAG, "Origin or Destination missing - waiting")
        }
    }

    // --- UI: show route card and a tidy list of fields (keeps same visual structure as before) ---
    var search by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
    }, containerColor = BackgroundGray) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 110.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top route card and fields display
                item {
                    RouteCardDynamic(origin = origin ?: fromDisplay, destination = destination ?: toDisplay)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Display parsed fields in a card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F1F6))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Load details (from navigation)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            @Composable
                            fun RowField(label: String, value: String) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = label, fontSize = 13.sp, color = Color(0xFF8E8E8E), modifier = Modifier.weight(0.45f))
                                    Text(text = value.ifBlank { "-" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            RowField("From", fromDisplay)
                            RowField("From - Pin/State", fromPinStateDisplay)
                            RowField("To", toDisplay)
                            RowField("To - Pin/State", toPinStateDisplay)
                            RowField("Distance", distanceInfoDisplay)
//                            RowField("Container", containerInfoDisplay)
//                            RowField("Loading Time", loadingTimeDisplay)
                            RowField("Material", materialDisplay)
                            RowField("Hauler", haulerDisplay)
                            RowField("Party Rate", partyRateDisplay)
                            RowField("Max Load (MT)", maxLoadMTDisplay)
//                            RowField("Wheels", wheelsDisplay)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Vehicle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search") },
                        placeholder = { Text(text = "Search vehicle", color = InfoTextGray) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(BorderStroke(1.dp, SearchFieldBorder), shape = RoundedCornerShape(12.dp))
                            .background(Color.White, shape = RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // The rest of the listing (vehicles) reuses original viewModel state & UI
                if (viewModel.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (!viewModel.errorMessage.isNullOrBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFF1F1F6))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Failed to load data", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = viewModel.errorMessage ?: "Unknown error")
                            }
                        }
                    }
                } else {
                    val mappedVehicles = viewModel.loads.map { load ->
                        val id = "${load.from}_${load.to}_${load.material ?: "unk"}"
                        val type = load.vehicleCategory ?: load.material ?: "Unknown"
                        val length = (load.maxLoadMT?.toDouble() ?: 0.0)
                        val capacity = (load.maxLoadMT?.toDouble() ?: 0.0)
                        val dist = parseDistanceKm(load.distanceInfo)
                        VehicleItem(id = id, type = type, lengthFt = length, capacityTon = capacity, distanceKm = dist)
                    }

                    if (mappedVehicles.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F1F6))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "No loads available", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "No loads without party rate were returned by the API.")
                                }
                            }
                        }
                    } else {
                        val filtered = if (search.isBlank()) mappedVehicles else mappedVehicles.filter {
                            it.id.contains(search, ignoreCase = true)
                        }
                        items(filtered) { vehicle ->
                            VehicleCard(vehicle = vehicle, selected = vehicle.id == selectedId, onSelect = { selectedId = vehicle.id })
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /* submit */ },
                    enabled = selectedId != null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SubmitGreen,
                        disabledContainerColor = SubmitGreen.copy(alpha = 0.6f),
                        contentColor = SubmitText
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Submit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }

    // Helpful debug hint: if you find navigation blocked in logs (currentRoute == targetRoute),
    // ensure you either navigate to a different route or set values on the correct backstack entry.
    // Example: prefer setting savedStateHandle on previousBackStackEntry before navigate:
    // navController.previousBackStackEntry?.savedStateHandle?.set("from", "AMBALA")
    // THEN navController.navigate("kyc")
    //
    // This comment is for your debugging; no runtime effect.
}



/* ---- Route card showing origin/destination ---- */
@Composable
private fun RouteCardDynamic(origin: String?, destination: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F1F6))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GreenDot))
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(modifier = Modifier.height(36.dp).width(1.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            repeat(6) {
                                Box(modifier = Modifier.width(1.dp).height(4.dp).background(Color(0xFFD8D8DB)))
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(RedSquare))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = origin ?: "Select origin",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = destination ?: "Select destination",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

//        Row(
//            modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(InfoBeige).padding(horizontal = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(text = "Fill details on time to confirm", style = MaterialTheme.typography.bodySmall.copy(color = InfoTextGray, fontSize = 13.sp), modifier = Modifier.weight(1f))
//            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TimerBg).padding(horizontal = 10.dp, vertical = 6.dp)) {
//                Text(text = "04:11", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))
//            }
//        }
    }
}

/* ---- Vehicle card UI ---- */
@Composable
fun VehicleCard(vehicle: VehicleItem, selected: Boolean, onSelect: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardInnerBg), border = BorderStroke(1.dp, CardBorderPurple), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                RadioButton(selected = selected, onClick = onSelect, modifier = Modifier.size(28.dp).align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = vehicle.id, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${vehicle.type} · ${vehicle.lengthFt} Ft · ${vehicle.capacityTon} Ton", style = MaterialTheme.typography.bodySmall.copy(color = InfoTextGray, fontSize = 13.sp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(LightPurpleBg.copy(alpha = 0.25f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(LightPurpleBg).padding(4.dp), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = "near", tint = Color(0xFFF2A64E), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Vehicle is ${vehicle.distanceKm} km near loading", style = MaterialTheme.typography.bodySmall.copy(color = InfoTextGray, fontSize = 13.sp))
            }
        }
    }
}

/* ---- Utility ---- */
private fun parseDistanceKm(distanceInfo: String?): Int {
    if (distanceInfo == null) return 0
    val regex = Regex("""(\d+)(?:\s*km|\s*kms|\s*KM)?""", RegexOption.IGNORE_CASE)
    val m = regex.find(distanceInfo)
    return try {
        m?.groups?.get(1)?.value?.toInt() ?: 0
    } catch (_: Exception) {
        0
    }
}

/* ---- Preview helper ---- */
@Preview(showBackground = true)
@Composable
fun VehicleKycScreenPreview() {
    MaterialTheme {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Preview - VehicleKycScreen layout")
            }
        }
    }
}
