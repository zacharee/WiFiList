package tk.zwander.wifilist.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tk.zwander.wifilist.R
import tk.zwander.wifilist.util.launchUrl

data class MenuItem(
    @StringRes val textRes: Int,
    val link: String? = null,
    val onClick: ((Context) -> Unit)? = null
) {
    fun handle(context: Context) {
        if (link != null) {
            context.launchUrl(link)
        }

        onClick?.invoke(context)
    }
}

@Composable
fun Menu(
    isShowing: Boolean,
    onDismissRequest: () -> Unit,
    onShowSupportersDialog: () -> Unit,
    onShowSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val links = remember {
        listOf(
            MenuItem(
                R.string.patreon,
                "https://patreon.com/zacharywander"
            ),
            MenuItem(
                R.string.supporters
            ) {
                onShowSupportersDialog()
            },
            MenuItem(
                R.string.website,
                "https://zwander.dev"
            ),
            MenuItem(
                R.string.github,
                "https://github.com/zacharee/WiFiList"
            ),
            MenuItem(
                R.string.twitter,
                "https://twitter.com/Wander1236"
            ),
            MenuItem(
                R.string.settings
            ) {
                onShowSettings()
            }
        )
    }

    val context = LocalContext.current

    DropdownMenu(
        expanded = isShowing,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        links.forEach { link ->
            DropdownMenuItem(
                text = { Text(text = stringResource(id = link.textRes)) },
                onClick = {
                    link.handle(context)
                    onDismissRequest()
                },
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
            )
        }
    }
}