package io.yocli.yo.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dev.zacsweers.moshix.reflect.MetadataKotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import timber.log.Timber

@JsonClass(generateAdapter = true)
data class RegisterBody(
    @Json(name = "pc_token") val scanToken: String,
    @Json(name = "apns_device_token") val fcmToken: String,
    @Json(name = "is_firebase") val isAndroidRegistering: Boolean = true,
)

interface Api {
    @POST
    suspend fun register(@Url url: String, @Body body: RegisterBody): Response<Unit>
}

class ApiClient() {
    private val api: Api

    init {
        val moshi = Moshi.Builder()
            .add(MetadataKotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient()
                    .newBuilder()
                    .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BODY })
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://api.yocli.io/")
            .validateEagerly(true)
            .build()
        api = retrofit.create()
    }

    suspend fun register(url: String, scanToken: String, fcmToken: String) {
        val body = RegisterBody(scanToken, fcmToken, true)
        val response = api.register("$url/register_for_notifications", body)
        return response.body() ?: Unit
    }
}