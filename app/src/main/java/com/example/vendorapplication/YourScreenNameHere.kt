package com.example.vendorapplication

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full ready-to-drop Kotlin file.
 * File name: YourScreenNameHere.kt
 */

@Composable
fun YourScreenNameHere() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F6FA)
    ) {
        Column {
            TopRow()
            ChipsRow()
            // LazyColumn holds the scrollable content. Trimmed vertical padding to remove big top/bottom gaps.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) } // small top gap
                item { WalletCard() }
                // Reduced spacing of banner so it doesn't take too much vertical space
                item { SuperFastagBanner() }
                item {
                    FastagCard(
                        tag = "MAT563016M5G09...",
                        statusText = "Low Balance",
                        statusColor = Color(0xFFEF9A3C),
                        dateText = "22 Jul 24, 05:35 AM",
                        vehicleBalance = "₹ 0",
                        showRupeeRed = true
                    )
                }
                item {
                    FastagCard(
                        tag = "MB1A5ECD4RANR7347",
                        statusText = "Active",
                        statusColor = Color(0xFF10B981),
                        dateText = "03 Dec 25, 03:36 PM",
                        vehicleBalance = "₹ 500",
                        showRupeeRed = false
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) } // small bottom gap
            }
        }
    }
}

@Composable
private fun TopRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F6FA))
            // moved further down
            .padding(start = 12.dp, end = 12.dp, top = 26.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* menu */ }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF0F172A)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Image(
//                painter = painterResource(id = R.drawable.logocjdarcl1),
//                contentDescription = "WheelsEye logo",
//                modifier = Modifier
//                    .size(48.dp)   // increased size (28 → 36 → now 48)
//            )
        }

        IconButton(onClick = { /* call */ }) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = Color(0xFF0F172A)
            )
        }
    }
}


@Composable
private fun ChipsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SimpleChip(text = "FASTAG", selected = true)
        SimpleChip(text = "GPS")
        SimpleChip(text = "DIESEL")
        SimpleChip(text = "LOAD")
    }
}

@Composable
private fun SimpleChip(text: String, selected: Boolean = false) {
    Surface(
        color = if (selected) Color.White else Color(0xFFEFF4F9),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(36.dp)
            .clickable { /* handle click */ }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = text,
                fontSize = 13.sp,
                color = if (selected) Color(0xFF0F172A) else Color(0xFF334155),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WalletCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Column {
                        Text(
                            text = "Wallet Balance",
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "₹ 250",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { /* Add Money */ },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(text = "Add Money", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* view transactions */ }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = "transactions",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "View Transactions",
                    color = Color(0xFF475569),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = ">", color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun SuperFastagBanner() {
    // Reduced height and image size to prevent large empty gaps
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(90.dp),
        contentAlignment = Alignment.Center
    ) {
//        Image(
//            painter = painterResource(id = R.drawable.fastag3),
//            contentDescription = "FASTag Logo",
//            modifier = Modifier
//                .width(160.dp)
//                .height(104.dp),
//            contentScale = ContentScale.Fit
//        )
    }
}

@Composable
private fun FastagCard(
    tag: String,
    statusText: String,
    statusColor: Color,
    dateText: String,
    vehicleBalance: String,
    showRupeeRed: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFFAF0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("7", color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tag,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        // Only show the symbol for non-active states (keeps Active clean)
                        val prefix = if (statusText.equals("Active", ignoreCase = true)) "" else "⚠ "
                        Text(
                            text = "$prefix$statusText",
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "--", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = dateText, color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Vehicle Balance", color = Color(0xFF475569), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vehicleBalance,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showRupeeRed) Color(0xFFDC2626) else Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = { /* Recharge */ },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Recharge", color = Color(0xFF0F172A))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F6FA)
@Composable
fun YourScreenNameHerePreview() {
    MaterialTheme {
        YourScreenNameHere()
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun YourScreenNameHerePreviewLight() {
    MaterialTheme {
        YourScreenNameHere()
    }
}
