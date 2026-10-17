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

package com.gyugle.mitjul

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.gyugle.mitjul.core.database.di.coreDatabaseModule
import com.gyugle.mitjul.core.datastore.di.coreDataStoreModule
import com.gyugle.mitjul.core.logging.installLogging
import com.gyugle.mitjul.core.ml.di.coreMlModule
import com.gyugle.mitjul.core.network.di.coreNetworkModule
import com.gyugle.mitjul.core.session.di.coreSessionModule
import com.gyugle.mitjul.di.appModule
import com.gyugle.mitjul.feature.auth.data.di.authDataModule
import com.gyugle.mitjul.feature.auth.presentation.di.authPresentationModule
import com.gyugle.mitjul.feature.library.data.di.libraryDataModule
import com.gyugle.mitjul.feature.library.presentation.di.libraryPresentationModule
import com.gyugle.mitjul.feature.quote.data.di.quoteDataModule
import com.gyugle.mitjul.feature.quote.presentation.di.quotePresentationModule
import com.gyugle.mitjul.feature.settings.data.di.settingsDataModule
import com.gyugle.mitjul.feature.settings.presentation.di.settingsPresentationModule
import com.gyugle.mitjul.feature.stats.data.di.statsDataModule
import com.gyugle.mitjul.feature.stats.presentation.di.statsPresentationModule
import io.ktor.client.HttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MitjulApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        installLogging(enabled = BuildConfig.DEBUG)
        startKoin {
            androidContext(this@MitjulApplication)
            workManagerFactory()
            modules(
                coreNetworkModule(baseUrl = BuildConfig.BASE_URL, isDebugBuild = BuildConfig.DEBUG),
                coreSessionModule,
                coreDatabaseModule,
                coreDataStoreModule,
                coreMlModule,
                authDataModule,
                authPresentationModule,
                libraryDataModule,
                libraryPresentationModule,
                quoteDataModule,
                quotePresentationModule,
                settingsDataModule,
                settingsPresentationModule,
                statsDataModule,
                statsPresentationModule,
                appModule,
            )
        }
    }

    override fun newImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .components { add(KtorNetworkFetcherFactory(httpClient = get<HttpClient>())) }
        .build()
}
