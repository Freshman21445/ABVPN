package com.abvpn.vpn.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abvpn.vpn.data.model.ConnectionState
import com.abvpn.vpn.data.model.VpnServer
import com.abvpn.vpn.vpn.WgManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpnConnectionViewModel(app: Application) : AndroidViewModel(app) {

    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer: StateFlow<VpnServer?> = _selectedServer.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
    }

    fun permissionIntentIfNeeded(): Intent? = VpnService.prepare(getApplication())

    fun connect(configId: String) {
        _connectionState.value = ConnectionState.CONNECTING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                WgManager.connect(getApplication())
                _connectionState.value = ConnectionState.CONNECTED
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                WgManager.disconnect(getApplication())
            } catch (_: Exception) {
            }
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}
