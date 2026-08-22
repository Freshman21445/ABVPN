package com.abvpn.vpn

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import com.abvpn.vpn.ui.screens.HomeScreen
import com.abvpn.vpn.ui.theme.ABVPNTheme
import com.abvpn.vpn.viewmodel.VpnConnectionViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VpnConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Standard Android VPN consent flow: VpnService.prepare() returns an Intent
            // only if the user hasn't already approved this app. We launch it, and only
            // proceed to actually connect once RESULT_OK comes back.
            val vpnPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.connect(configId = "default")
                }
                // If denied, Home screen's connectionState stays DISCONNECTED and the UI
                // should surface the "VPN permission has not been granted" message.
            }

            ABVPNTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onConnectRequested = {
                        val permissionIntent = viewModel.permissionIntentIfNeeded()
                        if (permissionIntent != null) {
                            vpnPermissionLauncher.launch(permissionIntent)
                        } else {
                            viewModel.connect(configId = "default")
                        }
                    }
                )
            }
        }
    }
}
