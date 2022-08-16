package tk.zwander.wifilist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tk.zwander.patreonsupportersretrieval.data.SupporterInfo
import tk.zwander.patreonsupportersretrieval.util.DataParser
import tk.zwander.wifilist.R
import tk.zwander.wifilist.util.launchUrl

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SupportersDialog(
    isShowing: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val supporters = remember {
        mutableStateListOf<SupporterInfo>()
    }

    LaunchedEffect(key1 = null) {
        supporters.clear()
        supporters.addAll(withContext(Dispatchers.IO) {
            DataParser.getInstance(context).parseSupporters()
        })
    }

    if (isShowing) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = modifier.fillMaxWidth(0.7f),
            title = {
                Text(text = stringResource(id = R.string.supporters))
            },
            text = {
                LazyColumn {
                    items(supporters, { it.hashCode() }) {
                        Card(
                            onClick = { context.launchUrl(it.link) },
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.heightIn(min = 48.dp).fillMaxWidth()
                            ) {
                                Text(text = it.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}