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
import android.view.View;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.listitem.RevealableListItem.RevealGravity;
import java.util.LinkedHashSet;

/**
 * A {@link MaterialCardView} that is styled as a list item and can be swiped in a
 * {@link ListItemLayout} with a sibling {@link RevealableListItem}.
 */
public class ListItemCardView extends MaterialCardView implements SwipeableListItem {

  /** Callback for changes to the {@link SwipeState} of the ListItemCardView. */
  public abstract static class SwipeCallback {

    /**
     * Called when the position of the SwipeableListItem changes.
     *
     * @param swipeOffset The offset from the original position of the SwipeableListItem, in pixels.
     */
    public abstract void onSwipe(@Px int swipeOffset);

    /**
     * Called when the swipe state of the SwipeableListItem changes.
     *
     * @param newState The new state. This will be one of {@link #STATE_DRAGGING}, {@link
     *     #STATE_SETTLING}, {@link #STATE_CLOSED}, {@link #STATE_OPEN}, or {@link
     *     #STATE_SWIPE_PRIMARY_ACTION}.
     * @param activeRevealableListItem The associated {@link RevealableListItem} view that is being
     *     revealed when swiped. If the new state is {@link #STATE_CLOSED}, this will be the last
     *     active {@link RevealableListItem}.
     * @param revealGravity The {@link RevealGravity} of the revealableListItem.
     */
    public abstract <T extends View & RevealableListItem> void onSwipeStateChanged(
        @SwipeState int newState,
        @NonNull T activeRevealableListItem,
        @RevealGravity int revealGravity);
  }

  private static final int[] SWIPED_STATE_SET = {R.attr.state_swiped};

  private boolean isSwiped = false;

  private final int swipeMaxOvershoot;
  private boolean swipeEnabled;
  @NonNull private final LinkedHashSet<SwipeCallback> swipeCallbacks = new LinkedHashSet<>();

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
    swipeEnabled = attributes.getBoolean(R.styleable.ListItemCardView_swipeEnabled, true);
    attributes.recycle();
  }

  @Override
  public int getSwipeMaxOvershoot() {
    return swipeMaxOvershoot;
  }

  /**
   * Whether or not to enabling swiping when there is a sibling {@link RevealableListItem}.
   */
  @Override
  public void setSwipeEnabled(boolean swipeEnabled) {
    this.swipeEnabled = swipeEnabled;
  }

  @Override
  public boolean isSwipeEnabled() {
    return swipeEnabled;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (isSwiped) {
      mergeDrawableStates(drawableState, SWIPED_STATE_SET);
    }
    return drawableState;
  }

  /** Add a callback to be invoked when the swipe state of the SwipeableListItem changes. */
  public void addSwipeCallback(@NonNull SwipeCallback callback) {
    swipeCallbacks.add(callback);
  }

  /**
   * Remove the specified callback from being called when the swipe state of the SwipeableListItem
   * changes.
   */
  public void removeSwipeCallback(@NonNull SwipeCallback callback) {
    swipeCallbacks.remove(callback);
  }

  @Override
  public void onSwipe(@Px int swipeOffset) {
    for (SwipeCallback callback : swipeCallbacks) {
      callback.onSwipe(swipeOffset);
    }
  }

  @Override
  public <T extends View & RevealableListItem> void onSwipeStateChanged(
      int swipeState, T revealableListItem, int revealGravity) {
    isSwiped = swipeState != STATE_CLOSED;
    refreshDrawableState();

    for (SwipeCallback callback : swipeCallbacks) {
      callback.onSwipeStateChanged(swipeState, revealableListItem, revealGravity);
    }
  }
}
