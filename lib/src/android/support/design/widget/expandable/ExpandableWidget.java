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

/**
 * A widget that has expanded/collapsed state. When the expanded state changes, {@link
 * android.support.design.widget.CoordinatorLayout#dispatchDependentViewsChanged an event is
 * dispatched} so that other widgets may react via a {@link
 * android.support.design.widget.CoordinatorLayout.Behavior}.
 *
 * <p>Implementations of this interface should create an instance of {@link ExpandableWidgetHelper}
 * and forward all calls to it.
 *
 * <p>The expanded state can saved across configuration changes by implementing {@link
 * #onSaveInstanceState} and {@link #onRestoreInstanceState}:
 *
 * <pre><code>
 * {@literal @}Override
 * protected Parcelable onSaveInstanceState() {
 *   Parcelable superState = super.onSaveInstanceState();
 *   ExtendableSavedState state = new ExtendableSavedState(superState);
 *
 *   state.extendableStates.put(
 *       "expandableWidgetHelper", expandableWidgetHelper.onSaveInstanceState());
 *
 *   return state;
 * }
 *
 * {@literal @}Override
 * protected void onRestoreInstanceState(Parcelable state) {
 *   if (!(state instanceof ExtendableSavedState)) {
 *     super.onRestoreInstanceState(state);
 *     return;
 *   }
 *
 *   ExtendableSavedState ess = (ExtendableSavedState) state;
 *   super.onRestoreInstanceState(ess.getSuperState());
 *
 *   expandableWidgetHelper.onRestoreInstanceState(
 *       ess.extendableStates.get("expandableWidgetHelper"));
 * }
 * </code></pre>
 */
public interface ExpandableWidget {

  /**
   * Returns whether this widget is expanded.
   *
   * <p>Implementations should call {@link ExpandableWidgetHelper#isExpanded()}.
   */
  boolean isExpanded();

  /**
   * Sets the expanded state on this widget.
   *
   * <p>Implementations should call {@link ExpandableWidgetHelper#setExpanded(boolean)}.
   *
   * @return true if the expanded state changed as a result of this call.
   */
  boolean setExpanded(boolean expanded);
}
