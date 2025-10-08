package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.R
import org.friesoft.porturl.data.model.ColorSource
import org.friesoft.porturl.data.model.CustomColors
import org.friesoft.porturl.data.model.ThemeMode
import org.friesoft.porturl.data.model.UserPreferences
import org.friesoft.porturl.ui.theme.predefinedThemes
import org.friesoft.porturl.viewmodels.SettingsViewModel
import org.friesoft.porturl.viewmodels.ValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle(
        initialValue = UserPreferences(ThemeMode.SYSTEM, ColorSource.SYSTEM, null, null)
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()

    LaunchedEffect(validationState) {
        if (validationState == ValidationState.SUCCESS) {
            navController.popBackStack()
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
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.settings_back_button)
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
                    onThemeModeSelected = { viewModel.saveThemeMode(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                ColorSettings(
                    userPreferences = userPreferences,
                    onColorSourceSelected = { viewModel.saveColorSource(it) },
                    onPredefinedColorSelected = { viewModel.savePredefinedColorName(it) },
                    onCustomColorsSelected = { viewModel.saveCustomColors(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                LanguageSettings(
                    onLanguageSelected = { viewModel.saveLanguage(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
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
    onThemeModeSelected: (ThemeMode) -> Unit
) {
    Column {
        SectionTitle(stringResource(id = R.string.settings_appearance_title))
        ThemeMode.values().forEach { themeMode ->
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
                    primary = primaryColor.value.toInt(),
                    secondary = secondaryColor.value.toInt(),
                    tertiary = tertiaryColor.value.toInt()
                )).let {
                    when (colorToEdit) {
                        "primary" -> it.copy(primary = newColor.value.toInt())
                        "secondary" -> it.copy(secondary = newColor.value.toInt())
                        else -> it.copy(tertiary = newColor.value.toInt())
                    }
                }
                onCustomColorsSelected(newCustomColors)
                showDialog = false
            }
        )
    }

    Column {
        SectionTitle(stringResource(id = R.string.settings_color_title))
        ColorSource.values().forEach { colorSource ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onColorSourceSelected(colorSource) }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = userPreferences.colorSource == colorSource,
                    onClick = { onColorSourceSelected(colorSource) }
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
                    modifier = Modifier.menuAnchor()
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
                ColorPickerButton(stringResource(id = R.string.settings_color_primary), customColors?.primary, fallbackColor = primaryColor) {
                    colorToEdit = "primary"; showDialog = true
                }
                ColorPickerButton(stringResource(id = R.string.settings_color_secondary), customColors?.secondary, fallbackColor = secondaryColor) {
                    colorToEdit = "secondary"; showDialog = true
                }
                ColorPickerButton(stringResource(id = R.string.settings_color_tertiary), customColors?.tertiary, fallbackColor = tertiaryColor) {
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
                        .height(300.dp),
                    controller = controller,
                    initialColor = initialColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                BrightnessSlider(
                    modifier = Modifier.fillMaxWidth(),
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
    onLanguageSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentLanguageTag = AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag() ?: "en"

    if (showDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguageTag,
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
            Text(stringResource(id = R.string.settings_language_label), modifier = Modifier.weight(1f))
            val currentLanguageName = when {
                currentLanguageTag.startsWith("de") -> "Deutsch"
                else -> "English"
            }
            Text(currentLanguageName)
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf("en", "de")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.settings_language_dialog_title)) },
        text = {
            LazyColumn {
                items(languages.size) { index ->
                    val language = languages[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = currentLanguage.startsWith(language),
                            onClick = { onLanguageSelected(language) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(if (language == "en") "English" else "Deutsch")
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
