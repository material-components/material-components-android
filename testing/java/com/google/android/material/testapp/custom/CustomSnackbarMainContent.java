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
package com.google.android.material.testapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Layout for the custom snackbar that shows two separate text views and two images in the main
 * content area.
 */
public class CustomSnackbarMainContent extends RelativeLayout {
  private final int maxWidth;

  public CustomSnackbarMainContent(Context context) {
    this(context, null);
  }

  public CustomSnackbarMainContent(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CustomSnackbarMainContent(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    maxWidth = context.getResources().getDimensionPixelSize(R.dimen.custom_snackbar_max_width);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if ((maxWidth > 0) && (getMeasuredWidth() > maxWidth)) {
      super.onMeasure(
          MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
    }
  }
}
