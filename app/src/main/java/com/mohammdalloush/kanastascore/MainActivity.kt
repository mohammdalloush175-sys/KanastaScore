package com.mohammdalloush.kanastascore

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.databinding.ActivityMainBinding
import com.mohammdalloush.kanastascore.engine.AppThemeMode
import com.mohammdalloush.kanastascore.ui.HistoryFragment
import com.mohammdalloush.kanastascore.ui.MatchSetupFragment
import com.mohammdalloush.kanastascore.ui.ResultFragment
import com.mohammdalloush.kanastascore.ui.RoundInputFragment
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

        val savedTheme = StorageHelper.loadThemeMode(this)
        applyTheme(savedTheme)
        viewModel.setAppThemeMode(savedTheme)

        val savedGame = StorageHelper.loadGameState(this)
        viewModel.setGameState(savedGame)

        if (savedInstanceState == null) {
            if (savedGame == null) {
                replaceFragment(MatchSetupFragment())
            } else {
                replaceFragment(RoundInputFragment())
            }
        }

        viewModel.gameState.observe(this) { state ->
            if (state != null) {
                StorageHelper.saveGameState(this, state)
            }
        }

        viewModel.appThemeMode.observe(this) { mode ->
            StorageHelper.saveThemeMode(this, mode)
            applyTheme(mode)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, bars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is MatchSetupFragment && viewModel.gameState.value != null) {
                    replaceFragment(RoundInputFragment())
                } else if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun applyTheme(mode: AppThemeMode) {
        val nightMode = when (mode) {
            AppThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
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
