package com.kotlingdgocucb.elimuApp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kotlingdgocucb.elimuApp.ui.viewmodel.AuthentificationViewModel
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.kotlingdgocucb.elimuApp.domain.model.User
import com.kotlingdgocucb.elimuApp.ui.screens.introScreen.OnboardingScreen
import com.kotlingdgocucb.elimuApp.ui.theme.ElimuTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

// Définition des routes
const val OnboardingRoute = "onboarding"
const val LoaddingRoute = "loading"
const val AuthentificationRoute = "authentification"
const val ScreenTrackRoute = "screen_track"
const val ChooseMentorRoute = "chooseMentor"
const val ConfirmProfileRoute = "ConfirmProfileScreen"
const val AppScreenRoute = "appScreen"
const val VideosRoute = "videos"

// Nouvelles routes pour les items du Drawer
const val FeedbackRoute = "feedback"
const val TermsRoute = "terms"
const val AboutRoute = "about"
const val NotificationsRoute = "notifications"
const val ProfileRoute = "profile"
const val ScreenVideoPopulareRoute = "screenVideoPopulare"
const val ScreenVideoTrackRoute = "screenVideoTrack/{track}"
const val VideoDetailRoute = "videoDetail/{videoId}"

// Nouvelles routes pour les contacts
const val ContactListRoute = "contactList"
const val MentorScreenRoute = "mentorScreen"
const val IAScreenRoute = "iaScreen"


class MainActivity : ComponentActivity() {

    private val credentialManager: CredentialManager by inject()
    private val request: GetCredentialRequest by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        setContent {

            val viewModel: AuthentificationViewModel by viewModel()
            val isLoading = viewModel.isLoading.collectAsStateWithLifecycle()
            val currentUser = viewModel.currentUser.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            // Maintient l'écran de démarrage tant que le chargement est actif
            splashScreen.setKeepOnScreenCondition { isLoading.value }

            ElimuTheme {
                FirebaseApp.initializeApp(this)

                NavHost(
                    navController = navController,
                    startDestination = LoaddingRoute
                ) {
                    // Écran d'onboarding
                    composable(OnboardingRoute) {
                        OnboardingScreen(navController = navController)
                    }
                    // Écran de chargement
                    composable(LoaddingRoute) {
                        LaunchedEffect(isLoading.value) {
                            if (!isLoading.value) {
                                if (currentUser.value != null) {
                                    navController.navigate(ScreenTrackRoute) {
                                        popUpTo(LoaddingRoute) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(OnboardingRoute) {
                                        popUpTo(LoaddingRoute) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    // Écran d'authentification
                    composable(AuthentificationRoute) {
                        AuthentificationScreen(
                            onSignInClicked = {
                                lifecycleScope.launch {
                                    try {
                                        val credentialResponse = credentialManager.getCredential(
                                            request = request,
                                            context = this@MainActivity
                                        )
                                        handleSignIn(
                                            viewModel = viewModel,
                                            response = credentialResponse,
                                            onSignInUser = { user ->
                                                // Dès la connexion réussie, navigation immédiate
                                                if (user != null) {
                                                    navController.navigate(ScreenTrackRoute) {

                                                    }
                                                } else {
                                                    // Gérer l'erreur de connexion si nécessaire
                                                }
                                            }
                                        )
                                    } catch (e: GetCredentialCancellationException) {
                                        Log.e(
                                            "ElIMUDEBUG",
                                            "Connexion annulée par l'utilisateur",
                                            e
                                        )
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Connexion annulée",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate(AuthentificationRoute) {
                                            popUpTo(AuthentificationRoute) { inclusive = true }

                                        }
                                    } catch (e: Exception) {
                                        Log.e("ElIMUDEBUG", "Erreur lors de la connexion", e)
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Erreur lors de la connexion",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            navController = navController
                        )
                    }

                    // Écran principal pour la sélection de track
                    composable(ScreenTrackRoute) {
                        LaunchedEffect(currentUser.value) {
                            if (currentUser.value == null) {
                                navController.navigate(AuthentificationRoute) {

                                    popUpTo(ScreenTrackRoute) { inclusive = true }
                                }
                            }
                        }
                        AdaptiveProfileScreen(
                            currentUser = currentUser.value,
                            onNext = { selectedTrack ->
                                val updatedUser = currentUser.value?.copy(track = selectedTrack)
                                viewModel.login(updatedUser)
                                navController.navigate(ChooseMentorRoute)
                            }
                        )
                    }
                    // Sélection de mentor
                    composable(ChooseMentorRoute) {
                        ChooseMentorScreen(
                            selectedTrack = currentUser.value?.track ?: "",
                            onMentorChosen = { mentor ->
                                val updatedUser = currentUser.value?.copy(
                                    mentor_name = mentor.name,
                                    mentor_email = mentor.email,
                                    mentor_experience = mentor.experience,
                                    mentor_description = mentor.description,
                                    mentor_xUrl = mentor.xUrl,
                                    mentor_githubUrl = mentor.githubUrl,
                                    mentor_profileUrl = mentor.profileUrl,
                                    mentor_linkedinUrl = mentor.linkedinUrl,
                                    mentor_instagramUrl = mentor.instagramUrl
                                )
                                viewModel.login(updatedUser)

                                Log.d("currentUser", "${currentUser}")
                                navController.navigate(ConfirmProfileRoute)
                            },
                            onBack = { navController.popBackStack() },
                            navController = navController
                        )
                    }
                    // Confirmation de profil
                    composable(ConfirmProfileRoute) {
                        ConfirmProfileScreen(
                            userInfo = currentUser.value,
                            onConfirm = {
                                viewModel.createUser(currentUser.value)
                                navController.navigate(AppScreenRoute) {

                                }
                            },
                            onBack = { },
                            navController = navController
                        )
                    }
                    // Écran principal de l'application
                    composable(AppScreenRoute) {
                        LaunchedEffect(currentUser.value) {
                            if (currentUser.value == null) {
                                navController.navigate(AuthentificationRoute) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }



                        AppScreen(
                            userInfo = currentUser.value,
                            notificationsCount = 3, // Exemple
                            onSigninOutClicked = { viewModel.logout() },
                            navController = navController,

                            )

                    }
                    // Sous-graphe pour les vidéos
                    navigation(startDestination = "videoList", route = VideosRoute) {
                        composable("videoList") {
                            CourseScreen(
                                navController = navController,
                                userInfo = currentUser.value
                            )
                        }
                        composable(
                            route = VideoDetailRoute,
                            arguments = listOf(navArgument("videoId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val videoId = backStackEntry.arguments?.getInt("videoId") ?: 0
                            VideoDetailScreen(
                                videoId = videoId,
                                navController = navController,
                                UserEmail = currentUser.value!!.email
                            )
                        }
                        // Nouvelle route pour l'écran des vidéos populaires
                        composable(ScreenVideoPopulareRoute) {
                            ScreenVideoPopulare(navController = navController)
                        }
                        // Nouvelle route pour l'écran des vidéos par track
                        composable(
                            ScreenVideoTrackRoute,
                            arguments = listOf(navArgument("track") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val track = backStackEntry.arguments?.getString("track") ?: ""
                            ScreenVideoTrack(track = track, navController = navController)
                        }
                    }
                    // Destination Notifications (accessible depuis le TopAppBar ou le Drawer)
                    composable(NotificationsRoute) {
                        NotificationsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    // Destination Profil (accessible depuis le TopAppBar ou le Drawer)
                    composable(ProfileRoute) {
                        ProfileScreen(
                            userInfo = currentUser.value,
                            onUpdateProfile = { updatedUser ->
                                viewModel.login(updatedUser)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    // Nouvelles destinations pour les items du Drawer
                    composable(FeedbackRoute) {
                        // Implémentez votre écran Feedback ici
                        FeedbackScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(TermsRoute) {
                        // Implémentez votre écran Terms & Conditions ici
                        TermsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(AboutRoute) {
                        // Implémentez votre écran À propos ici
                        AboutScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }// Nouvelles routes pour les contacts
                    composable(ContactListRoute) {
                        ContactListScreen(
                            navController = navController,
                            user = currentUser.value
                        )
                    }
                    composable(MentorScreenRoute) {
                        MentorScreen(navController = navController, mentor = currentUser.value)
                    }
                    composable(IAScreenRoute) {
                        IAScreen(navController = navController)

                    }
                }
            }
        }
    }

    private fun handleSignIn(
        viewModel: AuthentificationViewModel,
        response: GetCredentialResponse,
        onSignInUser: (User?) -> Unit
    ) {
        val credential = response.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        viewModel.firebaseSignInWithGoogle(idToken) { success, errorMsg, user ->
                            if (success) {
                                onSignInUser(user)
                            } else {
                                onSignInUser(null)
                                Log.e("ElIMUDEBUG", "Erreur Firebase : $errorMsg")
                            }
                        }
                    } catch (e: Exception) {
                        onSignInUser(null)
                        Log.e("ElIMUDEBUG", "Erreur du token Google dans la réponse", e)
                    }
                } else {
                    onSignInUser(null)
                    Log.e("ElIMUDEBUG", "Type de credential inconnu")
                }
            }

            else -> {
                onSignInUser(null)
                Log.e("ElIMUDEBUG", "Type de credential non supporté")
            }
        }
    }


}
