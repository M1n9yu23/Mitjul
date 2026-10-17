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

package com.gyugle.mitjul.core.ml

import android.content.Context
import android.net.Uri
import co.touchlab.kermit.Logger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.gyugle.mitjul.core.domain.Result
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MlKitTextRecognitionEngine(private val context: Context) : TextRecognitionEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    override suspend fun recognize(imagePath: String): Result<List<String>, TextRecognitionError> {
        val image = try {
            withContext(Dispatchers.IO) { InputImage.fromFilePath(context, Uri.fromFile(File(imagePath))) }
        } catch (_: IOException) {
            return Result.Error(TextRecognitionError.IMAGE_UNREADABLE)
        }
        return try {
            val text = recognizer.process(image).await()
            Result.Success(text.textBlocks.map { block -> block.lines.joinToString(" ") { it.text } })
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e { "텍스트를 인식하지 못했다: ${e::class.simpleName}" }
            Result.Error(TextRecognitionError.RECOGNITION_FAILED)
        }
    }
}
