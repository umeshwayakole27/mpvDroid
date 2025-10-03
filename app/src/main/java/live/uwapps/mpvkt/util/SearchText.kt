// filepath: /home/umesh/UserData/mpvKt/app/src/main/java/live/uwapps/mpvkt/util/SearchText.kt
package live.uwapps.mpvkt.util

import java.text.Normalizer
import java.util.Locale

object SearchText {
  /**
   * Normalize text for search by:
   * - lowercasing (Locale.ROOT)
   * - removing diacritics (accents)
   * - removing all non-alphanumeric characters (spaces, punctuation, symbols)
   */
  fun normalize(input: String?): String {
    if (input.isNullOrEmpty()) return ""
    val lower = input.lowercase(Locale.ROOT)
    val decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD)
    val strippedAccents = decomposed.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    // Keep only letters and digits so we ignore spaces and symbols during search
    return strippedAccents.replace("[^a-z0-9]".toRegex(), "")
  }
}

