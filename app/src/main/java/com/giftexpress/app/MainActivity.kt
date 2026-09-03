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
import com.giftexpress.app.utils.NetworkObserver
import com.giftexpress.app.ui.components.NoInternetScreen
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
        setupNetworkObserver()
        startTokenRefresh()

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

        // Bottom nav destination IDs — used to sync selection on back press / programmatic nav
        val bottomNavIds = setOf(
            R.id.homeFragment,
            R.id.categoriesFragment,
            R.id.offersFragment,
            R.id.ordersFragment,
            R.id.accountFragment
        )

        // Sync bottom nav indicator whenever the NavController destination changes.
        // Using menu.findItem().isChecked directly avoids re-triggering setOnItemSelectedListener.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = ""
            if (destination.id in bottomNavIds) {
                binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
            }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == navController.currentDestination?.id) {
                // Already on this destination — do nothing (avoid duplicate back-stack entries)
                return@setOnItemSelectedListener true
            }
            navController.navigate(item.itemId)
            true
        }
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.signupFragment, R.id.changePasswordFragment, R.id.forgotPasswordFragment, R.id.categoryFragment, R.id.productDetailsFragment, R.id.specialProductsFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.bottomNavShadow.visibility = View.GONE
                    binding.drawerLayout.setDrawerLockMode(
                        androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    )
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.bottomNavShadow.visibility = View.VISIBLE
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
    private fun setupNetworkObserver() {
        val networkObserver = NetworkObserver(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkObserver.observe.collect { status ->
                    when (status) {
                        NetworkObserver.Status.Unavailable, NetworkObserver.Status.Lost -> {
                            showNoInternetOverlay()
                        }
                        else -> {
                            hideNoInternetOverlay()
                        }
                    }
                }
            }
        }
    }

    private fun showNoInternetOverlay() {
        binding.noInternetComposeView.visibility = View.VISIBLE
        binding.noInternetComposeView.setContent {
            androidx.compose.material3.MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                NoInternetScreen(onRetry = {
                    // Status will update automatically
                })
            }
        }
    }

    private fun hideNoInternetOverlay() {
        binding.noInternetComposeView.visibility = View.GONE
    }

    /**
     * Token refresh: the server issues 60-minute access tokens, so we refresh on every
     * app foreground AND every 45 minutes — comfortably ahead of expiry. (The previous
     * 90-minute interval was longer than the token's own lifetime, guaranteeing a window
     * where every authenticated call — e.g. the cart — failed with a 401.)
     */
    private fun startTokenRefresh() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (authRepository.isLoggedIn()) {
                    authRepository.refreshToken()
                }
                while (true) {
                    kotlinx.coroutines.delay(45 * 60 * 1000L) // 45 minutes (< 60-min token life)
                    if (authRepository.isLoggedIn()) {
                        authRepository.refreshToken()
                    }
                }
            }
        }
    }

    /**
     * Handle up navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * PayPal web checkout returns to the app via a deep link (launchMode="singleTop").
     * Store the new intent so PaymentFragment can complete the checkout in onResume().
     */
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        if (intent != null) setIntent(intent)
    }
}
