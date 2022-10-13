package com.tyrano.smsgames.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tyrano.smsgames.datastore.StoreParameters
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Preview(showBackground = true)
@Composable
fun ParameterPagePreview() {
    SMSGamesTheme {
        ParametersList()
    }
}

@Composable
fun ParametersList() {
    val scope = rememberCoroutineScope()
    val storeParameters = StoreParameters(LocalContext.current)

    val savedCommandChar = runBlocking { storeParameters.getCommandPrefix() }
    var commandChar by rememberSaveable(savedCommandChar) {
        mutableStateOf(savedCommandChar)
    }

    val savedMyNumber = runBlocking { storeParameters.getMyNumber() }
    var myNumber by rememberSaveable(savedMyNumber) {
        mutableStateOf(savedMyNumber)
    }

    val savedRegisterMessage = runBlocking { storeParameters.getRegisterMessage() }
    var registerMessage by rememberSaveable(savedRegisterMessage) {
        mutableStateOf(savedRegisterMessage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 15.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = myNumber,
            onValueChange = { myNumber = it },
            singleLine = true,
            label = { Text("Mon numéro de téléphone") }
        )

        OutlinedTextField(
            value = commandChar,
            onValueChange = { if (it.length <= 1) commandChar = it },
            singleLine = true,
            label = { Text("Préfixe des commandes") }
        )

        OutlinedTextField(
            value = registerMessage,
            onValueChange = { registerMessage = it },
            label = { Text("Message pré-inscription") }
        )

        Button(onClick = {
            scope.launch {
                storeParameters.saveCommandPrefix(commandChar)
                storeParameters.saveMyNumber(myNumber)
                storeParameters.saveRegisterMessage(registerMessage)
            }
        }) {
            Text("Sauver")
        }
    }
}
