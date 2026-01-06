package com.giftexpress.app.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Splash Screen Fragment
 * Checks login status and navigates to appropriate screen
 */
@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe login status
        observeLoginStatus()
        
        // Check login status
        viewModel.checkLoginStatus()
    }

    /**
     * Observe login status and navigate accordingly
     */
    private fun observeLoginStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoggedIn.collect { isLoggedIn ->
                    when (isLoggedIn) {
                        true -> {
                            // Navigate to home screen
                            findNavController().navigate(
                                R.id.action_splashFragment_to_homeFragment
                            )
                        }
                        false -> {
                            // Navigate to login screen
                            findNavController().navigate(
                                R.id.action_splashFragment_to_loginFragment
                            )
                        }
                        null -> {
                            // Still loading, do nothing
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
