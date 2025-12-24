package com.example.vendorapplication

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.PaddingValues


import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * EmptyTruck.kt
 *
 * Recreates the UI from the supplied screenshot.
 * - package: com.example.vendorapplication (required by the user)
 * - Uses Jetpack Compose (Material3)
 *
 * Notes:
 * - This file draws a small stylized orange truck illustration using Canvas so it doesn't
 *   require external drawable assets. The art is intentionally simple but positioned and
 *   colored to visually match the screenshot's orange truck on the header and tiny avatar.
 * - All interactive elements include onClick callbacks (no-op by default). Replace these
 *   with navigation or real handlers in your app.
 */

/** --------------------
 * Colors & dimensions
 * ---------------------*/
private val GradientStart = Color(0xFFE8F6C9)
private val GradientEnd = Color(0xFF9CC74A)
private val HeaderTextColor = Color(0xFF263221)
private val SubtitleGray = Color(0xFFBDBDBD)
private val CardBackground = Color.White
private val CardBorder = Color(0xFFF0F0F0)
private val NotAvailableRed = Color(0xFFEF6C6C)
private val EmptyGreen = Color(0xFF43A047)
private val BottomBarBg = Color(0xFFFFFFFF)

@Immutable
data class TruckModel(
    val id: String,
    val meta: String,
    val isEmpty: Boolean = false
)

@Composable
private fun sampleTrucks() = listOf(
    TruckModel("HR47E7551", "CONTAINER • 6 Tyre • 9.5 Ton • 32.0 Ft • 9.5 Ft"),
    TruckModel("HR47F5121", "CONTAINER • 6 Tyre • 6.0 Ton • 32.0 Ft • 10.0 Ft"),
    TruckModel("HR47F6264", "CONTAINER • 6 Tyre • 10.0 Ton • 32.0 Ft • 9.5 Ft")
)

/** --------------------
 * Top header with gradient and truck art
 * ---------------------*/
@Composable
private fun Header(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val headerHeight = 170.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = HeaderTextColor
            )
        }

        // Title - multiline left aligned
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
        ) {
            Text(
                text = "Which of your\ntrucks are empty?",
                color = HeaderTextColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp
            )
        }

        // Truck illustration on right (drawn with Canvas)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(120.dp)
        ) {
            TruckIllustration(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** A compact orange truck drawing that works as both header art and avatar (smaller scales). */
@Composable
private fun TruckIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // body
        val bodyLeft = w * 0.08f
        val bodyTop = h * 0.28f
        val bodyWidth = w * 0.66f
        val bodyHeight = h * 0.48f
        drawRoundRect(
            color = Color(0xFFFF8200),
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // cabin
        val cabinLeft = bodyLeft + bodyWidth
        val cabinTop = bodyTop + h * 0.06f
        val cabinW = w * 0.24f
        val cabinH = bodyHeight * 0.8f
        drawRoundRect(
            color = Color(0xFFFF5A00),
            topLeft = Offset(cabinLeft - 8f, cabinTop),
            size = Size(cabinW + 8f, cabinH),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // wheel circles
        val wheelRadius = h * 0.12f
        val wheelY = bodyTop + bodyHeight + wheelRadius * 0.4f
        val wheel1X = bodyLeft + bodyWidth * 0.25f
        val wheel2X = bodyLeft + bodyWidth * 0.75f
        drawCircle(color = Color.Black, radius = wheelRadius, center = Offset(wheel1X, wheelY))
        drawCircle(
            color = Color.Black,
            radius = wheelRadius,
            center = Offset(wheel2X, wheelY)
        )

        // small window on cab
        drawRoundRect(
            color = Color(0xFFB9E6FF),
            topLeft = Offset(cabinLeft + 6f, cabinTop + 8f),
            size = Size(cabinW * 0.6f, cabinH * 0.45f),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }
}

/** --------------------
 * Truck card listing
 * ---------------------*/
@Composable
private fun TruckCard(
    truck: TruckModel,
    onNotAvailable: (TruckModel) -> Unit,
    onEmpty: (TruckModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .background(CardBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // left avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFF6F7F8)),
                contentAlignment = Alignment.Center
            ) {
                // tiny truck illustration scaled down
                TruckIllustration(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = truck.id,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF121212)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = truck.meta,
                    color = SubtitleGray,
                    fontSize = 12.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // two pill buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNotAvailable(truck) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF8F8F8)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = Dp.Hairline),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                        ,
                        modifier = Modifier
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Not available",
                            tint = NotAvailableRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Not Available",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { onEmpty(truck) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFFFFFF)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = Dp.Hairline),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        modifier = Modifier
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Empty",
                            tint = EmptyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Empty",
                            color = Color(0xFF444444),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/** --------------------
 * Bottom Navigation - replicates bottom bar from screenshot
 * ---------------------*/
//@Composable
//private fun BottomNavigationBar(
//    selectedIndex: Int = 1,
//    onItemSelected: (Int) -> Unit = {}
//) {
//    NavigationBar(
//        containerColor = BottomBarBg,
//        tonalElevation = 8.dp,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(78.dp)
//    ) {
//        val items = listOf(
//            Triple("View Load", Icons.Default.Home, 0),
//            Triple("खाली गाड़ी", Icons.Default.TravelExplore, 1),
//            Triple("Trip", Icons.Default.TravelExplore, 2),
//            Triple("Help", Icons.Default.SupportAgent, 3),
//            Triple("Account", Icons.Default.Person, 4)
//        )
//        items.forEachIndexed { index, triple ->
//            val selected = index == selectedIndex
//            NavigationBarItem(
//                icon = {
//                    Icon(
//                        imageVector = when (index) {
//                            0 -> Icons.Default.Home
//                            1 -> Icons.Default.TravelExplore
//                            2 -> Icons.Default.TravelExplore
//                            3 -> Icons.Default.SupportAgent
//                            else -> Icons.Default.Person
//                        },
//                        contentDescription = triple.first,
//                        tint = if (selected) Color.Black else Color(0xFF9E9E9E),
//                        modifier = Modifier.size(22.dp)
//                    )
//                },
//                label = {
//                    Text(
//                        text = triple.first,
//                        fontSize = 12.sp,
//                        color = if (selected) Color.Black else Color(0xFF9E9E9E)
//                    )
//                },
//                selected = selected,
//                onClick = { onItemSelected(index) }
//            )
//        }
//    }
//}

/** --------------------
 * Screen that composes everything together
 * ---------------------*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyTruckScreen(
    trucks: List<TruckModel> = sampleTrucks(),
    onBack: () -> Unit = {},
    onNotAvailable: (TruckModel) -> Unit = {},
    onEmpty: (TruckModel) -> Unit = {},
    onBottomNav: (Int) -> Unit = {}
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF6F7F8)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(onBack = onBack)

            // content list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                content = {
                    items(items = trucks) { truck ->
                        TruckCard(
                            truck = truck,
                            onNotAvailable = { onNotAvailable(it) },
                            onEmpty = { onEmpty(it) },
                        )
                    }

                    // little bottom spacer so last card isn't blocked by bottom nav
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            )

//            BottomNavigationBar(selectedIndex = 1, onItemSelected = onBottomNav)
        }
    }
}

/** --------------------
 * Previews
 * ---------------------*/
@Preview(showBackground = true, name = "Empty Truck - Light", widthDp = 412, heightDp = 915)
@Composable
private fun PreviewEmptyTruckLight() {
    EmptyTruckScreen()
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Empty Truck - Dark (for dev)",
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun PreviewEmptyTruckDark() {
    EmptyTruckScreen()
}
