package com.ctw.mediaselector

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class AppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .disallowHardwareConfig()
                .timeout(10_000)
        )

        val calculator = MemorySizeCalculator.Builder(context)
            .setMemoryCacheScreens(2f)
            .build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))

        builder.setDiskCache(
            InternalCacheDiskCacheFactory(
                context,
                "glide_cache",
                100 * 1024 * 1024 // 100MB
            )
        )
    }
    
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // 注册自定义组件（可选）
    }

    override fun isManifestParsingEnabled() = false
} 