package com.v2ray.ang.ui.main

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.ConnectionTestResult
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.ui.compose.QRCodeDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isRunning = uiState.isRunning
    val displayText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val doubleColumnDisplay = uiState.doubleColumnDisplay
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    var animationMode by remember { mutableStateOf(readAnimationMode()) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) animationMode = readAnimationMode()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val animationsOn = animationsEnabled(animationMode, context)
    val overlays = remember { mutableStateListOf<MainOverlay>() }
    val openOverlay: (MainOverlay) -> Unit = { overlay -> if (overlays.lastOrNull() != overlay) overlays.add(overlay) }
    BackHandler(enabled = overlays.isNotEmpty()) { overlays.removeAt(overlays.lastIndex) }
    var subscriptionQr by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showTestSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }
    val handleMoreMenuAction: (MainMoreMenuAction) -> Unit = { action ->
        when (action) {
            MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
            MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
            MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
            MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
            MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
            MainMoreMenuAction.LocateSelected -> {
                selectedTab = MainTab.Servers
                onAction(MainAction.LocateSelectedServer)
            }
            MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
            MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
            MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
            MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
        }
    }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    val removeServer: (String) -> Unit = { guid ->
        if (confirmRemove) showRemoveConfirm = guid else onAction(MainAction.RemoveServer(guid))
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { groups.size.coerceAtLeast(1) }
    )

    val lazyListStates = remember { mutableStateMapOf<String, LazyListState>() }
    val lazyGridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    LaunchedEffect(groups) {
        val validGroupIds = groups.map { it.id }.toSet()
        lazyListStates.keys.retainAll(validGroupIds)
        lazyGridStates.keys.retainAll(validGroupIds)
    }

    LaunchedEffect(groups, uiState.selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == uiState.selectedGroupId }
            .takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    val latestGroups by rememberUpdatedState(groups)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val currentGroups = latestGroups
                if (page in currentGroups.indices) {
                    onAction(MainAction.SelectGroup(currentGroups[page].id))
                }
            }
    }

    // Live data for the Connect tab, read from the saved servers and the latest test status.
    val selectedProfile = remember(selectedGuid, uiState.status, uiState.isTesting, groups) {
        selectedGuid?.let { MmkvManager.decodeServerConfig(it) }
    }
    val selectedDelay = remember(selectedGuid, uiState.status, uiState.isTesting, groups) {
        val liveDelay = (uiState.status as? MainStatus.ConnectionTest)?.result?.delayMillis ?: -1L
        if (isRunning && liveDelay >= 0L) liveDelay
        else selectedGuid?.let { MmkvManager.decodeServerAffiliationInfo(it)?.testDelayMillis } ?: 0L
    }
    var subscriptionRefresh by remember { mutableStateOf(0) }
    val subscriptions = remember(groups, selectedTab, overlays.size, subscriptionRefresh, uiState.isTesting, uiState.status) {
        MmkvManager.decodeSubscriptions()
    }
    val connectedSeconds = rememberConnectionSeconds(isRunning)

    // Exit location and IP come from the real connection test result.
    var lastExit by remember { mutableStateOf<ConnectionTestResult?>(null) }
    LaunchedEffect(uiState.status, isRunning) {
        val status = uiState.status
        if (!isRunning) {
            lastExit = null
        } else if (status is MainStatus.ConnectionTest && status.result.delayMillis >= 0L && !status.result.country.isNullOrBlank()) {
            lastExit = status.result
        }
    }

    // Live speed samples and the total transferred while this connection is up.
    val speedHistory = remember { mutableStateListOf<Pair<Long, Long>>() }
    var sessionDown by remember { mutableLongStateOf(0L) }
    var sessionUp by remember { mutableLongStateOf(0L) }
    val latestDown by rememberUpdatedState(uiState.downloadSpeedBytesPerSec)
    val latestUp by rememberUpdatedState(uiState.uploadSpeedBytesPerSec)
    LaunchedEffect(isRunning) {
        speedHistory.clear()
        sessionDown = 0L
        sessionUp = 0L
        if (!isRunning) return@LaunchedEffect
        while (true) {
            delay(1000L)
            speedHistory.add(latestDown to latestUp)
            if (speedHistory.size > 60) speedHistory.removeAt(0)
            sessionDown += latestDown
            sessionUp += latestUp
        }
    }

    // Short vibration when the VPN connects (optional setting).
    var wasRunning by remember { mutableStateOf(isRunning) }
    LaunchedEffect(isRunning) {
        if (isRunning && !wasRunning && MmkvManager.decodeSettingsBool(AppConfig.PREF_LUMIX_HAPTIC, true)) {
            view.performHapticFeedback(
                if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS
            )
        }
        wasRunning = isRunning
    }

    val topOverlay = overlays.lastOrNull()
    LaunchedEffect(topOverlay) {
        if (topOverlay == MainOverlay.Report && isRunning && lastExit == null) onAction(MainAction.TestCurrentServer)
    }

    // Lowest measured ping across all saved servers.
    val bestRoute = remember(groups, uiState.status, uiState.isTesting) {
        groups.flatMap { MmkvManager.decodeServerList(it.id) }.distinct()
            .mapNotNull { guid ->
                val pingValue = MmkvManager.decodeServerAffiliationInfo(guid)?.testDelayMillis ?: 0L
                if (pingValue > 0L) guid to pingValue else null
            }
            .minByOrNull { it.second }
            ?.let { (guid, pingValue) ->
                Triple(guid, MmkvManager.decodeServerConfig(guid)?.remarks.orEmpty().ifBlank { guid }, pingValue)
            }
    }
    val navigateInApp: (MainDestination) -> Unit = { destination ->
        when (destination) {
            MainDestination.Subscriptions -> openOverlay(MainOverlay.Subscriptions)
            MainDestination.Settings -> openOverlay(MainOverlay.Settings)
            else -> onNavigate(destination)
        }
    }
    val serverNameForUi = if (isRunning && uiState.connectedServerName.isNotBlank()) uiState.connectedServerName else selectedProfile?.remarks.orEmpty()
    val serverTypeForUi = selectedProfile?.configType?.name.orEmpty()

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) }
    )

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = more,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = removeServer,
        )
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }
    if (subscriptionQr != null) {
        QRCodeDialog(bitmap = subscriptionQr, onDismiss = { subscriptionQr = null })
    }
    if (showTestSheet) {
        TestRoutesSheet(
            isTesting = uiState.isTesting,
            statusText = displayText,
            onAction = onAction,
            onDismiss = { showTestSheet = false }
        )
    }
    if (showAddSheet) {
        AddSubscriptionSheet(
            onAction = onAction,
            onDismiss = { showAddSheet = false }
        )
    }

    CompositionLocalProvider(LocalLumixAnimations provides animationsOn) {
    Box(Modifier.fillMaxSize().background(LumixBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                MainBottomNav(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )
            },
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            // The floating bottom bar overlaps the content, so only the top/side insets are applied here.
            val contentModifier = Modifier.padding(
                PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
            )

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (animationsOn) {
                        fadeIn(tween(240)) togetherWith fadeOut(tween(140))
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                },
                label = "main-tab"
            ) { tab ->
            when (tab) {
                MainTab.Home -> HomeTabContent(
                    statusText = displayText,
                    isRunning = isRunning,
                    isBusy = uiState.isTesting,
                    connectedSeconds = connectedSeconds,
                    usage = subscriptions.primaryUsage(),
                    uploadSpeedBytesPerSec = uiState.uploadSpeedBytesPerSec,
                    downloadSpeedBytesPerSec = uiState.downloadSpeedBytesPerSec,
                    serverName = serverNameForUi,
                    serverType = serverTypeForUi,
                    pingMillis = selectedDelay,
                    onAction = onAction,
                    onOpenServers = { selectedTab = MainTab.Servers },
                    onOpenReport = { openOverlay(MainOverlay.Report) },
                    onOpenSubscriptions = { openOverlay(MainOverlay.Subscriptions) },
                    onOpenTestSheet = { showTestSheet = true },
                    modifier = contentModifier
                )

                MainTab.Account -> AccountTabContent(
                    subscriptions = subscriptions,
                    onAction = onAction,
                    onMoreMenuAction = handleMoreMenuAction,
                    onNavigate = navigateInApp,
                    onOpenAddSheet = { showAddSheet = true },
                    onOpenReport = { openOverlay(MainOverlay.Report) },
                    onOpenSubscriptions = { openOverlay(MainOverlay.Subscriptions) },
                    onOpenSupport = { openOverlay(MainOverlay.Support) },
                    modifier = contentModifier
                )

                MainTab.Servers -> if (groups.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxSize().then(contentModifier)) {
                        ServersHeader(
                            groupCount = groups.size,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { query ->
                                searchQuery = query
                                onAction(MainAction.Search(query))
                            },
                            onOpenTestSheet = { showTestSheet = true },
                            onAction = onAction,
                            onMoreMenuAction = handleMoreMenuAction
                        )
                        bestRoute?.let { (guid, name, bestPing) ->
                            BestRouteCard(
                                name = name,
                                delayMillis = bestPing,
                                onUse = {
                                    onAction(MainAction.SelectServer(guid))
                                    scope.launch {
                                        delay(250L)
                                        if (!isRunning) onAction(MainAction.ToggleService)
                                    }
                                }
                            )
                        }
                        if (groups.size > 1) {
                            GroupTabBar(
                                groups = groups,
                                selectedTabIndex = pagerState.currentPage.coerceIn(0, groups.lastIndex),
                                mainViewModel = mainViewModel,
                                onTabClick = { targetIndex ->
                                    scope.launch {
                                        pagerState.navigateToPageOptimized(
                                            targetPage = targetIndex,
                                            animateAdjacentPage = true
                                        )
                                    }
                                }
                            )
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            userScrollEnabled = true,
                            beyondViewportPageCount = 1,
                            key = { page -> groups.getOrNull(page)?.id ?: "group-page-$page" }
                        ) { page ->
                            val group = groups.getOrNull(page) ?: return@HorizontalPager

                            GroupPagerPage(
                                groupId = group.id,
                                mainViewModel = mainViewModel,
                                selectedGuid = selectedGuid,
                                locateTarget = uiState.locateTarget,
                                doubleColumnDisplay = doubleColumnDisplay,
                                searchQuery = searchQuery,
                                lazyListStates = lazyListStates,
                                lazyGridStates = lazyGridStates,
                                onSelectServer = { guid -> onAction(MainAction.SelectServer(guid)) },
                                onEditServer = { guid, profile -> onAction(MainAction.EditServer(guid, profile)) },
                                onShareServer = { guid, profile ->
                                    shareTarget = Triple(guid, profile, false)
                                },
                                onMoreServer = { guid, profile ->
                                    shareTarget = Triple(guid, profile, true)
                                },
                                onRemoveServer = removeServer,
                                contentPadding = PaddingValues(
                                    start = 0.dp,
                                    top = 4.dp,
                                    end = 0.dp,
                                    bottom = 120.dp
                                )
                            )
                        }
                    }
                } else {
                    EmptyServersState(
                        onImportAction = onAction,
                        modifier = contentModifier
                    )
                }
            }
            }
        }

        AnimatedContent(
            targetState = topOverlay,
            transitionSpec = {
                if (animationsOn) {
                    (fadeIn(tween(240)) + slideInHorizontally(tween(260)) { it / 6 }) togetherWith fadeOut(tween(160))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "main-overlay"
        ) { overlay ->
            when (overlay) {
                null -> Box(Modifier.fillMaxSize())
                MainOverlay.Report -> ConnectionReportScreen(
                    isRunning = isRunning,
                    statusText = displayText,
                    connectedSeconds = connectedSeconds,
                    downloadSpeed = uiState.downloadSpeedBytesPerSec,
                    uploadSpeed = uiState.uploadSpeedBytesPerSec,
                    speedHistory = speedHistory.toList(),
                    sessionDownloadBytes = sessionDown,
                    sessionUploadBytes = sessionUp,
                    serverName = serverNameForUi,
                    serverType = serverTypeForUi,
                    pingMillis = selectedDelay,
                    exitCountry = lastExit?.country,
                    exitIp = lastExit?.ipAddress,
                    onRecheck = { onAction(MainAction.TestCurrentServer) },
                    onOpenSupport = { openOverlay(MainOverlay.Support) },
                    onBack = { overlays.remove(MainOverlay.Report) }
                )
                MainOverlay.Settings -> LumixSettingsScreen(
                    animationMode = animationMode,
                    onAnimationModeChange = { animationMode = it },
                    onNavigate = onNavigate,
                    onOpenSupport = { openOverlay(MainOverlay.Support) },
                    onBack = { overlays.remove(MainOverlay.Settings) }
                )
                MainOverlay.Support -> SupportScreen(
                    reportText = buildSupportReport(isRunning, serverTypeForUi, selectedDelay, lastExit?.country, connectedSeconds),
                    onNavigate = onNavigate,
                    onBack = { overlays.remove(MainOverlay.Support) }
                )
                MainOverlay.Subscriptions -> SubscriptionsScreen(
                    subscriptions = subscriptions,
                    onUpdate = { onAction(MainAction.UpdateSubscriptions) },
                    onDelete = { guid ->
                        MmkvManager.removeSubscription(guid)
                        subscriptionRefresh++
                        onAction(MainAction.RefreshGroups)
                    },
                    onShowQr = { subscriptionQr = it },
                    onOpenAddSheet = { showAddSheet = true },
                    onOpenSupport = { context.openSupportChat() },
                    onNavigate = onNavigate,
                    onBack = { overlays.remove(MainOverlay.Subscriptions) }
                )
            }
        }
    }
    }
}

