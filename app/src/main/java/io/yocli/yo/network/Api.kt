package io.yocli.yo.network

import io.yocli.yo.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import timber.log.Timber

const val JSON_TEMPLATE = """{ "pc_token": "%s", "apns_device_token": "%s", "is_firebase": true }"""

interface Api {
    @POST
    suspend fun register(@Url url: String, @Body body: RequestBody): Response<Unit>
}

class ApiClient() {
    private val api: Api

    init {
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient()
                    .newBuilder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = when (BuildConfig.DEBUG) {
                            true -> Level.BODY
                            else -> Level.BASIC
                        }
                    })
                    .build()
            )
            .baseUrl("https://api.yocli.io/")
            .validateEagerly(true)
            .build()
        api = retrofit.create()
    }

    suspend fun register(url: String, scanToken: String, fcmToken: String) {
        val json = JSON_TEMPLATE.format(scanToken, fcmToken)
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        return runCatching { api.register("$url/register_for_notifications", body).body() }
            .onFailure { Timber.e(it, """Error registering with backend for scan token "%s" """, scanToken) }
            .getOrNull()
            ?: Unit
    }
}