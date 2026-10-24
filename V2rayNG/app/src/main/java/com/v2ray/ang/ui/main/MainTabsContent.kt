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
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.extension.toSpeedString

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
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LUMIXVPN",
            style = MaterialTheme.typography.headlineSmall,
            color = primaryText
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Private connection, simplified",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryText
        )
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(204.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(
                    (if (isRunning) Color(0xFF36DDB8) else Color(0xFF4D9DFF)).copy(alpha = 0.16f),
                    Color.Transparent
                )))
                .border(1.dp, Color(0xFF80B7FF).copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(174.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(orbColors))
                    .border(1.dp, Color.White.copy(alpha = 0.42f), CircleShape)
                    .clickable { onAction(MainAction.ToggleService) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isRunning) painterResource(R.drawable.ic_stop_24dp)
                    else painterResource(R.drawable.ic_play_24dp),
                    contentDescription = if (isRunning) "Disconnect VPN" else "Connect VPN",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(if (isRunning) Color(0xFF1E806F).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f))
                .border(1.dp, (if (isRunning) Color(0xFF4EE0BB) else Color(0xFF8FB8F3)).copy(alpha = 0.32f), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) Color(0xFF4EE0BB) else Color(0xFF8FB8F3))
            )
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleSmall,
                color = primaryText
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            GlassStatCard(
                label = "آپلود",
                value = uploadSpeedBytesPerSec.toSpeedString(),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            GlassStatCard(
                label = "دانلود",
                value = downloadSpeedBytesPerSec.toSpeedString(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        GlassStatCard(
            label = if (isRunning) "سرور متصل" else "سرور انتخاب‌شده",
            value = connectedServerName.ifBlank { "برای شروع، یک سرور انتخاب کن" },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GlassStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

@Composable
private fun GlassRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ToolsTabContent(
    onImportAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { GlassRow(stringResource(R.string.menu_item_import_config_qrcode)) { onImportAction(MainAction.ImportQRcode) } }
        item { GlassRow(stringResource(R.string.menu_item_import_config_clipboard)) { onImportAction(MainAction.ImportClipboard) } }
        item { GlassRow(stringResource(R.string.menu_item_import_config_local)) { onImportAction(MainAction.ImportConfigLocal) } }
        items(MainMoreMenuAction.entries) { action ->
            GlassRow(stringResource(action.labelRes)) { onMoreMenuAction(action) }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SettingsTabContent(
    onNavigate: (MainDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        items(MainDestination.entries) { destination ->
            GlassRow(stringResource(destination.labelRes)) { onNavigate(destination) }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
