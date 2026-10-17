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

package com.gyugle.mitjul.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test

class NavigatorTest {

    private data object ShelfKey : NavKey

    private data class BookKey(val id: String) : NavKey

    @Test
    fun goTo_sameKeyAsTop_doesNotStackTwice() {
        val navigator = Navigator(NavBackStack(ShelfKey))

        navigator.goTo(BookKey("book-1"))
        navigator.goTo(BookKey("book-1"))
        navigator.goTo(BookKey("book-2"))

        assertThat(navigator.backStack).containsExactly(ShelfKey, BookKey("book-1"), BookKey("book-2"))
    }

    @Test
    fun goBack_onlyOneLeft_keepsIt() {
        val navigator = Navigator(NavBackStack(ShelfKey, BookKey("book-1")))

        navigator.goBack()
        navigator.goBack()

        assertThat(navigator.backStack).containsExactly(ShelfKey)
    }
}
