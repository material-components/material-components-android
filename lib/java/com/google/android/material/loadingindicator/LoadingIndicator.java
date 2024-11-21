/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.material.loadingindicator;

import com.google.android.material.R;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.min;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ProgressBar;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.AnimatorDurationScaleProvider;
import java.util.Arrays;

/** This class implements the loading indicators. */
public final class LoadingIndicator extends View implements Drawable.Callback {
  static final int DEF_STYLE_RES = R.style.Widget_Material3_LoadingIndicator;

  @NonNull private final LoadingIndicatorDrawable drawable;
  @NonNull private final LoadingIndicatorSpec specs;

  public LoadingIndicator(@NonNull Context context) {
    this(context, null);
  }

  public LoadingIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.loadingIndicatorStyle);
  }

  public LoadingIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

    // Ensures that we are using the correctly themed context rather than the context that was
    // passed in.
    context = getContext();

    drawable =
        LoadingIndicatorDrawable.create(
            context, new LoadingIndicatorSpec(context, attrs, defStyleAttr));
    drawable.setCallback(this);

    specs = drawable.getDrawingDelegate().specs;
    setAnimatorDurationScaleProvider(new AnimatorDurationScaleProvider());
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    LoadingIndicatorDrawingDelegate drawingDelegate = drawable.getDrawingDelegate();

    int preferredWidth = drawingDelegate.getPreferredWidth() + getPaddingLeft() + getPaddingRight();
    int preferredHeight =
        drawingDelegate.getPreferredHeight() + getPaddingTop() + getPaddingBottom();

    if (widthMode == AT_MOST) {
      widthMeasureSpec = makeMeasureSpec(min(widthSize, preferredWidth), EXACTLY);
    } else if (widthMode == UNSPECIFIED) {
      widthMeasureSpec = makeMeasureSpec(preferredWidth, EXACTLY);
    }

    if (heightMode == AT_MOST) {
      heightMeasureSpec = makeMeasureSpec(min(heightSize, preferredHeight), EXACTLY);
    } else if (heightMode == UNSPECIFIED) {
      heightMeasureSpec = makeMeasureSpec(preferredHeight, EXACTLY);
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    int saveCount = canvas.save();
    if (getPaddingLeft() != 0 || getPaddingTop() != 0) {
      canvas.translate(getPaddingLeft(), getPaddingTop());
    }
    if (getPaddingRight() != 0 || getPaddingBottom() != 0) {
      int w = getWidth() - (getPaddingLeft() + getPaddingRight());
      int h = getHeight() - (getPaddingTop() + getPaddingBottom());
      canvas.clipRect(0, 0, w, h);
    }

    drawable.draw(canvas);

    canvas.restoreToCount(saveCount);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    drawable.setBounds(0, 0, w, h);
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    drawable.setVisible(
        visibleToUser(), /* restart= */ false, /* animate= */ visibility == VISIBLE);
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    drawable.setVisible(
        visibleToUser(), /* restart= */ false, /* animate= */ visibility == VISIBLE);
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable drawable) {
    invalidate();
  }

  @NonNull
  @Override
  public CharSequence getAccessibilityClassName() {
    return ProgressBar.class.getName();
  }

  /**
   * Returns whether or not this view is currently displayed in window, based on whether it is
   * attached to a window and whether it and its ancestors are visible.
   */
  boolean visibleToUser() {
    return isAttachedToWindow() && getWindowVisibility() == View.VISIBLE && isEffectivelyVisible();
  }

  /**
   * Returns whether or not this view and all of its ancestors are visible (and thus it is
   * effectively visible to the user). This is *very* similar to {@link #isShown()}, except that
   * when attached to a visible window, it will treat a null ViewParent in the hierarchy as being
   * visible (whereas {@link #isShown()} treats this case as non-visible).
   *
   * <p>In cases where the return value of this method differs from {@link #isShown()} (which
   * generally only occur through usage of {@link android.view.ViewGroup#detachViewFromParent(View)}
   * by things like {@link android.widget.ListView} and {@link android.widget.Spinner}), this view
   * would still be attached to a window (meaning it's mAttachInfo is non-null), but it or one of
   * its ancestors would have had its {@code mParent} reference directly set to null by the
   * aforementioned method. In correctly written code, this is a transient state, but this transient
   * state often includes things that may trigger a view to re-check its visibility (like re-binding
   * a view for view recycling), and thus {@link #isShown()} can return false negatives.
   *
   * <p>This is necessary as before API 24, it is not guaranteed that Views will ever be notified
   * about their parent changing. Thus, we don't have a proper point to hook in and re-check {@link
   * #isShown()} on parent changes that result from {@link
   * android.view.ViewGroup#attachViewToParent(View, int, LayoutParams)}, which *can* change our
   * effective visibility. So this method errs on the side of assuming visibility unless we can
   * conclusively prove otherwise (but may result in some false positives, if this view ends up
   * being attached to a non-visible hierarchy after being detached in a visible state).
   */
  boolean isEffectivelyVisible() {
    View current = this;
    do {
      if (current.getVisibility() != VISIBLE) {
        return false;
      }
      ViewParent parent = current.getParent();
      if (parent == null) {
        return getWindowVisibility() == View.VISIBLE;
      }
      if (!(parent instanceof View)) {
        return true;
      }
      current = (View) parent;
    } while (true);
  }

  // ******************* Getters and Setters *******************

  /** Returns the {@link LoadingIndicatorDrawable} object used in this loading indicator. */
  @NonNull
  public LoadingIndicatorDrawable getDrawable() {
    return drawable;
  }

  /**
   * Sets a new indicator size for this loading indicator.
   *
   * @param indicatorSize The new indicator size in px.
   */
  public void setIndicatorSize(@Px int indicatorSize) {
    if (specs.indicatorSize != indicatorSize) {
      specs.indicatorSize = indicatorSize;
      requestLayout();
      invalidate();
    }
  }

  /** Returns the indicator size for this loading indicator in px. */
  @Px
  public int getIndicatorSize() {
    return specs.indicatorSize;
  }

  /**
   * Sets a new container width for this loading indicator.
   *
   * @param containerWidth The new container width in px.
   */
  public void setContainerWidth(@Px int containerWidth) {
    if (specs.containerWidth != containerWidth) {
      specs.containerWidth = containerWidth;
      requestLayout();
      invalidate();
    }
  }

  /** Returns the container width for this loading indicator in px. */
  @Px
  public int getContainerWidth() {
    return specs.containerWidth;
  }

  /**
   * Sets a new container height for this loading indicator.
   *
   * @param containerHeight The new container height in px.
   */
  public void setContainerHeight(@Px int containerHeight) {
    if (specs.containerHeight != containerHeight) {
      specs.containerHeight = containerHeight;
      requestLayout();
      invalidate();
    }
  }

  /** Returns the container height for this loading indicator in px. */
  @Px
  public int getContainerHeight() {
    return specs.containerHeight;
  }

  /**
   * Sets a new indicator color (or a sequence of indicator colors) for this loading indicator.
   *
   * @param indicatorColors The new indicator color(s).
   */
  public void setIndicatorColor(@ColorInt int... indicatorColors) {
    if (indicatorColors.length == 0) {
      // Uses theme primary color for indicator by default. Indicator color cannot be empty.
      indicatorColors = new int[] {MaterialColors.getColor(getContext(), R.attr.colorPrimary, -1)};
    }
    if (!Arrays.equals(getIndicatorColor(), indicatorColors)) {
      specs.indicatorColors = indicatorColors;
      drawable.getAnimatorDelegate().invalidateSpecValues();
      invalidate();
    }
  }

  /** Returns the indicator color(s) for this loading indicator in an int array. */
  @NonNull
  public int[] getIndicatorColor() {
    return specs.indicatorColors;
  }

  /**
   * Sets a new container color for this loading indicator.
   *
   * @param containerColor A new container color.
   */
  public void setContainerColor(@ColorInt int containerColor) {
    if (specs.containerColor != containerColor) {
      specs.containerColor = containerColor;
      invalidate();
    }
  }

  /** Returns the container color for this loading indicator. */
  @ColorInt
  public int getContainerColor() {
    return specs.containerColor;
  }

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @VisibleForTesting
  public void setAnimatorDurationScaleProvider(
      @NonNull AnimatorDurationScaleProvider animatorDurationScaleProvider) {
    drawable.animatorDurationScaleProvider = animatorDurationScaleProvider;
  }
}
