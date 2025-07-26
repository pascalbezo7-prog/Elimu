package com.kotlingdgocucb.elimuApp.ui.screens.introScreen

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.ui.AuthentificationRoute

data class OnboardingPageData(
    val title: String,
    val description: String,
    val buttonText: String,
    val lottieAnimation: Int,
    val onClick: () -> Unit
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalPagerApi::class
)
@Composable
fun OnboardingScreen(navController: NavController) {
    // Définition des trois pages d'onboarding
    val pages = listOf(
        OnboardingPageData(
            title = "Plongez dans un océan de contenus vidéo",
            description = "Accédez à des milliers de vidéos en streaming, où que vous soyez. Découvrez des contenus passionnants, divertissants et informatifs, et profitez d'une expérience de visionnage fluide et personnalisée.",
            buttonText = "Suivant",
            lottieAnimation = R.raw.video_welcome,
            onClick = {} // L'action sera gérée via le pager
        ),
        OnboardingPageData(
            title = "Apprenez et échangez avec votre mentor",
            description = "Connectez-vous avec votre mentor grâce à des vidéos interactives. Partagez vos découvertes, posez vos questions et bénéficiez d'un accompagnement personnalisé.",
            buttonText = "Suivant",
            lottieAnimation = R.raw.learn_animation,
            onClick = {} // L'action sera gérée via le pager
        ),
        OnboardingPageData(
            title = "Explorez les vidéos avec l'IA",
            description = "Interagissez avec l'Intelligence Artificielle pour approfondir votre compréhension des vidéos. L'IA est votre partenaire pour une expérience de visionnage enrichie.",
            buttonText = "Commencer",
            lottieAnimation = R.raw.ai_help,
            onClick = { navController.navigate(AuthentificationRoute) }
        )
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ELIMU",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingTemplateContent(
                    pageData = pages[page],
                    navController = navController
                )
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        pages.last().onClick.invoke()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),

                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Suivant" else "Commencer",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun OnboardingTemplateContent(pageData: OnboardingPageData, navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600

    var startAnimation by remember { mutableStateOf(false) }
    val textOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -200f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    val animationOffset by animateFloatAsState(
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

    if (!isTablet) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            LottieAnimationView(
                animationFile = pageData.lottieAnimation,
                animationOffset = animationOffset,
                animationAlpha = alphaValue
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = pageData.title,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(translationX = textOffset)
                    .alpha(alphaValue)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = pageData.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(translationX = textOffset)
                    .alpha(alphaValue)
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimationView(
                    animationFile = pageData.lottieAnimation,
                    animationOffset = animationOffset,
                    animationAlpha = alphaValue
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = pageData.title,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(translationX = textOffset)
                        .alpha(alphaValue)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = pageData.description,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(translationX = textOffset)
                        .alpha(alphaValue)
                )
            }
        }
    }
}

@Composable
fun LottieAnimationView(animationFile: Int, animationOffset: Float, animationAlpha: Float) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationFile))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever, // Boucle infinie
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
