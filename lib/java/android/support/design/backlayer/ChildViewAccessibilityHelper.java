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
import java.util.HashMap;
import java.util.Map;

/**
 * Class that overrides the {@code importantForAccessibility} and {@code focusable} properties for
 * child views. This is necessary to offer the same experience for vision impaired users that depend
 * on accessibility services such as TalkBack by making sure the AccesibilityNodeInfo tree matches
 * the views with which it is possible to interact in both backlayer states, expanded and collapsed.
 * These changes are also important for the growing number of users who use a physical keyboard with
 * a TAB key (i.e. tablet users, Chromebook with Android apps ...)
 *
 * <p>When the back layer is collapsed, this is used by the back layer to disable all views outside
 * of CollapsedBackLayerContents.
 *
 * <p>When the back layer is expanded it is used by the {@link BackLayerSiblingBehavior} to disable
 * all subviews of the content layer, making clicks on the content layer collapse the backlayer.
 *
 * <p>This class implements {@link android.view.ViewGroup.OnHierarchyChangeListener} for two
 * purposes: to start tracking the status of newly-added subviews, and to stop tracking the status
 * of removed views. It is particularly important for the latter purpose because in order to track
 * the status of a view, the {@link ChildViewAccessibilityHelper} keeps a reference to the view and
 * could be leaking memory if the view in question is removed from its parent.
 */
class ChildViewAccessibilityHelper implements ViewGroup.OnHierarchyChangeListener {

  private final ViewGroup parent;
  private final HashMap<View, Integer> importantForAccessibilityMap = new HashMap<>();
  private final HashMap<View, Boolean> focusableMap = new HashMap<>();
  private boolean isChildFocusEnabled = true;

  ChildViewAccessibilityHelper(ViewGroup parent) {
    this.parent = parent;
  }

  /**
   * Disable focus on child views.
   *
   * <p>It is possible to restore the original status by calling {@link #restoreChildFocus()}. For
   * that purpose, this method also keeps a copy of the {@code importantForAccessibility} and {@code
   * focusable} properties for each subview whose focus is disabled.
   *
   * <p>Caveat: This method does not change {@link CollapsedBackLayerContents} or its child views
   * because these views must be always visible and accessible to all users, vision-impaired or not.
   */
  void disableChildFocus() {
    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);
      if (shouldOverrideView(child)) {
        importantForAccessibilityMap.put(child, ViewCompat.getImportantForAccessibility(child));
        focusableMap.put(child, child.isFocusable());
        ViewCompat.setImportantForAccessibility(
            child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        child.setFocusable(false);
      }
    }
    isChildFocusEnabled = false;
  }

  /**
   * Restores the values of focusable and importantForAccessibility to the values stored when {@link
   * #disableChildFocus()} was called.
   *
   * <p>If {@link #disableChildFocus()} was never called this method is essentially a no-op.
   */
  void restoreChildFocus() {
    for (Map.Entry<View, Integer> entry : importantForAccessibilityMap.entrySet()) {
      ViewCompat.setImportantForAccessibility(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<View, Boolean> entry : focusableMap.entrySet()) {
      entry.getKey().setFocusable(entry.getValue());
    }
    importantForAccessibilityMap.clear();
    focusableMap.clear();
    isChildFocusEnabled = true;
  }

  // We shouldn't change anything in the CollapsedBackLayerContents.
  private boolean shouldOverrideView(View view) {
    return !(view instanceof CollapsedBackLayerContents);
  }

  @Override
  public void onChildViewAdded(View parent, View child) {
    if (!isChildFocusEnabled && shouldOverrideView(child)) {
      importantForAccessibilityMap.put(child, ViewCompat.getImportantForAccessibility(child));
      focusableMap.put(child, child.isFocusable());
      ViewCompat.setImportantForAccessibility(
          child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      child.setFocusable(false);
    }
  }

  @Override
  public void onChildViewRemoved(View parent, View child) {
    // Remove mappings for removed views to avoid memory leaks
    importantForAccessibilityMap.remove(child);
    focusableMap.remove(child);
  }
}
