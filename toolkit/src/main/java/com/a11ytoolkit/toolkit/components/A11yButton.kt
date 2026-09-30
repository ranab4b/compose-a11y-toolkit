package com.a11ytoolkit.toolkit.components

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * A [Button] that always exposes a Button [Role] to accessibility services
 * and enforces a minimum [MinTouchTargetSize] touch target, regardless of
 * what the caller passes in [modifier].
 *
 * [text] is required and always visible, so the button always has a
 * screen-reader-visible label. [contentDescription] is optional and only
 * needed when [text] alone isn't descriptive enough (e.g. an abbreviation).
 */
@Composable
fun A11yButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .sizeIn(minWidth = MinTouchTargetSize, minHeight = MinTouchTargetSize)
            .then(modifier)
            .semantics {
                role = Role.Button
                if (!contentDescription.isNullOrBlank()) {
                    this.contentDescription = contentDescription
                }
            },
    ) {
        Text(text)
    }
}
