package com.mohammdalloush.kanastascore

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.databinding.ActivityMainBinding
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
}
