package com.deiapp.dsound.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.deiapp.dsound.R
import com.deiapp.dsound.model.Cancion

class CancionAdapter(
    private val onCancionClick: (Cancion) -> Unit
) : ListAdapter<Cancion, CancionAdapter.CancionViewHolder>(
    CancionDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CancionViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_cancion,
                    parent,
                    false
                )

        return CancionViewHolder(view)
    }


    override fun onBindViewHolder(
        holder: CancionViewHolder,
        position: Int
    ) {

        holder.render(
            getItem(position),
            onCancionClick
        )
    }


    class CancionViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTituloCancion: TextView =
            itemView.findViewById(
                R.id.tvTituloCancion
            )

        private val tvArtistaCancion: TextView =
            itemView.findViewById(
                R.id.tvArtistaCancion
            )

        private val tvDuracionCancion: TextView =
            itemView.findViewById(
                R.id.tvDuracionCancion
            )

        private val imgPortadaCancion: ImageView =
            itemView.findViewById(
                R.id.imgPortadaCancion
            )


        fun render(
            cancion: Cancion,
            onCancionClick: (Cancion) -> Unit
        ) {

            tvTituloCancion.text =
                cancion.titulo

            tvArtistaCancion.text =
                cancion.artista

            tvDuracionCancion.text =
                DateUtils.formatElapsedTime(
                    cancion.duracion / 1000
                )

            imgPortadaCancion.load(
                cancion.portadaUri
            )

            itemView.setOnClickListener {
                onCancionClick(cancion)
            }
        }
    }


    private class CancionDiffCallback :
        DiffUtil.ItemCallback<Cancion>() {

        override fun areItemsTheSame(
            oldItem: Cancion,
            newItem: Cancion
        ): Boolean {

            return oldItem.id == newItem.id
        }


        override fun areContentsTheSame(
            oldItem: Cancion,
            newItem: Cancion
        ): Boolean {

            return oldItem == newItem
        }
    }
}
