package com.abvpn.vpn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Wraps EncryptedSharedPreferences (AES256-GCM, key in Android Keystore) so private keys,
 * OpenVPN auth passwords, etc. are never touched as plaintext on disk.
 *
 * This is intentionally the ONLY place in the codebase allowed to read/write raw secret
 * material. Everything else deals in opaque `secureCredentialAlias` strings.
 */
class SecureConfigStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "abvpn_secure_config",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun putSecret(alias: String, value: String) {
        prefs.edit().putString(alias, value).apply()
    }

    fun getSecret(alias: String): String? = prefs.getString(alias, null)

    fun deleteSecret(alias: String) {
        prefs.edit().remove(alias).apply()
    }
}
