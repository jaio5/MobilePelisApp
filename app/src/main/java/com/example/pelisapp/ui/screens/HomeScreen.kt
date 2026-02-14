package com.example.pelisapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pelisapp.data.remote.dataclass.Movie

@Composable
fun HomeScreen(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(movies.size) { index ->
                val movie = movies[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onMovieClick(movie) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = movie.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = movie.overview ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Página $currentPage de $totalPages", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onPrevPage, enabled = currentPage > 1) {
                Text("Atrás")
            }
            Button(onClick = onNextPage, enabled = currentPage < totalPages) {
                Text("Siguiente")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onProfileClick() }) {
                Text("Ir al perfil del usuario")
            }
            Button(onClick = { onSettingsClick() }) {
                Text("Ajustes")
            }
        }
    }
}