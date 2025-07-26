package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kotlingdgocucb.elimuApp.ui.viewmodel.VideoViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenVideoPopulare(
    videoViewModel: VideoViewModel = koinViewModel(),
    navController: NavController
) {
    // Observer la liste des vidéos
    val videosState = videoViewModel.videos.observeAsState(initial = emptyList())

    // Lancer la requête pour récupérer les vidéos
    LaunchedEffect(Unit) {
        videoViewModel.fetchAllVideos()
    }

    // Filtrer les vidéos populaires (note > 3.5)
    val popularVideos = videosState.value.filter { it.stars > 3.5 }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // Bouton de retour
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    androidx.compose.material.Text(
                        text = "Vidéos Populaires",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                   // containerColor = Color(0xFF1B1B1B), // Fond de la TopAppBar
                    titleContentColor = Color.White // Couleur du titre
                )
            )
        }

    ) { innerPadding ->
        if (popularVideos.isEmpty()) {
            // Afficher une animation de chargement ou un message en cas d'absence de données
            FullScreenLoadingAnimation()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(popularVideos) { video ->
                    VideoRowItem(video = video) {
                        navController.navigate("videoDetail/${video.id}")
                    }
                }
            }
        }
    }
}
