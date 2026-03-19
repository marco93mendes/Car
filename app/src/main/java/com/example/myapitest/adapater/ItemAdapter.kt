package com.example.myapitest.adapater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.ItemValue
import com.example.myapitest.ui.loadUrl

class ItemAdapter(
    private val items: List<ItemValue>,
    private val onItemClick: (ItemValue) -> Unit
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
        val car = items[position]
        holder.imageView.loadUrl(car.imageUrl)
        holder.modelTextView.text = car.name
        holder.yearTextView.text = car.year
        holder.licenceTextView.text = car.licence
        holder.itemView.setOnClickListener {
            onItemClick(car)
        }

    }

    override fun getItemCount(): Int = items.size

}
