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
package com.google.android.material.animation;

import com.google.android.material.R;

import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

/**
 * A Property for the alpha of a ViewGroup's children.
 *
 * <p>All values are directly correlated with the children's {@link View#setAlpha(float) alpha}.
 *
 * <p>This property assumes that it is the only component responsible for the children's {@link
 * View#setAlpha(float) alpha}.
 */
public class ChildrenAlphaProperty extends Property<ViewGroup, Float> {

  /**
   * A Property wrapper around the <code>alpha</code> functionality of a ViewGroup's children.
   */
  public static final Property<ViewGroup, Float> CHILDREN_ALPHA =
      new ChildrenAlphaProperty("childrenAlpha");

  private ChildrenAlphaProperty(String name) {
    super(Float.class, name);
  }

  @NonNull
  @Override
  public Float get(@NonNull ViewGroup object) {
    Float alpha = (Float) object.getTag(R.id.mtrl_internal_children_alpha_tag);
    if (alpha != null) {
      return alpha;
    } else {
      return 1f;
    }
  }

  @Override
  public void set(@NonNull ViewGroup object, @NonNull Float value) {
    float alpha = value;

    object.setTag(R.id.mtrl_internal_children_alpha_tag, alpha);

    for (int i = 0, count = object.getChildCount(); i < count; i++) {
      View child = object.getChildAt(i);
      child.setAlpha(alpha);
    }
  }
}
