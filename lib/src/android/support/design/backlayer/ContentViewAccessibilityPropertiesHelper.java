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

package android.support.design.backlayer;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

/**
 * Helper that overrides the values of {@code focusable}, {@code importantForAccessibility} and
 * {@code contentDescription} for a view to make it focusable. Stores the previous values to allow
 * restoring them later.
 *
 * <p>This also affects child views to make them not focusable when the parent view is focusable.
 */
class ContentViewAccessibilityPropertiesHelper {

  private ChildViewAccessibilityHelper childViewAccessibilityHelper = null;
  private final View view;
  private boolean previousFocusability;
  private int previousImportantForAccesibility;
  private CharSequence previousContentDescription;

  ContentViewAccessibilityPropertiesHelper(View view) {
    this.view = view;
    previousContentDescription = view.getContentDescription();
    previousFocusability = view.isFocusable();
    previousImportantForAccesibility = ViewCompat.getImportantForAccessibility(view);
    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      childViewAccessibilityHelper = new ChildViewAccessibilityHelper(viewGroup);
      viewGroup.setOnHierarchyChangeListener(childViewAccessibilityHelper);
    }
  }

  /**
   * Overrides the values of accessibility-related properties to make the content view focusable in
   * accessibility mode.
   *
   * <p>This method sets the view's {@code contentDescription} to this method's only argument and
   * sets both {@code importantForAccessibility} and {@code focusable} to true.
   *
   * <p>This method also stores previous values of these properties so they can be restored by
   * calling {@link #restoreAccessibilityProperties()}.
   *
   * <p>If the content view is actually a ViewGroup, this method uses a {@link
   * ChildViewAccessibilityHelper} to disable focus on the subviews while the content view is forced
   * to be focusable. Child views' focusability also is reverted in {@link
   * #restoreAccessibilityProperties()}.
   */
  void makeFocusableWithContentDescription(CharSequence contentDescription) {
    previousContentDescription = view.getContentDescription();
    previousFocusability = view.isFocusable();
    previousImportantForAccesibility = ViewCompat.getImportantForAccessibility(view);
    view.setFocusable(true);
    view.setContentDescription(contentDescription);
    ViewCompat.setImportantForAccessibility(view, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    if (childViewAccessibilityHelper != null) {
      childViewAccessibilityHelper.disableChildFocus();
    }
  }

  /**
   * Restores the values of {@code focusable}, {@code importantForAccessibility} and {@code
   * contentDescription} to the values stored when {@link
   * #makeFocusableWithContentDescription(CharSequence)} was called.
   *
   * <p>If this {@link ContentViewAccessibilityPropertiesHelper} is being used with a {@link
   * ViewGroup}, this also restores the original values of these properties for its child views.
   *
   * <p>This method is a no-op if not called after to {@link
   * #makeFocusableWithContentDescription(CharSequence)}
   */
  void restoreAccessibilityProperties() {
    view.setContentDescription(previousContentDescription);
    view.setFocusable(previousFocusability);
    ViewCompat.setImportantForAccessibility(view, previousImportantForAccesibility);
    if (childViewAccessibilityHelper != null) {
      childViewAccessibilityHelper.restoreChildFocus();
    }
  }
}
