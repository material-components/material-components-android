/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.tabs;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

@RestrictTo(LIBRARY)
abstract class SeslAbsIndicatorView extends View {
  public SeslAbsIndicatorView(Context context) {
    super(context);
  }

  public SeslAbsIndicatorView(Context context,
                              @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SeslAbsIndicatorView(Context context,
                              @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public SeslAbsIndicatorView(Context context,
                              @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void setSelectedIndicatorColor(int color) {
    onSetSelectedIndicatorColor(color);
  }

  abstract void onSetSelectedIndicatorColor(int color);

  public void setPressed() {
    startPressEffect();
  }

  abstract void startPressEffect();

  public void setReleased() {
    startReleaseEffect();
  }

  abstract void startReleaseEffect();

  public void setHide() {
    onHide();
  }

  abstract void onHide();

  public void setShow() {
    onShow();
  }

  abstract void onShow();
}
