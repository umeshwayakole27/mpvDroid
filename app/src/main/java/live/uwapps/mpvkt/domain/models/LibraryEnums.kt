package live.uwapps.mpvkt.domain.models

enum class SortOption(val displayName: String) {
  NAME_ASC("Name (A-Z)"),
  NAME_DESC("Name (Z-A)"),
  DATE_ADDED_ASC("Date Added (Oldest)"),
  DATE_ADDED_DESC("Date Added (Newest)"),
  DATE_MODIFIED_ASC("Modified (Oldest)"),
  DATE_MODIFIED_DESC("Modified (Newest)"),
  SIZE_ASC("Size (Smallest)"),
  SIZE_DESC("Size (Largest)"),
  DURATION_ASC("Duration (Shortest)"),
  DURATION_DESC("Duration (Longest)"),
  LAST_WATCHED_ASC("Last Watched (Oldest)"),
  LAST_WATCHED_DESC("Last Watched (Recent)")
}

enum class FilterOption(val displayName: String) {
  ALL("All Videos"),
  FAVORITES("Favorites"),
  WATCHED("Watched"),
  UNWATCHED("Unwatched"),
  PARTIALLY_WATCHED("Continue Watching"),
  RECENT("Recently Added")
}

enum class GroupOption(val displayName: String) {
  NONE("No Grouping"),
  FOLDER("By Folder"),
  FORMAT("By Format"),
  DURATION("By Duration"),
  SIZE("By File Size"),
  DATE_ADDED("By Date Added")
}

enum class ViewMode {
  GRID,
  LIST
}
