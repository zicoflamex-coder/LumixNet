package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R

/** Bottom sheet that measures the ping of every server. Progress text comes from the real test status. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TestRoutesSheet(
    isTesting: Boolean,
    statusText: String,
    onAction: (MainAction) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(R.string.lumix_test_routes), color = LumixInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.lumix_test_routes_desc), color = LumixMuted, fontSize = 13.sp, lineHeight = 19.sp)
            if (isTesting) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(LumixBlue.copy(alpha = 0.08f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = LumixBlue, strokeWidth = 2.5.dp)
                    Text(
                        statusText.ifBlank { stringResource(R.string.lumix_testing) },
                        color = LumixInk, fontSize = 13.sp, modifier = Modifier.weight(1f)
                    )
                }
                LumixTonalButton(
                    stringResource(R.string.lumix_cancel),
                    { onAction(MainAction.CancelTesting) },
                    Modifier.fillMaxWidth()
                )
            } else {
                LumixPrimaryButton(
                    stringResource(R.string.lumix_real_ping),
                    { onAction(MainAction.TestRealAllServers) },
                    Modifier.fillMaxWidth(),
                    R.drawable.ic_lumix_ping_24
                )
                LumixTonalButton(
                    stringResource(R.string.lumix_quick_ping),
                    { onAction(MainAction.TestAllServers) },
                    Modifier.fillMaxWidth(),
                    R.drawable.ic_lumix_ping_24
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/** Bottom sheet to add a subscription link or configuration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddSubscriptionSheet(
    onAction: (MainAction) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(R.string.lumix_add_subscription), color = LumixInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.lumix_add_subscription_desc), color = LumixMuted, fontSize = 13.sp, lineHeight = 19.sp)
            LumixPrimaryButton(
                stringResource(R.string.lumix_paste_link),
                { onDismiss(); onAction(MainAction.ImportClipboard) },
                Modifier.fillMaxWidth(),
                R.drawable.ic_copy
            )
            val fieldShape = RoundedCornerShape(18.dp)
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = LumixInk, fontSize = 14.sp),
                cursorBrush = SolidColor(LumixBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(fieldShape)
                    .background(Color(0xFFF4F8FF))
                    .border(1.dp, LumixStroke, fieldShape)
                    .padding(16.dp),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(stringResource(R.string.lumix_link_hint), color = LumixMuted, fontSize = 14.sp)
                    }
                    inner()
                }
            )
            LumixTonalButton(
                stringResource(R.string.lumix_add),
                {
                    val value = text.trim()
                    if (value.isNotEmpty()) {
                        onDismiss()
                        onAction(MainAction.ImportBatchConfig(value))
                    }
                },
                Modifier.fillMaxWidth(),
                R.drawable.ic_add_24dp
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LumixTonalButton(
                    stringResource(R.string.lumix_scan_qr),
                    { onDismiss(); onAction(MainAction.ImportQRcode) },
                    Modifier.weight(1f),
                    R.drawable.ic_scan_24dp
                )
                LumixTonalButton(
                    stringResource(R.string.lumix_import_file),
                    { onDismiss(); onAction(MainAction.ImportConfigLocal) },
                    Modifier.weight(1f),
                    R.drawable.ic_file_24dp
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
