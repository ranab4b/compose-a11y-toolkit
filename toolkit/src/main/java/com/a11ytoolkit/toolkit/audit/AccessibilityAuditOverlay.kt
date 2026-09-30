package com.a11ytoolkit.toolkit.audit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.a11ytoolkit.toolkit.BuildConfig

/**
 * Debug-only overlay that wraps [content], re-walks its live semantics tree
 * every frame while [enabled] is true, and draws a red outline around any
 * node that violates one of [rules]. The check is a genuine runtime
 * inspection of bounds and semantics via [findAccessibilityIssues] — nothing
 * here is aware of which elements were "meant" to be broken.
 *
 * No-ops outside debug builds so the traversal and drawing never ship to
 * release.
 */
@Composable
fun AccessibilityAuditOverlay(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    rules: List<AuditRule> = DefaultAuditRules,
    onIssuesFound: (List<AuditIssue>) -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (!BuildConfig.DEBUG) {
        Box(modifier = modifier) { content() }
        return
    }

    val view = LocalView.current
    val density = LocalDensity.current
    var overlayCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var issues by remember { mutableStateOf<List<AuditIssue>>(emptyList()) }

    Box(modifier = modifier.onGloballyPositioned { overlayCoordinates = it }) {
        content()

        if (enabled) {
            LaunchedEffect(view) {
                val semanticsOwner = (view as? RootForTest)?.semanticsOwner ?: return@LaunchedEffect
                while (true) {
                    withFrameNanos { }
                    val coordinates = overlayCoordinates
                    if (coordinates != null && coordinates.isAttached) {
                        val found = findAccessibilityIssues(
                            root = semanticsOwner.rootSemanticsNode,
                            density = density,
                            rules = rules,
                        )
                        issues = found
                        onIssuesFound(found)
                    }
                }
            }

            val coordinates = overlayCoordinates
            if (coordinates != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    issues.forEach { issue ->
                        val bounds = issue.node.boundsInWindow
                        val topLeft = coordinates.windowToLocal(bounds.topLeft)
                        drawRect(
                            color = Color.Red,
                            topLeft = topLeft,
                            size = Size(bounds.width, bounds.height),
                            style = Stroke(width = 4.dp.toPx()),
                        )
                    }
                }
            }
        }
    }
}
