package com.a11ytoolkit.toolkit.components

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * An [OutlinedTextField] that requires a non-blank [label] — unlike the
 * underlying Material component, where the label is an optional composable
 * — because an unlabeled text field has nothing for a screen reader to
 * announce when it gains focus. Also enforces [MinTouchTargetSize] as a
 * minimum height.
 */
@Composable
fun A11yTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    require(label.isNotBlank()) {
        "A11yTextField: label must not be blank — a screen reader needs it " +
            "to announce what this field is for."
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        modifier = Modifier
            .sizeIn(minHeight = MinTouchTargetSize)
            .then(modifier),
    )
}
