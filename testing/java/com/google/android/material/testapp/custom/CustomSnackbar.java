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

import android.view.View;
import android.widget.TextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Sample code for a custom snackbar that shows two separate text views and two images in the main
 * content area.
 */
public class CustomSnackbar extends BaseTransientBottomBar<CustomSnackbar> {
  public CustomSnackbar(
      CoordinatorLayout parent,
      View content,
      BaseTransientBottomBar.ContentViewCallback contentViewCallback) {
    super(parent, content, contentViewCallback);
  }

  /** Sets the title of this custom snackbar. */
  @CanIgnoreReturnValue
  public CustomSnackbar setTitle(String title) {
    TextView titleView = getView().findViewById(R.id.custom_snackbar_title);
    titleView.setText(title);
    return this;
  }

  /** Sets the subtitle of this custom snackbar. */
  @CanIgnoreReturnValue
  public CustomSnackbar setSubtitle(String subtitle) {
    TextView subtitleView = getView().findViewById(R.id.custom_snackbar_subtitle);
    subtitleView.setText(subtitle);
    return this;
  }
}
