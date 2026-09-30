package com.a11ytoolkit.sample

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.a11ytoolkit.toolkit.audit.DefaultAuditRules
import com.a11ytoolkit.toolkit.audit.findAccessibilityIssues
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders the real [SampleScreen] and runs the exact same rule engine that
 * [com.a11ytoolkit.toolkit.audit.AccessibilityAuditOverlay] uses at runtime
 * against its live, merged semantics tree. This is the actual measurement
 * behind the "N/N caught" metric quoted in the README and post — not a
 * hand count.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xhdpi")
class AccessibilityAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sampleScreen_flagsExactlyTheThreeIntentionallyBrokenElements() {
        lateinit var density: Density
        composeTestRule.setContent {
            density = LocalDensity.current
            SampleScreen(auditModeEnabled = false, onAuditModeChange = {})
        }

        val root = composeTestRule.onRoot().fetchSemanticsNode()
        val issues = findAccessibilityIssues(root, density, DefaultAuditRules)

        val report = issues.joinToString(separator = "\n") { issue ->
            "  - ${issue.rule.id}: bounds=${issue.node.boundsInWindow}"
        }
        println("Accessibility issues found on SampleScreen (${issues.size}):\n$report")

        assertEquals(
            "Expected exactly the 3 intentionally-broken elements to be flagged",
            3,
            issues.size,
        )
        assertEquals(1, issues.count { it.rule === com.a11ytoolkit.toolkit.audit.MinTouchTargetRule })
        assertEquals(2, issues.count { it.rule === com.a11ytoolkit.toolkit.audit.MissingContentDescriptionRule })
    }

    @Test
    fun correctlyBuiltToolkitElements_produceNoIssuesOnTheirOwn() {
        // A screen using only the toolkit's own components, with no raw
        // "broken" elements mixed in, should be clean.
        lateinit var density: Density
        composeTestRule.setContent {
            density = LocalDensity.current
            androidx.compose.foundation.layout.Column {
                com.a11ytoolkit.toolkit.components.A11yButton(onClick = {}, text = "Save")
                com.a11ytoolkit.toolkit.components.A11yCard(
                    contentDescription = "Open details",
                    onClick = {},
                ) {
                    androidx.compose.material3.Text("Details")
                }
                com.a11ytoolkit.toolkit.components.A11yTextField(
                    value = "",
                    onValueChange = {},
                    label = "Name",
                )
            }
        }

        val root = composeTestRule.onRoot().fetchSemanticsNode()
        val issues = findAccessibilityIssues(root, density, DefaultAuditRules)

        assertEquals("Toolkit components alone should never be flagged", 0, issues.size)
    }
}
