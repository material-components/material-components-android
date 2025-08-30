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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.AnimatorDurationScaleProvider;
import java.util.Arrays;

/** This class implements the loading indicators. */
public final class LoadingIndicator extends View implements Drawable.Callback {
  static final int DEF_STYLE_RES = R.style.Widget_Material3_LoadingIndicator;

  /**
   * The maximum time, in milliseconds, that the requested hide action is allowed to wait once
   * {@link #show()} is called.
   */
  static final int MAX_HIDE_DELAY = 1000;

  @NonNull private final LoadingIndicatorDrawable drawable;
  @NonNull private final LoadingIndicatorSpec specs;

  /**
   * The time, in milliseconds, that the loading indicator will wait to show once the component
   * becomes visible. If set to zero (as default) or negative values, the show action will start
   * immediately.
   */
  private final int showDelay;

  /**
   * The minimum time, in milliseconds, that the requested hide action will wait to start once
   * {@link #show()} is called. If set to zero or negative values, the requested hide action will
   * start as soon as {@link #hide()} is called. This value is capped to {@link #MAX_HIDE_DELAY}.
   *
   * @see #showDelay
   */
  private final int minHideDelay;

  private long lastShowStartTime = -1L;

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

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.LoadingIndicator, defStyleAttr, DEF_STYLE_RES);
    showDelay = a.getInt(R.styleable.LoadingIndicator_showDelay, -1);
    int minHideDelayUncapped = a.getInt(R.styleable.LoadingIndicator_minHideDelay, -1);
    minHideDelay = min(minHideDelayUncapped, MAX_HIDE_DELAY);
    a.recycle();

    setAnimatorDurationScaleProvider(new AnimatorDurationScaleProvider());
  }

  /**
   * Shows the loading indicator. If {@code showDelay} has been set to a positive value, wait until
   * the delay elapsed before starting the show action. Otherwise start showing immediately.
   */
  public void show() {
    if (showDelay > 0) {
      removeCallbacks(delayedShow);
      postDelayed(delayedShow, showDelay);
    } else {
      delayedShow.run();
    }
  }

  /**
   * Sets the visibility to {@code VISIBLE}. If this changes the visibility it will invoke {@code
   * onVisibilityChanged} and handle the visibility with animation of the drawables.
   *
   * @see #onVisibilityChanged(View, int)
   */
  private void internalShow() {
    if (minHideDelay > 0) {
      // The hide delay is positive, saves the time of starting show action.
      lastShowStartTime = SystemClock.uptimeMillis();
    }
    setVisibility(VISIBLE);
  }

  /**
   * Hides the loading indicator. If {@code minHideDelay} has been set to a positive value, wait
   * until the delay elapsed before starting the hide action. Otherwise start hiding immediately.
   */
  public void hide() {
    if (getVisibility() != VISIBLE) {
      // No need to hide, as the component is still invisible.
      removeCallbacks(delayedShow);
      return;
    }

    removeCallbacks(delayedHide);
    long timeElapsedSinceShowStart = SystemClock.uptimeMillis() - lastShowStartTime;
    boolean enoughTimeElapsed = timeElapsedSinceShowStart >= minHideDelay;
    if (enoughTimeElapsed) {
      delayedHide.run();
      return;
    }
    postDelayed(delayedHide, /* delayMillis= */ minHideDelay - timeElapsedSinceShowStart);
  }

  /**
   * If the component uses {@link DrawableWithAnimatedVisibilityChange} and needs to be hidden with
   * animation, it will trigger the drawable to start the hide animation. Otherwise, it will
   * directly set the visibility to {@code INVISIBLE}.
   *
   * @see #hide()
   */
  private void internalHide() {
    getDrawable().setVisible(/* visible= */ false, /* restart= */ false, /* animate= */ true);

    if (!getDrawable().isVisible()) {
      setVisibility(INVISIBLE);
    }
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
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (visibleToUser()) {
      internalShow();
    }
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
   * android.view.ViewGroup#attachViewToParent(View, int, ViewGroup.LayoutParams)}, which *can*
   * change our effective visibility. So this method errs on the side of assuming visibility unless
   * we can conclusively prove otherwise (but may result in some false positives, if this view
   * ends up being attached to a non-visible hierarchy after being detached in a visible state).
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
      indicatorColors =
          new int[] {
            MaterialColors.getColor(
                getContext(), androidx.appcompat.R.attr.colorPrimary, -1)
          };
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

  // ************************ In-place defined parameters ****************************

  /**
   * The runnable, which executes the start action. This is used to schedule delayed show actions.
   *
   * @see #show()
   */
  private final Runnable delayedShow =
      new Runnable() {
        @Override
        public void run() {
          internalShow();
        }
      };

  /**
   * The runnable, which executes the hide action. This is used to schedule delayed hide actions.
   *
   * @see #hide()
   */
  private final Runnable delayedHide =
      new Runnable() {
        @Override
        public void run() {
          internalHide();
          lastShowStartTime = -1L;
        }
      };
}
