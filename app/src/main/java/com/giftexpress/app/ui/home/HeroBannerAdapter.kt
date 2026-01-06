package com.giftexpress.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giftexpress.app.R
import com.giftexpress.app.data.model.SliderBanner
import com.giftexpress.app.databinding.ItemHeroBannerBinding

class HeroBannerAdapter(private var banners: List<SliderBanner>) : RecyclerView.Adapter<HeroBannerAdapter.BannerViewHolder>() {

    fun updateData(newBanners: List<SliderBanner>) {
        banners = newBanners
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemHeroBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    override fun getItemCount(): Int = banners.size

    class BannerViewHolder(private val binding: ItemHeroBannerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(banner: SliderBanner) {
            Glide.with(binding.ivBanner.context)
                .load(banner.mobImage)
                .placeholder(R.drawable.ic_gift)
                .error(R.drawable.ic_gift)
                .into(binding.ivBanner)
        }
    }
}
