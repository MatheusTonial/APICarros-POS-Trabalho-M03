package com.tonial.apicarros.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tonial.apicarros.model.Carro
import com.tonial.apicarros.R
import com.tonial.apicarros.ui.loadUrl

class CarroAdapter (
    private val items: List<Carro>,
    private val onItemClick: (Carro) -> Unit,
) : RecyclerView.Adapter<CarroAdapter.CarroViewHolder>() {
    class CarroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.image)
        val nameTextView = view.findViewById<TextView>(R.id.name)
        val yearTextView = view.findViewById<TextView>(R.id.year)
        val licenceTextView = view.findViewById<TextView>(R.id.licence)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return CarroViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarroViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.yearTextView.text = item.year
        holder.licenceTextView.text = item.licence
        holder.imageView.loadUrl(item.imageUrl)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}