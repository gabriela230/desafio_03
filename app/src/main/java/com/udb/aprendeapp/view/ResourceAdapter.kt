package com.udb.aprendeapp.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udb.aprendeapp.R
import com.udb.aprendeapp.databinding.ItemResourceBinding
import com.udb.aprendeapp.model.Resource

class ResourceAdapter(
    private var resources: List<Resource>,
    private var favoriteIds: Set<String>,
    private val onItemClick: (Resource) -> Unit,
    private val onFavClick: (Resource) -> Unit
) : RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder>() {

    inner class ResourceViewHolder(val binding: ItemResourceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        val resource = resources[position]
        with(holder.binding) {
            tvTitle.text = resource.titulo
            tvType.text = "Tipo: ${resource.tipo}"
            tvDescription.text = resource.descripcion
            tvRating.text = String.format("★ %.1f", resource.calificacion)

            Glide.with(holder.itemView.context)
                .load(resource.imagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(ivThumb)

            val isFav = favoriteIds.contains(resource.id)
            btnFavorite.setImageResource(
                if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )

            root.setOnClickListener { onItemClick(resource) }
            btnFavorite.setOnClickListener { onFavClick(resource) }
        }
    }

    override fun getItemCount(): Int = resources.size

    fun updateData(newResources: List<Resource>, newFavs: Set<String>) {
        this.resources = newResources
        this.favoriteIds = newFavs
        notifyDataSetChanged()
    }
}
