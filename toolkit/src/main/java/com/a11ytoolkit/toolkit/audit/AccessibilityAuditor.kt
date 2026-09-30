package com.a11ytoolkit.toolkit.audit

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.unit.Density

/**
 * One (node, rule) pair where [AuditRule.isViolatedBy] returned true.
 */
data class AuditIssue(
    val node: SemanticsNode,
    val rule: AuditRule,
)

/**
 * Walks the semantics tree rooted at [root] — the same merged tree a screen
 * reader like TalkBack perceives — and returns one [AuditIssue] per node/rule
 * violation found anywhere in it. There is no allowlist or denylist of
 * composables: every node reachable from [root] is checked against every
 * rule in [rules].
 */
fun findAccessibilityIssues(
    root: SemanticsNode,
    density: Density,
    rules: List<AuditRule> = DefaultAuditRules,
): List<AuditIssue> {
    val issues = mutableListOf<AuditIssue>()

    fun visit(node: SemanticsNode) {
        for (rule in rules) {
            if (rule.isViolatedBy(node, density)) {
                issues += AuditIssue(node, rule)
            }
        }
        node.children.forEach(::visit)
    }

    visit(root)
    return issues
}
