package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.kotlingdgocucb.elimuApp.R
import com.kotlingdgocucb.elimuApp.domain.model.User
import com.kotlingdgocucb.elimuApp.domain.utils.AppDestinations
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(

    userInfo: User?,
    notificationsCount: Int, // Nombre de notifications
    onSigninOutClicked: () -> Unit,
    navController: NavController
) {
    // Animation d'entrée pour le TopAppBar
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

    // Gestion du drawer et de la navigation
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.Accueil) }

    // État pour afficher la boîte de dialogue de déconnexion
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerContent = {
                ModernDrawerContent(
                    userInfo = userInfo,
                    textOffset = textOffset,
                    alphaValue = alphaValue,
                    navController = navController,
                    onDestinationClicked = { destination ->
                        coroutineScope.launch { drawerState.close() }
                        currentDestination = destination
                        navController.navigate(destination.route)
                    },
                    onLogoutClicked = { showLogoutDialog = true },
                    onSigninOutClicked = onSigninOutClicked
                )
            },
            drawerState = drawerState
        ) {
            Scaffold(

                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Photo de profil : clic pour accéder à la page de modification du profil
                                IconButton(
                                    onClick = { navController.navigate("profile") }
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(userInfo?.profile_picture_uri)
                                            .crossfade(true)
                                            .build(),
                                        placeholder = painterResource(R.drawable.account),
                                        contentDescription = "Photo de profil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .graphicsLayer(
                                                translationY = textOffset,
                                                alpha = alphaValue
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Message de bienvenue
                                TypewriterText(
                                    text = "Bonjour ${userInfo?.name ?: "Invité"}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.graphicsLayer(
                                        translationY = textOffset,
                                        alpha = alphaValue
                                    )
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                // Bouton notification avec badge
                                IconButton(
                                    onClick = { navController.navigate("notifications") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (notificationsCount > 0) {
                                                Badge(
                                                    containerColor = Color.Red
                                                ) {
                                                    Text(
                                                        text = notificationsCount.toString(),
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,

                        )
                    )
                },
                content = { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                    ) {
                        Column {
                            NavigationSuiteScaffold(
                                modifier = Modifier.height(900.dp),
                                navigationSuiteItems = {
                                    AppDestinations.entries.forEach {
                                        item(
                                            icon = {
                                                Icon(it.icon, contentDescription = it.contentDescription, modifier = Modifier.size(20.dp))
                                            },
                                            label = { Text(it.label, style = MaterialTheme.typography.labelSmall) },
                                            selected = it == currentDestination,
                                            onClick = {
                                                currentDestination = it
                                            }
                                        )
                                    }
                                }
                            ) {
                                when (currentDestination) {
                                    AppDestinations.Accueil -> CourseScreen(
                                        navController = navController,
                                        userInfo = userInfo
                                    )
                                    AppDestinations.Message -> MessageScreen(
                                        navController = navController,
                                        user = userInfo)
                                }
                            }
                        }
                    }
                }
            )
        }

        // Boîte de dialogue de confirmation de déconnexion
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = onSigninOutClicked
            )
        }
    }
}

/**
 * Contenu du drawer moderne avec le menu.
 *
 * @param onDestinationClicked Fonction appelée lors du clic sur un item du drawer
 *        pour la navigation via la BottomNavigationSuite.
 */
@Composable
fun ModernDrawerContent(
    userInfo: User?,
    textOffset: Float,
    alphaValue: Float,
    navController: NavController,
    onDestinationClicked: (AppDestinations) -> Unit,
    onSigninOutClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.8f),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        drawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // En-tête profil avec dégradé horizontal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userInfo?.profile_picture_uri)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.account),
                    contentDescription = "Photo de profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .graphicsLayer(
                            translationY = textOffset,
                            alpha = alphaValue
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userInfo?.name ?: "Nom utilisateur",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = userInfo?.email ?: "email@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Track : ${userInfo?.track ?: "Non défini"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Mentor : ${userInfo?.mentor_name ?: "Non défini"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        // Exemple d'item : Feedbacks
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Feedback, contentDescription = "Feedbacks") },
            label = { Text("Feedbacks") },
            selected = false,
            onClick = {
                navController.navigate("feedback")
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        // Exemple d'item : Terms & Conditions
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Description, contentDescription = "Terms & Conditions") },
            label = { Text("Terms & Conditions") },
            selected = false,
            onClick = {
                navController.navigate("terms")
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        // Exemple d'item : À propos
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "À propos") },
            label = { Text("À propos") },
            selected = false,
            onClick = {
                navController.navigate("about")
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Se déconnecter") },
            label = { Text("Se déconnecter") },
            selected = false,
            onClick = {
                onLogoutClicked()
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Boîte de dialogue de confirmation de déconnexion avec animation Lottie.
 */
@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(3000)
            onConfirm()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            if (!isLoading) {
                Text("Déconnexion")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isLoading) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.logout_animation)
                    )
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Voulez-vous vous déconnecter ?")
                } else {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.loading)
                    )
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Déconnexion en cours...")
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                TextButton(onClick = { isLoading = true }) {
                    Text("Confirmer")
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
            }
        }
    )
}

/**
 * Effet "machine à écrire" pour un texte.
 */
@Composable
fun TypewriterText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    typeSpeed: Long = 100L,
    waitEnd: Long = 1500L
) {
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        while (true) {
            displayedText = ""
            for (char in text) {
                displayedText += char
                delay(typeSpeed)
            }
            delay(waitEnd)
        }
    }
    Text(
        text = displayedText,
        style = style,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}
