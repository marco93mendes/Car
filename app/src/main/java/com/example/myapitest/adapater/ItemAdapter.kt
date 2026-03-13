package com.example.myapitest.adapater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.Item
import com.example.myapitest.ui.loadUrl

class ItemAdapter(
    private val items: List<Item>,
    private val onItemClick: (Item) -> Unit
): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.image)
        val modelTextView = view.findViewById<TextView>(R.id.model)
        val yearTextView = view.findViewById<TextView>(R.id.year)
        val licenceTextView = view.findViewById<TextView>(R.id.licence)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.loadUrl(item.imageUrl)
        holder.modelTextView.text = item.name
        holder.yearTextView.text = item.year
        holder.licenceTextView.text = item.licence
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

    }

    override fun getItemCount(): Int = items.size

}
