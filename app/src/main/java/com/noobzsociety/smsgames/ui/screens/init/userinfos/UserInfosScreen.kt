package com.noobzsociety.smsgames.ui.screens.init.userinfos

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.components.init.NavNextButton
import com.noobzsociety.smsgames.ui.navigation.AppScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserInfosScreen(
    navHostController: NavHostController,
    viewModel: UserInfosViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    val name by viewModel.name.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val isEveryFieldFilled by viewModel.isEveryFieldFilled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setPhoneNumber(getPhoneNumber(context))
    }

    ScreenBase(navHostController, stringResource(R.string.title_userinfos)) {
        Column {
            TextField(
                value = name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Name") },
                placeholder = { Text("Enter your name") },
            )

            TextField(
                value = phoneNumber ?: "",
                onValueChange = { viewModel.setPhoneNumber(it) },
                label = { Text("Phone Number") },
                placeholder = { Text("Enter your phone number") },
            )

            NavNextButton(navHostController, AppScreen.Gamemode.ListScreen, isEveryFieldFilled, isLastScreen = true) {
                viewModel.save()
            }
        }
    }
}

@SuppressLint("MissingPermission", "HardwareIds")
fun getPhoneNumber(context: Context): String? {
    val telephonyManager = context.getSystemService(TelephonyManager::class.java) as TelephonyManager
    @Suppress("DEPRECATION")
    return telephonyManager.line1Number
}