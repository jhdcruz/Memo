package io.github.jhdcruz.memo.ui.components

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.github.jhdcruz.memo.AuthActivity
import io.github.jhdcruz.memo.R
import io.github.jhdcruz.memo.service.reminders.ReminderNotifyService
import io.github.jhdcruz.memo.ui.screens.container.ContainerViewModel
import io.github.jhdcruz.memo.ui.screens.container.ContainerViewModelPreview
import io.github.jhdcruz.memo.ui.screens.login.LoginViewModel
import io.github.jhdcruz.memo.ui.screens.login.LoginViewModelImpl
import io.github.jhdcruz.memo.ui.theme.MemoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearch(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    containerViewModel: ContainerViewModel,
    loginViewModel: LoginViewModel = hiltViewModel<LoginViewModelImpl>(),
    profile: String? = null,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val query = containerViewModel.query.collectAsState(initial = "").value

    var showProfileMenu by remember { mutableStateOf(false) }

    val voiceSearch =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // matches[0] will contain the result of voice input
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                scope.launch {
                    containerViewModel.onQueryChange(matches?.get(0) ?: "")
                    containerViewModel.onSearch()
                }
            }
        }

    val requestVoicePermission =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                voiceSearch.launch(containerViewModel.onVoiceSearch())
            } else {
                Toast.makeText(
                    context,
                    "Microphone permission is required.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    DockedSearchBar(
        modifier =
            modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        query = query,
        onQueryChange = containerViewModel::onQueryChange,
        onSearch = {
            scope.launch {
                containerViewModel.onSearch()
            }
        },
        leadingIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                },
            ) {
                Image(
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    painter = painterResource(id = R.drawable.baseline_menu_24),
                    contentDescription = "Tasks menu",
                )
            }
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    modifier = Modifier.offset(x = 4.dp),
                    onClick = {
                        scope.launch {
                            // check first if voice permission is granted
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.RECORD_AUDIO,
                                ) ==
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                voiceSearch.launch(containerViewModel.onVoiceSearch())
                            } else {
                                requestVoicePermission.launch(
                                    android.Manifest.permission.RECORD_AUDIO,
                                )
                            }
                        }
                    },
                ) {
                    Image(
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        painter = painterResource(id = R.drawable.baseline_mic_24),
                        contentDescription = "Search tasks using voice input",
                    )
                }
                IconButton(onClick = { showProfileMenu = true }) {
                    AsyncImage(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                        model = profile,
                        contentDescription = "Profile icon",
                        placeholder = painterResource(id = R.drawable.baseline_user_circle_24),
                        error = painterResource(id = R.drawable.baseline_user_circle_24),
                    )

                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Sign out") },
                            onClick = {
                                scope.launch {
                                    loginViewModel.onSignOut()

                                    val appContext = context.applicationContext
                                    // terminate the reminder service
                                    Intent(
                                        appContext,
                                        ReminderNotifyService::class.java,
                                    ).apply {
                                        appContext.applicationContext.stopService(this)
                                    }

                                    // go back to login activity
                                    Intent(
                                        appContext,
                                        AuthActivity::class.java,
                                    ).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        appContext.applicationContext.startActivity(this)
                                    }
                                }
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                }
            }
        },
        // we don't need the search bar content, we filter directly
        active = false,
        onActiveChange = {},
        placeholder = {
            Text(text = "Search your tasks")
        },
    ) {
    }
}

@Preview
@Composable
private fun AppSearchPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    MemoTheme {
        AppSearch(
            containerViewModel = ContainerViewModelPreview(),
            drawerState = drawerState,
        )
    }
}
