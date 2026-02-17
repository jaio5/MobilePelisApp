package com.example.pppp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pppp.ui.screens.AdminScreen
import com.example.pppp.ui.screens.HomeScreen
import com.example.pppp.ui.screens.LoginScreen
import com.example.pppp.ui.screens.MovieDetailScreen
import com.example.pppp.ui.screens.RegisterScreen
import com.example.pppp.ui.screens.SettingsScreen
import com.example.pppp.ui.screens.UserProfileScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pppp.viewmodel.AuthViewModel
import com.example.pppp.viewmodel.MoviesViewModel
import com.example.pppp.viewmodel.AdminViewModel
import com.example.pppp.data.repository.AuthRepository
import com.example.pppp.data.repository.MoviesRepository
import com.example.pppp.data.repository.UserRepository
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.datastore.TokenDataStore
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.room.Room
import com.example.pppp.data.local.AppDatabase
import com.example.pppp.data.repository.UserLocalRepository
import androidx.compose.runtime.collectAsState

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authApi = Retrofit.apiAuth
    val moviesApi = Retrofit.Movies
    val userApi = Retrofit.Users
    val authRepository = AuthRepository(authApi)
    val moviesRepository = MoviesRepository(moviesApi)
    val userRepository = UserRepository(userApi)
    val tokenDataStore = TokenDataStore(context)
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "app-db").build()
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

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { username, password -> authViewModel.login(com.example.pppp.data.remote.dataclass.LoginRequest(username, password)) },
                onRegister = { navController.navigate(Routes.REGISTER) },
                uiState = uiState
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegister = { username, email, password -> authViewModel.register(username, email, password) },
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HOME) {
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
                            popUpTo(Routes.HOME) { inclusive = true }
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
                            popUpTo(Routes.PROFILE) { inclusive = true }
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
                currentUserId = userId
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}