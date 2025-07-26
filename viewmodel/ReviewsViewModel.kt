package com.kotlingdgocucb.elimuApp.ui.viewmodel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.Review
import com.kotlingdgocucb.elimuApp.data.datasource.local.room.entity.ReviewCreate
import com.kotlingdgocucb.elimuApp.domain.usecase.GetAverageRatingUseCase
import com.kotlingdgocucb.elimuApp.domain.usecase.GetReviewsUseCase
import com.kotlingdgocucb.elimuApp.domain.usecase.PostReviewUseCase
import kotlinx.coroutines.launch

class ReviewsViewModel(
    private val getReviewsUseCase: GetReviewsUseCase,
    private val getAverageRatingUseCase: GetAverageRatingUseCase,
    private val postReviewUseCase: PostReviewUseCase
) : ViewModel() {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> get() = _reviews

    private val _averageRating = MutableLiveData<Float>()
    val averageRating: LiveData<Float> get() = _averageRating

    private val _postResult = MutableLiveData<Review>()
    val postResult: LiveData<Review> get() = _postResult

    fun fetchReviews(videoId: Int) {
        viewModelScope.launch {
            try {
                _reviews.value = getReviewsUseCase(videoId)
                _averageRating.value = getAverageRatingUseCase(videoId)
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    fun sendReview(reviewCreate: ReviewCreate) {
        viewModelScope.launch {
            try {
                val result = postReviewUseCase(reviewCreate)
                _postResult.value = result
                // Optionnel : recharger la liste des reviews après l'envoi
                fetchReviews(reviewCreate.videoId)
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

}
