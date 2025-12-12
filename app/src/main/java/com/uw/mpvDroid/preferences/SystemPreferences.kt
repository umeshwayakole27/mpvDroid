package com.uw.mpvDroid.preferences

import com.uw.mpvDroid.preferences.preference.PreferenceStore

class SystemPreferences(
  preferenceStore: PreferenceStore,
) {
  /**
   * Tracks whether the app has requested storage permissions on first start.
   * Used to determine if permission dialog should be shown automatically.
   */
  val permissionsRequestedAtFirstStart = preferenceStore.getBoolean(
    "permissions_requested_at_first_start",
    false,
  )
}
