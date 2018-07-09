/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.feature;

import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/** Utils for demos. */
public abstract class DemoUtils {

  public static <T extends View> List<T> findViewsWithType(View root, Class<T> type) {
    List<T> views = new ArrayList<>();
    findViewsWithType(root, type, views);
    return views;
  }

  private static <T extends View> void findViewsWithType(View view, Class<T> type, List<T> views) {
    if (type.isInstance(view)) {
      views.add(type.cast(view));
    }

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        findViewsWithType(viewGroup.getChildAt(i), type, views);
      }
    }
  }
}
