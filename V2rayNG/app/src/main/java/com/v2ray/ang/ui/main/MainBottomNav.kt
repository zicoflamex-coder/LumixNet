package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R

/** Main app navigation: Connect, Servers and Account. */
enum class MainTab(val labelRes: Int, val iconRes: Int) {
    Home(R.string.lumix_tab_connect, R.drawable.ic_lumix_power_24),
    Servers(R.string.title_server, R.drawable.ic_lumix_globe_24),
    Account(R.string.lumix_tab_account, R.drawable.ic_lumix_person_24)
}

@Composable
fun MainBottomNav(
    selectedTab: MainTab,
    onTabSelect: (MainTab) -> Unit
) {
    val barShape = RoundedCornerShape(32.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
            .shadow(14.dp, barShape, ambientColor = LumixShadowAmbient, spotColor = LumixShadowSpot)
            .clip(barShape)
            .background(Color.White.copy(alpha = 0.96f))
            .border(1.dp, LumixStroke, barShape)
            .padding(7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val itemShape = RoundedCornerShape(26.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(itemShape)
                    .then(if (selected) Modifier.background(LumixBlue.copy(alpha = 0.12f)) else Modifier)
                    .clickable { onTabSelect(tab) }
                    .padding(vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    painter = painterResource(tab.iconRes),
                    contentDescription = stringResource(tab.labelRes),
                    tint = if (selected) LumixBlue else LumixMuted,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(tab.labelRes),
                    color = if (selected) LumixBlue else LumixMuted,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
