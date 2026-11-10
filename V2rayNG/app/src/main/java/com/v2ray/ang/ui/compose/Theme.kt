package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val LightColor = lightColorScheme(
    primary = Color(0xFF1E88E5), // Lumix Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F1FF),
    onPrimaryContainer = Color(0xFF0B3A6B),
    secondary = Color(0xFF0A6CC8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEBFA),
    onSecondaryContainer = Color(0xFF0B2A4A),
    tertiary = Color(0xFF00A67E), // Connected Green
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD3F5EA),
    onTertiaryContainer = Color(0xFF00382B),
    error = Color(0xFFD93B4F),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF4F8FF),
    onBackground = Color(0xFF0E1B2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0E1B2E),
    surfaceVariant = Color(0xFFE8F0FB),
    onSurfaceVariant = Color(0xFF5B6B82),
    outline = Color(0xFF8DA0BA),
    outlineVariant = Color(0xFFD5E2F3),
    inverseSurface = Color(0xFF1E2A3C),
    inverseOnSurface = Color(0xFFF4F8FF),
    inversePrimary = Color(0xFF8CC4FF),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF1E88E5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FBFF),
    surfaceContainer = Color(0xFFF1F6FD),
    surfaceContainerHigh = Color(0xFFEBF2FB),
    surfaceContainerHighest = Color(0xFFE5EEF9),
)

private val DarkColor = darkColorScheme(
    primary = Color(0xFF3477FF), // Lumix Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF14294F),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFF62A0FF), // Lumix Light Blue
    onSecondary = Color(0xFF0B1A33),
    secondaryContainer = Color(0xFF1D3E73),
    onSecondaryContainer = Color(0xFFDCE8FF),
    tertiary = Color(0xFF55D6A5), // Connected Green
    onTertiary = Color(0xFF00332A),
    tertiaryContainer = Color(0xFF0B4A3C),
    onTertiaryContainer = Color(0xFFA6F2DB),
    error = Color(0xFFFF718B),
    errorContainer = Color(0xFF4A0F1D),
    onError = Color(0xFF1A0009),
    onErrorContainer = Color(0xFFFFD8DF),
    background = Color(0xFF071326), // Lumix Deep Navy
    onBackground = Color(0xFFF5F8FF),
    surface = Color(0xFF0C1D3A),
    onSurface = Color(0xFFF5F8FF),
    surfaceVariant = Color(0xFF15294D),
    onSurfaceVariant = Color(0xFF9FB0CA),
    outline = Color(0xFF3A527A),
    outlineVariant = Color(0xFF22375C),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF1C1B1F),
    inversePrimary = Color(0xFF0B3E9E),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF3477FF),
    surfaceContainerLowest = Color(0xFF050D1C),
    surfaceContainerLow = Color(0xFF0A182F),
    surfaceContainer = Color(0xFF0E2038),
    surfaceContainerHigh = Color(0xFF13294A),
    surfaceContainerHighest = Color(0xFF19305A),
)

private val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private val baseTypography = Typography()
val LumixTypography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = PoppinsFontFamily),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = PoppinsFontFamily),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = PoppinsFontFamily),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = PoppinsFontFamily),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = PoppinsFontFamily),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = PoppinsFontFamily),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = PoppinsFontFamily),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = PoppinsFontFamily),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium)
)
// Semantic Colors
val colorPing = Color(0xFF00A67E) // Green
val colorPingRed = Color(0xFFFF0099) // Pink Red
val colorConfigType = Color(0xFF1E88E5) // Blue
val colorFabActive = Color(0xFF1E88E5) // Blue
val colorFabInactiveLight = Color(0xFF9C9C9C) // Gray
val colorFabInactiveDark = Color(0xFF646464) // Dark Gray
val dividerColorLight = Color(0xFFE0E0E0) // Light Gray
val dividerColorDark = Color(0xFF424242) // Dark Gray

// Toast Colors 70%
val toastNormalBgLight = Color(0xB3353A3E) // Dark Gray
val toastNormalBgDark = Color(0xB34A4F54) // Darker Gray
val toastSuccessBg = Color(0xB3388E3C) // Green
val toastErrorBg = Color(0xB3D50000) // Red
val toastInfoBg = Color(0xB33F51B5) // Indigo Blue
val toastIconCircleBg = Color(0x33FFFFFF) // Semi-transparent White
val toastTextColor = Color.White // White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    return false
}
val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LumixTypography
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
