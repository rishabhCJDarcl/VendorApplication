//package com.example.vendorapplication
//
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.animation.*
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.graphics.TileMode
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.semantics.contentDescription
//import androidx.compose.ui.semantics.semantics
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import retrofit2.http.Headers
//import java.net.URLEncoder
//import kotlin.random.Random
//
//private const val TAG = "LoginScreenAPI"
//
//enum class LoginStep { WELCOME, VERIFY_OTP, PASSWORD }
//
//// Keep your logo url as-is (you provided it earlier)
//private const val LOGO_URL =
//    "https://tms-test.cjdarcl.com:8002/organisations/v1/admin/logo/by-domain?secret=Admin@Fretron&domain=logo:tms-test.cjdarcl.com"
//
//private const val OTP_BASE_URL = "https://apis.cjdarcl.com/route-redirect/sms/send-sms"
//
//private val httpClient = OkHttpClient()
//
///**
// * Performs a GET request on the IO dispatcher and returns the response body (or null).
// * Uses withContext(Dispatchers.IO) to avoid NetworkOnMainThreadException.
// */
//private suspend fun getRequest(url: String): String? {
//    return try {
//        Log.d(TAG, "API call started (getRequest)")
//        Log.d(TAG, "URL = $url")
//        val req = Request.Builder().url(url).get().build()
//
//        // Execute blocking network call on IO dispatcher
//        val responseString = withContext(Dispatchers.IO) {
//            val call = httpClient.newCall(req)
//            val res = call.execute()
//            res.use { resp ->
//                val body = resp.body?.string()
//                if (resp.isSuccessful) {
//                    if (body.isNullOrBlank()) {
//                        Log.d(TAG, "SUCCESS → response body empty")
//                    } else {
//                        Log.d(TAG, "SUCCESS → response received: ${body.take(1000)}")
//                    }
//                    body
//                } else {
//                    Log.d(
//                        TAG,
//                        "ERROR → code=${resp.code} msg=${resp.message} body=${body?.take(1000) ?: "null"}"
//                    )
//                    null
//                }
//            }
//        }
//
//        responseString
//    } catch (e: Exception) {
//        // Log full exception for diagnosis
//        Log.e(TAG, "EXCEPTION during HTTP request: ${e.message}", e)
//        null
//    }
//}
//
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun LoginScreen(
//    onLoginSuccess: () -> Unit,
//    onSignupSuccess: () -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    var step by remember { mutableStateOf(LoginStep.WELCOME) }
//    var isSignupMode by remember { mutableStateOf(false) }
//    var phone by remember { mutableStateOf("+91") }
//    var password by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var showPassword by remember { mutableStateOf(false) }
//    var agreed by remember { mutableStateOf(true) }
//
//    val otpLength = 6
//    var otp by remember { mutableStateOf(List(otpLength) { "" }) }
//    var otpTimer by remember { mutableStateOf(30) }
//    var loading by remember { mutableStateOf(false) }
//    var error by remember { mutableStateOf<String?>(null) }
//
//    // Store the last generated OTP for verification (kept in state)
//    var generatedOtp by remember { mutableStateOf<String?>(null) }
//
//    LaunchedEffect(step) {
//        if (step == LoginStep.VERIFY_OTP) {
//            otpTimer = 30
//            while (otpTimer > 0 && step == LoginStep.VERIFY_OTP) {
//                delay(1000)
//                otpTimer--
//            }
//        }
//    }
//
//    val bgGradient = Brush.verticalGradient(
//        colors = listOf(Color(0xFFF6F8FB), Color(0xFFEFF3F9)),
//        startY = 0f,
//        endY = 1000f,
//        tileMode = TileMode.Clamp
//    )
//    val cardShape = RoundedCornerShape(20.dp)
//
//    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(bgGradient)
//                .padding(padding)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 18.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.logocjdarcl1),
//                    contentDescription = "CJ Darcl Logo",
//                    modifier = Modifier
//                        .height(56.dp)
//                        .clip(CircleShape)
//                )
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 18.dp)
//                    .padding(top = 88.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                AnimatedContent(
//                    targetState = step,
//                    transitionSpec = {
//                        slideInHorizontally(animationSpec = tween(280)) + fadeIn() with
//                                slideOutHorizontally(animationSpec = tween(240), targetOffsetX = { -it }) + fadeOut()
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) { target ->
//                    when (target) {
//                        LoginStep.WELCOME -> {
//                            WelcomeCard(
//                                phone = phone,
//                                onPhoneChange = { phone = it },
//                                onSendOtp = {
//                                    if (phone.length >= 4 && agreed) {
//                                        scope.launch {
//                                            loading = true
//                                            error = null
//
//                                            // Generate a 6-digit OTP (100000 - 999999)
//                                            val code = Random.nextInt(100000, 1000000).toString()
//                                            generatedOtp = code // keep OTP available in state
//
//                                            // Prepare mobile number for API: remove any non-digit chars
//                                            val mobileForApi = phone.filter { it.isDigit() }
//                                                .takeIf { it.isNotBlank() } ?: phone
//
//                                            // Form the message exactly as requested
//                                            val plainMessage = "Your OTP for login is $code CJDARCL."
//
//                                            // URLEncode and replace + with %20 to match Postman style
//                                            val msg = URLEncoder.encode(plainMessage, "UTF-8").replace("+", "%20")
//                                            val url = "$OTP_BASE_URL?mobileNo=$mobileForApi&message=$msg"
//
//                                            // Detailed logs before sending
//                                            Log.d(TAG, "OTP_REQUEST_PREPARING → originalPhone='$phone'")
//                                            Log.d(TAG, "OTP_REQUEST_PREPARING → mobileForApi='$mobileForApi'")
//                                            Log.d(TAG, "OTP_REQUEST_PREPARING → generatedOtp='$code'")
//                                            Log.d(TAG, "OTP_REQUEST_PREPARING → message='$plainMessage'")
//                                            Log.d(TAG, "OTP_REQUEST_PREPARING → finalUrl='$url'")
//
//                                            val res = try {
//                                                getRequest(url)
//                                            } catch (e: Exception) {
//                                                Log.e(TAG, "EXCEPTION when calling getRequest: ${e.message}", e)
//                                                null
//                                            }
//
//                                            loading = false
//
//                                            if (!res.isNullOrBlank()) {
//                                                // Success
//                                                Log.d(TAG, "API_HIT_SUCCESS → response='${res}'")
//                                                Log.d(TAG, "OTP_SENT_SUCCESS → OTP available in state")
//                                                // reset otp input boxes (UI still has otpLength boxes)
//                                                otp = List(otpLength) { "" }
//                                                step = LoginStep.VERIFY_OTP
//                                            } else {
//                                                // Failure
//                                                Log.d(TAG, "API_HIT_FAILED → response null/blank")
//                                                Log.d(TAG, "OTP_SENT_FAILED → Could not send OTP to $mobileForApi (original: $phone)")
//                                                error = "Failed to send OTP"
//                                            }
//                                        }
//                                    } else {
//                                        Log.d(TAG, "OTP_REQUEST_NOT_TRIGGERED → phone too short or not agreed")
//                                    }
//                                },
//                                onContinueWithPassword = {
//                                    isSignupMode = false
//                                    step = LoginStep.PASSWORD
//                                },
//                                agreed = agreed,
//                                onAgreeToggle = { agreed = it },
//                                onSignupNow = {
//                                    isSignupMode = true
//                                    step = LoginStep.PASSWORD
//                                },
//                                cardShape = cardShape
//                            )
//                        }
//
//                        LoginStep.VERIFY_OTP -> {
//                            VerifyOtpCard(
//                                maskedPhone = phone,
//                                otp = otp,
//                                onOtpChange = { i, v ->
//                                    if (v.length <= 1) {
//                                        otp = otp.toMutableList().also { it[i] = v }
//                                    }
//                                },
//                                onBack = { step = LoginStep.WELCOME },
//                                onLogin = {
//                                    if (otp.joinToString("").length == otpLength) {
//                                        scope.launch {
//                                            loading = true
//                                            error = null
//                                            // For demo auth we call LOGO_URL. Replace with real auth when ready.
//                                            val res = getRequest(LOGO_URL)
//                                            loading = false
//                                            if (!res.isNullOrBlank()) {
//                                                Log.d(TAG, "SUCCESS → login verified")
//                                                onLoginSuccess()
//                                            } else error = "Login failed"
//                                        }
//                                    }
//                                },
//                                timerSeconds = otpTimer,
//                                onResend = {
//                                    otp = List(otpLength) { "" }
//                                    otpTimer = 30
//                                    // Optionally re-trigger OTP send flow here if desired
//                                    Log.d(TAG, "USER_REQUESTED_RESEND → otp cleared and timer reset")
//                                },
//                                cardShape = cardShape
//                            )
//                        }
//
//                        LoginStep.PASSWORD -> {
//                            PasswordCard(
//                                phone = phone,
//                                onPhoneChange = { phone = it },
//                                password = password,
//                                onPasswordChange = { password = it },
//                                name = name,
//                                onNameChange = { name = it },
//                                showPassword = showPassword,
//                                onTogglePassword = { showPassword = !showPassword },
//                                isSignup = isSignupMode,
//                                onBack = {
//                                    isSignupMode = false
//                                    step = LoginStep.WELCOME
//                                },
//                                onLogin = {
//                                    if (phone.length >= 4 && password.length >= 6) {
//                                        scope.launch {
//                                            loading = true
//                                            error = null
//                                            val res = getRequest(LOGO_URL)
//                                            loading = false
//                                            if (!res.isNullOrBlank()) {
//                                                Log.d(TAG, "SUCCESS → auth ok")
//                                                if (isSignupMode) onSignupSuccess() else onLoginSuccess()
//                                            } else error = "Auth failed"
//                                        }
//                                    }
//                                },
//                                onForgotPassword = {},
//                                cardShape = cardShape
//                            )
//                        }
//                    }
//                }
//
//                if (!error.isNullOrBlank()) {
//                    Spacer(Modifier.height(12.dp))
//                    Text(error!!, color = Color.Red, fontSize = 13.sp)
//                }
//            }
//
//            if (loading) {
//                Box(
//                    Modifier
//                        .fillMaxSize()
//                        .background(Color(0x55000000)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun WelcomeCard(
//    phone: String,
//    onPhoneChange: (String) -> Unit,
//    onSendOtp: () -> Unit,
//    onContinueWithPassword: () -> Unit,
//    agreed: Boolean,
//    onAgreeToggle: (Boolean) -> Unit,
//    onSignupNow: () -> Unit,
//    cardShape: Shape
//) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Surface(
//            shape = cardShape,
//            tonalElevation = 8.dp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .shadow(6.dp, cardShape)
//        ) {
//            Column(modifier = Modifier.padding(20.dp)) {
//                Text("Login to your account", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
//                Spacer(Modifier.height(12.dp))
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = onPhoneChange,
//                    modifier = Modifier.fillMaxWidth().height(56.dp),
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                    shape = RoundedCornerShape(14.dp)
//                )
//                Spacer(Modifier.height(14.dp))
//                GradientButton("Send OTP", onSendOtp, Modifier.fillMaxWidth().height(52.dp))
//                Spacer(Modifier.height(10.dp))
//                OutlinedButton(
//                    onClick = onContinueWithPassword,
//                    modifier = Modifier.fillMaxWidth().height(50.dp),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Text("Continue with password")
//                }
//                Spacer(Modifier.height(12.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Checkbox(checked = agreed, onCheckedChange = onAgreeToggle)
//                    Spacer(Modifier.width(8.dp))
//                    Text("I agree to receive updates.", fontSize = 12.sp)
//                }
//            }
//        }
//        Spacer(Modifier.height(26.dp))
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//            Text("Don't have an account? ")
//            Text(
//                "Sign up now",
//                color = Color(0xFF2A6CF1),
//                modifier = Modifier.clickable { onSignupNow() },
//                fontWeight = FontWeight.Medium
//            )
//        }
//    }
//}
//
//@Composable
//private fun VerifyOtpCard(
//    maskedPhone: String,
//    otp: List<String>, // MUST be size = 6
//    onOtpChange: (Int, String) -> Unit,
//    onBack: () -> Unit,
//    onLogin: () -> Unit,
//    timerSeconds: Int,
//    onResend: () -> Unit,
//    cardShape: Shape
//) {
//    val focusRequesters = remember {
//        List(6) { FocusRequester() }
//    }
//
//    Surface(
//        shape = cardShape,
//        tonalElevation = 8.dp,
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(6.dp, cardShape)
//    ) {
//        Column(modifier = Modifier.padding(18.dp)) {
//
//            IconButton(onClick = onBack) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "back")
//            }
//
//            Text(
//                text = "Verify your phone number",
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 20.sp
//            )
//
//            Text(
//                text = "Enter the OTP sent to $maskedPhone",
//                fontSize = 13.sp
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // ================= OTP BOXES =================
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                repeat(6) { index ->
//                    OutlinedTextField(
//                        value = otp[index],
//                        onValueChange = { value ->
//                            if (value.length <= 1 && value.all { it.isDigit() }) {
//                                onOtpChange(index, value)
//
//                                // Auto move to next
//                                if (value.isNotEmpty() && index < 5) {
//                                    focusRequesters[index + 1].requestFocus()
//                                }
//
//                                // Move back on delete
//                                if (value.isEmpty() && index > 0) {
//                                    focusRequesters[index - 1].requestFocus()
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .size(56.dp)
//                            .focusRequester(focusRequesters[index])
//                            .semantics { contentDescription = "otp_$index" },
//                        singleLine = true,
//                        textStyle = LocalTextStyle.current.copy(
//                            textAlign = TextAlign.Center,
//                            fontSize = 18.sp
//                        ),
//                        keyboardOptions = KeyboardOptions(
//                            keyboardType = KeyboardType.Number,
//                            imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
//                        ),
//                        shape = RoundedCornerShape(10.dp)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(18.dp))
//
//            // ================= LOGIN BUTTON =================
//            GradientButton(
//                text = "Login",
//                onClick = onLogin,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(52.dp)
//            )
//
//            Spacer(Modifier.height(10.dp))
//
//            // ================= RESEND =================
//            if (timerSeconds > 0) {
//                Text(
//                    text = "Resend in 0:${timerSeconds.toString().padStart(2, '0')}",
//                    fontSize = 13.sp
//                )
//            } else {
//                Text(
//                    text = "Resend OTP",
//                    color = Color(0xFF2A6CF1),
//                    fontSize = 13.sp,
//                    modifier = Modifier.clickable { onResend() }
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//private fun PasswordCard(
//    phone: String,
//    onPhoneChange: (String) -> Unit,
//    password: String,
//    onPasswordChange: (String) -> Unit,
//    name: String,
//    onNameChange: (String) -> Unit,
//    showPassword: Boolean,
//    onTogglePassword: () -> Unit,
//    isSignup: Boolean,
//    onBack: () -> Unit,
//    onLogin: () -> Unit,
//    onForgotPassword: () -> Unit,
//    cardShape: androidx.compose.ui.graphics.Shape
//) {
//    var email by remember { mutableStateOf("") }
//    var otp by remember { mutableStateOf("") }
//    var captchaChecked by remember { mutableStateOf(false) }
//    var loading by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    // OkHttp client can be reused
//    val client = remember { OkHttpClient() }
//
//    // suspend function to call the provided curl endpoint (GET)
//    suspend fun callLogoApi(): Pair<Int, String> {
//        // URL from your curl (exact)
//        val url =
//            "https://tms-test.cjdarcl.com:8002/organisations/v1/admin/logo/by-domain?secret=Admin@Fretron&domain=logo:tms-test.cjdarcl.com"
//        val headers = okhttp3.Headers.Builder()
//            .add("Accept", "application/json, text/plain, */*")
//            .add("Accept-Language", "en-US,en;q=0.9")
//            .add("Connection", "keep-alive")
//            .add("Content-Type", "application/json")
//            .add("Origin", "https://tms-test.cjdarcl.com:4200")
//            .add("Referer", "https://tms-test.cjdarcl.com:4200/")
//            .add("Sec-Fetch-Dest", "empty")
//            .add("Sec-Fetch-Mode", "cors")
//            .add("Sec-Fetch-Site", "same-site")
//            .add(
//                "User-Agent",
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36"
//            )
//            .add("sec-ch-ua", "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"")
//            .add("sec-ch-ua-mobile", "?0")
//            .add("sec-ch-ua-platform", "\"Windows\"")
//            .build()
//
//        val request = Request.Builder()
//            .url(url)
//            .get()
//            .headers(headers)
//            .build()
//
//        val response = client.newCall(request).execute()
//        val code = response.code
//        val body = response.body?.string() ?: ""
//        response.close()
//        return Pair(code, body)
//    }
//
//    Surface(
//        shape = cardShape,
//        tonalElevation = 8.dp,
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(6.dp, cardShape)
//    ) {
//        Column(modifier = Modifier.padding(18.dp)) {
//
//            IconButton(onClick = onBack) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "back")
//            }
//
//            Text(
//                text = "Sign Up",
//                fontWeight = FontWeight.Bold,
//                fontSize = 26.sp
//            )
//
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                text = "Create New Account",
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 16.sp,
//                color = Color.Gray
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // Name
//            Text("Enter Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
//            Spacer(Modifier.height(6.dp))
//            OutlinedTextField(
//                value = name,
//                onValueChange = onNameChange,
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                singleLine = true,
//                shape = RoundedCornerShape(14.dp)
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // Email
//            Text("Enter Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
//            Spacer(Modifier.height(6.dp))
//            OutlinedTextField(
//                value = email,
//                onValueChange = { email = it },
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
//                shape = RoundedCornerShape(14.dp)
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // Phone + Send OTP
//            Text("+91 Enter Mobile Number", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
//            Spacer(Modifier.height(6.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = onPhoneChange,
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp),
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                    shape = RoundedCornerShape(14.dp)
//                )
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = "Send OTP",
//                    color = Color(0xFF2A6CF1),
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier
//                        .clickable {
//                            // Send OTP action placeholder - you can trigger your OTP flow here.
//                            Toast.makeText(context, "Send OTP clicked", Toast.LENGTH_SHORT).show()
//                        }
//                        .padding(6.dp)
//                )
//
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            // OTP
//            Text("Enter OTP", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
//            Spacer(Modifier.height(6.dp))
//            OutlinedTextField(
//                value = otp,
//                onValueChange = { otp = it },
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                shape = RoundedCornerShape(14.dp)
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // Password
//            Text("Create Password", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
//            Spacer(Modifier.height(6.dp))
//            OutlinedTextField(
//                value = password,
//                onValueChange = onPasswordChange,
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                singleLine = true,
//                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = onTogglePassword) {
//                        Icon(
//                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                            contentDescription = null
//                        )
//                    }
//                },
//                shape = RoundedCornerShape(14.dp)
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // Fake reCAPTCHA
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp)
//                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
//                    .padding(horizontal = 12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Checkbox(
//                    checked = captchaChecked,
//                    onCheckedChange = { captchaChecked = it }
//                )
//                Spacer(Modifier.width(8.dp))
//                Text("I'm not a robot", fontWeight = FontWeight.SemiBold)
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // Sign Up button -> calls API when clicked
//            GradientButton(
//                text = if (loading) "Please wait..." else "Sign Up",
//                enabled = !loading,
//                onClick = {
//                    if (!captchaChecked) {
//                        Toast.makeText(context, "Please verify reCAPTCHA", Toast.LENGTH_SHORT).show()
//                        return@GradientButton
//                    }
//                    // Basic validation example - ensure phone/email/password are present
//                    if (phone.isBlank() || email.isBlank() || password.isBlank() || name.isBlank()) {
//                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                        return@GradientButton
//                    }
//
//                    scope.launch {
//                        loading = true
//                        try {
//                            val (code, body) = withContext(Dispatchers.IO) { callLogoApi() }
//                            if (code in 200..299) {
//                                Toast.makeText(context, "API success: $code", Toast.LENGTH_LONG).show()
//                                // on success, you may want to call onLogin() or onSignup() as required
//                                onLogin()
//                            } else {
//                                // show first 200 chars of body for debug; trim for safety
//                                val snippet = if (body.length > 200) body.substring(0, 200) + "..." else body
//                                Toast.makeText(context, "API error $code: $snippet", Toast.LENGTH_LONG).show()
//                            }
//                        } catch (e: Exception) {
//                            Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//                        } finally {
//                            loading = false
//                        }
//                    }
//                },
//                modifier = Modifier.fillMaxWidth().height(52.dp)
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            // Bottom Login Box
//            Surface(
//                shape = RoundedCornerShape(10.dp),
//                tonalElevation = 2.dp,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text("Already have an account? ", color = Color.Gray)
//                    Text(
//                        "Login",
//                        color = Color(0xFF2A6CF1),
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.clickable { onLogin() }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun GradientButton(
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    corner: Dp = 12.dp,
//    enabled: Boolean = true
//) {
//    val gradient = Brush.horizontalGradient(
//        listOf(Color(0xFF2A6CF1), Color(0xFF4B8CFF))
//    )
//    val shape = RoundedCornerShape(corner)
//
//    if (enabled) {
//        Box(
//            modifier = modifier
//                .clip(shape)
//                .background(brush = gradient)
//                .clickable { onClick() }
//                .graphicsLayer {
//                    shadowElevation = 8f
//                    this.shape = shape
//                    clip = true
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
//        }
//    } else {
//        // disabled appearance
//        Box(
//            modifier = modifier
//                .clip(shape)
//                .background(color = Color(0xFFDDDDDD))
//                .graphicsLayer {
//                    shadowElevation = 2f
//                    this.shape = shape
//                    clip = true
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
//        }
//    }
//}
//
//@Preview(showBackground = true, widthDp = 360, heightDp = 800)
//@Composable
//fun LoginScreenPreview() {
//    MaterialTheme {
//        // You'll need a LoginScreen composable in your file - replace with minimal scaffold if needed
//        // For previewing only, call PasswordCard directly with dummy callbacks:
//        PasswordCard(
//            phone = "",
//            onPhoneChange = {},
//            password = "",
//            onPasswordChange = {},
//            name = "",
//            onNameChange = {},
//            showPassword = false,
//            onTogglePassword = {},
//            isSignup = true,
//            onBack = {},
//            onLogin = {},
//            onForgotPassword = {},
//            cardShape = RoundedCornerShape(16.dp)
//        )
//    }
//}
package com.example.vendorapplication

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import kotlin.random.Random

private const val TAG_LOGIN = "LOGIN_FLOW"
private const val TAG_OTP = "OTP_API"
private const val TAG_SIGNUP = "SIGNUP_API"
private const val TAG_NAV = "NAVIGATION"
private const val TAG_API = "API_DEBUG"

enum class LoginStep { WELCOME, VERIFY_OTP, PASSWORD }

private const val LOGO_URL =
    "https://tms-test.cjdarcl.com:8002/organisations/v1/admin/logo/by-domain?secret=Admin@Fretron&domain=logo:tms-test.cjdarcl.com"

private const val OTP_BASE_URL =
    "https://apis.cjdarcl.com/route-redirect/sms/send-sms"

private val httpClient = OkHttpClient()

private suspend fun getRequest(url: String): String? {
    return try {
        Log.d(TAG_API, "GET → $url")
        val req = Request.Builder().url(url).get().build()
        withContext(Dispatchers.IO) {
            httpClient.newCall(req).execute().use {
                val body = it.body?.string()
                Log.d(TAG_API, "Response code=${it.code}")
                Log.d(TAG_API, "Response body=${body?.take(1000)}")
                if (it.isSuccessful) body else null
            }
        }
    } catch (e: Exception) {
        Log.e(TAG_API, "Exception ${e.message}", e)
        null
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onSignupSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var step by remember { mutableStateOf(LoginStep.WELCOME) }
    var isSignupMode by remember { mutableStateOf(false) }

    var phone by remember { mutableStateOf("+91") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(true) }

    val otpLength = 6
    var otp by remember { mutableStateOf(List(otpLength) { "" }) }
    var otpTimer by remember { mutableStateOf(30) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var generatedOtp by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(step) {
        if (step == LoginStep.VERIFY_OTP) {
            otpTimer = 30
            while (otpTimer > 0 && step == LoginStep.VERIFY_OTP) {
                delay(1000)
                otpTimer--
            }
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF6F8FB), Color(0xFFEFF3F9)),
        startY = 0f,
        endY = 1000f,
        tileMode = TileMode.Clamp
    )

    val cardShape = RoundedCornerShape(20.dp)

    Scaffold { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logocjdarcl1),
                    contentDescription = "CJ Darcl Logo",
                    modifier = Modifier.height(56.dp).clip(CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(top = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally(tween(280)) + fadeIn() with
                                slideOutHorizontally(tween(240)) + fadeOut()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { target ->

                    when (target) {

                        LoginStep.WELCOME -> WelcomeCard(
                            phone = phone,
                            onPhoneChange = { phone = it },
                            agreed = agreed,
                            onAgreeToggle = { agreed = it },
                            cardShape = cardShape,
                            onSendOtp = {
                                if (!agreed) {
                                    Toast.makeText(context, "Please accept terms", Toast.LENGTH_SHORT).show()
                                    return@WelcomeCard
                                }

                                scope.launch {
                                    loading = true
                                    error = null

                                    val code = Random.nextInt(100000, 999999).toString()
                                    generatedOtp = code

                                    val mobile = phone.filter { it.isDigit() }
                                    val msg =
                                        URLEncoder.encode(
                                            "Your OTP for login is $code CJDARCL.",
                                            "UTF-8"
                                        ).replace("+", "%20")

                                    val url = "$OTP_BASE_URL?mobileNo=$mobile&message=$msg"

                                    Log.d(TAG_OTP, "Generated OTP=$code")
                                    Log.d(TAG_OTP, "OTP URL=$url")

                                    val res = getRequest(url)
                                    loading = false

                                    if (res != null) {
                                        Log.d(TAG_OTP, "OTP sent successfully")
                                        otp = List(otpLength) { "" }
                                        step = LoginStep.VERIFY_OTP
                                    } else {
                                        error = "Failed to send OTP"
                                    }
                                }
                            },
                            onContinueWithPassword = {
                                isSignupMode = false
                                step = LoginStep.PASSWORD
                            },
                            onSignupNow = {
                                isSignupMode = true
                                step = LoginStep.PASSWORD
                            }
                        )

                        LoginStep.VERIFY_OTP -> VerifyOtpCard(
                            maskedPhone = phone,
                            otp = otp,
                            onOtpChange = { i, v ->
                                otp = otp.toMutableList().also { it[i] = v }
                            },
                            onBack = { step = LoginStep.WELCOME },
                            timerSeconds = otpTimer,
                            onResend = {
                                otp = List(otpLength) { "" }
                                otpTimer = 30
                            },
                            cardShape = cardShape,
                            onLogin = {
                                val entered = otp.joinToString("")
                                Log.d(TAG_OTP, "Entered OTP=$entered, Expected=$generatedOtp")

                                if (entered != generatedOtp) {
                                    error = "Invalid OTP"
                                    return@VerifyOtpCard
                                }

                                scope.launch {
                                    loading = true
                                    Log.d(TAG_LOGIN, "Checking TMS user for phone=$phone")
                                    val res = getRequest(LOGO_URL)
                                    loading = false

                                    if (res != null) {
                                        Log.d(TAG_LOGIN, "TMS user exists")
                                        Log.d(TAG_NAV, "Navigating after login")
                                        onLoginSuccess(phone)
                                    } else {
                                        Log.d(TAG_LOGIN, "TMS user NOT found")
                                        Toast.makeText(
                                            context,
                                            "Account not found. Please sign up.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        isSignupMode = true
                                        step = LoginStep.PASSWORD
                                    }
                                }
                            }
                        )

                        LoginStep.PASSWORD -> PasswordCard(
                            phone = phone,
                            onPhoneChange = { phone = it },
                            password = password,
                            onPasswordChange = { password = it },
                            name = name,
                            onNameChange = { name = it },
                            showPassword = showPassword,
                            onTogglePassword = { showPassword = !showPassword },
                            isSignup = isSignupMode,
                            onBack = {
                                isSignupMode = false
                                step = LoginStep.WELCOME
                            },
                            cardShape = cardShape,
                            onLogin = {
                                if (!isSignupMode) {
                                    Toast.makeText(context, "Please login via OTP", Toast.LENGTH_SHORT).show()
                                    return@PasswordCard
                                }

                                scope.launch {
                                    loading = true
                                    Log.d(TAG_SIGNUP, "Signup request name=$name phone=$phone")
                                    val res = getRequest(LOGO_URL)
                                    loading = false

                                    if (res != null) {
                                        Log.d(TAG_SIGNUP, "Signup success")
                                        Toast.makeText(
                                            context,
                                            "Account created successfully. Please login.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onSignupSuccess()
                                        step = LoginStep.WELCOME
                                    } else {
                                        error = "Signup failed"
                                    }
                                }
                            },
                            onForgotPassword = {}
                        )
                    }
                }

                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = Color.Red, fontSize = 13.sp)
                }
            }

            if (loading) {
                Box(
                    Modifier.fillMaxSize().background(Color(0x55000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/* ---------------- UI COMPONENTS (UNCHANGED STRUCTURE) ---------------- */

@Composable
private fun WelcomeCard(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onContinueWithPassword: () -> Unit,
    agreed: Boolean,
    onAgreeToggle: (Boolean) -> Unit,
    onSignupNow: () -> Unit,
    cardShape: Shape
) {
    Column {
        Surface(
            shape = cardShape,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().shadow(6.dp, cardShape)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Login to your account", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(Modifier.height(14.dp))
                GradientButton("Send OTP", onSendOtp, Modifier.fillMaxWidth().height(52.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onContinueWithPassword,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Continue with password") }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = agreed, onCheckedChange = onAgreeToggle)
                    Text("I agree to receive updates.", fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Don't have an account? ")
            Text(
                "Sign up now",
                color = Color(0xFF2A6CF1),
                modifier = Modifier.clickable { onSignupNow() }
            )
        }
    }
}

@Composable
private fun VerifyOtpCard(
    maskedPhone: String,
    otp: List<String>,
    onOtpChange: (Int, String) -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    timerSeconds: Int,
    onResend: () -> Unit,
    cardShape: Shape
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Surface(
        shape = cardShape,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().shadow(6.dp, cardShape)
    ) {
        Column(Modifier.padding(18.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }

            Text("Verify your phone number", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Enter the OTP sent to $maskedPhone", fontSize = 13.sp)

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { index ->
                    OutlinedTextField(
                        value = otp[index],
                        onValueChange = {
                            if (it.length <= 1 && it.all(Char::isDigit)) {
                                onOtpChange(index, it)
                                if (it.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier.size(56.dp).focusRequester(focusRequesters[index]),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            GradientButton("Login", onLogin, Modifier.fillMaxWidth().height(52.dp))

            Spacer(Modifier.height(10.dp))
            if (timerSeconds > 0) {
                Text("Resend in 0:${timerSeconds.toString().padStart(2, '0')}", fontSize = 13.sp)
            } else {
                Text(
                    "Resend OTP",
                    color = Color(0xFF2A6CF1),
                    modifier = Modifier.clickable { onResend() }
                )
            }
        }
    }
}

@Composable
private fun PasswordCard(
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    isSignup: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    cardShape: Shape
) {
    Surface(
        shape = cardShape,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().shadow(6.dp, cardShape)
    ) {
        Column(Modifier.padding(18.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }

            Text(
                if (isSignup) "Sign Up" else "Login",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(Modifier.height(12.dp))

            if (isSignup) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") }
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone") }
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                visualTransformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null
                        )
                    }
                }
            )

            Spacer(Modifier.height(16.dp))
            GradientButton(
                if (isSignup) "Sign Up" else "Login",
                onLogin,
                Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    corner: Dp = 12.dp,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF2A6CF1), Color(0xFF4B8CFF)))
    val shape = RoundedCornerShape(corner)

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = enabled) { onClick() }
            .graphicsLayer { shadowElevation = 8f },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = {},
            onSignupSuccess = {}
        )
    }
}
