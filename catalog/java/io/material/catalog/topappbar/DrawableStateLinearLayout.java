/*
 * Copyright 2026 The Android Open Source Project
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

package io.material.catalog.topappbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

class DrawableStateLinearLayout extends LinearLayout {
  private int[] extraDrawableState;

  public DrawableStateLinearLayout(Context context) {
    super(context);
  }

  public DrawableStateLinearLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DrawableStateLinearLayout(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public DrawableStateLinearLayout(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (extraDrawableState == null) {
      return super.onCreateDrawableState(extraSpace);
    }
    return mergeDrawableStates(
        super.onCreateDrawableState(extraSpace + extraDrawableState.length), extraDrawableState);
  }

  public void setExtraDrawableState(int[] extraDrawableState) {
    this.extraDrawableState = extraDrawableState;
    refreshDrawableState();
  }
}
