package org.friesoft.porturl.ui.utils

import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewConfigurationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Modifier.mouseWheelScroll(
    state: ScrollableState,
    scope: CoroutineScope
): Modifier = composed {
    val context = LocalContext.current
    val config = remember(context) { ViewConfiguration.get(context) }
    // On Android, scroll factors are pixels.
    val scrollFactor = remember(config) {
        ViewConfigurationCompat.getScaledVerticalScrollFactor(config, context)
    }

    pointerInput(state, scrollFactor) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                    val scrollDelta = event.changes.fold(Offset.Zero) { acc, c -> acc + c.scrollDelta }
                    // Android mouse wheel:
                    // Positive Y delta usually means "scroll content down" (finger moves up) in touch terms,
                    // but for Mouse Wheel, AXIS_VSCROLL +1.0 means "scroll up" (content moves down).
                    // Compose scrollBy(pixels): Positive pixels moves content UP (you see lower items).
                    // So if we scroll DOWN (negative AXIS_VSCROLL in some systems, or positive?), we want POSITIVE pixels.

                    // Let's rely on standard Compose behavior which usually normalizes this.
                    // However, we need to manually map "ticks" (scrollDelta.y) to "pixels".
                    // Usually scrollDelta.y is +/- 1.0 for a notch.

                    // If scrollDelta.y is negative (Wheel Down), we want to scroll DOWN (positive pixels).
                    // So we invert the sign.
                    val delta = -scrollDelta.y * scrollFactor

                    if (delta != 0f) {
                        scope.launch {
                            state.scrollBy(delta)
                        }
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        }
    }
}
