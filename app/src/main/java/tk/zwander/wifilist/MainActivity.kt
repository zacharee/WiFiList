package tk.zwander.wifilist

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ParceledListSlice
import android.net.wifi.WifiConfiguration
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuProvider
import rikka.shizuku.SystemServiceHelper
import tk.zwander.wifilist.ui.theme.WiFiListTheme
import tk.zwander.wifilist.util.hasShizukuPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import tk.zwander.wifilist.util.stripQuotes

class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {
    companion object {
        private const val SHIZUKU_PERM = 10001
    }

    private val permResultListener = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
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
        val iwm = asInterface.invoke(null, ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.WIFI_SERVICE)))

        val getPrivilegedConfiguredNetworks = base.getMethod("getPrivilegedConfiguredNetworks", String::class.java, String::class.java)
        val privilegedConfigs = getPrivilegedConfiguredNetworks.invoke(iwm, "shell", "com.android.network")
        val privilegedConfigsList = privilegedConfigs::class.java.getMethod("getList").invoke(privilegedConfigs) as List<WifiConfiguration>

        currentNetworks.clear()
        currentNetworks.addAll(privilegedConfigsList.sortedBy { it.SSID.lowercase() })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(networks: List<WifiConfiguration>) {
    val context = LocalContext.current
    val cbm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    WiFiListTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn {
                items(networks) { config ->
                    val psk = config.preSharedKey
                    val wep = config.wepKeys
                    val key = when {
                        !psk.isNullOrBlank() -> psk.stripQuotes()
                        !wep.all { it.isNullOrBlank() } -> wep.joinToString("\n") { it.stripQuotes() }
                        else -> "<NONE>"
                    }

                    Card(
                        modifier = Modifier.padding(4.dp)
                            .fillMaxWidth()
                            .combinedClickable(
                                onLongClick = {
                                    cbm.primaryClip = ClipData.newPlainText(config.SSID, key)
                                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                                },
                                onClick = {}
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = config.printableSsid,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = key)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainContent(listOf())
}