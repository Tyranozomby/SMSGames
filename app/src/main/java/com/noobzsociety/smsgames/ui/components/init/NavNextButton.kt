package com.noobzsociety.smsgames.ui.components.init

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.ui.navigation.AppScreen

@Composable
fun NavNextButton(
    navHostController: NavHostController,
    nextScreen: AppScreen,
    enabled: Boolean = true,
    isLastScreen: Boolean = false,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = {
            if (enabled) {
                onClick()
                navHostController.navigate(nextScreen) {
                    if (isLastScreen) {
                        popUpTo(nextScreen) { inclusive = true }
                    }
                }
            }
        },
        enabled = enabled,
    ) {
        if (isLastScreen) {
            Text("Finish")
        } else {
            Text("Next")
        }
    }
}