package com.example.pelisapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pelisapp.viewmodel.MoviesViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pelisapp.data.remote.dataclass.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesListScreen(viewModel: MoviesViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadMoviesByGenres(genres)
    }

    // Observa el StateFlow de películas agrupadas por género
    val moviesByGenre by viewModel.moviesByGenre.collectAsState(initial = emptyMap())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Películas") }) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            // Por cada género, muestra el título y la fila horizontal de películas
            moviesByGenre.entries.forEach { (genre, movies) ->
                item {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.titleLarge, // Material3 style
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(movies) { movie ->
                            MovieItem(movie)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie) {
    Card(
        modifier = Modifier
            .padding(end = 8.dp)
            .width(120.dp)
            .height(180.dp)
    ) {
        Text(
            text = movie.title,
            modifier = Modifier.padding(8.dp)
        )
    }
}