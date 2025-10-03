package live.uwapps.mpvkt.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import live.uwapps.mpvkt.ui.player.PlayerActivity

object PlayerLauncher {
  fun playFile(filepath: String, context: Context) {
    val uri: Uri = if (filepath.startsWith("/")) Uri.fromFile(File(filepath)) else filepath.toUri()
    val i = Intent(Intent.ACTION_VIEW, uri)
    i.putExtra("uri", filepath)
    i.setClass(context, PlayerActivity::class.java)
    context.startActivity(i)
  }
}

