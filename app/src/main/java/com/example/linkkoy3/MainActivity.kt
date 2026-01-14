package com.example.linkkoy3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.linkkoy3.data.AuthManager
import com.example.linkkoy3.ui.auth.LoginScreen
import com.example.linkkoy3.ui.auth.LoginViewModel
import com.example.linkkoy3.ui.auth.RegisterScreen
import com.example.linkkoy3.ui.auth.RegisterViewModel
import com.example.linkkoy3.ui.home.HomeScreen
import com.example.linkkoy3.ui.home.HomeViewModel
import com.example.linkkoy3.ui.folder.FolderScreen
import com.example.linkkoy3.ui.folder.FolderViewModel
import com.example.linkkoy3.ui.theme.Linkkoy3Theme
import com.example.linkkoy3.ui.theme.Background
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authManager = AuthManager(this)
        setContent {
            Linkkoy3Theme {
                AppNavigation(authManager)
            }
        }
    }
}

@Composable
fun AppNavigation(authManager: AuthManager) {
    val navController = rememberNavController()
    val token by authManager.token.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = "splash" // İlk durak açılış ekranı
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                val nextDestination = if (token == null) "login" else "home"
                navController.navigate(nextDestination) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("login") {
            val viewModel = remember { LoginViewModel(authManager) }
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        
        composable("register") {
            val viewModel = remember { RegisterViewModel(authManager) }
            RegisterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        
        composable("home") {
            val viewModel = remember { HomeViewModel(authManager) }
            HomeScreen(
                viewModel = viewModel,
                onNavigateToFolder = { folderId -> navController.navigate("folder/$folderId") },
                onLogout = { navController.navigate("login") { popUpTo("home") { inclusive = true } } }
            )
        }
        
        composable(
            route = "folder/{folderId}",
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val viewModel = remember { FolderViewModel() }
            FolderScreen(
                folderId = folderId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSubfolder = { subId -> navController.navigate("folder/$subId") }
            )
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 saniye göster
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Ekranı tamamen kaplaması için
        )
    }
}
