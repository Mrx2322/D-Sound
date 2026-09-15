package com.deiapp.dsound.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.deiapp.dsound.R
import com.deiapp.dsound.model.Cancion
import android.content.ContentResolver

class MusicaLocalRepository(
    private val context: Context
) {

    private val uriPortadasAlbum: Uri =
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(MediaStore.AUTHORITY)
            .appendPath("external")
            .appendPath("audio")
            .appendPath("albumart")
            .build()

    fun obtenerCanciones(): List<Cancion> {

        val canciones =
            mutableListOf<Cancion>()

        val contentResolver =
            context.contentResolver

        val uriColeccion =
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI


        val columnas =
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            )


        val seleccion =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                    "${MediaStore.Audio.Media.DURATION} > 0"


        val orden =
            "${MediaStore.Audio.Media.TITLE} ASC"


        contentResolver.query(
            uriColeccion,
            columnas,
            seleccion,
            null,
            orden
        )?.use { cursor ->

            val indiceId =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media._ID
                )

            val indiceTitulo =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.TITLE
                )

            val indiceArtista =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.ARTIST
                )

            val indiceAlbum =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.ALBUM
                )

            val indiceAlbumId =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.ALBUM_ID
                )

            val indiceDuracion =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.DURATION
                )


            while (cursor.moveToNext()) {

                val id =
                    cursor.getLong(indiceId)

                val titulo =
                    limpiarTexto(
                        cursor.getString(indiceTitulo),
                        R.string.unknown_title
                    )

                val artista =
                    limpiarTexto(
                        cursor.getString(indiceArtista),
                        R.string.unknown_artist
                    )

                val album =
                    limpiarTexto(
                        cursor.getString(indiceAlbum),
                        R.string.unknown_album
                    )

                val albumId =
                    cursor.getLong(indiceAlbumId)

                val duracion =
                    cursor.getLong(indiceDuracion)


                val uriCancion =
                    ContentUris.withAppendedId(
                        uriColeccion,
                        id
                    )

                val uriPortada =
                    if (albumId > 0L) {
                        ContentUris.withAppendedId(
                            uriPortadasAlbum,
                            albumId
                        )
                    } else {
                        null
                    }


                canciones.add(
                    Cancion(
                        id = id,
                        titulo = titulo,
                        artista = artista,
                        album = album,
                        duracion = duracion,
                        uri = uriCancion,
                        portadaUri = uriPortada
                    )
                )
            }
        }

        return canciones
    }


    private fun limpiarTexto(
        texto: String?,
        textoAlternativo: Int
    ): String {

        return if (
            texto.isNullOrBlank() ||
            texto == MediaStore.UNKNOWN_STRING
        ) {
            context.getString(textoAlternativo)
        } else {
            texto
        }
    }
}
