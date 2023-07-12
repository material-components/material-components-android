/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.color.utilities;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

/**
 * Describes the relationship in lightness between two colors.
 *
 * <p>'nearer' and 'farther' describes closeness to the surface roles. For instance,
 * ToneDeltaPair(A, B, 10, 'nearer', stayTogether) states that A should be 10 lighter than B in
 * light mode, and 10 darker than B in dark mode.
 *
 * <p>See `ToneDeltaPair` for details.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public enum TonePolarity {
  DARKER,
  LIGHTER,
  NEARER,
  FARTHER;
}
