package com.dogballs.audiodecoder;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder;
import com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.module.AppGlideModule;
import com.dogballs.audiodocoder.AudioDecoder;
import com.dogballs.audiodocoder.AudioVideoDecoder;

@GlideModule
public class AlbumPicModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        Resources resources = context.getResources();
        BitmapPool bitmapPool = glide.getBitmapPool();
        Downsampler downsampler = new Downsampler(
                registry.getImageHeaderParsers(),
                resources.getDisplayMetrics(),
                bitmapPool,
                glide.getArrayPool()
        );
        ByteBufferBitmapDecoder byteBufferBitmapDecoder = new ByteBufferBitmapDecoder(downsampler);
        // only audio file
        ResourceDecoder<AssetFileDescriptor, Bitmap> asset = AudioDecoder.Companion.asset(byteBufferBitmapDecoder);
        ResourceDecoder<ParcelFileDescriptor, Bitmap> parcel = AudioDecoder.Companion.parcel(byteBufferBitmapDecoder);

        // both audio and video file this will be better
//        ResourceDecoder<AssetFileDescriptor, Bitmap> asset = AudioVideoDecoder.asset(bitmapPool,byteBufferBitmapDecoder);
//        ResourceDecoder<ParcelFileDescriptor, Bitmap> parcel = AudioVideoDecoder.parcel(bitmapPool,byteBufferBitmapDecoder);
        registry.prepend(
                Registry.BUCKET_BITMAP_DRAWABLE, ParcelFileDescriptor.class,
                BitmapDrawable.class, new BitmapDrawableDecoder<>(resources, parcel))
                .prepend(
                        Registry.BUCKET_BITMAP,
                        AssetFileDescriptor.class,
                        Bitmap.class,
                        asset
                )
                .prepend(
                        Registry.BUCKET_BITMAP,
                        ParcelFileDescriptor.class,
                        Bitmap.class,
                        parcel
                );
    }


}
