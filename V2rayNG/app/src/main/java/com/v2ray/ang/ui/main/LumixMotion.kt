package com.v2ray.ang.ui.main

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionCache
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.delay
import java.util.Locale

/** True when LumixVPN transitions should play. Provided by MainScreen from the animation setting. */
internal val LocalLumixAnimations = compositionLocalOf { true }

internal const val ANIM_MODE_SYSTEM = "system"
internal const val ANIM_MODE_ON = "full"
internal const val ANIM_MODE_OFF = "off"

internal fun readAnimationMode(): String =
    MmkvManager.decodeSettingsString(AppConfig.PREF_LUMIX_ANIM_MODE, ANIM_MODE_SYSTEM) ?: ANIM_MODE_SYSTEM

/** "system" follows the phone's own animation scale, so Android's "remove animations" is respected. */
internal fun animationsEnabled(mode: String, context: Context): Boolean = when (mode) {
    ANIM_MODE_ON -> true
    ANIM_MODE_OFF -> false
    else -> Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
}

internal fun <T> lumixTween(enabled: Boolean, millis: Int): FiniteAnimationSpec<T> =
    if (enabled) tween(millis) else snap()

/** Seconds since the VPN connected, read from the timestamp the service writes. -1 when unknown or disconnected. */
@Composable
internal fun rememberConnectionSeconds(isRunning: Boolean): Long {
    var seconds by remember { mutableLongStateOf(-1L) }
    LaunchedEffect(isRunning) {
        if (!isRunning) {
            seconds = -1L
            return@LaunchedEffect
        }
        while (true) {
            val startedAt = MmkvManager.decodeSettingsLong(AppConfig.PREF_LUMIX_CONNECT_TS, 0L)
            seconds = if (startedAt > 0L) ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L) else -1L
            delay(1000L)
        }
    }
    return seconds
}

internal fun formatDuration(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d:%02d", h, m, s)
}

internal fun formatBytes(bytes: Long): String {
    val b = bytes.coerceAtLeast(0L).toDouble()
    return when {
        b >= 1024.0 * 1024.0 * 1024.0 -> String.format(Locale.US, "%.2f GB", b / (1024.0 * 1024.0 * 1024.0))
        b >= 1024.0 * 1024.0 -> String.format(Locale.US, "%.1f MB", b / (1024.0 * 1024.0))
        b >= 1024.0 -> String.format(Locale.US, "%.0f KB", b / 1024.0)
        else -> String.format(Locale.US, "%.0f B", b)
    }
}

/** Real usage reported by the subscription panel (Subscription-Userinfo header). */
internal data class SubscriptionUsage(
    val name: String,
    val usedBytes: Long,
    val totalBytes: Long,
    val expireAtSeconds: Long
) {
    val hasVolume: Boolean get() = totalBytes > 0L && usedBytes >= 0L
    val hasExpire: Boolean get() = expireAtSeconds > 0L
    val remainingBytes: Long get() = (totalBytes - usedBytes).coerceAtLeast(0L)
    /** Fraction of the volume that is still left, 0..1. */
    val volumeLeftFraction: Float get() = if (hasVolume) (remainingBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
    val daysLeft: Int
        get() = if (hasExpire) {
            val millis = expireAtSeconds * 1000L - System.currentTimeMillis()
            if (millis <= 0L) 0 else ((millis + 86_399_999L) / 86_400_000L).toInt()
        } else -1
    val isExpired: Boolean get() = hasExpire && expireAtSeconds * 1000L <= System.currentTimeMillis()
}

internal fun SubscriptionCache.toUsage(): SubscriptionUsage? {
    val item = subscription
    if (item.totalBytes <= 0L && item.expireAtSeconds <= 0L) return null
    return SubscriptionUsage(item.remarks.ifBlank { guid }, item.usedBytes, item.totalBytes, item.expireAtSeconds)
}

internal fun List<SubscriptionCache>.primaryUsage(): SubscriptionUsage? =
    filter { it.subscription.enabled }.firstNotNullOfOrNull { it.toUsage() }

@Composable
internal fun usageSummaryText(usage: SubscriptionUsage): String {
    val parts = mutableListOf<String>()
    if (usage.hasExpire) {
        parts += if (usage.isExpired) stringResource(R.string.lumix_expired) else stringResource(R.string.lumix_days_left, usage.daysLeft)
    }
    if (usage.hasVolume) {
        parts += stringResource(R.string.lumix_gb_left, String.format(Locale.US, "%.1f", usage.remainingBytes / 1_000_000_000.0))
    }
    return parts.joinToString(" · ")
}

/** Ring that shows how much of the subscription volume is left. */
@Composable
internal fun UsageRing(fraction: Float, size: Dp, stroke: Dp, color: Color = LumixBlue) {
    Canvas(Modifier.size(size)) {
        val s = stroke.toPx()
        val arcSize = Size(this.size.width - s, this.size.height - s)
        val topLeft = Offset(s / 2f, s / 2f)
        drawArc(color = color.copy(alpha = 0.14f), startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(s, cap = StrokeCap.Round))
        drawArc(brush = Brush.sweepGradient(listOf(LumixBlueLight, LumixBlueDark, LumixBlueLight)), startAngle = -90f, sweepAngle = 360f * fraction.coerceIn(0f, 1f), useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(s, cap = StrokeCap.Round))
    }
}

/** Card with the subscription ring, remaining days and volume, and a renew shortcut. */
@Composable
internal fun SubscriptionSummaryCard(
    usage: SubscriptionUsage?,
    onRenew: () -> Unit,
    modifier: Modifier = Modifier
) {
    LumixCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(stringResource(R.string.lumix_your_subscription), color = LumixMuted, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            if (usage == null) {
                Text(
                    stringResource(R.string.lumix_subscription_no_usage),
                    color = LumixInk, fontSize = 14.sp, lineHeight = 21.sp
                )
            } else {
                Text(usage.name, color = LumixInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        val fraction = when {
                            usage.hasVolume -> usage.volumeLeftFraction
                            usage.isExpired -> 0f
                            else -> 1f
                        }
                        UsageRing(fraction, 112.dp, 11.dp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (usage.hasExpire) usage.daysLeft.toString() else "∞",
                                color = LumixInk, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
                            )
                            Text(stringResource(R.string.lumix_expired).takeIf { usage.isExpired } ?: "", color = LumixRed, fontSize = 10.sp)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(usageSummaryText(usage), color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                        if (usage.hasVolume) {
                            Text(
                                formatBytes(usage.usedBytes) + " / " + formatBytes(usage.totalBytes),
                                color = LumixMuted, fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            LumixPrimaryButton(
                stringResource(R.string.lumix_renew_telegram),
                onRenew,
                Modifier.fillMaxWidth(),
                R.drawable.ic_telegram_24dp
            )
        }
    }
}
