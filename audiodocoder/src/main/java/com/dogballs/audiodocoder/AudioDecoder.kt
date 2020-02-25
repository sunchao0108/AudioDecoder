package com.dogballs.audiodocoder

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import java.io.IOException
import java.nio.ByteBuffer

class AudioDecoder<T>(
    private val initializer: MediaMetadataRetrieverInitializer<T>,
    private val byteBufferBitmapDecoder: ResourceDecoder<ByteBuffer, Bitmap>
) : ResourceDecoder<T, Bitmap> {

    companion object {
        private const val TAG = "AudioDecoder"
        fun asset(byteBufferBitmapDecoder: ResourceDecoder<ByteBuffer, Bitmap>): ResourceDecoder<AssetFileDescriptor, Bitmap> {
            return AudioDecoder(AssetFileDescriptorInitializer(), byteBufferBitmapDecoder)
        }

        fun parcel(byteBufferBitmapDecoder: ResourceDecoder<ByteBuffer, Bitmap>): ResourceDecoder<ParcelFileDescriptor, Bitmap> {
            return AudioDecoder(ParcelFileDescriptorInitializer(), byteBufferBitmapDecoder)
        }
    }

    /**
     * Calling setDataSource is expensive so avoid doing so unless we're actually called.
     * if there are both audio and video files and prepend this decoder,will slow down videoDecoder 50-100ms
     */
    override fun handles(source: T, options: Options) = true

    @Throws(IOException::class)
    override fun decode(source: T, width: Int, height: Int, options: Options): Resource<Bitmap>? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        Log.i(TAG,"AudioDecoder ========== decode")
        val result = try {
            initializer.initialize(mediaMetadataRetriever, source)
            mediaMetadataRetriever.embeddedPicture
        } catch (e: RuntimeException) { // MediaMetadataRetriever APIs throw generic runtime exceptions when given invalid data.
            throw IOException(e)
        } finally {
            mediaMetadataRetriever.release()
        } ?: return null
        return byteBufferBitmapDecoder.decode(ByteBuffer.wrap(result), width, height, options)
    }

    @VisibleForTesting
    interface MediaMetadataRetrieverInitializer<T> {
        fun initialize(retriever: MediaMetadataRetriever, data: T)
    }

    private class AssetFileDescriptorInitializer :
        MediaMetadataRetrieverInitializer<AssetFileDescriptor> {
        override fun initialize(retriever: MediaMetadataRetriever, data: AssetFileDescriptor) =
            retriever.setDataSource(data.fileDescriptor, data.startOffset, data.length)
    }

    private class ParcelFileDescriptorInitializer :
        MediaMetadataRetrieverInitializer<ParcelFileDescriptor> {
        override fun initialize(retriever: MediaMetadataRetriever, data: ParcelFileDescriptor) =
            retriever.setDataSource(data.fileDescriptor)
    }
}