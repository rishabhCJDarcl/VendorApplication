//package com.example.vendorapplication
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//
//
//
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.material3.Icon
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.rememberNavController
//
//@Composable
//fun MyLoadScreen(navController: NavHostController) {
//    val selectedTab = remember { mutableStateOf(0) } // Track selected tab
//
//    MaterialTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = Color(0xFFF6F6F9)
//        ) {
//            Box(modifier = Modifier.fillMaxSize()) {
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(bottom = 72.dp)
//                ) {
//                    TopBar()
//                    Spacer(modifier = Modifier.height(12.dp))
//                    StatusPills()
//                    Spacer(modifier = Modifier.height(18.dp))
//                    InfoBanner()
//                    Spacer(modifier = Modifier.height(12.dp))
//                    DotsIndicator()
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    // ✅ Pass required parameters
//                    TabsRows(
//                        selectedIndex = selectedTab.value,
//                        navController = navController,
//                        onTabSelected = { index ->
//                            selectedTab.value = index
//                        }
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    // Scrollable Content
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp)
//                            .verticalScroll(rememberScrollState())
//                    ) {
//
//                        LoadCard(
//                            pickupTitle = "I.A. Surajpur, Gautam Buddha Nagar",
//                            pickupSub = "201306 • UP",
//                            dropTitle = "Dabra, Gwalior",
//                            dropSub = "475110 • MP",
//                            vehicleInfo = "Container | 32 Ft | Goods 4.5 Ton",
//                            timeInfo = "Loading today evening at 4",
//                            distanceInfo = "Your truck is 27.0 km away from loading",
//                            timer = "02 : 07"
//                        )
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        LoadCardMinimal(
//                            pickup = "Another Pickup, Area",
//                            drop = "Another Drop, Area"
//                        )
//
//                        Spacer(modifier = Modifier.height(24.dp))
//                    }
//                }
//
//                // Bottom Navigation
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .fillMaxWidth()
//                ) {
//                    BottomNavigationBar(modifier = Modifier)
//                }
//            }
//        }
//    }
//}
//
//
//
//@Composable
//private fun TopBar() {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 14.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = Icons.Default.ArrowBack,
//            contentDescription = "Back",
//            tint = Color.Black,
//            modifier = Modifier
//                .size(28.dp)
//                .padding(4.dp)
//        )
//
//        Text(
//            text = "View Loads",
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            modifier = Modifier
//                .weight(1f)
//                .padding(start = 8.dp)
//        )
//
//        // small A/a circle placeholder
//        Box(
//            modifier = Modifier
//                .size(36.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFF0F0F3)),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text = "A", fontWeight = FontWeight.Bold)
//        }
//
//        Spacer(modifier = Modifier.width(10.dp))
//
//        Column(
//            horizontalAlignment = Alignment.End
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(text = "Load alert", fontSize = 12.sp)
//                Spacer(modifier = Modifier.width(6.dp))
//                // Toggle lookalike
//                Box(
//                    modifier = Modifier
//                        .size(width = 44.dp, height = 26.dp)
//                        .clip(RoundedCornerShape(20.dp))
//                        .background(Color(0xFF2E6BFF))
//                        .padding(4.dp),
//                    contentAlignment = Alignment.CenterStart
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(18.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun StatusPills() {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        modifier = Modifier
//            .padding(horizontal = 16.dp)
//            .fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Column(modifier = Modifier.padding(12.dp)) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                // Loading pill
//                Row(
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(end = 8.dp)
//                        .clickable { }
//                        .background(Color(0xFFF7F7FB), RoundedCornerShape(10.dp))
//                        .padding(vertical = 14.dp, horizontal = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(12.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF2EC03B))
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Column {
//                        Text(text = "Loading", fontWeight = FontWeight.SemiBold)
//                    }
//                    Spacer(modifier = Modifier.weight(1f))
//                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
//                }
//
//                // Unloading pill
//                Row(
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(start = 8.dp)
//                        .clickable { }
//                        .background(Color(0xFFF7F7FB), RoundedCornerShape(10.dp))
//                        .padding(vertical = 14.dp, horizontal = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(12.dp)
//                            .clip(RoundedCornerShape(3.dp))
//                            .background(Color(0xFFEA6060))
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Column {
//                        Text(text = "Unloading", fontWeight = FontWeight.SemiBold)
//                    }
//                    Spacer(modifier = Modifier.weight(1f))
//                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun InfoBanner() {
//    Box(
//        modifier = Modifier
//            .padding(horizontal = 16.dp)
//            .fillMaxWidth()
//            .height(120.dp)
//            .clip(RoundedCornerShape(14.dp))
//            .background(
//                Brush.linearGradient(
//                    colors = listOf(Color(0xFFDCD3F8), Color(0xFFF0EAFE)),
//                    start = Offset(0f, 0f),
//                    end = Offset(1000f, 1000f)
//                )
//            )
//            .shadow(4.dp, RoundedCornerShape(14.dp))
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(18.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Load confirm in 30 min\nor get token back",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF5A3F9A)
//                )
//            }
//
//            Box(
//                modifier = Modifier
//                    .size(54.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF7F5CD6)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Info,
//                    contentDescription = "info",
//                    tint = Color.White,
//                    modifier = Modifier.size(26.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun DotsIndicator() {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .size(8.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFBDBDBD))
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Box(
//            modifier = Modifier
//                .size(8.dp)
//                .clip(CircleShape)
//                .background(Color(0xFF6AA6FF))
//        )
//    }
//}
//
//@Composable
//fun TabsRows(
//    selectedIndex: Int,
//    navController: NavHostController,
//    onTabSelected: (Int) -> Unit
//) {
//    val tabs = listOf("My Load(0)", "Load(6)", "New load(35)")
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        tabs.forEachIndexed { idx, title ->
//            val selected = idx == selectedIndex
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(horizontal = 6.dp)
//                    .height(42.dp)
//                    .clip(RoundedCornerShape(18.dp))
//                    .background(if (selected) Color(0xFF000000) else Color.White)
//                    .border(
//                        width = 1.dp,
//                        color = if (selected) Color.Transparent else Color(0xFFEEEEEE),
//                        shape = RoundedCornerShape(18.dp)
//                    )
//                    .clickable {
//                        // navigate to MyLoad screen when first tab clicked
//                        if (idx == 0) {
//                            navController.navigate("my_load") {
//                                launchSingleTop = true
//                                restoreState = true
//                            }
//                        }
//                        onTabSelected(idx)
//                    }
//            ) {
//                Text(
//                    text = title,
//                    color = if (selected) Color.White else Color(0xFF6B7280),
//                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//private fun LoadCard(
//    pickupTitle: String,
//    pickupSub: String,
//    dropTitle: String,
//    dropSub: String,
//    vehicleInfo: String,
//    timeInfo: String,
//    distanceInfo: String,
//    timer: String
//) {
//    Card(
//        shape = RoundedCornerShape(14.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            // pickup / drop section
//            Row(modifier = Modifier.fillMaxWidth()) {
//                // vertical dotted line + icons column
//                Column(
//                    modifier = Modifier
//                        .width(28.dp)
//                        .padding(end = 12.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(10.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF2EC03B))
//                    )
//
//                    Spacer(modifier = Modifier.height(6.dp))
//
//                    Canvas(modifier = Modifier.height(60.dp).width(1.dp)) {
//                        drawLine(
//                            color = Color(0xFF9E9E9E),
//                            start = Offset(0f, 0f),
//                            end = Offset(0f, size.height),
//                            strokeWidth = 2f,
//                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(6.dp))
//
//                    Box(
//                        modifier = Modifier
//                            .size(10.dp)
//                            .clip(RoundedCornerShape(3.dp))
//                            .background(Color(0xFFEA6060))
//                    )
//                }
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = pickupTitle,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(text = pickupSub, color = Color(0xFF9E9E9E))
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Text(
//                        text = dropTitle,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(text = dropSub, color = Color(0xFF9E9E9E))
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Divider(color = Color(0xFFE9E9EF), thickness = 1.dp)
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // vehicle & time rows
//            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                IconWithText(icon = Icons.Default.LocalShipping, text = vehicleInfo)
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                IconWithText(icon = Icons.Default.AccessTime, text = timeInfo)
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // purple info pill
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(Color(0xFFF0E8FF))
//                    .padding(12.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.FlashOn,
//                        contentDescription = null,
//                        tint = Color(0xFF6A2EDB),
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Spacer(modifier = Modifier.width(10.dp))
//                    Text(text = distanceInfo, fontWeight = FontWeight.Medium)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(14.dp))
//
//            // Timer badge centered
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Box(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color(0xFFFFF4D6))
//                        .padding(vertical = 6.dp, horizontal = 18.dp)
//                ) {
//                    Text(text = timer, fontWeight = FontWeight.Bold)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(14.dp))
//
//            // Token card section
//            Card(
//                shape = RoundedCornerShape(12.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F8)),
//                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(text = "Token received", fontWeight = FontWeight.SemiBold)
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Button(
//                        onClick = { },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(44.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA60)),
//                        shape = RoundedCornerShape(10.dp)
//                    ) {
//                        Text(text = "Enter vehicle details", color = Color.White, fontWeight = FontWeight.SemiBold)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun IconWithText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Box(
//            modifier = Modifier
//                .size(34.dp)
//                .clip(RoundedCornerShape(8.dp))
//                .background(Color(0xFFF7F7FB)),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
//        }
//        Spacer(modifier = Modifier.width(12.dp))
//        Text(text = text)
//    }
//}
//
//@Composable
//private fun LoadCardMinimal(pickup: String, drop: String) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
//    ) {
//        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(text = pickup, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(6.dp))
//                Text(text = drop, color = Color(0xFF9E9E9E))
//            }
//            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
//        }
//    }
//}
//
//@Composable
//private fun BottomNavigationBar(modifier: Modifier = Modifier) {
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(72.dp)
//            .padding(horizontal = 8.dp, vertical = 8.dp),
//        shape = RoundedCornerShape(22.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 14.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            BottomNavItem(icon = Icons.Default.Home, label = "View Load", selected = true)
//            BottomNavItem(icon = Icons.Default.LocalShipping, label = "खाली गाड़ी", selected = false)
//            BottomNavItem(icon = Icons.Default.Timeline, label = "Trip", selected = false)
//            BottomNavItem(icon = Icons.Default.Help, label = "Help", selected = false)
//            BottomNavItem(icon = Icons.Default.AccountCircle, label = "Account", selected = false, showDot = true)
//        }
//    }
//}
//
//@Composable
//private fun BottomNavItem(
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    label: String,
//    selected: Boolean,
//    showDot: Boolean = false
//) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
//        Box(contentAlignment = Alignment.Center) {
//            Icon(
//                imageVector = icon,
//                contentDescription = label,
//                modifier = Modifier.size(22.dp),
//                tint = if (selected) Color(0xFF000000) else Color(0xFF9E9E9E)
//            )
//            if (showDot) {
//                Box(
//                    modifier = Modifier
//                        .size(8.dp)
//                        .align(Alignment.TopEnd)
//                        .offset(x = 6.dp, y = (-4).dp)
//                        .clip(CircleShape)
//                        .background(Color(0xFFFF4D4D))
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = label,
//            fontSize = 10.sp,
//            color = if (selected) Color.Black else Color(0xFF9E9E9E)
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun MyLoadScreenPreview() {
//    val navController = rememberNavController() // dummy nav controller for preview
//    MyLoadScreen(navController = navController)
//}