/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * {@link FrameLayout} that provides callbacks for all touch events.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class TouchObserverFrameLayout extends FrameLayout {

  @Nullable private OnTouchListener onTouchListener;

  public TouchObserverFrameLayout(@NonNull Context context) {
    super(context);
  }

  public TouchObserverFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TouchObserverFrameLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (onTouchListener != null) {
      onTouchListener.onTouch(this, ev);
    }
    return super.onInterceptTouchEvent(ev);
  }

  @Override
  public void setOnTouchListener(@Nullable OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
  }
}
