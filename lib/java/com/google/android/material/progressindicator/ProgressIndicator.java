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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;

import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ProgressBar;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.color.MaterialColors;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Progress indicators are animated views that represent the progress of a undergoing process.
 *
 * <p>The progress indicator widget can represent determinate or indeterminate progress in a linear
 * or circular form.
 */
public class ProgressIndicator extends ProgressBar {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_ProgressIndicator_Linear_Determinate;

  // Used for debug purpose to slow down the animations.
  protected static final float ANIMATION_SPEED_FACTOR = 1f;

  // Constants for track shape.

  /** The indicator appears as a horizontal bar. */
  public static final int LINEAR = 0;
  /** The indicator appears as a circular spinner. */
  public static final int CIRCULAR = 1;

  // Constants for show/hide animation.

  /** There is no animation used while showing or hiding. */
  public static final int GROW_MODE_NONE = 0;
  /**
   * For linear type, the progress indicator appears as expanding from the top to the bottom
   * (downward), and disappears with an reversed animation. For circular type, the progress
   * indicator appears as expanding from the outer edge to the inner edge (inward radial direction),
   * and disappears with an reversed animation.
   */
  public static final int GROW_MODE_INCOMING = 1;
  /**
   * For linear type, the progress indicator appears as expanding from the bottom to the top
   * (upward), and disappears with an reversed animation. For circular type, the progress indicator
   * appears as expanding from the inner edge to the outer edge (outward radial direction), and
   * disappears with an reversed animation.
   */
  public static final int GROW_MODE_OUTGOING = 2;
  /** The progress indicator will expand from and shrink to the central line of the indicator. */
  public static final int GROW_MODE_BIDIRECTIONAL = 3;

  private static final float DEFAULT_OPACITY = 0.2f;
  private static final int MAX_ALPHA = 255;

  /** The max delay the user is allowed to postpone the hide callback */
  private static final int MAX_HIDE_DELAY = 1000;

  // Default dimensions.
  private int defaultIndicatorWidth;
  private int defaultCircularInset;
  private int defaultCircularRadius;
  private int defaultCornerRadius;

  /** The type of the progress indicator, either {@code #LINEAR} or {@code #CIRCULAR}. */
  private int indicatorType;

  /** The width of the progress track and indicator. */
  private int indicatorWidth;

  /**
   * When this is greater than 0, the corners of both the track and the indicator will be rounded
   * with this radius. If the radius is greater than half of the track width, an {@code
   * IllegalArgumentException} will be thrown during initialization.
   */
  private int indicatorCornerRadius;

  /**
   * The color array of the progress stroke. In determinate mode and single color indeterminate
   * mode, only the first item will be used. This field combines the attribute indicatorColor and
   * indicatorColors defined in the XML.
   */
  private int[] indicatorColors;

  /**
   * The color used for the progress track. If not defined, it will be set to the indicatorColor and
   * apply the first disable alpha value from the theme.
   */
  private int trackColor;

  /**
   * Whether to inverse the progress direction. Linear positive directory is start-to-end; circular
   * positive directory is clockwise.
   */
  private boolean inverse;

  /**
   * How the progress indicator appears and disappears. {@see #GROW_MODE_NONE} {@see
   * #GROW_MODE_INCOMING} {@see #GROW_MODE_OUTGOING} {@see #GROW_MODE_BIDIRECTIONAL}
   */
  private int growMode;

  /** The extra space from the edge of the stroke to the edge of canvas. Ignored in linear mode. */
  private int circularInset;

  /** The radius of the outer bound of the circular progress stroke. Ignored in linear mode. */
  private int circularRadius;

  /**
   * The animation style used in indeterminate mode. The strokes in different colors are end-to-end
   * connected. Ignored for determinate mode and indeterminate mode with less than 3 colors.
   */
  private boolean linearSeamless;

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
   * The minimum delay the progress will wait before hiding after
   * {@link ProgressIndicator#show()} is called, in milliseconds.
   * If not set, the progress will dismiss as soon as {@link ProgressIndicator#hide()} is called.
   * Capped at 1 seconds.
   */
  private int minHideDelay;
  private long showStartTime = -1L;

  // ******************** Interfaces **********************

  /** The type of the progress indicator. */
  @IntDef({LINEAR, CIRCULAR})
  @Retention(RetentionPolicy.SOURCE)
  public @interface IndicatorType {}

  /** How the indicator appears and disappears. */
  @IntDef({GROW_MODE_NONE, GROW_MODE_INCOMING, GROW_MODE_OUTGOING, GROW_MODE_BIDIRECTIONAL})
  @Retention(RetentionPolicy.SOURCE)
  public @interface GrowMode {}

  // ******************** Constructors **********************

  public ProgressIndicator(Context context) {
    this(context, null);
  }

  public ProgressIndicator(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.progressIndicatorStyle);
  }

  public ProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, DEF_STYLE_RES);
  }

  public ProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    isParentDoneInitializing = true;
    // Ensure we are using the correctly themed context rather than the context was passed in.
    context = getContext();

    loadResources(context.getResources());
    loadAttributes(context, attrs, defStyleAttr, defStyleRes);

    initializeDrawables();
    applyNewVisibility();
  }

  // ******************** Initialization **********************

  /** Loads some default dimensions from resource file. */
  private void loadResources(Resources resources) {
    defaultIndicatorWidth = resources.getDimensionPixelSize(R.dimen.mtrl_progress_indicator_width);
    defaultCircularInset = resources.getDimensionPixelSize(R.dimen.mtrl_progress_circular_inset);
    defaultCircularRadius = resources.getDimensionPixelSize(R.dimen.mtrl_progress_circular_radius);
    // By default, rounded corners are not applied.
    defaultCornerRadius = 0;
  }

  /** Loads attributes defined in layout or style files. */
  private void loadAttributes(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            attrs, R.styleable.ProgressIndicator, defStyleAttr, defStyleRes);

    indicatorType = a.getInt(R.styleable.ProgressIndicator_indicatorType, LINEAR);
    indicatorWidth =
        a.getDimensionPixelSize(
            R.styleable.ProgressIndicator_indicatorWidth, defaultIndicatorWidth);
    circularInset =
        a.getDimensionPixelSize(R.styleable.ProgressIndicator_circularInset, defaultCircularInset);
    circularRadius =
        a.getDimensionPixelSize(
            R.styleable.ProgressIndicator_circularRadius, defaultCircularRadius);
    if (indicatorType == CIRCULAR && circularRadius < indicatorWidth / 2) {
      // Throws an exception if circularRadius is less than half of the indicatorWidth, which will
      // result in a part of the inner side of the indicator overshoots the center, and the visual
      // becomes undefined.
      throw new IllegalArgumentException(
          "The circularRadius cannot be less than half of the indicatorWidth.");
    }
    inverse = a.getBoolean(R.styleable.ProgressIndicator_inverse, false);
    growMode = a.getInt(R.styleable.ProgressIndicator_growMode, GROW_MODE_NONE);

    // Gets indicator colors from resource if existed, otherwise use indicatorColor attribute.
    if (a.hasValue(R.styleable.ProgressIndicator_indicatorColors)) {
      indicatorColors =
          getResources()
              .getIntArray(a.getResourceId(R.styleable.ProgressIndicator_indicatorColors, -1));
      if (a.hasValue(R.styleable.ProgressIndicator_indicatorColor)) {
        // Throws an exception if both indicatorColors and indicatorColor exist in attribute set.
        throw new IllegalArgumentException(
            "Attributes indicatorColors and indicatorColor cannot be used at the same time.");
      } else if (indicatorColors.length == 0) {
        // Throws an exception if indicatorColor doesn't exist and indicatorColors is empty.
        throw new IllegalArgumentException(
            "indicatorColors cannot be empty when indicatorColor is not used.");
      }
    } else if (a.hasValue(R.styleable.ProgressIndicator_indicatorColor)) {
      indicatorColors = new int[] {a.getColor(R.styleable.ProgressIndicator_indicatorColor, -1)};
    } else {
      // Uses theme primary color for indicator if neither indicatorColor nor indicatorColors exists
      // in attribute set.
      indicatorColors = new int[] {MaterialColors.getColor(getContext(), R.attr.colorPrimary, -1)};
    }
    // Gets track color if defined, otherwise, use indicator color with the disable alpha value.
    if (a.hasValue(R.styleable.ProgressIndicator_trackColor)) {
      trackColor = a.getColor(R.styleable.ProgressIndicator_trackColor, -1);
    } else {
      trackColor = indicatorColors[0];

      TypedArray disabledAlphaArray =
          getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.disabledAlpha});
      float defaultOpacity = disabledAlphaArray.getFloat(0, DEFAULT_OPACITY);
      disabledAlphaArray.recycle();

      int trackAlpha = (int) (MAX_ALPHA * defaultOpacity);
      trackColor = MaterialColors.compositeARGBWithAlpha(trackColor, trackAlpha);
    }
    // Gets linearSeamless or overrides it if necessary.
    if (isEligibleToSeamless()) {
      linearSeamless = a.getBoolean(R.styleable.ProgressIndicator_linearSeamless, true);
    } else {
      linearSeamless = false;
    }
    // Gets the radius of rounded corners if defined, otherwise, use 0 (sharp corner).
    setIndicatorCornerRadius(
        a.getDimensionPixelSize(
            R.styleable.ProgressIndicator_indicatorCornerRadius, defaultCornerRadius));
    // Sets if is indeterminate.
    setIndeterminate(a.getBoolean(R.styleable.ProgressIndicator_android_indeterminate, false));

    if (a.hasValue(R.styleable.ProgressIndicator_minHideDelay)) {
      int minHideDelayUncapped = a.getInt(R.styleable.ProgressIndicator_minHideDelay, -1);
      // Cap the value at 1 seconds (1000 ms)
      minHideDelay = Math.min(minHideDelayUncapped, MAX_HIDE_DELAY);
    }

    a.recycle();
  }

  private void initializeDrawables() {
    // Creates and sets the determinate and indeterminate drawables based on track shape.
    if (indicatorType == LINEAR) {
      setIndeterminateDrawable(new LinearIndeterminateDrawable(getContext(), this));
      setProgressDrawable(new DeterminateDrawable(this, new LinearDrawingDelegate()));
    } else {
      setIndeterminateDrawable(new CircularIndeterminateDrawable(this));
      setProgressDrawable(new DeterminateDrawable(this, new CircularDrawingDelegate()));
    }

    // Creates and register an animation callback to switch indeterminate mode at the end of
    // indeterminate animation.
    ((IndeterminateAnimatorControl) getIndeterminateDrawable())
        .registerMainAnimatorCompleteEndCallback(
            new AnimationCallback() {
              @Override
              public void onAnimationEnd(Drawable drawable) {
                post(
                    new Runnable() {
                      @Override
                      public void run() {
                        setIndeterminate(false);

                        // Resets progress bar to minimum value then updates to new progress.
                        setProgressCompat(0, /*animated=*/ false);
                        setProgressCompat(storedProgress, storedProgressAnimated);
                      }
                    });
              }
            });

    // Creates an animation callback to set the component invisible at the end of hide animation.
    AnimationCallback hideAnimationCallback =
        new AnimationCallback() {
          @Override
          public void onAnimationEnd(Drawable drawable) {
            super.onAnimationEnd(drawable);
            post(
                new Runnable() {
                  @Override
                  public void run() {
                    if (getVisibility() == VISIBLE) {
                      setVisibility(INVISIBLE);
                    }
                  }
                });
          }
        };

    // Registers the hide animation callback to both drawables.
    ((Animatable2Compat) getProgressDrawable()).registerAnimationCallback(hideAnimationCallback);
    ((Animatable2Compat) getIndeterminateDrawable())
        .registerAnimationCallback(hideAnimationCallback);
  }

  // ******************** Visibility control **********************

  /**
   * This method sets a flag to prevent using any animators. It should be called before being
   * visible.
   */
  @VisibleForTesting
  public void disableAnimatorsForTesting() {
    getProgressDrawable().disableAnimatorsForTesting();
    getIndeterminateDrawable().disableAnimatorsForTesting();
  }

  /**
   * Sets the visibility to {@code VISIBLE}. If this changes the visibility it will invoke {@code
   * onVisibilityChanged} and handle the visibility with animation of the drawables.
   *
   * @see #onVisibilityChanged(View, int)
   */
  public void show() {
    if (minHideDelay != -1) {
      // The hide delay is set, save the initial time before showing
      showStartTime = SystemClock.uptimeMillis();
    }
    setVisibility(VISIBLE);
  }

  /**
   * Hide the progress indicator.
   * If {@code minHideDelay} has been set wait until the delay ends before dismissing the
   * progress. Otherwise start hiding as soon as possible.
   */
  public void hide() {
    if (minHideDelay != -1) {
      // Remove delay callback if already scheduled
      removeCallbacks(delayedHide);
      long diff = SystemClock.uptimeMillis() - showStartTime;
      if (diff >= minHideDelay) {
        // The progress was showing for at least minHideDelay millis.
        // We can hide it and reset the show timer
        internalHide();
        showStartTime = -1L;
      } else {
        // The progress can't be hidden because minHideDelay has not passed yet.
        // Schedule the delay to execute in the future
        postDelayed(delayedHide, minHideDelay - diff);
      }
    } else {
      internalHide();
    }
  }

  /**
   * If the component uses {@link DrawableWithAnimatedVisibilityChange} and needs to be hidden with
   * animation, it will trigger the drawable to start the hide animation. Otherwise, it will
   * directly set the visibility to {@code INVISIBLE}.
   *
   * @see #hide()
   */
  private void internalHide() {
    DrawableWithAnimatedVisibilityChange currentDrawable = getCurrentDrawable();

    // Hide animation should be used if it's visible to user and potentially can be hidden with
    // animation.
    boolean shouldHideAnimated = visibleToUser() && growMode != GROW_MODE_NONE;

    currentDrawable.setVisible(false, shouldHideAnimated);

    if (!shouldHideAnimated) {
      setVisibility(INVISIBLE);
    }
  }

  /**
   * Runnable which executes an hide operations on the progress.
   *
   * @see #hide()
   */
  private final Runnable delayedHide = new Runnable() {
    @Override
    public void run() {
      internalHide();
      showStartTime = -1L;
    }
  };

  @Override
  protected void onVisibilityChanged(View changeView, int visibility) {
    super.onVisibilityChanged(changeView, visibility);
    applyNewVisibility();
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    applyNewVisibility();
  }

  /**
   * If it changes to visible, the start animation will be started if {@code growMode} indicates
   * any. If it changes to invisible, hides the drawable immediately.
   */
  private void applyNewVisibility() {
    if (!isParentDoneInitializing) {
      return;
    }

    Drawable currentDrawable = getCurrentDrawable();
    boolean visibleToUser = visibleToUser();

    // Sets the drawable to visible/invisible if the component is currently visible/invisible. Only
    // show animation should be started (when the component is currently visible). Hide animation
    // should have already ended or is not necessary at this point.
    currentDrawable.setVisible(visibleToUser, visibleToUser);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // Shows with animation.
    if (visibleToUser()) {
      show();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    // Remove the delayedHide runnable from the queue if has been scheduled
    removeCallbacks(delayedHide);
    getCurrentDrawable().setVisible(false, false);
    super.onDetachedFromWindow();
  }

  // ******************** Draw methods **********************

  @Override
  protected synchronized void onDraw(Canvas canvas) {
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
    if (indicatorType == CIRCULAR) {
      setMeasuredDimension(
          circularRadius * 2
              + indicatorWidth
              + circularInset * 2
              + getPaddingLeft()
              + getPaddingRight(),
          circularRadius * 2
              + indicatorWidth
              + circularInset * 2
              + getPaddingTop()
              + getPaddingBottom());
    } else {
      setMeasuredDimension(
          getMeasuredWidth(), indicatorWidth + getPaddingTop() + getPaddingBottom());
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    // set the drawable bounds ourselves for linear, since ProgressBar tries to maintain aspect
    // ratio and we don't want that.
    if (indicatorType == LINEAR) {
      int contentWidth = w - (getPaddingLeft() + getPaddingRight());
      int contentHeight = h - (getPaddingTop() + getPaddingBottom());
      Drawable drawable = getIndeterminateDrawable();
      if (drawable != null) {
        drawable.setBounds(0, 0, contentWidth, contentHeight);
      }
      drawable = getProgressDrawable();
      if (drawable != null) {
        drawable.setBounds(0, 0, contentWidth, contentHeight);
      }
    } else {
      super.onSizeChanged(w, h, oldw, oldh);
    }
  }

  @Override
  public void invalidate() {
    super.invalidate();
    if (getCurrentDrawable() != null) {
      getCurrentDrawable().invalidateSelf();
    }
  }

  // ******************** Helper methods **********************

  /**
   * Returns if the configuration of this progress indicator is eligible to seamless indeterminate
   * animation style.
   */
  private boolean isEligibleToSeamless() {
    return isIndeterminate() && indicatorType == LINEAR && indicatorColors.length >= 3;
  }

  /** Returns the corresponding drawable based on current indeterminate state. */
  @Override
  @NonNull
  public DrawableWithAnimatedVisibilityChange getCurrentDrawable() {
    return isIndeterminate() ? getIndeterminateDrawable() : getProgressDrawable();
  }

  /** Blocks any attempts to set the progress drawable for this progress indicator. */
  @Override
  public void setProgressDrawable(@NonNull Drawable drawable) {
    if (drawable instanceof DeterminateDrawable) {
      super.setProgressDrawable(drawable);
      // Every time ProgressBar sets progress drawable, it refreshes the drawable's level with
      // progress then secondary progress. Since secondary progress is not used here. We need to set
      // the level actively to overcome the affects from secondary progress.
      ((DeterminateDrawable) drawable).setLevelByFraction((float) getProgress() / getMax());
    } else {
      throw new IllegalArgumentException("Cannot set framework drawable as progress drawable.");
    }
  }

  /** Blocks any attempts to set the indeterminate drawable for this progress indicator. */
  @Override
  public void setIndeterminateDrawable(@NonNull Drawable drawable) {
    if (drawable instanceof DrawableWithAnimatedVisibilityChange) {
      super.setIndeterminateDrawable(drawable);
    } else {
      throw new IllegalArgumentException(
          "Cannot set framework drawable as indeterminate drawable.");
    }
  }

  @Override
  public DeterminateDrawable getProgressDrawable() {
    return (DeterminateDrawable) super.getProgressDrawable();
  }

  @Override
  public DrawableWithAnimatedVisibilityChange getIndeterminateDrawable() {
    return (DrawableWithAnimatedVisibilityChange) super.getIndeterminateDrawable();
  }

  /**
   * Returns whether or not this view is currently displayed in window, based on whether it is
   * attached to a window and whether it and its ancestors are visible.
   */
  private boolean visibleToUser() {
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

  private void updateColorsInDrawables() {
    getProgressDrawable().recalculateColors();
    getIndeterminateDrawable().recalculateColors();
  }

  // ******************** Getters and setters **********************

  /**
   * Returns the indicator type of this progress indicator.
   *
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorType
   */
  public int getIndicatorType() {
    return indicatorType;
  }

  /**
   * Sets the indicator type of this progress indicator. Will throw an {@link IllegalStateException}
   * if the progress indicator is visible.
   *
   * @param indicatorType The new indicator type.
   * @see #getIndicatorType()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorType
   */
  public void setIndicatorType(@IndicatorType int indicatorType) {
    if (visibleToUser() && this.indicatorType != indicatorType) {
      throw new IllegalStateException(
          "Cannot change indicatorType while the progress indicator is visible.");
    }
    this.indicatorType = indicatorType;
    initializeDrawables();
    requestLayout();
  }

  /**
   * Sets the progress mode of the progress indicator. Will throw an {@link IllegalStateException}
   * if the progress indicator is visible.
   *
   * @param indeterminate Whether the progress indicator should be in indeterminate mode.
   */
  @Override
  public synchronized void setIndeterminate(boolean indeterminate) {
    if (visibleToUser() && isIndeterminate() != indeterminate && indeterminate) {
      throw new IllegalStateException(
          "Cannot switch to indeterminate mode while the progress indicator is visible.");
    }
    super.setIndeterminate(indeterminate);
  }

  /**
   * Returns the indicator width of this progress indicator in pixels.
   *
   * @see #setIndicatorWidth(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorWidth
   */
  public int getIndicatorWidth() {
    return indicatorWidth;
  }

  /**
   * Sets the indicator width of this progress indicator.
   *
   * @param indicatorWidth The new indicator width in pixel.
   * @see #getIndicatorWidth()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorWidth
   */
  public void setIndicatorWidth(@Px int indicatorWidth) {
    if (this.indicatorWidth != indicatorWidth) {
      this.indicatorWidth = indicatorWidth;
      requestLayout();
    }
  }

  /**
   * Returns the array of colors used in the indicator of this progress indicator.
   *
   * @see #setIndicatorColors(int[])
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorColors
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorColor
   */
  public int[] getIndicatorColors() {
    return indicatorColors;
  }

  /**
   * Sets the array of colors used in the indicator of this progress indicator.
   *
   * @param indicatorColors The array of new colors used in indicator.
   * @see #getIndicatorColors()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorColors
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorColor
   */
  public void setIndicatorColors(int[] indicatorColors) {
    this.indicatorColors = indicatorColors;
    updateColorsInDrawables();
    if (!isEligibleToSeamless()) {
      linearSeamless = false;
    }
    invalidate();
  }

  /**
   * Returns the color used in the track of this progress indicator.
   *
   * @see #setTrackColor(int)
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_trackColor
   */
  public int getTrackColor() {
    return trackColor;
  }

  /**
   * Sets the color of the track of this progress indicator.
   *
   * @param trackColor The new color used in the track of this progress indicator.
   * @see #getTrackColor()
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_trackColor
   */
  public void setTrackColor(@ColorInt int trackColor) {
    if (this.trackColor != trackColor) {
      this.trackColor = trackColor;
      updateColorsInDrawables();
      invalidate();
    }
  }

  /**
   * Returns whether the indicator progresses in inverse direction.
   *
   * @see #setInverse(boolean)
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_inverse
   */
  public boolean isInverse() {
    return inverse;
  }

  /**
   * Sets whether the indicator progresses in inverse direction. Linear positive directory is
   * start-to-end; circular positive directory is clockwise.
   *
   * @param inverse Whether the indicator progresses in inverse direction.
   * @see #isInverse()
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_inverse
   */
  public void setInverse(boolean inverse) {
    if (this.inverse != inverse) {
      this.inverse = inverse;
      invalidate();
    }
  }

  /**
   * Returns the mode of how this progress indicator will appear and disappear.
   *
   * @see #setGrowMode(int)
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_growMode
   */
  public int getGrowMode() {
    return growMode;
  }

  /**
   * Sets the mode of how this progress indicator will appear and disappear.
   *
   * @param growMode New grow mode.
   * @see #getGrowMode()
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_growMode
   */
  public void setGrowMode(@GrowMode int growMode) {
    if (this.growMode != growMode) {
      this.growMode = growMode;
      invalidate();
    }
  }

  /**
   * Returns whether seamless style animation is used for the linear indeterminate type.
   *
   * @see #setLinearSeamless(boolean)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_linearSeamless
   */
  public boolean isLinearSeamless() {
    return linearSeamless;
  }

  /**
   * Set whether seamless style animation is used for the linear indeterminate type. The new value
   * will only be set if the configuration is eligible to the seamless style and the component
   * currently is not visible to the user. Will throw an {@link IllegalStateException} if visible.
   *
   * @param linearSeamless Whether seamless style animation should be used for this progress
   *     indicator.
   * @see #isLinearSeamless()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_linearSeamless
   */
  public void setLinearSeamless(boolean linearSeamless) {
    if (visibleToUser() && isIndeterminate() && this.linearSeamless != linearSeamless) {
      throw new IllegalStateException(
          "Cannot change linearSeamless while the progress indicator is shown in indeterminate"
              + " mode.");
    }
    if (isEligibleToSeamless()) {
      this.linearSeamless = linearSeamless;
      if (linearSeamless) {
        indicatorCornerRadius = defaultCornerRadius;
      }
    } else {
      this.linearSeamless = false;
    }
    invalidate();
  }

  /**
   * Returns the corner radius for progress indicator with rounded corners in pixels.
   *
   * @see #setIndicatorCornerRadius(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorCornerRadius
   */
  public int getIndicatorCornerRadius() {
    return indicatorCornerRadius;
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
    if (this.indicatorCornerRadius != indicatorCornerRadius) {
      this.indicatorCornerRadius = Math.min(indicatorCornerRadius, indicatorWidth / 2);
      if (linearSeamless && indicatorCornerRadius > 0) {
        throw new IllegalArgumentException(
            "Rounded corners are not supported in linear seamless mode.");
      }
    }
  }

  /**
   * Returns the inset of circular progress indicator.
   *
   * @see #setCircularInset(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularInset
   */
  public int getCircularInset() {
    return circularInset;
  }

  /**
   * Sets the inset of this progress indicator, if it's circular type.
   *
   * @param circularInset The new inset in pixels.
   * @see #getCircularInset()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularInset
   */
  public void setCircularInset(@Px int circularInset) {
    if (indicatorType == CIRCULAR && this.circularInset != circularInset) {
      this.circularInset = circularInset;
      invalidate();
    }
  }

  /**
   * Returns the radius of circular progress indicator.
   *
   * @see #setCircularRadius(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularRadius
   */
  public int getCircularRadius() {
    return circularRadius;
  }

  /**
   * Sets the radius of this progress indicator, if it's circular type.
   *
   * @param circularRadius The new radius in pixels.
   * @see #getCircularRadius()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularRadius
   */
  public void setCircularRadius(@Px int circularRadius) {
    if (indicatorType == CIRCULAR && this.circularRadius != circularRadius) {
      this.circularRadius = circularRadius;
      invalidate();
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
    setProgressCompat(progress, false);
  }

  /**
   * Sets the current progress to the specified value with/without animation based on the input.
   * Does not do anything if the progress bar is in indeterminate mode.
   *
   * @param progress The new progress value.
   * @param animated Whether to update the progress with the animation.
   * @see #setProgress(int)
   */
  public void setProgressCompat(int progress, boolean animated) {
    if (isIndeterminate()) {
      ((IndeterminateAnimatorControl) getIndeterminateDrawable())
          .requestCancelMainAnimatorAfterCurrentCycle();
      // Holds new progress to a temp field, since setting progress is ignored in indeterminate
      // mode.
      storedProgress = progress;
      storedProgressAnimated = animated;
      return;
    }

    // Calls ProgressBar setProgress(int) to update the progress value and level. We don't rely on
    // it to draw or animate the indicator.
    super.setProgress(progress);

    // The animation will start to update the indicator by default. If no animation is desired, we
    // need to call jumpToCurrentState() to skip/fast-forward the animation.
    if (!animated && getProgressDrawable() != null) {
      getProgressDrawable().jumpToCurrentState();
    }
  }

}
