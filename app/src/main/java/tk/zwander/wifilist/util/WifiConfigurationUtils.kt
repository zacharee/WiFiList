@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.util

import android.annotation.SuppressLint
import android.content.Context
import android.internal.wifi.WifiAnnotations.SecurityType
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.AuthAlgorithm
import android.net.wifi.WifiConfiguration.GroupCipher
import android.net.wifi.WifiConfiguration.GroupMgmtCipher
import android.net.wifi.WifiConfiguration.PairwiseCipher
import android.net.wifi.WifiConfiguration.Protocol
import android.os.Build
import androidx.annotation.StringRes
import tk.zwander.wifilist.R

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

data class SecurityParams(
    @SecurityType
    val type: ExportSecurityType,
    @StringRes
    val labelRes: Int,
    val allowedKeyManagement: List<Int> = listOf(),
    val allowedProtocols: List<Int> = listOf(),
    val allowedPairwiseCiphers: List<Int> = listOf(),
    val allowedGroupCiphers: List<Int> = listOf(),
    val allowedAuthAlgorithms: List<Int> = listOf(),
    val allowedGroupManagementCiphers: List<Int> = listOf(),
    val requirePmf: Boolean = false,
) {
    @SuppressLint("InlinedApi")
    fun matches(config: WifiConfiguration): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            when (type) {
                ExportSecurityType.OPEN -> allowedKeyManagement.all { config.allowedKeyManagement.get(it) }
                ExportSecurityType.WEP -> allowedKeyManagement.all { config.allowedKeyManagement.get(it) } &&
                        allowedAuthAlgorithms.all { config.allowedAuthAlgorithms.get(it) }
                ExportSecurityType.WPA_WPA2_PERSONAL -> allowedKeyManagement.all { config.allowedKeyManagement.get(it) }
                ExportSecurityType.WPA_WPA2_ENTERPRISE -> allowedKeyManagement.all { config.allowedKeyManagement.get(it) }
                else -> {
                    allowedKeyManagement.all { config.allowedKeyManagement.get(it) } &&
                            allowedProtocols.all { config.allowedProtocols.get(it) } &&
                            allowedPairwiseCiphers.filter { it != PairwiseCipher.GCMP_128 }.all { config.allowedPairwiseCiphers.get(it) } &&
                            allowedGroupCiphers.filter { it != GroupCipher.GCMP_128 }.all { config.allowedGroupCiphers.get(it) } &&
                            allowedAuthAlgorithms.all { config.allowedAuthAlgorithms.get(it) } &&
                            allowedGroupManagementCiphers.all { config.allowedGroupManagementCiphers.get(it) } &&
                            requirePmf == config.requirePmf
                }
            }
        } else {
            allowedKeyManagement.all { config.allowedKeyManagement.get(it) } &&
                    allowedProtocols.all { config.allowedProtocols.get(it) } &&
                    allowedPairwiseCiphers.all { config.allowedPairwiseCiphers.get(it) } &&
                    allowedGroupCiphers.all { config.allowedGroupCiphers.get(it) } &&
                    allowedAuthAlgorithms.all { config.allowedAuthAlgorithms.get(it) } &&
                    allowedGroupManagementCiphers.all { config.allowedGroupManagementCiphers.get(it) } &&
                    requirePmf == config.requirePmf
        }
    }
}

@Suppress("unused")
@SuppressLint("InlinedApi")
enum class ExportSecurityType(val typeValue: Int) {
    WPA_WPA2_ENTERPRISE(WifiConfiguration.SECURITY_TYPE_EAP),
    ENHANCED_OPEN(WifiConfiguration.SECURITY_TYPE_OWE),
    OPEN(WifiConfiguration.SECURITY_TYPE_OPEN),
    OSEN(10),
    WAPI_CERT(WifiConfiguration.SECURITY_TYPE_WAPI_CERT),
    WAPI_PSK(WifiConfiguration.SECURITY_TYPE_WAPI_PSK),
    WEP(WifiConfiguration.SECURITY_TYPE_WEP),
    WPA3_ENTERPRISE_192BIT(WifiConfiguration.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT),
    WPA3_ENTERPRISE(WifiConfiguration.SECURITY_TYPE_EAP_WPA3_ENTERPRISE),
    WPA3_PERSONAL(WifiConfiguration.SECURITY_TYPE_SAE),
    WPA_WPA2_PERSONAL(WifiConfiguration.SECURITY_TYPE_PSK),
    EASY_CONNECT_DPP(WifiConfiguration.SECURITY_TYPE_DPP),
    PASSPOINT_R1_R2(11),
    PASSPOINT_R3(12),
    UNKNOWN(-1),
}

@SuppressLint("InlinedApi")
private val typeMapping = mapOf(
    WifiConfiguration.SECURITY_TYPE_EAP to SecurityParams(
        type = ExportSecurityType.WPA_WPA2_ENTERPRISE,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WPA_EAP, WifiConfiguration.KeyMgmt.IEEE8021X),
        allowedProtocols = listOf(Protocol.RSN, Protocol.WPA),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.TKIP, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.TKIP, GroupCipher.GCMP_256),
        labelRes = R.string.security_wpa_wpa2_enterprise,
    ),
    WifiConfiguration.SECURITY_TYPE_OWE to SecurityParams(
        type = ExportSecurityType.ENHANCED_OPEN,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.OWE),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.GCMP_128, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.GCMP_128, GroupCipher.GCMP_256),
        labelRes = R.string.security_enhanced_open,
    ),
    WifiConfiguration.SECURITY_TYPE_OPEN to SecurityParams(
        type = ExportSecurityType.OPEN,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.NONE),
        allowedProtocols = listOf(Protocol.RSN, Protocol.WPA),
        labelRes = R.string.security_open,
    ),
    10 /* OSEN */ to SecurityParams(
        type = ExportSecurityType.OSEN,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.OSEN),
        allowedProtocols = listOf(2),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.TKIP),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.TKIP),
        labelRes = R.string.security_osen,
    ),
    WifiConfiguration.SECURITY_TYPE_WAPI_CERT to SecurityParams(
        type = ExportSecurityType.WAPI_CERT,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WAPI_CERT),
        allowedProtocols = listOf(Protocol.WAPI),
        allowedPairwiseCiphers = listOf(PairwiseCipher.SMS4),
        allowedGroupCiphers = listOf(GroupCipher.SMS4),
        labelRes = R.string.security_wapi_cert,
    ),
    WifiConfiguration.SECURITY_TYPE_WAPI_PSK to SecurityParams(
        type = ExportSecurityType.WAPI_PSK,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WAPI_PSK),
        allowedProtocols = listOf(Protocol.WAPI),
        allowedPairwiseCiphers = listOf(PairwiseCipher.SMS4),
        allowedGroupCiphers = listOf(GroupCipher.SMS4),
        labelRes = R.string.security_wapi_psk,
    ),
    WifiConfiguration.SECURITY_TYPE_WEP to SecurityParams(
        type = ExportSecurityType.WEP,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.NONE),
        allowedProtocols = listOf(Protocol.RSN),
        allowedAuthAlgorithms = listOf(AuthAlgorithm.OPEN, AuthAlgorithm.SHARED),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.TKIP),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.TKIP, GroupCipher.WEP40, GroupCipher.WEP104),
        labelRes = R.string.security_wep,
    ),
    WifiConfiguration.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT to SecurityParams(
        type = ExportSecurityType.WPA3_ENTERPRISE_192BIT,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WPA_EAP, WifiConfiguration.KeyMgmt.IEEE8021X, WifiConfiguration.KeyMgmt.SUITE_B_192),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.GCMP_128, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.GCMP_128, GroupCipher.GCMP_256),
        allowedGroupManagementCiphers = listOf(GroupMgmtCipher.BIP_GMAC_256),
        requirePmf = true,
        labelRes = R.string.security_wpa3_enterprise_192_bit,
    ),
    WifiConfiguration.SECURITY_TYPE_EAP_WPA3_ENTERPRISE to SecurityParams(
        type = ExportSecurityType.WPA3_ENTERPRISE,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WPA_EAP, WifiConfiguration.KeyMgmt.IEEE8021X),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.GCMP_256),
        requirePmf = true,
        labelRes = R.string.security_wpa3_enterprise,
    ),
    WifiConfiguration.SECURITY_TYPE_SAE to SecurityParams(
        type = ExportSecurityType.WPA3_PERSONAL,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.SAE),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.GCMP_128, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.GCMP_128, GroupCipher.GCMP_256),
        requirePmf = true,
        labelRes = R.string.security_wpa3_personal,
    ),
    WifiConfiguration.SECURITY_TYPE_PSK to SecurityParams(
        type = ExportSecurityType.WPA_WPA2_PERSONAL,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WPA_PSK),
        allowedProtocols = listOf(Protocol.RSN, Protocol.WPA),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.TKIP),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.TKIP, GroupCipher.WEP40, GroupCipher.WEP104),
        labelRes = R.string.security_wpa_wpa2_personal,
    ),
    WifiConfiguration.SECURITY_TYPE_DPP to SecurityParams(
        type = ExportSecurityType.EASY_CONNECT_DPP,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.DPP),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.GCMP_128, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.GCMP_128, GroupCipher.GCMP_256),
        requirePmf = true,
        labelRes = R.string.security_dpp,
    ),
    11 /* PASSPOINT_R1_R2 */ to SecurityParams(
        type = ExportSecurityType.PASSPOINT_R1_R2,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WPA_EAP, WifiConfiguration.KeyMgmt.IEEE8021X),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.GCMP_256),
        labelRes = R.string.security_passpoint_r1_r2,
    ),
    12 /* PASSPOINT_R3 */ to SecurityParams(
        type = ExportSecurityType.PASSPOINT_R3,
        allowedKeyManagement = listOf(WifiConfiguration.KeyMgmt.WPA_EAP, WifiConfiguration.KeyMgmt.IEEE8021X),
        allowedProtocols = listOf(Protocol.RSN),
        allowedPairwiseCiphers = listOf(PairwiseCipher.CCMP, PairwiseCipher.GCMP_256),
        allowedGroupCiphers = listOf(GroupCipher.CCMP, GroupCipher.GCMP_256),
        requirePmf = true,
        labelRes = R.string.security_passpoint_r3,
    ),
)

fun WifiConfiguration.getSecurityType(context: Context): String {
    return context.resources.getString(
        securityParamsObj?.labelRes ?: R.string.security_unknown,
    )
}

val WifiConfiguration.securityParamsObj: SecurityParams?
    get() {
        return typeMapping.values.lastOrNull { it.matches(this) }
    }
