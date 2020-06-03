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

import android.view.View;

import androidx.annotation.RestrictTo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class AdaptableVisibility<T> {

  private View adaptableView;
  private AdaptableView<T> contentVisibility;

  public AdaptableVisibility(View adaptableView, AdaptableView<T> contentVisibility) {
    this.adaptableView = adaptableView;
    this.contentVisibility = contentVisibility;
    adaptableView.setVisibility(GONE);
  }

  public void updateContent(T content) {
    boolean isContentVisible = contentVisibility.isContentVisible(content);
    int visibility = isContentVisible ? VISIBLE : GONE;

    adaptableView.setVisibility(visibility);
  }
}
