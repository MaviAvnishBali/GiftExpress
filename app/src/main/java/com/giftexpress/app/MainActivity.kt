package com.giftexpress.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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
import com.giftexpress.app.data.update.FlexibleUpdateManager
import com.giftexpress.app.data.update.FlexibleUpdateStatus
import com.google.android.material.snackbar.Snackbar
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

    @Inject
    lateinit var flexibleUpdateManager: FlexibleUpdateManager
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private var updateSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Splash Screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWindowInsets()

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
        setupFlexibleUpdate()

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
                when {
                    it.title.contains("Best Sellers", ignoreCase = true) || it.url?.contains("all-products", ignoreCase = true) == true -> {
                        val bundle = Bundle().apply {
                            putInt("specialFlag", 15)
                            putString("title", "Best Sellers")
                            putInt("categoryId", 0)
                            putInt("brandId", 0)
                        }
                        navController.navigate(R.id.specialProductsFragment, bundle)
                    }
                    it.categoryId != null -> {
                        val bundle = Bundle().apply {
                            putInt("categoryId", it.categoryId)
                            putString("categoryName", it.title)
                        }
                        navController.navigate(R.id.categoryFragment, bundle)
                    }
                    else -> {
                        navController.navigate(R.id.perfumeEnquiryFragment)
                    }
                }
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
            if (destination.id in bottomNavIds) {
                binding.bottomNav.visibility = View.VISIBLE
                binding.bottomNavShadow.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(
                    androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
                )
            } else {
                binding.bottomNav.visibility = View.GONE
                binding.bottomNavShadow.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(
                    androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                )
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
     * Proactive token refresh matching iOS:
     * iOS checks AppPreference.isSessionExpired() (30-minute threshold) on every screen appearance.
     * We check when the app enters STARTED state, and periodically every 15 minutes while running.
     * OkHttp's TokenAuthenticator also transparently handles reactive 401 recovery.
     */
    private fun startTokenRefresh() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (authRepository.isLoggedIn() && authRepository.isSessionExpired()) {
                    authRepository.refreshToken()
                }
                while (true) {
                    kotlinx.coroutines.delay(15 * 60 * 1000L) // Check every 15 minutes
                    if (authRepository.isLoggedIn() && authRepository.isSessionExpired()) {
                        authRepository.refreshToken()
                    }
                }
            }
        }
    }

    /**
     * Set up Google Play Flexible Update flow and observe download states
     */
    private fun setupFlexibleUpdate() {
        flexibleUpdateManager.registerListener()
        flexibleUpdateManager.checkForUpdate(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flexibleUpdateManager.updateStatus.collect { status ->
                    when (status) {
                        is FlexibleUpdateStatus.Downloaded -> {
                            showUpdateDownloadedSnackbar()
                        }
                        else -> {
                            // Other statuses (Checking, Available, Downloading, etc.) handled internally
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles edge-to-edge window insets for Android 15+ (API 35+ / API 36).
     * Binds the status bar height to statusBarSpacer and navigation bar height to navBarSpacer.
     */
    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Primary header background is black, so status bar icons should be light/white
        insetsController.isAppearanceLightStatusBars = false
        // Bottom navigation spacer background is white, so system nav buttons should be dark
        insetsController.isAppearanceLightNavigationBars = true

        // Prevent BottomNavigationView from consuming window insets and adding extra bottom padding
        // because navBarSpacer handles the bottom system window insets.
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { _, insets ->
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val statusBars = windowInsets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val navigationBars = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
            )

            binding.statusBarSpacer.updateLayoutParams {
                height = maxOf(1, statusBars.top)
            }
            binding.navBarSpacer.updateLayoutParams {
                height = maxOf(1, navigationBars.bottom)
            }

            // Drawer status bar spacer ensures status bar area remains black with white icons
            binding.drawerStatusBarSpacer.updateLayoutParams {
                height = maxOf(1, statusBars.top)
            }
            // Ensure drawer menu items don't overlap the bottom navigation bar
            binding.navView.setPadding(0, 0, 0, navigationBars.bottom)

            windowInsets
        }

        ViewCompat.requestApplyInsets(binding.root)
    }

    /**
     * Displays a persistent Snackbar informing user that the flexible update is downloaded,
     * with a RESTART action to install and restart immediately.
     */
    private fun showUpdateDownloadedSnackbar() {
        if (updateSnackbar?.isShown == true) return

        val snackbar = Snackbar.make(
            binding.root,
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("RESTART") {
            flexibleUpdateManager.completeUpdate()
        }

        // Anchor above bottom navigation bar when visible so it doesn't overlap navigation icons
        if (binding.bottomNav.visibility == View.VISIBLE) {
            snackbar.anchorView = binding.bottomNav
        } else {
            snackbar.anchorView = binding.navBarSpacer
        }

        updateSnackbar = snackbar
        snackbar.show()
    }

    override fun onResume() {
        super.onResume()
        // Check if an update finished downloading while app was in background
        flexibleUpdateManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        flexibleUpdateManager.unregisterListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FlexibleUpdateManager.REQUEST_CODE_FLEXIBLE_UPDATE) {
            if (resultCode != RESULT_OK) {
                android.util.Log.d("MainActivity", "Flexible update canceled or failed by user: $resultCode")
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

