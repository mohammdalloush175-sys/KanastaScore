# Update feature test plan

These modified builds include the update checker. Builds that were published before this code existed cannot retroactively display update notifications.

## Correct end-to-end test

1. Build and install the modified baseline apps:
   - LockNet `1.0.0` (`versionCode 2`)
   - KanastaScore `1.0.0` (`versionCode 3`)
2. Open **Settings → Updates** in each app.
3. Grant notification permission and leave automatic checks enabled.
4. Confirm that the current GitHub releases do not appear newer than `1.0.0`.
5. Create the next release with a higher tag, for example `v1.1.0`, and attach its APK.
6. Tap **Check now**, reopen the app, or wait for the scheduled daily check.
7. Confirm that:
   - the new version and release notes appear;
   - the download button opens the APK asset or release page;
   - an Android update notification appears once for that release tag.

Android requires user confirmation before installing a downloaded APK. Silent installation is intentionally not implemented.
