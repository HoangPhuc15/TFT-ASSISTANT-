package com.tftassistant.app.data

import android.content.Context
import com.tftassistant.app.data.local.MetaSnapshotDao
import com.tftassistant.app.data.local.MetaSnapshotEntity
import com.tftassistant.app.data.model.MetaSnapshot
import com.tftassistant.app.data.remote.MetaApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetaRepository @Inject constructor(
    private val apiService: MetaApiService,
    private val metaSnapshotDao: MetaSnapshotDao,
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun bootstrapSnapshot(): MetaSnapshot = withContext(ioDispatcher) {
        val cached = metaSnapshotDao.latestSnapshot()?.let { entity ->
            runCatching { json.decodeFromString(MetaSnapshot.serializer(), entity.payloadJson) }.getOrNull()
        }
        cached ?: readSeedSnapshot().also { persistSnapshot(it) }
    }

    suspend fun refreshSnapshot(): MetaSnapshot = withContext(ioDispatcher) {
        val snapshot = apiService.fetchMeta()
        persistSnapshot(snapshot)
        snapshot
    }

    suspend fun latestSnapshot(): MetaSnapshot = withContext(ioDispatcher) {
        metaSnapshotDao.latestSnapshot()?.let { entity ->
            json.decodeFromString(MetaSnapshot.serializer(), entity.payloadJson)
        } ?: bootstrapSnapshot()
    }

    private suspend fun persistSnapshot(snapshot: MetaSnapshot) {
        val jsonPayload = json.encodeToString(MetaSnapshot.serializer(), snapshot)
        metaSnapshotDao.upsert(
            MetaSnapshotEntity(
                version = snapshot.version,
                payloadJson = jsonPayload,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun readSeedSnapshot(): MetaSnapshot {
        val assetManager = context.assets
        val inputStream = assetManager.open("meta/bootstrap_meta.json")
        val jsonContent = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        return json.decodeFromString(MetaSnapshot.serializer(), jsonContent)
    }
}
