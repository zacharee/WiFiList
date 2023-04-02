@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.util

import android.net.wifi.WifiConfiguration

val WifiConfiguration.simpleKey: String?
    get() {
        val psk = preSharedKey
        val wep = wepKeys
        return when {
            !psk.isNullOrBlank() -> psk.stripQuotes()
            !wep.all { it.isNullOrBlank() } -> wep.filterNotNull().joinToString("\n") { it.stripQuotes() }
            else -> null
        }
    }
