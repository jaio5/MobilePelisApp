package com.example.pppp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.remote.MoviesApi
import com.example.pppp.data.remote.RetrofitClient
import com.example.pppp.data.remote.dataclass.ReviewRequest
import com.example.pppp.data.repository.MoviesRepository
import com.example.pppp.viewmodel.MoviesViewModel
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBack: () -> Unit = {},
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
    val context = LocalContext.current
    val tokenDataStore = remember { TokenDataStore(context) }
    var token by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<Long?>(null) }
    var username by remember { mutableStateOf<String?>(null) }
    // Cargar token y username desde DataStore y obtener userId real
    LaunchedEffect(Unit) {
        token = tokenDataStore.getAccessToken().first()
        if (!token.isNullOrBlank()) {
            // Obtener userId y username reales
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(RetrofitClient.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = retrofit.create(MoviesApi::class.java)
                val userApi = retrofit.create(com.example.pppp.data.remote.UserApi::class.java)
                val response = userApi.getMe("Bearer $token")
                if (response.isSuccessful) {
                    val user = response.body()
                    userId = user?.id
                    username = user?.username
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val movieReviews by viewModel.movieReviews.collectAsState()
    val reviewPostResult by viewModel.reviewPostResult.collectAsState()
    var reviewText by remember { mutableStateOf("") }
    var reviewStars by remember { mutableStateOf(3) }

    // Lanzar la carga de comentarios solo una vez al entrar en la pantalla y cuando el token cambie
    LaunchedEffect(movieId, token) {
        if (!token.isNullOrBlank()) {
            viewModel.getMovieReviews(movieId, "Bearer $token")
        }
    }

    LaunchedEffect(movieId) {
        if (movieId > 0) {
            viewModel.getMovieDetails(movieId)
            viewModel.getMovieFiles(movieId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Película") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            movieId <= 0L -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ID de película inválido")
                }
            }
            movieDetailsState == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            movieDetailsState.isSuccessful -> {
                val movie = (movieDetailsState as retrofit2.Response<*>).body() as? com.example.pppp.data.remote.dataclass.Movie
                if (movie != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Hero section con poster grande
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                            ) {
                                // Poster de fondo con blur
                                if (movie.posterUrl != null) {
                                    AsyncImage(
                                        model = movie.posterUrl,
                                        contentDescription = movie.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.3f
                                    )
                                }

                                // Gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.7f),
                                                    Color.Black.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )

                                // Contenido del hero
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Poster principal
                                    Card(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(270.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(12.dp)
                                    ) {
                                        if (movie.posterUrl != null) {
                                            AsyncImage(
                                                model = movie.posterUrl,
                                                contentDescription = movie.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF6366F1),
                                                                Color(0xFFEC4899)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    movie.title.take(1),
                                                    fontSize = 64.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(24.dp))

                                    // Información de la película
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = movie.title,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Acciones rápidas
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { /* Like */ },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Favorite,
                                                    "Me gusta",
                                                    tint = Color(0xFFE94560)
                                                )
                                            }

                                            IconButton(
                                                onClick = { /* Añadir a watchlist */ },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    "Watchlist",
                                                    tint = Color.White
                                                )
                                            }

                                            IconButton(
                                                onClick = { /* Compartir */ },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Share,
                                                    "Compartir",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sinopsis
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "Sinopsis",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = movie.overview ?: "Sin descripción disponible",
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Archivos disponibles
                        val files = if (movieFilesState?.isSuccessful == true) {
                            (movieFilesState as retrofit2.Response<*>).body() as? com.example.pppp.data.remote.dataclass.MovieFiles
                        } else null
                        if (files != null && files.files.isNotEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                ) {
                                    Text(
                                        "Archivos disponibles",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                            items(files.files) { file ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            "Archivo",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                file.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "${file.size / 1024 / 1024} MB",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        IconButton(onClick = { /* Descargar */ }) {
                                            Icon(Icons.Filled.Download, "Descargar")
                                        }
                                    }
                                }
                            }
                        }

                        // Sección de reseñas (mejorada)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "Reseñas",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                when {
                                    movieReviews == null -> {
                                        CircularProgressIndicator()
                                    }
                                    movieReviews?.isSuccessful == true -> {
                                        val reviews = movieReviews?.body()?.content ?: emptyList()
                                        if (reviews.isEmpty()) {
                                            Text("No hay reseñas todavía.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        } else {
                                            reviews.forEach { review ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text("Usuario: ${review.username ?: review.userId}", fontWeight = FontWeight.Bold)
                                                        Text(review.text)
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            repeat(review.stars) {
                                                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.Yellow)
                                                            }
                                                            repeat(5 - review.stars) {
                                                                Icon(Icons.Filled.StarBorder, contentDescription = null, tint = Color.Gray)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Text("Error al cargar reseñas", color = Color.Red)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = {
                                            if (token != null) viewModel.getMovieReviews(movieId, "Bearer $token")
                                        }) {
                                            Text("Reintentar")
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (!token.isNullOrBlank()) {
                                    Text("Añadir reseña", fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = reviewText,
                                        onValueChange = { reviewText = it },
                                        label = { Text("Comentario") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Valoración:")
                                        // StarBar interactivo
                                        Row(modifier = Modifier.padding(start = 8.dp)) {
                                            for (i in 1..5) {
                                                IconButton(onClick = { reviewStars = i }) {
                                                    Icon(
                                                        imageVector = if (i <= reviewStars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                        contentDescription = null,
                                                        tint = if (i <= reviewStars) Color.Yellow else Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                        Text("${reviewStars}★", modifier = Modifier.padding(start = 8.dp))
                                    }
                                    Button(
                                        onClick = {
                                            if (reviewText.isNotBlank()) {
                                                viewModel.postReview(
                                                    ReviewRequest(
                                                        userId = userId ?: 0L,
                                                        movieId = movieId,
                                                        text = reviewText,
                                                        stars = reviewStars
                                                    ),
                                                    "Bearer $token"
                                                )
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Enviar")
                                    }
                                    // Feedback tras enviar reseña
                                    LaunchedEffect(reviewPostResult) {
                                        if (reviewPostResult?.isSuccessful == true) {
                                            // Refrescar reseñas y resetear campos
                                            viewModel.getMovieReviews(movieId, "Bearer $token")
                                            reviewText = ""
                                            reviewStars = 3
                                        }
                                    }
                                    if (reviewPostResult != null) {
                                        if (reviewPostResult?.isSuccessful == true) {
                                            Text("¡Reseña enviada!", color = Color.Green)
                                        } else {
                                            Text("Error al enviar reseña", color = Color.Red)
                                        }
                                    }
                                } else {
                                    Text("Inicia sesión para dejar una reseña.", color = Color.Gray)
                                }
                            }
                        }

                        // Espaciado final
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error al cargar los detalles de la película")
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error al cargar los detalles de la película")
                }
            }
        }
    }
}