package com.noobzsociety.smsgames.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeViewModel = koinViewModel()
) {
    Text("Home Screen")
}