package com.noobzsociety.smsgames.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme

@Composable
fun NavigationBar(navHostController: NavHostController) {
    androidx.compose.material3.NavigationBar {
        val current by navHostController.currentBackStackEntryAsState()

        navigationBarItems.forEach { element ->
            val isCurrent = current?.destination?.hasRoute(element.screen::class) == true
            val label = stringResource(element.label)

            NavigationBarItem(
                icon = { Icon(imageVector = element.icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = isCurrent,
                onClick = {
                    if (isCurrent) {
                        element.onReClick?.invoke()
                    } else {
                        navHostController.navigate(element.screen) {
                            popUpTo(navHostController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AppNavigationPreview() {
    SMSGamesTheme {
        NavigationBar(navHostController = rememberNavController())
    }
}

private data class NavigationBarElement(
    val screen: AppScreen,
    val icon: ImageVector,
    @StringRes val label: Int,
    val onReClick: (() -> Unit)? = null,
)

private val navigationBarItems = listOf(
    NavigationBarElement(
        screen = AppScreen.HomeScreen,
        icon = Icons.Outlined.Build,
        label = R.string.app_name
    ),
)