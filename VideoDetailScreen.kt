package com.kotlingdgocucb.elimuApp.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
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
                    title = { Text("Play", color = MaterialTheme.colorScheme.onBackground) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            },
            floatingActionButton = {
                val userHasReviewed = reviews.any { it.menteeEmail.equals(UserEmail, ignoreCase = true) }
                if (!userHasReviewed) {
                    FloatingActionButton(
                        onClick = { showReviewDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("Review")
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
                        .padding(16.dp)
                ) {
                    if (isPostingReview) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                        ExpandableTitle2(
                            title = video!!.title,
                            maxLength = 20,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            if (!isPlaying) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("https://img.youtube.com/vi/${video!!.youtube_url}/hqdefault.jpg")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Miniature de la vidéo",
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
                                                modifier = Modifier.size(90.dp)
                                            )
                                        }
                                        else -> SubcomposeAsyncImageContent()
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        isPlaying = true
                                        progressViewModel.trackProgress(videoId, UserEmail)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.play),
                                        contentDescription = "Play",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                // Affichage du nombre de vues dans un Box en haut à droite
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                                        .padding(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Vues",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$viewCount",
                                            color = MaterialTheme.colorScheme.onBackground,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            } else {
                                YoutubeViewerComponent(videoId = video!!.youtube_url)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (averageRating > 0f) {
                            Text(
                                text = "Note moyenne : ${"%.1f".format(averageRating)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Rating(rating = averageRating)
                            // Affichage du nombre de vues et du cours numéro avec catégorie
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Vues",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$viewCount vues",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Cours numéro",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cours numéro : ${video!!.order}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = "Catégorie",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = video!!.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Avis des utilisateurs :",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (reviews.isEmpty()) {
                            Text("Aucun avis pour le moment.", color = MaterialTheme.colorScheme.onBackground)
                        } else {
                            LazyColumn (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(reviews) { review ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Rating(rating = review.stars.toFloat())
                                            Spacer(modifier = Modifier.height(8.dp))
                                            review.comment?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Par : ${review.menteeEmail}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val fullUrl = "https://www.youtube.com/watch?v=${video!!.youtube_url}"
                        OutlinedTextField(
                            value = fullUrl,
                            onValueChange = {},
                            readOnly = true,
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
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
