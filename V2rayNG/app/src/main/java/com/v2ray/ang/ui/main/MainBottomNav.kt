package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R

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
            Color(0xFF1D3E73).copy(alpha = 0.75f),
            Color(0xFF0A182F).copy(alpha = 0.90f)
        )
    )
    Column {
        HorizontalDivider(color = Color.White.copy(alpha = 0.14f), thickness = 1.dp)
        NavigationBar(
            modifier = Modifier.background(glassGradient),
            containerColor = Color.Transparent
        ) {
            MainTab.values().forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelect(tab) },
                    icon = { Icon(painterResource(tab.iconRes), contentDescription = null) },
                    label = { Text(stringResource(tab.labelRes)) }
                )
            }
        }
    }
}
