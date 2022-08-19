package tk.zwander.wifilist.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TwoLineText(
    value: String?,
    label: String,
    modifier: Modifier = Modifier,
    secure: Boolean = false
) {
    TwoLineTextInternal(secure, value, label, modifier)
}

@Composable
private fun TwoLineTextInternal(
    secure: Boolean,
    value: String?,
    label: String,
    modifier: Modifier = Modifier
) {
    var showing by remember(secure) {
        mutableStateOf(!secure)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Crossfade(
            targetState = secure && !showing,
            modifier = Modifier.animateContentSize()
        ) {
            if (it) {
                ValueContent(
                    value = CharArray(value?.length ?: 0) { '·' }.joinToString(""),
                    secure = secure,
                    onShowingChanged = { showing = !showing }
                )
            } else {
                ValueContent(
                    value = value,
                    secure = secure,
                    onShowingChanged = { showing = !showing }
                )
            }
        }

        Text(
            fontSize = 13.sp,
            text = label
        )
    }
}

@Composable
private fun ValueContent(
    value: String?,
    secure: Boolean,
    onShowingChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .then(if (secure) {
                    Modifier.clickable {
                        onShowingChanged()
                    }
                } else {
                    Modifier
                })
                .padding(8.dp)
        ) {
            Text(
                fontSize = 16.sp,
                text = value ?: "",
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
