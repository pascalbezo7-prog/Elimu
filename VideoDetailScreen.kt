package com.kotlingdgocucb.elimuApp.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.ReviewCreate
import com.kotlingdgocucb.elimuApp.ui.components.Rating
import com.kotlingdgocucb.elimuApp.ui.components.RatingBarInput
import com.kotlingdgocucb.elimuApp.ui.viewmodel.MentorViewModel
import com.kotlingdgocucb.elimuApp.ui.viewmodel.ProgressViewModel
import com.kotlingdgocucb.elimuApp.ui.viewmodel.ReviewsViewModel
import com.kotlingdgocucb.elimuApp.ui.viewmodel.VideoViewModel



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableTitle2(
    title: String,
    maxLength: Int = 28,
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
fun VideoDetailScreen(
    videoId: Int,
    navController: NavController,
    videoViewModel: VideoViewModel = koinViewModel(),
    reviewsViewModel: ReviewsViewModel = koinViewModel(),
    mentorViewModel: MentorViewModel = koinViewModel(),
    progressViewModel: ProgressViewModel = koinViewModel(),
    UserEmail: String
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val coroutineScope = rememberCoroutineScope()

    // Chargement de la vidéo et des avis
    LaunchedEffect(videoId) {
        videoViewModel.fetchVideoById(videoId)
        reviewsViewModel.fetchReviews(videoId)
    }

    val video by videoViewModel.videoDetail.observeAsState()
    val reviews by reviewsViewModel.reviews.observeAsState(initial = emptyList())
    val averageRating = if (reviews.isNotEmpty())
        reviews.map { it.stars }.average().toFloat() else 0f

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val mentorsList by mentorViewModel.mentors.observeAsState(initial = emptyList())
    val mentorForVideo = mentorsList!!.find { it.email.equals(video?.mentor_email, ignoreCase = true) }

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewStars by remember { mutableStateOf(5) }
    var isPostingReview by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    // Nombre de vues récupéré depuis la liste des progresses
    val viewCount = video?.progresses?.size ?: 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Lecture vidéo", 
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "Retour", 
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            },
            floatingActionButton = {
                val userHasReviewed = reviews.any { it.menteeEmail.equals(UserEmail, ignoreCase = true) }
                AnimatedVisibility(
                    visible = !userHasReviewed,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleIn(targetScale = 0.8f) + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showReviewDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = "Ajouter un avis",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Donner un avis",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        ) { padding ->
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    isRefreshing = true
                    videoViewModel.fetchVideoById(videoId)
                    reviewsViewModel.fetchReviews(videoId)
                    coroutineScope.launch {
                        delay(1000)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AnimatedVisibility(
                        visible = isPostingReview,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Transparent
                            )
                        }
                    }
                    
                    if (video == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            FullScreenLoadingAnimation()
                        }
                        Log.d("VideoDetailScreen", "Aucune vidéo trouvée pour l'ID: $videoId")
                    } else {
                        // Affichage du titre avec possibilité de voir plus
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            ExpandableTitle2(
                                title = video!!.title,
                                maxLength = 30,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Lecteur vidéo avec design amélioré
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                if (!isPlaying) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("https://img.youtube.com/vi/${video!!.youtube_url}/hqdefault.jpg")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Miniature de la vidéo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp))
                                    ) {
                                        when (painter.state) {
                                            is AsyncImagePainter.State.Loading -> {
                                                LottieAnimation(
                                                    composition = rememberLottieComposition(
                                                        LottieCompositionSpec.RawRes(R.raw.imageloading)
                                                    ).value,
                                                    iterations = LottieConstants.IterateForever,
                                                    modifier = Modifier.size(90.dp)
                                                )
                                            }
                                            else -> SubcomposeAsyncImageContent()
                                        }
                                    }
                                    
                                    // Overlay gradient
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.3f)
                                                    )
                                                )
                                            )
                                    )
                                    
                                    // Bouton play amélioré
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(80.dp)
                                            .shadow(8.dp, CircleShape),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        onClick = {
                                            isPlaying = true
                                            progressViewModel.trackProgress(videoId, UserEmail)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                    
                                    // Badge nombre de vues
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Black.copy(alpha = 0.7f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "Vues",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$viewCount",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    YoutubeViewerComponent(videoId = video!!.youtube_url)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Informations vidéo dans une carte
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (averageRating > 0f) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Note:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Rating(rating = averageRating)
                                        Text(
                                            text = "${"%.1f".format(averageRating)}/5",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                // Tags informatifs
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VideoLibrary,
                                                contentDescription = "Cours",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Cours ${video!!.order}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = "Catégorie",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = video!!.category,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Section des avis
                        Text(
                            text = "Avis des utilisateurs",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        if (reviews.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Aucun avis pour le moment. Soyez le premier à donner votre avis !",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(reviews) { index, review ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = slideInVertically(
                                            animationSpec = tween(300, delayMillis = index * 50),
                                            initialOffsetY = { it / 4 }
                                        ) + fadeIn(animationSpec = tween(300, delayMillis = index * 50))
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Rating(rating = review.stars.toFloat())
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = review.menteeEmail.substringBefore("@"),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                                review.comment?.let { comment ->
                                                    Text(
                                                        text = comment,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        lineHeight = 20.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Lien YouTube
                        val fullUrl = "https://www.youtube.com/watch?v=${video!!.youtube_url}"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            OutlinedTextField(
                                value = fullUrl,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Lien YouTube") },
                                trailingIcon = {
                                    var showCopyAnimation by remember { mutableStateOf(false) }
                                    val composition by rememberLottieComposition(
                                        LottieCompositionSpec.RawRes(R.raw.copy_animation)
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(fullUrl))
                                            showCopyAnimation = true
                                        }
                                    ) {
                                        if (showCopyAnimation) {
                                            LottieAnimation(
                                                composition = composition,
                                                iterations = 1,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            LaunchedEffect(showCopyAnimation) {
                                                delay(2000)
                                                showCopyAnimation = false
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copier le lien",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Informations mentor
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mentor:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                if (mentorForVideo != null) {
                                    Card(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .shadow(2.dp, CircleShape),
                                        shape = CircleShape
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(mentorForVideo.profileUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Image du mentor",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            when (painter.state) {
                                                is AsyncImagePainter.State.Loading -> {
                                                    LottieAnimation(
                                                        composition = rememberLottieComposition(
                                                            LottieCompositionSpec.RawRes(R.raw.imageloading)
                                                        ).value,
                                                        iterations = LottieConstants.IterateForever,
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                }
                                                else -> SubcomposeAsyncImageContent()
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = video!!.mentor_email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp)) // Espace pour le FAB
                    }
                }
            }
        }
    }

    // Dialog d'avis amélioré
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { 
                Text(
                    "Donner votre avis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            label = { Text("Votre commentaire") },
                            placeholder = { Text("Partagez votre expérience...") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Column {
                            Text(
                                text = "Note:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            RatingBarInput(
                                rating = reviewStars.toFloat(),
                                onRatingChanged = { newRating -> reviewStars = newRating.toInt() }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        video?.let {
                            isPostingReview = true
                            val reviewCreate = ReviewCreate(
                                videoId = it.id,
                                menteeEmail = UserEmail,
                                stars = reviewStars,
                                comment = reviewComment
                            )
                            reviewsViewModel.sendReview(reviewCreate)
                            isPostingReview = false
                        }
                        showReviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Publier l'avis",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReviewDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Annuler")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.shadow(12.dp, RoundedCornerShape(20.dp))
        )
    }
}
                            if (mentorForVideo != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(mentorForVideo.profileUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image du mentor",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                ) {
                                    when (painter.state) {
                                        is AsyncImagePainter.State.Loading -> {
                                            LottieAnimation(
                                                composition = rememberLottieComposition(
                                                    LottieCompositionSpec.RawRes(R.raw.imageloading)
                                                ).value,
                                                iterations = LottieConstants.IterateForever,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                        else -> SubcomposeAsyncImageContent()
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "Mentor : ${video!!.mentor_email}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Laisser un avis") },
            text = {
                Column {
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Votre commentaire") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RatingBarInput(
                        rating = reviewStars.toFloat(),
                        onRatingChanged = { newRating -> reviewStars = newRating.toInt() }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        video?.let {
                            isPostingReview = true
                            val reviewCreate = ReviewCreate(
                                videoId = it.id,
                                menteeEmail = UserEmail,
                                stars = reviewStars,
                                comment = reviewComment
                            )
                            reviewsViewModel.sendReview(reviewCreate)
                            isPostingReview = false
                        }
                        showReviewDialog = false
                    }
                ) {
                    Text("Envoyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
