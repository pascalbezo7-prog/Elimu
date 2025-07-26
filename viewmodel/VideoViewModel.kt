package com.kotlingdgocucb.elimuApp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.Video
import com.kotlingdgocucb.elimuApp.domain.usecase.GetAllVideosUseCase
import com.kotlingdgocucb.elimuApp.domain.usecase.GetVideoByIdUseCase
import kotlinx.coroutines.launch

class VideoViewModel(
    private val getAllVideosUseCase: GetAllVideosUseCase,
    private val getVideoByIdUseCase: GetVideoByIdUseCase
) : ViewModel() {

    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> get() = _videos

    private val _videoDetail = MutableLiveData<Video?>()
    val videoDetail: LiveData<Video> get() = _videoDetail as LiveData<Video>

    fun fetchAllVideos() {
        viewModelScope.launch {
            try {
                val videoList = getAllVideosUseCase()
                _videos.value = videoList
                if (!videoList.isNullOrEmpty()) {
                    // Exemple : log de la première vidéo de la liste
                    val firstVideo = videoList[0]
                    Log.d("ViewModel", "Vidéo récupérée : ${firstVideo.title} avec l'ID : ${firstVideo.id}")
                } else {
                    Log.e("ViewModel", "Aucune vidéo trouvée")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Erreur lors de la récupération des vidéos", e)
            }
        }
    }

    fun fetchVideoById(id: Int) {
        viewModelScope.launch {
            try {
                val video = getVideoByIdUseCase(id)
                _videoDetail.value = video

                if (video != null) {
                    Log.d("ViewModel", "Vidéo récupérée : ${video.title} avec l'ID : ${video.id}")
                } else {
                    Log.e("ViewModel", "Aucune vidéo trouvée pour l'ID $id")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Erreur lors de la récupération de la vidéo avec l'ID $id", e)
            }
        }
    }
}
