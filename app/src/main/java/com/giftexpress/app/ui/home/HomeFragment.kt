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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home Screen Fragment
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    HomeScreen(
                        viewModel = viewModel,
                        onMenuClick = {
                            val drawerLayout = requireActivity().findViewById<androidx.drawerlayout.widget.DrawerLayout>(
                                R.id.drawer_layout)
                            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
                        }
                    )
                }
            }
        }
    }
}
