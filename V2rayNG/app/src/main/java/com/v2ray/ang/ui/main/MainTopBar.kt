package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.unit.Dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.verticalScrollbar

@Composable
fun MainTopBar(
    title: String,
    isLoading: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit
) {
    var showImportMenu by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val importMenuScrollState = rememberScrollState()
    val moreMenuScrollState = rememberScrollState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val maxMenuHeight: Dp = LocalConfiguration.current.screenHeightDp.dp - statusBarHeight - navBarHeight - 20.dp
    val ink = Color(0xFFF2F7FF)
    val muted = Color(0xFF9BB4D9)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = statusBarHeight)
            .padding(start = 12.dp, end = 12.dp, top = statusBarHeight + 4.dp, bottom = 7.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xE6294C79), Color(0xF20B1930))))
            .border(1.dp, Color(0xFF9CCBFF).copy(alpha = 0.22f), RoundedCornerShape(22.dp))
            .padding(horizontal = 5.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        IconButton(
            onClick = if (showSearch) onSearchClose else onMenuClick,
            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.055f))
        ) {
            Icon(
                painter = painterResource(if (showSearch) R.drawable.ic_arrow_back_24dp else R.drawable.ic_menu_24dp),
                contentDescription = stringResource(if (showSearch) R.string.acc_back else R.string.acc_open_menu),
                tint = ink
            )
        }
        if (showSearch) {
            androidx.compose.material3.TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.menu_item_search), color = muted, fontSize = 13.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(color = ink, fontSize = 14.sp),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF65B5FF)
                )
            )
        } else {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.lumixvpn_logo),
                    contentDescription = "LumixVPN",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(37.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .border(1.dp, Color.White.copy(alpha = .42f), RoundedCornerShape(13.dp))
                )
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.Center) {
                    Text("LumixVPN", color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = .25.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(title, color = muted, fontSize = 11.sp, maxLines = 1)
                        if (isLoading) CircularProgressIndicator(
                            modifier = Modifier.size(10.dp),
                            color = Color(0xFF69B9FF),
                            strokeWidth = 1.5.dp
                        )
                    }
                }
            }
            IconButton(onClick = { onSearchToggle(true) }, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.055f))) {
                Icon(painterResource(R.drawable.ic_search_24dp), contentDescription = stringResource(R.string.acc_search), tint = ink)
            }
        }
        Box {
            IconButton(onClick = { showImportMenu = true }, modifier = Modifier.clip(CircleShape).background(Color(0xFF4D9DFF).copy(alpha = 0.13f))) {
                Icon(painterResource(R.drawable.ic_add_24dp), contentDescription = stringResource(R.string.acc_add), tint = Color(0xFF8CCBFF))
            }
            DropdownMenu(
                expanded = showImportMenu,
                onDismissRequest = { showImportMenu = false },
                scrollState = importMenuScrollState,
                containerColor = Color(0xFF10213D),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.heightIn(max = maxMenuHeight).verticalScrollbar(importMenuScrollState)
            ) {
                ImportMenuContent(onAction = { action ->
                    showImportMenu = false
                    onAction(action)
                })
            }
        }
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.055f))) {
                Icon(painterResource(R.drawable.ic_more_vert_24dp), contentDescription = stringResource(R.string.acc_more), tint = ink)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                scrollState = moreMenuScrollState,
                containerColor = Color(0xFF10213D),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.heightIn(max = maxMenuHeight).verticalScrollbar(moreMenuScrollState)
            ) {
                MoreMenuContent { action ->
                    showMenu = false
                    onMoreMenuAction(action)
                }
            }
        }
    }
}
