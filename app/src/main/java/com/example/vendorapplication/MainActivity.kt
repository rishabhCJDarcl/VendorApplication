//package com.example.vendorapplication
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.DrawerValue
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.rememberDrawerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.navigation.compose.rememberNavController
//import com.example.vendorapplication.navigation.MainNavGraph
//import com.example.vendorapplication.navigation.BottomNav
//import com.example.vendorapplication.Sidebar
//import kotlinx.coroutines.launch
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//
//            // ONE single navController (VERY IMPORTANT)
//            val navController = rememberNavController()
//
//            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//            val scope = rememberCoroutineScope()
//
//            MaterialTheme {
//
//                // Drawer should be OUTSIDE Scaffold + NavHost
//                ModalNavigationDrawer(
//                    drawerState = drawerState,
//                    drawerContent = {
//                        Sidebar(
//                            onClose = {
//                                scope.launch { drawerState.close() }
//                            },
//                            onMenuClick = { route ->
//                                scope.launch { drawerState.close() }
//                                navController.navigate(route)
//                            }
//                        )
//                    }
//                ) {
//
//                    Scaffold(
//                        bottomBar = {
//                            BottomNav(navController)
//                        }
//                    ) { innerPadding ->
//
//                        // NavHost receives SAME navController
//                        MainNavGraph(
//                            navController = navController,
//                            innerPadding = innerPadding
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
package com.example.vendorapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.vendorapplication.navigation.MainNavGraph
import com.example.vendorapplication.navigation.BottomNav
import com.example.vendorapplication.Sidebar
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // ONE single navController (VERY IMPORTANT)
            val navController = rememberNavController()

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // Observe current route to hide drawer/bottom nav on login
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            MaterialTheme {
                // If current route is "login" we want a fullscreen login without drawer or bottom nav.
                if (currentRoute == "login") {
                    // Simple scaffold without drawer or bottom bar
                    Scaffold { innerPadding ->
                        MainNavGraph(
                            navController = navController,
                            innerPadding = innerPadding
                        )
                    }
                } else {
                    // App UI with drawer and bottom bar
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            Sidebar(
                                onClose = {
                                    scope.launch { drawerState.close() }
                                },
                                onMenuClick = { route ->
                                    scope.launch { drawerState.close() }
                                    navController.navigate(route)
                                }
                            )
                        }
                    ) {

                        Scaffold(
                            bottomBar = {
                                BottomNav(navController)
                            }
                        ) { innerPadding ->

                            MainNavGraph(
                                navController = navController,
                                innerPadding = innerPadding
                            )
                        }
                    }
                }
            }
        }
    }
}
