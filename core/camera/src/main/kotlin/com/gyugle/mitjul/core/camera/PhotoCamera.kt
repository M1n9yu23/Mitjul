/*
 * Copyright 2026 MinGyu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gyugle.mitjul.core.camera

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import co.touchlab.kermit.Logger
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

private val CAPTURE_SIZE = Size(1920, 1440)

class PhotoCamera {
    private val preview = Preview.Builder().build()
    private val imageCapture = ImageCapture.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(CAPTURE_SIZE, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER),
                )
                .build(),
        )
        .build()
    private var provider: ProcessCameraProvider? = null

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    internal val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    init {
        preview.setSurfaceProvider { request -> _surfaceRequest.value = request }
    }

    internal suspend fun bind(context: Context, lifecycleOwner: LifecycleOwner): EmptyResult<CameraError> = try {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        provider = cameraProvider
        if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            Result.Success(Unit)
        } else {
            Result.Error(CameraError.NO_CAMERA)
        }
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "카메라를 켜지 못했다: ${e::class.simpleName}" }
        Result.Error(CameraError.UNAVAILABLE)
    }

    internal fun unbind() {
        provider?.unbind(preview, imageCapture)
        _surfaceRequest.value = null
    }

    suspend fun takePicture(file: File, rotation: Int): EmptyResult<CameraError> {
        imageCapture.targetRotation = rotation
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        return suspendCancellableCoroutine { continuation ->
            val callback = object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    continuation.resume(Result.Success(Unit))
                }

                override fun onError(exception: ImageCaptureException) {
                    Logger.e { "사진을 찍지 못했다: ${exception.imageCaptureError}" }
                    continuation.resume(Result.Error(CameraError.CAPTURE_FAILED))
                }
            }
            imageCapture.takePicture(options, Dispatchers.Default.asExecutor(), callback)
        }
    }
}
