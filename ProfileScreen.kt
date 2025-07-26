package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.domain.model.User

/**
 * Retourne l'ID de ressource de l'image de couverture en fonction du track.
 */
@Composable
fun getCoverImageForTrack(track: String): Int {
    return when (track.lowercase()) {
        "flutter" -> R.drawable.flutter
        "web" -> R.drawable.web
        "kotlin" -> R.drawable.kotlin
        "ai" -> R.drawable.ai_ml
        "python" -> R.drawable.python
        else -> R.drawable.ai_ml // image par défaut si le track n'est pas reconnu
    }
}

/**
 * Composant d'affichage d'une image en plein écran (ratio 5:3) avec un bouton "Retour".
 */
@Composable
fun FullScreenImage(
    imageData: Any,         // Peut être un Int (R.drawable.xxx) ou une URL
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image centrée, ratio 5:3
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(5f / 3f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageData)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image en plein écran",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bouton de retour (flèche en haut à gauche)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userInfo: User?,
    onUpdateProfile: (User) -> Unit,
    onBack: () -> Unit
) {
    // Variables d'état pour le formulaire
    var name by remember { mutableStateOf(userInfo?.name ?: "") }
    var email by remember { mutableStateOf(userInfo?.email ?: "") }
    var track by remember { mutableStateOf(userInfo?.track ?: "") }
    var mentor by remember { mutableStateOf(userInfo?.mentor_name ?: "") }

    // Variables d'état pour le plein écran
    var showCoverFullScreen by remember { mutableStateOf(false) }
    var showProfileFullScreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        },
        content = { padding ->
            // Contenu principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                // Partie supérieure : Cover + Photo de profil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)  // Hauteur augmentée pour mieux positionner la photo
                ) {
                    val coverImageRes = getCoverImageForTrack(track)
                    Image(
                        painter = painterResource(id = coverImageRes),
                        contentDescription = "Image de couverture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                showCoverFullScreen = true
                            }
                    )

                    // Photo de profil superposée
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomCenter)
                            // Décalage pour que la photo "dépasse" légèrement la cover
                            .offset(y = 60.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                showProfileFullScreen = true
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userInfo?.profile_picture_uri)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.account),
                            contentDescription = "Photo de profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Espace pour compenser la superposition de la photo
                Spacer(modifier = Modifier.height(60.dp))

                // Partie Formulaire / Infos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = track,
                        onValueChange = { track = it },
                        label = { Text("Track") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mentor,
                        onValueChange = { mentor = it },
                        label = { Text("Mentor") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Bouton de sauvegarde
                    Button(
                        onClick = {
                            val updatedUser = userInfo?.copy(
                                name = name,
                                track = track,
                                mentor_name = mentor
                            ) ?: User(
                                name = name,
                                email = email,
                                isLoggedIn = true,
                                profile_picture_uri = "",
                                track = track,
                                id = 0,
                                createdAt = "",
                                mentor_name = "",
                                mentor_email = "",
                                mentor_profileUrl = "",
                                mentor_experience = "",
                                mentor_description = "",
                                mentor_githubUrl = "",
                                mentor_linkedinUrl = "",
                                mentor_xUrl = "",
                                mentor_instagramUrl = ""
                            )
                            onUpdateProfile(updatedUser)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Sauvegarder")
                    }
                }
            }

            // Affichage plein écran (cover ou profil) si demandé
            if (showCoverFullScreen) {
                FullScreenImage(
                    imageData = getCoverImageForTrack(track),
                    onClose = { showCoverFullScreen = false }
                )
            }
            if (showProfileFullScreen) {
                FullScreenImage(
                    imageData = userInfo?.profile_picture_uri ?: R.drawable.account,
                    onClose = { showProfileFullScreen = false }
                )
            }
        }
    )
}
