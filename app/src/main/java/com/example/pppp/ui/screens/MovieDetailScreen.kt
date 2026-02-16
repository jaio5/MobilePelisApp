package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pppp.data.remote.MoviesApi
import com.example.pppp.data.remote.RetrofitClient
import com.example.pppp.data.repository.MoviesRepository
import com.example.pppp.viewmodel.MoviesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    isAdmin: Boolean = false,
    onAdmin: (() -> Unit)? = null,
    onLogout: () -> Unit = {},
    viewModel: MoviesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val api = RetrofitClient.instance.create(MoviesApi::class.java)
            val repository = MoviesRepository(api)
            return MoviesViewModel(repository) as T
        }
    })
) {
    val movieDetails by viewModel.movieDetails.collectAsState()
    val movieFiles by viewModel.movieFiles.collectAsState()
    val movieDetailsState = movieDetails
    val movieFilesState = movieFiles

    LaunchedEffect(movieId) {
        if (movieId > 0) {
            viewModel.getMovieDetails(movieId)
            viewModel.getMovieFiles(movieId)
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Detalle de Película", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            when {
                movieId <= 0L -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ID de película inválido",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                movieDetailsState == null -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                movieDetailsState is retrofit2.Response<*> && movieDetailsState.isSuccessful && (movieDetailsState.body() as? com.example.pppp.data.remote.dataclass.Movie) != null -> {
                    val movie = movieDetailsState.body() as com.example.pppp.data.remote.dataclass.Movie
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = movie.overview ?: "Sin descripción disponible",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        if (movieFilesState is retrofit2.Response<*> && movieFilesState.isSuccessful && (movieFilesState.body() as? com.example.pppp.data.remote.dataclass.MovieFiles) != null) {
                            val files = (movieFilesState.body() as com.example.pppp.data.remote.dataclass.MovieFiles).files
                            if (files.isNotEmpty()) {
                                Text(
                                    text = "Archivos disponibles:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                files.forEach { file ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Tamaño: ${file.size / 1024 / 1024} MB",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Descargar",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            // Aquí podrías añadir un botón para descargar o reproducir usando file.downloadUrl o file.streamUrl
                                        }
                                    }
                                }
                            } else {
                                Text("No hay archivos disponibles para esta película.")
                            }
                        } else if (movieFilesState is retrofit2.Response<*> && !movieFilesState.isSuccessful) {
                            Text("Error al cargar archivos de la película.")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        // Aquí podrías mostrar reviews de la película si tienes el endpoint y el modelo
                    }
                }
                movieDetailsState is retrofit2.Response<*> && movieDetailsState.isSuccessful && (movieDetailsState.body() as? com.example.pppp.data.remote.dataclass.Movie) == null -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No se encontraron detalles para esta película.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Error al cargar los detalles",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack) { Text("Volver") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onHome) { Text("Ir a Home") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onProfile) { Text("Perfil") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSettings) { Text("Ajustes") }
            if (isAdmin && onAdmin != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAdmin) { Text("Admin") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onLogout) { Text("Cerrar sesión") }
        }
    }
}
