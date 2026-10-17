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

package com.gyugle.mitjul.core.presentation.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PermissionController internal constructor(private val activity: ComponentActivity) {

    private val requestMutex = Mutex()

    fun getPermissionState(permission: Permission): PermissionState {
        val name = permission.toManifestName() ?: return PermissionState.GRANTED
        return when {
            activity.checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
            activity.shouldShowRequestPermissionRationale(name) -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
    }

    suspend fun requestPermission(permission: Permission): PermissionState = requestMutex.withLock {
        val name = permission.toManifestName() ?: return@withLock PermissionState.GRANTED
        if (activity.checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED) {
            return@withLock PermissionState.GRANTED
        }
        when {
            requestFromSystem(name) -> PermissionState.GRANTED
            activity.shouldShowRequestPermissionRationale(name) -> PermissionState.DENIED
            else -> PermissionState.PERMANENTLY_DENIED
        }
    }

    fun openAppSettings() {
        val uri = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }

    private suspend fun requestFromSystem(name: String): Boolean = suspendCancellableCoroutine { continuation ->
        val key = "permission:$name:${SystemClock.elapsedRealtimeNanos()}"
        var launcher: ActivityResultLauncher<String>? = null
        launcher = activity.activityResultRegistry.register(key, ActivityResultContracts.RequestPermission()) { granted ->
            launcher?.unregister()
            if (continuation.isActive) continuation.resume(granted)
        }
        continuation.invokeOnCancellation { launcher.unregister() }
        launcher.launch(name)
    }
}

private fun Permission.toManifestName(): String? = when (this) {
    Permission.CAMERA -> Manifest.permission.CAMERA
    Permission.NOTIFICATIONS ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
}
