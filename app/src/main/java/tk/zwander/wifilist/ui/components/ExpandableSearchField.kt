package tk.zwander.wifilist.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
    val (expanded, onExpandedChanged) = remember {
        mutableStateOf(expandedInitially)
    }

    Crossfade(targetState = expanded) { isSearchFieldVisible ->
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight()
        ) {
            when (isSearchFieldVisible) {
                true -> ExpandedSearchView(
                    searchDisplay = searchDisplay,
                    onSearchDisplayChanged = onSearchDisplayChanged,
                    onSearchDisplayClosed = onSearchDisplayClosed,
                    onExpandedChanged = onExpandedChanged,
                    modifier = modifier,
                    tint = tint
                )

                false -> CollapsedSearchView(
                    onExpandedChanged = {
                        onExpandedChanged(it)
                        if (it) {
                            onSearchDisplayOpened()
                        }
                    },
                    modifier = modifier,
                    tint = tint
                )
            }
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
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
        IconButton(onClick = {
            onExpandedChanged(false)
            onSearchDisplayClosed()
            keyboardController?.hide()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.back),
                tint = tint
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
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}