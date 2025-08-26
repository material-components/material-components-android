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
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A container layout for a List item.
 *
 * <p>This layout applies the following states according to its position in a list:
 *
 * <ul>
 *   <li>{@link android.R.attr.state_first}
 *   <li>{@link android.R.attr.state_last}
 *   <li>{@link android.R.attr.state_middle}
 *   <li>{@link android.R.attr.state_single}
 * </ul>
 *
 * <p>Children of ListItemLayout that wish to be affected by the ListItemLayout's position state
 * should duplicate the state through setting {@link android.R.attr.duplicateParentState} to true.
 *
 * <p>MaterialCardView is recommended as a ListItemLayout child, as it supports updating its shape /
 * corners based on states.
 */
public class ListItemLayout extends FrameLayout {

  private static final int[] FIRST_STATE_SET = { android.R.attr.state_first };
  private static final int[] MIDDLE_STATE_SET = { android.R.attr.state_middle };
  private static final int[] LAST_STATE_SET = { android.R.attr.state_last };
  private static final int[] SINGLE_STATE_SET = { android.R.attr.state_single };

  @Nullable private int[] positionState;

  public ListItemLayout(@NonNull Context context) {
    this(context, null);
  }

  public ListItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.listItemLayoutStyle);
  }

  public ListItemLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, R.attr.listItemLayoutStyle);
  }

  public ListItemLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (positionState == null) {
      return super.onCreateDrawableState(extraSpace);
    }
    int[] drawableState = super.onCreateDrawableState(extraSpace+1);
    return mergeDrawableStates(drawableState, positionState);
  }

  /**
   * Helper method that sets the drawable state of the ListItemLayout according to its position in
   * the list. This is already called by {@link ListItemViewHolder#bind} if the ListItemLayout is
   * inside of a {@link ListItemViewHolder}.
   *
   * <p>Children of ListItemLayout that wish to be affected by this state should duplicate its
   * parent's state.
   */
  public void updateAppearance(int position, int itemCount) {
    if (position < 0 || itemCount < 0) {
      positionState = null;
    } else if (itemCount == 1) {
      positionState = SINGLE_STATE_SET;
    } else if (position == 0) {
      positionState = FIRST_STATE_SET;
    } else if (position == itemCount - 1) {
      positionState = LAST_STATE_SET;
    } else {
      positionState = MIDDLE_STATE_SET;
    }
    refreshDrawableState();
  }
}
