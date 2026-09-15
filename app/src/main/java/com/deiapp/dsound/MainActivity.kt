package com.deiapp.dsound

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.dsound.adapter.CancionAdapter
import com.deiapp.dsound.data.MusicaLocalRepository
import com.deiapp.dsound.model.Cancion

class MainActivity : AppCompatActivity() {

    private lateinit var tvCantidadCanciones: TextView
    private lateinit var layoutSinMusica: View
    private lateinit var rvCanciones: RecyclerView

    private lateinit var tvCancionMini: TextView
    private lateinit var tvArtistaMini: TextView

    private lateinit var cancionAdapter: CancionAdapter


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


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        configurarInsets()
        inicializarComponentes()
        configurarListaCanciones()
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
    }


    private fun configurarListaCanciones() {

        cancionAdapter =
            CancionAdapter { cancion ->

                seleccionarCancion(
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

            // En Inicio mostramos solamente las primeras 10.
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


    private fun seleccionarCancion(
        cancion: Cancion
    ) {

        tvCancionMini.text =
            cancion.titulo

        tvArtistaMini.text =
            cancion.artista

        // La reproducción se conectará después con Media3.
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
}