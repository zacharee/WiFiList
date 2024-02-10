package tk.zwander.wifilist.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun TwoLineText(
    value: String?,
    label: String,
    modifier: Modifier = Modifier,
    secure: Boolean = false,
) {
    TwoLineTextInternal(secure, value, label, modifier)
}

@Composable
private fun TwoLineTextInternal(
    secure: Boolean,
    value: String?,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        ValueContent(
            value = value,
            secure = secure,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = MaterialTheme.typography.bodySmall.fontSize,
        )
    }
}

@Composable
private fun ValueContent(
    value: String?,
    secure: Boolean,
    modifier: Modifier = Modifier,
) {
    var showing by rememberSaveable(secure) {
        mutableStateOf(!secure)
    }
    val secureText = remember(value?.length) {
        CharArray(value?.length ?: 0) { '·' }.joinToString("")
    }

    Card(
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .then(if (secure) {
                    Modifier.clickable {
                        showing = !showing
                    }
                } else {
                    Modifier
                })
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Crossfade(targetState = showing, label = "SecureCrossfade${value}") {
                Text(
                    text = if (it) value ?: "" else secureText,
                    fontFamily = if (secure) FontFamily.Monospace else null,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                )
            }
        }
    }
}
