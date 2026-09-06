package com.v2ray.ang.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
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
