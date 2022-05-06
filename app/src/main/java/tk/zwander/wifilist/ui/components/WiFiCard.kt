package tk.zwander.wifilist.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import tk.zwander.wifilist.R
import tk.zwander.wifilist.util.stripQuotes
import kotlin.math.exp

@SuppressLint("SoonBlockedPrivateApi")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WiFiCard(
    config: WifiConfiguration,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cbm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val psk = config.preSharedKey
    val wep = config.wepKeys
    val key = when {
        !psk.isNullOrBlank() -> psk.stripQuotes()
        !wep.all { it.isNullOrBlank() } -> wep.joinToString("\n") { it.stripQuotes() }
        else -> stringResource(id = R.string.no_password)
    }

    Card(
        modifier = modifier
            .combinedClickable(
                onLongClick = {
                    cbm.primaryClip = ClipData.newPlainText(config.SSID, key)
                    Toast
                        .makeText(context, R.string.copied, Toast.LENGTH_SHORT)
                        .show()
                },
                onClick = {
                    onExpandChange(!expanded)
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val insecure = config.authType == WifiConfiguration.KeyMgmt.NONE

                Icon(
                    painter = painterResource(id = if (insecure) R.drawable.ic_unlocked else R.drawable.ic_locked),
                    contentDescription = null,
                    tint = if (insecure) Color.Yellow else Color.Green
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = config.printableSsid,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            FlowRow(
                mainAxisAlignment = FlowMainAxisAlignment.SpaceAround,
                mainAxisSize = SizeMode.Expand
            ) {
                TwoLineText(
                    label = "Security",
                    value = WifiConfiguration.KeyMgmt.strings[config.authType]
                )

                TwoLineText(
                    label = "Password",
                    value = key,
                    secure = config.authType != WifiConfiguration.KeyMgmt.NONE
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.size(16.dp))

                    FlowRow(
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceAround,
                        mainAxisSize = SizeMode.Expand
                    ) {
                        config.BSSID.letNotEmpty {
                            TwoLineText(value = config.BSSID, label = "BSSID")
                        }
                        config.FQDN.letNotEmpty {
                            TwoLineText(value = config.FQDN, label = "FQDN")
                        }
                        TwoLineText(value = config.creatorName, label = "Creator")
                        TwoLineText(value = config.lastUpdateName, label = "Last Update")
                        TwoLineText(value = config.allowAutojoin.toString(), label = "Auto-Join")
                        TwoLineText(value = config.hiddenSSID.toString(), label = "Hidden")

                        config.enterpriseConfig?.let { wifiEnterpriseConfig ->
                            wifiEnterpriseConfig.anonymousIdentity.letNotEmpty {
                                TwoLineText(value = it, label = "Anonymous ID")
                            }
                            wifiEnterpriseConfig.altSubjectMatch.letNotEmpty {
                                TwoLineText(value = it, label = "Alt Subject")
                            }
                            wifiEnterpriseConfig.caPath.letNotEmpty {
                                TwoLineText(value = it, label = "CA Path")
                            }
                            wifiEnterpriseConfig.clientCertificateAlias.letNotEmpty {
                                TwoLineText(value = it, label = "Cert Alias")
                            }
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                                wifiEnterpriseConfig.clientKeyPairAlias.letNotEmpty {
                                    TwoLineText(value = it, label = "Keypair Alias")
                                }
                                wifiEnterpriseConfig.decoratedIdentityPrefix.letNotEmpty {
                                    TwoLineText(value = it, label = "ID Prefix")
                                }
                                wifiEnterpriseConfig.isEapMethodServerCertUsed.let {
                                    TwoLineText(
                                        value = it.toString(),
                                        label = "EAP Server Cert Used"
                                    )

                                    if (it) {
                                        wifiEnterpriseConfig.isServerCertValidationEnabled.let {
                                            TwoLineText(value = it.toString(), label = "Server Cert Validation")
                                        }
                                    }
                                }
                            }
                            wifiEnterpriseConfig.clientPrivateKey?.let {
                                TwoLineText(value = it.format, label = "Private Key Format")
                                TwoLineText(value = it.algorithm, label = "Private Key Algorithm")
                            }
                            wifiEnterpriseConfig.domainSuffixMatch.letNotEmpty {
                                TwoLineText(value = it, label = "Domain Suffix")
                            }
                            wifiEnterpriseConfig.eapMethod.let {
                                if (it != -1) {
                                    TwoLineText(
                                        value = (WifiEnterpriseConfig.Eap::class.java.getDeclaredField(
                                            "strings"
                                        )
                                            .get(null) as Array<String>)[it],
                                        label = "EAP Method"
                                    )
                                }
                            }
                            wifiEnterpriseConfig.identity.letNotEmpty {
                                TwoLineText(value = it, label = "ID")
                            }
                            wifiEnterpriseConfig.isAuthenticationSimBased.let {
                                TwoLineText(value = it.toString(), label = "SIM Based")
                            }
                            wifiEnterpriseConfig.password.letNotEmpty {
                                TwoLineText(value = it, label = "EAP Password")
                            }
                            wifiEnterpriseConfig.plmn.letNotEmpty {
                                TwoLineText(value = it, label = "PLMN")
                            }
                            wifiEnterpriseConfig.realm.letNotEmpty {
                                TwoLineText(value = it, label = "Realm")
                            }
                            wifiEnterpriseConfig.wapiCertSuite.letNotEmpty {
                                TwoLineText(value = it, label = "Cert Suite")
                            }
                        }
                    }
                }
            }
        }
    }
}

private inline fun <R> String?.letNotEmpty(block: (String) -> R) {
    if (!isNullOrBlank()) {
        block(this)
    }
}
