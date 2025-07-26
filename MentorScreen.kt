package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kotlingdgocucb.elimuApp.domain.model.User
import kotlinx.coroutines.launch

data class Message(
    val senderName: String,
    val content: String,
    val isMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorScreen(
    navController: NavController,
    mentor: User?
) {
    // Liste mutable de messages pour la démo
    var messages by remember {
        mutableStateOf(
            listOf(
                Message("${mentor?.mentor_name}", "Hey, ça va ?", isMe = false),
                Message("Vous", "Bien, et toi ?", isMe = true),
                Message("${mentor?.mentor_name}", "Je vais bien, merci.", isMe = false),
                Message("${mentor?.mentor_name}", "Bonsoir", isMe = false),
                Message("Vous", "J'ai un problème, tu peux m'aider ?", isMe = true)
            )
        )
    }

    // Pour gérer le scroll automatique vers le dernier message
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MentorTopBar(
                mentor = mentor,
                onInfoClick = { navController.navigate("mentor_info_route") },
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            MessageInputBar(
                onMessageSent = { newMessage ->
                    messages = messages + newMessage
                    scope.launch {
                        // Scroll vers le dernier message
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            reverseLayout = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + expandVertically()
                ) {
                    ChatBubble(message = message)
                }
                // Ajout d'un espace entre les messages
                if (index < messages.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorTopBar(
    mentor: User?,
    onInfoClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var infoClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (infoClicked) 1.3f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        finishedListener = { infoClicked = false }
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar du mentor
                AsyncImage(
                    model = mentor?.profile_picture_uri,
                    contentDescription = "Photo de ${mentor?.mentor_name}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = mentor?.mentor_name ?: "Mentor",
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Icône d'information avec animation
                IconButton(
                    onClick = {
                        infoClicked = true
                        onInfoClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Informations sur le mentor",
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun ChatBubble(message: Message) {
    val bubbleColor = if (message.isMe)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(), // Animation fluide lors des changements de taille
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.padding(4.dp),
            color = bubbleColor,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!message.isMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MessageInputBar(
    onMessageSent: (Message) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Animation de couleur pour le champ de texte selon le focus
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Écrivez un message...") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true,
                interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { interaction ->
                            isFocused = interaction is androidx.compose.foundation.interaction.FocusInteraction.Focus
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (textValue.isNotBlank()) {
                        onMessageSent(Message("Vous", textValue, isMe = true))
                        textValue = ""
                        focusManager.clearFocus() // Permet de masquer le clavier
                    }
                },
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Envoyer")
            }
        }
    }
}
