package com.deiapp.dsound.player

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.deiapp.dsound.R
import com.google.common.util.concurrent.ListenableFuture

class ActivityReproductor : AppCompatActivity() {

    private lateinit var btnCerrarReproductor: ImageButton
    private lateinit var btnAnteriorReproductor: ImageButton
    private lateinit var btnPlayReproductor: ImageButton
    private lateinit var btnSiguienteReproductor: ImageButton

    private lateinit var tvTituloReproductor: TextView
    private lateinit var tvArtistaReproductor: TextView
    private lateinit var tvTiempoActual: TextView
    private lateinit var tvDuracionTotal: TextView

    private lateinit var seekBarReproductor: SeekBar

    private var mediaController: MediaController? = null

    private var controllerFuture:
            ListenableFuture<MediaController>? = null

    private val progressHandler =
        Handler(Looper.getMainLooper())


    private val actualizarProgresoRunnable =
        object : Runnable {

            override fun run() {

                actualizarProgreso()

                progressHandler.postDelayed(
                    this,
                    500
                )
            }
        }


    private val playerListener =
        object : Player.Listener {

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {

                actualizarBotonPlay(
                    isPlaying
                )
            }


            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {

                actualizarInformacion(
                    mediaItem
                )
            }


            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {

                actualizarProgreso()
            }
        }


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(
            R.layout.activity_reproductor
        )

        configurarInsets()
        inicializarComponentes()
        configurarBotones()
        configurarSeekBar()
    }


    override fun onStart() {
        super.onStart()

        conectarReproductor()
    }


    private fun configurarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.mainReproductor)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left + view.paddingLeft,
                systemBars.top,
                systemBars.right + view.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }


    private fun inicializarComponentes() {

        btnCerrarReproductor =
            findViewById(
                R.id.btnCerrarReproductor
            )

        btnAnteriorReproductor =
            findViewById(
                R.id.btnAnteriorReproductor
            )

        btnPlayReproductor =
            findViewById(
                R.id.btnPlayReproductor
            )

        btnSiguienteReproductor =
            findViewById(
                R.id.btnSiguienteReproductor
            )


        tvTituloReproductor =
            findViewById(
                R.id.tvTituloReproductor
            )

        tvArtistaReproductor =
            findViewById(
                R.id.tvArtistaReproductor
            )

        tvTiempoActual =
            findViewById(
                R.id.tvTiempoActual
            )

        tvDuracionTotal =
            findViewById(
                R.id.tvDuracionTotal
            )


        seekBarReproductor =
            findViewById(
                R.id.seekBarReproductor
            )
    }


    private fun configurarBotones() {

        btnCerrarReproductor.setOnClickListener {
            finish()
        }


        btnPlayReproductor.setOnClickListener {

            val controller =
                mediaController
                    ?: return@setOnClickListener


            if (controller.mediaItemCount == 0) {
                return@setOnClickListener
            }


            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }


        btnAnteriorReproductor.setOnClickListener {

            val controller =
                mediaController
                    ?: return@setOnClickListener


            if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
                controller.play()
            }
        }


        btnSiguienteReproductor.setOnClickListener {

            val controller =
                mediaController
                    ?: return@setOnClickListener


            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
                controller.play()
            }
        }
    }


    private fun configurarSeekBar() {

        seekBarReproductor.max =
            1000


        seekBarReproductor.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    if (!fromUser) {
                        return
                    }


                    val controller =
                        mediaController
                            ?: return


                    val duracion =
                        controller.duration


                    if (
                        duracion <= 0L ||
                        duracion == C.TIME_UNSET
                    ) {
                        return
                    }


                    val nuevaPosicion =
                        duracion * progress / 1000L


                    controller.seekTo(
                        nuevaPosicion
                    )
                }


                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                    // No se necesita ninguna acción.
                }


                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                    // No se necesita ninguna acción.
                }
            }
        )
    }


    private fun conectarReproductor() {

        if (controllerFuture != null) {
            return
        }


        val sessionToken =
            SessionToken(
                this,
                ComponentName(
                    this,
                    ReproductorService::class.java
                )
            )


        controllerFuture =
            MediaController.Builder(
                this,
                sessionToken
            ).buildAsync()


        controllerFuture?.addListener(
            {

                try {

                    val controller =
                        controllerFuture?.get()
                            ?: return@addListener


                    mediaController =
                        controller


                    controller.addListener(
                        playerListener
                    )


                    actualizarInformacion(
                        controller.currentMediaItem
                    )


                    actualizarBotonPlay(
                        controller.isPlaying
                    )


                    actualizarProgreso()


                    progressHandler.post(
                        actualizarProgresoRunnable
                    )

                } catch (error: Exception) {

                    Toast.makeText(
                        this,
                        R.string.player_connection_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            },
            ContextCompat.getMainExecutor(this)
        )
    }


    private fun actualizarInformacion(
        mediaItem: MediaItem?
    ) {

        tvTituloReproductor.text =
            mediaItem?.mediaMetadata?.title
                ?: getString(
                    R.string.nothing_playing
                )


        tvArtistaReproductor.text =
            mediaItem?.mediaMetadata?.artist
                ?: getString(
                    R.string.select_song
                )
    }


    private fun actualizarBotonPlay(
        reproduciendo: Boolean
    ) {

        if (reproduciendo) {

            btnPlayReproductor.setImageResource(
                android.R.drawable.ic_media_pause
            )

            btnPlayReproductor.contentDescription =
                getString(R.string.pause)

        } else {

            btnPlayReproductor.setImageResource(
                android.R.drawable.ic_media_play
            )

            btnPlayReproductor.contentDescription =
                getString(R.string.play)
        }
    }


    private fun actualizarProgreso() {

        val controller =
            mediaController
                ?: return


        val duracion =
            controller.duration


        val posicionActual =
            controller.currentPosition
                .coerceAtLeast(0L)


        if (
            duracion <= 0L ||
            duracion == C.TIME_UNSET
        ) {

            seekBarReproductor.progress =
                0

            tvTiempoActual.text =
                getString(R.string.initial_time)

            tvDuracionTotal.text =
                getString(R.string.initial_time)

            return
        }


        seekBarReproductor.progress =
            (
                    posicionActual * 1000L /
                            duracion
                    ).toInt()
                .coerceIn(0, 1000)


        tvTiempoActual.text =
            DateUtils.formatElapsedTime(
                posicionActual / 1000L
            )


        tvDuracionTotal.text =
            DateUtils.formatElapsedTime(
                duracion / 1000L
            )
    }


    override fun onStop() {

        progressHandler.removeCallbacks(
            actualizarProgresoRunnable
        )


        mediaController?.removeListener(
            playerListener
        )


        controllerFuture?.let { future ->

            MediaController.releaseFuture(
                future
            )
        }


        mediaController =
            null

        controllerFuture =
            null

        super.onStop()
    }
}