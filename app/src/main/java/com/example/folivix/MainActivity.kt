package com.example.folivix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.folivix.ui.screens.main.InfoScreen
import com.example.folivix.ui.screens.main.MainScreen
import com.example.folivix.ui.screens.main.UserManualScreen
import com.example.folivix.ui.screens.main.UserProfileScreen
import com.example.folivix.ui.screens.profile.CreateProfileScreen
import com.example.folivix.ui.screens.profile.ManageProfilesScreen
import com.example.folivix.ui.screens.profile.ProfileSelectionScreen
import com.example.folivix.ui.screens.splash.SplashScreen1
import com.example.folivix.ui.screens.splash.SplashScreen2
import com.example.folivix.ui.theme.FolivixTheme
import com.example.folivix.viewmodel.MainViewModel
import com.example.folivix.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FolivixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FolivixApp()
                }
            }
        }
    }
}

@Composable
fun FolivixApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    NavHost(navController = navController, startDestination = "splash1") {
        composable("splash1") {
            SplashScreen1(onTimeout = { navController.navigate("splash2") })
        }

        composable("splash2") {
            SplashScreen2(onTimeout = {
                if (uiState.users.isEmpty()) {
                    navController.navigate("create_profile") {
                        popUpTo("splash1") { inclusive = true }
                    }
                } else if (uiState.currentUser != null) {
                    navController.navigate("main") {
                        popUpTo("splash1") { inclusive = true }
                    }
                } else {
                    navController.navigate("profile_selection") {
                        popUpTo("splash1") { inclusive = true }
                    }
                }
            })
        }

        composable("profile_selection") {
            ProfileSelectionScreen(
                profiles = uiState.users,
                onProfileSelected = { selectedUser ->
                    viewModel.setCurrentUser(selectedUser)
                    navController.navigate("main") {
                        popUpTo("profile_selection") { inclusive = true }
                    }
                },
                onCreateProfile = { navController.navigate("create_profile") },
                onManageProfiles = { navController.navigate("manage_profiles") }
            )
        }

        composable("manage_profiles") {
            ManageProfilesScreen(
                profiles = uiState.users,
                onDeleteProfile = { user ->
                    viewModel.deleteUser(user)
                    if (uiState.users.size <= 1) {
                        navController.navigate("create_profile") {
                            popUpTo("manage_profiles") { inclusive = true }
                        }
                    }
                },
                onEditProfile = { user ->
                    viewModel.setCurrentUser(user)
                    navController.navigate("user_profile")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("create_profile") {
            CreateProfileScreen(
                onCreateProfile = { name, imageUri ->
                    viewModel.createUser(name, imageUri)
                    navController.navigate("main") {
                        popUpTo("create_profile") { inclusive = true }
                    }
                },
                onBack = {
                    if (uiState.users.isEmpty()) {
                        // If there are no users, there´s no where to go back to
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable("main") {
            uiState.currentUser?.let { user ->
                MainScreen(
                    navController = navController,
                    currentUser = user
                )
            }
        }

        composable("info") {
            InfoScreen(
                onNavigateToManual = { navController.navigate("user_manual") },
                onNavigateToHome = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("user_manual") {
            UserManualScreen(
                onNavigateToHome = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("user_profile") {
            val userProfileViewModel: UserProfileViewModel = hiltViewModel()

            UserProfileScreen(
                viewModel = userProfileViewModel,
                onNavigateToProfiles = {
                    navController.navigate("profile_selection") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onDeleteUser = {
                    navController.navigate("profile_selection") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
