@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.ui.components

import android.net.wifi.WifiConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.zwander.wifilist.R
import tk.zwander.wifilist.util.Preferences.cacheNetworks
import tk.zwander.wifilist.util.Preferences.updateCacheNetworks
import tk.zwander.wifilist.util.Preferences.updateCachedInfo

@Composable
fun SettingsUI(
    loadedNetworks: List<WifiConfiguration>,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.cache_networks),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(id = R.string.cache_networks_desc)
                    )
                }

                Switch(
                    checked = context.cacheNetworks.collectAsState(initial = false).value,
                    onCheckedChange = {
                        scope.launch(Dispatchers.IO) {
                            context.updateCacheNetworks(it)

                            if (it) {
                                context.updateCachedInfo(loadedNetworks)
                            } else {
                                context.updateCachedInfo(listOf())
                            }
                        }
                    }
                )
            }
        }
    }
}
