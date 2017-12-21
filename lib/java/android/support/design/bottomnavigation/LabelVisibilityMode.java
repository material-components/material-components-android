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
 * Label visibility mode enum for bottom navigation.
 *
 * <p>The label visibility mode determines whether to show or hide labels in the navigation items.
 * Setting the label visibility mode to {@link LabelVisibilityMode#LABEL_VISIBILITY_SELECTED} sets
 * the label to only show when selected, setting it to {@link
 * LabelVisibilityMode#LABEL_VISIBILITY_LABELED} sets the label to always show, and {@link
 * LabelVisibilityMode#LABEL_VISIBILITY_UNLABELED} sets the label to never show.
 *
 * <p>Setting the label visibility mode to {@link LabelVisibilityMode#LABEL_VISIBILITY_LEGACY} sets
 * the label to behave as it used to with {@link ShiftingMode}.
 */
@IntDef({
  LabelVisibilityMode.LABEL_VISIBILITY_LEGACY,
  LabelVisibilityMode.LABEL_VISIBILITY_SELECTED,
  LabelVisibilityMode.LABEL_VISIBILITY_LABELED,
  LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
})
@Retention(RetentionPolicy.SOURCE)
public @interface LabelVisibilityMode {
  /**
   * Label is shown when {@link ShiftingMode} is enabled, or hidden when it is not.
   *
   * @deprecated Use an explicit {@link LabelVisibilityMode}s instead.
   */
  @Deprecated int LABEL_VISIBILITY_LEGACY = -1;

  /** Label is shown on the selected navigation item. */
  int LABEL_VISIBILITY_SELECTED = 0;

  /** Label is shown on all navigation items. */
  int LABEL_VISIBILITY_LABELED = 1;

  /** Label is not shown on any navigation items. */
  int LABEL_VISIBILITY_UNLABELED = 2;
}
