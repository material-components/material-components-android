/*
 * Copyright 2025 The Android Open Source Project
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
package com.google.android.material.listitem;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.card.MaterialCardView;

/**
 * A {@link MaterialCardView} that is styled as a list item and can be swiped in a
 * {@link ListItemLayout} with a sibling {@link RevealableListItem}.
 */
public class ListItemCardView extends MaterialCardView implements SwipeableListItem {

  private static final int DEF_STYLE_RES = R.style.Widget_Material3_ListItemCardView;

  public ListItemCardView(Context context) {
    this(context, null);
  }

  public ListItemCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.listItemCardViewStyle);
  }

  public ListItemCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
  }
}
