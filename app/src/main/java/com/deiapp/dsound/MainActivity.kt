package com.deiapp.dsound

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.dsound.adapter.CancionAdapter
import com.deiapp.dsound.data.MusicaLocalRepository
import com.deiapp.dsound.model.Cancion
import com.deiapp.dsound.player.ActivityReproductor
import com.deiapp.dsound.player.ReproductorService
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : AppCompatActivity() {

    private lateinit var tvCantidadCanciones: TextView
    private lateinit var layoutSinMusica: View
    private lateinit var rvCanciones: RecyclerView

    private lateinit var tvCancionMini: TextView
    private lateinit var tvArtistaMini: TextView
    private lateinit var miniPlayerContainer: View
    private lateinit var btnPlayMini: ImageButton

    private lateinit var btnAnteriorMini: ImageButton
    private lateinit var btnSiguienteMini: ImageButton

    private lateinit var cancionAdapter: CancionAdapter

    private var cancionesDisponibles:
            List<Cancion> = emptyList()

    private var mediaController:
            MediaController? = null

    private var controllerFuture:
            ListenableFuture<MediaController>? = null


    private val permisoAudioLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permisoConcedido ->

            if (permisoConcedido) {
                cargarCanciones()
            } else {
                mostrarPermisoDenegado()
            }
        }


    private val playerListener =
        object : Player.Listener {

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {
                actualizarBotonReproduccion(
                    isPlaying
                )
            }


            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                actualizarMiniReproductor(
                    mediaItem
                )
            }


            override fun onPlayerError(
                error: PlaybackException
            ) {
                Toast.makeText(
                    this@MainActivity,
                    R.string.player_playback_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        configurarInsets()
        inicializarComponentes()
        configurarListaCanciones()
        configurarBotonMini()
        conectarReproductor()
        comprobarPermisoAudio()
    }


    private fun configurarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }


    private fun inicializarComponentes() {

        tvCantidadCanciones =
            findViewById(R.id.tvCantidadCanciones)

        layoutSinMusica =
            findViewById(R.id.layoutSinMusica)

        rvCanciones =
            findViewById(R.id.rvCanciones)

        tvCancionMini =
            findViewById(R.id.tvCancionMini)

        tvArtistaMini =
            findViewById(R.id.tvArtistaMini)

        miniPlayerContainer =
            findViewById(R.id.miniPlayerContainer)

        btnPlayMini =
            findViewById(R.id.btnPlayMini)

        btnAnteriorMini =
            findViewById(R.id.btnAnteriorMini)

        btnSiguienteMini =
            findViewById(R.id.btnSiguienteMini)
    }


    private fun configurarListaCanciones() {

        cancionAdapter =
            CancionAdapter { cancion ->

                reproducirCancion(
                    cancion
                )
            }

        rvCanciones.layoutManager =
            LinearLayoutManager(this)

        rvCanciones.adapter =
            cancionAdapter

        rvCanciones.itemAnimator =
            null
    }


    private fun configurarBotonMini() {

        miniPlayerContainer.setOnClickListener {

            val controller =
                mediaController

            if (
                controller == null ||
                controller.mediaItemCount == 0
            ) {

                Toast.makeText(
                    this,
                    R.string.player_not_ready,
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            startActivity(
                Intent(
                    this,
                    ActivityReproductor::class.java
                )
            )
        }

        btnPlayMini.setOnClickListener {

            val controller =
                mediaController

            if (controller == null) {

                Toast.makeText(
                    this,
                    R.string.player_not_ready,
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }


            if (controller.mediaItemCount == 0) {
                return@setOnClickListener
            }


            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }


        btnAnteriorMini.setOnClickListener {

            val controller =
                mediaController
                    ?: return@setOnClickListener


            if (
                controller.hasPreviousMediaItem()
            ) {

                controller.seekToPreviousMediaItem()
                controller.play()
            }
        }


        btnSiguienteMini.setOnClickListener {

            val controller =
                mediaController
                    ?: return@setOnClickListener


            if (
                controller.hasNextMediaItem()
            ) {

                controller.seekToNextMediaItem()
                controller.play()
            }
        }
    }


    private fun conectarReproductor() {

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

                    actualizarBotonReproduccion(
                        controller.isPlaying
                    )

                    actualizarMiniReproductor(
                        controller.currentMediaItem
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


    private fun comprobarPermisoAudio() {

        val permiso =
            obtenerPermisoAudio()

        val permisoConcedido =
            ContextCompat.checkSelfPermission(
                this,
                permiso
            ) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            cargarCanciones()
        } else {
            permisoAudioLauncher.launch(permiso)
        }
    }


    private fun obtenerPermisoAudio(): String {

        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }


    private fun cargarCanciones() {

        tvCantidadCanciones.text =
            getString(R.string.music_scanning)

        Thread {

            try {

                val repository =
                    MusicaLocalRepository(this)

                val canciones =
                    repository.obtenerCanciones()

                runOnUiThread {

                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    mostrarResultadoCanciones(
                        canciones
                    )
                }

            } catch (error: Exception) {

                runOnUiThread {

                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    mostrarErrorCarga()
                }
            }

        }.start()
    }


    private fun mostrarResultadoCanciones(
        canciones: List<Cancion>
    ) {

        cancionesDisponibles =
            canciones

        val cantidad =
            canciones.size

        if (cantidad > 0) {

            tvCantidadCanciones.text =
                resources.getQuantityString(
                    R.plurals.song_count,
                    cantidad,
                    cantidad
                )

            layoutSinMusica.visibility =
                View.GONE

            rvCanciones.visibility =
                View.VISIBLE

            cancionAdapter.submitList(
                canciones.take(10)
            )

        } else {

            tvCantidadCanciones.text =
                getString(
                    R.string.no_music_found
                )

            layoutSinMusica.visibility =
                View.VISIBLE

            rvCanciones.visibility =
                View.GONE

            cancionAdapter.submitList(
                emptyList()
            )
        }
    }


    private fun reproducirCancion(
        cancion: Cancion
    ) {

        val controller =
            mediaController

        if (controller == null) {

            Toast.makeText(
                this,
                R.string.player_not_ready,
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        val posicion =
            cancionesDisponibles.indexOfFirst {
                it.id == cancion.id
            }

        if (posicion == -1) {
            return
        }


        val mediaItems =
            cancionesDisponibles.map {
                convertirAMediaItem(it)
            }


        controller.setMediaItems(
            mediaItems,
            posicion,
            0L
        )

        controller.prepare()
        controller.play()
    }


    private fun convertirAMediaItem(
        cancion: Cancion
    ): MediaItem {

        val metadata =
            MediaMetadata.Builder()
                .setTitle(cancion.titulo)
                .setArtist(cancion.artista)
                .setAlbumTitle(cancion.album)
                .build()


        return MediaItem.Builder()
            .setMediaId(
                cancion.id.toString()
            )
            .setUri(
                cancion.uri
            )
            .setMediaMetadata(
                metadata
            )
            .build()
    }


    private fun actualizarMiniReproductor(
        mediaItem: MediaItem?
    ) {

        if (mediaItem == null) {
            return
        }


        tvCancionMini.text =
            mediaItem.mediaMetadata.title
                ?: getString(
                    R.string.unknown_title
                )


        tvArtistaMini.text =
            mediaItem.mediaMetadata.artist
                ?: getString(
                    R.string.unknown_artist
                )
    }


    private fun actualizarBotonReproduccion(
        reproduciendo: Boolean
    ) {

        if (reproduciendo) {

            btnPlayMini.setImageResource(
                android.R.drawable.ic_media_pause
            )

            btnPlayMini.contentDescription =
                getString(R.string.pause)

        } else {

            btnPlayMini.setImageResource(
                android.R.drawable.ic_media_play
            )

            btnPlayMini.contentDescription =
                getString(R.string.play)
        }
    }


    private fun mostrarPermisoDenegado() {

        tvCantidadCanciones.text =
            getString(
                R.string.audio_permission_denied
            )

        layoutSinMusica.visibility =
            View.VISIBLE

        rvCanciones.visibility =
            View.GONE

        Toast.makeText(
            this,
            R.string.audio_permission_denied,
            Toast.LENGTH_LONG
        ).show()
    }


    private fun mostrarErrorCarga() {

        tvCantidadCanciones.text =
            getString(
                R.string.music_load_error
            )

        layoutSinMusica.visibility =
            View.VISIBLE

        rvCanciones.visibility =
            View.GONE

        Toast.makeText(
            this,
            R.string.music_load_error,
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onDestroy() {

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

        super.onDestroy()
    }
}
