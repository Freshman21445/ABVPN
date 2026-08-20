package com.abvpn.vpn.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abvpn.vpn.data.model.ConnectionState
import com.abvpn.vpn.data.model.VpnServer
import com.abvpn.vpn.vpn.ABVPNService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Bridges the Home screen to ABVPNService. Owns the "does the user still need to grant
 * VPN permission?" check so the Composable stays declarative.
 */
class VpnConnectionViewModel(app: Application) : AndroidViewModel(app) {

    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer: StateFlow<VpnServer?> = _selectedServer.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = ABVPNService.state

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
    }

    /** Returns the VpnService.prepare() intent if the user still needs to grant permission, else null. */
    fun permissionIntentIfNeeded(): Intent? = VpnService.prepare(getApplication())

    fun connect(configId: String) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val intent = Intent(ctx, ABVPNService::class.java).apply {
                action = ABVPNService.ACTION_CONNECT
                putExtra(ABVPNService.EXTRA_CONFIG_ID, configId)
            }
            ctx.startForegroundService(intent)
        }
    }

    fun disconnect() {
        val ctx = getApplication<Application>()
        val intent = Intent(ctx, ABVPNService::class.java).apply {
            action = ABVPNService.ACTION_DISCONNECT
        }
        ctx.startService(intent)
    }
}
