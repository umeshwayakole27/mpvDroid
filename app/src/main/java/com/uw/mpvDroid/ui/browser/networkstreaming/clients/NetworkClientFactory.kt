package com.uw.mpvDroid.ui.browser.networkstreaming.clients

import com.uw.mpvDroid.domain.network.NetworkConnection
import com.uw.mpvDroid.domain.network.NetworkProtocol

object NetworkClientFactory {
  fun createClient(connection: NetworkConnection): NetworkClient =
    when (connection.protocol) {
      NetworkProtocol.SMB -> SmbClient(connection)
      NetworkProtocol.FTP -> FtpClient(connection)
      NetworkProtocol.WEBDAV -> WebDavClient(connection)
    }
}
