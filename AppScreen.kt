package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.sp
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    // Photo de profil avec animation
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = scaleIn(animationSpec = spring()) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .shadow(4.dp, CircleShape),
                                            shape = CircleShape,
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
                                                    .fillMaxSize()
                                                    .graphicsLayer(
                                                        translationY = textOffset,
                                                        alpha = alphaValue
                                                    )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    // Message de bienvenue amélioré
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Bonjour 👋",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.graphicsLayer(
                                                translationY = textOffset,
                                                alpha = alphaValue
                                            )
                                        )
                                        TypewriterText(
                                            text = userInfo?.name ?: "Invité",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            ),
                                            modifier = Modifier.graphicsLayer(
                                                translationY = textOffset,
                                                alpha = alphaValue
                                            )
                                        )
                                    }
                                    // Bouton notification amélioré
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = scaleIn(animationSpec = spring()) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .shadow(3.dp, CircleShape),
                                            shape = CircleShape,
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (notificationsCount > 0) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            onClick = { navController.navigate("notifications") }
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                BadgedBox(
                                                    badge = {
                                                        if (notificationsCount > 0) {
                                                            Badge(
                                                                containerColor = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.shadow(2.dp, CircleShape)
                                                            ) {
                                                                Text(
                                                                    text = notificationsCount.toString(),
                                                                    color = MaterialTheme.colorScheme.onError,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Notifications,
                                                        contentDescription = "Notifications",
                                                        tint = if (notificationsCount > 0) 
                                                            MaterialTheme.colorScheme.primary 
                                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
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
                                                Icon(
                                                    it.icon, 
                                                    contentDescription = it.contentDescription, 
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            },
                                            label = { 
                                                Text(
                                                    it.label, 
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (it == currentDestination) FontWeight.Bold else FontWeight.Normal
                                                ) 
                                            },
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
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // En-tête profil avec dégradé horizontal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(6.dp, CircleShape),
                        shape = CircleShape
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
                                .fillMaxSize()
                                .graphicsLayer(
                                    translationY = textOffset,
                                    alpha = alphaValue
                                )
                        )
                    }
                    Text(
                        text = userInfo?.name ?: "Nom utilisateur",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = userInfo?.email ?: "email@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = userInfo?.track ?: "Track",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = userInfo?.mentor_name?.take(10) ?: "Mentor",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Divider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Items du menu avec style amélioré
        val menuItems = listOf(
            Triple(Icons.Default.Feedback, "Feedbacks") { navController.navigate("feedback") },
            Triple(Icons.Default.Description, "Terms & Conditions") { navController.navigate("terms") },
            Triple(Icons.Default.Info, "À propos") { navController.navigate("about") },
            Triple(Icons.Default.ExitToApp, "Se déconnecter") { onLogoutClicked() }
        )
        
        menuItems.forEach { (icon, label, onClick) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                onClick = onClick
            ) {
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            icon, 
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    label = { 
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    selected = false,
                    onClick = onClick,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            }
        }
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
