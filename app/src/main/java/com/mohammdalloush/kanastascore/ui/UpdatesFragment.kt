package com.mohammdalloush.kanastascore.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mohammdalloush.kanastascore.BuildConfig
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.databinding.FragmentUpdatesBinding
import com.mohammdalloush.kanastascore.update.GitHubUpdateChecker
import com.mohammdalloush.kanastascore.update.UpdateCheckResult
import com.mohammdalloush.kanastascore.update.UpdateNotificationManager
import com.mohammdalloush.kanastascore.update.UpdatePreferences
import com.mohammdalloush.kanastascore.update.UpdateRelease
import com.mohammdalloush.kanastascore.update.UpdateScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

class UpdatesFragment : Fragment() {
    private var _binding: FragmentUpdatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: UpdatePreferences
    private var availableRelease: UpdateRelease? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val currentBinding = _binding ?: return@registerForActivityResult
        currentBinding.switchAutoUpdates.isChecked = granted
        preferences.automaticChecksEnabled = granted
        if (granted) UpdateScheduler.schedule(requireContext())
        else UpdateScheduler.cancel(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUpdatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = UpdatePreferences(requireContext())
        binding.tvCurrentVersion.text = BuildConfig.VERSION_NAME
        binding.switchAutoUpdates.isChecked = preferences.automaticChecksEnabled
        showLastCheck()

        binding.switchAutoUpdates.setOnCheckedChangeListener { _, enabled ->
            if (enabled && needsNotificationPermission()) {
                binding.switchAutoUpdates.isChecked = false
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                preferences.automaticChecksEnabled = enabled
                if (enabled) UpdateScheduler.schedule(requireContext())
                else UpdateScheduler.cancel(requireContext())
            }
        }

        binding.btnCheckUpdates.setOnClickListener { checkForUpdates() }
        binding.btnDownloadUpdate.setOnClickListener {
            availableRelease?.let { release ->
                openUrl(release.downloadUrl ?: release.releaseUrl)
            }
        }
        binding.btnReleases.setOnClickListener { openUrl(GitHubUpdateChecker.RELEASES_URL) }
        binding.btnGithub.setOnClickListener { openUrl(GitHubUpdateChecker.REPOSITORY_URL) }
        binding.btnTestNotification.isVisible = BuildConfig.DEBUG
        binding.btnTestNotification.setOnClickListener {
            UpdateNotificationManager.showUpdateAvailable(
                requireContext(),
                UpdateRelease(
                    tagName = "v1.1.0-test",
                    version = "1.1.0-test",
                    title = "اختبار إشعار التحديث",
                    notes = "هذا إشعار اختباري من نسخة Debug، وليس إصداراً منشوراً.",
                    publishedAt = "",
                    releaseUrl = GitHubUpdateChecker.RELEASES_URL,
                    downloadUrl = null,
                    prerelease = true,
                ),
            )
        }

        checkForUpdates()
    }

    private fun checkForUpdates() {
        binding.progressChecking.isVisible = true
        binding.btnCheckUpdates.isEnabled = false
        binding.tvUpdateStatus.setText(R.string.checking_updates)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
            }
            preferences.lastCheckAt = System.currentTimeMillis()
            showLastCheck()
            binding.progressChecking.isVisible = false
            binding.btnCheckUpdates.isEnabled = true
            renderResult(result)
        }
    }

    private fun renderResult(result: UpdateCheckResult) {
        availableRelease = null
        binding.btnDownloadUpdate.isVisible = false
        binding.tvReleaseNotes.isVisible = false

        when (result) {
            is UpdateCheckResult.UpdateAvailable -> {
                availableRelease = result.release
                binding.tvLatestVersion.text = result.release.tagName
                binding.tvUpdateStatus.setText(R.string.update_available)
                binding.tvUpdateStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.positiveScore),
                )
                binding.tvReleaseNotes.text = result.release.notes.ifBlank {
                    getString(R.string.update_available_default_notes)
                }
                binding.tvReleaseNotes.isVisible = true
                binding.btnDownloadUpdate.isVisible = true
            }

            is UpdateCheckResult.UpToDate -> {
                binding.tvLatestVersion.text = result.latestRelease?.tagName
                    ?: getString(R.string.no_published_releases)
                binding.tvUpdateStatus.setText(R.string.app_up_to_date)
                binding.tvUpdateStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.positiveScore),
                )
            }

            is UpdateCheckResult.Failure -> {
                binding.tvLatestVersion.setText(R.string.unknown_version)
                binding.tvUpdateStatus.text = getString(R.string.update_check_failed, result.message)
                binding.tvUpdateStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.negativeScore),
                )
            }
        }
    }

    private fun showLastCheck() {
        val lastCheck = preferences.lastCheckAt
        binding.tvLastCheck.isVisible = lastCheck > 0L
        if (lastCheck > 0L) {
            binding.tvLastCheck.text = getString(
                R.string.last_update_check,
                DateFormat.getDateTimeInstance().format(Date(lastCheck)),
            )
        }
    }

    private fun needsNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
