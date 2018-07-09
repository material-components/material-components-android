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

package com.google.android.material.internal;


import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This attribute controls whether the flex container is single-line or multi-line, and the
 * direction of the cross axis.
 *
 * @hide
 */
@IntDef({FlexWrap.NOWRAP, FlexWrap.WRAP})
@Retention(RetentionPolicy.SOURCE)
@RestrictTo(LIBRARY_GROUP)
public @interface FlexWrap {

  /** The flex container is single-line. */
  int NOWRAP = 0;

  /** The flex container is multi-line. */
  int WRAP = 1;
}
