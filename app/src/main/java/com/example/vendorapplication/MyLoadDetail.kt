package com.example.vendorapplication

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxHeight as fillMaxHeightAlias

private val ScreenBackground = Color(0xFFF8F8F8)
private val PrimaryGreen = Color(0xFF1EA33A)    // large green CTA
private val PrimaryGreenDark = Color(0xFF16702A) // darker left pill accent
private val AccentBlue = Color(0xFF1071FF)      // small blue link text
private val CardBackground = Color(0xFFFFFFFF)
private val MutedText = Color(0xFF9EA3A8)
private val DividerGray = Color(0xFFE9E9E9)
private val SoftGray = Color(0xFFF3F4F6)
private val RedBorder = Color(0xFFDD2C2C)

@Composable
fun MyLoadDetailScreen() {
    // Dialog state holders
    val showCancelDialog = remember { mutableStateOf(false) }
    val showStartLoadingConfirmDialog = remember { mutableStateOf(false) }
    val showLoadingSuccessDialog = remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = ScreenBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top area (app bar & map)
            Box(modifier = Modifier.weight(0.44f)) {
                Column {
                    AppBar()
//                    MapPreviewArea()
                }
            }

            // Bottom sheet-like content (scrollable)
            Box(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxWidth()
            ) {
                // pass the click handlers to BottomContent
                BottomContent(
                    onCancelClick = { showCancelDialog.value = true },
                    onVehicleAtLoadingClick = { showStartLoadingConfirmDialog.value = true }
                )
                // NOTE: Floating CTA removed — button now sits inside BottomContent below "Cancel load".
            }
        }
    }

    // ----- Cancel confirmation dialog -----
    if (showCancelDialog.value) {
        AlertDialog(
            onDismissRequest = { showCancelDialog.value = false },
            title = {
                Text(
                    text = "Cancel load?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Cancelling now may incur a cancellation fee according to the platform policy. Are you sure you want to cancel this load?",
                    color = MutedText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("MyLoadDetail", "User confirmed cancel load")
                        // TODO: add cancellation logic here
                        showCancelDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Text(text = "Confirm Cancel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog.value = false }
                ) {
                    Text(text = "Keep Load", color = Color.Black)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
        )
    }

    // ----- Start loading confirmation dialog -----
    if (showStartLoadingConfirmDialog.value) {
        AlertDialog(
            onDismissRequest = { showStartLoadingConfirmDialog.value = false },
            title = {
                Text(
                    text = "Start loading?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Confirm when the vehicle is ready to begin loading at the specified loading point.",
                    color = MutedText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("MyLoadDetail", "User confirmed start loading")
                        // Hide the confirmation and immediately show success popup
                        showStartLoadingConfirmDialog.value = false
                        showLoadingSuccessDialog.value = true
                        // TODO: place any start-loading backend call here
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Text(text = "Start loading", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartLoadingConfirmDialog.value = false }
                ) {
                    Text(text = "Not yet", color = Color.Black)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
        )
    }

    // ----- Loading started success dialog -----
    if (showLoadingSuccessDialog.value) {
        AlertDialog(
            onDismissRequest = { showLoadingSuccessDialog.value = false },
            title = {
                Text(
                    text = "Loading started",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "The vehicle has started loading at the loading point.",
                    color = MutedText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("MyLoadDetail", "User acknowledged loading started")
                        showLoadingSuccessDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Text(text = "OK", color = Color.White)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
private fun AppBar() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()       // ⭐ prevents collision with phone notch/status bar
            .padding(top = 10.dp)      // ⭐ push a little more downward
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ◀️ Back Arrow
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(22.dp)
                .rotate(180f)
        )

        Spacer(Modifier.width(12.dp))

        // 🚚 Vehicle number + route text
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "HR47E2147",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Text(
                text = "Delhi - Karur District, Tamil ...",
                fontSize = 12.sp,
                color = MutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 📞 Call Button
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }

        // 🔵 Loading party (2 lines)
        Text(
            text = "Loading\nparty",
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = AccentBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}



//@Composable
//private fun MapPreviewArea() {
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.Top
//    ) {
//
//        // 🔹 THE MAP BOX (no more overlap)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(260.dp)
//                .padding(horizontal = 8.dp)
//                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
//                .background(Color(0xFFEFEFF0))
//        ) {
//            Canvas(modifier = Modifier.fillMaxSize()) {
//                drawRect(color = Color(0xFFF7F7F7))
//
//                val w = size.width
//                val h = size.height
//
//                for (i in 0..6) {
//                    val y = h * i / 7f
//                    drawLine(
//                        color = Color(0xFFEDDFA8),
//                        start = Offset(0f, y),
//                        end = Offset(w, y),
//                        strokeWidth = 3f
//                    )
//                }
//
//                drawCircle(
//                    color = Color(0xFFBDE5C8),
//                    radius = 10f,
//                    center = Offset(w * 0.75f, h * 0.7f)
//                )
//            }
//        }
//
//        // 🔹 Perfect spacing so it does NOT overlap
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // 🔹 THE BOTTOM CARD (time to reach)
//        Card(
//            modifier = Modifier
//                .padding(horizontal = 18.dp)
//                .height(88.dp)
//                .fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//            colors = CardDefaults.cardColors(containerColor = CardBackground)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp, vertical = 12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Time to reach loading",
//                        fontSize = 12.sp,
//                        color = MutedText
//                    )
//
//                    Spacer(Modifier.height(6.dp))
//
//                    Text(
//                        text = "Tomorrow Morning 8 o'clock",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                }
//
//                Box(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color(0xFFEFFAF0))
//                        .padding(horizontal = 10.dp, vertical = 6.dp)
//                ) {
//                    Text(
//                        text = "On time",
//                        fontSize = 12.sp,
//                        color = Color(0xFF2A9D3A),
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//            }
//        }
//    }
//}


@Composable
private fun BottomContent(
    onCancelClick: () -> Unit = {},
    onVehicleAtLoadingClick: () -> Unit = {}
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .verticalScroll(scroll)
    ) {
        // Loading address card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // small green dot icon
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color = Color(0xFF1EA33A), shape = CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Loading address",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Warehouse1, NEW DELHI, DELHI, 110081",
                            fontSize = 13.sp,
                            color = MutedText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Divider(color = DividerGray, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Send to driver",
                        fontSize = 14.sp,
                        color = AccentBlue,
                        modifier = Modifier.clickable { /* action */ }
                    )
                    Text(
                        text = "Check map",
                        fontSize = 14.sp,
                        color = AccentBlue,
                        modifier = Modifier.clickable { /* action */ }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Second screen area: vehicle & driver with change and cancel button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SoftGray, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .rotate(90f)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HR47E2147",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Vehicle number",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                    Text(
                        text = "Change",
                        color = AccentBlue,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SoftGray, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "9466342567 · Ajit Singh 2147",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Trip driver",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                    Text(
                        text = "Change",
                        color = AccentBlue,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Cancel load button (outlined red)
        OutlinedButton(
            onClick = { onCancelClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RedBorder
            ),

            border = BorderStroke(1.dp, RedBorder)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "cancel",
                    tint = RedBorder,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Cancel load", fontSize = 16.sp, color = RedBorder)
            }
        }

        Spacer(Modifier.height(12.dp))

        // <-- Placed the VehicleAtLoadingButton directly BELOW the Cancel load button as requested
        VehicleAtLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            onClick = { onVehicleAtLoadingClick() }
        )

        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(18.dp))

        // Trip ledger
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "Trip ledger", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            LedgerRow(label = "Freight", amount = "₹ 102000")
            LedgerRow(label = "Platform", amount = "- ₹ 999")
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = DividerGray, thickness = 1.dp)
            LedgerRow(label = "Net Amount", amount = "₹ 101001", labelBold = true)
            Spacer(Modifier.height(8.dp))
            LedgerRow(label = "Balance", amount = "₹ 101001")
        }

        Spacer(Modifier.height(18.dp))

        Divider(color = DividerGray, thickness = 8.dp)

        Spacer(Modifier.height(12.dp))

        // Trip summary with timeline bullets
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "Trip Summary", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // timeline column with dots and line
                Column(
                    modifier = Modifier
                        .width(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // First green dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color = PrimaryGreen, shape = CircleShape)
                    )
                    // vertical line
                    Spacer(Modifier.height(6.dp))
                    Canvas(modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                    ) {
                        drawLine(
                            color = Color(0xFFBBDCC4),
                            start = Offset(x = size.width / 2, y = 0f),
                            end = Offset(x = size.width / 2, y = size.height),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Program confirmed", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "9 Dec 2025, पु 11:08", fontSize = 12.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Reached loading - NORTH WEST DISTRICT, DELHI", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Reached at unloading - KARUR DISTRICT, TAMIL NADU", fontSize = 13.sp, color = Color.Black)
                }
            }
        }

        Spacer(Modifier.height(120.dp)) // space for bottom area
    }
}

@Composable
fun VehicleAtLoadingButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .background(CardBackground, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        // Left pill (darker)
        Box(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryGreenDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardDoubleArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Right main button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Vehicle is at loading",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun LedgerRow(label: String, amount: String, labelBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (labelBold) FontWeight.SemiBold else FontWeight.Normal,
            color = Color.Black
        )
        Text(
            text = amount,
            fontSize = 14.sp,
            fontWeight = if (labelBold) FontWeight.SemiBold else FontWeight.Normal,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true, name = "MyLoadDetail (Light)")
@Composable
fun PreviewMyLoadDetailLight() {
    MyLoadDetailScreen()
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "MyLoadDetail (Dark)")
@Composable
fun PreviewMyLoadDetailDark() {
    MyLoadDetailScreen()
}
