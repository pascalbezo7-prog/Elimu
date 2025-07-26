package com.kotlingdgocucb.elimuApp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.Mentor
import com.kotlingdgocucb.elimuApp.ui.viewmodel.MentorViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@SuppressLint("ResourceType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseMentorScreen(
    onMentorChosen: (Mentor) -> Unit,  // Callback pour récupérer le mentor choisi
    onBack: () -> Unit,                // Callback pour gérer le bouton retour
    selectedTrack: String,
    navController: NavController
) {
    // Propriétés d'animation : translation verticale et opacité
    var startAnimation by remember { mutableStateOf(false) }
    val textOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -50f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val alphaValue by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )
    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
    }

    // État pour le mentor sélectionné (pour le bouton "Suivant")
    var chosenMentor by remember { mutableStateOf<Mentor?>(null) }
    // État pour le mentor dont on souhaite afficher le détail (dialog)
    var mentorForDialog by remember { mutableStateOf<Mentor?>(null) }

    // Récupération des mentors via le ViewModel
    val mentorViewModel: MentorViewModel = koinViewModel()
    val mentorsState by mentorViewModel.mentors.observeAsState(initial = null)

    // État pour gérer si la connexion est en échec
    var isConnectionError by remember { mutableStateOf(false) }
    // Pour éviter d'envoyer la notification plusieurs fois
    var notificationSent by remember { mutableStateOf(false) }

    // On attend quelques secondes : si toujours pas de data => pas de connexion
    LaunchedEffect(Unit) {
        delay(5000) // Par exemple 5 secondes
        if (mentorsState == null) {
            isConnectionError = true
        }
    }

    val context = LocalContext.current
    // Envoi de la notification dès que l'erreur de connexion est détectée
    if (isConnectionError && !notificationSent) {
        LaunchedEffect(isConnectionError) {
            sendConnectionErrorNotification(context)
            notificationSent = true
        }
    }

    // Si on n'a pas encore de données, on affiche soit le loading, soit l'animation « no connection »
    if (mentorsState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (!isConnectionError) {
                // Animation de chargement
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.loading)
                )
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(150.dp)
                )
            } else {
                // On peut afficher ici une animation ou un indicateur visuel si besoin
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.no_connection)
                )
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
        return
    }

    val allMentors = mentorsState ?: emptyList<Mentor>()
    // Filtrage des mentors en fonction du track choisi
    val mentorsForTrack = allMentors.filter { it.tack == selectedTrack }

    // Détection du type d'appareil (téléphone vs tablette)
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Créer un profil ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (mentorsForTrack.isEmpty()) {
            // Si aucun mentor n'est trouvé pour ce track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucun mentor trouvé pour ce track.")
            }
        } else {
            if (!isTablet) {
                // Disposition pour téléphone : mentors groupés par lignes de deux colonnes maximum
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choisissez votre mentor :",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        // Utilisation de chunked pour regrouper les mentors par paires
                        mentorsForTrack.chunked(2).forEach { mentorRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                mentorRow.forEach { mentor ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.graphicsLayer(
                                            translationY = textOffset,
                                            alpha = alphaValue
                                        )
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(mentor.profileUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Photo du mentor",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .clickable { chosenMentor = mentor }
                                                .let { modifier ->
                                                    if (chosenMentor == mentor) {
                                                        modifier.border(
                                                            BorderStroke(5.dp, MaterialTheme.colorScheme.primary),
                                                            CircleShape
                                                        )
                                                    } else modifier
                                                }
                                        ) {
                                            if (painter.state is AsyncImagePainter.State.Loading) {
                                                LottieAnimationExample()
                                            } else {
                                                SubcomposeAsyncImageContent()
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = mentor.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        TextButton(onClick = { mentorForDialog = mentor }) {
                                            Text("Voir plus")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                    // Zone d'actions ancrée en bas
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { chosenMentor?.let { onMentorChosen(it) } },
                            enabled = (chosenMentor != null),
                            modifier = Modifier.fillMaxWidth(0.6f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(text = "Suivant", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Termes & conditions",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { /* Ouvrir les conditions */ }
                                .graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                )
                        )
                    }
                }
            } else {
                // Disposition pour tablette : agencement en deux colonnes
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choisissez votre mentor :",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            mentorsForTrack.forEach { mentor ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.graphicsLayer(
                                        translationY = textOffset,
                                        alpha = alphaValue
                                    )
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(mentor.profileUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Photo du mentor",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .clickable { chosenMentor = mentor }
                                            .let { modifier ->
                                                if (chosenMentor == mentor) {
                                                    modifier.border(
                                                        BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                                                        CircleShape
                                                    )
                                                } else modifier
                                            }
                                    ) {
                                        if (painter.state is AsyncImagePainter.State.Loading) {
                                            LottieAnimationExample()
                                        } else {
                                            SubcomposeAsyncImageContent()
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = mentor.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    TextButton(onClick = { mentorForDialog = mentor }) {
                                        Text("Voir plus")
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { chosenMentor?.let { onMentorChosen(it) } },
                            enabled = (chosenMentor != null),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(text = "Suivant", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Termes & conditions",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { navController.navigate("terms") }
                                .graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                )
                        )
                    }
                }
            }
        }

        mentorForDialog?.let { mentor ->
            AlertDialog(
                onDismissRequest = { mentorForDialog = null },
                title = {
                    Text(
                        text = mentor.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mentor.profileUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo du mentor",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .clickable { chosenMentor = mentor }
                                .let { modifier ->
                                    if (chosenMentor == mentor) {
                                        modifier.border(
                                            BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                                            CircleShape
                                        )
                                    } else modifier
                                }
                        ) {
                            if (painter.state is AsyncImagePainter.State.Loading) {
                                LottieAnimationExample()
                            } else {
                                SubcomposeAsyncImageContent()
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Années d'expérience : ${mentor.experience}",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mentor.description,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SocialIcon(
                                iconRes = R.drawable.github,
                                url = mentor.githubUrl,
                                contentDescription = "GitHub"
                            )
                            SocialIcon(
                                iconRes = R.drawable.linkedin,
                                url = mentor.linkedinUrl,
                                contentDescription = "LinkedIn"
                            )
                            SocialIcon(
                                iconRes = R.drawable.twitter,
                                url = mentor.xUrl,
                                contentDescription = "X"
                            )
                            SocialIcon(
                                iconRes = R.drawable.instagram,
                                url = mentor.instagramUrl,
                                contentDescription = "Instagram"
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { mentorForDialog = null }) {
                        Text("Fermer")
                    }
                }
            )
        }
    }
}

@Composable
fun SocialIcon(iconRes: Int, url: String, contentDescription: String) {
    val context = LocalContext.current
    IconButton(onClick = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LottieAnimationExample() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.imageloading)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(90.dp)
    )
}

/**
 * Fonction utilitaire pour envoyer une notification indiquant qu'il est impossible de charger
 * en raison d'un problème de connexion.
 */
fun sendConnectionErrorNotification(context: Context) {
    val channelId = "connection_error_channel"
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Erreur de connexion",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications indiquant un problème de connexion"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ico_error) // Assurez-vous d'avoir une icône d'erreur dans vos ressources
        .setContentTitle("Impossible de charger")
        .setContentText("Vérifiez votre connexion internet.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    notificationManager.notify(1, notification)
}
