/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.testutils;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.testapp.R;
import java.util.ArrayList;

public class CoordinatorLayoutUtils {

  public static class DependentBehavior extends CoordinatorLayout.Behavior<View> {
    private final View dependency;

    public DependentBehavior(View dependency) {
      this.dependency = dependency;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
      return this.dependency != null && dependency == this.dependency;
    }
  }

  public static boolean hasAction(View view, int actionId) {
    ArrayList<AccessibilityActionCompat> actions = getActionList(view);
    for (int i = 0; i < actions.size(); i++) {
      if (actions.get(i).getId() == actionId) {
        return true;
      }
    }
    return false;
  }

  private static ArrayList<AccessibilityActionCompat> getActionList(View view) {
    @SuppressWarnings("unchecked")
    ArrayList<AccessibilityActionCompat> actions =
        (ArrayList<AccessibilityActionCompat>) view.getTag(R.id.tag_accessibility_actions);
    if (actions == null) {
      actions = new ArrayList<>();
    }
    return actions;
  }
}
