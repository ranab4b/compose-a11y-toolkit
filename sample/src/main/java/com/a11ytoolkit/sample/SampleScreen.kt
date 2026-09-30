package com.a11ytoolkit.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.a11ytoolkit.toolkit.components.A11yButton
import com.a11ytoolkit.toolkit.components.A11yCard
import com.a11ytoolkit.toolkit.components.A11yTextField

/**
 * Mostly built from the toolkit's accessibility-correct-by-default
 * components. The "Broken" section at the bottom deliberately bypasses the
 * toolkit with raw Compose primitives — a missing content description and an
 * undersized touch target, twice over — so [com.a11ytoolkit.toolkit.audit.AccessibilityAuditOverlay]
 * has real issues to find when Audit Mode is on, rather than a staged demo.
 */
@Composable
fun SampleScreen(
    auditModeEnabled: Boolean,
    onAuditModeChange: (Boolean) -> Unit,
) {
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Compose A11y Toolkit — Sample",
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = auditModeEnabled,
                    onValueChange = onAuditModeChange,
                    role = Role.Switch,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Audit Mode", modifier = Modifier.weight(1f))
            Switch(checked = auditModeEnabled, onCheckedChange = null)
        }

        HorizontalDivider()

        Text(text = "Built with the toolkit", style = MaterialTheme.typography.titleMedium)

        A11yButton(onClick = { /* like */ }, text = "Like this post")

        A11yCard(
            contentDescription = "Open the article: Accessibility isn't optional",
            onClick = { /* open article */ },
        ) {
            Text(
                text = "Accessibility isn't optional",
                modifier = Modifier.padding(16.dp),
            )
        }

        A11yTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = "Add a note",
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        Text(
            text = "Raw Compose — bypasses the toolkit (intentionally broken)",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Broken #1: icon-only button with no content description.
            IconButton(
                onClick = { /* favorite */ },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
            }

            // Broken #2: touch target well under the 48dp minimum.
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* delete */ }
                    .semantics { contentDescription = "Delete item" },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }

            // Broken #3: a raw clickable icon with no content description.
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { /* share */ },
            )
        }
    }
}
