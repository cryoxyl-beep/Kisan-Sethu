package com.kisansethu.app.navigation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kisansethu.app.data.UserPreferencesRepository
import com.kisansethu.app.ui.AuthEntryScreen
import com.kisansethu.app.ui.HomeScreen
import com.kisansethu.app.ui.LanguageSelectionScreen
import com.kisansethu.app.ui.LoginScreen
import com.kisansethu.app.ui.RegistrationScreen
import com.kisansethu.app.ui.language.LanguageSelectionViewModel
import com.kisansethu.app.ui.splash.SplashScreen
import kotlinx.serialization.Serializable
import java.util.Locale

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object LanguageSelection : Screen

    @Serializable
    data object AuthEntry : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Registration : Screen

    @Serializable
    data class Home(
        val farmerId: String,
        val farmerName: String
    ) : Screen
}

@Composable
fun KisanSethuNavHost(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    val persistedLanguageCode by userPreferencesRepository.selectedLanguage.collectAsState(initial = null)

    LaunchedEffect(persistedLanguageCode) {
        persistedLanguageCode?.let { code ->
            if (code.isNotEmpty()) {
                val appLocales = LocaleListCompat.forLanguageTags(code)
                if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != code) {
                    AppCompatDelegate.setApplicationLocales(appLocales)
                    Locale.setDefault(Locale.forLanguageTag(code))
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(380, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 1.04f, animationSpec = tween(300, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 1.04f, animationSpec = tween(380, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.94f, animationSpec = tween(300, easing = FastOutSlowInEasing))
        }
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onSplashFinished = {
                    val nextScreen = if (!persistedLanguageCode.isNullOrEmpty()) {
                        Screen.AuthEntry
                    } else {
                        Screen.LanguageSelection
                    }
                    
                    navController.navigate(nextScreen) {
                        popUpTo<Screen.Splash> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Screen.LanguageSelection> {
            val viewModel: LanguageSelectionViewModel = viewModel()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()

            LanguageSelectionScreen(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { viewModel.selectLanguage(it) },
                onContinueClicked = {
                    viewModel.confirmLanguageSelection {
                        navController.navigate(Screen.AuthEntry) {
                            popUpTo<Screen.LanguageSelection> {
                                inclusive = true
                            }
                        }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable<Screen.AuthEntry> {
            AuthEntryScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login)
                },
                onNavigateToRegistration = {
                    navController.navigate(Screen.Registration)
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = { farmer ->
                    navController.navigate(
                        Screen.Home(
                            farmerId = farmer.farmerId,
                            farmerName = farmer.fullName
                        )
                    ) {
                        popUpTo<Screen.AuthEntry> {
                            inclusive = false
                        }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable<Screen.Registration> {
            RegistrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = {
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Registration> {
                            inclusive = true
                        }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable<Screen.Home> { backStackEntry ->
            val homeRoute: Screen.Home = backStackEntry.toRoute()
            HomeScreen(
                farmerName = homeRoute.farmerName,
                farmerId = homeRoute.farmerId,
                onSignOut = {
                    navController.navigate(Screen.AuthEntry) {
                        popUpTo<Screen.Home> {
                            inclusive = true
                        }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }
}
