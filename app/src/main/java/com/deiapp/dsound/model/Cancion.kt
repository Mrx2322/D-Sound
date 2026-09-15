package com.deiapp.dsound.model

import android.net.Uri

data class Cancion(

    val id: Long,

    val titulo: String,

    val artista: String,

    val album: String,

    val duracion: Long,

    val uri: Uri,

    val portadaUri: Uri? = null
)