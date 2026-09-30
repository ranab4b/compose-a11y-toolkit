package com.a11ytoolkit.toolkit.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * A [Card] that, when made clickable via [onClick], requires a non-blank
 * [contentDescription] — a card's visible content is usually a loose mix of
 * text and images with no single accessible label the way a button's text
 * provides one, so this fails fast at composition time instead of shipping a
 * silent screen reader trap. Also enforces [MinTouchTargetSize] when
 * clickable.
 */
@Composable
fun A11yCard(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    require(onClick == null || !contentDescription.isNullOrBlank()) {
        "A11yCard: a clickable card must be given a non-blank contentDescription " +
            "so screen readers can announce what it does."
    }

    val resolvedModifier = if (onClick != null) {
        Modifier
            .sizeIn(minWidth = MinTouchTargetSize, minHeight = MinTouchTargetSize)
            .then(modifier)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            }
    } else {
        modifier
    }

    Card(modifier = resolvedModifier) {
        content()
    }
}
