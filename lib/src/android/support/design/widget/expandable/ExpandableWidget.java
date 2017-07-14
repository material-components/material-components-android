/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.support.design.widget.expandable;

import android.support.annotation.IdRes;

/**
 * A widget that has expanded/collapsed state.
 *
 * <p>When the expanded state changes, an event is dispatched so that other widgets may react via a
 * {@link android.support.design.widget.CoordinatorLayout.Behavior}.
 */
public interface ExpandableWidget {

  /** Sets the expanded state on this widget. */
  void setExpanded(boolean expanded);

  /** Returns whether this widget is expanded. */
  boolean isExpanded();

  /**
   * Sets the expanded component id hint, which may be used by a Behavior to determine whether it
   * should handle this widget's state change.
   */
  void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint);

  /** Returns the expanded component id hint. */
  @IdRes
  int getExpandedComponentIdHint();
}
