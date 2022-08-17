@file:Suppress("DEPRECATION")

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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import tk.zwander.wifilist.util.*

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
        !wep.all { it.isNullOrBlank() } -> wep.filterNotNull().joinToString("\n") { it.stripQuotes() }
        else -> stringResource(id = R.string.no_password)
    }

    Card(
        modifier = modifier
            .combinedClickable(
                onLongClick = {
                    cbm.setPrimaryClip(ClipData.newPlainText(config.SSID, key))
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
                    label = stringResource(id = R.string.security),
                    value = WifiConfiguration.KeyMgmt.strings[config.authType]
                )

                TwoLineText(
                    label = stringResource(id = R.string.password),
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
                            TwoLineText(value = config.BSSID, label = stringResource(id = R.string.bssid))
                        }
                        config.FQDN.letNotEmpty {
                            TwoLineText(value = config.FQDN, label = stringResource(id = R.string.fqdn))
                        }
                        TwoLineText(value = config.creatorName, label = stringResource(id = R.string.creator))
                        TwoLineText(value = config.lastUpdateName, label = stringResource(id = R.string.last_update))
                        TwoLineText(value = config.allowAutojoin.toString(), label = stringResource(
                            id = R.string.auto_join
                        ))
                        TwoLineText(value = config.hiddenSSID.toString(), label = stringResource(id = R.string.hidden))

                        config.enterpriseConfig?.let { wifiEnterpriseConfig ->
                            wifiEnterpriseConfig.anonymousIdentity.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.anonymous_id))
                            }
                            wifiEnterpriseConfig.altSubjectMatch.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.alt_subject))
                            }
                            wifiEnterpriseConfig.caPath.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.ca_path))
                            }
                            wifiEnterpriseConfig.clientCertificateAlias.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.cert_alias))
                            }
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                                wifiEnterpriseConfig.clientKeyPairAlias.letNotEmpty {
                                    TwoLineText(value = it, label = stringResource(id = R.string.keypair_alias))
                                }
                                wifiEnterpriseConfig.decoratedIdentityPrefix.letNotEmpty {
                                    TwoLineText(value = it, label = stringResource(id = R.string.id_prefix))
                                }
                                wifiEnterpriseConfig.isEapMethodServerCertUsed.let {
                                    TwoLineText(
                                        value = it.toString(),
                                        label = stringResource(id = R.string.eap_server_cert_used)
                                    )

                                    if (it) {
                                        TwoLineText(value = wifiEnterpriseConfig.isServerCertValidationEnabled.toString(), label = stringResource(
                                            id = R.string.server_cert_validation
                                        ))
                                    }
                                }
                            }
                            wifiEnterpriseConfig.clientPrivateKey?.let {
                                TwoLineText(value = it.format, label = stringResource(id = R.string.private_key_format))
                                TwoLineText(value = it.algorithm, label = stringResource(id = R.string.private_key_algorithm))
                            }
                            wifiEnterpriseConfig.domainSuffixMatch.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.domain_suffix))
                            }
                            wifiEnterpriseConfig.eapMethod.let {
                                if (it != -1) {
                                    @Suppress("UNCHECKED_CAST")
                                    TwoLineText(
                                        value = (WifiEnterpriseConfig.Eap::class.java.getDeclaredField("strings")
                                            .get(null) as Array<String>)[it],
                                        label = stringResource(id = R.string.eap_method)
                                    )
                                }
                            }
                            wifiEnterpriseConfig.identity.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.identity))
                            }
                            TwoLineText(
                                value = wifiEnterpriseConfig.isAuthenticationSimBased.toString(),
                                label = stringResource(id = R.string.sim_based)
                            )
                            wifiEnterpriseConfig.password.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.password))
                            }
                            wifiEnterpriseConfig.plmn.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.plmn))
                            }
                            wifiEnterpriseConfig.realm.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.realm))
                            }
                            wifiEnterpriseConfig.wapiCertSuite.letNotEmpty {
                                TwoLineText(value = it, label = stringResource(id = R.string.cert_suite))
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
