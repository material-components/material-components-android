/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ProgressBar;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import java.util.Arrays;

/**
 * This class contains the common functions shared between ProgressIndicator in different types.
 * This is an abstract class which is meant for directly use.
 *
 * <p>With the default style {@link R.style#Widget_MaterialComponents_ProgressIndicator}, 4dp
 * indicator/track size and no animation is used for visibility change. Without customization,
 * primaryColor will be used as the indicator color; the indicator color applying disabledAlpha will
 * be used as the track color. The following attributes can be used to customize the
 * ProgressIndicator's appearance:
 *
 * <ul>
 *   <li>{@code indicatorSize}: the stroke width of the indicator and track.
 *   <li>{@code indicatorColor}: the color of the indicator.
 *   <li>{@code trackColor}: the color of the track.
 *   <li>{@code indicatorCornerRadius}: the radius of the rounded corner of the indicator stroke.
 * </ul>
 */
public class BaseProgressIndicator extends ProgressBar {
  protected static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ProgressIndicator;

  protected static final float DEFAULT_OPACITY = 0.2f;
  protected static final int MAX_ALPHA = 255;
  /**
   * The maximum time, in milliseconds, that the requested hide action is allowed to wait once
   * {@link #show()} is called.
   */
  private static final int MAX_HIDE_DELAY = 1000;

  /** A place to hold all the attributes. */
  protected final BaseProgressIndicatorSpec baseSpec;

  /** A temp place to hold new progress while switching from indeterminate to determinate mode. */
  private int storedProgress;
  /**
   * A temp place to hold whether use animator to update the new progress after switching from
   * indeterminate to determinate mode.
   */
  private boolean storedProgressAnimated;

  // Don't make final even though it's assigned in the constructor so the compiler doesn't inline it
  private boolean isParentDoneInitializing;

  /**
   * The time, in milliseconds, that the progress indicator will wait to show once the component
   * becomes visible. If set to zero (as default) or negative values, the show action will start
   * immediately.
   */
  private final int showDelay;

  /**
   * The minimum time, in milliseconds, that the requested hide action will wait to start once
   * {@link ProgressIndicator#show()} is called. If set to zero or negative values, the requested
   * hide action will start as soon as {@link ProgressIndicator#hide()} is called. This value is
   * capped to {@link #MAX_HIDE_DELAY}.
   *
   * @see #showDelay
   */
  private final int minHideDelay;

  private long lastShowStartTime = -1L;

  private AnimatorDurationScaleProvider animatorDurationScaleProvider;

  // The flag to mark if an indeterminate mode switching is requested.
  private boolean isIndeterminateModeChangeRequested = false;

  // The visibility state that the component will be in after hide animation finishes.
  private int visibilityAfterHide = View.INVISIBLE;

  // **************** Constructors ****************

  protected BaseProgressIndicator(@NonNull Context context) {
    this(context, null);
  }

  protected BaseProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  protected BaseProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

    // Ensures that we are using the correctly themed context rather than the context that was
    // passed in.
    context = getContext();

    baseSpec = new BaseProgressIndicatorSpec(context, attrs, defStyleAttr);

    // Loads additional attributes for view level.
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.BaseProgressIndicator,
            defStyleAttr,
            BaseProgressIndicator.DEF_STYLE_RES);
    showDelay = a.getInt(R.styleable.BaseProgressIndicator_showDelay, -1);
    int minHideDelayUncapped = a.getInt(R.styleable.BaseProgressIndicator_minHideDelay, -1);
    minHideDelay = min(minHideDelayUncapped, MAX_HIDE_DELAY);
    a.recycle();

    animatorDurationScaleProvider = new AnimatorDurationScaleProvider();
    isParentDoneInitializing = true;
  }

  // ******************** Initialization **********************

  private void registerAnimationCallbacks() {
    if (getProgressDrawable() != null && getIndeterminateDrawable() != null) {
      // Registers the animation callback to switch indeterminate mode at the end of indeterminate
      // animation.
      getIndeterminateDrawable()
          .getAnimatorDelegate()
          .registerAnimatorsCompleteCallback(switchIndeterminateModeCallback);
    }

    // Registers the hide animation callback to determinate drawable.
    if (getProgressDrawable() != null) {
      getProgressDrawable().registerAnimationCallback(hideAnimationCallback);
    }
    // Registers the hide animation callback to indeterminate drawable.
    if (getIndeterminateDrawable() != null) {
      getIndeterminateDrawable().registerAnimationCallback(hideAnimationCallback);
    }
  }

  private void unregisterAnimationCallbacks() {
    if (getIndeterminateDrawable() != null) {
      getIndeterminateDrawable().unregisterAnimationCallback(hideAnimationCallback);
      getIndeterminateDrawable().getAnimatorDelegate().unregisterAnimatorsCompleteCallback();
    }
    if (getProgressDrawable() != null) {
      getProgressDrawable().unregisterAnimationCallback(hideAnimationCallback);
    }
  }

  // ******************** Visibility control **********************

  /**
   * Shows the progress indicator. If {@code showDelay} has been set to a positive value, wait until
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
   * Hides the progress indicator. If {@code minHideDelay} has been set to a positive value, wait
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
    postDelayed(delayedHide, /*delayMillis=*/ minHideDelay - timeElapsedSinceShowStart);
  }

  /**
   * If the component uses {@link DrawableWithAnimatedVisibilityChange} and needs to be hidden with
   * animation, it will trigger the drawable to start the hide animation. Otherwise, it will
   * directly set the visibility to {@code INVISIBLE}.
   *
   * @see #hide()
   */
  private void internalHide() {
    getCurrentDrawable()
        .setVisible(/*visible=*/ false, /*restart=*/ false, /*animationDesired=*/ true);

    if (isNoLongerNeedToBeVisible()) {
      setVisibility(INVISIBLE);
    }
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    applyNewVisibility(/*animationDesired=*/ visibility == VISIBLE);
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    applyNewVisibility(/*animationDesired=*/ false);
  }

  /**
   * If it changes to visible, the start animation will be started if {@code growMode} indicates
   * any. If it changes to invisible, hides the drawable immediately.
   *
   * @param animationDesired Whether to change the visibility with animation.
   */
  protected void applyNewVisibility(boolean animationDesired) {
    if (!isParentDoneInitializing) {
      return;
    }

    getCurrentDrawable().setVisible(visibleToUser(), /*restart=*/ false, animationDesired);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    registerAnimationCallbacks();
    // Shows with animation.
    if (visibleToUser()) {
      internalShow();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    // Removes the delayedHide and delayedShow runnables from the queue if it has been scheduled.
    removeCallbacks(delayedHide);
    removeCallbacks(delayedShow);
    getCurrentDrawable().hideNow();
    unregisterAnimationCallbacks();
    super.onDetachedFromWindow();
  }

  // ******************** Draw methods **********************

  @Override
  protected synchronized void onDraw(@NonNull Canvas canvas) {
    int saveCount = canvas.save();
    if (getPaddingLeft() != 0 || getPaddingTop() != 0) {
      canvas.translate(getPaddingLeft(), getPaddingTop());
    }
    if (getPaddingRight() != 0 || getPaddingBottom() != 0) {
      int w = getWidth() - (getPaddingLeft() + getPaddingRight());
      int h = getHeight() - (getPaddingTop() + getPaddingBottom());
      canvas.clipRect(0, 0, w, h);
    }

    // ProgressBar does a bunch of unnecessary stuff in onDraw, so we don't call super here
    getCurrentDrawable().draw(canvas);

    canvas.restoreToCount(saveCount);
  }

  @Override
  protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    DrawingDelegate drawingDelegate = getCurrentDrawingDelegate();
    int drawableMeasuredWidth = drawingDelegate.getPreferredWidth();
    int drawableMeasuredHeight = drawingDelegate.getPreferredHeight();
    setMeasuredDimension(
        (drawableMeasuredWidth < 0
            ? getMeasuredWidth()
            : drawableMeasuredWidth + getPaddingLeft() + getPaddingRight()),
        (drawableMeasuredHeight < 0
            ? getMeasuredHeight()
            : drawableMeasuredHeight + getPaddingTop() + getPaddingBottom()));
  }

  @Override
  public void invalidate() {
    super.invalidate();
    if (getCurrentDrawable() != null) {
      getCurrentDrawable().invalidateSelf();
    }
  }

  // ******************** Helper methods **********************

  /** Returns the corresponding drawable based on current indeterminate state. */
  @Override
  @Nullable
  public DrawableWithAnimatedVisibilityChange getCurrentDrawable() {
    return isIndeterminate() ? getIndeterminateDrawable() : getProgressDrawable();
  }

  /** Returns the drawing delegate associated with the current drawable. */
  @Nullable
  public DrawingDelegate getCurrentDrawingDelegate() {
    if (isIndeterminate()) {
      return getIndeterminateDrawable() == null
          ? null
          : getIndeterminateDrawable().getDrawingDelegate();
    } else {
      return getProgressDrawable() == null ? null : getProgressDrawable().getDrawingDelegate();
    }
  }

  /**
   * Sets a new progress drawable. It has to inherit from {@link DeterminateDrawable}.
   *
   * @param drawable The new progress drawable.
   * @throws IllegalArgumentException if a framework drawable is passed in.
   */
  @Override
  public void setProgressDrawable(@Nullable Drawable drawable) {
    if (drawable == null) {
      super.setProgressDrawable(null);
      return;
    }
    if (drawable instanceof DeterminateDrawable) {
      DeterminateDrawable determinateDrawable = (DeterminateDrawable) drawable;
      determinateDrawable.hideNow();
      super.setProgressDrawable(determinateDrawable);
      // Every time ProgressBar sets progress drawable, it refreshes the drawable's level with
      // progress then secondary progress. Since secondary progress is not used here. We need to set
      // the level actively to overcome the affects from secondary progress.
      determinateDrawable.setLevelByFraction((float) getProgress() / getMax());
    } else {
      throw new IllegalArgumentException("Cannot set framework drawable as progress drawable.");
    }
  }

  /**
   * Sets a new indeterminate drawable. It has to inherit from {@link IndeterminateDrawable}.
   *
   * @param drawable The new indeterminate drawable.
   * @throws IllegalArgumentException if a framework drawable is passed in.
   */
  @Override
  public void setIndeterminateDrawable(@Nullable Drawable drawable) {
    if (drawable == null) {
      super.setIndeterminateDrawable(null);
      return;
    }
    if (drawable instanceof IndeterminateDrawable) {
      ((IndeterminateDrawable) drawable).hideNow();
      super.setIndeterminateDrawable(drawable);
    } else {
      throw new IllegalArgumentException(
          "Cannot set framework drawable as indeterminate drawable.");
    }
  }

  @Nullable
  @Override
  public DeterminateDrawable getProgressDrawable() {
    return (DeterminateDrawable) super.getProgressDrawable();
  }

  @Nullable
  @Override
  public IndeterminateDrawable getIndeterminateDrawable() {
    return (IndeterminateDrawable) super.getIndeterminateDrawable();
  }

  /**
   * Returns whether or not this view is currently displayed in window, based on whether it is
   * attached to a window and whether it and its ancestors are visible.
   */
  protected boolean visibleToUser() {
    return ViewCompat.isAttachedToWindow(this)
        && getWindowVisibility() == View.VISIBLE
        && isEffectivelyVisible();
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
  protected boolean isEffectivelyVisible() {
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

  /**
   * Returns {@code true} if both drawables are either null or not visible; {@code false},
   * otherwise.
   */
  private boolean isNoLongerNeedToBeVisible() {
    return (getProgressDrawable() == null || !getProgressDrawable().isVisible())
        && (getIndeterminateDrawable() == null || !getIndeterminateDrawable().isVisible());
  }

  // **************** Getters and setters ****************

  /**
   * Sets the progress mode of the progress indicator. Will throw an {@link IllegalStateException}
   * if the progress indicator is visible.
   *
   * @param indeterminate Whether the progress indicator should be in indeterminate mode.
   */
  @Override
  public synchronized void setIndeterminate(boolean indeterminate) {
    if (indeterminate == isIndeterminate()) {
      // Early return if no change.
      return;
    }

    if (visibleToUser() && indeterminate) {
      throw new IllegalStateException(
          "Cannot switch to indeterminate mode while the progress indicator is visible.");
    }

    // Needs to explicitly set visibility of two drawables. ProgressBar.setIndeterminate doesn't
    // handle it properly for pre-lollipop.
    DrawableWithAnimatedVisibilityChange oldDrawable = getCurrentDrawable();
    if (oldDrawable != null) {
      oldDrawable.hideNow();
    }
    super.setIndeterminate(indeterminate);
    DrawableWithAnimatedVisibilityChange newDrawable = getCurrentDrawable();
    if (newDrawable != null) {
      newDrawable.setVisible(visibleToUser(), /*restart=*/ false, /*animationDesired=*/ false);
    }

    // Indeterminate mode change finished.
    isIndeterminateModeChangeRequested = false;
  }

  /**
   * Returns the indicator size of this progress indicator in pixels.
   *
   * @see #setIndicatorSize(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorSize
   */
  @Px
  public int getIndicatorSize() {
    return baseSpec.indicatorSize;
  }

  /**
   * Sets the indicator size of this progress indicator.
   *
   * @param indicatorSize The new indicator size in pixel.
   * @see #getIndicatorSize()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorSize
   */
  public void setIndicatorSize(@Px int indicatorSize) {
    if (baseSpec.indicatorSize != indicatorSize) {
      baseSpec.indicatorSize = indicatorSize;
      requestLayout();
    }
  }

  /**
   * Returns the array of colors used in the indicator of this progress indicator.
   *
   * @see #setIndicatorColor(int...)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorColor
   */
  @NonNull
  public int[] getIndicatorColor() {
    return baseSpec.indicatorColors;
  }

  /**
   * Sets the colors used in the indicator of this progress indicator.
   *
   * @param indicatorColors The new colors used in indicator.
   * @see #getIndicatorColor()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorColor
   */
  public void setIndicatorColor(@ColorInt int... indicatorColors) {
    if (indicatorColors.length == 0) {
      // Uses theme primary color for indicator by default. Indicator color cannot be empty.
      indicatorColors = new int[] {MaterialColors.getColor(getContext(), R.attr.colorPrimary, -1)};
    }
    if (!Arrays.equals(getIndicatorColor(), indicatorColors)) {
      baseSpec.indicatorColors = indicatorColors;
      getIndeterminateDrawable().getAnimatorDelegate().invalidateSpecValues();
      invalidate();
    }
  }

  /**
   * Returns the color used in the track of this progress indicator.
   *
   * @see #setTrackColor(int)
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_trackColor
   */
  @ColorInt
  public int getTrackColor() {
    return baseSpec.trackColor;
  }

  /**
   * Sets the color of the track of this progress indicator.
   *
   * @param trackColor The new color used in the track of this progress indicator.
   * @see #getTrackColor()
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_trackColor
   */
  public void setTrackColor(@ColorInt int trackColor) {
    if (baseSpec.trackColor != trackColor) {
      baseSpec.trackColor = trackColor;
      invalidate();
    }
  }

  /**
   * Returns the corner radius for progress indicator with rounded corners in pixels.
   *
   * @see #setIndicatorCornerRadius(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorCornerRadius
   */
  @Px
  public int getIndicatorCornerRadius() {
    return baseSpec.indicatorCornerRadius;
  }

  /**
   * Sets the corner radius for progress indicator with rounded corners in pixels.
   *
   * @param indicatorCornerRadius The new corner radius in pixels.
   * @see #getIndicatorCornerRadius()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorCornerRadius
   */
  public void setIndicatorCornerRadius(@Px int indicatorCornerRadius) {
    if (baseSpec.indicatorCornerRadius != indicatorCornerRadius) {
      baseSpec.indicatorCornerRadius = min(indicatorCornerRadius, baseSpec.indicatorSize / 2);
    }
  }

  /**
   * Sets the current progress to the specified value. Does not do anything if the progress bar is
   * in indeterminate mode. Animation is not used by default. This default setting is aligned with
   * {@link ProgressBar#setProgress(int)}.
   *
   * @param progress The new progress value.
   * @see ProgressBar#setProgress(int)
   * @see #setProgress(int, boolean)
   */
  @Override
  public synchronized void setProgress(int progress) {
    if (isIndeterminate()) {
      return;
    }
    setProgressCompat(progress, false);
  }

  /**
   * Sets the current progress to the specified value with/without animation based on the input.
   *
   * <p>If it's in the indeterminate mode, it will smoothly transition to determinate mode by
   * finishing the current indeterminate animation cycle.
   *
   * @param progress The new progress value.
   * @param animated Whether to update the progress with the animation.
   * @see #setProgress(int)
   */
  public void setProgressCompat(int progress, boolean animated) {
    if (isIndeterminate()) {
      if (getProgressDrawable() != null) {
        // Holds new progress to a temp field, since setting progress is ignored in indeterminate
        // mode.
        storedProgress = progress;
        storedProgressAnimated = animated;
        isIndeterminateModeChangeRequested = true;

        if (animatorDurationScaleProvider.getSystemAnimatorDurationScale(
                getContext().getContentResolver())
            == 0) {
          switchIndeterminateModeCallback.onAnimationEnd(getIndeterminateDrawable());
        } else {
          getIndeterminateDrawable().getAnimatorDelegate().requestCancelAnimatorAfterCurrentCycle();
        }
      }
    } else {
      // Calls ProgressBar setProgress(int) to update the progress value and level. We don't rely on
      // it to draw or animate the indicator.
      super.setProgress(progress);
      // Fast forward to the final state of the determinate animation.
      if (getProgressDrawable() != null && !animated) {
        getProgressDrawable().jumpToCurrentState();
      }
    }
  }

  @VisibleForTesting
  public void setAnimatorDurationScaleProvider(
      @NonNull AnimatorDurationScaleProvider animatorDurationScaleProvider) {
    this.animatorDurationScaleProvider = animatorDurationScaleProvider;
    if (getProgressDrawable() != null) {
      getProgressDrawable().animatorDurationScaleProvider = animatorDurationScaleProvider;
    }
    if (getIndeterminateDrawable() != null) {
      getIndeterminateDrawable().animatorDurationScaleProvider = animatorDurationScaleProvider;
    }
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

  /**
   * The {@code AnimationCallback} to switch indeterminate mode at the end of indeterminate
   * animation.
   *
   * @see #registerAnimationCallbacks()
   */
  private final AnimationCallback switchIndeterminateModeCallback =
      new AnimationCallback() {
        @Override
        public void onAnimationEnd(Drawable drawable) {

          setIndeterminate(false);

          // Resets progress bar to minimum value then updates to new progress.
          setProgressCompat(/*progress=*/ 0, /*animated=*/ false);
          setProgressCompat(storedProgress, storedProgressAnimated);
        }
      };

  /**
   * The {@code AnimationCallback} to set the component invisible at the end of hide animation.
   *
   * @see #registerAnimationCallbacks()
   */
  private final AnimationCallback hideAnimationCallback =
      new AnimationCallback() {
        @Override
        public void onAnimationEnd(Drawable drawable) {
          super.onAnimationEnd(drawable);
          if (!isIndeterminateModeChangeRequested && visibleToUser()) {
            // Don't hide the component if under transition from indeterminate mode to
            // determinate mode or the component is current not visible to users.
            setVisibility(visibilityAfterHide);
          }
        }
      };
}
