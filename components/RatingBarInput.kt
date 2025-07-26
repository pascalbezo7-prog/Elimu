package com.kotlingdgocucb.elimuApp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


@Composable
fun RatingBarInput (
    rating: Float,
    onRatingChanged : (Float) -> Unit
) {

    var currentRating by remember{ mutableFloatStateOf(rating) }

    Row {
        for (i in 1 .. 5){
            val star = when{

                rating >= i -> Icons.Default.Star
                rating >= i-0.5 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }

            Icon(
                imageVector = star,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.clickable{
                    currentRating = i.toFloat()
                    onRatingChanged(i.toFloat())
                }
            )
        }
    }


}