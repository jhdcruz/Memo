package io.github.jhdcruz.memo.ui.screens.container

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser
import io.github.jhdcruz.memo.R
import io.github.jhdcruz.memo.ui.components.AppSearch
import io.github.jhdcruz.memo.ui.components.Sidebar
import io.github.jhdcruz.memo.ui.navigation.BottomNavigation
import io.github.jhdcruz.memo.ui.navigation.RootScreens
import io.github.jhdcruz.memo.ui.screens.calendar.CalendarScreen
import io.github.jhdcruz.memo.ui.screens.settings.SettingsScreen
import io.github.jhdcruz.memo.ui.screens.tasks.TasksScreen
import io.github.jhdcruz.memo.ui.screens.tasks.detailsheet.TaskDetailsSheet
import io.github.jhdcruz.memo.ui.theme.MemoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerScreen(
    user: FirebaseUser?,
    modifier: Modifier = Modifier,
    containerViewModel: ContainerViewModel = hiltViewModel<ContainerViewModelImpl>(),
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val photoUrl = remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        photoUrl.value = user?.photoUrl.toString()
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Sidebar(drawerState = drawerState)
            }
        },
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != RootScreens.Settings.route) {
                    Box(
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .padding(top = 4.dp),
                    ) {
                        AppSearch(
                            profile = photoUrl.value,
                            drawerState = drawerState,
                            containerViewModel = containerViewModel,
                        )
                    }
                }
            },
            bottomBar = {
                BottomNavigation(
                    navController = navController,
                )
            },
            floatingActionButton = {
                if (currentRoute != RootScreens.Settings.route) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            scope.launch {
                                sheetState.show()
                            }
                        },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Image(
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = null,
                            )
                            Text(text = "New task")
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = RootScreens.Tasks.route,
            ) {
                composable(RootScreens.Tasks.route) {
                    TasksScreen(
                        modifier = Modifier.padding(innerPadding),
                        containerViewModel = containerViewModel,
                    )
                }

                composable(RootScreens.Calendar.route) {
                    CalendarScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable(RootScreens.Settings.route) {
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        photoUrl = photoUrl.value,
                    )
                }
            }

            // New task bottom sheet
            if (sheetState.isVisible) {
                TaskDetailsSheet(
                    sheetState = sheetState,
                    containerViewModel = containerViewModel,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ContainerScreenPreview() {
    MemoTheme {
        ContainerScreen(
            user = null,
            containerViewModel = ContainerViewModelPreview(),
        )
    }
}
