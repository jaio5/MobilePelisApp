package com.example.pppp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.repository.UserRepository
import com.example.pppp.viewmodel.UserViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val ProfileBg      = Color(0xFF0A0A0A)
private val ProfileCard    = Color(0xFF1A1A1A)
private val ProfileGreen   = Color(0xFF00C030)
private val ProfileAmber   = Color(0xFFFFAA00)
private val ProfileTextPri = Color(0xFFF0F0F0)
private val ProfileTextSec = Color(0xFF909090)
private val ProfileTextMut = Color(0xFF505050)
private val ProfileErr     = Color(0xFFFF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onHome: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(Retrofit.User) }
    val userViewModel: UserViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
    })

    val userState  by userViewModel.user.collectAsState()
    val myReviews  by userViewModel.myReviews.collectAsState()
    val tokenDataStore = remember { TokenDataStore(context) }
    val scope = rememberCoroutineScope()

    var accessToken by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reseñas", "Estadísticas")

    val usernameFromStore by tokenDataStore.getUsername().collectAsState(initial = null)

    LaunchedEffect(Unit) {
        accessToken = tokenDataStore.getAccessToken().first()
        if (!accessToken.isNullOrBlank()) {
            userViewModel.getMe(accessToken!!)
            userViewModel.getMyReviews(accessToken!!, 0, 20)
        }
    }

    Scaffold(
        containerColor = ProfileBg,
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ProfileTextPri) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, "Ajustes", tint = ProfileTextSec)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfileBg)
            )
        }
    ) { padding ->

        when {
            userState == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ProfileGreen, strokeWidth = 2.dp)
                }
            }

            userState?.isSuccessful == true -> {
                val user    = userState?.body()
                val isAdmin = user?.roles?.contains("ROLE_ADMIN") == true
                val reviews = myReviews?.body()?.content ?: emptyList()

                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(ProfileGreen.copy(alpha = 0.06f), Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(88.dp).background(ProfileGreen.copy(alpha = 0.15f), CircleShape))
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(ProfileGreen.copy(alpha = 0.3f), ProfileGreen.copy(alpha = 0.1f))
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user?.username?.take(2)?.uppercase() ?: "??",
                                        fontSize = 28.sp, fontWeight = FontWeight.Black, color = ProfileGreen
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Text(user?.username ?: usernameFromStore ?: "Usuario", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ProfileTextPri)

                            user?.displayName?.let {
                                if (it != user.username) Text(it, fontSize = 14.sp, color = ProfileTextSec)
                            }
                            user?.email?.let { Text(it, fontSize = 12.sp, color = ProfileTextMut) }

                            if (isAdmin) {
                                Spacer(Modifier.height(8.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE94560).copy(alpha = 0.15f)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.AdminPanelSettings, null, tint = Color(0xFFE94560), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Administrador", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE94560))
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                ProfileStatItem(value = (myReviews?.body()?.totalElements ?: 0).toString(), label = "Reseñas")
                                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF222222)))
                                ProfileStatItem(value = (user?.criticLevel ?: 0).toString(), label = "Nivel crítico")
                                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF222222)))
                                ProfileStatItem(value = user?.roles?.size?.toString() ?: "1", label = "Roles")
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isAdmin) {
                                    OutlinedButton(
                                        onClick = onAdmin,
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE94560).copy(alpha = 0.5f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE94560))
                                    ) {
                                        Icon(Icons.Filled.AdminPanelSettings, null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Admin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                OutlinedButton(
                                    onClick = onHome,
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfileTextSec)
                                ) {
                                    Icon(Icons.Filled.Home, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Inicio", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onLogout,
                                    modifier = Modifier.height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E), contentColor = ProfileTextSec)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A1A1A)))
                    }

                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = ProfileBg,
                            contentColor = ProfileGreen,
                            // FIX: use Material3 built-in tabIndicatorOffset (imported at top)
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .padding(horizontal = 32.dp),
                                    color = ProfileGreen,
                                    height = 2.dp
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, label ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    selectedContentColor = ProfileGreen,
                                    unselectedContentColor = ProfileTextMut
                                ) {
                                    Text(
                                        label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A1A1A)))
                    }

                    when (selectedTab) {
                        0 -> {
                            if (reviews.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.RateReview, null, tint = ProfileTextMut, modifier = Modifier.size(40.dp))
                                            Spacer(Modifier.height(12.dp))
                                            Text("Sin reseñas aún", color = ProfileTextMut, fontSize = 14.sp)
                                        }
                                    }
                                }
                            } else {
                                items(reviews) { review ->
                                    ProfileReviewCard(review = review)
                                    Box(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp).background(Color(0xFF1A1A1A)))
                                }
                            }
                        }
                        1 -> {
                            item {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    ProfileInfoRow("Usuario", user?.username ?: "-")
                                    ProfileInfoRow("Email", user?.email ?: "-")
                                    ProfileInfoRow("Nombre", user?.displayName ?: "-")
                                    ProfileInfoRow("Nivel crítico", (user?.criticLevel ?: 0).toString())
                                    ProfileInfoRow("Roles", user?.roles?.joinToString(", ") ?: "-")
                                    ProfileInfoRow("Total reseñas", (myReviews?.body()?.totalElements ?: 0).toString())
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = ProfileErr, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Error al cargar el perfil", color = ProfileTextSec)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val token = tokenDataStore.getAccessToken().first()
                                    if (!token.isNullOrBlank()) userViewModel.getMe(token)
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
                        ) {
                            Text("Reintentar", color = ProfileTextSec)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = ProfileTextPri)
        Text(label, fontSize = 11.sp, color = ProfileTextMut)
    }
}

@Composable
private fun ProfileReviewCard(review: Review) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Película #${review.movie?.id}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ProfileTextPri)
            Row {
                repeat(review.stars ?: 0) { Icon(Icons.Filled.Star, null, tint = ProfileAmber, modifier = Modifier.size(12.dp)) }
                repeat(5 - (review.stars ?: 0)) { Icon(Icons.Filled.StarBorder, null, tint = Color(0xFF2A2A2A), modifier = Modifier.size(12.dp)) }
            }
        }
        if (!review.text.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(review.text ?: "", fontSize = 13.sp, color = ProfileTextSec, lineHeight = 19.sp, maxLines = 4)
        }
        Spacer(Modifier.height(4.dp))
        Text(review.createdAt?.take(10) ?: "", fontSize = 11.sp, color = ProfileTextMut)
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(ProfileCard, RoundedCornerShape(8.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = ProfileTextSec)
        Text(value, fontSize = 13.sp, color = ProfileTextPri, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
    Spacer(Modifier.height(6.dp))
}