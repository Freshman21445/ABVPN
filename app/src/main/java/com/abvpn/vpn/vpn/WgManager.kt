package com.abvpn.vpn.vpn

import android.content.Context
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.BufferedReader
import java.io.StringReader

class ABVPNTunnel(private val tunnelName: String) : Tunnel {
    override fun getName(): String = tunnelName
    override fun onStateChange(newState: Tunnel.State) {}
}

object WgManager {
    private var backend: GoBackend? = null
    private var tunnel: ABVPNTunnel? = null

    private const val CONFIG_TEXT = """
[Interface]
PrivateKey = kEWDSUdDNM1xHBJNAb8yVzh1jcO2/nGL/glGu9VpnG8=
Address = 10.2.0.2/32
DNS = 10.2.0.1

[Peer]
PublicKey = xxqx935HQ/GiV+RaMtaHu4g6aDKSPF8icpo1vYF+Dj8=
AllowedIPs = 0.0.0.0/0, ::/0
Endpoint = 212.8.248.176:51820
PersistentKeepalive = 25
"""

    private fun getBackend(context: Context): GoBackend {
        if (backend == null) backend = GoBackend(context.applicationContext)
        return backend!!
    }

    fun connect(context: Context) {
        val be = getBackend(context)
        val cfg = Config.parse(BufferedReader(StringReader(CONFIG_TEXT)))
        tunnel = ABVPNTunnel("abvpn")
        be.setState(tunnel!!, Tunnel.State.UP, cfg)
    }

    fun disconnect(context: Context) {
        val be = getBackend(context)
        tunnel?.let { be.setState(it, Tunnel.State.DOWN, null) }
    }
}
