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

package com.google.android.material.expandable;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewParent;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * ExpandableWidgetHelper is a helper class for writing custom {@link ExpandableWidget}s and {@link
 * ExpandableTransformationWidget}. Please see the interface documentation when implementing your
 * custom class.
 */
public final class ExpandableWidgetHelper {

  @NonNull private final View widget;

  private boolean expanded = false;
  @IdRes private int expandedComponentIdHint = 0;

  /** Call this from the constructor. */
  public ExpandableWidgetHelper(ExpandableWidget widget) {
    this.widget = (View) widget;
  }

  /** Call this from {@link ExpandableWidget#setExpanded(boolean)}. */
  public boolean setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      dispatchExpandedStateChanged();
      return true;
    }
    return false;
  }

  /** Call this from {@link ExpandableWidget#isExpanded()}. */
  public boolean isExpanded() {
    return expanded;
  }

  /** Call this from {@link View#onSaveInstanceState()}. */
  @NonNull
  public Bundle onSaveInstanceState() {
    Bundle state = new Bundle();
    state.putBoolean("expanded", expanded);
    state.putInt("expandedComponentIdHint", expandedComponentIdHint);

    return state;
  }

  /** Call this from {@link View#onRestoreInstanceState(Parcelable)}. */
  public void onRestoreInstanceState(@NonNull Bundle state) {
    expanded = state.getBoolean("expanded", false);
    expandedComponentIdHint = state.getInt("expandedComponentIdHint", 0);

    if (expanded) {
      dispatchExpandedStateChanged();
    }
  }

  /** Call this from {@link ExpandableTransformationWidget#setExpandedComponentIdHint(int)}. */
  public void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint) {
    this.expandedComponentIdHint = expandedComponentIdHint;
  }

  /** Call this from {@link ExpandableTransformationWidget#getExpandedComponentIdHint()}. */
  @IdRes
  public int getExpandedComponentIdHint() {
    return expandedComponentIdHint;
  }

  private void dispatchExpandedStateChanged() {
    ViewParent parent = widget.getParent();
    if (parent instanceof CoordinatorLayout) {
      ((CoordinatorLayout) parent).dispatchDependentViewsChanged(widget);
    }
  }
}
