package com.noobzsociety.smsgames.presentation.permissions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    smsPermissionsState: MultiplePermissionsState,
) {
    Column {
        Text("Permission required for this feature to be available. Please grant the permission")
        Button(onClick = { smsPermissionsState.launchMultiplePermissionRequest() }) {
            Text("Request Permissions")
        }
    }
}