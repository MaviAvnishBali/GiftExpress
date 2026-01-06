package com.giftexpress.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giftexpress.app.R
import com.giftexpress.app.databinding.ItemCategoryBinding

import com.giftexpress.app.data.model.MenuItem

class CategoryAdapter(private val categories: List<MenuItem>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: MenuItem) {
            binding.tvCategoryName.text = category.title
            // For now, use a placeholder icon as MenuItem doesn't have an image field
            binding.ivCategory.setImageResource(R.drawable.ic_gift)
        }
    }
}
