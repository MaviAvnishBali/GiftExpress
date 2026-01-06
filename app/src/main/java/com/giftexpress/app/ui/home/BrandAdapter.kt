package com.giftexpress.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giftexpress.app.databinding.ItemBrandBinding

data class Brand(val name: String, val imageRes: Int? = null)

class BrandAdapter(private val brands: List<Brand>) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val binding = ItemBrandBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        holder.bind(brands[position])
    }

    override fun getItemCount(): Int = brands.size

    class BrandViewHolder(private val binding: ItemBrandBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(brand: Brand) {
            brand.imageRes?.let {
                binding.ivBrand.setImageResource(it)
            }
        }
    }
}
