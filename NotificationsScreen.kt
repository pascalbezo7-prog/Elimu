package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// Modèle de notification avec image optionnelle
data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val imageUrl: String? = null // URL de l'image de la vidéo (si applicable)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    // Exemple de liste de notifications dynamique
    val notifications = remember {
        listOf(
            NotificationItem(
                id = 1,
                title = "Nouvelle vidéo",
                message = "Une nouvelle vidéo a été ajoutée par votre mentor.",
                timestamp = "Il y a 5 minutes",
                imageUrl = "https://img.youtube.com/vi/flp_BeuAKc0/hqdefault.jpg" // Exemple d'image
            ),
            NotificationItem(
                id = 2,
                title = "Mise à jour",
                message = "Votre profil a été mis à jour avec succès.",
                timestamp = "Il y a 1 heure"
            ),
            NotificationItem(
                id = 3,
                title = "Invitation",
                message = "Vous avez reçu une invitation à rejoindre un nouveau groupe.",
                timestamp = "Aujourd'hui"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
        content = { paddingValues ->
            if (notifications.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications) { notification ->
                        NotificationCard(notification = notification)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune notification pour le moment.")
                }
            }
        }
    )
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Si l'image est fournie, l'afficher en haut
            notification.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Image associée à la notification",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
