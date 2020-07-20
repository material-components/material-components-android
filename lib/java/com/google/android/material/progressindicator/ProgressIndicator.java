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

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ProgressBar;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
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

  // Constants for track shape.

  /** The indicator appears as a horizontal bar. */
  public static final int LINEAR = 0;
  /** The indicator appears as a circular spinner. */
  public static final int CIRCULAR = 1;
  /**
   * The indicator uses a custom drawable. This prevents to initialize pre-defined drawables for
   * linear/circular type. Drawables have to be manually initialized.
   */
  public static final int CUSTOM = 2;

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

  /**
   * The maximum time, in milliseconds, that the requested hide action is allowed to wait once
   * {@link #show()} is called.
   */
  private static final int MAX_HIDE_DELAY = 1000;

  /** A place to hold all the attributes. */
  private final ProgressIndicatorSpec spec;

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
   * The minimum time, in milliseconds, that the requested hide action will wait to start once
   * {@link ProgressIndicator#show()} is called. If set to zero or negative values, the requested
   * hide action will start as soon as {@link ProgressIndicator#hide()} is called. This value is
   * capped to {@link #MAX_HIDE_DELAY}.
   */
  private int minHideDelay;

  private long lastShowStartTime = -1L;

  private AnimatorDurationScaleProvider animatorDurationScaleProvider;

  // The flag to mark if an indeterminate mode switching is requested.
  private boolean isIndeterminateModeChangeRequested = false;

  // ******************** Interfaces **********************

  /** The type of the progress indicator. */
  @IntDef({LINEAR, CIRCULAR, CUSTOM})
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
    animatorDurationScaleProvider = new AnimatorDurationScaleProvider();
    isParentDoneInitializing = true;
    // Ensure we are using the correctly themed context rather than the context was passed in.
    context = getContext();

    spec = new ProgressIndicatorSpec();
    loadDefaultAttributes(context.getResources(), spec);
    loadAttributes(context, attrs, defStyleAttr, defStyleRes);

    if (spec.indicatorType != CUSTOM) {
      initializeDrawables();
    }
  }

  // ******************** Initialization **********************

  /** Loads some default dimensions from resource file. */
  private static void loadDefaultAttributes(Resources resources, ProgressIndicatorSpec spec) {
    spec.indicatorWidth = resources.getDimensionPixelSize(R.dimen.mtrl_progress_indicator_width);
    spec.circularInset = resources.getDimensionPixelSize(R.dimen.mtrl_progress_circular_inset);
    spec.circularRadius = resources.getDimensionPixelSize(R.dimen.mtrl_progress_circular_radius);
    // By default, rounded corners are not applied.
    spec.indicatorCornerRadius = 0;
  }

  /** Loads attributes defined in layout or style files. */
  private void loadAttributes(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            attrs, R.styleable.ProgressIndicator, defStyleAttr, defStyleRes);

    spec.indicatorType = a.getInt(R.styleable.ProgressIndicator_indicatorType, LINEAR);
    spec.indicatorWidth =
        a.getDimensionPixelSize(R.styleable.ProgressIndicator_indicatorWidth, spec.indicatorWidth);
    spec.circularInset =
        a.getDimensionPixelSize(R.styleable.ProgressIndicator_circularInset, spec.circularInset);
    spec.circularRadius =
        a.getDimensionPixelSize(R.styleable.ProgressIndicator_circularRadius, spec.circularRadius);
    if (spec.indicatorType == CIRCULAR && spec.circularRadius < spec.indicatorWidth / 2) {
      // Throws an exception if circularRadius is less than half of the indicatorWidth, which will
      // result in a part of the inner side of the indicator overshoots the center, and the visual
      // becomes undefined.
      throw new IllegalArgumentException(
          "The circularRadius cannot be less than half of the indicatorWidth.");
    }
    spec.inverse = a.getBoolean(R.styleable.ProgressIndicator_inverse, false);
    spec.growMode = a.getInt(R.styleable.ProgressIndicator_growMode, GROW_MODE_NONE);

    // Gets indicator colors from resource if existed, otherwise use indicatorColor attribute.
    if (a.hasValue(R.styleable.ProgressIndicator_indicatorColors)) {
      spec.indicatorColors =
          getResources()
              .getIntArray(a.getResourceId(R.styleable.ProgressIndicator_indicatorColors, -1));
      if (a.hasValue(R.styleable.ProgressIndicator_indicatorColor)) {
        // Throws an exception if both indicatorColors and indicatorColor exist in attribute set.
        throw new IllegalArgumentException(
            "Attributes indicatorColors and indicatorColor cannot be used at the same time.");
      } else if (spec.indicatorColors.length == 0) {
        // Throws an exception if indicatorColor doesn't exist and indicatorColors is empty.
        throw new IllegalArgumentException(
            "indicatorColors cannot be empty when indicatorColor is not used.");
      }
    } else if (a.hasValue(R.styleable.ProgressIndicator_indicatorColor)) {
      spec.indicatorColors =
          new int[] {a.getColor(R.styleable.ProgressIndicator_indicatorColor, -1)};
    } else {
      // Uses theme primary color for indicator if neither indicatorColor nor indicatorColors exists
      // in attribute set.
      spec.indicatorColors = new int[] {MaterialColors.getColor(context, R.attr.colorPrimary, -1)};
    }
    // Gets track color if defined, otherwise, use indicator color with the disable alpha value.
    if (a.hasValue(R.styleable.ProgressIndicator_trackColor)) {
      spec.trackColor = a.getColor(R.styleable.ProgressIndicator_trackColor, -1);
    } else {
      spec.trackColor = spec.indicatorColors[0];

      TypedArray disabledAlphaArray =
          context.getTheme().obtainStyledAttributes(new int[] {android.R.attr.disabledAlpha});
      float defaultOpacity = disabledAlphaArray.getFloat(0, DEFAULT_OPACITY);
      disabledAlphaArray.recycle();

      int trackAlpha = (int) (MAX_ALPHA * defaultOpacity);
      spec.trackColor = MaterialColors.compositeARGBWithAlpha(spec.trackColor, trackAlpha);
    }
    // Gets linearSeamless or overrides it if necessary.
    if (isEligibleToSeamless()) {
      spec.linearSeamless = a.getBoolean(R.styleable.ProgressIndicator_linearSeamless, true);
    } else {
      spec.linearSeamless = false;
    }
    // Gets the radius of rounded corners if defined, otherwise, use 0 (sharp corner).
    setIndicatorCornerRadius(
        a.getDimensionPixelSize(
            R.styleable.ProgressIndicator_indicatorCornerRadius, spec.indicatorCornerRadius));
    // Sets if is indeterminate.
    setIndeterminate(a.getBoolean(R.styleable.ProgressIndicator_android_indeterminate, false));

    if (a.hasValue(R.styleable.ProgressIndicator_minHideDelay)) {
      int minHideDelayUncapped = a.getInt(R.styleable.ProgressIndicator_minHideDelay, -1);
      minHideDelay = min(minHideDelayUncapped, MAX_HIDE_DELAY);
    }

    a.recycle();
  }

  private void initializeDrawables() {
    // Creates and sets the determinate and indeterminate drawables based on track shape.
    if (spec.indicatorType == LINEAR) {
      DrawingDelegate drawingDelegate = new LinearDrawingDelegate();
      IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate =
          isLinearSeamless()
              ? new LinearIndeterminateSeamlessAnimatorDelegate()
              : new LinearIndeterminateNonSeamlessAnimatorDelegate(getContext());
      setIndeterminateDrawable(
          new IndeterminateDrawable(getContext(), spec, drawingDelegate, animatorDelegate));
      setProgressDrawable(new DeterminateDrawable(getContext(), spec, drawingDelegate));
    } else {
      DrawingDelegate drawingDelegate = new CircularDrawingDelegate();
      setIndeterminateDrawable(
          new IndeterminateDrawable(
              getContext(), spec, drawingDelegate, new CircularIndeterminateAnimatorDelegate()));
      setProgressDrawable(new DeterminateDrawable(getContext(), spec, drawingDelegate));
    }

    applyNewVisibility();
  }

  /**
   * Manually initializes drawables and applies visibility. This intends to be used for applying
   * custom drawable.
   *
   * @param indeterminateDrawable Custom indeterminate drawable. Switches to determinate mode if
   *     null.
   * @param determinateDrawable Custom determinate drawable. Switches to indeterminate mode if null.
   * @throws IllegalArgumentException If {@code indicatorType} is not {@code CUSTOM}.
   * @throws IllegalArgumentException If the arguments are both null.
   */
  public void initializeDrawables(
      IndeterminateDrawable indeterminateDrawable, DeterminateDrawable determinateDrawable) {
    if (spec.indicatorType != CUSTOM) {
      throw new IllegalStateException(
          "Manually setting drawables can only be done while indicator type is custom. Current"
              + " indicator type is "
              + (spec.indicatorType == LINEAR ? "linear" : "circular"));
    }
    if (indeterminateDrawable == null && determinateDrawable == null) {
      throw new IllegalArgumentException(
          "Indeterminate and determinate drawables cannot be null at the same time.");
    }
    setIndeterminateDrawable(indeterminateDrawable);
    setProgressDrawable(determinateDrawable);

    setIndeterminate(
        indeterminateDrawable != null && (determinateDrawable == null || isIndeterminate()));

    applyNewVisibility();
  }

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
   * Sets the visibility to {@code VISIBLE}. If this changes the visibility it will invoke {@code
   * onVisibilityChanged} and handle the visibility with animation of the drawables.
   *
   * @see #onVisibilityChanged(View, int)
   */
  public void show() {
    if (minHideDelay > 0) {
      // The hide delay is positive, saves the time of starting show action.
      lastShowStartTime = SystemClock.uptimeMillis();
    }
    setVisibility(VISIBLE);
  }

  /**
   * Hide the progress indicator. If {@code minHideDelay} has been set to positive value, wait until
   * the delay elapsed before starting hide action. Otherwise start hiding immediately.
   */
  public void hide() {
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
    getCurrentDrawable().setVisible(/*visible=*/ false, /*restart=*/ false);

    if (isNoLongerNeedToBeVisible()) {
      setVisibility(INVISIBLE);
    }
  }

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

    getCurrentDrawable().setVisible(visibleToUser(), /*restart=*/ false);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    registerAnimationCallbacks();
    // Shows with animation.
    if (visibleToUser()) {
      show();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    // Removes the delayedHide runnable from the queue if it has been scheduled.
    removeCallbacks(delayedHide);
    getCurrentDrawable().setVisible(/*visible=*/ false, /*restart=*/ false);
    unregisterAnimationCallbacks();
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
    DrawingDelegate drawingDelegate = getCurrentDrawingDelegate();
    int drawableMeasuredWidth = drawingDelegate.getPreferredWidth(spec);
    int drawableMeasuredHeight = drawingDelegate.getPreferredHeight(spec);
    setMeasuredDimension(
        (drawableMeasuredWidth < 0
            ? getMeasuredWidth()
            : drawableMeasuredWidth + getPaddingLeft() + getPaddingRight()),
        (drawableMeasuredHeight < 0
            ? getMeasuredHeight()
            : drawableMeasuredHeight + getPaddingTop() + getPaddingBottom()));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    // set the drawable bounds ourselves for linear, since ProgressBar tries to maintain aspect
    // ratio and we don't want that.
    if (spec.indicatorType == LINEAR) {
      int contentWidth = w - (getPaddingLeft() + getPaddingRight());
      int contentHeight = h - (getPaddingTop() + getPaddingBottom());
      Drawable drawable = getIndeterminateDrawable();
      if (drawable != null) {
        drawable.setBounds(/*left=*/ 0, /*top=*/ 0, contentWidth, contentHeight);
      }
      drawable = getProgressDrawable();
      if (drawable != null) {
        drawable.setBounds(/*left=*/ 0, /*top=*/ 0, contentWidth, contentHeight);
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
    return isIndeterminate() && spec.indicatorType == LINEAR && spec.indicatorColors.length >= 3;
  }

  /** Returns the corresponding drawable based on current indeterminate state. */
  @Override
  @Nullable
  public DrawableWithAnimatedVisibilityChange getCurrentDrawable() {
    return isIndeterminate() ? getIndeterminateDrawable() : getProgressDrawable();
  }

  /** Returns the drawing delegate associated with the current drawable. */
  @NonNull
  public DrawingDelegate getCurrentDrawingDelegate() {
    return isIndeterminate()
        ? getIndeterminateDrawable().getDrawingDelegate()
        : getProgressDrawable().getDrawingDelegate();
  }

  /** Sets a new progress drawable. It has to inherit from {@link DeterminateDrawable}.
   *
   * @param drawable The new progress drawable.
   * @throws IllegalArgumentException if a framework drawable is passed in.
   */
  @Override
  public void setProgressDrawable(Drawable drawable) {
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

  /** Sets a new indeterminate drawable. It has to inherit from {@link IndeterminateDrawable}.
   *
   * @param drawable The new indeterminate drawable.
   * @throws IllegalArgumentException if a framework drawable is passed in.
   */
  @Override
  public void setIndeterminateDrawable(Drawable drawable) {
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

  @Override
  public DeterminateDrawable getProgressDrawable() {
    return (DeterminateDrawable) super.getProgressDrawable();
  }

  @Override
  public IndeterminateDrawable getIndeterminateDrawable() {
    return (IndeterminateDrawable) super.getIndeterminateDrawable();
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
    getIndeterminateDrawable().getAnimatorDelegate().invalidateSpecValues();
  }

  /**
   * Returns {@code true} if both drawables are either null or not visible; {@code false},
   * otherwise.
   */
  private boolean isNoLongerNeedToBeVisible() {
    return (getProgressDrawable() == null || !getProgressDrawable().isVisible())
        && (getIndeterminateDrawable() == null || !getIndeterminateDrawable().isVisible());
  }

  // ******************** Getters and setters **********************

  /** Returns the spec of this progress indicator. */
  public ProgressIndicatorSpec getSpec() {
    return spec;
  }

  /**
   * Returns the indicator type of this progress indicator.
   *
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorType
   */
  public int getIndicatorType() {
    return spec.indicatorType;
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
    if (visibleToUser() && spec.indicatorType != indicatorType) {
      throw new IllegalStateException(
          "Cannot change indicatorType while the progress indicator is visible.");
    }
    spec.indicatorType = indicatorType;
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
    if (indeterminate == isIndeterminate()) {
      // Early return if no change.
      return;
    }
    if (!indeterminate && isLinearSeamless()) {
      // Early return if trying to set to determinate mode while in linear seamless mode, as it's
      // not supported in spec.
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
   * Returns the indicator width of this progress indicator in pixels.
   *
   * @see #setIndicatorWidth(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_indicatorWidth
   */
  public int getIndicatorWidth() {
    return spec.indicatorWidth;
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
    if (spec.indicatorWidth != indicatorWidth) {
      spec.indicatorWidth = indicatorWidth;
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
    return spec.indicatorColors;
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
    spec.indicatorColors = indicatorColors;
    updateColorsInDrawables();
    if (!isEligibleToSeamless()) {
      spec.linearSeamless = false;
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
    return spec.trackColor;
  }

  /**
   * Sets the color of the track of this progress indicator.
   *
   * @param trackColor The new color used in the track of this progress indicator.
   * @see #getTrackColor()
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_trackColor
   */
  public void setTrackColor(@ColorInt int trackColor) {
    if (spec.trackColor != trackColor) {
      spec.trackColor = trackColor;
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
    return spec.inverse;
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
    if (spec.inverse != inverse) {
      spec.inverse = inverse;
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
    return spec.growMode;
  }

  /**
   * Sets the mode of how this progress indicator will appear and disappear.
   *
   * @param growMode New grow mode.
   * @see #getGrowMode()
   * @attr ref com.google.android.material.progressindicator.R.stylable#ProgressIndicator_growMode
   */
  public void setGrowMode(@GrowMode int growMode) {
    if (spec.growMode != growMode) {
      spec.growMode = growMode;
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
    return spec.linearSeamless;
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
    if (spec.linearSeamless == linearSeamless) {
      return;
    }
    if (visibleToUser() && isIndeterminate()) {
      throw new IllegalStateException(
          "Cannot change linearSeamless while the progress indicator is shown in indeterminate"
              + " mode.");
    }
    if (isEligibleToSeamless()) {
      spec.linearSeamless = linearSeamless;
      if (linearSeamless) {
        spec.indicatorCornerRadius = 0;
      }
      if (linearSeamless) {
        getIndeterminateDrawable()
            .setAnimatorDelegate(new LinearIndeterminateSeamlessAnimatorDelegate());
      } else {
        getIndeterminateDrawable()
            .setAnimatorDelegate(new LinearIndeterminateNonSeamlessAnimatorDelegate(getContext()));
      }
    } else {
      spec.linearSeamless = false;
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
    return spec.indicatorCornerRadius;
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
    if (spec.indicatorCornerRadius != indicatorCornerRadius) {
      spec.indicatorCornerRadius = min(indicatorCornerRadius, spec.indicatorWidth / 2);
      if (spec.linearSeamless && indicatorCornerRadius > 0) {
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
    return spec.circularInset;
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
    if (spec.indicatorType == CIRCULAR && spec.circularInset != circularInset) {
      spec.circularInset = circularInset;
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
    return spec.circularRadius;
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
    if (spec.indicatorType == CIRCULAR && spec.circularRadius != circularRadius) {
      spec.circularRadius = circularRadius;
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
      if (getProgressDrawable() != null && !isLinearSeamless()) {
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
          if (!isIndeterminateModeChangeRequested) {
            // Don't hide the component if under transition from indeterminate mode to
            // determinate mode.
            setVisibility(INVISIBLE);
          }
        }
      };
}
