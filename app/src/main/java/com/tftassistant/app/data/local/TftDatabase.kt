package com.tftassistant.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MetaSnapshotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TftDatabase : RoomDatabase() {
    abstract fun metaSnapshotDao(): MetaSnapshotDao
}
