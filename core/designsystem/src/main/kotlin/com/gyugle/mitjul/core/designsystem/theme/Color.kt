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

package com.gyugle.mitjul.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val PaperWarm = Color(0xFFFAF6EF)
internal val PaperSheet = Color(0xFFFFFCF6)
internal val PaperSunken = Color(0xFFEFE7DA)
internal val InkBlack = Color(0xFF231E19)
internal val InkFaded = Color(0xFF5B5248)
internal val UnderlineRed = Color(0xFF9E3B22)
internal val UnderlineTint = Color(0xFFFFDBD0)
internal val UnderlineDeep = Color(0xFF3B0D02)
internal val LeafGreen = Color(0xFF4B6152)
internal val LeafTint = Color(0xFFCFE6D5)
internal val LeafDeep = Color(0xFF0B2014)
internal val EdgeLine = Color(0xFF8B7F71)
internal val AlertRed = Color(0xFFB3261E)
internal val Leather = Color(0xFF7B5A33)
internal val LeatherTint = Color(0xFFF6E3C6)
internal val LeatherDeep = Color(0xFF2C1B05)
internal val PaperLift = Color(0xFFFDF9F2)
internal val PaperCard = Color(0xFFF8F2E8)
internal val PaperRaised = Color(0xFFF2EBDF)
internal val PaperTop = Color(0xFFECE4D5)
internal val PaperDim = Color(0xFFE7DECE)

internal val LightColors = lightColorScheme(
    primary = UnderlineRed,
    onPrimary = Color.White,
    primaryContainer = UnderlineTint,
    onPrimaryContainer = UnderlineDeep,
    secondary = LeafGreen,
    onSecondary = Color.White,
    secondaryContainer = LeafTint,
    onSecondaryContainer = LeafDeep,
    tertiary = Leather,
    onTertiary = Color.White,
    tertiaryContainer = LeatherTint,
    onTertiaryContainer = LeatherDeep,
    background = PaperWarm,
    onBackground = InkBlack,
    surface = PaperSheet,
    onSurface = InkBlack,
    surfaceVariant = PaperSunken,
    onSurfaceVariant = InkFaded,
    surfaceBright = PaperSheet,
    surfaceDim = PaperDim,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PaperLift,
    surfaceContainer = PaperCard,
    surfaceContainerHigh = PaperRaised,
    surfaceContainerHighest = PaperTop,
    outline = EdgeLine,
    error = AlertRed,
    onError = Color.White,
)

internal val NightRoom = Color(0xFF16130F)
internal val NightDesk = Color(0xFF1E1A16)
internal val NightShelf = Color(0xFF2B2620)
internal val Moonlight = Color(0xFFEDE4D8)
internal val MoonlightDim = Color(0xFFCFC3B4)
internal val UnderlineWarm = Color(0xFFFFB59D)
internal val UnderlineDusk = Color(0xFF7C2C14)
internal val LeafPale = Color(0xFFB2C8B6)
internal val LeafDusk = Color(0xFF344A3B)
internal val EdgeDim = Color(0xFF9A8D7E)
internal val AlertPink = Color(0xFFF2B8B5)
internal val Brass = Color(0xFFE3C08D)
internal val BrassDusk = Color(0xFF5D452A)
internal val NightFloor = Color(0xFF100D0A)
internal val NightLow = Color(0xFF1A1713)
internal val NightMid = Color(0xFF221E19)
internal val NightHigh = Color(0xFF2D2822)
internal val NightTop = Color(0xFF38322B)
internal val NightBright = Color(0xFF3E372F)

internal val DarkColors = darkColorScheme(
    primary = UnderlineWarm,
    onPrimary = UnderlineDeep,
    primaryContainer = UnderlineDusk,
    onPrimaryContainer = UnderlineTint,
    secondary = LeafPale,
    onSecondary = InkBlack,
    secondaryContainer = LeafDusk,
    onSecondaryContainer = LeafTint,
    tertiary = Brass,
    onTertiary = LeatherDeep,
    tertiaryContainer = BrassDusk,
    onTertiaryContainer = LeatherTint,
    background = NightRoom,
    onBackground = Moonlight,
    surface = NightDesk,
    onSurface = Moonlight,
    surfaceVariant = NightShelf,
    onSurfaceVariant = MoonlightDim,
    surfaceBright = NightBright,
    surfaceDim = NightRoom,
    surfaceContainerLowest = NightFloor,
    surfaceContainerLow = NightLow,
    surfaceContainer = NightMid,
    surfaceContainerHigh = NightHigh,
    surfaceContainerHighest = NightTop,
    outline = EdgeDim,
    error = AlertPink,
    onError = UnderlineDeep,
)
