package com.example.pppp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val authRepository = AuthRepository(Retrofit.apiAuth)
    val moviesRepository = MoviesRepository(Retrofit.Movies)
    val userRepository = UserRepository(Retrofit.Users)
    val tokenDataStore = TokenDataStore(context)

    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "app-db")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    val userLocalRepository = UserLocalRepository(db.userDao())

    val authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, userLocalRepository, tokenDataStore) as T
        }
    })
    val moviesViewModel: MoviesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MoviesViewModel(moviesRepository) as T
        }
    })
    val adminViewModel: AdminViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(userRepository) as T
        }
    })

    val uiState by authViewModel.uiState.collectAsState()
    val moviesResponse by moviesViewModel.movies.collectAsState()
    val currentPage by moviesViewModel.currentPage.collectAsState()
    val totalPages = moviesResponse?.body()?.totalPages ?: 1
    val moviesList = moviesResponse?.body()?.content ?: emptyList()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                val isAdmin = state.response.user?.roles?.any {
                    it.equals("ROLE_ADMIN", ignoreCase = true) || it.equals("ADMIN", ignoreCase = true)
                } == true
                if (isAdmin) {
                    navController.navigate(Routes.ADMIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            else -> {}
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
            // ✅ FIX: Load movies when entering HOME and on page changes
            LaunchedEffect(currentPage) {
                moviesViewModel.getMovies(currentPage - 1, 12) // API is 0-indexed
            }

            HomeScreen(
                movies = moviesList,
                onMovieClick = { movie -> navController.navigate("movieDetail/${movie.id}") },
                currentPage = currentPage,
                totalPages = totalPages,
                onNextPage = { moviesViewModel.nextPage() },
                onPrevPage = { moviesViewModel.prevPage() },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
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
            route = Routes.MOVIE_DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toLongOrNull() ?: -1L
            MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() },
                viewModel = moviesViewModel
            )
        }

        composable(Routes.ADMIN) {
            val token = runBlocking { tokenDataStore.getAccessToken().first() ?: "" }
            val userId = runBlocking { tokenDataStore.getUserId().first()?.toLongOrNull() ?: -1L }
            AdminScreen(
                viewModel = adminViewModel,
                token = token,
                currentUserId = userId,
                onBack = { navController.popBackStack() },
                onNavigateModeration = { navController.navigate(Routes.MODERATION) },
                onNavigateMovies = { navController.navigate(Routes.MOVIES) },
                onNavigateReviews = { navController.navigate(Routes.REVIEWS) },
                onNavigateHome = { navController.navigate(Routes.HOME) },
                onNavigateProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // Pantalla de moderación
        composable(Routes.MODERATION) {
            AdminModerationScreen(onNavigateBack = { navController.navigate(Routes.ADMIN) })
        }
        // Pantalla de gestión de películas
        composable(Routes.MOVIES) {
            AdminMoviesScreen(onNavigateBack = { navController.navigate(Routes.ADMIN) })
        }
        // Pantalla de gestión de reviews
        composable(Routes.REVIEWS) {
            AdminReviewsScreen(onNavigateBack = { navController.navigate(Routes.ADMIN) })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
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
    }
}