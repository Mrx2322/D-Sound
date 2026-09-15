package com.deiapp.dsound.player

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class ReproductorService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession


    override fun onCreate() {
        super.onCreate()

        val audioAttributes =
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(
                    C.AUDIO_CONTENT_TYPE_MUSIC
                )
                .build()


        player =
            ExoPlayer.Builder(this)
                .build()
                .apply {

                    setAudioAttributes(
                        audioAttributes,
                        true
                    )

                    setHandleAudioBecomingNoisy(
                        true
                    )
                }


        mediaSession =
            MediaSession.Builder(
                this,
                player
            ).build()
    }


    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession {

        return mediaSession
    }


    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {

        if (
            !player.playWhenReady ||
            player.mediaItemCount == 0
        ) {
            stopSelf()
        }
    }


    override fun onDestroy() {

        mediaSession.release()
        player.release()

        super.onDestroy()
    }
}