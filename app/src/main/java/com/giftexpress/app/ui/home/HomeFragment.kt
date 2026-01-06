package com.giftexpress.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.giftexpress.app.R
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.model.SliderResponse
import com.giftexpress.app.databinding.FragmentHomeBinding
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home Screen Fragment
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var homeMainAdapter: HomeMainAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMainAdapter()
        observeSliders()
    }

    private fun setupMainAdapter() {
        homeMainAdapter = HomeMainAdapter(emptyList()) {
            val drawerLayout = requireActivity().findViewById<androidx.drawerlayout.widget.DrawerLayout>(
                R.id.drawer_layout)
            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
        }
        binding.rvHomeMain.adapter = homeMainAdapter
    }

    private fun observeSliders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe both sliders and menu items
                launch {
                    viewModel.slidersState.collect { state ->
                        if (state is UiState.Success) {
                            updateUi(state.data, (viewModel.menuState.value as? UiState.Success)?.data ?: emptyList())
                        }
                    }
                }
                launch {
                    viewModel.menuState.collect { state: UiState<List<MenuItem>> ->
                        if (state is UiState.Success) {
                            updateUi((viewModel.slidersState.value as? UiState.Success)?.data ?: emptyList(), state.data)
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(sliders: List<SliderResponse>, menuItems: List<MenuItem>) {
        val sections = mutableListOf<HomeSection>()
        
        // 1. Header (Logo, Search)
        sections.add(HomeSection.Header)

        // 2. Dynamic Categories from Menu API
        if (menuItems.isNotEmpty()) {
            sections.add(HomeSection.Categories(menuItems))
        }

        // 3. Process Sliders from API
        sliders.forEach { slider ->
            when {
                slider.banners != null -> {
                    // Hero Banner
                    sections.add(HomeSection.HeroBanner(slider.banners))
                    
                    // Promo Banner (using first banner as before)
                    slider.banners.firstOrNull()?.let { banner ->
                        sections.add(HomeSection.PromoBanner(banner.mobImage))
                    }
                }
                slider.products != null -> {
                    // Dynamic Product Section
                    sections.add(HomeSection.ProductSection(slider.title ?: "Products", slider.products))
                }
            }
        }

        // 4. Static Brands (Keep for now, but structured for easy replacement)
        sections.add(HomeSection.Brands(getMockBrands()))

        homeMainAdapter.updateData(sections)
    }

    private fun getMockBrands(): List<Brand> {
        return listOf(
            Brand("Armaf"),
            Brand("Azzaro"),
            Brand("Calvin Klein"),
            Brand("Burberry"),
            Brand("Versace")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
