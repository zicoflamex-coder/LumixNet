package com.v2ray.ang.ui.main

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.extension.toSpeedString

private val LumixBlue = Color(0xFF64B7FF)
private val LumixMuted = Color(0xFF9BB2D3)
private val LumixCard = Color(0xFFFFFFFF).copy(alpha = 0.055f)
private val LumixStroke = Color(0xFFB8D8FF).copy(alpha = 0.14f)

@Composable
fun HomeTabContent(
    displayText: String,
    isRunning: Boolean,
    isDarkTheme: Boolean,
    onAction: (MainAction) -> Unit,
    uploadSpeedBytesPerSec: Long,
    downloadSpeedBytesPerSec: Long,
    connectedServerName: String,
    modifier: Modifier = Modifier
) {
    val primaryText = if (isDarkTheme) Color(0xFFF3F7FF) else Color(0xFF10213D)
    val secondaryText = if (isDarkTheme) Color(0xFF9FB2D1) else Color(0xFF536987)
    val orbColors = if (isRunning) {
        listOf(Color(0xFF48E0BC), Color(0xFF147A91), Color(0xFF0C1D3A))
    } else {
        listOf(Color(0xFF8AC7FF), Color(0xFF347CE0), Color(0xFF0C1D3A))
    }
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LUMIXVPN", style = MaterialTheme.typography.headlineSmall, color = primaryText, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        Text("Private connection, simplified", style = MaterialTheme.typography.bodyMedium, color = secondaryText)
        Spacer(Modifier.height(30.dp))
        Box(
            modifier = Modifier.size(204.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf((if (isRunning) Color(0xFF36DDB8) else Color(0xFF4D9DFF)).copy(alpha = 0.16f), Color.Transparent))
                ).border(1.dp, Color(0xFF80B7FF).copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(174.dp).clip(CircleShape).background(Brush.radialGradient(orbColors))
                    .border(1.dp, Color.White.copy(alpha = 0.42f), CircleShape)
                    .clickable { onAction(MainAction.ToggleService) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isRunning) painterResource(R.drawable.ic_stop_24dp) else painterResource(R.drawable.ic_play_24dp),
                    contentDescription = if (isRunning) "Disconnect VPN" else "Connect VPN",
                    tint = Color.White, modifier = Modifier.size(54.dp)
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.clip(RoundedCornerShape(50.dp))
                .background(if (isRunning) Color(0xFF1E806F).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f))
                .border(1.dp, (if (isRunning) Color(0xFF4EE0BB) else Color(0xFF8FB8F3)).copy(alpha = 0.32f), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (isRunning) Color(0xFF4EE0BB) else Color(0xFF8FB8F3)))
            Text(displayText, style = MaterialTheme.typography.titleSmall, color = primaryText)
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassStatCard("آپلود", uploadSpeedBytesPerSec.toSpeedString(), Modifier.weight(1f))
            GlassStatCard("دانلود", downloadSpeedBytesPerSec.toSpeedString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionChip(
                title = "بروزرسانی اشتراک",
                iconRes = R.drawable.ic_lumix_refresh_24,
                modifier = Modifier.weight(1f),
                onClick = { onAction(MainAction.UpdateSubscriptions) }
            )
            QuickActionChip(
                title = "تست پینگ واقعی",
                iconRes = R.drawable.ic_lumix_ping_24,
                modifier = Modifier.weight(1f),
                onClick = { onAction(MainAction.TestRealAllServers) }
            )
        }
        Spacer(Modifier.height(12.dp))
        GlassStatCard(if (isRunning) "سرور متصل" else "سرور انتخاب‌شده", connectedServerName.ifBlank { "برای شروع، یک سرور انتخاب کن" }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun QuickActionChip(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF183B69), Color(0xFF10213D))))
            .border(1.dp, LumixStroke, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(iconRes), contentDescription = title, tint = LumixBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(title, color = Color(0xFFF3F7FF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun GlassStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(LumixCard)
            .border(1.dp, LumixStroke, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LumixMuted)
        Spacer(Modifier.height(5.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
    }
}

@Composable
private fun SectionIntro(title: String, subtitle: String, tag: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF183B69), Color(0xFF0D1B32), Color(0xFF102342))))
            .border(1.dp, Color(0xFF75B9FF).copy(alpha = .22f), RoundedCornerShape(23.dp))
            .padding(18.dp)
    ) {
        Text(tag.uppercase(), color = LumixBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color(0xFFF3F7FF), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = LumixMuted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun GlassActionRow(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp)).background(LumixCard)
            .border(1.dp, LumixStroke, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier.size(43.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF287DE5).copy(alpha = .34f), Color(0xFF142B4F)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = LumixBlue, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = LumixMuted, fontSize = 11.sp, lineHeight = 16.sp)
        }
        Text("‹", color = LumixBlue, fontSize = 25.sp)
    }
}


@Composable
fun EmptyServersState(
    onImportAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2A83F5).copy(alpha = .30f), Color(0xFF142A4D))))
                .border(1.dp, Color(0xFF86C5FF).copy(alpha = .24f), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_subscriptions_24dp), contentDescription = null,
                tint = Color(0xFF8CCBFF), modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("هنوز سروری اضافه نشده", color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text("برای شروع، یک کانفیگ از QR، کلیپ‌بورد یا فایل وارد کن.",
            color = LumixMuted, fontSize = 13.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(22.dp))
        GlassActionRow("واردکردن از کلیپ‌بورد", "افزودن لینک کپی‌شده", R.drawable.ic_copy) {
            onImportAction(MainAction.ImportClipboard)
        }
        GlassActionRow("واردکردن از فایل", "انتخاب فایل کانفیگ از گوشی", R.drawable.ic_file_24dp) {
            onImportAction(MainAction.ImportConfigLocal)
        }
        GlassActionRow("اسکن کد QR", "افزودن کانفیگ با دوربین", R.drawable.ic_add_24dp) {
            onImportAction(MainAction.ImportQRcode)
        }
    }
}

@Composable
fun ServersTabHeader(serverCount: Int, onUpdateSubscriptions: () -> Unit, onTestRealPing: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF183B69), Color(0xFF0D1B32), Color(0xFF102342))))
            .border(1.dp, Color(0xFF75B9FF).copy(alpha = .22f), RoundedCornerShape(22.dp))
            .padding(17.dp)
    ) {
        Text("LUMIX NETWORK", color = LumixBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(7.dp))
        Text("سرورهای تو", color = Color(0xFFF3F7FF), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text("$serverCount گروه سرور · برای انتخاب، یکی از کانفیگ‌ها را لمس کن", color = LumixMuted, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            QuickActionChip("بروزرسانی اشتراک", R.drawable.ic_lumix_refresh_24, Modifier.weight(1f), onUpdateSubscriptions)
            QuickActionChip("تست پینگ واقعی", R.drawable.ic_lumix_ping_24, Modifier.weight(1f), onTestRealPing)
        }
    }
}

@Composable
fun ToolsTabContent(
    onImportAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SectionIntro("ابزارهای اتصال", "واردکردن کانفیگ و مدیریت سرورها را از این بخش انجام بده.", "LUMIX TOOLS") }
        item { GlassActionRow(stringResource(R.string.menu_item_import_config_qrcode), "اسکن کد QR برای افزودن پیکربندی", R.drawable.ic_add_24dp) { onImportAction(MainAction.ImportQRcode) } }
        item { GlassActionRow(stringResource(R.string.menu_item_import_config_clipboard), "خواندن لینک یا کانفیگ کپی‌شده", R.drawable.ic_copy) { onImportAction(MainAction.ImportClipboard) } }
        item { GlassActionRow(stringResource(R.string.menu_item_import_config_local), "واردکردن فایل پیکربندی از گوشی", R.drawable.ic_file_24dp) { onImportAction(MainAction.ImportConfigLocal) } }
        item { Spacer(Modifier.height(13.dp)); Text("مدیریت پیشرفته", color = LumixBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) }
        items(MainMoreMenuAction.entries) { action ->
            GlassActionRow(stringResource(action.labelRes), "عملیات مدیریت و نگهداری پیکربندی‌ها", R.drawable.ic_settings_24dp) { onMoreMenuAction(action) }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
fun SettingsTabContent(
    onNavigate: (MainDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SectionIntro("تنظیمات برنامه", "LumixVPN را مطابق نیاز خود مدیریت کن.", "PERSONALIZE") }
        items(MainDestination.entries) { destination ->
            GlassActionRow(stringResource(destination.labelRes), "تنظیمات و گزینه‌های مربوط به این بخش", R.drawable.ic_settings_24dp) { onNavigate(destination) }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(18.dp)).background(Color(0xFF17345A).copy(alpha = .45f))
                    .border(1.dp, LumixStroke, RoundedCornerShape(18.dp)).padding(16.dp)
            ) {
                Text("LumixVPN", color = Color(0xFFF3F7FF), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(5.dp))
                Text("طراحی اختصاصی با تم آبی و شیشه‌ای", color = LumixMuted, fontSize = 11.sp)
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}
