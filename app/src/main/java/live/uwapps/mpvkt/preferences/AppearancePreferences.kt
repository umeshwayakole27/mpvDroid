package live.uwapps.mpvkt.preferences

import android.os.Build
import live.uwapps.mpvkt.preferences.preference.PreferenceStore
import live.uwapps.mpvkt.preferences.preference.getEnum
import live.uwapps.mpvkt.ui.theme.DarkMode

class AppearancePreferences(preferenceStore: PreferenceStore) {
  val darkMode = preferenceStore.getEnum("dark_mode", DarkMode.System)
  val materialYou = preferenceStore.getBoolean("material_you", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

  // Show a dot on Settings tab until the user visits it once
  val settingsBadgeUnseen = preferenceStore.getBoolean("settings_badge_unseen", true)

  // Last app version seen by the user
  val lastSeenVersion = preferenceStore.getString("last_seen_version", "")
}
