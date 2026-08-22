package com.abvpn.vpn.data.model

/** Supported legitimate VPN protocols. Add new entries here as the app grows. */
enum class VpnProtocol {
    WIREGUARD,
    OPENVPN,
    IKEV2_IPSEC
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

/**
 * A VPN server entry. This is what a remote "server list" API would return —
 * see data/ServerRepository.kt for where that call would be wired in.
 */
data class VpnServer(
    val id: String,
    val name: String,
    val country: String,
    val countryFlagEmoji: String,
    val city: String,
    val host: String,
    val port: Int,
    val protocol: VpnProtocol,
    val publicKey: String? = null, // required for WireGuard
    val pingMs: Int? = null,
    val loadPercent: Int? = null,
    val isOnline: Boolean = true
)

/**
 * A saved/imported VPN configuration. Secrets referenced here (private key,
 * password) are never stored inline — only a reference/alias into SecureConfigStore, which
 * wraps EncryptedSharedPreferences.
 */
data class VpnConfig(
    val id: String,
    val name: String,
    val protocol: VpnProtocol,
    val server: VpnServer,
    val secureCredentialAlias: String,
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true
)

/** Live stats shown on Home + Statistics screens. */
data class ConnectionStats(
    val state: ConnectionState,
    val server: VpnServer? = null,
    val protocol: VpnProtocol? = null,
    val connectedSinceMillis: Long? = null,
    val downloadBps: Long = 0,
    val uploadBps: Long = 0,
    val bytesDownloaded: Long = 0,
    val bytesUploaded: Long = 0,
    val currentVpnIp: String? = null,
    val pingMs: Int? = null,
    val errorMessage: String? = null
)

data class LogEntry(val timestampMillis: Long, val message: String)
