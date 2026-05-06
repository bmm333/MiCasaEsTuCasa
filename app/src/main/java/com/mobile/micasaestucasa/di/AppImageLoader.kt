package com.mobile.micasaestucasa.di

import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppImageLoader {
    fun initialize(context: Context) {
        SingletonImageLoader.setSafe {
            buildImageLoader(context)
        }
    }

    private fun buildImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                // Utilizziamo OkHttp per gestire il fetching di rete (fondamentale per URL Firebase)
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { buildOkHttpClient() }
                    )
                )
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                // Abilitiamo la cache su disco per evitare consumi eccessivi con Firebase
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            }
            // Politiche di cache: abilita sia lettura che scrittura
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }

    private fun buildOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            // Se in futuro dovessi aggiungere header particolari per Firebase, lo farai qui
            .build()
}