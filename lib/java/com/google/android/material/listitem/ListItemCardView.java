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
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.internal.ThemeEnforcement;

/**
 * A {@link MaterialCardView} that is styled as a list item and can be swiped in a
 * {@link ListItemLayout} with a sibling {@link RevealableListItem}.
 */
public class ListItemCardView extends MaterialCardView implements SwipeableListItem {

  private static final int[] SWIPED_STATE_SET = {R.attr.state_swiped};

  private boolean isSwiped = false;

  private final int swipeMaxOvershoot;
  private boolean swipeToPrimaryActionEnabled;

  public ListItemCardView(Context context) {
    this(context, null);
  }

  public ListItemCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.listItemCardViewStyle);
  }

  public ListItemCardView(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    this(context, attrs, defStyleAttr, R.style.Widget_Material3_ListItemCardView);
  }

  public ListItemCardView(Context context, AttributeSet attrs, int defStyleAttr, @StyleRes int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    swipeMaxOvershoot = getResources().getDimensionPixelSize(R.dimen.m3_list_max_swipe_overshoot);

    /* Custom attributes */
    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.ListItemCardView, defStyleAttr, defStyleRes);
    swipeToPrimaryActionEnabled = attributes.getBoolean(R.styleable.ListItemCardView_swipeToPrimaryActionEnabled, false);
    attributes.recycle();
  }

  @Override
  public int getSwipeMaxOvershoot() {
    return swipeMaxOvershoot;
  }

  /**
   * Set whether or not to enable the swipe to action. This enables the ListItemCardView to be
   * swiped fully out of its parent {@link ListItemLayout}, in order to trigger an action.
   */
  // TODO(b/447226552): Link the onSwipeStateChanged listener here when ready
  public void setSwipeToPrimaryActionEnabled(boolean swipeToPrimaryActionEnabled) {
    this.swipeToPrimaryActionEnabled = swipeToPrimaryActionEnabled;
  }

  /** Returns whether or not the swipe to action is enabled. */
  @Override
  public boolean isSwipeToPrimaryActionEnabled() {
    return swipeToPrimaryActionEnabled;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (isSwiped) {
      mergeDrawableStates(drawableState, SWIPED_STATE_SET);
    }
    return drawableState;
  }

  @Override
  public void onSwipeStateChanged(int swipeState) {
    isSwiped = swipeState != STATE_CLOSED;
    refreshDrawableState();
  }
}
