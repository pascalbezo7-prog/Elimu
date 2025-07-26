package com.kotlingdgocucb.elimuApp.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kotlingdgocucb.elimuApp.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthentificationScreen(
    onSignInClicked: () -> Unit,
    navController: NavController
) {
    // Variable d'état pour simuler le chargement
    var loading by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Seuil pour distinguer tablette (>600.dp) et téléphone
        val isTablet = maxWidth > 600.dp

        // Définition des animations (offset, alpha)
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

        // Dimensions et styles adaptatifs
        val titleFontSize = if (isTablet) 32.sp else 28.sp
        val subtitleFontSize = if (isTablet) 18.sp else 16.sp
        val buttonHeight = if (isTablet) 60.dp else 50.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ELIMU",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        )
                    }
                )
            }
        ) { paddingValues ->
            if (isTablet) {
                // Mise en page tablette : animation à gauche, texte et bouton à droite
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animation Lottie à gauche
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimationView(
                                animationFile = R.raw.splash_animation, // Remplacez par votre animation Lottie
                                animationOffset = imageOffset,
                                animationAlpha = alphaValue
                            )
                        }
                        // Texte et bouton à droite
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bienvenue dans Elimu",
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Apprenez avec votre mentor et explorez l'IA !",
                                fontSize = subtitleFontSize,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                )
                            )
                            Spacer(modifier = Modifier.height(30.dp))
                            Button(
                                onClick = {
                                    loading = true // Simule le démarrage du chargement
                                    onSignInClicked()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(buttonHeight)
                                    .graphicsLayer(
                                        translationY = textOffset,
                                        alpha = alphaValue
                                    )
                            ) {
                                if (loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Text("Se connecter avec Google", color = Color.White)
                                }
                            }
                        }
                    }
                }
            } else {
                // Mise en page téléphone : tout en colonne
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimationView(
                                animationFile = R.raw.splash_animation, // Remplacez par votre animation Lottie
                                animationOffset = imageOffset,
                                animationAlpha = alphaValue
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Bienvenue dans Elimu",
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Apprenez avec votre mentor et explorez l'IA !",
                            fontSize = subtitleFontSize,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer(
                                translationY = textOffset,
                                alpha = alphaValue
                            )
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Button(
                            onClick = {
                                loading = true // Simule le démarrage du chargement
                                onSignInClicked()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                )
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Icône Google",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Se connecter avec Google", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LottieAnimationView(
    animationFile: Int,
    animationOffset: Float,
    animationAlpha: Float
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationFile))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .size(250.dp)
            .graphicsLayer(translationY = animationOffset)
            .alpha(animationAlpha)
    )
}
