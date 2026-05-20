package com.example.grit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.grit.ui.screens.auth.LoginScreen
import com.example.grit.ui.screens.auth.RegisterScreen
import com.example.grit.ui.screens.detail.DetailScreen
import com.example.grit.ui.screens.home.HomeScreen
import com.example.grit.ui.screens.profile.MyFarmlandScreen
import com.example.grit.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable("detail/{propertyId}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            DetailScreen(
                propertyId = propertyId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onMyFarmlandClick = { navController.navigate(NavRoutes.MY_FARMLAND) },
                onEditProfileClick = { /* TODO: Edit Profile */ },
                onLogoutClick = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                },
                onHomeClick = { navController.navigate(NavRoutes.HOME) { popUpTo(NavRoutes.HOME) { inclusive = true } } },
                onAddClick = { navController.navigate(NavRoutes.FORM) }
            )
        }
        composable(NavRoutes.MY_FARMLAND) {
            MyFarmlandScreen(
                onBack = { navController.popBackStack() },
                onEditProperty = { propertyId -> navController.navigate("form?propertyId=$propertyId") }
            )
        }
        composable(NavRoutes.HOME) {
            HomeScreen(
                onPropertyClick = { propertyId ->
                    navController.navigate("detail/$propertyId")
                },
                onAddProperty = {
                    navController.navigate(NavRoutes.FORM)
                },
                onProfileClick = {
                    navController.navigate(NavRoutes.PROFILE)
                }
            )
        }
    }
}
