package com.v2ray.ang.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R

/** Shared white/blue design tokens and small building blocks for the LumixVPN UI. */
internal val LumixBlue = Color(0xFF1E88E5)
internal val LumixBlueLight = Color(0xFF3FB0FF)
internal val LumixBlueDark = Color(0xFF0B63C4)
internal val LumixInk = Color(0xFF0E1B2E)
internal val LumixMuted = Color(0xFF5B6B82)
internal val LumixStroke = Color(0xFFD5E2F3)
internal val LumixGreen = Color(0xFF00A67E)
internal val LumixRed = Color(0xFFD93B4F)
internal val LumixShadowAmbient = Color(0x1A1E88E5)
internal val LumixShadowSpot = Color(0x331E88E5)

internal val LumixBackground: Brush = Brush.verticalGradient(
    listOf(Color(0xFFE6F2FF), Color(0xFFF6FAFF), Color(0xFFFFFFFF))
)
internal val LumixBlueGradient: Brush = Brush.linearGradient(listOf(LumixBlueLight, LumixBlueDark))
internal val LumixRedGradient: Brush = Brush.linearGradient(listOf(Color(0xFFFF6B7A), LumixRed))

@Composable
internal fun LumixCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = LumixShadowAmbient, spotColor = LumixShadowSpot)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, LumixStroke, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content
    )
}

@Composable
internal fun LumixPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    gradient: Brush = LumixBlueGradient
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = LumixShadowAmbient, spotColor = LumixShadowSpot)
            .clip(shape)
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(painterResource(iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1)
    }
}

@Composable
internal fun LumixTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(BorderStroke(1.dp, LumixBlue.copy(alpha = 0.35f)), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(painterResource(iconRes), contentDescription = null, tint = LumixBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, color = LumixBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
internal fun LumixRoundIconButton(iconRes: Int, contentDescription: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, LumixStroke, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), contentDescription = contentDescription, tint = LumixBlue, modifier = Modifier.size(22.dp))
    }
}

@Composable
internal fun LumixIconBadge(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(LumixBlue.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = LumixBlue, modifier = Modifier.size(23.dp))
    }
}

@Composable
internal fun LumixListRow(
    title: String,
    subtitle: String?,
    iconRes: Int,
    onClick: () -> Unit,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LumixIconBadge(iconRes)
        Column(Modifier.weight(1f)) {
            Text(title, color = LumixInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = LumixMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        if (showChevron) {
            Icon(painterResource(R.drawable.ic_lumix_chevron_24), contentDescription = null, tint = LumixMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun LumixRowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 74.dp, end = 16.dp)
            .height(1.dp)
            .background(LumixStroke.copy(alpha = 0.7f))
    )
}

@Composable
internal fun LumixSectionLabel(text: String) {
    Text(
        text,
        color = LumixMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
    )
}

@Composable
internal fun LumixScreenTitle(title: String, subtitle: String?) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp)) {
        Text(title, color = LumixInk, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = LumixMuted, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}
