package com.example.pppp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.local.AppDatabase
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.repository.AuthRepository
import com.example.pppp.data.repository.MoviesRepository
import com.example.pppp.data.repository.UserLocalRepository
import com.example.pppp.data.repository.UserRepository
import com.example.pppp.ui.screens.AdminModerationScreen
import com.example.pppp.ui.screens.AdminMoviesScreen
import com.example.pppp.ui.screens.AdminReviewsScreen
import com.example.pppp.ui.screens.AdminScreen
import com.example.pppp.ui.screens.HomeScreen
import com.example.pppp.ui.screens.LoginScreen
import com.example.pppp.ui.screens.MovieDetailScreen
import com.example.pppp.ui.screens.RegisterScreen
import com.example.pppp.ui.screens.SettingsScreen
import com.example.pppp.ui.screens.UserProfileScreen
import com.example.pppp.uiState.AuthUiState
import com.example.pppp.viewmodel.AdminViewModel
import com.example.pppp.viewmodel.AuthViewModel
import com.example.pppp.viewmodel.MoviesViewModel
import com.example.pppp.viewmodel.NavigationTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ── Dependencies (created once, stable across recompositions) ───────────
    val tokenDataStore = remember { TokenDataStore(context) }

    val authRepository = remember { AuthRepository(Retrofit.apiAuth) }
    val moviesRepository = remember { MoviesRepository(Retrofit.Movies) }
    val userRepository = remember { UserRepository(Retrofit.Users) }

    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "app-db")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    val userLocalRepository = remember { UserLocalRepository(db.userDao()) }

    // ── ViewModels ────────────────────────────────────────────────────────────
    val authViewModel: AuthViewModel =
        viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository, userLocalRepository, tokenDataStore) as T
            }
        })
    val moviesViewModel: MoviesViewModel =
        viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MoviesViewModel(moviesRepository) as T
            }
        })
    val adminViewModel: AdminViewModel =
        viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AdminViewModel(userRepository) as T
            }
        })

    // ── State ─────────────────────────────────────────────────────────────────
    val uiState by authViewModel.uiState.collectAsState()
    val navTarget by authViewModel.navigationTarget.collectAsState()
    val moviesResp by moviesViewModel.movies.collectAsState()
    val currentPage by moviesViewModel.currentPage.collectAsState()

    val totalPages = moviesResp?.body()?.totalPages ?: 1
    val moviesList = moviesResp?.body()?.content ?: emptyList()

    // ── Reactive navigation after login/register ──────────────────────────────
    LaunchedEffect(navTarget) {
        when (navTarget) {
            is NavigationTarget.Admin -> {
                navController.navigate(Routes.ADMIN) {
                    popUpTo(0) { inclusive = true }
                }
                authViewModel.resetNavigationTarget()
            }

            is NavigationTarget.User -> {
                navController.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                }
                authViewModel.resetNavigationTarget()
            }

            is NavigationTarget.None -> { /* stay on current screen */
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { username, password ->
                    authViewModel.login(
                        com.example.pppp.data.remote.dataclass.LoginRequest(username, password)
                    )
                },
                onRegister = { navController.navigate(Routes.REGISTER) },
                uiState = uiState
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegister = { username, email, password ->
                    authViewModel.register(username, email, password)
                },
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            // Load movies when entering Home and when the page changes
            LaunchedEffect(currentPage) {
                moviesViewModel.getMovies(currentPage - 1, 12) // API is 0-indexed
            }

            val isAdmin = (uiState as? AuthUiState.Success)
                ?.response?.user?.roles
                ?.any { it.equals("ROLE_ADMIN", true) || it.equals("ADMIN", true) }
                ?: false

            HomeScreen(
                movies = moviesList,
                onMovieClick = { movie -> navController.navigate("movieDetail/${movie.id}") },
                currentPage = currentPage,
                totalPages = totalPages,
                onNextPage = { moviesViewModel.nextPage() },
                onPrevPage = { moviesViewModel.prevPage() },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                isAdmin = isAdmin,
                onAdminClick = { navController.navigate(Routes.ADMIN) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            UserProfileScreen(
                onHome = { navController.navigate(Routes.HOME) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onAdmin = { navController.navigate(Routes.ADMIN) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = "movieDetail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.LongType })
        ) { back ->
            val movieId = back.arguments?.getLong("movieId") ?: -1L
            MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() },
                viewModel = moviesViewModel
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onProfile = { navController.navigate(Routes.PROFILE) },
                onAdmin = { navController.navigate(Routes.ADMIN) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.ADMIN) {
            val token = runBlocking { tokenDataStore.getAccessToken().first() ?: "" }
            val userId = runBlocking { tokenDataStore.getUserId().first()?.toLongOrNull() ?: -1L }
            val roles = (uiState as? AuthUiState.Success)?.response?.user?.roles ?: emptyList()
            val isAdmin = roles.any { it.equals("ROLE_ADMIN", true) || it.equals("ADMIN", true) }
            if (isAdmin || token.isNotBlank()) {
                AdminScreen(
                    viewModel = adminViewModel,
                    token = token,
                    currentUserId = userId,
                    currentUserRoles = roles,
                    onBack = { navController.popBackStack() },
                    onNavigateModeration = { navController.navigate(Routes.MODERATION) },
                    onNavigateMovies = { navController.navigate(Routes.MOVIES) },
                    onNavigateReviews = { navController.navigate(Routes.REVIEWS) }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(Routes.MODERATION) {
            val token = runBlocking { tokenDataStore.getAccessToken().first() ?: "" }
            AdminModerationScreen(
                token = token,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MOVIES) {
            val token = runBlocking { tokenDataStore.getAccessToken().first() ?: "" }
            AdminMoviesScreen(
                token = token,
                onNavigateBack = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate("movieDetail/$movieId") }
            )
        }

        composable(Routes.REVIEWS) {
            val token = runBlocking { tokenDataStore.getAccessToken().first() ?: "" }
            AdminReviewsScreen(
                token = token,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}