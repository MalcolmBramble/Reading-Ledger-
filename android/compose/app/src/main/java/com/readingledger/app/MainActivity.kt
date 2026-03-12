package com.readingledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.readingledger.app.ui.shelf.ShelfScreen
import com.readingledger.app.ui.timeline.TimelineScreen
import com.readingledger.app.ui.analytics.AnalyticsScreen
import com.readingledger.app.ui.review.ReviewScreen
import com.readingledger.app.ui.detail.BookDetailScreen
import com.readingledger.app.ui.form.BookFormScreen
import com.readingledger.app.ui.settings.SettingsScreen
import com.readingledger.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Shelf : Screen("shelf", "Shelf", Icons.Outlined.MenuBook)
    object Timeline : Screen("timeline", "Timeline", Icons.Outlined.ViewTimeline)
    object Analytics : Screen("analytics", "Analytics", Icons.Outlined.BarChart)
    object Review : Screen("review", "Review", Icons.Outlined.Star)
}

val bottomTabs = listOf(Screen.Shelf, Screen.Timeline, Screen.Analytics, Screen.Review)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadingLedgerTheme {
                ReadingLedgerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingLedgerApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                contentColor = TextMedium,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomTabs.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            unselectedIconColor = TextDim,
                            unselectedTextColor = TextDim,
                            indicatorColor = Accent.copy(alpha = 0.12f),
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Shelf.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Shelf.route) {
                ShelfScreen(
                    onBookClick = { bookId -> navController.navigate("detail/$bookId") },
                    onAddClick = { navController.navigate("form") },
                    onSettingsClick = { navController.navigate("settings") },
                )
            }
            composable(Screen.Timeline.route) {
                TimelineScreen(onBookClick = { bookId -> navController.navigate("detail/$bookId") })
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.Review.route) {
                ReviewScreen(onBookClick = { bookId -> navController.navigate("detail/$bookId") })
            }
            composable("detail/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                BookDetailScreen(
                    bookId = bookId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("form?bookId=$bookId") },
                )
            }
            composable("form?bookId={bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")
                BookFormScreen(
                    bookId = bookId,
                    onDone = { navController.popBackStack() },
                )
            }
            composable("form") {
                BookFormScreen(
                    bookId = null,
                    onDone = { navController.popBackStack() },
                )
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
