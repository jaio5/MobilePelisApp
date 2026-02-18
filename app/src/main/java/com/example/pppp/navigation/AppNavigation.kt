package com.example.pppp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.pppp.ui.screens.UserDetailScreen
import com.example.pppp.ui.screens.EditUserScreen
import com.example.pppp.ui.screens.EditMovieScreen
import com.example.pppp.ui.screens.EditReviewScreen
import com.example.pppp.ui.screens.ReviewDetailScreen
import com.example.pppp.ui.screens.ErrorScreen
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
            val currentUser by authViewModel.currentUser.collectAsState()
            val token = runBlocking { tokenDataStore.getAccessToken().first() ?: "" }
            val userId = currentUser?.id ?: -1L
            val currentUserRoles = currentUser?.roles
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.ifEmpty { listOf("ROLE_USER") } ?: listOf("ROLE_USER")

            // Espera a que los roles sean correctos antes de mostrar la pantalla
            if (currentUserRoles.any { it.equals("ROLE_ADMIN", ignoreCase = true) || it.equals("ADMIN", ignoreCase = true) }) {
                AdminScreen(
                    viewModel = adminViewModel,
                    token = token,
                    currentUserId = userId,
                    currentUserRoles = currentUserRoles,
                    onBack = { navController.popBackStack() },
                    onNavigateToModeration = { navController.navigate(Routes.MODERATION) },
                    onNavigateToMovies = { navController.navigate(Routes.MOVIES) },
                    onNavigateReviews = { navController.navigate(Routes.REVIEWS) },
                    onNavigateHome = { navController.navigate(Routes.HOME) },
                    onNavigateProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateMovieDetail = { movieId -> navController.navigate("movieDetail/$movieId") },
                    onNavigateToUserProfile = { userId: Long -> navController.navigate("userProfile/$userId") },
                    onNavigateBulkUpload = { navController.navigate("bulkUpload") }
                )
            } else {
                // Muestra un indicador de carga mientras se actualizan los roles
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(Routes.MODERATION) {
            AdminModerationScreen(
                onNavigateBack = { navController.navigate(Routes.ADMIN) }
            )
        }
        composable(Routes.MOVIES) {
            AdminMoviesScreen(
                onNavigateBack = { navController.navigate(Routes.ADMIN) }
            )
        }
        composable(Routes.REVIEWS) {
            AdminReviewsScreen(
                onNavigateBack = { navController.navigate(Routes.ADMIN) }
            )
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
        composable("userProfile/{userId}", arguments = listOf(navArgument("userId") { type = NavType.StringType })) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            userId?.let {
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
        }
        composable("bulkUpload") {
            // IMPORTS necesarios para Box, Modifier, Alignment, Text
            Surface {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pantalla de carga masiva (por implementar)")
                }
            }
        }
        composable("userDetail/{userId}", arguments = listOf(navArgument("userId") { type = NavType.LongType })) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: -1L
            UserDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("editUser/{userId}", arguments = listOf(navArgument("userId") { type = NavType.LongType })) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: -1L
            EditUserScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable("editMovie/{movieId}", arguments = listOf(navArgument("movieId") { type = NavType.LongType })) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: -1L
            EditMovieScreen(movieId = movieId, onBack = { navController.popBackStack() })
        }
        composable("editReview/{reviewId}", arguments = listOf(navArgument("reviewId") { type = NavType.LongType })) { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getLong("reviewId") ?: -1L
            EditReviewScreen(reviewId = reviewId, onBack = { navController.popBackStack() })
        }
        composable("reviewDetail/{reviewId}", arguments = listOf(navArgument("reviewId") { type = NavType.LongType })) { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getLong("reviewId") ?: -1L
            ReviewDetailScreen(reviewId = reviewId, onBack = { navController.popBackStack() })
        }
        composable("movieDetailAdmin/{movieId}", arguments = listOf(navArgument("movieId") { type = NavType.LongType })) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: -1L
            MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() },
                viewModel = moviesViewModel
            )
        }
        composable("error/{message}", arguments = listOf(navArgument("message") { type = NavType.StringType })) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: "Error desconocido"
            ErrorScreen(message = message, onBack = { navController.popBackStack() })
        }
    }
}