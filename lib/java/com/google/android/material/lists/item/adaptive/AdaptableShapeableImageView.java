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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.google.android.material.imageview.ShapeableImageView;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class AdaptableShapeableImageView extends ShapeableImageView implements AdaptableView<Drawable> {

  AdaptableVisibility<Drawable> adaptableVisibility = new AdaptableVisibility<>(this, this);

  public AdaptableShapeableImageView(Context context) {
    super(context);
  }

  public AdaptableShapeableImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public AdaptableShapeableImageView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    super.setImageDrawable(drawable);
    adaptableVisibility.updateContent(drawable);
  }

  @Override
  public boolean isContentVisible(Drawable content) {
    return content != null;
  }
}
