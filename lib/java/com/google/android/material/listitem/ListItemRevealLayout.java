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

import static java.lang.Math.max;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.google.android.material.animation.AnimationUtils;
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


  // TODO:b/443149411 - Make the min child width customizable
  private static final int MIN_CHILD_WIDTH = 15;
  private int originalWidthMeasureSpec = UNSET;
  private int originalHeightMeasureSpec = UNSET;

  public ListItemRevealLayout(Context context) {
    this(context, null);
  }

  public ListItemRevealLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ListItemRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setClipToPadding(false);
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
    } else if (revealedWidth > intrinsicWidth + overswipeAllowance
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
    // Expand only the last visible child and shrink other visible children to the min child size
    // We keep the margins the same even as we shrink the other children
    Integer lastVisibleChildIndex = findLastVisibleChildIndex();
    if (lastVisibleChildIndex != null) {
      int targetWidthMinusLastChild = getPaddingStart() + getPaddingEnd();
      // when progress is 0, we are at the intrinsic width
      // when progress is 1, we are at fully swiped width
      float progress =
          (float) (revealedWidth - intrinsicWidth) / (fullRevealableWidth - intrinsicWidth);
      for (int i = 0; i < lastVisibleChildIndex; i++) {
        View child = getChildAt(i);
        if (child.getVisibility() == GONE) {
          continue;
        }
        child.measure(
            MeasureSpec.makeMeasureSpec(
                AnimationUtils.lerp(
                    max(originalChildWidths[i], MIN_CHILD_WIDTH), MIN_CHILD_WIDTH, progress),
                MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(originalChildHeights[i], MeasureSpec.EXACTLY));
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        targetWidthMinusLastChild += lp.leftMargin + lp.rightMargin + MIN_CHILD_WIDTH;
      }
      View lastChild = getChildAt(lastVisibleChildIndex);
      final MarginLayoutParams lp = (MarginLayoutParams) lastChild.getLayoutParams();
      int lastChildTargetWidth =
          fullRevealableWidth - targetWidthMinusLastChild - lp.rightMargin - lp.leftMargin;
      // This is not an intended use case, but if for some reason the revealed width is set to be
      // larger than the full revealable width (the swipe view width), we'll just add the extra
      // width to the last child.
      int extraLastChildWidth = Math.max((revealedWidth - fullRevealableWidth), 0);
      lastChild.measure(
          MeasureSpec.makeMeasureSpec(
              AnimationUtils.lerp(
                      originalChildWidths[lastVisibleChildIndex], lastChildTargetWidth, progress)
                  + extraLastChildWidth,
              MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(
              originalChildHeights[lastVisibleChildIndex], MeasureSpec.EXACTLY));
    }
    setMeasuredDimension(revealedWidth, intrinsicHeight);
  }

  private void measureByPreservingSwipeActionRatios(int childCount) {
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
      int childWidth = max(MIN_CHILD_WIDTH, (int) (originalChildWidths[i] * ratio));
      child.measure(
          MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(originalChildHeights[i], MeasureSpec.EXACTLY));
      final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
      int adjustedLeftMargin = (int) (lp.leftMargin * ratio);
      int adjustedRightMargin = (int) (lp.rightMargin * ratio);
      realWidth += childWidth + adjustedLeftMargin + adjustedRightMargin;
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
}
