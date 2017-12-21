/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.bottomnavigation;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Shifting mode types for bottom navigation. Shifting mode hides text labels for child views when
 * enabled, unless the child view is selected. Shifting mode is enabled for {@code
 * BottomNavigationView}s with more than 3 children by default, or for {@code BottomNavigationView}s
 * with any number of children if shifting mode flag is set to {@link
 * ShiftingMode#SHIFTING_MODE_ON}.
 *
 * @see <a
 *     href="https://material.io/guidelines/components/bottom-navigation.html#bottom-navigation-specs">Material
 *     Design guidelines</a>
 * @deprecated use {@link LabelVisibilityMode instead}
 */
@Deprecated
@IntDef({
  ShiftingMode.SHIFTING_MODE_AUTO,
  ShiftingMode.SHIFTING_MODE_OFF,
  ShiftingMode.SHIFTING_MODE_ON
})
@Retention(RetentionPolicy.SOURCE)
public @interface ShiftingMode {
  /**
   * Shifting mode enabled only when BottomNavigationView has 3 or more children (default).
   *
   * @deprecated use {@link LabelVisibilityMode instead}
   */
  @Deprecated int SHIFTING_MODE_AUTO = -1;

  /**
   * Shifting mode disabled in all cases.
   *
   * @deprecated use {@link LabelVisibilityMode instead}
   */
  @Deprecated int SHIFTING_MODE_OFF = 0;

  /**
   * Shifting mode enabled in all cases.
   *
   * @deprecated use {@link LabelVisibilityMode instead}
   */
  @Deprecated int SHIFTING_MODE_ON = 1;
}
