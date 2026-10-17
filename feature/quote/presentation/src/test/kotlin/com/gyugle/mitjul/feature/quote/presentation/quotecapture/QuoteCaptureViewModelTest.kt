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

package com.gyugle.mitjul.feature.quote.presentation.quotecapture

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.ml.FakeTextRecognitionEngine
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteCaptureViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onShutterClick_twiceThenTwoBlocksPicked_takesOnePictureAndKeepsPageOrder() = runTest {
        val engine = FakeTextRecognitionEngine(Result.Success(listOf("첫 문단", "둘째 문단", "셋째 문단")))
        val viewModel = QuoteCaptureViewModel(engine)

        viewModel.events.test {
            viewModel.onAction(QuoteCaptureAction.OnShutterClick)
            viewModel.onAction(QuoteCaptureAction.OnShutterClick)
            assertThat(awaitItem()).isEqualTo(QuoteCaptureEvent.TakePicture)

            viewModel.onAction(QuoteCaptureAction.OnPictureTaken("page.jpg"))
            viewModel.onAction(QuoteCaptureAction.OnBlockClick(2))
            viewModel.onAction(QuoteCaptureAction.OnBlockClick(0))
            viewModel.onAction(QuoteCaptureAction.OnInsertClick)
            assertThat(awaitItem()).isEqualTo(QuoteCaptureEvent.TextPicked("첫 문단 셋째 문단"))
        }

        assertThat(viewModel.state.value.blocks).isEmpty()
    }

    @Test
    fun onPermissionChecked_afterPermanentDenial_keepsTheSettingsRoute() {
        val viewModel = QuoteCaptureViewModel(FakeTextRecognitionEngine())

        viewModel.onAction(QuoteCaptureAction.OnPermissionResult(PermissionState.PERMANENTLY_DENIED))
        viewModel.onAction(QuoteCaptureAction.OnPermissionChecked(PermissionState.NOT_DETERMINED))
        assertThat(viewModel.state.value.permission).isEqualTo(PermissionState.PERMANENTLY_DENIED)

        viewModel.onAction(QuoteCaptureAction.OnPermissionChecked(PermissionState.GRANTED))
        assertThat(viewModel.state.value.permission).isEqualTo(PermissionState.GRANTED)
    }

    @Test
    fun onCameraBound_rebindWhileNothingIsRead_releasesTheShutter() {
        val viewModel = QuoteCaptureViewModel(FakeTextRecognitionEngine())

        viewModel.onAction(QuoteCaptureAction.OnShutterClick)
        viewModel.onAction(QuoteCaptureAction.OnCameraBound(Result.Success(Unit)))

        assertThat(viewModel.state.value.isReading).isFalse()
    }
}
