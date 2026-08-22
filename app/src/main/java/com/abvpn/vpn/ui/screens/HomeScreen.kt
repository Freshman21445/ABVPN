package com.abvpn.vpn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abvpn.vpn.data.model.ConnectionState
import com.abvpn.vpn.viewmodel.VpnConnectionViewModel

/**
 * Home screen: status, one big connect button, selected server, nothing else
 * required to get a beginner connected.
 */
@Composable
fun HomeScreen(
    viewModel: VpnConnectionViewModel,
    onConnectRequested: () -> Unit
) {
    val state by viewModel.connectionState.collectAsState()
    val server by viewModel.selectedServer.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("ABVPN") })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StatusBadge(state)

            Spacer(Modifier.height(32.dp))

            Text(
                text = server?.let { "${it.countryFlagEmoji} ${it.name}" } ?: "No server selected",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(40.dp))

            ConnectButton(
                state = state,
                onClick = {
                    if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
                        viewModel.disconnect()
                    } else {
                        onConnectRequested()
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            if (state == ConnectionState.ERROR) {
                Text(
                    "Unable to connect. Check the server, your internet connection, or that " +
                        "VPN permission was granted.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(state: ConnectionState) {
    val label = when (state) {
        ConnectionState.DISCONNECTED -> "DISCONNECTED"
        ConnectionState.CONNECTING -> "CONNECTING…"
        ConnectionState.CONNECTED -> "CONNECTED"
        ConnectionState.RECONNECTING -> "RECONNECTING…"
        ConnectionState.ERROR -> "ERROR"
    }
    Text(label, style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun ConnectButton(state: ConnectionState, onClick: () -> Unit) {
    val label = if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
        "DISCONNECT"
    } else {
        "CONNECT"
    }
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(140.dp)
    ) {
        Text(label, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
