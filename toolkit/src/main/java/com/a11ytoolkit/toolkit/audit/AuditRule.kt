package com.a11ytoolkit.toolkit.audit

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/**
 * A single, independently testable accessibility check. A rule only ever
 * looks at a [SemanticsNode]'s resolved semantics config and measured
 * bounds — it has no notion of which composable produced the node, so it
 * flags real issues on whatever is actually on screen rather than a
 * hardcoded list of "known broken" elements.
 */
interface AuditRule {
    val id: String
    val message: String
    fun isViolatedBy(node: SemanticsNode, density: Density): Boolean
}

/**
 * Flags an interactive node (has a click/expand/collapse action) that exposes
 * no accessible label at all: no content description, no visible text, and
 * no state description for a screen reader to announce.
 */
object MissingContentDescriptionRule : AuditRule {
    override val id = "missing_content_description"
    override val message = "Missing content description"

    override fun isViolatedBy(node: SemanticsNode, density: Density): Boolean {
        val config = node.config
        val isInteractive = SemanticsActions.OnClick in config ||
            SemanticsActions.Expand in config ||
            SemanticsActions.Collapse in config
        if (!isInteractive) return false

        val hasContentDescription = config.getOrNull(SemanticsProperties.ContentDescription)
            ?.any { it.isNotBlank() } == true
        val hasText = config.getOrNull(SemanticsProperties.Text)
            ?.any { it.text.isNotBlank() } == true
        val hasStateDescription = config.getOrNull(SemanticsProperties.StateDescription)
            ?.isNotBlank() == true

        return !hasContentDescription && !hasText && !hasStateDescription
    }
}

/**
 * Flags an interactive node whose measured window bounds are smaller than
 * the [MIN_TOUCH_TARGET] recommended by Android and WCAG 2.5.5 (AAA) in
 * either dimension.
 */
object MinTouchTargetRule : AuditRule {
    val MIN_TOUCH_TARGET = 48.dp
    override val id = "min_touch_target"
    override val message = "Touch target smaller than 48dp"

    override fun isViolatedBy(node: SemanticsNode, density: Density): Boolean {
        val config = node.config
        val isInteractive = SemanticsActions.OnClick in config
        if (!isInteractive) return false

        val bounds = node.boundsInWindow
        val minPx = with(density) { MIN_TOUCH_TARGET.toPx() }
        return bounds.width < minPx || bounds.height < minPx
    }
}

val DefaultAuditRules: List<AuditRule> = listOf(
    MissingContentDescriptionRule,
    MinTouchTargetRule,
)
