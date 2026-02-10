package com.noobzsociety.smsgames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme
import com.wakaztahir.codeeditor.highlight.model.CodeLang
import com.wakaztahir.codeeditor.highlight.prettify.PrettifyParser
import com.wakaztahir.codeeditor.highlight.theme.CodeThemeType
import com.wakaztahir.codeeditor.highlight.utils.parseCodeAsAnnotatedString

@Composable
fun CodeEditor(value: String, onValueChange: (String) -> Unit) {
    val systemInDarkTheme = isSystemInDarkTheme()

    val parser = remember { PrettifyParser() }
    val themeState by remember {
        mutableStateOf(
            if (systemInDarkTheme) CodeThemeType.Monokai
            else CodeThemeType.Default
        )
    }
    val theme = remember(themeState) { themeState.theme() }

    fun parse(code: String): AnnotatedString = parseCodeAsAnnotatedString(
        parser = parser,
        theme = theme,
        lang = CodeLang.JavaScript,
        code = code
    )

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(parse(value)))
    }

    LaunchedEffect(value) {
        textFieldValue = textFieldValue.copy(annotatedString = parse(value))
    }

    val textStyle = MaterialTheme.typography.bodyMedium
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(density) { textStyle.lineHeight.toDp() * 20 })
            .verticalScroll(scrollState)
    ) {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                repeat(textFieldValue.text.lines().size) {
                    Text(
                        modifier = Modifier,
                        text = (it + 1).toString(),
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onBackground.copy(.3f),
                        textAlign = TextAlign.End
                    )
                }
            }
            TextField(
                ::parse,
                textStyle,
                value,
                onValueChange,
                textFieldValue,
            ) { textFieldValue = it }
        }
    }
}

@Composable
private fun TextField(
    parse: (String) -> AnnotatedString,
    textStyle: TextStyle,
    startValue: String,
    onValueChange: (String) -> Unit,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
) {
    var previousValue by remember { mutableStateOf(startValue) }

    fun update(newValue: TextFieldValue) {
        // Update the previous value
        previousValue = newValue.text

        // Update the text field value and call onValueChange
        onTextFieldValueChange(newValue.copy(annotatedString = parse(newValue.text)))
        onValueChange(newValue.text.trim())
    }

    BasicTextField(
        modifier = Modifier.fillMaxWidth(),
        textStyle = textStyle,
        value = textFieldValue,
        minLines = 20,
        onValueChange = ::update
    )
}

@PreviewLightDark
@Composable
private fun CodeEditorPreview() {
    SMSGamesTheme {
        CodeEditor(
            value = """
            function helloWorld() {
                console.log('Hello, World!');
            }
            
            // Call the function
            helloWorld();
        """.trimIndent(),
            onValueChange = {}
        )
    }
}
