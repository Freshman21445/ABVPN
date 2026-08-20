package com.abvpn.vpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.abvpn.vpn.MainActivity
import com.abvpn.vpn.data.model.ConnectionState
import com.abvpn.vpn.data.model.VpnConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Android VpnService implementation.
 *
 * IMPORTANT / HONEST SCOPE NOTE:
 * This class handles everything Android itself is responsible for: requesting the TUN
 * interface, configuring routes/DNS/MTU, running as a foreground service with a
 * notification, and exposing connection state. It does NOT implement the WireGuard or
 * OpenVPN cryptographic protocol itself — that's a substantial undertaking that should
 * use the vetted upstream libraries rather than a hand-rolled implementation:
 *   - WireGuard: com.wireguard.android:tunnel (wraps the official Go/JNI backend)
 *   - OpenVPN:   an ICS-OpenVPN-derived core, or ship OpenVPN3 via JNI
 * `establishTunnel()` below is where that library's tunnel handle gets bound to the
 * ParcelFileDescriptor this service creates. See README.md for wiring instructions.
 */
class ABVPNService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.abvpn.vpn.action.CONNECT"
        const val ACTION_DISCONNECT = "com.abvpn.vpn.action.DISCONNECT"
        const val EXTRA_CONFIG_ID = "extra_config_id"

        private const val NOTIFICATION_CHANNEL_ID = "abvpn_status"
        private const val NOTIFICATION_ID = 1001

        // Process-wide observable state so the UI (ViewModel) can collect it without binding.
        private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
        val state = _state.asStateFlow()
    }

    private var tunInterface: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val configId = intent.getStringExtra(EXTRA_CONFIG_ID)
                connect(configId)
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    private fun connect(configId: String?) {
        _state.value = ConnectionState.CONNECTING
        startForeground(NOTIFICATION_ID, buildNotification("Connecting…"))

        serviceJob = scope.launch {
            try {
                // 1. Load the resolved VpnConfig (server + protocol + secure credential alias)
                //    via a repository — omitted here, see viewmodel/VpnConnectionViewModel.kt
                //    for how the UI passes configId through.
                val config = loadConfig(configId) ?: throw IllegalStateException("Unknown configuration")

                // 2. Build + establish the TUN interface using Android's Builder API.
                tunInterface = establishTunnel(config)

                // 3. Hand the TUN fd off to the protocol backend (WireGuard/OpenVPN library).
                //    Left as an extension point — see class doc.
                bindProtocolBackend(config, tunInterface!!)

                _state.value = ConnectionState.CONNECTED
                updateNotification("Connected — ${config.server.name}")
            } catch (e: Exception) {
                _state.value = ConnectionState.ERROR
                updateNotification("Connection failed")
                cleanupTunnel()
            }
        }
    }

    private fun disconnect() {
        cleanupTunnel()
        _state.value = ConnectionState.DISCONNECTED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Configures the TUN device: address, routes, DNS, MTU, and (if split tunneling is
     * enabled) per-app include/exclude lists via addAllowedApplication /
     * addDisallowedApplication. This uses only Android's public, supported VpnService.Builder
     * APIs — no bypass of carrier or system network controls.
     */
    private fun establishTunnel(config: VpnConfig): ParcelFileDescriptor {
        val builder = Builder()
            .setSession(config.name)
            .addAddress("10.8.0.2", 32) // placeholder client tunnel address — real value
                                        // comes from the server/handshake response
            .addDnsServer("1.1.1.1")   // replace with user's DNS Protection setting
            .addRoute("0.0.0.0", 0)     // route all traffic; narrowed for split tunneling below
            .setMtu(1420)

        // Split tunneling would filter here based on saved app selections,
        // e.g.: config.excludedPackages.forEach { builder.addDisallowedApplication(it) }

        return builder.establish()
            ?: throw IllegalStateException("VPN interface could not be established")
    }

    /**
     * Extension point: bind the established TUN fd to the actual protocol implementation.
     * This is intentionally unimplemented — plugging in the WireGuard-Android tunnel library
     * (or an OpenVPN3 JNI core) happens here.
     */
    private fun bindProtocolBackend(config: VpnConfig, tun: ParcelFileDescriptor) {
        // TODO: e.g. GoBackend(this).setState(tunnel, Tunnel.State.UP, wgConfig)
    }

    private fun loadConfig(configId: String?): VpnConfig? {
        // TODO: read from Room / repository by id
        return null
    }

    private fun cleanupTunnel() {
        try {
            tunInterface?.close()
        } catch (_: Exception) {
        }
        tunInterface = null
        serviceJob?.cancel()
    }

    private fun buildNotification(text: String): Notification {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, "VPN Status", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val openAppIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ABVPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onRevoke() {
        // User revoked VPN permission from system settings — tear down cleanly.
        disconnect()
        super.onRevoke()
    }

    override fun onDestroy() {
        cleanupTunnel()
        super.onDestroy()
    }
}
