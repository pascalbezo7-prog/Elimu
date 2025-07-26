package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.domain.model.User
import kotlinx.coroutines.delay

@Composable
fun AdaptiveProfileScreen(currentUser: User?, onNext: (String) -> Unit) {
    val screenWidthDp = LocalContext.current.resources.configuration.screenWidthDp
    if (screenWidthDp < 600) {
        ProfileScreenVertical(currentUser = currentUser, onNext = onNext)
    } else {
        ProfileScreenHorizontal(currentUser = currentUser, onNext = onNext)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenVertical(currentUser: User?, onNext: (String) -> Unit) {
    val navController = rememberNavController()

    // Propriétés d'animation
    var startAnimation by remember { mutableStateOf(false) }
    val textOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -50f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val imageOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    val alphaValue by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )
    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
    }

    // Disposition verticale pour mobile
    var selectedTrack by remember { mutableStateOf("Kotlin") }
    val tracks = listOf("Web", "Flutter", "Kotlin", "Python")
    var expanded by remember { mutableStateOf(false) }
    val trackDetails = mapOf(
        "Kotlin" to Pair(R.drawable.kotlin_logo, "Langage moderne pour Android."),
        "Python" to Pair(R.drawable.python_logot, "Langage polyvalent pour la data."),
        "Web" to Pair(R.drawable.web_logo, "Technologies du développement web."),
        "Flutter" to Pair(R.drawable.flutter_logo, "Framework cross-platform basé sur Dart.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Créer un profil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo de profil
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentUser?.profile_picture_uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo de profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .graphicsLayer(
                        translationX = imageOffset,
                        alpha = alphaValue
                    )
            ) {
                if (painter.state is AsyncImagePainter.State.Loading) {
                    // Par exemple, vous pouvez afficher ici une animation Lottie pendant le chargement
                    LottieAnimationExample()
                } else {
                    // Une fois l'image chargée, affichez-la
                    SubcomposeAsyncImageContent()
                }
            }

            // Nom et email
            Text(
                text = currentUser?.name.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.graphicsLayer(
                    translationY = textOffset,
                    alpha = alphaValue
                )
            )
            Text(
                text = currentUser?.email.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.graphicsLayer(
                    translationY = textOffset,
                    alpha = alphaValue
                )
            )
            // Sélection du track
            Text(
                "Choisissez votre track :",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(
                    translationY = textOffset,
                    alpha = alphaValue
                )
            )
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        ),
                    shape = RectangleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Ouvrir le menu",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = selectedTrack)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    tracks.forEach { track ->
                        DropdownMenuItem(
                            text = { Text(text = track) },
                            onClick = {
                                selectedTrack = track
                                expanded = false
                            }
                        )
                    }
                }
            }
            // Affichage du logo et du détail du track sélectionné
            trackDetails[selectedTrack]?.let { (logoRes, details) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer(
                        translationY = textOffset,
                        alpha = alphaValue
                    )
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Logo du track",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = details, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
            Button(
                onClick = { onNext(selectedTrack) },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .graphicsLayer(
                        translationY = textOffset,
                        alpha = alphaValue
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Suivant", fontSize = 16.sp)
            }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenHorizontal(currentUser: User?, onNext: (String) -> Unit) {
    val navController = rememberNavController()

    // Propriétés d'animation
    var startAnimation by remember { mutableStateOf(false) }
    val textOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -50f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val imageOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    val alphaValue by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )
    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
    }

    // Disposition horizontale pour tablette/desktop :
    var selectedTrack by remember { mutableStateOf("Kotlin") }
    val tracks = listOf("Web", "Flutter", "Kotlin", "Python")
    var expanded by remember { mutableStateOf(false) }
    val trackDetails = mapOf(
        "Kotlin" to Pair(
            R.drawable.kotlin_logo,
            "Langage moderne pour Android. Utilisé avec Jetpack Compose pour créer des applications performantes."
        ),
        "Python" to Pair(
            R.drawable.python_logot,
            "Langage polyvalent pour la data, l'IA et le développement web. Très utilisé en science des données."
        ),
        "Web" to Pair(
            R.drawable.web_logo,
            "Technologies du développement web : HTML, CSS, JavaScript, React, et plus encore."
        ),
        "Flutter" to Pair(
            R.drawable.flutter_logo,
            "Framework cross-platform basé sur Dart, permettant de créer des applications mobiles et web."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Créer un profil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colonne de gauche : image et infos utilisateur
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentUser?.profile_picture_uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo de profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .graphicsLayer(
                                translationX = imageOffset,
                                alpha = alphaValue
                            )
                    ) {
                        if (painter.state is AsyncImagePainter.State.Loading) {
                            // Par exemple, vous pouvez afficher ici une animation Lottie pendant le chargement
                            LottieAnimationExample()
                        } else {
                            // Une fois l'image chargée, affichez-la
                            SubcomposeAsyncImageContent()
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentUser?.name.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                    )
                    Text(
                        text = currentUser?.email ?: "Nom inconnu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                    )
                }
                // Colonne de droite : sélection du track et autres infos
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Choisissez votre track :",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                    )
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                ),
                            shape = RectangleShape
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Ouvrir le menu",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = selectedTrack)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            tracks.forEach { track ->
                                DropdownMenuItem(
                                    text = { Text(text = track) },
                                    onClick = {
                                        selectedTrack = track
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    // Affichage du logo et du détail du track sélectionné
                    trackDetails[selectedTrack]?.let { (logoRes, details) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        ) {
                            Image(
                                painter = painterResource(id = logoRes),
                                contentDescription = "Logo du track",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = details, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onNext(selectedTrack) },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Suivant", fontSize = 16.sp)
                    }
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
        }
    }
}
