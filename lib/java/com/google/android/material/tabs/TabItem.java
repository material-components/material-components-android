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

package com.google.android.material.tabs;

import com.google.android.material.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.View;

/**
 * TabItem is a special 'view' which allows you to declare tab items for a {@link TabLayout} within
 * a layout. This view is not actually added to TabLayout, it is just a dummy which allows setting
 * of a tab items's text, icon and custom layout. See TabLayout for more information on how to use
 * it.
 *
 * @attr ref com.google.android.material.R.styleable#TabItem_android_icon
 * @attr ref com.google.android.material.R.styleable#TabItem_android_text
 * @attr ref com.google.android.material.R.styleable#TabItem_android_layout
 * @see TabLayout
 */
//TODO(b/76413401): make class final after the widget migration
public class TabItem extends View {
  //TODO(b/76413401): make package private after the widget migration
  public final CharSequence text;
  //TODO(b/76413401): make package private after the widget migration
  public final Drawable icon;
  //TODO(b/76413401): make package private after the widget migration
  public final int customLayout;

  public TabItem(Context context) {
    this(context, null);
  }

  public TabItem(Context context, AttributeSet attrs) {
    super(context, attrs);

    final TintTypedArray a =
        TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.TabItem);
    text = a.getText(R.styleable.TabItem_android_text);
    icon = a.getDrawable(R.styleable.TabItem_android_icon);
    customLayout = a.getResourceId(R.styleable.TabItem_android_layout, 0);
    a.recycle();
  }
}
