package tk.zwander.wifilist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import tk.zwander.wifilist.R

//https://gist.github.com/hardiksachan/0b63ab1e18a52d1e5a374a0df0bccb96
@Composable
fun ExpandableSearchView(
    searchDisplay: String,
    onSearchDisplayChanged: (String) -> Unit,
    onSearchDisplayClosed: () -> Unit,
    onSearchDisplayOpened: () -> Unit,
    modifier: Modifier = Modifier,
    expandedInitially: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onPrimary
) {
    var expanded by remember {
        mutableStateOf(expandedInitially)
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxHeight(),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandIn(expandFrom = Alignment.CenterEnd),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd),
        ) {
            ExpandedSearchView(
                searchDisplay = searchDisplay,
                onSearchDisplayChanged = onSearchDisplayChanged,
                onSearchDisplayClosed = onSearchDisplayClosed,
                onExpandedChanged = { expanded = it },
                tint = tint,
            )
        }

        AnimatedVisibility(
            visible = !expanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CollapsedSearchView(
                onExpandedChanged = {
                    expanded = it
                    if (it) {
                        onSearchDisplayOpened()
                    }
                },
                tint = tint,
            )
        }
    }
}

@Composable
fun SearchIcon(iconTint: Color) {
    Icon(
        painter = painterResource(id = R.drawable.ic_search),
        contentDescription = stringResource(id = R.string.search),
        tint = iconTint
    )
}

@Composable
fun CollapsedSearchView(
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Row(
        modifier = modifier
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onExpandedChanged(true) }) {
            SearchIcon(iconTint = tint)
        }
    }
}

@Composable
fun ExpandedSearchView(
    searchDisplay: String,
    onSearchDisplayChanged: (String) -> Unit,
    onSearchDisplayClosed: () -> Unit,
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    val focusManager = LocalFocusManager.current

    val textFieldFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    SideEffect {
        textFieldFocusRequester.requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onExpandedChanged(false)
                onSearchDisplayClosed()
                keyboardController?.hide()
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.back),
                tint = tint,
            )
        }

        TextField(
            value = searchDisplay,
            onValueChange = {
                onSearchDisplayChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .focusRequester(textFieldFocusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            placeholder = {
                Text(stringResource(id = R.string.search))
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                },
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
            ),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        onSearchDisplayChanged("")
                    },
                    enabled = searchDisplay.isNotEmpty(),
                ) {
                    val iconColor by animateColorAsState(
                        targetValue = if (searchDisplay.isNotEmpty()) tint else tint.copy(alpha = LocalContentColor.current.alpha),
                        label = "SearchClearIconColor",
                    )

                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(id = R.string.clear),
                        tint = iconColor,
                    )
                }
            },
        )
    }
}