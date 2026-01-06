package com.giftexpress.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giftexpress.app.R
import com.giftexpress.app.databinding.*

class HomeMainAdapter(
    private var sections: List<HomeSection>,
    private val onMenuClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_HERO = 1
        private const val TYPE_CATEGORIES = 2
        private const val TYPE_PROMO = 3
        private const val TYPE_PRODUCTS = 4
        private const val TYPE_BRANDS = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (sections[position]) {
            is HomeSection.Header -> TYPE_HEADER
            is HomeSection.HeroBanner -> TYPE_HERO
            is HomeSection.Categories -> TYPE_CATEGORIES
            is HomeSection.PromoBanner -> TYPE_PROMO
            is HomeSection.ProductSection -> TYPE_PRODUCTS
            is HomeSection.Brands -> TYPE_BRANDS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(SectionHomeHeaderBinding.inflate(inflater, parent, false))
            TYPE_HERO -> HeroViewHolder(SectionHomeHeroBinding.inflate(inflater, parent, false))
            TYPE_PROMO -> PromoViewHolder(SectionHomePromoBinding.inflate(inflater, parent, false))
            else -> HorizontalViewHolder(SectionHomeHorizontalBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val section = sections[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind()
            is HeroViewHolder -> holder.bind(section as HomeSection.HeroBanner)
            is PromoViewHolder -> holder.bind(section as HomeSection.PromoBanner)
            is HorizontalViewHolder -> holder.bind(section)
        }
    }

    override fun getItemCount(): Int = sections.size

    fun updateData(newSections: List<HomeSection>) {
        sections = newSections
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(private val binding: SectionHomeHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.ivMenu.setOnClickListener { onMenuClick() }
        }
    }

    inner class HeroViewHolder(private val binding: SectionHomeHeroBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: HomeSection.HeroBanner) {
            binding.vpHeroBanner.adapter = HeroBannerAdapter(section.banners)
        }
    }

    inner class PromoViewHolder(private val binding: SectionHomePromoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: HomeSection.PromoBanner) {
            Glide.with(binding.root.context)
                .load(section.imageUrl)
                .placeholder(R.drawable.ic_gift)
                .into(binding.ivPromoBanner)
        }
    }

    inner class HorizontalViewHolder(private val binding: SectionHomeHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: HomeSection) {
            when (section) {
                is HomeSection.Categories -> {
                    binding.tvSectionTitle.text = "SHOP BY CATEGORY"
                    binding.rvHorizontal.adapter = CategoryAdapter(section.categories)
                }
                is HomeSection.ProductSection -> {
                    binding.tvSectionTitle.text = section.title
                    binding.rvHorizontal.adapter = ProductAdapter(section.products)
                }
                is HomeSection.Brands -> {
                    binding.tvSectionTitle.text = "POPULAR BRANDS"
                    binding.rvHorizontal.adapter = BrandAdapter(section.brands)
                }
                else -> {}
            }
        }
    }
}
