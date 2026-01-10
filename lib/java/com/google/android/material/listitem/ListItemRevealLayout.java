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
import static java.lang.Math.max;

import android.content.Context;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.internal.ThemeEnforcement;
import java.lang.ref.WeakReference;

/**
 * Layout that is intended to be used as the {@link android.view.ViewGroup} that is revealed when a
 * a {@link SwipeableListItem} is swiped. This layout always measures its width intrinsically, and
 * uses its direct children's intrinsic width ratios to re-size them accordingly when the layout
 * width changes.
 *
 * <p>Eg. If ListItemRevealLayout has an intrinsic width of 100 pixels with the first child having
 * an intrinsic width of 20 and the second child having an intrinsic width of 80, when the
 * ListItemRevealLayout changes to a width of 50, the ratios will be kept the same. The first child
 * will have a width of 10 and the second will have a width of 40.
 *
 * <p>ListItemRevealLayout will be measured at 0 pixels if the desired width is not set.
 */
public class ListItemRevealLayout extends ViewGroup implements RevealableListItem {

  private static final int UNSET = -1;
  private int intrinsicWidth = UNSET;
  private int intrinsicHeight = UNSET;
  private int revealedWidth = 0;
  private int[] originalChildWidths;
  private int[] originalChildHeights;
  @Nullable private WeakReference<View> siblingSwipeableView;

  private int minChildWidth;
  private int originalWidthMeasureSpec = UNSET;
  private int originalHeightMeasureSpec = UNSET;

  @PrimaryActionSwipeMode
  private int primaryActionSwipeMode;

  public ListItemRevealLayout(Context context) {
    this(context, null);
  }

  public ListItemRevealLayout(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.listItemRevealLayoutStyle);
  }

  public ListItemRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, R.style.Widget_Material3_ListItemRevealLayout);
  }

  public ListItemRevealLayout(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    setClipToPadding(false);

    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.ListItemRevealLayout, defStyleAttr, defStyleRes);

    minChildWidth =
        attributes.getDimensionPixelSize(
            R.styleable.ListItemRevealLayout_minChildWidth,
            getResources().getDimensionPixelSize(R.dimen.m3_list_reveal_min_child_width));
    primaryActionSwipeMode =
        attributes.getInt(
            R.styleable.ListItemRevealLayout_primaryActionSwipeMode, PRIMARY_ACTION_SWIPE_DISABLED);
    attributes.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int childCount = getChildCount();
    if (shouldRemeasureIntrinsicSizes(originalHeightMeasureSpec, heightMeasureSpec, intrinsicHeight)
        || shouldRemeasureIntrinsicSizes(
            originalWidthMeasureSpec, widthMeasureSpec, intrinsicWidth)) {
      originalHeightMeasureSpec = heightMeasureSpec;
      originalWidthMeasureSpec = widthMeasureSpec;
      measureIntrinsicSize(widthMeasureSpec, heightMeasureSpec);
      // At this point all the children are measured and we have our intrinsic sizes, so we can
      // go through and save the original intrinsic child sizes
      saveOriginalChildSizes(childCount);
    }

    if (siblingSwipeableView == null || siblingSwipeableView.get() == null) {
      siblingSwipeableView = new WeakReference<>(findSiblingSwipeableView());
    }
    int overswipeAllowance =
        siblingSwipeableView.get() != null
            ? ((SwipeableListItem) siblingSwipeableView.get()).getSwipeMaxOvershoot()
            : 0;
    int fullRevealableWidth = calculateFullRevealableWidth();
    setVisibility(revealedWidth == 0 ? INVISIBLE : VISIBLE);

    if (revealedWidth == 0) {
      // If the desired width is 0, we want to measure the width as 0 so this layout is not
      // shown at all
      setMeasuredDimension(0, intrinsicHeight);
    } else if (childCount == 0) {
      // If there's no children, just set to desired width without doing anything.
      setMeasuredDimension(revealedWidth, intrinsicHeight);
    } else if (primaryActionSwipeMode != PRIMARY_ACTION_SWIPE_DISABLED
        && revealedWidth > intrinsicWidth + overswipeAllowance
        && fullRevealableWidth > intrinsicWidth) {
      measureByGrowingPrimarySwipeAction(fullRevealableWidth);
    } else {
      measureByPreservingSwipeActionRatios(childCount);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    float ratio = revealedWidth >= intrinsicWidth ? 1f : (float) revealedWidth / intrinsicWidth;
    int currentLeft = (int) (getPaddingLeft() * ratio);
    final int paddingTop = getPaddingTop();
    final int count = getChildCount();

    int start = 0;
    int dir = 1;
    // In case of RTL, start drawing from the last child.
    if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
      start = count - 1;
      dir = -1;
    }
    for (int i = 0; i < count; i++) {
      final int childIndex = start + dir * i;
      final View child = getChildAt(childIndex);
      if (child.getVisibility() == GONE) {
        continue;
      }

      final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

      final int childWidth = child.getMeasuredWidth();
      final int childHeight = child.getMeasuredHeight();

      // Calculate the top and left coordinates for the current child.
      final int childTop = paddingTop + lp.topMargin;
      int adjustedLeftMargin = (int) (lp.leftMargin * ratio);
      int adjustedRightMargin = (int) (lp.rightMargin * ratio);
      final int childLeft = currentLeft + adjustedLeftMargin;

      child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

      // Update 'currentLeft' to position the next child to the right of this one.
      currentLeft += adjustedLeftMargin + childWidth + adjustedRightMargin;
    }
  }

  private boolean shouldRemeasureIntrinsicSizes(
      int originalMeasureSpec, int newMeasureSpec, int intrinsicSize) {
    // We only want to measure the intrinsic size if we don't know it yet, OR if the measure spec
    // has changed and it might be different than the existing intrinsic measured size. We assume
    // that if the MeasureSpec mode is UNSPECIFIED, we can use the existing intrinsic sizes. This
    // is to prevent unnecessary re-measuring when our parent continually gives us an unspecified
    // measure spec.
    if (intrinsicSize == UNSET) {
      return true;
    }
    if (originalMeasureSpec == newMeasureSpec) {
      return false;
    }
    int mode = MeasureSpec.getMode(newMeasureSpec);
    // We don't want to re-measure if the new measure spec is UNSPECIFIED, or if it's EXACTLY
    // the same as the intrinsic size.
    return mode != MeasureSpec.UNSPECIFIED
        && (mode != MeasureSpec.EXACTLY || MeasureSpec.getSize(newMeasureSpec) != intrinsicSize);
  }

  void measureIntrinsicSize(int widthMeasureSpec, int heightMeasureSpec) {
    int totalWidth = 0;
    int maxHeight = 0;
    final int childCount = getChildCount();
    // Used to combine child measurement states; important for passing on the
    // MEASURED_STATE_TOO_SMALL flag to pass along to parent layouts. We're only concerned about
    // this for the measured height, since children are always measured to exactly fit the width of
    // ListItemRevealLayout.
    int childState = 0;

    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
      // Determine how big this child would like to be.
      measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, 0);
      totalWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
      maxHeight = max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

      // Combine the measured states of the children. This is important for telling
      // the parent layout if a child would like to be bigger.
      childState = combineMeasuredStates(childState, child.getMeasuredState());
    }

    // Add in our padding
    totalWidth += getPaddingLeft() + getPaddingRight();

    // Check against our minimum width
    totalWidth = max(totalWidth, getSuggestedMinimumWidth());

    maxHeight += getPaddingTop() + getPaddingBottom();

    // Check against our minimum height
    maxHeight = max(maxHeight, getSuggestedMinimumHeight());

    // Reconcile our calculated size with the heightMeasureSpec
    int heightSizeAndState = resolveSizeAndState(maxHeight, heightMeasureSpec, 0);
    maxHeight = heightSizeAndState & MEASURED_SIZE_MASK;

    // Set the final measured dimensions for this ViewGroup.
    intrinsicWidth = resolveSizeAndState(totalWidth, widthMeasureSpec, 0);
    intrinsicHeight =
        resolveSizeAndState(
            maxHeight, heightMeasureSpec, (childState << MEASURED_HEIGHT_STATE_SHIFT));
  }

  private void measureByGrowingPrimarySwipeAction(int fullRevealableWidth) {
    boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    boolean expandFirst = ListItemUtils.isRightAligned(this) == isRtl;
    Integer targetChildIndex =
        expandFirst ? findFirstVisibleChildIndex() : findLastVisibleChildIndex();

    if (targetChildIndex != null) {
      int targetWidthMinusTargetChild = getPaddingStart() + getPaddingEnd();
      // when progress is 0, we are at the intrinsic width
      // when progress is 1, we are at fully swiped width
      float progress =
          Math.max(
              0f,
              Math.min(
                  1f,
                  (float) (revealedWidth - intrinsicWidth)
                      / (fullRevealableWidth - intrinsicWidth)));

      int childCount = getChildCount();
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (child.getVisibility() == GONE) {
          continue;
        }

        // Skip the target child since we will measure it last with the remaining space
        if (i == targetChildIndex) {
          continue;
        }

        child.measure(
            MeasureSpec.makeMeasureSpec(
                AnimationUtils.lerp(
                    max(originalChildWidths[i], minChildWidth), minChildWidth, progress),
                MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(originalChildHeights[i], MeasureSpec.EXACTLY));
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        targetWidthMinusTargetChild += lp.leftMargin + lp.rightMargin + minChildWidth;

        if (child instanceof MaterialButton && ((MaterialButton) child).getIcon() != null) {
          ((MaterialButton) child).getIcon().setAlpha(AnimationUtils.lerp(255, 0, progress));
        }
      }

      View targetChild = getChildAt(targetChildIndex);
      final MarginLayoutParams lp = (MarginLayoutParams) targetChild.getLayoutParams();
      int targetChildAvailableWidth =
          fullRevealableWidth - targetWidthMinusTargetChild - lp.rightMargin - lp.leftMargin;

      // This is not an intended use case, but if for some reason the revealed width is set to be
      // larger than the full revealable width (the swipe view width), we'll just add the extra
      // width to the target child.
      int extraTargetChildWidth = Math.max((revealedWidth - fullRevealableWidth), 0);

      targetChild.measure(
          MeasureSpec.makeMeasureSpec(
              AnimationUtils.lerp(
                      originalChildWidths[targetChildIndex], targetChildAvailableWidth, progress)
                  + extraTargetChildWidth,
              MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(originalChildHeights[targetChildIndex], MeasureSpec.EXACTLY));
      if (targetChild instanceof MaterialButton
          && ((MaterialButton) targetChild).getIcon() != null) {
        ((MaterialButton) targetChild).getIcon().setAlpha(255);
      }
    }
    setMeasuredDimension(revealedWidth, intrinsicHeight);
  }

  private void measureByPreservingSwipeActionRatios(int childCount) {
    // Calculate alpha for any MaterialButtons in the ListItemRevealLayout
    // The icon should start fading in at 25% of the intrinsic width, and finish fading in at 50% of
    // the intrinsic width.
    int materialButtonAlpha =
        (int)
            AnimationUtils.lerp(
                0, 255, (float) intrinsicWidth / 4, (float) intrinsicWidth / 2, revealedWidth);

    // This measures all children to keep the same intrinsic ratios no matter what the measure
    // policy is. This case covers all other cases, including the case where fullRevealableWidth
    // is smaller than the intrinsic width, which means that there's not enough room to even grow
    // past the intrinsic width (eg. the swipe sibling is smaller than the intrinsic width) so
    // the expand last child policy should default to this measure policy.
    float ratio = (float) revealedWidth / intrinsicWidth;
    int realWidth = 0;
    int adjustedPaddingLeft = (int) (getPaddingLeft() * ratio);
    int adjustedPaddingRight = (int) (getPaddingRight() * ratio);
    for (int i = 0; i < childCount; i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      // We want to keep the intrinsic child ratios the same
      int childWidth = max(minChildWidth, (int) (originalChildWidths[i] * ratio));
      child.measure(
          MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(originalChildHeights[i], MeasureSpec.EXACTLY));
      final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
      int adjustedLeftMargin = (int) (lp.leftMargin * ratio);
      int adjustedRightMargin = (int) (lp.rightMargin * ratio);
      realWidth += childWidth + adjustedLeftMargin + adjustedRightMargin;

      if (child instanceof MaterialButton && ((MaterialButton) child).getIcon() != null) {
        ((MaterialButton) child).getIcon().setAlpha(materialButtonAlpha);
      }
    }
    // revealedWidth and realWidth should be the same apart from the minimum child restrictions,
    // but because of rounding, revealedWidth may be a few pixels bigger. Thus we take the
    // max.
    setMeasuredDimension(
        max(revealedWidth, realWidth + adjustedPaddingLeft + adjustedPaddingRight),
        intrinsicHeight);
  }

  private void saveOriginalChildSizes(int childCount) {
    originalChildWidths = new int[childCount];
    originalChildHeights = new int[childCount];
    for (int i = 0; i < childCount; ++i) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        originalChildWidths[i] = child.getMeasuredWidth();
        originalChildHeights[i] = child.getMeasuredHeight();
        // If the original child height was MATCH_PARENT, we set the height to the intrinsic height
        // instead.
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        if (lp.height == LayoutParams.MATCH_PARENT) {
          // update height with intrinsic height now
          originalChildHeights[i] = intrinsicHeight;
        }
      }
    }
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new MarginLayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(LayoutParams p) {
    return new MarginLayoutParams(p);
  }

  /**
   * Resets the intrinsic width remembered by the ListItemRevealLayout. This will cause a re-measure
   * of the ListItemRevealLayout and its children, and should only be called when a re-measure is
   * necessary due to a width change in the layout or any of its children (eg. if a child's
   * visibility changes).
   */
  public void resetIntrinsicWidth() {
    intrinsicWidth = UNSET;
    requestLayout();
  }

  @Override
  @Px
  public int getIntrinsicWidth() {
    return intrinsicWidth != UNSET ? intrinsicWidth : 0;
  }

  @Override
  public void setRevealedWidth(int revealedWidth) {
    revealedWidth = max(0, revealedWidth);
    if (this.revealedWidth == revealedWidth) {
      return;
    }
    this.revealedWidth = revealedWidth;
    requestLayout();
  }

  private int calculateFullRevealableWidth() {
    if (siblingSwipeableView != null && siblingSwipeableView.get() != null) {
      return siblingSwipeableView.get().getMeasuredWidth();
    } else if (getParent() instanceof View) {
      return ((View) getParent()).getMeasuredWidth();
    } else {
      return intrinsicWidth;
    }
  }

  // Returns the sibling SwipeableListItem if it exists.
  @Nullable
  private View findSiblingSwipeableView() {
    if (!(getParent() instanceof ViewGroup)) {
      return null;
    }
    ViewGroup parent = (ViewGroup) getParent();
    int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = parent.getChildAt(i);
      if (child instanceof SwipeableListItem) {
        return child;
      }
    }
    return null;
  }

  /**
   * Sets the minimum width, in pixels, that the children of ListItemRevealLayout can be measured to
   * be.
   */
  public void setMinChildWidth(@Px int minChildWidth) {
    if (this.minChildWidth == minChildWidth) {
      return;
    }
    this.minChildWidth = minChildWidth;
    requestLayout();
  }

  /**
   * Sets the minimum width, in pixels, that the children of ListItemRevealLayout can be measured to
   * be.
   */
  @Px
  public int getMinChildWidth() {
    return minChildWidth;
  }

  @Nullable
  private Integer findLastVisibleChildIndex() {
    int childCount = getChildCount();
    for (int i = childCount - 1; i >= 0; i--) {
      if (getChildAt(i).getVisibility() != GONE) {
        return i;
      }
    }
    return null;
  }

  @Nullable
  private Integer findFirstVisibleChildIndex() {
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      if (getChildAt(i).getVisibility() != GONE) {
        return i;
      }
    }
    return null;
  }

  /**
   * Sets the swipe-to-primary-action behavior of this RevealableListItem when swiping with a
   * sibling {@link SwipeableListItem}.
   *
   * <p>Use {@link SwipeableListItem#onSwipeStateChanged(int, View, int)} to listen for when the
   * primary action is triggered to initiate the action.
   */
  @Override
  public void setPrimaryActionSwipeMode(@PrimaryActionSwipeMode int primaryActionSwipeMode) {
    this.primaryActionSwipeMode = primaryActionSwipeMode;
  }

  @Override
  @PrimaryActionSwipeMode
  public int getPrimaryActionSwipeMode() {
    return primaryActionSwipeMode;
  }
}
