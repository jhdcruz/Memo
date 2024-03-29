package io.github.jhdcruz.memo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.github.jhdcruz.memo.ui.login.LoginScreen
import io.github.jhdcruz.memo.ui.theme.MemoTheme

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoTheme {

                Scaffold { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        context = this,
                    )
                }
            }
        }
    }
}
