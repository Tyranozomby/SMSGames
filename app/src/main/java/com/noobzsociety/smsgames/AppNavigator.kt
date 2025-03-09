package com.noobzsociety.smsgames

import android.Manifest
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.noobzsociety.smsgames.navigation.AppScreen
import com.noobzsociety.smsgames.navigation.NavigationBar
import com.noobzsociety.smsgames.presentation.home.HomeScreen
import com.noobzsociety.smsgames.presentation.permissions.PermissionsScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavigator() {
    val navHostController: NavHostController = rememberNavController()

    val smsPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
    )
    val allPermissionsGranted = smsPermissionsState.permissions.all { it.status.isGranted }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(navHostController)
        }
    ) { innerPadding ->

        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            navController = navHostController,
            startDestination = when (allPermissionsGranted) {
                true -> AppScreen.HomeScreen
                false -> AppScreen.PermissionsScreen
            },
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = {
                ExitTransition.None
            }
        ) {
            composable<AppScreen.PermissionsScreen> {
                PermissionsScreen(smsPermissionsState)
            }

            composable<AppScreen.HomeScreen> {
                HomeScreen(navHostController)
            }
        }
    }
}

