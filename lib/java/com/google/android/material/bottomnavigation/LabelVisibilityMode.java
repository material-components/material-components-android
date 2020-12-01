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

package com.google.android.material.bottomnavigation;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Label visibility mode enum for bottom navigation.
 *
 * <p>The label visibility mode determines whether to show or hide labels in the navigation items.
 * Setting the label visibility mode to {@link BottomNavigationView#LABEL_VISIBILITY_SELECTED} sets
 * the label to only show when selected, setting it to {@link
 * BottomNavigationView#LABEL_VISIBILITY_LABELED} sets the label to always show, and {@link
 * BottomNavigationView#LABEL_VISIBILITY_UNLABELED} sets the label to never show.
 *
 * <p>Setting the label visibility mode to {@link BottomNavigationView#LABEL_VISIBILITY_AUTO} sets
 * the label to behave as "labeled" when there are 3 items or less, or "selected" when there are 4
 * items or more.
 *
 * @deprecated Please use the label visibility constants mentioned above.
 */
@IntDef({
  LabelVisibilityMode.LABEL_VISIBILITY_AUTO,
  LabelVisibilityMode.LABEL_VISIBILITY_SELECTED,
  LabelVisibilityMode.LABEL_VISIBILITY_LABELED,
  LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
})
@Retention(RetentionPolicy.SOURCE)
@Deprecated
public @interface LabelVisibilityMode {
  /**
   * Label behaves as "labeled" when there are 3 items or less, or "selected" when there are 4 items
   * or more.
   */
  int LABEL_VISIBILITY_AUTO = BottomNavigationView.LABEL_VISIBILITY_AUTO;

  /** Label is shown on the selected navigation item. */
  int LABEL_VISIBILITY_SELECTED = BottomNavigationView.LABEL_VISIBILITY_SELECTED;

  /** Label is shown on all navigation items. */
  int LABEL_VISIBILITY_LABELED = BottomNavigationView.LABEL_VISIBILITY_LABELED;

  /** Label is not shown on any navigation items. */
  int LABEL_VISIBILITY_UNLABELED = BottomNavigationView.LABEL_VISIBILITY_UNLABELED;
}
