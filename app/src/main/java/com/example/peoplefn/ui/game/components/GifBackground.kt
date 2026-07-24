package com.example.peoplefn.ui.game.components

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GifBackground(
    modifier: Modifier = Modifier,
    playDurationMs: Long = 2000L // Plays for 4 seconds, then stops
) {
    val context = LocalContext.current
    
    // Look up the resource dynamically so the app compiles even if the GIF is missing
    val gifResId = remember {
        context.resources.getIdentifier("game_backgrond", "raw", context.packageName)
    }

    if (gifResId == 0) {
        // Fallback: do nothing if the asset is missing
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        val source = ImageDecoder.createSource(ctx.resources, gifResId)
                        val drawable = ImageDecoder.decodeDrawable(source)
                        setImageDrawable(drawable)
                        if (drawable is AnimatedImageDrawable) {
                            drawable.start()
                            // Stop the animation after playDurationMs
                            postDelayed({
                                if (drawable.isRunning) {
                                    drawable.stop()
                                }
                            }, playDurationMs)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        setImageResource(gifResId)
                    }
                } else {
                    // Fallback for API < 28 (renders the first frame as static)
                    setImageResource(gifResId)
                }
            }
        }
    )
}
