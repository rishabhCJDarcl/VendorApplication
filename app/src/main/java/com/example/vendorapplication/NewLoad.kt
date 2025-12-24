package com.example.vendorapplication

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import com.example.vendorapplication.navigation.BottomNavItem

// ✅ THIS MUST BE kotlinx

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.round
import java.util.concurrent.TimeUnit


private fun getFirstString(obj: JsonObject?, vararg keys: String): String? {
    if (obj == null) return null
    for (k in keys) {
        try {
            if (obj.has(k) && !obj.get(k).isJsonNull) {
                return obj.get(k).asString
            }
        } catch (_: Exception) { /* ignore and continue */ }
    }
    return null
}
// -----------------------------
// Screen navigation route names
// -----------------------------
private const val ROUTE_LIST = "list"
private const val ROUTE_DETAILS = "details"

// -----------------------------
// Data models (Parcelable for nav data passing)
// -----------------------------
data class LoadEntry(
    val from: String,
    val fromPinState: String,
    val to: String,
    val toPinState: String,
    val containerInfo: String? = null,
    val loadingTime: String? = null,
    val distanceInfo: String? = null,
    val partyRate: String? = null,
    val material: String? = null,
    val hauler: String? = null,
    // NEW: remaining (net) quantity in MT (nullable)
    val netQuantityMT: Double? = null,
    val maxLoadMT: Float? = null,
    val wheels: Int? = null,
    val vehicleCategory: String? = null,
    val orderId: String? = null,
    val lineItemId: String? = null,
    // NEW: status field (from backend)
    val status: String? = null
) : Parcelable  {

    constructor(parcel: Parcel) : this(
        from = parcel.readString().orEmpty(),
        fromPinState = parcel.readString().orEmpty(),
        to = parcel.readString().orEmpty(),
        toPinState = parcel.readString().orEmpty(),
        containerInfo = parcel.readString(),
        loadingTime = parcel.readString(),
        distanceInfo = parcel.readString(),
        partyRate = parcel.readString(),
        material = parcel.readString(),
        hauler = parcel.readString(),
        // read netQuantityMT as Double?
        netQuantityMT = parcel.readValue(Double::class.java.classLoader) as? Double,
        maxLoadMT = parcel.readValue(Float::class.java.classLoader) as? Float,
        wheels = parcel.readValue(Int::class.java.classLoader) as? Int,
        vehicleCategory = parcel.readString(),
        orderId = parcel.readString(),
        lineItemId = parcel.readString(),
        status = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(fromPinState)
        parcel.writeString(to)
        parcel.writeString(toPinState)
        parcel.writeString(containerInfo)
        parcel.writeString(loadingTime)
        parcel.writeString(distanceInfo)
        parcel.writeString(partyRate)
        parcel.writeString(material)
        parcel.writeString(hauler)
        // write netQuantityMT
        parcel.writeValue(netQuantityMT)
        parcel.writeValue(maxLoadMT)
        parcel.writeValue(wheels)
        parcel.writeString(vehicleCategory)
        parcel.writeString(orderId)
        parcel.writeString(lineItemId)
        parcel.writeString(status)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LoadEntry> {
        override fun createFromParcel(parcel: Parcel): LoadEntry {
            return LoadEntry(parcel)
        }

        override fun newArray(size: Int): Array<LoadEntry?> {
            return arrayOfNulls(size)
        }
    }
}

data class MarketRateUnique(val rank: Int, val initials: String, val amount: String, val rating: String)

// -----------------------------
// Retrofit API & builder
// (unchanged from your original code, minimal edits only)
// -----------------------------
private const val ORDERS_BASE_URL = "https://tms-test.cjdarcl.com:8002/"
private const val ORDERS_RAW_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTk5MDczNTQsInVzZXJJZCI6IjIyYWM1NjFkLWRhNWMtNDE2Ni1hYmJhLWQ5NzNlMmYwNDZhMyIsImVtYWlsIjoibmlraGlsLm1haGFyYUBjamRhcmNsLmNvbSIsIm1vYmlsZU51bWJlciI6Ijk2NTQ2MzI3NDQiLCJvcmdJZCI6ImVmMzAwNjgzLTkwMWItNDc3NC1iZjBlLTk1NWQ0OWU3OTZiYyIsIm5hbWUiOiJOaWtoaWwgU2luZ2ggTWFoYXJhIiwib3JnVHlwZSI6IkZMRUVUX09XTkVSIiwiaXNHb2QiOnRydWUsInBvcnRhbFR5cGUiOiJiYXNpYyJ9.MDDgj22r1Hlu4cbl_qySQpqLndzS1D_JAk0owgnQxJ8"

interface OrdersApi {
    @GET
    suspend fun fetchOrdersRaw(@Url url: String): Response<JsonElement>
}

private fun createOrdersApi(): OrdersApi {
    val logger = HttpLoggingInterceptor { m -> Log.d("OkHttp", m) }.apply { level = HttpLoggingInterceptor.Level.BODY }

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val orig = chain.request()
            val req = orig.newBuilder()
                .header("Authorization", "Bearer $ORDERS_RAW_TOKEN")
                .header("Accept", "application/json")
                .method(orig.method, orig.body)
                .build()
            Log.d("OrdersNetwork", "Request -> ${req.url}")
            chain.proceed(req)
        }
        .addInterceptor(logger)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(ORDERS_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(OrdersApi::class.java)
}

fun makeOrdersUrl(origin: String, destination: String): String {
    val filterJson = """
        {
          "origin": ["$origin"],
          "destination": ["$destination"],
          "lineItems.expectedPickupDate": { "from": null, "till": null },
          "orderType": ["Order", "MTROrder", "MarketOrder"]
        }
    """.trimIndent()
    val encoded = try {
        URLEncoder.encode(filterJson, "UTF-8")
    } catch (t: Throwable) {
        Log.w("Orders", "URLEncoder failed: ${t.message}")
        filterJson.replace(" ", "%20")
    }
    return "https://tms-test.cjdarcl.com:8002/shipment-view/sales/v2/orders?limit=50&filters=$encoded"
}

// -----------------------------
// Repository
// -----------------------------
class OrdersRepository(private val api: OrdersApi) {
    suspend fun getOrders(origin: String, destination: String): Response<JsonElement> {
        val url = makeOrdersUrl(origin, destination)
        Log.d("OrdersRepo", "Calling: $url")
        return api.fetchOrdersRaw(url)
    }
}

// -----------------------------
// UI state
// -----------------------------
sealed class OrdersUiState {
    object Idle : OrdersUiState()
    object Loading : OrdersUiState()
    data class Success(val items: List<LoadEntry>) : OrdersUiState()
    object Empty : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

// -----------------------------
// ViewModel
// -----------------------------
class OrdersViewModel(private val repo: OrdersRepository) : ViewModel() {
    private val _ui = MutableStateFlow<OrdersUiState>(OrdersUiState.Idle)
    val uiState: StateFlow<OrdersUiState> = _ui

    private val _origin = MutableStateFlow<String?>(null)
    private val _destination = MutableStateFlow<String?>(null)

    fun setOrigin(v: String?) { _origin.value = v; Log.d("OrdersVM","Origin set to: $v") }
    fun setDestination(v: String?) { _destination.value = v; Log.d("OrdersVM","Dest set to: $v") }

    fun fetchIfReady() {
        val o = _origin.value
        val d = _destination.value
        if (o.isNullOrBlank() || d.isNullOrBlank()) {
            Log.d("OrdersVM", "Not ready to fetch (missing origin/dest)")
            return
        }
        fetchOrders(o, d)
    }

    private fun fetchOrders(origin: String, destination: String) {
        viewModelScope.launch {
            _ui.value = OrdersUiState.Loading
            try {
                val resp = repo.getOrders(origin, destination)
                Log.d("OrdersVM", "Response: code=${resp.code()} success=${resp.isSuccessful}")
                if (!resp.isSuccessful) {
                    val err = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _ui.value = OrdersUiState.Error("Server error ${resp.code()}: ${err?.take(200) ?: "no body"}")
                    return@launch
                }
                val body = resp.body()
                if (body == null) {
                    _ui.value = OrdersUiState.Empty
                    return@launch
                }
                val parsed = parseOrdersJson(body)
                Log.d("OrdersVM", "Parsed ${parsed.size} load entries")
                // Filter: show only items with no party rate or "Rate on Request"
                val filtered = parsed.filter {
                    val r = it.partyRate ?: ""
                    r.contains("Rate on Request", ignoreCase = true) || r.isBlank()
                }
                Log.d("OrdersVM", "Filtered -> ${filtered.size} (without party rate)")
                _ui.value = if (filtered.isEmpty()) OrdersUiState.Empty else OrdersUiState.Success(filtered)
            } catch (io: java.io.IOException) {
                Log.e("OrdersVM", "Network IO", io)
                _ui.value = OrdersUiState.Error("Network error: ${io.message ?: "IO"}")
            } catch (e: Exception) {
                Log.e("OrdersVM", "Unknown", e)
                _ui.value = OrdersUiState.Error("Unknown error: ${e.message ?: "error"}")
            }
        }
    }
}

// -----------------------------
// JSON parser (robust, defensive) - unchanged
// -----------------------------
private fun parseOrdersJson(json: JsonElement): List<LoadEntry> {
    val out = mutableListOf<LoadEntry>()
    try {
        val rootArray: JsonArray? = when {
            json.isJsonArray -> json.asJsonArray
            json.isJsonObject -> {
                val obj = json.asJsonObject
                when {
                    obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                    obj.has("orders") && obj.get("orders").isJsonArray -> obj.getAsJsonArray("orders")
                    obj.has("items") && obj.get("items").isJsonArray -> obj.getAsJsonArray("items")
                    else -> obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
                }
            }
            else -> null
        }

        if (rootArray == null) {
            Log.w("OrdersParser", "No array root found")
            return emptyList()
        }

        Log.d("OrdersParser", "items = ${rootArray.size()}")
        for (orderEl in rootArray) {
            if (!orderEl.isJsonObject) continue
            val orderObj = orderEl.asJsonObject

            if (!orderObj.has("lineItems") || !orderObj.get("lineItems").isJsonArray) continue
            val lineItems = orderObj.getAsJsonArray("lineItems")

            for (li in lineItems) {
                if (!li.isJsonObject) continue
                val liObj = li.asJsonObject

                val distance = try {
                    if (liObj.has("distance") && !liObj.get("distance").isJsonNull) {
                        val v = liObj.get("distance")
                        when {
                            v.isJsonPrimitive && (v as JsonPrimitive).isNumber -> v.asFloat
                            v.isJsonPrimitive -> v.asString.toFloatOrNull()
                            else -> null
                        }
                    } else null
                } catch (_: Exception) { null }

                val freightType = try { if (liObj.has("freightChargeType") && !liObj.get("freightChargeType").isJsonNull) liObj.get("freightChargeType").asString else null } catch (_: Exception) { null }
                val freightRate = try {
                    if (liObj.has("freightChargeRate") && !liObj.get("freightChargeRate").isJsonNull) {
                        val v = liObj.get("freightChargeRate")
                        when {
                            v.isJsonPrimitive && (v as JsonPrimitive).isNumber -> v.asDouble
                            v.isJsonPrimitive -> v.asString.replace(",", "").trim().toDoubleOrNull()
                            else -> null
                        }
                    } else null
                } catch (_: Exception) { null }

                val netQuantity = try {
                    if (liObj.has("remainingPlannedQuantity") && liObj.get("remainingPlannedQuantity").isJsonObject) {
                        val rem = liObj.getAsJsonObject("remainingPlannedQuantity")
                        if (rem.has("weight") && rem.get("weight").isJsonObject) {
                            val wt = rem.getAsJsonObject("weight")
                            when {
                                wt.has("netQuantity") && !wt.get("netQuantity").isJsonNull -> {
                                    val v = wt.get("netQuantity")
                                    when {
                                        v.isJsonPrimitive && (v as JsonPrimitive).isNumber -> v.asFloat
                                        v.isJsonPrimitive -> v.asString.toFloatOrNull()
                                        else -> null
                                    }
                                }
                                else -> null
                            }
                        } else null
                    } else null
                } catch (_: Exception) { null }

                val material = try { if (liObj.has("loadInfo") && liObj.get("loadInfo").isJsonObject) { val lo = liObj.getAsJsonObject("loadInfo"); if (lo.has("material") && !lo.get("material").isJsonNull) lo.get("material").asString else null } else null } catch (_: Exception) { null }

                val consignerState = try {
                    if (liObj.has("consigner") && liObj.get("consigner").isJsonObject) {
                        val con = liObj.getAsJsonObject("consigner")
                        if (con.has("places") && con.getAsJsonArray("places").size() > 0) {
                            val p0 = con.getAsJsonArray("places").get(0)
                            if (p0.isJsonObject && p0.asJsonObject.has("state") && !p0.asJsonObject.get("state").isJsonNull) p0.asJsonObject.get("state").asString else null
                        } else if (con.has("state") && !con.get("state").isJsonNull) con.get("state").asString else null
                    } else null
                } catch (_: Exception) { null }

                val consigneeState = try {
                    if (liObj.has("consignee") && liObj.get("consignee").isJsonObject) {
                        val con = liObj.getAsJsonObject("consignee")
                        if (con.has("places") && con.getAsJsonArray("places").size() > 0) {
                            val p0 = con.getAsJsonArray("places").get(0)
                            if (p0.isJsonObject && p0.asJsonObject.has("state") && !p0.asJsonObject.get("state").isJsonNull) p0.asJsonObject.get("state").asString else null
                        } else if (con.has("state") && !con.get("state").isJsonNull) con.get("state").asString else null
                    } else null
                } catch (_: Exception) { null }

                var wheelsDerived: Int? = null
                var haulerName: String? = null
                var vehicleCat: String? = null
                var maxLoad: Float? = null
                try {
                    if (liObj.has("allowedCustomerLoadTypes") && liObj.get("allowedCustomerLoadTypes").isJsonArray) {
                        val arr = liObj.getAsJsonArray("allowedCustomerLoadTypes")
                        if (arr.size() > 0) {
                            val a0 = arr.get(0)
                            if (a0.isJsonObject) {
                                val obj = a0.asJsonObject
                                haulerName = obj.get("name")?.takeIf { !it.isJsonNull }?.let { if (it.isJsonPrimitive) it.asString else it.toString() }
                                vehicleCat = obj.get("vehicleCategory")?.takeIf { !it.isJsonNull }?.let { if (it.isJsonPrimitive) it.asString else it.toString() }
                                try {
                                    val pmt = obj.get("passingCapacityMT")
                                    if (pmt != null && !pmt.isJsonNull && pmt.isJsonPrimitive && (pmt as JsonPrimitive).isNumber) {
                                        maxLoad = try { pmt.asFloat } catch (_: Throwable) { pmt.asString.toFloatOrNull() }
                                    } else { maxLoad = obj.get("passingCapacityMT")?.asString?.toFloatOrNull() }
                                } catch (_: Exception) { }
                                try {
                                    val nw = obj.get("numberOfWheels")
                                    if (nw != null && !nw.isJsonNull && nw.isJsonPrimitive && (nw as JsonPrimitive).isNumber) {
                                        wheelsDerived = try { nw.asInt } catch (_: Throwable) { nw.asString.toIntOrNull() }
                                    } else { wheelsDerived = obj.get("numberOfWheels")?.asString?.toIntOrNull() }
                                } catch (_: Exception) { }
                            }
                        }
                    }
                } catch (_: Exception) { }

                var partyRateStr: String? = "Rate on Request"
                try {
                    if (freightType?.equals("perMT", true) == true && netQuantity != null && netQuantity > 0 && freightRate != null) {
                        val amount = freightRate * netQuantity
                        val amtLong = round(amount).toLong()
                        val formatted = NumberFormat.getNumberInstance(Locale("en", "IN")).format(amtLong)
                        partyRateStr = "₹ $formatted"
                    } else if (freightType?.equals("perVehicle", true) == true && freightRate != null) {
                        partyRateStr = "₹ ${freightRate.toLong()}"
                    }
                } catch (_: Exception) { }

                val distanceInfo = distance?.let { "${String.format("%.0f", it)} km" }

                val fromName = try {
                    if (liObj.has("consigner") && liObj.get("consigner").isJsonObject) {
                        val con = liObj.getAsJsonObject("consigner")
                        if (con.has("places") && con.getAsJsonArray("places").size() > 0) {
                            val p0 = con.getAsJsonArray("places").get(0)
                            if (p0.isJsonObject && p0.asJsonObject.has("name") && !p0.asJsonObject.get("name").isJsonNull) p0.asJsonObject.get("name").asString else null
                        } else null
                    } else null
                } catch (_: Exception) { null } ?: orderObj.getAsJsonObject("salesOffice")?.get("name")?.asString ?: "N/A"

                val toName = try {
                    if (liObj.has("consignee") && liObj.get("consignee").isJsonObject) {
                        val con = liObj.getAsJsonObject("consignee")
                        if (con.has("places") && con.getAsJsonArray("places").size() > 0) {
                            val p0 = con.getAsJsonArray("places").get(0)
                            if (p0.isJsonObject && p0.asJsonObject.has("name") && !p0.asJsonObject.get("name").isJsonNull) p0.asJsonObject.get("name").asString else null
                        } else null
                    } else null
                } catch (_: Exception) { null } ?: orderObj.getAsJsonObject("customer")?.getAsJsonArray("places")?.get(0)?.asJsonObject?.get("name")?.asString ?: "N/A"

                val orderIdVal = getFirstString(
                    orderObj,
                    "id",
                    "orderId",
                    "order_id",
                    "salesOrderId",
                    "orderReference"
                )

                val lineItemIdVal = getFirstString(
                    liObj.asJsonObject,
                    "id",
                    "lineItemId",
                    "line_item_id",
                    "lineItemReference"
                )

                // Extract status from order object defensively
                val statusVal = try {
                    // try common keys
                    getFirstString(orderObj, "status", "orderStatus", "order_state", "state") ?:
                    // fallback: maybe status is nested
                    orderObj.getAsJsonObject("status")?.get("value")?.asString
                } catch (_: Exception) { null }

// Debug once if still missing
                if (orderIdVal.isNullOrBlank() || lineItemIdVal.isNullOrBlank()) {
                    Log.w(
                        "OrdersParser",
                        "IDs missing. orderId=$orderIdVal lineItemId=$lineItemIdVal"
                    )
                }


                val entry = LoadEntry(
                    from = fromName,
                    fromPinState = consignerState ?: "",
                    to = toName,
                    toPinState = consigneeState ?: "",
                    containerInfo = try { liObj.get("containerInfo")?.asString } catch (_: Exception) { null },
                    loadingTime = try { liObj.get("loadingTime")?.asString } catch (_: Exception) { null },
                    distanceInfo = distanceInfo,
                    partyRate = partyRateStr,
                    material = material ?: "-",
                    hauler = haulerName ?: "-",
                    netQuantityMT = netQuantity?.toDouble(),
                    maxLoadMT = maxLoad,
                    wheels = wheelsDerived,
                    vehicleCategory = vehicleCat ?: "-",

                    // ✅ FIXED ID PASSING
                    orderId = orderIdVal,
                    lineItemId = lineItemIdVal,
                    // include status
                    status = statusVal ?: "-"
                )



                out.add(entry)
            }
        }
    } catch (t: Throwable) {
        Log.e("OrdersParser", "parse error", t)
    }
    return out
}

// -----------------------------
// ViewModel factory
// -----------------------------
class OrdersViewModelFactory(private val repo: OrdersRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrdersViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

// -----------------------------
// UI colors / tokens
// -----------------------------
private val GreenDotUnique = Color(0xFF22C55E)
private val RedDotUnique = Color(0xFFEF4444)
private val PurplePillBgUnique = Color(0xFFE9D5FF)
private val PurpleTextUnique = Color(0xFF7C3AED)
private val ConfirmGreenUnique = Color(0xFF16A34A)
private val ConfirmGreenTextUnique = Color(0xFF15803D)

// -----------------------------
// Top-level composable entry (keeps same name for integration)
// We set up a NavHost so parent NavGraph can call NewLoadScreenCompose(navController)
// -----------------------------
//@Composable
//fun NewLoadScreenCompose(parentNavController: NavHostController) {
//    // We want to work with an internal nav controller for list->details within this screen,
//    // or you can pass your appNavController instead. For simplicity and robustness we create a local navController.
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = ROUTE_LIST) {
//        composable(ROUTE_LIST) {
//            // LIST screen - pass the local navController so it can set savedStateHandle + navigate to details
//            NewLoadListScreen(navController = navController)
//        }
//        composable(ROUTE_DETAILS) {
//            // DETAILS screen - retrieve LoadEntry using savedStateHandle from the previous backStackEntry
//            val previousEntry = navController.previousBackStackEntry
//            // Try to get the parcelable safely
//            val load: LoadEntry? = previousEntry?.savedStateHandle?.get<LoadEntry>("selected_load")
//            LaunchedEffect(load?.orderId ?: "null") {
//                if (load != null) {
//                    Log.d("Navigation", "Entered Details Screen - Order ID: ${load.orderId} LineItem: ${load.lineItemId} From: ${load.from} To: ${load.to} PartyRate: ${load.partyRate} netQuantityMT=${load.netQuantityMT} status=${load.status}")
//                } else {
//                    Log.w("Navigation", "Entered Details Screen with null load data")
//                }
//            }
//            OrderDetailsScreen(load, onBack = { navController.popBackStack() })
//        }
//    }
//}
//@Composable
//fun NewLoadScreenCompose(parentNavController: NavHostController) {
//
//    // 🔹 Local NavController (same pattern as MyTripsScreen)
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = ROUTE_LIST
//    ) {
//
//        // ================= NEW LOAD LIST =================
//        composable(ROUTE_LIST) {
//            NewLoadListScreen(navController = navController)
//        }
//
//        // ================= LOAD DETAILS =================
//        composable(ROUTE_DETAILS) {
//            val previousEntry = navController.previousBackStackEntry
//            val load: LoadEntry? =
//                previousEntry?.savedStateHandle?.get("selected_load")
//
//            LaunchedEffect(load?.orderId ?: "null") {
//                if (load != null) {
//                    Log.d(
//                        "Navigation",
//                        "Entered Details Screen - Order ID=${load.orderId}, " +
//                                "LineItem=${load.lineItemId}, From=${load.from}, To=${load.to}"
//                    )
//                } else {
//                    Log.w("Navigation", "Entered Details Screen with null load")
//                }
//            }
//
//            OrderDetailsScreen(
//                load = load,
//                onBack = { navController.popBackStack() }
//            )
//        }
//
//        // ================= LOAD SCREEN (TAB) =================
//        composable(BottomNavItem.Load.route) {
//            Log.d("NewLoadNav", "Open: Load Screen")
//            LoadScreen(navController)
//        }
//
//        // ================= MY LOAD PREVIEW (TAB) =================
//        composable("my_load_preview") {
//            Log.d("NewLoadNav", "Open: My Load Preview")
//            MyLoadScreen(navController)
//        }
//    }
//}
@Composable
fun NewLoadScreenCompose(parentNavController: NavHostController) {

    // 🔹 Local NavController (same pattern as MyTripsScreen)
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_LIST
    ) {

        // ================= NEW LOAD LIST =================
        composable(ROUTE_LIST) {
            NewLoadListScreen(
                navController = navController,
                parentNavController = parentNavController
            )
        }


        // ================= LOAD DETAILS =================
        composable(ROUTE_DETAILS) {
            val previousEntry = navController.previousBackStackEntry
            val load: LoadEntry? =
                previousEntry?.savedStateHandle?.get("selected_load")

            LaunchedEffect(load?.orderId ?: "null") {
                if (load != null) {
                    Log.d(
                        "Navigation",
                        "Entered Details Screen - Order ID=${load.orderId}, " +
                                "LineItem=${load.lineItemId}, From=${load.from}, To=${load.to}"
                    )
                } else {
                    Log.w("Navigation", "Entered Details Screen with null load")
                }
            }

            OrderDetailsScreen(
                load = load,
                onBack = { navController.popBackStack() }
            )
        }

        // ================= LOAD SCREEN (TAB) =================
        composable(BottomNavItem.Load.route) {
            Log.d("NewLoadNav", "Open: Load Screen")
            LoadScreen(navController)
        }

        // ================= MY LOAD PREVIEW (TAB) =================
        composable("my_load_preview") {
            Log.d("NewLoadNav", "Open: My Load Preview")
            MyLoadScreen(navController)
        }

        // ================= MAIN (APP ROOT) [ADD-ON] =================
        composable("main") {
            Log.d("NewLoadNav", "Go to Main App Root")
            parentNavController.navigate("main") {
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}


@Composable
fun BannerCards() {
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


@Composable
fun SegmentedTabss(
    localNavController: NavHostController,
    parentNavController: NavHostController
) {
    val TAG = "SegmentedTabs"
    val selectedState = remember { mutableStateOf(2) }

    val tabs = listOf(
        TabItem.Route("My Load(1)", "main"), // parent
        TabItem.Route("Load(4)", BottomNavItem.Load.route), // parent
        TabItem.Route("New load(33)", ROUTE_LIST) // local
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

        tabs.forEachIndexed { index, tab ->

            Card(
                modifier = tabModifier.clickable {

                    selectedState.value = index

                    val route = tab.route
                    if (route.isNullOrBlank()) {
                        Log.w(TAG, "Route is null/blank for tab=${tab.title}")
                        return@clickable
                    }

                    Log.d(TAG, "================ TAB CLICK ================")
                    Log.d(TAG, "Title: ${tab.title}, route=$route")

                    when (route) {
                        ROUTE_LIST -> {
                            Log.d(TAG, "Using LOCAL NavController -> $route")
                            localNavController.navigate(route) {
                                launchSingleTop = true
                                popUpTo(ROUTE_LIST) { inclusive = false }
                            }
                        }

                        else -> {
                            Log.d(TAG, "Using PARENT NavController -> $route")
                            parentNavController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    Log.d(TAG, "==========================================")
                },
                shape = RoundedCornerShape(50)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(tab.title)
                }
            }
        }
    }
}







// -----------------------------
// List screen
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLoadListScreen(
    navController: NavHostController,
    parentNavController: NavHostController
) {

    val api = remember { createOrdersApi() }
    val repo = remember { OrdersRepository(api) }
    val vm: OrdersViewModel = viewModel(factory = OrdersViewModelFactory(repo))
    val uiState by vm.uiState.collectAsState(initial = OrdersUiState.Idle)

    // guard to prevent double navigation on very fast taps
    var navigateGuard by rememberSaveable { mutableStateOf(false) }

    // ---------- NEW: reset navigateGuard when destination changes ----------
    // This fixes the issue where navigateGuard stays true after first navigation and blocks subsequent navigations.
    // We add a destination change listener and remove it on dispose.
    androidx.compose.runtime.DisposableEffect(navController) {
        val listener =
            NavController.OnDestinationChangedListener { _: NavController, destination: NavDestination, _ ->
                // Reset guard when navigation completes
                navigateGuard = false
                Log.d(
                    "OrdersUI_Nav",
                    "Destination changed to=${destination.route}. navigateGuard reset=false"
                )
            }
        // register - NavController's addOnDestinationChangedListener is an instance function,
        // but different nav versions have different signatures. Use addOnDestinationChangedListener if available.
        try {
            navController.addOnDestinationChangedListener(listener)
        } catch (_: Throwable) {
            // Safety: older nav versions may differ; ignore gracefully.
            Log.w("OrdersUI_Nav", "Could not add destination listener (fallback).")
        }
        onDispose {
            try {
                navController.removeOnDestinationChangedListener(listener)
            } catch (_: Throwable) {
                // ignore
            }
        }
    }
    // ---------- END NEW ----------

    // ✅ FIXED navigation lambda (NO let, NO type issue)
    // ✅ FIXED navigation lambda (NO return, NO label)
    val navigateToDetails: (LoadEntry) -> Unit = { item ->

        val TAG = "OrdersUI_Nav"

        Log.d(
            TAG,
            "Navigating to Details - Order ID: ${item.orderId} " +
                    "LineItem: ${item.lineItemId} From: ${item.from} " +
                    "To: ${item.to} PartyRate: ${item.partyRate} netQuantityMT=${item.netQuantityMT} status=${item.status}"
        )

        val currentRoute =
            navController.currentBackStackEntry?.destination?.route

        val canNavigate =
            currentRoute == ROUTE_LIST && !navigateGuard

        if (canNavigate) {
            // If IDs are missing, we still allow navigation — but sanitize the payload.
            if (item.orderId.isNullOrBlank() || item.lineItemId.isNullOrBlank()) {
                Log.w(TAG, "IDs missing; navigating with sanitized defaults. orderId=${item.orderId}, lineItemId=${item.lineItemId}")
            }

            navigateGuard = true

            try {
                val sanitized = item.copy(
                    partyRate = item.partyRate ?: "Rate on Request",
                    netQuantityMT = item.netQuantityMT ?: 0.0,
                    orderId = item.orderId ?: "",        // empty string is safe in details screen
                    lineItemId = item.lineItemId ?: "",
                    status = item.status ?: "-"
                )

                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("selected_load", sanitized)

                Log.d(TAG, "Saved sanitized load into savedStateHandle: orderId=${sanitized.orderId} lineItemId=${sanitized.lineItemId} netQuantityMT=${sanitized.netQuantityMT} status=${sanitized.status}")

                navController.navigate(ROUTE_DETAILS)

            } catch (t: Throwable) {
                Log.e(TAG, "Navigation failed: ${t.message}", t)
                navigateGuard = false
            }
        } else {
            Log.w(TAG, "Navigation skipped. currentRoute=$currentRoute, navigateGuard=$navigateGuard")
        }


    }

    // Helper to avoid awkward return in lambda above - we use this block to allow early returns in lambda
    // (This small helper is local to the composable; it does not change behavior)
    fun <T> run(block: () -> T): T = block()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "View Loads", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = { IconButton(onClick = { /* pop handled by parent */ }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    Box(modifier = Modifier.padding(end = 12.dp).size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Text(text = "A/अ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Text(text = "Load alert", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        var checked by rememberSaveable { mutableStateOf(true) }
                        Switch(checked = checked, onCheckedChange = { checked = it })
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            RouteSelectorRows(onApplySelection = { origin, dest ->
                vm.setOrigin(origin)
                vm.setDestination(dest)
                vm.fetchIfReady()
            })

            Spacer(modifier = Modifier.height(12.dp))
            BannerCards()
            Spacer(modifier = Modifier.height(12.dp))
            SegmentedTabss(
                localNavController = navController,
                parentNavController = parentNavController
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState) {
                is OrdersUiState.Idle -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Choose origin & destination then tap View Loads", color = Color(0xFF9B9B9B)) }
                is OrdersUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is OrdersUiState.Empty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF9B9B9B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No loads without calculated rate found for the selected route.", color = Color(0xFF6B6B6B))
                    }
                }
                is OrdersUiState.Error -> {
                    val m = (uiState as OrdersUiState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Something went wrong", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(m, color = Color(0xFF6B6B6B))
                        }
                    }
                }
                is OrdersUiState.Success -> {
                    val items = (uiState as OrdersUiState.Success).items
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 160.dp)) {
                        items(items) { item ->
                            OrderCard(item = item, onConfirm = {
                                // Confirm button uses same navigation lambda
                                Log.d("OrdersUI", "Confirm clicked - Order ID: ${item.orderId} LineItem: ${item.lineItemId} From: ${item.from} To: ${item.to} PartyRate: ${item.partyRate}")
                                // single source
                                navigateToDetails(item)
                            }, onCardClick = {
                                // Card click uses same navigation lambda
                                Log.d("OrdersUI", "Card clicked - Order ID: ${item.orderId} LineItem: ${item.lineItemId} From: ${item.from} To: ${item.to} PartyRate: ${item.partyRate}")
                                navigateToDetails(item)
                            })
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------
// Order card (single-source click handler: onCardClick used everywhere inside the card)
// Now enhanced to display "Order Number" and "Status" fetched from backend (orderId & status)
// -----------------------------
@Composable
fun OrderCard(
    item: LoadEntry,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    fun hasText(s: String?) = !s.isNullOrBlank()
    fun fmt(f: Float?) = f?.let { String.format("%.2f", it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            // Make whole card clickable -> single click handler
            .clickable { try { onCardClick() } catch (_: Exception) {} },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GreenDotUnique))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.width(2.dp).height(36.dp).background(Color(0xFFECECEC)))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(RedDotUnique))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (hasText(item.from)) {
                        Text(text = item.from, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (hasText(item.fromPinState)) Text(text = item.fromPinState, fontSize = 12.sp, color = Color(0xFF6B6B6B))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (hasText(item.to)) {
                        Text(text = item.to, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111111), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (hasText(item.toPinState)) Text(text = item.toPinState, fontSize = 12.sp, color = Color(0xFF6B6B6B))
                    }
                }
                // Arrow area also triggers same navigation - call onCardClick
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { try { onCardClick() } catch (_: Exception) {} }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (hasText(item.distanceInfo)) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PurplePillBgUnique).padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFC85A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.distanceInfo ?: "", fontSize = 13.sp, color = PurpleTextUnique, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = Color(0xFFF1F1F1), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                @Composable fun DataRow(label: String, value: String) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = label, fontSize = 13.sp, color = Color(0xFF8E8E8E), modifier = Modifier.weight(0.45f))
                        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111111), modifier = Modifier.weight(0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // NEW: show Order Number and Status fetched from backend
                DataRow(label = "Order Number", value = item.orderId ?: "N/A")
                DataRow(label = "Status", value = item.status ?: "N/A")

                if (!item.material.isNullOrBlank()) { DataRow(label = "Material", value = item.material!!) }
                if (!item.hauler.isNullOrBlank()) { DataRow(label = "Hauler", value = item.hauler!!) }
                if (item.maxLoadMT != null) { DataRow(label = "Max Load (MT)", value = fmt(item.maxLoadMT)!!) }
                if (item.wheels != null) { DataRow(label = "Wheels", value = item.wheels.toString()) }
                if (!item.vehicleCategory.isNullOrBlank()) { DataRow(label = "Vehicle Category", value = item.vehicleCategory!!) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Party's Rate", fontSize = 12.sp, color = Color(0xFF8E8E8E))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (!item.partyRate.isNullOrBlank()) {
                        Text(text = item.partyRate!!, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111))
                    } else Spacer(modifier = Modifier.height(0.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { try { onConfirm() } catch (_: Exception) {} },
                    colors = ButtonDefaults.buttonColors(containerColor = ConfirmGreenUnique),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp).defaultMinSize(minWidth = 120.dp)
                ) {
                    Text(text = "Confirm", color = ConfirmGreenTextUnique, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// -----------------------------
// Destination Screen - uses the LoadEntry passed via savedStateHandle (type-safe Parcelable)
// Renders exact UI from passed data (no API calls)
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(load: LoadEntry?, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
    }, containerColor = Color(0xFFF5F7FA)) { innerPadding ->
        if (load == null) {
            // Safe null handling UI
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF9B9B9B), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unable to display details (missing data).", color = Color(0xFF6B6B6B))
                }
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            // Top route card (from / to)
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(12.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF2EBF84), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = load.from, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            Text("${load.fromPinState}", color = Color.Gray, modifier = Modifier.padding(start = 16.dp, top = 6.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5A44), RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = load.to, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            Text("${load.toPinState}", color = Color.Gray, modifier = Modifier.padding(start = 16.dp, top = 6.dp))
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            // Summary card: distance / container / material
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(12.dp))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.TravelExplore, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text(text = load.distanceInfo ?: "N/A") }
                    Divider()
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocalShipping, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text(text = load.containerInfo ?: "Vehicle details not provided") }
                    Divider()
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text(text = load.material ?: "-") }
                }
            }

            // ---------- Enter Your Rate Card (Market Rates removed) ----------
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header (now focused on entering rate)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enter Your Rate", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // STATE: show/hide input, user text, submission state, calculated party rate
                    val showRateInput = remember { mutableStateOf(false) }
                    val userRateText = rememberSaveable { mutableStateOf("") }
                    val isSubmitted = rememberSaveable { mutableStateOf(false) }
                    val calculatedPartyRate = rememberSaveable { mutableStateOf<Double?>(null) }

                    // simple, safe read of net quantity (Double?), fallback to 0.0
                    val netQuantityMT: Double = load.netQuantityMT ?: 0.0

                    // Button that toggles the input area
                    Spacer(modifier = Modifier.height(6.dp))

                    // Show the "Enter your rate" button only when not yet submitted
                    if (!isSubmitted.value) {
                        Button(
                            onClick = {
                                // toggle input visibility
                                showRateInput.value = !showRateInput.value
                                // reset submission UI when opening input again
                                if (!showRateInput.value) {
                                    isSubmitted.value = false
                                    calculatedPartyRate.value = null
                                    userRateText.value = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EBF84)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enter your rate", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input area (appears inline on the same screen) — hidden after submit because showRateInput will be false
                    if (showRateInput.value && !isSubmitted.value) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = userRateText.value,
                                onValueChange = { new ->
                                    // allow only digits and decimal point
                                    val filtered = new.filter { it.isDigit() || it == '.' }
                                    userRateText.value = filtered
                                },
                                label = { Text("Enter rate (numeric)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = {
                                        val entered = userRateText.value.trim()
                                        val parsed = entered.toDoubleOrNull()
                                        if (entered.isEmpty() || parsed == null) {
                                            // invalid input: show simple inline feedback (log for now)
                                            Log.d("RateFlow", "Submit failed - invalid input: '$entered'")
                                            return@Button
                                        }

                                        // Use the dynamic formula (no hardcodes)
                                        val userEnteredRate = parsed
                                        val partyRate = userEnteredRate * netQuantityMT

                                        // Save to state for UI display
                                        calculatedPartyRate.value = partyRate
                                        isSubmitted.value = true

                                        // Close input area and hide the enter button (because isSubmitted is true)
                                        showRateInput.value = false

                                        // Logging (mandatory)
                                        Log.d("RateFlow", "User entered rate: $userEnteredRate")
                                        Log.d("RateFlow", "netQuantityMT used: $netQuantityMT")
                                        Log.d("RateFlow", "PartyRate calculation: $userEnteredRate * $netQuantityMT = $partyRate")
                                        Log.d("RateFlow", "Full breakdown: freightChargeType=perMT, freightChargeRate=$userEnteredRate, netQuantityMT=$netQuantityMT, PartyRate=$partyRate")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EBF84))
                                ) {
                                    Text("Submit", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Success UI and display of entered rate + calculated PartyRate
                    if (isSubmitted.value && calculatedPartyRate.value != null) {
                        val partyRateValue = calculatedPartyRate.value ?: 0.0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1FFF6), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // green tick icon
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Color(0xFF2EBF84),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rate submitted successfully", fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Display the user entered rate
                            Text(
                                text = "Your rate: ${userRateText.value.ifBlank { "—" }}",
                                fontWeight = FontWeight.Medium
                            )

                            // Display the calculated PartyRate (formatted)
                            val formattedPartyRate = try {
                                java.text.NumberFormat.getNumberInstance().run {
                                    maximumFractionDigits = 2
                                    minimumFractionDigits = 0
                                    this.format(partyRateValue)
                                }
                            } catch (e: Exception) {
                                partyRateValue.toString()
                            }

                            Text(
                                text = "Party Rate: ₹$formattedPartyRate",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Provide a small breakdown line
                            Text(
                                text = "Breakdown: freightChargeRate × netQuantityMT = PartyRate",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "freightChargeRate = ${userRateText.value}, netQuantityMT = $netQuantityMT",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

// ---------- End Enter Your Rate Card ----------


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -----------------------------
// Route selector & helper composables remain unchanged (copied from user's original implementation)
// -----------------------------
@Composable
fun RouteSelectorRows(onApplySelection: (originSelection: String?, destSelection: String?) -> Unit) {
    val TAG = "RouteSelectorRows"
    val green = Color(0xFF23A455)
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showUnloadingDialog by remember { mutableStateOf(false) }
    var loadingPlaceholder by remember { mutableStateOf<String?>(null) }
    var unloadingPlaceholder by remember { mutableStateOf<String?>(null) }

    val loadingCities = listOf("All NCR", "Central Delhi", "East Delhi", "New Delhi", "North Delhi", "North East Delhi", "North West Delhi", "Shahdara", "South Delhi", "South East Delhi", "South West Delhi", "AMBALA")
    val unloadingCities = listOf("Rajasthan", "Himachal Pradesh", "Nagaland", "Uttarakhand", "Andhra Pradesh", "Madhya Pradesh", "Lakshadweep", "Meghalaya", "Sikkim", "Kerala", "Chhattisgarh", "Tamil Nadu", "KOLKATA")

    val loadingChecked = remember { mutableStateListOf<Boolean>().apply { repeat(loadingCities.size) { add(false) } } }
    val unloadingChecked = remember { mutableStateListOf<Boolean>().apply { repeat(unloadingCities.size) { add(false) } } }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).shadow(4.dp, RoundedCornerShape(16.dp)).background(Color.White, RoundedCornerShape(16.dp)).padding(vertical = 16.dp, horizontal = 20.dp)) {
        RouteSelectorItem(label = "Loading", dotColor = Color(0xFF1EB980), placeholder = loadingPlaceholder ?: "कृपया शहर चुनें", onClick = { showLoadingDialog = true })
        Spacer(modifier = Modifier.height(10.dp))
        RouteSelectorItem(label = "Unloading", dotColor = Color(0xFFED5565), placeholder = unloadingPlaceholder ?: "कृपया शहर चुनें", onClick = { showUnloadingDialog = true })
        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showLoadingDialog) {
        Dialog(onDismissRequest = { showLoadingDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)) {
                            IconButton(onClick = { showLoadingDialog = false }) { Icon(imageVector = Icons.Default.Close, contentDescription = "Close") }
                            Text(text = "Select Loading", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 68.dp, bottom = 88.dp, start = 12.dp, end = 12.dp)) {
                            itemsIndexed(loadingCities) { idx, city ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                                    Checkbox(checked = loadingChecked[idx], onCheckedChange = { loadingChecked[idx] = it }, colors = CheckboxDefaults.colors(checkedColor = green, uncheckedColor = Color.Gray, checkmarkColor = Color.White))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(city)
                                }
                                Divider(color = Color(0xFFFAFAFA), thickness = 1.dp)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 18.dp, vertical = 18.dp)) {
                            Button(onClick = {
                                val sel = loadingCities.mapIndexedNotNull { i, c -> if (loadingChecked[i]) c else null }
                                loadingPlaceholder = if (sel.isEmpty()) null else sel.joinToString(", ")
                                Log.d(TAG, "Loading selected: $loadingPlaceholder")
                                showLoadingDialog = false
                                onApplySelection(loadingPlaceholder, unloadingPlaceholder)
                            }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                                Text(text = "View Loads", fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnloadingDialog) {
        Dialog(onDismissRequest = { showUnloadingDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000)))
                Surface(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp), color = Color.White, shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            IconButton(onClick = { showUnloadingDialog = false }) { Icon(imageVector = Icons.Default.Close, contentDescription = "Close") }
                            Text(text = "Select Unloading", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                            itemsIndexed(unloadingCities) { idx, city ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                                    Checkbox(checked = unloadingChecked[idx], onCheckedChange = { unloadingChecked[idx] = it }, colors = CheckboxDefaults.colors(checkedColor = green, uncheckedColor = Color.Gray, checkmarkColor = Color.White))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = city, fontSize = 16.sp)
                                }
                                Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                            Button(onClick = {
                                val sel = unloadingCities.mapIndexedNotNull { i, c -> if (unloadingChecked[i]) c else null }
                                unloadingPlaceholder = if (sel.isEmpty()) null else sel.joinToString(", ")
                                Log.d(TAG, "Unloading selected: $unloadingPlaceholder")
                                showUnloadingDialog = false
                                onApplySelection(loadingPlaceholder, unloadingPlaceholder)
                            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
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
fun RouteSelectorItem(label: String, dotColor: Color, placeholder: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = placeholder, fontSize = 13.sp, color = Color(0xFF6B6B6B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) }
    }
}
