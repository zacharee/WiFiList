@file:Suppress("DEPRECATION")

package tk.zwander.wifilist

import android.annotation.SuppressLint
import android.content.AttributionSource
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tk.zwander.wifilist.ui.components.ExpandableSearchView
import tk.zwander.wifilist.ui.components.Menu
import tk.zwander.wifilist.ui.components.SupportersDialog
import tk.zwander.wifilist.ui.components.WiFiCard
import tk.zwander.wifilist.util.launchUrl

class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {
    companion object {
        private const val SHIZUKU_PERM = 10001
    }

    private val permResultListener =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                showShizukuFailureDialog()
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
            showShizukuFailureDialog()
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
            showShizukuFailureDialog()
        } else {
            loadNetworks()
        }
    }

    private fun showShizukuFailureDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.shizuku_required_title)
            .setMessage(R.string.shizuku_required_desc)
            .setNegativeButton(R.string.close, null)
            .setCancelable(false)
            .setOnDismissListener { finish() }
            .apply {
                try {
                    packageManager.getApplicationInfo("moe.shizuku.privileged.api", 0)
                    setPositiveButton(R.string.open_shizuku) { _, _ ->
                        val shizukuIntent = Intent(Intent.ACTION_MAIN)
                        shizukuIntent.component = ComponentName("moe.shizuku.privileged.api", "moe.shizuku.manager.MainActivity")
                        shizukuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(shizukuIntent)
                    }
                } catch (_: PackageManager.NameNotFoundException) {
                    setPositiveButton(R.string.install_shizuku) { _, _ ->
                        launchUrl("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")
                    }
                }
            }
            .show()
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
        val pkg = "com.android.shell"

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
                        AttributionSource::class.java.getConstructor(
                            Int::class.java,
                            String::class.java,
                            String::class.java,
                            Set::class.java,
                            AttributionSource::class.java
                        ).newInstance(Shizuku.getUid(), pkg, pkg, null as Set<String>?, null)
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
    var showingPopup by remember(searchExpanded) {
        mutableStateOf(false)
    }
    var showingSupporters by remember {
        mutableStateOf(false)
    }

    WiFiListTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AnimatedVisibility(visible = !searchExpanded) {
                            Text(
                                text = stringResource(id = R.string.saved_wifi_networks),
                                modifier = Modifier.padding(start = 16.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

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
                            tint = MaterialTheme.colorScheme.onSurface,
                        )

                        AnimatedVisibility(visible = !searchExpanded) {
                            IconButton(onClick = { showingPopup = !showingPopup }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(id = R.string.menu)
                                )

                                Menu(
                                    isShowing = showingPopup,
                                    onDismissRequest = { showingPopup = false },
                                    onShowSupportersDialog = { showingSupporters = !showingSupporters }
                                )
                            }
                        }
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

        SupportersDialog(isShowing = showingSupporters, onDismissRequest = { showingSupporters = false })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WiFiListTheme(darkTheme = true) {
        MainContent(listOf())
    }
}