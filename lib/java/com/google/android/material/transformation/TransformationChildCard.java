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
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.android.material.expandable.ExpandableWidget;

/**
 * CardView layout for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes by transforming the ExpandableWidget into
 * itself.
 *
 * <p>This ViewGroup should contain exactly one child.
 *
 * <p>This class should be used if you need to support shadows on pre-L devices.
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public class TransformationChildCard extends CircularRevealCardView {

  public TransformationChildCard(Context context) {
    this(context, null);
  }

  public TransformationChildCard(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
