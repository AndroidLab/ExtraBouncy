package com.alab.extrabouncy

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alab.extrabouncy.databinding.RvHorizontalHolderBinding
import java.util.Random


class RVAdapter(private val data: List<String>) : RecyclerView.Adapter<RVAdapter.ItemViewHolder>() {

    private val rnd = Random()

    override fun getItemCount(): Int = data.size // Количество элементов в списке данных

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RvHorizontalHolderBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        holder.binding.tv.setBackgroundColor(color)
        holder.binding.tv.text = data[position]
    }

    class ItemViewHolder(val binding: RvHorizontalHolderBinding) : RecyclerView.ViewHolder(binding.root)
}