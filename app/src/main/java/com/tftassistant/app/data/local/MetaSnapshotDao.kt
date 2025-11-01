package com.tftassistant.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MetaSnapshotDao {
    @Query("SELECT * FROM meta_snapshots ORDER BY updated_at DESC LIMIT 1")
    suspend fun latestSnapshot(): MetaSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MetaSnapshotEntity)
}
