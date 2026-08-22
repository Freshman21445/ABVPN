package com.abvpn.vpn

import android.app.Application

class VpnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Wire up DI / repositories here as the app grows (Room DB, ServerRepository, etc.)
    }
}
