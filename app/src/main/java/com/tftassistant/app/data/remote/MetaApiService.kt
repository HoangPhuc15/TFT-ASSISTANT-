package com.tftassistant.app.data.remote

import com.tftassistant.app.data.model.MetaSnapshot
import retrofit2.http.GET

interface MetaApiService {
    @GET("v1/tft/meta.json")
    suspend fun fetchMeta(): MetaSnapshot
}
