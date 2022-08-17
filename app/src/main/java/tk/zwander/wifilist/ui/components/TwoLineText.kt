package tk.zwander.wifilist.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun TwoLineText(
    value: String?,
    label: String,
    modifier: Modifier = Modifier,
    secure: Boolean = false
) {
    if (secure) {
        SelectionContainer {
            TwoLineTextInternal(secure, value, label, modifier)
        }
    } else {
        TwoLineTextInternal(secure, value, label, modifier)
    }
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
        Text(
            fontSize = 16.sp,
            text = if (secure && !showing) CharArray(value?.length ?: 0) { '*' }
                .joinToString("") else (value ?: ""),
            modifier = Modifier
                .animateContentSize()
                .then(
                    if (secure) {
                        Modifier.clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null
                        ) {
                            showing = !showing
                        }
                    } else {
                        Modifier
                    }
                ),
        )

        Text(
            fontSize = 13.sp,
            text = label
        )
    }
}
