package com.kotlingdgocucb.elimuApp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubeViewerComponent(videoId: String) {
    val context = LocalContext.current
    val activity = context as? Activity

    val youtubeVideoUrl = "https://www.youtube.com/embed/$videoId?enablejsapi=1&autoplay=1"

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                val displayMetrics = ctx.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val videoHeight = (screenWidth * 9) / 16
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    videoHeight
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // Autoriser la lecture en plein écran HTML5
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                // Associer le WebChromeClient personnalisé pour le plein écran
                if (activity != null) {
                    webChromeClient = FullscreenWebChromeClient(activity)
                }

                loadUrl(youtubeVideoUrl)
            }
        },
        update = { webView ->
            // Si besoin de rafraîchir l'URL
            webView.loadUrl(youtubeVideoUrl)
        }
    )
}
