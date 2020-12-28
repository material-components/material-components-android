/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.transformation;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.circularreveal.CircularRevealFrameLayout;
import com.google.android.material.expandable.ExpandableWidget;

/**
 * Wrapper layout for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes by transforming the ExpandableWidget into
 * itself.
 *
 * <p>This ViewGroup should contain exactly one child.
 *
 * <p>If this layout needs to support shadows on pre-L devices, use {@link TransformationChildCard}
 * instead.
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public class TransformationChildLayout extends CircularRevealFrameLayout {

  public TransformationChildLayout(@NonNull Context context) {
    this(context, null);
  }

  public TransformationChildLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }
}
