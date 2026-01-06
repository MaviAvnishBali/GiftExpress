package com.giftexpress.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giftexpress.app.R
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.databinding.ItemProductBinding

class ProductAdapter(private var products: List<SliderProduct>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateData(newProducts: List<SliderProduct>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: SliderProduct) {
            binding.tvProductName.text = product.name
            binding.tvProductCategory.text = product.attributes.firstOrNull() ?: ""
            binding.tvPrice.text = "$${product.price}"
            binding.tvRating.text = "4.5" // Default rating as API doesn't provide it
            binding.tvReviewCount.text = "(0 reviews)" // Default reviews as API doesn't provide it
            
            Glide.with(binding.ivProductImage.context)
                .load(product.image)
                .placeholder(R.drawable.ic_gift)
                .error(R.drawable.ic_gift)
                .into(binding.ivProductImage)
        }
    }
}
