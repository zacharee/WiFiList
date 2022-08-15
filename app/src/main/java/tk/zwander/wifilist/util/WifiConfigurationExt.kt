@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.util

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiSsid

val WifiConfiguration.authType: Int
    get() = WifiConfiguration::class.java.getMethod("getAuthType").invoke(this) as Int

val WifiConfiguration.printableSsid: String
    get() = WifiConfiguration::class.java.getMethod("getPrintableSsid").invoke(this) as String

val WifiConfiguration.creatorName: String
    get() = WifiConfiguration::class.java.getField("creatorName").get(this) as String

val WifiConfiguration.lastUpdateName: String
    get() = WifiConfiguration::class.java.getField("lastUpdateName").get(this) as String

val WifiConfiguration.allowAutojoin: Boolean
    get() = WifiConfiguration::class.java.getField("allowAutojoin").get(this) as Boolean

val WifiEnterpriseConfig.caPath: String
    get() = WifiEnterpriseConfig::class.java.getMethod("getCaPath").invoke(this) as String

val WifiEnterpriseConfig.clientCertificateAlias: String
    get() = WifiEnterpriseConfig::class.java.getMethod("getClientCertificateAlias").invoke(this) as String

val WifiEnterpriseConfig.wapiCertSuite: String
    get() = WifiEnterpriseConfig::class.java.getMethod("getWapiCertSuite").invoke(this) as String
