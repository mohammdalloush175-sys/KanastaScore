package com.mohammdalloush.kanastascore.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mohammdalloush.kanastascore.BuildConfig
import com.mohammdalloush.kanastascore.MainActivity
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentSettingsBinding
import com.mohammdalloush.kanastascore.engine.*
import com.mohammdalloush.kanastascore.utils.StorageHelper
import com.mohammdalloush.kanastascore.viewmodel.GameViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveBackupToFile(uri)
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadBackupFromFile(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = (activity as MainActivity).viewModel

        setupThemeSelection(viewModel)
        
        binding.btnStatistics.setOnClickListener {
            (activity as MainActivity).replaceFragment(StatisticsFragment(), true)
        }
        
        binding.btnRules.setOnClickListener {
            (activity as MainActivity).replaceFragment(RulesFragment(), true)
        }

        binding.btnUpdates.setOnClickListener {
            (activity as MainActivity).replaceFragment(UpdatesFragment(), true)
        }
        
        binding.btnExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "KanastaScore_Backup_${System.currentTimeMillis()}.json")
            }
            exportLauncher.launch(intent)
        }
        
        binding.btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            importLauncher.launch(intent)
        }
        
        binding.btnDeleteAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_all_data)
                .setMessage(R.string.confirm_delete_all_msg)
                .setPositiveButton(R.string.yes) { _, _ ->
                    StorageHelper.clearAllData(requireContext())
                    viewModel.setMatchesStore(MatchesStore())
                    Toast.makeText(requireContext(), R.string.delete_match, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
        
        binding.tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
    }

    private fun setupThemeSelection(viewModel: GameViewModel) {
        when (viewModel.appThemeMode.value) {
            AppThemeMode.SYSTEM -> binding.rbThemeSystem.isChecked = true
            AppThemeMode.LIGHT -> binding.rbThemeLight.isChecked = true
            AppThemeMode.DARK -> binding.rbThemeDark.isChecked = true
            null -> binding.rbThemeSystem.isChecked = true
        }
        
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rb_theme_system -> AppThemeMode.SYSTEM
                R.id.rb_theme_light -> AppThemeMode.LIGHT
                R.id.rb_theme_dark -> AppThemeMode.DARK
                else -> AppThemeMode.SYSTEM
            }
            if (mode != viewModel.appThemeMode.value) {
                viewModel.setAppThemeMode(mode)
                StorageHelper.saveThemeMode(requireContext(), mode)
                (activity as MainActivity).updateAppTheme(mode)
            }
        }
    }

    private fun saveBackupToFile(uri: android.net.Uri) {
        val viewModel = (activity as MainActivity).viewModel
        val json = StorageHelper.exportBackup(viewModel.matchesStore.value ?: MatchesStore())
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { 
                it.write(json.toByteArray())
            }
            Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadBackupFromFile(uri: android.net.Uri) {
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) {
                val importedStore = StorageHelper.importBackup(json)
                if (importedStore != null) {
                    showImportOptions(importedStore)
                } else {
                    Toast.makeText(requireContext(), R.string.import_failed, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImportOptions(importedStore: MatchesStore) {
        val viewModel = (activity as MainActivity).viewModel
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.backup_import)
            .setMessage(getString(R.string.import_summary, importedStore.matches.size))
            .setPositiveButton(R.string.merge_data) { _, _ ->
                val current = viewModel.matchesStore.value ?: MatchesStore()
                val mergedMatches = current.matches.toMutableList()
                importedStore.matches.forEach { importedMatch ->
                    if (mergedMatches.none { it.id == importedMatch.id }) {
                        mergedMatches.add(importedMatch)
                    }
                }
                val newStore = current.copy(matches = mergedMatches.sortedByDescending { it.updatedAt })
                viewModel.setMatchesStore(newStore)
                StorageHelper.saveMatchesStore(requireContext(), newStore)
                Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton(R.string.replace_data) { _, _ ->
                viewModel.setMatchesStore(importedStore)
                StorageHelper.saveMatchesStore(requireContext(), importedStore)
                Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
