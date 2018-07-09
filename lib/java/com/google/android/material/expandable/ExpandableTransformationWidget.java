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
package com.google.android.material.expandable;

import android.support.annotation.IdRes;

/** An ExpandableWidget that visually transforms into another component when expanded. */
public interface ExpandableTransformationWidget extends ExpandableWidget {

  /** Returns the expanded component id hint. */
  @IdRes
  int getExpandedComponentIdHint();

  /**
   * Sets the expanded component id hint, which may be used by a Behavior to determine whether it
   * should handle this widget's state change.
   */
  void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint);
}
