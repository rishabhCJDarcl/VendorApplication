package com.example.vendorapplication

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Shape

// Updated ProfileScreen.kt — includes the three settings rows inside the large white card
// Replace any placeholder drawables (if used) with actual resources as needed.

private val AppBackground = Color(0xFFFCF8FB)
private val TopHeader = Color(0xFFF2F8EA)
private val ScoreCardBg = Color(0xFFE8F3C8)
private val PrimaryGreen = Color(0xFF178C32)
private val ScoreNumberColor = Color(0xFF2F7D2E)
private val CardWhite = Color(0xFFFFFFFF)
private val BottomBarBg = Color(0xFFF6EDF3)
private val MutedText = Color(0xFF6D6D6D)
private val OrangeBadge = Color(0xFFFF8A00)

@Composable
fun ProfileScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        topBar = { ProfileTopBar() },
        bottomBar = { ProfileBottomBar() },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            HeaderContent()
            Spacer(modifier = Modifier.height(12.dp))
            ScoreCard(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            ActionsRow()
            Spacer(modifier = Modifier.height(16.dp))

            // Big white card that holds the settings rows (language + others)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(1.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Language row
                    SettingsItem(
                        leading = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        title = "भाषा बदले",
                        subtitle = "हिंदी, इंग्लिश, कन्नड..."
                    )

                    Divider(modifier = Modifier.padding(start = 68.dp)) // align divider after icon

                    // Load policy row with orange "नए बदलाव" badge + subtitle
                    SettingsItem(
                        leading = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        title = "लोड पॉलिसी",
                        subtitle = "कैंसिलेशन शुल्क, टोकन रिफंड पॉलिसी, डिटेल्स...",
                        trailingBadge = {
                            Text(
                                text = "नए बदलाव",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .background(OrangeBadge, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    )

                    Divider(modifier = Modifier.padding(start = 68.dp))

                    // MSME info row
                    SettingsItem(
                        leading = { Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        title = "MSME की जानकारी",
                        subtitle = "MSME सर्टिफिकेट अपलोड"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfileTopBar() {
    Surface(
        color = TopHeader,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* back */ }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

@Composable
private fun HeaderContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0))
                .border(2.dp, CardWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "avatar", modifier = Modifier.size(36.dp), tint = Color(0xFFBDBDBD))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Satender Singh", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2AA84A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "verified", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "9729790996", fontSize = 13.sp, color = MutedText)
        }
    }
}

@Composable
private fun ScoreCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(150.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ScoreCardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "शानदार सर्विस", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF222222))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "लोड में आपको प्राथमिकता दी जाएगी", fontSize = 13.sp, color = Color(0xFF4C4C4C))
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(88.dp)) {
                            val stroke = Stroke(width = 10f, cap = StrokeCap.Round)
                            val start = 160f
                            val sweep = 220f
                            drawArc(color = Color(0xFFCFE8A9), startAngle = start, sweepAngle = sweep, useCenter = false, style = stroke, size = Size(size.width, size.height))
                            drawArc(color = ScoreNumberColor, startAngle = start, sweepAngle = sweep * 0.96f, useCenter = false, style = stroke, size = Size(size.width, size.height))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "96", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = ScoreNumberColor)
                            Text(text = "/100", fontSize = 12.sp, color = Color(0xFF4B4B4B))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { /* score details */ },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .weight(1f)
                    ) {
                        Text(text = "अपने स्कोर", color = Color.White, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.width(100.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF6F7EE))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color(0xFFF2CBAA), radius = size.minDimension * 0.22f, center = Offset(size.width * 0.75f, size.height * 0.28f))
                        drawCircle(Color(0xFFDCF3D1), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.32f, size.height * 0.66f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallActionCard(icon = Icons.Default.ReceiptLong, title = "रेट लिस्ट", modifier = Modifier.weight(1f))
        SmallActionCard(icon = Icons.Default.LocalShipping, title = "आपकी गाड़ियाँ", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SmallActionCard(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(92.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF7F7F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFF6E6E6E))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
        }
    }
}

/**
 * SettingsItem with optional trailingBadge, used inside the big white card.
 * leading: composable for the icon
 * title, subtitle: text shown
 * trailingBadge: optional small pill composable (e.g. "नए बदलाव")
 */
@Composable
private fun SettingsItem(
    leading: @Composable (() -> Unit),
    title: String,
    subtitle: String,
    trailingBadge: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            leading()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF111111))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 13.sp, color = MutedText)
        }
        if (trailingBadge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingBadge()
        }
        Spacer(modifier = Modifier.width(10.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = "more", tint = Color(0xFF9E9E9E))
    }
}

@Composable
private fun ProfileBottomBar() {
    NavigationBar(
        containerColor = BottomBarBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
    ) {
        BottomNavItem(icon = Icons.Default.LocalShipping, label = "View Load", selected = false)
        BottomNavItem(icon = Icons.Default.DirectionsCar, label = "Empty Truck", selected = false)
        BottomNavItem(icon = Icons.Default.ReceiptLong, label = "Trips", selected = false)
        BottomNavItem(icon = Icons.Default.Verified, label = "Vehicle KYC", selected = false)
        BottomNavItem(icon = Icons.Default.Person, label = "Account", selected = true)
    }
}

@Composable
private fun RowScope.BottomNavItem(icon: ImageVector, label: String, selected: Boolean) {
    NavigationBarItem(
        selected = selected,
        onClick = { /* navigate */ },
        icon = {
            if (selected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEDDFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp), tint = Color(0xFF3B2C52))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2B203F)))
                    }
                }
            } else {
                Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp), tint = Color(0xFF7B6F7C))
            }
        },
        label = {
            Text(text = label, fontSize = 12.sp, color = if (selected) Color(0xFF3B2C52) else Color(0xFF7B6F7C))
        },
        alwaysShowLabel = true
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
            ProfileScreen()
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ProfileScreenPreviewDark() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
            ProfileScreen()
        }
    }
}
