/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.slider;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Mode to specify the visibility of tick marks. */
@IntDef({
  TickVisibilityMode.TICK_VISIBILITY_AUTO_LIMIT,
  TickVisibilityMode.TICK_VISIBILITY_AUTO_HIDE,
  TickVisibilityMode.TICK_VISIBILITY_HIDDEN
})
@Retention(RetentionPolicy.SOURCE)
public @interface TickVisibilityMode {

  /**
   * All tick marks will be drawn if they are not spaced too densely. Otherwise, the maximum allowed
   * number of tick marks will be drawn. Note that in this case the drawn ticks may not match the
   * actual snap values.
   */
  int TICK_VISIBILITY_AUTO_LIMIT = 0;

  /**
   * All tick marks will be drawn if they are not spaced too densely. Otherwise, the tick marks will
   * not be drawn.
   */
  int TICK_VISIBILITY_AUTO_HIDE = 1;

  /** Tick marks will not be drawn. */
  int TICK_VISIBILITY_HIDDEN = 2;
}
