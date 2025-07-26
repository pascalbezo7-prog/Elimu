package com.kotlingdgocucb.elimuApp.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient

/**
 * WebChromeClient personnalisé pour autoriser le plein écran
 */
class FullscreenWebChromeClient(
    private val activity: Activity
) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalSystemUiVisibility = 0
    private var originalOrientation = 0

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        super.onShowCustomView(view, callback)

        // Éviter la redondance
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback

        // Sauvegarder l'état actuel de l'UI
        originalSystemUiVisibility = activity.window.decorView.systemUiVisibility
        originalOrientation = activity.requestedOrientation

        // Ajouter la vue en plein écran
        (activity.window.decorView as ViewGroup).addView(
            customView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Masquer la barre de statut/navigation si nécessaire
        activity.window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onHideCustomView() {
        super.onHideCustomView()

        // Rétablir l'état précédent
        if (customView != null) {
            (activity.window.decorView as ViewGroup).removeView(customView)
            customView = null
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null

            activity.window.decorView.systemUiVisibility = originalSystemUiVisibility
            activity.requestedOrientation = originalOrientation
        }
    }
}
