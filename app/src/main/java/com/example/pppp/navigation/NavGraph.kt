package com.example.pppp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pppp.ui.screens.AdminScreen
import com.example.pppp.ui.screens.HomeScreen
import com.example.pppp.ui.screens.LoginScreen
import com.example.pppp.ui.screens.RegisterScreen
import com.example.pppp.ui.screens.SettingsScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.pppp.viewmodel.AuthViewModel
import com.example.pppp.viewmodel.MoviesViewModel
import com.example.pppp.data.repository.AuthRepository
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.AuthApi
import com.example.pppp.uiState.AuthUiState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pppp.data.repository.UserLocalRepository
import com.example.pppp.data.local.UserDao
import androidx.compose.ui.platform.LocalContext
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.ui.components.BottomNavigationBar
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text

@Composable
fun NavGraph(navController: NavHostController, userDao: UserDao) {
    val context = LocalContext.current
    val authApi = Retrofit.instance.create(AuthApi::class.java)
    val repository = AuthRepository(authApi)
    val userLocalRepository = UserLocalRepository(userDao)
    val tokenDataStore = TokenDataStore(context)
    val authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, userLocalRepository, tokenDataStore, context) as T
        }
    })
    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // ViewModel compartido para películas
    val moviesApi = Retrofit.instance.create(com.example.pppp.data.remote.MoviesApi::class.java)
    val moviesRepository = com.example.pppp.data.repository.MoviesRepository(moviesApi)
    val moviesViewModel: MoviesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MoviesViewModel(moviesRepository) as T
        }
    })
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLogin = { username, password ->
                    authViewModel.login(username, password)
                },
                onRegister = {
                    navController.navigate("register")
                },
                uiState = uiState
            )
            if (uiState is AuthUiState.Error) {
                val errorMsg = (uiState as AuthUiState.Error).message
                if (errorMsg.isNotBlank()) {
                    LaunchedEffect(errorMsg) {
                        snackbarHostState.showSnackbar(errorMsg)
                    }
                }
            }
            SnackbarHost(hostState = snackbarHostState)
            LaunchedEffect(uiState) {
                when (uiState) {
                    is AuthUiState.Success -> {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
        }
        composable("register") {
            RegisterScreen(
                onRegister = { username, email, password ->
                    authViewModel.register(username, email, password)
                },
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
            LaunchedEffect(uiState) {
                when (uiState) {
                    is AuthUiState.Success -> {
                        val user = (uiState as AuthUiState.Success).response.user
                        if (user != null) {
                            val isAdmin = user.roles.contains("ROLE_ADMIN")
                            if (isAdmin) {
                                navController.navigate("admin") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
        composable("home") {
            val moviesResponse by moviesViewModel.movies.collectAsState()
            val currentPage by moviesViewModel.currentPage.collectAsState()
            val totalPages = moviesResponse?.body()?.totalPages ?: 1
            LaunchedEffect(currentPage) {
                moviesViewModel.getMovies(currentPage, 20)
            }
            val moviesList = moviesResponse?.body()?.content ?: emptyList()
            HomeScreen(
                movies = moviesList,
                onMovieClick = {
                    if (it.id > 0) {
                        navController.navigate("movieDetail/${it.id}")
                    }
                },
                currentPage = currentPage,
                totalPages = totalPages,
                onNextPage = { moviesViewModel.nextPage() },
                onPrevPage = { moviesViewModel.prevPage() },
                onProfileClick = { navController.navigate("profile") },
                onSettingsClick = { navController.navigate("settings") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable("movieDetail/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toLongOrNull() ?: -1L
            com.example.pppp.ui.screens.MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("admin"){
            val userApi = Retrofit.instance.create(com.example.pppp.data.remote.UserApi::class.java)
            val userRepository = com.example.pppp.data.repository.UserRepository(userApi)
            val adminViewModel: com.example.pppp.viewmodel.AdminViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return com.example.pppp.viewmodel.AdminViewModel(userRepository) as T
                }
            })
            when (uiState) {
                is AuthUiState.Success -> {
                    val token = (uiState as AuthUiState.Success).response.accessToken
                    val userId = (uiState as AuthUiState.Success).response.user?.id ?: -1L
                    AdminScreen(
                        viewModel = adminViewModel,
                        token = token,
                        currentUserId = userId
                    )
                }
                is AuthUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is AuthUiState.Error -> {
                    Text("Error: " + (uiState as AuthUiState.Error).message)
                }
                else -> {
                    Text("Cargando información de usuario...")
                }
            }
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("profile") {
            com.example.pppp.ui.screens.UserProfileScreen()
        }
    }
    val user = if (uiState is AuthUiState.Success) (uiState as AuthUiState.Success).response.user else null
    if (currentRoute !in listOf("login", "register") && user != null) {
        BottomNavigationBar(
            currentRoute = currentRoute,
            onNavigate = { route -> navController.navigate(route) },
            isAdmin = user.roles.contains("ROLE_ADMIN")
        )
    }
}