package com.kotlingdgocucb.elimuApp.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.airbnb.lottie.compose.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.domain.model.User
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.Video
import com.kotlingdgocucb.elimuApp.ui.components.Rating
import com.kotlingdgocucb.elimuApp.ui.viewmodel.VideoViewModel

// Composable permettant d'afficher un titre tronqué (maxLength lettres)
// L'utilisateur peut maintenir l'appui sur le titre pour basculer entre l'affichage complet et tronqué.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableTitle(
    title: String,
    maxLength: Int = 25,
    style: TextStyle,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val displayTitle = if (expanded || title.length <= maxLength) title else title.take(maxLength) + "..."
    Text(
        text = displayTitle,
        style = style,
        color = color,
        modifier = Modifier.combinedClickable(
            onClick = { /* Action sur clic simple si nécessaire */ },
            onLongClick = { expanded = !expanded }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    videoViewModel: VideoViewModel = koinViewModel(),
    navController: NavController,
    userInfo: User?
) {
    // Observer la liste des vidéos depuis le ViewModel
    val videosState = videoViewModel.videos.observeAsState(initial = emptyList())
    var isRefreshing by remember { mutableStateOf(false) }
    var showMorePopular by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        videoViewModel.fetchAllVideos()
    }

    if (videosState.value.isEmpty()) {
        FullScreenLoadingAnimation()
        return
    }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val sortedVideos = videosState.value.sortedBy { it.order }
    val mentorVideos = sortedVideos.filter { it.mentor_email == userInfo?.mentor_email }
    Log.d("ELIMUMENTOR", "Lecture du mentor: ${userInfo?.mentor_email}")
    val trackVideos = mentorVideos.filter { it.category.equals(userInfo?.track, ignoreCase = true) }

    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    val suggestions = if (searchQuery.isNotEmpty()) {
        trackVideos.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else emptyList()

    val filteredVideos = if (searchQuery.isBlank()) trackVideos
    else trackVideos.filter { it.title.contains(searchQuery, ignoreCase = true) }

    val popularVideos = if (showMorePopular) {
        filteredVideos.filter { it.stars > 3.5 }
    } else {
        filteredVideos.take(3)
    }
    val recommendedVideos = if (showMorePopular) {
        emptyList()
    } else {
        filteredVideos.drop(3)
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold { innerPadding ->
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                showSuggestions = it.isNotEmpty()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Rechercher un cours...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true
                        )
                    }

                    // Suggestions avec animation
                    AnimatedVisibility(
                        visible = showSuggestions && suggestions.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
                        exit = fadeOut(animationSpec = tween(200))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                itemsIndexed(suggestions) { index, suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchQuery = suggestion.title
                                                showSuggestions = false
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = "https://img.youtube.com/vi/${suggestion.youtube_url}/default.jpg",
                                            contentDescription = "Miniature",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = suggestion.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (index < suggestions.size - 1) {
                                        Divider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                    )
                    }

                    SectionTitle(
                        title = "Populaires",
                        onVoirPlus = { navController.navigate("screenVideoPopulare") },
                        textColor = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        itemsIndexed(popularVideos) { index, video ->
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    initialScale = 0.8f
                                ) + fadeIn(animationSpec = tween(300, delayMillis = index * 50))
                            ) {
                                VideoCardPopular(video = video) {
                                    navController.navigate("videoDetail/${video.id}")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    if (recommendedVideos.isNotEmpty()) {
                        SectionTitle(
                            title = "Pour vous",
                            onVoirPlus = {
                                navController.navigate("videoDetail/${video.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isTablet) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(recommendedVideos) { index, video ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = scaleIn(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy
                                            ),
                                            initialScale = 0.9f
                                        ) + fadeIn(animationSpec = tween(400, delayMillis = index * 30))
                                    ) {
                                        VideoGridItem(video = video) {
                                            navController.navigate("videoDetail/${video.id}")
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                itemsIndexed(recommendedVideos) { index, video ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = slideInVertically(
                                            animationSpec = tween(300, delayMillis = index * 50),
                                            initialOffsetY = { it / 4 }
                                        ) + fadeIn(animationSpec = tween(300, delayMillis = index * 50))
                                    ) {
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
    }
}
                                        navController.navigate("videoDetail/${video.id}")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(recommendedVideos) { video ->
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
}

/** Animation Lottie de chargement en plein écran */
@Composable
fun FullScreenLoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.loading)
        )
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(150.dp)
        )
    }
}

/** Titre de section avec lien "voir plus" */
@Composable
fun SectionTitle(
    title: String,
    onVoirPlus: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 20.sp
            )
            TextButton(
                onClick = onVoirPlus,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Voir plus",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** Carte "Populaire" pour une vidéo */
@Composable
fun VideoCardPopular(video: Video, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(320.dp)
            .height(200.dp)
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SubcomposeAsyncImage(
                    model = "https://img.youtube.com/vi/${video.youtube_url}/hqdefault.jpg",
                    contentDescription = "Miniature de ${video.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> LottieImageLoadingAnimation()
                        else -> SubcomposeAsyncImageContent()
                    }
                }
                
                // Gradient overlay pour améliorer la lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                ExpandableTitle(
                    title = video.title,
                    maxLength = 25,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Rating(rating = video.stars)
                    Spacer(modifier = Modifier.width(8.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Vues",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${video.progresses.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cours ${video.order}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = video.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
                    )
                }
            }
        }
    }
}

/** Élément d'une liste (téléphone) : miniature et texte à droite */
@Composable
fun VideoRowItem(video: Video, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SubcomposeAsyncImage(
                    model = "https://img.youtube.com/vi/${video.youtube_url}/hqdefault.jpg",
                    contentDescription = "Miniature de ${video.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> LottieImageLoadingAnimation()
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExpandableTitle(
                    title = video.title,
                    maxLength = 30,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Rating(rating = video.stars)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Vues",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${video.progresses.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Cours ${video.order}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
                )
            }
            // Ligne d'affichage du numéro de cours et de la catégorie
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Icône vidéo",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Cours numéro : ${video.order}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Catégorie",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = video.category,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Élément d'une grille (tablette) pour une vidéo */
@Composable
fun VideoGridItem(video: Video, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SubcomposeAsyncImage(
                    model = "https://img.youtube.com/vi/${video.youtube_url}/hqdefault.jpg",
                    contentDescription = "Miniature de ${video.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> LottieImageLoadingAnimation()
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExpandableTitle(
                    title = video.title,
                    maxLength = 20,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Rating(rating = video.stars)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Cours ${video.order}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = video.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}
                    )
                }
            }
        }
    }
}

/** Animation Lottie pour le chargement d'une image miniature */
@Composable
fun LottieImageLoadingAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.imageloading)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(80.dp)
    )
}
