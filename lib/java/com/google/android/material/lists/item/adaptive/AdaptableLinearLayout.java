/*
 * Copyright 2020 The Android Open Source Project
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
package com.google.android.material.lists.item.adaptive;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class AdaptableLinearLayout extends LinearLayout implements AdaptableView<Boolean> {

  AdaptableVisibility<Boolean> adaptableVisibility = new AdaptableVisibility<>(this, this);

  public AdaptableLinearLayout(@NonNull Context context) {
    this(context, null);
  }

  public AdaptableLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AdaptableLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        boolean areAllChildrenGone = true;
        int childCount = getChildCount();
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
          int currentChildVisibility = getChildAt(childIndex).getVisibility();
          if (currentChildVisibility == View.VISIBLE) {
            areAllChildrenGone = false;
            break;
          }
        }
        adaptableVisibility.updateContent(areAllChildrenGone);
      }
    });
  }

  @Override
  public boolean isContentVisible(Boolean areAllChildrenGone) {
    return !areAllChildrenGone;
  }
}
