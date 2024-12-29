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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuProvider
import rikka.shizuku.SystemServiceHelper
import tk.zwander.wifilist.data.rememberExportChoiceLaunchers
import tk.zwander.wifilist.data.rememberExportChoices
import tk.zwander.wifilist.ui.components.ExpandableSearchView
import tk.zwander.wifilist.ui.components.Menu
import tk.zwander.wifilist.ui.components.SettingsUI
import tk.zwander.wifilist.ui.components.SupportersDialog
import tk.zwander.wifilist.ui.components.WiFiCard
import tk.zwander.wifilist.ui.theme.WiFiListTheme
import tk.zwander.wifilist.util.Preferences.cacheNetworks
import tk.zwander.wifilist.util.Preferences.cachedInfo
import tk.zwander.wifilist.util.Preferences.updateCachedInfo
import tk.zwander.wifilist.util.hasShizukuPermission
import tk.zwander.wifilist.util.launchUrl
import tk.zwander.wifilist.util.plus

class MainActivity : AppCompatActivity(),
    Shizuku.OnRequestPermissionResultListener,
    CoroutineScope by MainScope() {
    companion object {
        private const val SHIZUKU_PERM = 10001
    }

    private val permResultListener =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                showShizukuFailureDialog()
            } else {
                launch {
                    loadNetworks()
                }
            }
        }

    private val currentNetworks = mutableStateListOf<WifiConfiguration>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

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
            launch {
                loadNetworks()
            }
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult != PackageManager.PERMISSION_GRANTED) {
            showShizukuFailureDialog()
        } else {
            launch {
                loadNetworks()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private fun showShizukuFailureDialog() {
        launch {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.shizuku_required_title)
                .setMessage(R.string.shizuku_required_desc)
                .setNegativeButton(R.string.close) { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .apply {
                    try {
                        packageManager.getApplicationInfo("moe.shizuku.privileged.api", 0)
                        setPositiveButton(R.string.open_shizuku) { _, _ ->
                            val shizukuIntent = Intent(Intent.ACTION_MAIN)
                            shizukuIntent.component = ComponentName(
                                "moe.shizuku.privileged.api",
                                "moe.shizuku.manager.MainActivity"
                            )
                            shizukuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(shizukuIntent)
                            finish()
                        }
                    } catch (_: PackageManager.NameNotFoundException) {
                        setPositiveButton(R.string.install_shizuku) { _, _ ->
                            launchUrl("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")
                            finish()
                        }
                    }

                    if (cachedInfo.first().isNotEmpty()) {
                        setNeutralButton(R.string.view_cached) { _, _ ->
                            loadCachedNetworks()
                        }
                    }
                }
                .show()
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

    private fun loadCachedNetworks() {
        launch(Dispatchers.IO) {
            cachedInfo.collect {
                currentNetworks.clear()
                currentNetworks.addAll(it)
            }
        }
    }

    @SuppressLint("PrivateApi")
    private suspend fun loadNetworks() {
        val base = Class.forName("android.net.wifi.IWifiManager")
        val stub = Class.forName("android.net.wifi.IWifiManager\$Stub")
        val asInterface = stub.getMethod("asInterface", IBinder::class.java)
        val iwm = asInterface.invoke(
            null,
            ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.WIFI_SERVICE))
        )

        val user = when (Shizuku.getUid()) {
            0 -> "root"
            1000 -> "system"
            2000 -> "shell"
            else -> throw IllegalArgumentException("Unknown Shizuku user ${Shizuku.getUid()}")
        }
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
            try {
                val getPrivilegedConfiguredNetworks = base.getMethod(
                    "getPrivilegedConfiguredNetworks",
                    String::class.java,
                    String::class.java
                )
                getPrivilegedConfiguredNetworks.invoke(iwm, user, pkg)
            } catch (e: NoSuchMethodException) {
                val getPrivilegedConfiguredNetworks = base.getMethod(
                    "getPrivilegedConfiguredNetworks",
                    String::class.java,
                    String::class.java,
                    Bundle::class.java,
                )
                getPrivilegedConfiguredNetworks.invoke(iwm, user, pkg, null)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val privilegedConfigsList = privilegedConfigs?.let {
            it::class.java.getMethod("getList")
                .invoke(it) as List<WifiConfiguration>
        } ?: listOf()

        val items = privilegedConfigsList
            .sortedBy { it.SSID.lowercase() }
            .distinctBy { it.key }

        currentNetworks.clear()
        currentNetworks.addAll(items)

        if (cacheNetworks.first()) {
            updateCachedInfo(items)
        }
    }
}

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
    var showingSettings by remember {
        mutableStateOf(false)
    }
    var showingExportDialog by remember {
        mutableStateOf(false)
    }

    val exportChoices = rememberExportChoices(configs = networks)
    val exportLaunchers = rememberExportChoiceLaunchers(choices = exportChoices)

    WiFiListTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth()
                            .imePadding(),
                    ) {
                        AnimatedVisibility(
                            visible = !searchExpanded,
                            enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterStart),
                        ) {
                            Text(
                                text = stringResource(id = R.string.saved_wifi_networks),
                                modifier = Modifier.padding(start = 16.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
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

                        AnimatedVisibility(
                            visible = !searchExpanded,
                            enter = fadeIn() + expandIn(expandFrom = Alignment.CenterEnd),
                            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd),
                        ) {
                            IconButton(onClick = { showingPopup = !showingPopup }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(id = R.string.menu),
                                )

                                Menu(
                                    isShowing = showingPopup,
                                    onDismissRequest = { showingPopup = false },
                                    onShowSupportersDialog = {
                                        showingSupporters = !showingSupporters
                                    },
                                    onShowSettings = { showingSettings = true },
                                    onShowExportDialog = { showingExportDialog = true },
                                )
                            }
                        }
                    }
                },
            ) { padding ->
                LazyVerticalStaggeredGrid(
                    contentPadding = padding + PaddingValues(horizontal = 8.dp),
                    columns = StaggeredGridCells.Adaptive(minSize = 400.dp),
                ) {
                    items(
                        items = networks.filter { it.SSID.contains(searchText, true) },
                        key = { item -> item.key },
                    ) { config ->
                        var expanded by remember {
                            mutableStateOf(false)
                        }
                        WiFiCard(
                            config = config,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                            expanded = expanded,
                            onExpandChange = { expanded = it },
                        )
                    }
                }
            }
        }

        SupportersDialog(
            isShowing = showingSupporters,
            onDismissRequest = { showingSupporters = false },
        )

        if (showingSettings) {
            AlertDialog(
                onDismissRequest = {
                    showingSettings = false
                },
                title = {
                    Text(text = stringResource(id = R.string.settings))
                },
                text = {
                    SettingsUI(loadedNetworks = networks)
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier.fillMaxWidth(0.75f),
                confirmButton = {
                    TextButton(onClick = { showingSettings = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
            )
        }

        if (showingExportDialog) {
            AlertDialog(
                onDismissRequest = { showingExportDialog = false },
                title = {
                    Text(text = stringResource(id = R.string.export))
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(exportChoices, { it.mimeType }) { choice ->
                            Card(
                                onClick = {
                                    exportLaunchers[choice.mimeType]?.invoke()
                                    showingExportDialog = false
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = stringResource(id = choice.titleRes))
                                }
                            }
                        }
                    }
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier.fillMaxWidth(0.75f),
                confirmButton = {
                    TextButton(onClick = { showingExportDialog = false }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
            )
        }
    }
}
