package com.example.postsapplication.di

import android.content.Context
import com.example.postsapplication.BuildConfig
import com.example.postsapplication.data.remote.PostApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val HTTP_CACHE_SIZE = 10L * 1024 * 1024 // 10 mb
    private const val TIMEOUT_SECONDS = 15L

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideBaseOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS },
                    )
                }
            }
            .build()

    // Same client plus an HTTP cache that stores the ETag. Images use the base client (Coil caches them).
    @Provides
    @Singleton
    @ApiClient
    fun provideApiOkHttpClient(
        @ApplicationContext context: Context,
        baseClient: OkHttpClient,
    ): OkHttpClient =
        baseClient.newBuilder()
            .cache(Cache(context.cacheDir.resolve("http_cache"), HTTP_CACHE_SIZE))
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(@ApiClient client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun providePostApi(retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)
}
