/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.shape;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * CornerFamily enum that holds which family to be used to create a {@link CornerTreatment}
 *
 * <p>The corner family determines which family to use to create a {@link CornerTreatment}. Setting
 * the CornerFamily to {@link CornerFamily#ROUNDED} sets the corner treatment to {@link
 * RoundedCornerTreatment}, and setting the CornerFamily to {@link CornerFamily#CUT} sets the corner
 * treatment to a {@link CutCornerTreatment}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
@IntDef({CornerFamily.ROUNDED, CornerFamily.CUT})
@Retention(RetentionPolicy.SOURCE)
public @interface CornerFamily {
  /** Corresponds to a {@link RoundedCornerTreatment}. */
  int ROUNDED = 0;
  /** Corresponds to a {@link CutCornerTreatment}. */
  int CUT = 1;
}
