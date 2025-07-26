package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.Video
import com.kotlingdgocucb.elimuApp.ui.viewmodel.VideoViewModel
import com.kotlingdgocucb.elimuApp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenVideoTrack(
    track: String,
    videoViewModel: VideoViewModel = koinViewModel(),
    navController: NavController
) {
    // Observer la liste des vidéos depuis le ViewModel
    val videosState = videoViewModel.videos.observeAsState(initial = emptyList())
    // Etat de rafraîchissement
    var isRefreshing by remember { mutableStateOf(false) }
    // Lancer la récupération des vidéos
    LaunchedEffect(Unit) {
        videoViewModel.fetchAllVideos()
    }

    // Pour le pull-to-refresh
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val coroutineScope = rememberCoroutineScope()

    // Barre de recherche et suggestions
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    // Gestion du menu déroulant de tri/filtrage
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Ce que le mentor a prévu") }

    // Filtrer d’abord par track (la catégorie correspond au track)
    val trackVideos = videosState.value.filter {
        it.category.equals(track, ignoreCase = true)
    }
    // Suggestions extraites en fonction du texte saisi
    val suggestions = if (searchQuery.isNotEmpty()) {
        trackVideos.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else emptyList()

    // Liste finale en fonction du filtre sélectionné et de la recherche sur le titre
    val finalVideos: List<Video> = when (selectedFilter) {
        "Ce que le mentor a prévu" -> trackVideos.sortedBy { it.order.toIntOrNull() ?: 0 }
        "Date" -> trackVideos.sortedByDescending { it.id }
        "Track" -> trackVideos
        "Catégorie" -> trackVideos
        "Voir tout" -> videosState.value
        else -> trackVideos.sortedBy { it.order.toIntOrNull() ?: 0 }
    }.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // Bouton de retour
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                title = {
                    Text(
                        text = "Vidéos pour $track",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    // Icône pour ouvrir le menu déroulant de filtre
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrer",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ce que le mentor a prévu") },
                            onClick = {
                                selectedFilter = "Ce que le mentor a prévu"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Date") },
                            onClick = {
                                selectedFilter = "Date"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Track") },
                            onClick = {
                                selectedFilter = "Track"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Catégorie") },
                            onClick = {
                                selectedFilter = "Catégorie"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Voir tout") },
                            onClick = {
                                selectedFilter = "Voir tout"
                                expanded = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
//                    titleContentColor = MaterialTheme.colorScheme.onBackground,
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        // Contenu principal enveloppé dans un SwipeRefresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                videoViewModel.fetchAllVideos()
                coroutineScope.launch {
                    delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (videosState.value.isEmpty()) {
                // Affichage de l'animation Lottie pendant le chargement
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(150.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    // Barre de recherche avec suggestions
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showSuggestions = it.isNotEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        label = {
                            Text(
                                "Chercher un cours",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50.dp),
                        maxLines = 1
                    )
                    // Suggestions en overlay sous le champ de recherche
                    if (showSuggestions && suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            LazyColumn {
                                items(suggestions) { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Mise à jour du champ de recherche lors du clic sur la suggestion
                                                searchQuery = suggestion.title
                                                showSuggestions = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Image à gauche (miniature YouTube)
                                        AsyncImage(
                                            model = "https://img.youtube.com/vi/${suggestion.youtube_url}/default.jpg",
                                            contentDescription = "Miniature de ${suggestion.title}",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RectangleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        // Titre de la vidéo
                                        Text(
                                            text = suggestion.title,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = "Tous les cours",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Affichage de la liste des vidéos
                    if (finalVideos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucune vidéo trouvée.",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                        ) {
                            items(finalVideos) { video ->
                                VideoRowItem(video = video) {
                                    navController.navigate("videoDetail/${video.id}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
