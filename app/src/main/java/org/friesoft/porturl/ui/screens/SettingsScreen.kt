package org.friesoft.porturl.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.Language
import org.friesoft.porturl.R
import org.friesoft.porturl.data.model.ColorSource
import org.friesoft.porturl.data.model.CustomColors
import org.friesoft.porturl.data.model.ThemeMode
import org.friesoft.porturl.data.model.UserPreferences
import org.friesoft.porturl.data.repository.TelemetryInfo
import org.friesoft.porturl.ui.components.PortUrlTopAppBar
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.ui.theme.predefinedThemes
import org.friesoft.porturl.viewmodels.SettingsViewModel
import org.friesoft.porturl.viewmodels.ValidationState
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navigator: Navigator, viewModel: SettingsViewModel = hiltViewModel()) {
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle(
        initialValue = UserPreferences(
            ThemeMode.SYSTEM,
            ColorSource.SYSTEM,
            null,
            null,
            translucentBackground = false,
            telemetryEnabled = true
        )
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRestartDialog by remember { mutableStateOf(false) }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(id = R.string.settings_restart_dialog_title)) },
            text = { Text(stringResource(id = R.string.settings_restart_dialog_text)) },
            confirmButton = {
                Button(onClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    exitProcess(0)
                }) {
                    Text(stringResource(id = R.string.settings_restart_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }


    LaunchedEffect(validationState) {
        if (validationState == ValidationState.SUCCESS) {
            navigator.goBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.resetValidationState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            PortUrlTopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(    id = R.string.back_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ThemeSettings(
                    selectedThemeMode = userPreferences.themeMode,
                    onThemeModeSelected = { viewModel.saveThemeMode(it) },
                    translucentBackground = userPreferences.translucentBackground,
                    onTranslucentBackgroundChange = { viewModel.saveTranslucentBackground(it) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                ColorSettings(
                    userPreferences = userPreferences,
                    onColorSourceSelected = { viewModel.saveColorSource(it) },
                    onPredefinedColorSelected = { viewModel.savePredefinedColorName(it) },
                    onCustomColorsSelected = { viewModel.saveCustomColors(it) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                LanguageSettings(
                    currentLanguage = settingsState.selectedLanguage,
                    availableLanguages = settingsState.availableLanguages,
                    onLanguageSelected = {
                        viewModel.changeLanguage(it)
                        (context as? Activity)?.recreate()
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                TelemetrySettings(
                    telemetryEnabled = userPreferences.telemetryEnabled,
                    telemetryInfo = settingsState.telemetryInfo,
                    onTelemetryEnabledChange = {
                        viewModel.saveTelemetryEnabled(it)
                        showRestartDialog = true
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                ServerSettings(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ThemeSettings(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    translucentBackground: Boolean,
    onTranslucentBackgroundChange: (Boolean) -> Unit
) {
    Column {
        SectionTitle(stringResource(id = R.string.settings_appearance_title))
        ThemeMode.entries.forEach { themeMode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeModeSelected(themeMode) }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = selectedThemeMode == themeMode,
                    onClick = { onThemeModeSelected(themeMode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                val themeName = when (themeMode) {
                    ThemeMode.SYSTEM -> stringResource(id = R.string.theme_mode_system)
                    ThemeMode.LIGHT -> stringResource(id = R.string.theme_mode_light)
                    ThemeMode.DARK -> stringResource(id = R.string.theme_mode_dark)
                }
                Text(text = themeName)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTranslucentBackgroundChange(!translucentBackground) }
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings_translucent_background_label),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = translucentBackground,
                onCheckedChange = onTranslucentBackgroundChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSettings(
    userPreferences: UserPreferences,
    onColorSourceSelected: (ColorSource) -> Unit,
    onPredefinedColorSelected: (String) -> Unit,
    onCustomColorsSelected: (CustomColors) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var colorToEdit by remember { mutableStateOf<String?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    if (showDialog && colorToEdit != null) {
        val initialColor = when (colorToEdit) {
            "primary" -> userPreferences.customColors?.let { Color(it.primary) } ?: primaryColor
            "secondary" -> userPreferences.customColors?.let { Color(it.secondary) } ?: secondaryColor
            else -> userPreferences.customColors?.let { Color(it.tertiary) } ?: tertiaryColor
        }
        ColorPickerDialog(
            initialColor = initialColor,
            onDismiss = { showDialog = false },
            onColorSelected = { newColor ->
                val newCustomColors = (userPreferences.customColors ?: CustomColors(
                    primary = primaryColor.toArgb(),
                    secondary = secondaryColor.toArgb(),
                    tertiary = tertiaryColor.toArgb()
                )).let {
                    when (colorToEdit) {
                        "primary" -> it.copy(primary = newColor.toArgb())
                        "secondary" -> it.copy(secondary = newColor.toArgb())
                        else -> it.copy(tertiary = newColor.toArgb())
                    }
                }
                onCustomColorsSelected(newCustomColors)
                showDialog = false
            }
        )
    }

    Column {
        SectionTitle(stringResource(id = R.string.settings_color_title))
        ColorSource.entries.forEach { colorSource ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (colorSource == ColorSource.CUSTOM && userPreferences.customColors == null) {
                            onCustomColorsSelected(
                                CustomColors(
                                    primary = primaryColor.toArgb(),
                                    secondary = secondaryColor.toArgb(),
                                    tertiary = tertiaryColor.toArgb()
                                )
                            )
                        }
                        onColorSourceSelected(colorSource)
                    }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = userPreferences.colorSource == colorSource,
                    onClick = {
                        if (colorSource == ColorSource.CUSTOM && userPreferences.customColors == null) {
                            onCustomColorsSelected(
                                CustomColors(
                                    primary = primaryColor.toArgb(),
                                    secondary = secondaryColor.toArgb(),
                                    tertiary = tertiaryColor.toArgb()
                                )
                            )
                        }
                        onColorSourceSelected(colorSource)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                val colorSourceName = when (colorSource) {
                    ColorSource.SYSTEM -> stringResource(id = R.string.color_source_system)
                    ColorSource.PREDEFINED -> stringResource(id = R.string.color_source_predefined)
                    ColorSource.CUSTOM -> stringResource(id = R.string.color_source_custom)
                }
                Text(text = colorSourceName)
            }
        }

        if (userPreferences.colorSource == ColorSource.PREDEFINED) {
            var expanded by remember { mutableStateOf(false) }
            val currentThemeName = userPreferences.predefinedColorName ?: predefinedThemes.keys.first()

            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(start = 40.dp)
            ) {
                OutlinedTextField(
                    value = currentThemeName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    predefinedThemes.keys.forEach { themeName ->
                        DropdownMenuItem(
                            text = { Text(themeName) },
                            onClick = {
                                onPredefinedColorSelected(themeName)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (userPreferences.colorSource == ColorSource.CUSTOM) {
            val customColors = userPreferences.customColors
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorPickerButton(
                    stringResource(id = R.string.settings_color_primary),
                    customColors?.primary,
                    fallbackColor = primaryColor
                ) {
                    colorToEdit = "primary"; showDialog = true
                }
                ColorPickerButton(
                    stringResource(id = R.string.settings_color_secondary),
                    customColors?.secondary,
                    fallbackColor = secondaryColor
                ) {
                    colorToEdit = "secondary"; showDialog = true
                }
                ColorPickerButton(
                    stringResource(id = R.string.settings_color_tertiary),
                    customColors?.tertiary,
                    fallbackColor = tertiaryColor
                ) {
                    colorToEdit = "tertiary"; showDialog = true
                }
            }
        }
    }
}

@Composable
fun ColorPickerButton(label: String, colorValue: Int?, fallbackColor: Color, onClick: () -> Unit) {
    val color = colorValue?.let { Color(it) } ?: fallbackColor
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                .clickable(onClick = onClick)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(10.dp),
                    controller = controller,
                    initialColor = initialColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(35.dp),
                    controller = controller
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onColorSelected(controller.selectedColor.value) }) {
                        Text(stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSettings(
    currentLanguage: String,
    availableLanguages: List<Language>,
    onLanguageSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            availableLanguages = availableLanguages,
            onLanguageSelected = {
                onLanguageSelected(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    Column {
        SectionTitle(stringResource(id = R.string.settings_language_title))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(vertical = 8.dp)
        ) {
            Text(
                stringResource(id = R.string.settings_language_label),
                modifier = Modifier.weight(1f)
            )
            val currentLanguageName = availableLanguages.find { it.code == currentLanguage }
                ?.let { stringResource(id = it.displayLanguage) }
                ?: currentLanguage // Fallback to the language code if its name isn't found
            Text(text = currentLanguageName)
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    availableLanguages: List<Language>,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(stringResource(id = R.string.settings_language_dialog_title)) },
        text = {
            LazyColumn {
                items(availableLanguages.size) { index ->
                    val language = availableLanguages[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language.code) }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = currentLanguage == language.code,
                            onClick = { onLanguageSelected(language.code) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(id = language.displayLanguage))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
private fun TelemetrySettings(
    telemetryEnabled: Boolean,
    telemetryInfo: TelemetryInfo?,
    onTelemetryEnabledChange: (Boolean) -> Unit
) {
    Column {
        SectionTitle(stringResource(id = R.string.settings_telemetry_title))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTelemetryEnabledChange(!telemetryEnabled) }
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.settings_telemetry_label))
                
                val statusText = when {
                    telemetryInfo == null -> stringResource(id = R.string.settings_telemetry_status_not_configured)
                    telemetryInfo.healthy -> stringResource(id = R.string.settings_telemetry_status_active)
                    else -> stringResource(id = R.string.settings_telemetry_status_unavailable)
                }
                
                val statusColor = when {
                    telemetryInfo?.healthy == true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.settings_telemetry_status_label),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Switch(
                checked = telemetryEnabled,
                onCheckedChange = onTelemetryEnabledChange
            )
        }
    }
}

@Composable
private fun ServerSettings(viewModel: SettingsViewModel) {
    val backendUrl by viewModel.backendUrl.collectAsStateWithLifecycle(initialValue = "")
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    var currentBackendUrl by remember(backendUrl) { mutableStateOf(backendUrl) }

    Column {
        SectionTitle(stringResource(id = R.string.settings_server_title))
        OutlinedTextField(
            value = currentBackendUrl,
            onValueChange = { currentBackendUrl = it },
            label = { Text(stringResource(id = R.string.settings_server_backend_url_label)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.settings_server_backend_url_placeholder)) },
            isError = validationState == ValidationState.ERROR,
            enabled = validationState != ValidationState.LOADING,
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(id = R.string.settings_server_backend_url_hint),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.saveAndValidateBackendUrl(currentBackendUrl) },
            modifier = Modifier.fillMaxWidth(),
            enabled = validationState != ValidationState.LOADING
        ) {
            if (validationState == ValidationState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(id = R.string.settings_server_save_and_validate_button))
            }
        }
    }
}
