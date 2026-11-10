package com.v2ray.ang.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionCache
import com.v2ray.ang.extension.toSpeedString
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.QRCodeDecoder

/** Full screens that open on top of the main tabs. */
internal enum class MainOverlay { Report, Settings, Support, Subscriptions }

internal fun Context.openSupportChat() {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.LUMIX_SUPPORT_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
internal fun LumixOverlayScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(LumixBackground)
            .statusBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LumixRoundIconButton(R.drawable.ic_arrow_back_24dp, stringResource(R.string.acc_back), onBack)
            Text(title, color = LumixInk, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
        content()
    }
}

/* ---------------------------------------------------------------- Connection report */

@Composable
internal fun ConnectionReportScreen(
    isRunning: Boolean,
    statusText: String,
    connectedSeconds: Long,
    downloadSpeed: Long,
    uploadSpeed: Long,
    speedHistory: List<Pair<Long, Long>>,
    sessionDownloadBytes: Long,
    sessionUploadBytes: Long,
    serverName: String,
    serverType: String,
    pingMillis: Long,
    exitCountry: String?,
    exitIp: String?,
    onRecheck: () -> Unit,
    onOpenSupport: () -> Unit,
    onBack: () -> Unit
) {
    LumixOverlayScaffold(stringResource(R.string.lumix_report_title), onBack) {
        val title = statusText.lines().firstOrNull().orEmpty().trim()
        val accent = if (isRunning) LumixGreen else LumixMuted
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LumixCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (isRunning && connectedSeconds >= 0L) formatDuration(connectedSeconds) else "—",
                            color = LumixInk, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            Modifier.clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
                            Text(title, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                    Text(stringResource(R.string.lumix_report_duration), color = LumixMuted, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(LumixStroke))
                    Row(Modifier.fillMaxWidth()) {
                        ReportStat(stringResource(R.string.lumix_download), downloadSpeed.toSpeedString(), Modifier.weight(1f))
                        ReportStat(stringResource(R.string.lumix_upload), uploadSpeed.toSpeedString(), Modifier.weight(1f))
                        ReportStat(
                            stringResource(R.string.lumix_ping),
                            if (pingMillis > 0L) stringResource(R.string.server_test_delay_value, pingMillis) else "—",
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            LumixCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.lumix_report_live_speed), color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        val peak = speedHistory.maxOfOrNull { maxOf(it.first, it.second) } ?: 0L
                        Text(stringResource(R.string.lumix_report_peak, peak.toSpeedString()), color = LumixMuted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    SpeedChart(speedHistory, Modifier.fillMaxWidth().height(130.dp))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(LumixBlue, stringResource(R.string.lumix_download))
                        LegendDot(LumixGreen, stringResource(R.string.lumix_upload))
                    }
                }
            }

            LumixCard(Modifier.fillMaxWidth()) {
                LumixListRow(
                    title = stringResource(R.string.lumix_report_session_usage),
                    subtitle = if (isRunning) stringResource(R.string.lumix_report_usage_value, formatBytes(sessionDownloadBytes), formatBytes(sessionUploadBytes)) else "—",
                    iconRes = R.drawable.ic_lumix_chart_24,
                    onClick = {},
                    showChevron = false
                )
            }

            LumixCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.lumix_report_exit), color = LumixGreen.takeIf { exitCountry != null } ?: LumixMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(exitCountry ?: stringResource(R.string.lumix_report_exit_unknown), color = LumixInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    if (!exitIp.isNullOrBlank()) Text(exitIp, color = LumixMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    LumixTonalButton(stringResource(R.string.lumix_report_recheck), onRecheck, Modifier, R.drawable.ic_lumix_refresh_24)
                }
            }

            val perAppOn = MmkvManager.decodeSettingsBool(AppConfig.PREF_PER_APP_PROXY, false)
            val perAppCount = remember(perAppOn) { MmkvManager.decodeSettingsStringSet(AppConfig.PREF_PER_APP_PROXY_SET)?.size ?: 0 }
            LumixCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.lumix_report_checks), color = LumixMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 18.dp, top = 14.dp, end = 18.dp))
                CheckRow(
                    isRunning,
                    stringResource(R.string.lumix_check_tunnel),
                    stringResource(if (isRunning) R.string.lumix_check_tunnel_ok else R.string.lumix_check_tunnel_off)
                )
                LumixRowDivider()
                CheckRow(
                    exitCountry != null,
                    stringResource(R.string.lumix_check_exit),
                    if (exitCountry != null) stringResource(R.string.lumix_check_exit_ok, exitCountry) else stringResource(R.string.lumix_check_exit_none)
                )
                LumixRowDivider()
                CheckRow(
                    pingMillis in 1L..600L,
                    stringResource(R.string.lumix_check_speed),
                    when {
                        pingMillis <= 0L -> stringResource(R.string.lumix_check_speed_none)
                        pingMillis <= 600L -> stringResource(R.string.lumix_check_speed_good, pingMillis)
                        else -> stringResource(R.string.lumix_check_speed_slow, pingMillis)
                    }
                )
                LumixRowDivider()
                CheckRow(
                    true,
                    stringResource(R.string.lumix_check_apps),
                    if (perAppOn) stringResource(R.string.lumix_check_apps_some, perAppCount) else stringResource(R.string.lumix_check_apps_all)
                )
            }

            LumixCard(Modifier.fillMaxWidth()) {
                LumixListRow(
                    title = serverName.ifBlank { stringResource(R.string.lumix_report_current_route) },
                    subtitle = serverType.ifBlank { null },
                    iconRes = R.drawable.ic_lumix_globe_24,
                    onClick = {},
                    showChevron = false
                )
            }

            LumixCard(Modifier.fillMaxWidth(), onClick = onOpenSupport) {
                LumixListRow(
                    title = stringResource(R.string.lumix_report_trouble),
                    subtitle = stringResource(R.string.lumix_report_trouble_desc),
                    iconRes = R.drawable.ic_lumix_help_24,
                    onClick = onOpenSupport
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportStat(label: String, value: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = LumixMuted, fontSize = 12.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = LumixMuted, fontSize = 12.sp)
    }
}

@Composable
private fun CheckRow(ok: Boolean, title: String, detail: String) {
    val tint = if (ok) LumixGreen else LumixMuted
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_lumix_check_24), contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = LumixInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = LumixMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

/** Line chart of the last measured download (blue) and upload (green) speeds. */
@Composable
private fun SpeedChart(history: List<Pair<Long, Long>>, modifier: Modifier) {
    Canvas(modifier) {
        val gridColor = LumixStroke
        for (i in 0..2) {
            val y = size.height * i / 2f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        if (history.size < 2) return@Canvas
        val maxValue = history.maxOf { maxOf(it.first, it.second) }.coerceAtLeast(1024L).toFloat()
        val stepX = size.width / (60 - 1).toFloat()
        val startX = size.width - stepX * (history.size - 1)
        fun path(select: (Pair<Long, Long>) -> Long): Path {
            val p = Path()
            history.forEachIndexed { index, sample ->
                val x = startX + stepX * index
                val y = size.height - (select(sample).toFloat() / maxValue) * (size.height - 6.dp.toPx()) - 3.dp.toPx()
                if (index == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            return p
        }
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(path { it.second }, LumixGreen, style = stroke)
        drawPath(path { it.first }, LumixBlue, style = stroke)
    }
}

/* ---------------------------------------------------------------- Settings */

@Composable
private fun LumixSwitchRow(iconRes: Int, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LumixIconBadge(iconRes)
        Column(Modifier.weight(1f)) {
            Text(title, color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = LumixMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LumixBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = LumixStroke,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
internal fun LumixSettingsScreen(
    animationMode: String,
    onAnimationModeChange: (String) -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onOpenSupport: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var haptic by remember { mutableStateOf(MmkvManager.decodeSettingsBool(AppConfig.PREF_LUMIX_HAPTIC, true)) }
    var speedNotification by remember { mutableStateOf(MmkvManager.decodeSettingsBool(AppConfig.PREF_SPEED_ENABLED, true)) }

    LumixOverlayScaffold(stringResource(R.string.lumix_settings_title), onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            LumixSectionLabel(stringResource(R.string.lumix_settings_connection))
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                LumixSwitchRow(
                    R.drawable.ic_lumix_vibrate_24,
                    stringResource(R.string.lumix_settings_haptic),
                    stringResource(R.string.lumix_settings_haptic_desc),
                    haptic
                ) {
                    haptic = it
                    MmkvManager.encodeSettings(AppConfig.PREF_LUMIX_HAPTIC, it)
                }
                LumixRowDivider()
                LumixSwitchRow(
                    R.drawable.ic_lumix_chart_24,
                    stringResource(R.string.lumix_settings_speed_notification),
                    stringResource(R.string.lumix_settings_speed_notification_desc),
                    speedNotification
                ) {
                    speedNotification = it
                    MmkvManager.encodeSettings(AppConfig.PREF_SPEED_ENABLED, it)
                }
                LumixRowDivider()
                LumixListRow(
                    stringResource(R.string.lumix_settings_killswitch),
                    stringResource(R.string.lumix_settings_killswitch_desc),
                    R.drawable.ic_lock_24dp,
                    { runCatching { context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
                )
            }

            LumixSectionLabel(stringResource(R.string.lumix_settings_apps_section))
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                LumixListRow(
                    stringResource(R.string.per_app_proxy_settings),
                    stringResource(R.string.lumix_settings_apps_desc),
                    R.drawable.ic_per_apps_24dp,
                    { onNavigate(MainDestination.PerAppProxy) }
                )
                LumixRowDivider()
                LumixListRow(
                    stringResource(R.string.routing_settings_title),
                    stringResource(R.string.lumix_settings_routing_desc),
                    R.drawable.ic_routing_24dp,
                    { onNavigate(MainDestination.Routing) }
                )
            }

            LumixSectionLabel(stringResource(R.string.lumix_settings_display))
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        LumixIconBadge(R.drawable.ic_lumix_motion_24)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.lumix_settings_animations), color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.lumix_settings_animations_desc), color = LumixMuted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0xFFF1F6FD)).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            ANIM_MODE_SYSTEM to R.string.lumix_anim_system,
                            ANIM_MODE_ON to R.string.lumix_anim_on,
                            ANIM_MODE_OFF to R.string.lumix_anim_off
                        ).forEach { (mode, label) ->
                            val selected = animationMode == mode
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) Color.White else Color.Transparent)
                                    .then(if (selected) Modifier.border(1.dp, LumixBlue.copy(alpha = 0.35f), RoundedCornerShape(50)) else Modifier)
                                    .clickable {
                                        MmkvManager.encodeSettings(AppConfig.PREF_LUMIX_ANIM_MODE, mode)
                                        onAnimationModeChange(mode)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(label),
                                    color = if (selected) LumixBlue else LumixMuted,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                LumixRowDivider()
                LumixListRow(
                    stringResource(R.string.lumix_settings_notifications),
                    stringResource(R.string.lumix_settings_notifications_desc),
                    R.drawable.ic_lumix_bell_24,
                    {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                )
                LumixRowDivider()
                LumixListRow(
                    stringResource(R.string.lumix_settings_advanced),
                    stringResource(R.string.lumix_settings_advanced_desc),
                    R.drawable.ic_settings_24dp,
                    { onNavigate(MainDestination.Settings) }
                )
            }

            LumixSectionLabel(stringResource(R.string.lumix_settings_more))
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                LumixListRow(
                    stringResource(R.string.lumix_settings_help), null, R.drawable.ic_lumix_help_24, onOpenSupport
                )
                LumixRowDivider()
                LumixListRow(stringResource(R.string.title_configuration_backup_restore), null, R.drawable.ic_restore_24dp, { onNavigate(MainDestination.BackupRestore) })
                LumixRowDivider()
                LumixListRow(stringResource(R.string.update_check_for_update), null, R.drawable.ic_check_update_24dp, { onNavigate(MainDestination.CheckUpdate) })
                LumixRowDivider()
                LumixListRow(stringResource(R.string.title_about), null, R.drawable.ic_about_24dp, { onNavigate(MainDestination.About) })
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

/* ---------------------------------------------------------------- Support */

@Composable
internal fun SupportScreen(
    reportText: String,
    onNavigate: (MainDestination) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.lumix_support_copied)
    LumixOverlayScaffold(stringResource(R.string.lumix_support_title), onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            LumixSectionLabel(stringResource(R.string.lumix_support_contact))
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                LumixListRow(
                    stringResource(R.string.lumix_support_telegram),
                    AppConfig.LUMIX_SUPPORT_URL.removePrefix("https://t.me/").let { "@$it" },
                    R.drawable.ic_telegram_24dp,
                    { context.openSupportChat() }
                )
                LumixRowDivider()
                LumixListRow(
                    stringResource(R.string.lumix_support_copy_report),
                    stringResource(R.string.lumix_support_copy_report_desc),
                    R.drawable.ic_copy,
                    {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("LumixVPN report", reportText))
                        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                    },
                    showChevron = false
                )
                LumixRowDivider()
                LumixListRow(
                    stringResource(R.string.lumix_support_logs),
                    stringResource(R.string.lumix_support_logs_desc),
                    R.drawable.ic_logcat_24dp,
                    { onNavigate(MainDestination.Logcat) }
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

internal fun buildSupportReport(
    isRunning: Boolean,
    serverType: String,
    pingMillis: Long,
    exitCountry: String?,
    connectedSeconds: Long
): String = buildString {
    appendLine("LumixVPN ${BuildConfig.VERSION_NAME}")
    appendLine("Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
    appendLine("Connected: $isRunning")
    if (isRunning && connectedSeconds >= 0L) appendLine("Connected for: ${formatDuration(connectedSeconds)}")
    if (serverType.isNotBlank()) appendLine("Protocol: $serverType")
    if (pingMillis > 0L) appendLine("Ping: $pingMillis ms")
    if (!exitCountry.isNullOrBlank()) appendLine("Exit: $exitCountry")
}

/* ---------------------------------------------------------------- Subscriptions */

@Composable
internal fun SubscriptionsScreen(
    subscriptions: List<SubscriptionCache>,
    onUpdate: () -> Unit,
    onDelete: (String) -> Unit,
    onShowQr: (android.graphics.Bitmap) -> Unit,
    onOpenAddSheet: () -> Unit,
    onOpenSupport: () -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onBack: () -> Unit
) {
    var pendingDelete by remember { mutableStateOf<SubscriptionCache?>(null) }
    LumixOverlayScaffold(stringResource(R.string.lumix_subs_title), onBack) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (subscriptions.isEmpty()) {
                LumixCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.lumix_no_subscriptions), color = LumixMuted, fontSize = 13.sp, modifier = Modifier.padding(18.dp))
                }
            }
            subscriptions.forEach { sub ->
                val usage = sub.toUsage()
                val count = remember(sub.guid, subscriptions) { MmkvManager.decodeServerList(sub.guid).size }
                LumixCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (usage != null && usage.hasVolume) {
                                Box(contentAlignment = Alignment.Center) {
                                    UsageRing(usage.volumeLeftFraction, 64.dp, 7.dp)
                                    Text(
                                        if (usage.hasExpire) usage.daysLeft.toString() else "",
                                        color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                LumixIconBadge(R.drawable.ic_subscriptions_24dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(sub.subscription.remarks.ifBlank { sub.guid }, color = LumixInk, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                val detail = buildList {
                                    if (usage != null) add(usageSummaryText(usage))
                                    add(stringResource(R.string.lumix_servers_count, count))
                                    add(stringResource(if (sub.subscription.enabled) R.string.lumix_sub_active else R.string.lumix_sub_disabled))
                                }.filter { it.isNotBlank() }.joinToString(" · ")
                                Text(detail, color = LumixMuted, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(LumixStroke))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LumixTonalButton(stringResource(R.string.lumix_subs_update), onUpdate, Modifier.weight(1f), R.drawable.ic_lumix_refresh_24)
                            if (sub.subscription.url.isNotBlank()) {
                                LumixTonalButton(
                                    stringResource(R.string.lumix_subs_qr),
                                    { QRCodeDecoder.createQRCode(sub.subscription.url)?.let(onShowQr) },
                                    Modifier.weight(1f),
                                    R.drawable.ic_scan_24dp
                                )
                            }
                            LumixTonalButton(stringResource(R.string.lumix_subs_delete), { pendingDelete = sub }, Modifier.weight(1f), R.drawable.ic_delete_24dp)
                        }
                    }
                }
            }
            LumixPrimaryButton(stringResource(R.string.lumix_add_subscription), onOpenAddSheet, Modifier.fillMaxWidth(), R.drawable.ic_add_24dp)
            LumixTonalButton(stringResource(R.string.lumix_renew_telegram), onOpenSupport, Modifier.fillMaxWidth(), R.drawable.ic_telegram_24dp)
            LumixTonalButton(stringResource(R.string.lumix_subs_edit), { onNavigate(MainDestination.Subscriptions) }, Modifier.fillMaxWidth(), R.drawable.ic_settings_24dp)
            Spacer(Modifier.height(32.dp))
        }
    }
    pendingDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = Color.White,
            title = { Text(stringResource(R.string.lumix_subs_delete_title), color = LumixInk, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.lumix_subs_delete_desc), color = LumixMuted) },
            confirmButton = {
                TextButton(onClick = { pendingDelete = null; onDelete(sub.guid) }) {
                    Text(stringResource(R.string.lumix_subs_delete), color = LumixRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.lumix_cancel), color = LumixBlue) }
            }
        )
    }
}
