# ABVPN — Prototype

A starting point for a legitimate Android VPN client (Kotlin + Jetpack Compose + `VpnService`),
built to the "Home → Server selection → VPN permission → Connect → Connected status →
Disconnect" prototype loop.

## What's real in this prototype

- **Project/module structure** matching the spec's layered architecture (UI → ViewModel →
  VPN Manager → protocol → `VpnService`).
- **`MainActivity`** with the correct Android VPN consent flow: it calls
  `VpnService.prepare()` and only proceeds to connect after the user approves the system
  dialog, exactly as Android requires.
- **`ABVPNService`**: a working `VpnService` that builds a TUN interface
  (`Builder().addAddress/addDnsServer/addRoute/setMtu().establish()`), runs as a foreground
  service with a status notification, exposes connection state via `StateFlow`, and tears
  down cleanly on disconnect or permission revocation.
- **`SecureConfigStore`**: `EncryptedSharedPreferences` wrapper so private keys/passwords
  are never written to disk as plaintext.
- **Home screen** (Compose): status, selected server, one large connect/disconnect button,
  matching the "beginner shouldn't need to understand VPN technology" requirement.

## What's intentionally NOT implemented yet

This is the honest part: a VPN app's value is in the actual encrypted tunnel, and that
piece is deliberately left as an extension point rather than faked, per the spec's own
instruction *("Do not provide fake implementations that appear functional but are not")*.

1. **Protocol backend** (`ABVPNService.bindProtocolBackend`) — plug in:
   - WireGuard: the official [`wireguard-android`](https://github.com/WireGuard/wireguard-android)
     library (`com.wireguard.android:tunnel`). It gives you a `GoBackend` that takes the
     `ParcelFileDescriptor` this service already creates and a parsed `Config`.
   - OpenVPN: typically via an ICS-OpenVPN-derived core or OpenVPN3 JNI bindings — more
     involved; budget real time for this if you need it in v1.
   - IKEv2/IPsec: Android's `android.net.ipsec.ike` package (API 28+) can handle this
     without a third-party library.
2. **Server list / backend API** (spec #24-25) — `ServerRepository` isn't included; point
   it at whatever backend you stand up (server health, load, ping).
3. **Config import parsing** (spec #6) — `.conf`/`.ovpn` file parsing and validation UI.
4. **Room database** for saved servers/configs/logs (currently everything is in-memory).
5. **Split tunneling app picker, kill switch, DNS settings UI, statistics/log screens** —
   the data models (`ConnectionStats`, `LogEntry`) exist in `data/model/Models.kt`; the
   screens themselves aren't built yet.
6. **Auto-reconnect logic**.

## Suggested next steps, in order

1. Open in Android Studio, let it sync, fix any dependency-version drift.
2. Wire in `wireguard-android` and get one real config connecting end-to-end against a
   server you control — that de-risks the whole rest of the app.
3. Build `ServerRepository` + a minimal backend for the server list.
4. Add Room for persistence, then build out Servers/Configurations/Settings screens.
5. Kill switch + split tunneling once the core tunnel is solid.
