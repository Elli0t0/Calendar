package io.github.elli0t0.calendar.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The shared filled text field for sheets and search: surfaceContainerHigh,
 * 16dp corners, no border or indicator line, at least 56dp tall, muted
 * label; focus only changes the cursor and label color to the accent.
 * When a field gets focus it scrolls itself into view above the keyboard.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val scheme = MaterialTheme.colorScheme
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = suffix,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainerHigh,
            unfocusedContainerColor = scheme.surfaceContainerHigh,
            disabledContainerColor = scheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = scheme.primary,
            focusedLabelColor = scheme.primary,
            unfocusedLabelColor = scheme.onSurfaceVariant,
            focusedTextColor = scheme.onSurface,
            unfocusedTextColor = scheme.onSurface,
            focusedSuffixColor = scheme.onSurfaceVariant,
            unfocusedSuffixColor = scheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    scope.launch {
                        // Let the keyboard finish opening so the scroll
                        // target is measured against the visible area.
                        delay(250)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            }
    )
}

/** Fully rounded save button: accent when enabled, surfaceContainerHigh
 * with muted text when disabled. */
@Composable
fun SheetSaveButton(
    onClick: () -> Unit,
    enabled: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
            disabledContainerColor = scheme.surfaceContainerHigh,
            disabledContentColor = scheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}
