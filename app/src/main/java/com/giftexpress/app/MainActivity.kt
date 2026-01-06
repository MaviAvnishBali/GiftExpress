package com.giftexpress.app

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.databinding.ActivityMainBinding
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Splash Screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupDrawer() // Call the new setupDrawer function
        observeMenu()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                    return
                }

                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }

                doubleBackToExitPressedOnce = true
                android.widget.Toast.makeText(this@MainActivity, "Please click BACK again to exit", android.widget.Toast.LENGTH_SHORT).show()

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
        
        menuItems.forEach { item ->
            val menuItem = menu.add(0, item.id, 0, item.title)
            // Set icon based on type or title if needed
            when {
                item.title.contains("Women", ignoreCase = true) -> menuItem.setIcon(R.drawable.ic_person_outline)
                item.title.contains("Men", ignoreCase = true) -> menuItem.setIcon(R.drawable.ic_person_outline)
                item.title.contains("Gift", ignoreCase = true) -> menuItem.setIcon(R.drawable.ic_gift)
                item.title.contains("Unisex", ignoreCase = true) -> menuItem.setIcon(R.drawable.ic_unisex)
                item.title.contains("Best", ignoreCase = true) -> menuItem.setIcon(R.drawable.ic_thumbs_up)
                else -> menuItem.setIcon(R.drawable.ic_offer)
            }
        }
        
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val item = menuItems.find { it.id == menuItem.itemId }
            item?.let {
                val bundle = Bundle().apply {
                    putString("url", it.url)
                    putString("title", it.title)
                }
                navController.navigate(R.id.webViewFragment, bundle)
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
        val navView = binding.navView
        
        // Handle Header Clicks
        val headerView = navView.getHeaderView(0)
        
        headerView.findViewById<View>(R.id.ivCloseDrawer)?.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
        
        headerView.findViewById<View>(R.id.btnMyOrders)?.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            // Navigate to Orders
            // navController.navigate(R.id.nav_orders) 
        }
        
        headerView.findViewById<View>(R.id.btnTrackOrders)?.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            // Navigate to Track Orders
        }

        // Handle Logout
        val btnLogout = binding.root.findViewById<View>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            // Perform Logout
            // viewModel.logout()
        }
    }
    /**
     * Handle up navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
