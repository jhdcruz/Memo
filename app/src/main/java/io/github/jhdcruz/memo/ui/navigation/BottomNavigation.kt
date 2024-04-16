package io.github.jhdcruz.memo.ui.navigation

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.jhdcruz.memo.ui.theme.MemoTheme

@Composable
fun BottomNavigation(navController: NavHostController) {
    var selected by remember { mutableIntStateOf(0) }
    val rootScreens = listOf(
        RootScreens.Tasks,
        RootScreens.Calendar,
        RootScreens.Settings,
    )

    NavigationBar {
        rootScreens.forEachIndexed { index, item ->
            NavigationBarItem(
                alwaysShowLabel = true,
                label = {
                    Text(
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal,
                        text = item.title,
                    )
                },
                selected = selected == index,
                icon = {
                    // change icon to filled based on selected
                    val icon: Int by animateIntAsState(
                        targetValue = if (selected == index) item.activeIcon else item.inactiveIcon,
                        label = "selected navigation transition"
                    )

                    Image(
                        colorFilter = if (selected == index) {
                            ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        } else {
                            ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        painter = painterResource(id = icon),
                        contentDescription = item.title
                    )
                },
                onClick = {
                    selected = index

                    navController.navigate(
                        when (selected) {
                            0 -> RootScreens.Tasks.route
                            1 -> RootScreens.Calendar.route
                            2 -> RootScreens.Settings.route
                            else -> RootScreens.Tasks.route
                        }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NavPreview() {
    MemoTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { BottomNavigation(navController) }
        ) { innerPadding ->
            Text(
                modifier = Modifier.padding(innerPadding),
                text = "Surface content"
            )
        }
    }
}
