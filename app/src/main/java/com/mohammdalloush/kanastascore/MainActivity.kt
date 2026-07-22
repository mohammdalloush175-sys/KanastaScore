package com.mohammdalloush.kanastascore

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.databinding.ActivityMainBinding
import com.mohammdalloush.kanastascore.engine.AppThemeMode
import com.mohammdalloush.kanastascore.ui.*
import com.mohammdalloush.kanastascore.utils.StorageHelper
import com.mohammdalloush.kanastascore.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val savedTheme = StorageHelper.loadThemeMode(this)
        applyTheme(savedTheme)
        viewModel.setAppThemeMode(savedTheme)

        val store = StorageHelper.loadMatchesStore(this)
        viewModel.setMatchesStore(store)

        if (savedInstanceState == null) {
            replaceFragment(MatchesFragment())
        }

        viewModel.matchesStore.observe(this) { store ->
            StorageHelper.saveMatchesStore(this, store)
        }

        viewModel.appThemeMode.observe(this) { mode ->
            StorageHelper.saveThemeMode(this, mode)
            applyTheme(mode)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutouts = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(top = Math.max(bars.top, cutouts.top))
            insets
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = bars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                when (fragment) {
                    is RoundInputFragment, is MatchSetupFragment, is HistoryFragment -> {
                        viewModel.saveCurrentDraft()
                        replaceFragment(MatchesFragment())
                    }
                    is ResultFragment -> {
                        replaceFragment(RoundInputFragment())
                    }
                    is SettingsFragment, is StatisticsFragment, is RulesFragment, is MatchEndFragment -> {
                        if (supportFragmentManager.backStackEntryCount > 0) {
                            supportFragmentManager.popBackStack()
                        } else {
                            replaceFragment(MatchesFragment())
                        }
                    }
                    is MatchesFragment -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                    else -> {
                        if (supportFragmentManager.backStackEntryCount > 0) {
                            supportFragmentManager.popBackStack()
                        } else {
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                            isEnabled = true
                        }
                    }
                }
            }
        })

        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbar()
        }
    }

    private fun applyTheme(mode: AppThemeMode) {
        val nightMode = when (mode) {
            AppThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun updateAppTheme(mode: AppThemeMode) {
        applyTheme(mode)
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        // Wait for commit to update toolbar
        binding.root.post { updateToolbar() }
    }

    private fun updateToolbar() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        title = when (fragment) {
            is MatchesFragment -> getString(R.string.matches_title)
            is MatchSetupFragment -> getString(R.string.new_match)
            is RoundInputFragment -> {
                val match = viewModel.getActiveMatch()
                val nextRound = (match?.gameState?.rounds?.maxByOrNull { it.roundNumber }?.roundNumber ?: 0) + 1
                getString(R.string.round_number_label, nextRound)
            }
            is ResultFragment -> getString(R.string.preview_title)
            is HistoryFragment -> getString(R.string.history_title)
            is SettingsFragment -> getString(R.string.settings_title)
            is StatisticsFragment -> getString(R.string.statistics_label)
            is RulesFragment -> getString(R.string.rules_title)
            is MatchEndFragment -> getString(R.string.match_ended_title)
            else -> getString(R.string.app_name)
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(fragment !is MatchesFragment)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        menu?.findItem(R.id.action_new_match)?.isVisible = fragment !is MatchSetupFragment
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                showThemeSelectionDialog()
                true
            }
            R.id.action_new_match -> {
                replaceFragment(MatchSetupFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val currentMode = viewModel.appThemeMode.value ?: AppThemeMode.SYSTEM
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.theme_selection)
            .setSingleChoiceItems(themes, currentMode.ordinal) { dialog, which ->
                val newMode = AppThemeMode.entries[which]
                viewModel.setAppThemeMode(newMode)
                dialog.dismiss()
            }
            .show()
    }
}
