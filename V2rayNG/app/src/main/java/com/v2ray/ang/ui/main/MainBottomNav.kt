package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R

/** Main app navigation. The items keep using the existing app destinations. */
enum class MainTab(val labelRes: Int, val iconRes: Int) {
    Home(R.string.title_home, R.drawable.ic_play_24dp),
    Servers(R.string.title_server, R.drawable.ic_subscriptions_24dp),
    Tools(R.string.title_tools, R.drawable.ic_add_24dp),
    Settings(R.string.title_settings, R.drawable.ic_settings_24dp)
}

@Composable
fun MainBottomNav(
    selectedTab: MainTab,
    onTabSelect: (MainTab) -> Unit
) {
    val glassGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xE62B5791),
            Color(0xF20D1D37),
            Color(0xF70A1428)
        )
    )
    val selectedGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF397FEA), Color(0xFF174B9A))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(glassGradient)
            .border(1.dp, Color.White.copy(alpha = 0.17f), RoundedCornerShape(28.dp))
            .padding(horizontal = 7.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(21.dp))
                    .then(if (selected) Modifier.background(selectedGradient) else Modifier)
                    .clickable { onTabSelect(tab) }
                    .padding(horizontal = 4.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(tab.iconRes),
                    contentDescription = stringResource(tab.labelRes),
                    tint = if (selected) Color.White else Color(0xFFB7C7E2),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(tab.labelRes),
                    color = if (selected) Color.White else Color(0xFFB7C7E2),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
