package com.kotlingdgocucb.elimuApp.ui.viewmodel

// viewmodel/ProgressViewModel.kt


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlingdgocucb.elimuApp.domain.model.ProgressResponse
import com.kotlingdgocucb.elimuApp.domain.usecase.ProgressUseCase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(private val progressUseCase: ProgressUseCase) : ViewModel() {

    private val _progress = MutableStateFlow<ProgressResponse?>(null)
    val progress: StateFlow<ProgressResponse?> = _progress

    // Enregistrer la progression (POST)
    fun trackProgress(videoId: Int, menteeEmail: String) {
        viewModelScope.launch {
            try {
                val response = progressUseCase.trackProgress(videoId, menteeEmail)
                _progress.value = response
            } catch (e: Exception) {
                // Gérer l'erreur (log, notifier l'utilisateur, etc.)
                e.printStackTrace()
            }
        }
    }

    // Récupérer la progression (GET)
    fun loadProgress(videoId: Int, menteeEmail: String) {
        viewModelScope.launch {
            try {
                val response = progressUseCase.retrieveProgress(videoId, menteeEmail)
                _progress.value = response
            } catch (e: Exception) {
                // Gérer l'erreur
                e.printStackTrace()
            }
        }
    }
}
