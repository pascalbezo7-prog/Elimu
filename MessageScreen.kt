package com.kotlingdgocucb.elimuApp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kotlingdgocucb.elimuApp.domain.model.User

data class Contact(val name: String, val email: String)

@Composable
fun MessageScreen(navController: NavController, user: User?) {
    ContactListScreen(navController = navController, user = user)
}

@Composable
fun ContactListScreen(navController: NavController, user: User?) {
    // Liste de contacts : Mentor et IA
    val contacts = listOf(
        Contact("${user?.mentor_name}", "${user?.mentor_email}"),
        Contact("IA", "ia@example.com")
    )




    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(contacts) { contact ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        if (contact.name == "${user?.mentor_name}")
                            navController.navigate("mentorScreen")
                        else
                            navController.navigate("iaScreen")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                ListItem(
                    leadingContent = {
                        if (contact.name == "Mentor") {
                            AsyncImage(
                                model = user?.mentor_profileUrl,
                                contentDescription = "Image de profil de ${contact.name}",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Image de profil de ${contact.name}",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            contact.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = {
                        Text(
                            contact.email,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}



