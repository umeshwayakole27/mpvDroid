package live.uwapps.mpvkt.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migrations: Array<Migration> = arrayOf(
  MIGRATION1to2,
  MIGRATION2to3,
  MIGRATION3to4,
  MIGRATION4to5,
  MIGRATION5to6,
)

private object MIGRATION1to2 : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    listOf("subDelay", "secondarySubDelay", "audioDelay").forEach {
      db.execSQL("ALTER TABLE PlaybackStateEntity ADD COLUMN $it INTEGER NOT NULL DEFAULT 0")
    }
    db.execSQL("ALTER TABLE PlaybackStateEntity ADD COLUMN subSpeed REAL NOT NULL DEFAULT 0")
  }
}

private object MIGRATION2to3 : Migration(2, 3) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE PlaybackStateEntity ADD COLUMN playbackSpeed REAL NOT NULL DEFAULT 0")
  }
}

private object MIGRATION3to4 : Migration(3, 4) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `CustomButtonEntity` (
        `id` INTEGER NOT NULL,
        `title` TEXT NOT NULL, 
        `content` TEXT NOT NULL, 
        `index` INTEGER NOT NULL, 
        PRIMARY KEY(`id`)
      )
      """.trimIndent()
    )
  }
}

private object MIGRATION4to5 : Migration(4, 5) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
        ALTER TABLE CustomButtonEntity ADD COLUMN longPressContent TEXT NOT NULL DEFAULT ''
      """.trimIndent()
    )
  }
}

private object MIGRATION5to6 : Migration(5, 6) {
  override fun migrate(db: SupportSQLiteDatabase) {
    // Create videos table
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `videos` (
        `path` TEXT NOT NULL,
        `title` TEXT NOT NULL,
        `displayName` TEXT NOT NULL,
        `duration` INTEGER NOT NULL,
        `size` INTEGER NOT NULL,
        `dateAdded` INTEGER NOT NULL,
        `dateModified` INTEGER NOT NULL,
        `mimeType` TEXT NOT NULL,
        `resolution` TEXT,
        `thumbnail` TEXT,
        `folder` TEXT NOT NULL,
        `folderName` TEXT NOT NULL,
        `isVideo` INTEGER NOT NULL DEFAULT 1,
        `format` TEXT,
        `aspectRatio` TEXT,
        `frameRate` REAL,
        `bitrate` INTEGER,
        `hasSubtitles` INTEGER NOT NULL DEFAULT 0,
        `isWatched` INTEGER NOT NULL DEFAULT 0,
        `isFavorite` INTEGER NOT NULL DEFAULT 0,
        `lastWatchedTime` INTEGER,
        `watchProgress` REAL NOT NULL DEFAULT 0.0,
        PRIMARY KEY(`path`)
      )
      """.trimIndent()
    )

    // Create folders table
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `folders` (
        `path` TEXT NOT NULL,
        `name` TEXT NOT NULL,
        `videoCount` INTEGER NOT NULL DEFAULT 0,
        `totalDuration` INTEGER NOT NULL DEFAULT 0,
        `totalSize` INTEGER NOT NULL DEFAULT 0,
        `lastScanned` INTEGER NOT NULL DEFAULT 0,
        `isHidden` INTEGER NOT NULL DEFAULT 0,
        PRIMARY KEY(`path`)
      )
      """.trimIndent()
    )
  }
}
