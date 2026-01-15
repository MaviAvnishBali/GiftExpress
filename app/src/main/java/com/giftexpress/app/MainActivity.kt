package com.giftexpress.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.databinding.ActivityMainBinding
import com.giftexpress.app.utils.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity - Single Activity Architecture
 * Hosts all fragments using Navigation Component
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false
    
    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Splash Screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Inject Google Sign-In client into AuthRepository for logout
        authRepository.googleSignInClient = googleSignInClient

        setupNavigation()
        setupDrawer() // Call the new setupDrawer function
        observeMenu()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 1. Close Drawer if open
                if (binding.drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    return
                }

                // 2. Try to navigate up in the fragment backstack
                // This handles sub-pages and tab-to-home navigation
                if (navController.previousBackStackEntry != null) {
                    navController.navigateUp()
                    return
                }

                // 3. If we are on Home, use the double-tap to exit logic
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }

                doubleBackToExitPressedOnce = true
                android.widget.Toast.makeText(this@MainActivity, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }

    private fun observeMenu() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.menuState.collect { state ->
                    if (state is UiState.Success) {
                        updateDrawerMenu(state.data)
                    }
                }
            }
        }
    }

    private fun updateDrawerMenu(menuItems: List<MenuItem>) {
        val menu = binding.navView.menu
        menu.clear()

        // Disable icon tint to show original image colors
        binding.navView.itemIconTintList = null
        
        menuItems.forEach { item ->
            val menuItem = menu.add(0, item.id, 0, item.title)
            
            // Load image using Glide
            com.bumptech.glide.Glide.with(this)
                .asDrawable()
                .load(item.image)
                .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                    ) {
                        menuItem.icon = resource
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // Handle cleanup if needed
                    }
                })
        }
        
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val item = menuItems.find { it.id == menuItem.itemId }
            item?.let {
                val bundle = Bundle().apply {
                    putInt("categoryId", it.categoryId ?: it.id)
                    putString("categoryName", it.title)
                }
                navController.navigate(R.id.categoryFragment, bundle)
                binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                true
            } ?: false
        }
    }
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        // 🚫 Disable automatic screen titles
        navController.addOnDestinationChangedListener { _, _, _ ->
            title = ""
        }

        binding.bottomNav.setupWithNavController(navController)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.signupFragment, R.id.changePasswordFragment, R.id.forgotPasswordFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.drawerLayout.setDrawerLockMode(
                        androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    )
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.drawerLayout.setDrawerLockMode(
                        androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
                    )
                }
            }
        }
    }

    /**
     * Setup Navigation Drawer
     */
    private fun setupDrawer() {
        val drawerLayout = binding.drawerLayout
        
        // Handle Logout
        val btnLogout = binding.root.findViewById<View>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            // Perform Logout
            lifecycleScope.launch {
                authRepository.logout()
                // Navigate to login screen
                navController.navigate(R.id.loginFragment)
            }
        }
    }
    /**
     * Handle up navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
