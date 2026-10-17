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

package com.gyugle.mitjul.core.logging

import android.util.Log
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

private const val RULE = "+--------------------------------------"

internal class BoxLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val lines = buildList {
            addAll(redact(message).lines())
            if (throwable != null) {
                addAll(redact(throwable.stackTraceToString()).trimEnd().lines())
            }
        }
        val box = lines.joinToString("\n", "$RULE\n", "\n$RULE") { "| $it" }
        Log.println(severity.toPriority(), tag, box)
    }
}

private fun Severity.toPriority(): Int = when (this) {
    Severity.Verbose -> Log.VERBOSE
    Severity.Debug -> Log.DEBUG
    Severity.Info -> Log.INFO
    Severity.Warn -> Log.WARN
    Severity.Error -> Log.ERROR
    Severity.Assert -> Log.ASSERT
}
