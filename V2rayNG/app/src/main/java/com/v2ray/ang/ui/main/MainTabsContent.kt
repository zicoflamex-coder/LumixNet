package com.v2ray.ang.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionCache
import com.v2ray.ang.extension.toSpeedString
import com.v2ray.ang.handler.MmkvManager

/** Connect tab. Everything shown here is live data from the VPN service and the saved servers. */
@Composable
internal fun HomeTabContent(
    statusText: String,
    isRunning: Boolean,
    isBusy: Boolean,
    connectedSeconds: Long,
    usage: SubscriptionUsage?,
    uploadSpeedBytesPerSec: Long,
    downloadSpeedBytesPerSec: Long,
    serverName: String,
    serverType: String,
    pingMillis: Long,
    onAction: (MainAction) -> Unit,
    onOpenServers: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenTestSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anim = LocalLumixAnimations.current
    val lines = statusText.lines()
    val title = lines.firstOrNull().orEmpty().trim()
    val detail = lines.drop(1).joinToString("\n").trim()
        .ifBlank { if (isRunning) "" else stringResource(R.string.lumix_tap_to_connect) }
    val accent by animateColorAsState(if (isRunning) LumixGreen else LumixBlue, lumixTween(anim, 500), label = "accent")
    val glowColor by animateColorAsState(if (isRunning) LumixGreen else LumixBlueLight, lumixTween(anim, 500), label = "glow-color")

    val transition = rememberInfiniteTransition(label = "home")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "spin"
    )
    // One expanding ring every time the VPN connects or disconnects.
    val ripple = remember { Animatable(1f) }
    LaunchedEffect(isRunning) {
        if (anim) {
            ripple.snapTo(0f)
            ripple.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.lumixvpn_logo),
                contentDescription = "LumixVPN",
                modifier = Modifier.size(40.dp)
            )
            Text(
                "LumixVPN",
                color = LumixInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            if (usage != null) {
                val pillShape = RoundedCornerShape(50)
                Row(
                    Modifier
                        .clip(pillShape)
                        .background(Color.White)
                        .border(1.dp, LumixStroke, pillShape)
                        .clickable(onClick = onOpenSubscriptions)
                        .padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(usageSummaryText(usage), color = LumixInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    UsageRing(
                        if (usage.hasVolume) usage.volumeLeftFraction else if (usage.isExpired) 0f else 1f,
                        30.dp, 4.dp
                    )
                }
            }
            LumixRoundIconButton(R.drawable.ic_lumix_ping_24, stringResource(R.string.lumix_test_routes), onOpenTestSheet)
        }

        Spacer(Modifier.height(18.dp))
        Box(Modifier.size(270.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(250.dp)
                    .scale(if (isRunning && anim) pulse else 1f)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(glowColor.copy(alpha = 0.30f), Color.Transparent)))
            )
            Canvas(Modifier.size(270.dp)) {
                if (anim && ripple.value < 1f) {
                    val radius = size.minDimension * (0.28f + 0.22f * ripple.value)
                    drawCircle(accent.copy(alpha = (1f - ripple.value) * 0.45f), radius = radius, style = Stroke(width = 3.dp.toPx()))
                }
                if (isBusy && anim) {
                    val inset = 14.dp.toPx()
                    rotate(degrees = spin) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Color.Transparent, LumixBlue)),
                            startAngle = 250f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2, size.height - inset * 2),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.lumixvpn_logo),
                contentDescription = "LumixVPN",
                modifier = Modifier.size(176.dp)
            )
        }

        Spacer(Modifier.height(6.dp))
        AnimatedContent(
            targetState = title,
            transitionSpec = {
                if (anim) {
                    (fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 3 }) togetherWith fadeOut(tween(140))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "status-title"
        ) { text ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(accent.copy(alpha = 0.10f))
                    .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
                Text(text, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (isRunning && connectedSeconds >= 0L) {
            Spacer(Modifier.height(10.dp))
            Text(formatDuration(connectedSeconds), color = LumixInk, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (detail.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(detail, color = LumixMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(22.dp))
        AnimatedContent(
            targetState = isRunning,
            transitionSpec = {
                if (anim) fadeIn(tween(300)) togetherWith fadeOut(tween(200)) else EnterTransition.None togetherWith ExitTransition.None
            },
            label = "connect-button"
        ) { running ->
            if (running) {
                LumixPrimaryButton(
                    text = stringResource(R.string.lumix_disconnect_vpn),
                    onClick = { onAction(MainAction.ToggleService) },
                    modifier = Modifier.fillMaxWidth(),
                    iconRes = R.drawable.ic_lumix_power_24,
                    gradient = LumixRedGradient
                )
            } else {
                LumixPrimaryButton(
                    text = stringResource(R.string.lumix_connect_vpn),
                    onClick = { onAction(MainAction.ToggleService) },
                    modifier = Modifier.fillMaxWidth(),
                    iconRes = R.drawable.ic_lumix_power_24
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        LumixCard(Modifier.fillMaxWidth(), onClick = onOpenReport) {
            Row(Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                StatCell(stringResource(R.string.lumix_download), downloadSpeedBytesPerSec.toSpeedString(), Modifier.weight(1f))
                StatDivider()
                StatCell(stringResource(R.string.lumix_upload), uploadSpeedBytesPerSec.toSpeedString(), Modifier.weight(1f))
                StatDivider()
                StatCell(
                    stringResource(R.string.lumix_ping),
                    if (pingMillis > 0L) stringResource(R.string.server_test_delay_value, pingMillis) else "—",
                    Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        LumixCard(Modifier.fillMaxWidth()) {
            LumixListRow(
                title = serverName.ifBlank { stringResource(R.string.lumix_select_server_hint) },
                subtitle = if (serverName.isBlank()) null else serverType.ifBlank { null },
                iconRes = R.drawable.ic_lumix_globe_24,
                onClick = onOpenServers
            )
            LumixRowDivider()
            LumixListRow(
                title = stringResource(R.string.lumix_open_report),
                subtitle = stringResource(R.string.lumix_open_report_desc),
                iconRes = R.drawable.ic_lumix_chart_24,
                onClick = onOpenReport
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LumixTonalButton(
                stringResource(R.string.lumix_refresh_subscription),
                { onAction(MainAction.UpdateSubscriptions) },
                Modifier.weight(1f),
                R.drawable.ic_lumix_refresh_24
            )
            LumixTonalButton(
                stringResource(R.string.lumix_real_ping),
                { onAction(MainAction.TestRealAllServers) },
                Modifier.weight(1f),
                R.drawable.ic_lumix_ping_24
            )
        }
        Spacer(Modifier.height(130.dp))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = LumixMuted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).height(34.dp).background(LumixStroke))
}

@Composable
fun EmptyServersState(
    onImportAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(R.drawable.lumixvpn_logo), contentDescription = null, modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.lumix_empty_servers), color = LumixInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.lumix_empty_servers_desc),
            color = LumixMuted, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        LumixCard(Modifier.fillMaxWidth()) {
            LumixListRow(stringResource(R.string.lumix_import_clipboard), stringResource(R.string.lumix_import_clipboard_desc), R.drawable.ic_copy, { onImportAction(MainAction.ImportClipboard) })
            LumixRowDivider()
            LumixListRow(stringResource(R.string.lumix_scan_qr), stringResource(R.string.lumix_scan_qr_short_desc), R.drawable.ic_scan_24dp, { onImportAction(MainAction.ImportQRcode) })
            LumixRowDivider()
            LumixListRow(stringResource(R.string.lumix_import_file), stringResource(R.string.lumix_import_file_desc), R.drawable.ic_file_24dp, { onImportAction(MainAction.ImportConfigLocal) })
        }
        Spacer(Modifier.height(130.dp))
    }
}

/** Header of the Servers tab: title, add/more menus and a search field. */
@Composable
internal fun ServersHeader(
    groupCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenTestSheet: () -> Unit,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit
) {
    var showImportMenu by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.title_server), color = LumixInk, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.lumix_server_header_desc, groupCount), color = LumixMuted, fontSize = 12.sp)
            }
            LumixRoundIconButton(R.drawable.ic_lumix_ping_24, stringResource(R.string.lumix_test_routes), onOpenTestSheet)
            Box {
                LumixRoundIconButton(R.drawable.ic_add_24dp, stringResource(R.string.acc_add)) { showImportMenu = true }
                DropdownMenu(
                    expanded = showImportMenu,
                    onDismissRequest = { showImportMenu = false },
                    containerColor = Color.White
                ) {
                    ImportMenuContent(onAction = { action ->
                        showImportMenu = false
                        onAction(action)
                    })
                }
            }
            Box {
                LumixRoundIconButton(R.drawable.ic_more_vert_24dp, stringResource(R.string.acc_more)) { showMenu = true }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = Color.White
                ) {
                    MoreMenuContent { action ->
                        showMenu = false
                        onMoreMenuAction(action)
                    }
                }
            }
        }
        val fieldShape = RoundedCornerShape(50)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clip(fieldShape)
                .background(Color.White)
                .border(1.dp, LumixStroke, fieldShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(painterResource(R.drawable.ic_search_24dp), contentDescription = null, tint = LumixMuted, modifier = Modifier.size(20.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = LumixInk, fontSize = 14.sp),
                cursorBrush = SolidColor(LumixBlue),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text(stringResource(R.string.lumix_search_servers), color = LumixMuted, fontSize = 14.sp)
                    }
                    inner()
                }
            )
        }
    }
}

/** Account tab: subscriptions, server tools and the app destinations (formerly the drawer). */
@Composable
internal fun AccountTabContent(
    subscriptions: List<SubscriptionCache>,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onOpenAddSheet: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var protocolsExpanded by remember { mutableStateOf(false) }
    val serverTools = listOf(
        MainMoreMenuAction.SortByTestResults,
        MainMoreMenuAction.LocateSelected,
        MainMoreMenuAction.TestAll,
        MainMoreMenuAction.DeleteDuplicate,
        MainMoreMenuAction.DeleteInvalid,
        MainMoreMenuAction.DeleteAll,
        MainMoreMenuAction.ExportAll,
        MainMoreMenuAction.RestartService
    )
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { LumixScreenTitle(stringResource(R.string.lumix_account_title), stringResource(R.string.lumix_account_desc)) }

        item {
            SubscriptionSummaryCard(
                usage = subscriptions.primaryUsage(),
                onRenew = onOpenSupport,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                LumixListRow(stringResource(R.string.lumix_open_report), stringResource(R.string.lumix_open_report_desc), R.drawable.ic_lumix_chart_24, onOpenReport)
                LumixRowDivider()
                LumixListRow(stringResource(R.string.lumix_settings_help), null, R.drawable.ic_lumix_help_24, onOpenSupport)
            }
        }

        item { LumixSectionLabel(stringResource(R.string.lumix_subscriptions_section)) }
        item {
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                if (subscriptions.isEmpty()) {
                    Text(
                        stringResource(R.string.lumix_no_subscriptions),
                        color = LumixMuted, fontSize = 13.sp,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    subscriptions.forEachIndexed { index, sub ->
                        val count = remember(sub.guid, subscriptions) { MmkvManager.decodeServerList(sub.guid).size }
                        val state = stringResource(
                            if (sub.subscription.enabled) R.string.lumix_sub_active else R.string.lumix_sub_disabled
                        )
                        LumixListRow(
                            title = sub.subscription.remarks.ifBlank { sub.guid },
                            subtitle = stringResource(R.string.lumix_servers_count, count) + " · " + state,
                            iconRes = R.drawable.ic_subscriptions_24dp,
                            onClick = onOpenSubscriptions
                        )
                        if (index != subscriptions.lastIndex) LumixRowDivider()
                    }
                }
                LumixRowDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LumixTonalButton(
                        stringResource(R.string.lumix_refresh_subscription),
                        { onAction(MainAction.UpdateSubscriptions) },
                        Modifier.weight(1f),
                        R.drawable.ic_lumix_refresh_24
                    )
                    LumixTonalButton(
                        stringResource(R.string.lumix_manage),
                        onOpenSubscriptions,
                        Modifier.weight(1f),
                        R.drawable.ic_settings_24dp
                    )
                }
                Box(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                    LumixPrimaryButton(
                        stringResource(R.string.lumix_add_subscription),
                        onOpenAddSheet,
                        Modifier.fillMaxWidth(),
                        R.drawable.ic_add_24dp
                    )
                }
            }
        }

        item { LumixSectionLabel(stringResource(R.string.lumix_import_tools)) }
        item {
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                LumixListRow(stringResource(R.string.menu_item_import_config_qrcode), stringResource(R.string.lumix_scan_qr_desc), R.drawable.ic_scan_24dp, { onAction(MainAction.ImportQRcode) })
                LumixRowDivider()
                LumixListRow(stringResource(R.string.menu_item_import_config_clipboard), stringResource(R.string.lumix_clipboard_desc), R.drawable.ic_copy, { onAction(MainAction.ImportClipboard) })
                LumixRowDivider()
                LumixListRow(stringResource(R.string.menu_item_import_config_local), stringResource(R.string.lumix_local_file_desc), R.drawable.ic_file_24dp, { onAction(MainAction.ImportConfigLocal) })
                LumixRowDivider()
                LumixListRow(
                    title = stringResource(if (protocolsExpanded) R.string.lumix_protocols_collapse else R.string.lumix_protocols_expand),
                    subtitle = stringResource(R.string.lumix_protocols_desc),
                    iconRes = R.drawable.ic_add_24dp,
                    onClick = { protocolsExpanded = !protocolsExpanded },
                    showChevron = false
                )
                if (protocolsExpanded) {
                    ImportMenuAction.entries.drop(3).forEach { item ->
                        LumixRowDivider()
                        LumixListRow(stringResource(item.labelRes), null, R.drawable.ic_add_24dp, { onAction(item.action) })
                    }
                }
            }
        }

        item { LumixSectionLabel(stringResource(R.string.lumix_server_tools)) }
        item {
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                serverTools.forEachIndexed { index, action ->
                    LumixListRow(stringResource(action.labelRes), null, R.drawable.ic_settings_24dp, { onMoreMenuAction(action) })
                    if (index != serverTools.lastIndex) LumixRowDivider()
                }
            }
        }

        item { LumixSectionLabel(stringResource(R.string.lumix_app_section)) }
        item {
            LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                MainDestination.entries.forEachIndexed { index, destination ->
                    LumixListRow(stringResource(destination.labelRes), null, destination.iconRes, { onNavigate(destination) })
                    if (index != MainDestination.entries.lastIndex) LumixRowDivider()
                }
            }
        }

        item {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painterResource(R.drawable.lumixvpn_logo), contentDescription = null, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(6.dp))
                Text("LumixVPN", color = LumixMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item { Spacer(Modifier.height(110.dp)) }
    }
}

/** Shows the server with the lowest ping from the last test, with a one-tap use button. */
@Composable
internal fun BestRouteCard(name: String, delayMillis: Long, onUse: () -> Unit) {
    LumixCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LumixIconBadge(R.drawable.ic_lumix_ping_24)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.lumix_best_route), color = LumixBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(name, color = LumixInk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(
                    stringResource(R.string.server_test_delay_value, delayMillis) + " · " + stringResource(R.string.lumix_best_route_desc),
                    color = LumixMuted, fontSize = 12.sp, lineHeight = 17.sp
                )
            }
            LumixTonalButton(stringResource(R.string.lumix_best_route_use), onUse, Modifier, R.drawable.ic_lumix_power_24)
        }
    }
}
