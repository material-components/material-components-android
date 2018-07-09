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
import com.google.android.material.circularreveal.CircularRevealFrameLayout;
import com.google.android.material.expandable.ExpandableWidget;
import android.util.AttributeSet;

/**
 * Wrapper layout for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes by transforming the ExpandableWidget into
 * itself.
 *
 * <p>This ViewGroup should contain exactly one child.
 *
 * <p>If this layout needs to support shadows on pre-L devices, use {@link TransformationChildCard}
 * instead.
 */
public class TransformationChildLayout extends CircularRevealFrameLayout {

  public TransformationChildLayout(Context context) {
    this(context, null);
  }

  public TransformationChildLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
