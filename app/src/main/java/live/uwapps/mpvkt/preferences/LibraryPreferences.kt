package live.uwapps.mpvkt.preferences

import live.uwapps.mpvkt.preferences.preference.PreferenceStore
import live.uwapps.mpvkt.preferences.preference.getEnum
import live.uwapps.mpvkt.domain.models.SortOption
import live.uwapps.mpvkt.domain.models.FilterOption
import live.uwapps.mpvkt.domain.models.GroupOption
import live.uwapps.mpvkt.domain.models.ViewMode

class LibraryPreferences(preferenceStore: PreferenceStore) {
  val sortOption = preferenceStore.getEnum("library_sort_option", SortOption.DATE_ADDED_DESC)
  val filterOption = preferenceStore.getEnum("library_filter_option", FilterOption.ALL)
  val groupOption = preferenceStore.getEnum("library_group_option", GroupOption.NONE)
  val viewMode = preferenceStore.getEnum("library_view_mode", ViewMode.GRID)
  val selectedFolderPath = preferenceStore.getString("library_selected_folder_path", "")

  // For scroll position restoration
  val lastGridScrollIndex = preferenceStore.getInt("library_last_grid_scroll_index", 0)
  val lastGridScrollOffset = preferenceStore.getInt("library_last_grid_scroll_offset", 0)
  val lastListScrollIndex = preferenceStore.getInt("library_last_list_scroll_index", 0)
  val lastListScrollOffset = preferenceStore.getInt("library_last_list_scroll_offset", 0)
}

