package com.lua.dsbcafe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.lua.dsbcafe.auth.AuthManager
import com.lua.dsbcafe.nfc.NfcManager
import com.lua.dsbcafe.ui.screen.LoginScreen
import com.lua.dsbcafe.ui.screen.MainScreen
import com.lua.dsbcafe.ui.theme.DSBCafeTheme
import com.lua.dsbcafe.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager
    private lateinit var authManager: AuthManager
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        nfcManager = NfcManager(this)
        authManager = AuthManager()

        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            viewModel = mainViewModel

            var isSignedIn by remember {
                mutableStateOf(authManager.currentUser != null)
            }

            DSBCafeTheme {
                if (isSignedIn) {
                    MainScreen(
                        onSignOut = {
                            authManager.signOut()
                            isSignedIn = false
                        },
                        viewModel = mainViewModel,
                    )
                } else {
                    LoginScreen(
                        authManager = authManager,
                        webClientId = getString(R.string.default_web_client_id),
                        onSignedIn = { isSignedIn = true },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val badgeId = NfcManager.extractBadgeId(intent) ?: return
        if (authManager.currentUser != null) {
            viewModel.onNfcTagRead(badgeId)
        }
    }
}
