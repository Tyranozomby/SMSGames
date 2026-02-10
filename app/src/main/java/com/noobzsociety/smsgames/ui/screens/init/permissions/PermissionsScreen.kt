package com.noobzsociety.smsgames.ui.screens.init.permissions

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.components.init.NavNextButton
import com.noobzsociety.smsgames.ui.navigation.AppScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    navHostController: NavHostController,
) {
    val smsPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_PHONE_STATE,
        )
    )

    val allPermissionsGranted = smsPermissionsState.permissions.all { it.status.isGranted }

    ScreenBase(navHostController, stringResource(R.string.title_permissions)) {
        Column {
            Text("Permission required for this feature to be available. Please grant the permission")
            Button(onClick = { smsPermissionsState.launchMultiplePermissionRequest() }) {
                Text("Request Permissions")
            }

            NavNextButton(navHostController, AppScreen.Init.UserInfosScreen, enabled = allPermissionsGranted)
        }
    }
}