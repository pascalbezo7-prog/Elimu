package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.domain.model.User
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmProfileScreen(
    userInfo: User?,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    navController : NavController
) {
    // Détection de la largeur d'écran (téléphone vs tablette)
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    // Animation : translation et opacité
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

    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Valider vos informations",
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
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        if (!isTablet) {
            // Layout pour téléphone : colonne avec zone scrollable centrée et footer fixe en bas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Contenu principal centré verticalement dans la zone scrollable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Photo de profil
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userInfo?.profile_picture_uri)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.account),
                        contentDescription = "Photo de profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Informations utilisateur
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(translationY = textOffset, alpha = alphaValue)
                    ) {
                        Text("Noms : ${userInfo?.name}", fontSize = 16.sp)
                        Text("Mail : ${userInfo?.email}", fontSize = 16.sp)
                        Text("Track : ${userInfo?.track}", fontSize = 16.sp)
                        Text("Mentor : ${userInfo?.mentor_name}", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Avertissement
                    Text(
                        text = "Ces données seront celles utilisées pour votre profil sur la plateforme",
                        fontSize = 14.sp,
                       // modifier = Modifier.graphicsLayer(translationY = textOffset, alpha = alphaValue),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Footer fixe en bas : bouton "Terminer" et lien "Termes & conditions"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Terminer", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        text = "Termes & conditions",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { navController.navigate("terms") }
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue)
                    )
                }
            }
        } else {
            // Layout pour tablette : disposition en Row avec chaque colonne bien centrée
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colonne gauche : image et informations, centrée verticalement
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userInfo?.profile_picture_uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo du mentor",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue)
                    ) {
                        if (painter.state is AsyncImagePainter.State.Loading) {
                            LottieAnimationExample()
                        } else {
                            SubcomposeAsyncImageContent()
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(translationY = textOffset, alpha = alphaValue)
                    ) {
                        Text("Noms : ${userInfo?.name}", fontSize = 18.sp)
                        Text("Courriel : ${userInfo?.email}", fontSize = 18.sp)
                        Text("Track : ${userInfo?.track}", fontSize = 18.sp)
                        Text("Mentor : ${userInfo?.mentor_name}", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ces données seront celles utilisées pour votre profil sur la plateforme",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                // Colonne droite : footer centré en bas
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Terminer", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Termes & conditions",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { /* Ouvrir ou afficher les CGU */ }
                            .graphicsLayer(translationY = textOffset, alpha = alphaValue)
                    )
                }
            }
        }
    }
}

