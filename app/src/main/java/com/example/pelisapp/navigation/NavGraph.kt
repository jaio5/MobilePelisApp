package com.example.pelisapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pelisapp.ui.screens.AdminScreen
import com.example.pelisapp.ui.screens.HomeScreen
import com.example.pelisapp.ui.screens.LoginScreen
import com.example.pelisapp.ui.screens.RegisterScreen
import com.example.pelisapp.ui.screens.SettingsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.pelisapp.viewmodel.AuthViewModel
import com.example.pelisapp.viewmodel.MoviesViewModel

@Composable
fun NavGraph (navController: NavHostController){
    val authViewModel: AuthViewModel = viewModel()
    val authResponse by authViewModel.authResponse.collectAsState()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLogin = { username, password ->
                    authViewModel.login(username, password)
                },
                onRegister = {
                    navController.navigate("register")
                }
            )
            LaunchedEffect(authResponse) {
                val user = authResponse?.body()?.user
                if (authResponse != null && authResponse!!.isSuccessful && user != null) {
                    if (user.username == "admin") {
                        navController.navigate("admin") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }
        }
        composable("register") {
            RegisterScreen(function = { navController.popBackStack() })
        }
        composable("home") {
            val moviesViewModel: MoviesViewModel = viewModel()
            val moviesResponse by moviesViewModel.movies.collectAsState()
            val currentPage by moviesViewModel.currentPage.collectAsState()
            val totalPages = moviesResponse?.body()?.totalPages ?: 1
            LaunchedEffect(currentPage) {
                moviesViewModel.getMovies(currentPage, 20)
            }
            val moviesList = moviesResponse?.body()?.content ?: emptyList()
            HomeScreen(
                movies = moviesList,
                onMovieClick = { navController.navigate("movieDetail/${it.id}") },
                currentPage = currentPage,
                totalPages = totalPages,
                onNextPage = { moviesViewModel.nextPage() },
                onPrevPage = { moviesViewModel.prevPage() },
                onProfileClick = { navController.navigate("profile") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("movieDetail/{movieId}") {

        }
        composable("admin"){
            AdminScreen {}
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("profile") {

        }
    }
}