package com.kotlingdgocucb.elimuApp.ui.viewmodel
import com.kotlingdgocucb.elimuApp.domain.usecase.CreateUserUseCase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlingdgocucb.elimuApp.domain.model.User
import com.kotlingdgocucb.elimuApp.domain.usecase.GetCurrentUserUseCase
import com.kotlingdgocucb.elimuApp.domain.usecase.SetCurrentUserUseCase
import com.kotlingdgocucb.elimuApp.domain.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser

class AuthentificationViewModel(
    private val setCurrentUserUseCase: SetCurrentUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    private var _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val currentUser = _currentUser.asStateFlow()

    private var _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading = _isLoading
        .onStart {
            getCurrentUser()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            true
        )

    fun login(user: User?) {
        viewModelScope.launch {
            Log.d("ElIMUDEBUG", "login(user) appelé avec $user")
            val result = setCurrentUserUseCase(user)
            when(result) {
                is Result.Error -> {
                    Log.d("ELIMUDEBUG", result.message.toString())
                }
                is Result.Loading -> { /* Optionnel */ }
                is Result.Success -> {
                    _currentUser.value = result.data
                    Log.d("ELIMUDEBUG", "Utilisateur mis à jour: ${result.data}")
                }
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        viewModelScope.launch {
            val result = setCurrentUserUseCase(null)
            when (result) {
                is Result.Error -> {
                    Log.d("ELIMUDEBUG", result.message.toString())
                }
                is Result.Loading -> { /* Optionnel */ }
                is Result.Success -> {
                    _currentUser.value = result.data
                }
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            when (result) {
                is Result.Error -> {
                    _isLoading.value = false
                    _currentUser.value = null
                    Log.d("ELIMUDEBUG", result.message.toString())
                }
                is Result.Loading -> { /* Optionnel */ }
                is Result.Success -> {
                    _isLoading.value = false
                    _currentUser.value = result.data
                }
            }
        }
    }

    fun createUser(user: User?) {
        if (user == null) return
        viewModelScope.launch {
            try {
                val createdUser = createUserUseCase(user)
                _currentUser.value = createdUser
            } catch (e: Exception) {
                // Gérer l'erreur (afficher un message, log, etc.)
            }
        }
    }

    // Intégration de l'authentification Firebase dans le ViewModel
    fun firebaseSignInWithGoogle(
        idToken: String,
        onResult: (Boolean, String?, User?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                    val user = firebaseUser?.let {
                        User(
                            name = it.displayName ?: "Pas de nom",
                            email = it.email ?: "Pas de mail",
                            isLoggedIn = true,
                            profile_picture_uri = it.photoUrl?.toString() ?: "",
                            track = "",
                            mentor_name = "",
                            id = 0,
                            createdAt = ""
                        )
                    }
                    Log.d("ElIMUDEBUG", "Connexion réussie pour ${firebaseUser?.email}")
                    login(user) // Met à jour le state
                    onResult(true, null, user)
                } else {
                    onResult(false, task.exception?.message, null)
                }
            }
    }


}