@file:Suppress("DEPRECATION")

package tk.zwander.wifilist

import android.annotation.SuppressLint
import android.content.AttributionSource
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuProvider
import rikka.shizuku.SystemServiceHelper
import tk.zwander.wifilist.ui.theme.WiFiListTheme
import tk.zwander.wifilist.util.hasShizukuPermission
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tk.zwander.wifilist.ui.components.ExpandableSearchView
import tk.zwander.wifilist.ui.components.WiFiCard

class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {
    companion object {
        private const val SHIZUKU_PERM = 10001
    }

    private val permResultListener =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                finish()
            } else {
                loadNetworks()
            }
        }

    private val currentNetworks = mutableStateListOf<WifiConfiguration>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainContent(currentNetworks)
        }

        if (!Shizuku.pingBinder()) {
            finish()
            return
        }

        if (!hasShizukuPermission) {
            requestShizukuPermission()
        } else {
            loadNetworks()
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult != PackageManager.PERMISSION_GRANTED) {
            finish()
        } else {
            loadNetworks()
        }
    }

    private fun requestShizukuPermission() {
        if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            permResultListener.launch(ShizukuProvider.PERMISSION)
        } else {
            Shizuku.addRequestPermissionResultListener(this)
            Shizuku.requestPermission(SHIZUKU_PERM)
        }
    }

    @SuppressLint("PrivateApi")
    private fun loadNetworks() {
        val base = Class.forName("android.net.wifi.IWifiManager")
        val stub = Class.forName("android.net.wifi.IWifiManager\$Stub")
        val asInterface = stub.getMethod("asInterface", IBinder::class.java)
        val iwm = asInterface.invoke(
            null,
            ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.WIFI_SERVICE))
        )

        val user = if (Shizuku.getUid() == 0) "root" else "shell"
        val pkg = "com.android.network"

        val privilegedConfigs = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            val getPrivilegedConfiguredNetworks = base.getMethod(
                "getPrivilegedConfiguredNetworks",
                String::class.java,
                String::class.java,
                Bundle::class.java
            )
            getPrivilegedConfiguredNetworks.invoke(
                iwm, user, pkg,
                Bundle().apply {
                    putParcelable(
                        "EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE",
                        AttributionSource::class.java.getConstructor(Int::class.java, String::class.java, String::class.java, Set::class.java, AttributionSource::class.java)
                            .newInstance(Shizuku.getUid(), pkg, pkg, null as Set<String>?, null)
                    )
                }
            )
        } else {
            val getPrivilegedConfiguredNetworks = base.getMethod(
                "getPrivilegedConfiguredNetworks",
                String::class.java,
                String::class.java
            )
            getPrivilegedConfiguredNetworks.invoke(iwm, user, pkg)
        }
        @Suppress("UNCHECKED_CAST")
        val privilegedConfigsList = privilegedConfigs::class.java.getMethod("getList")
            .invoke(privilegedConfigs) as List<WifiConfiguration>

        currentNetworks.clear()
        currentNetworks.addAll(
            privilegedConfigsList
                .sortedBy { it.SSID.lowercase() }
                .distinctBy { "${it.SSID}${it.preSharedKey}${it.wepKeys.joinToString("")}" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(networks: List<WifiConfiguration>) {
    var searchText by remember {
        mutableStateOf("")
    }
    var searchExpanded by remember {
        mutableStateOf(false)
    }

    WiFiListTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(id = R.string.saved_wifi_networks),
                            modifier = Modifier
                                .animateContentSize()
                                .then(
                                    if (!searchExpanded) {
                                        Modifier.padding(start = 16.dp)
                                    } else {
                                        Modifier.width(0.dp)
                                    }
                                ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        ExpandableSearchView(
                            searchDisplay = searchText,
                            onSearchDisplayChanged = {
                                searchText = it
                            },
                            onSearchDisplayClosed = {
                                searchExpanded = false
                                searchText = ""
                            },
                            onSearchDisplayOpened = {
                                searchExpanded = true
                            },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            ) { padding ->
                LazyColumn(
                    contentPadding = padding
                ) {
                    items(networks.filter { it.SSID.contains(searchText, true) }, { item -> "${item.SSID}${item.preSharedKey}${item.wepKeys.joinToString("")}" }) { config ->
                        var expanded by remember {
                            mutableStateOf(false)
                        }
                        WiFiCard(
                            config = config,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                            expanded = expanded,
                            onExpandChange = { expanded = it }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WiFiListTheme(darkTheme = true) {
        MainContent(listOf())
    }
}