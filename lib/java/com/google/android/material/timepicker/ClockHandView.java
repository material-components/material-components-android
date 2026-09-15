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

package com.google.android.material.timepicker;

import static android.view.HapticFeedbackConstants.CLOCK_TICK;

import com.google.android.material.R;

import static com.google.android.material.timepicker.RadialViewGroup.LEVEL_1;
import static com.google.android.material.timepicker.RadialViewGroup.LEVEL_2;
import static com.google.android.material.timepicker.RadialViewGroup.LEVEL_RADIUS_RATIO;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.annotation.Dimension;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.timepicker.RadialViewGroup.Level;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/** A Class to draw the hand on a Clock face. */
class ClockHandView extends View {

  private static final int SNAP_MODE_CONTINUOUS = 0;
  private static final int SNAP_MODE_BY_STOPS = 1;
  private static final int SNAP_MODE_BY_NUMBER_STOPS = 2;

  @IntDef({
      SNAP_MODE_CONTINUOUS,
      SNAP_MODE_BY_STOPS,
      SNAP_MODE_BY_NUMBER_STOPS
  })
  @Retention(RetentionPolicy.SOURCE)
  private @interface SnapMode {}

  private static final int DEFAULT_ANIMATION_DURATION = 200;
  private final int animationDuration;
  private final TimeInterpolator animationInterpolator;
  @NonNull private final ValueAnimator rotationAnimator = new ValueAnimator();
  private boolean animatingOnTouchUp;
  private float downX;
  private float downY;
  private final int scaledTouchSlop;
  private boolean isMultiLevel;

  private int stopCount;
  private int numberStopCount;
  private int stopOffset;

  /** A listener whenever the hand is rotated. */
  public interface OnRotateListener {
    void onRotate(
        @FloatRange(from = 0f, to = 360f) float degrees,
        @Level int level,
        boolean animating);
  }

  /** A listener called whenever the hand is released, after a touch event stream. */
  public interface OnActionUpListener {
    void onActionUp(
        @FloatRange(from = 0f, to = 360f) float degrees,
        @Level int level);
  }

  private final List<OnRotateListener> listeners = new ArrayList<>();

  private final int selectorRadius;
  private final int centerDotRadius;
  private final Paint paint = new Paint();

  private float centerX;
  private float centerY;
  private float selectorCenterX;
  private float selectorCenterY;
  private float selectorLineX;
  private float selectorLineY;

  @Px private final int selectorStrokeWidth;

  private float originalDeg;

  private boolean isBeingDragged;
  private OnActionUpListener onActionUpListener;

  private int circleRadius;

  @Level private int currentLevel = LEVEL_1;

  public ClockHandView(Context context) {
    this(context, null);
  }

  public ClockHandView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialClockStyle);
  }

  public ClockHandView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ClockHandView,
            defStyleAttr,
            R.style.Widget_MaterialComponents_TimePicker_Clock);

    animationDuration =
        MotionUtils.resolveThemeDuration(
            context, R.attr.motionDurationLong2, DEFAULT_ANIMATION_DURATION);
    animationInterpolator =
        MotionUtils.resolveThemeInterpolator(
            context,
            R.attr.motionEasingEmphasizedInterpolator,
            AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    circleRadius = a.getDimensionPixelSize(R.styleable.ClockHandView_materialCircleRadius, 0);
    selectorRadius = a.getDimensionPixelSize(R.styleable.ClockHandView_selectorSize, 0);
    Resources res = getResources();
    selectorStrokeWidth = res.getDimensionPixelSize(R.dimen.material_clock_hand_stroke_width);
    centerDotRadius = res.getDimensionPixelSize(R.dimen.material_clock_hand_center_dot_radius);
    int selectorColor = a.getColor(R.styleable.ClockHandView_clockHandColor, 0);
    paint.setAntiAlias(true);
    paint.setColor(selectorColor);
    setHandRotation(0);

    scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    a.recycle();

    initRotationAnimator();
  }

  private void initRotationAnimator() {
    rotationAnimator.addUpdateListener(
        animation -> {
          float animatedValue = (float) animation.getAnimatedValue();
          setHandRotationInternal(animatedValue, true);
        });

    rotationAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        animation.end();
      }
    });
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    updateClockHandXY();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (!rotationAnimator.isRunning()) {
      // Refresh selector position.
      setHandRotation(getHandRotation());
    }
  }

  public void setHandRotation(@FloatRange(from = 0f, to = 360f) float degrees) {
    setHandRotation(degrees, false);
  }

  public void setHandRotation(@FloatRange(from = 0f, to = 360f) float degrees, boolean animate) {
    setHandPosition(degrees, getCurrentLevel(), SNAP_MODE_CONTINUOUS, animate);
  }

  private void setHandRotationInternal(
      @FloatRange(from = 0f, to = 360f) float degrees, boolean animate) {
    setHandPositionInternal(degrees, getCurrentLevel(), SNAP_MODE_CONTINUOUS, animate);
  }

  private void setHandPosition(float degrees, int level, @SnapMode int mode, boolean animate) {
    rotationAnimator.cancel();

    if (!animate) {
      setHandPositionInternal(degrees, level, mode, /* animate= */ false);
      return;
    }

    Pair<Float, Float> animationValues = getValuesForAnimation(degrees);
    rotationAnimator.setFloatValues(animationValues.first, animationValues.second);
    rotationAnimator.setDuration(animationDuration);
    rotationAnimator.setInterpolator(animationInterpolator);
    rotationAnimator.start();
  }

  private Pair<Float, Float> getValuesForAnimation(float degrees) {
    float currentDegrees = getHandRotation();
    // 12 O'clock is located at 0 degrees, so if we rotate from
    // 330 (11 O'clock) degrees to 0 degrees it would do almost a full rotation.
    // Same to rotate from 0 to 330. Adjust adding a full rotation for both cases. So it animates
    // between 330 and 360 or 360 and 330 respectively.
    if (Math.abs(currentDegrees - degrees) > 180) {
      if (currentDegrees > 180 && degrees < 180) {
        degrees += 360;
      }

      if (currentDegrees < 180 && degrees > 180) {
        currentDegrees += 360;
      }
    }

    return new Pair<>(currentDegrees, degrees);
  }

  private void setHandPositionInternal(float degrees, int level, @SnapMode int mode, boolean animate) {
    degrees = calculateActualAngle(degrees, mode);

    if (degrees == this.originalDeg && level == this.currentLevel) {
      return;
    }

    this.originalDeg = degrees;
    this.currentLevel = level;
    updateClockHandXY();

    if (mode == SNAP_MODE_BY_STOPS || mode == SNAP_MODE_BY_NUMBER_STOPS) {
      performHapticFeedback(CLOCK_TICK);
    }

    dispatchOnRotate(degrees, level, animate);
    invalidate();
  }

  private float calculateActualAngle(float degrees, @SnapMode int mode) {
    switch (mode) {
      case SNAP_MODE_BY_STOPS:
        return calculateActualAngle(degrees, stopCount, stopOffset);
      case SNAP_MODE_BY_NUMBER_STOPS:
        return calculateActualAngle(degrees, numberStopCount, stopOffset);
      case SNAP_MODE_CONTINUOUS:
        return degrees % 360f;
      default:
        throw new RuntimeException("Unhandled mode: " + mode);
    }
  }

  private static float calculateActualAngle(float degrees, int stopCount, int stopOffset) {
    if (stopCount == 0) {
      return degrees % 360f;
    }

    float step = 360f / stopCount;
    float closestDegrees = (float) (Math.floor((degrees + step / 2f) / step)) * step;
    return (closestDegrees + stopOffset) % 360;
  }

  private void dispatchOnRotate(
      @FloatRange(from = 0f, to = 360f) float degrees,
      @Level int level,
      boolean animating) {
    for (OnRotateListener listener : listeners) {
      listener.onRotate(degrees, level, animating);
    }
  }

  public void setAnimateOnTouchUp(boolean animating) {
    animatingOnTouchUp = animating;
  }

  public void addOnRotateListener(OnRotateListener listener) {
    listeners.add(listener);
  }

  public void setOnActionUpListener(OnActionUpListener listener) {
    this.onActionUpListener = listener;
  }

  void setStopParams(int stopCount, int numberStopCount, int stopOffset) {
    if (this.stopCount == stopCount
        && this.numberStopCount == numberStopCount
        && this.stopOffset == stopOffset) {
      return;
    }

    this.stopCount = stopCount;
    this.numberStopCount = numberStopCount;
    this.stopOffset = stopOffset;
    invalidate();
  }

  @FloatRange(from = 0f, to = 360f)
  public float getHandRotation() {
    return originalDeg;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Draw the line.
    paint.setStrokeWidth(selectorStrokeWidth);
    canvas.drawLine(centerX, centerY, selectorLineX, selectorLineY, paint);
    canvas.drawCircle(centerX, centerY, centerDotRadius, paint);

    // Draw the selection circle.
    paint.setStrokeWidth(0);
    canvas.drawCircle(selectorCenterX, selectorCenterY, selectorRadius, paint);
  }

  private void updateClockHandXY() {
    centerX = getWidth() / 2f;
    centerY = getHeight() / 2f;

    // Subtract 90f so that 0 degrees is at number 12.
    double degRad = Math.toRadians(originalDeg - 90f);

    double sin = Math.sin(degRad);
    double cos = Math.cos(degRad);

    float leveledCircleRadius = getLeveledCircleRadius(currentLevel);
    selectorCenterX = (float) (centerX + leveledCircleRadius * cos);
    selectorCenterY = (float) (centerY + leveledCircleRadius * sin);

    // Shorten the line to only go from the edge of the center dot to the
    // edge of the selection circle.
    float lineLength = leveledCircleRadius - selectorRadius;
    selectorLineX = (float) (centerX + lineLength * cos);
    selectorLineY = (float) (centerY + lineLength * sin);
  }

  /** Returns the current bounds of the selector, relative to the this view. */
  public RectF getCurrentSelectorBox() {
    return new RectF(
        selectorCenterX - selectorRadius,
        selectorCenterY - selectorRadius,
        selectorCenterX + selectorRadius,
        selectorCenterY + selectorRadius);
  }

  /** Returns the current radius of the selector */
  public int getSelectorRadius() {
    return selectorRadius;
  }

  /**
   * Set the size of the of the circle. This is the radius from the center of this view to the outer
   * edge of the selector.
   */
  public void setCircleRadius(@Dimension int circleRadius) {
    if (this.circleRadius == circleRadius) {
      return;
    }

    this.circleRadius = circleRadius;
    updateClockHandXY();
    invalidate();
  }

  @Override
  @SuppressLint("ClickableViewAccessibility")
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    float x = event.getX();
    float y = event.getY();

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        downX = x;
        downY = y;
        isBeingDragged = isOnSelector(x, y);

        @SnapMode int mode = isBeingDragged ? SNAP_MODE_BY_STOPS : SNAP_MODE_BY_NUMBER_STOPS;
        setHandPositionFromXY(x, y, mode, /* animate= */ false);
        break;
      case MotionEvent.ACTION_MOVE:
        isBeingDragged |= isOutsideTapRegion(x, y, downX, downY);
        if (isBeingDragged) {
          setHandPositionFromXY(x, y, SNAP_MODE_BY_STOPS, /* animate= */ false);
        }
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        isBeingDragged |= isOutsideTapRegion(x, y, downX, downY);
        if (isBeingDragged) {
          setHandPositionFromXY(x, y, SNAP_MODE_BY_STOPS, animatingOnTouchUp);
        }

        dispatchOnActionUp(originalDeg, currentLevel);
        break;
      default:
        break;
    }

    return true;
  }

  private boolean isOnSelector(float x, float y) {
    return Math.hypot(x - selectorCenterX, y - selectorCenterY) <= selectorRadius;
  }

  private boolean isOutsideTapRegion(float upX, float upY, float downX, float downY) {
    return Math.hypot(upX - downX, upY - downY) > scaledTouchSlop;
  }

  private void setHandPositionFromXY(float x, float y, @SnapMode int mode, boolean animate) {
    float degrees = getDegreesFromXY(x, y);
    int level = getLevelFromXY(x, y);

    setHandPosition(degrees, level, mode, animate);
  }

  private float getDegreesFromXY(float x, float y) {
    int xCenter = getWidth() / 2;
    int yCenter = getHeight() / 2;
    double dX = x - xCenter;
    double dY = y - yCenter;
    float degrees = (float) (Math.toDegrees(Math.atan2(dY, dX)) + 90);
    if (degrees < 0) {
      degrees += 360;
    }
    return degrees;
  }

  @Level
  private int getLevelFromXY(float x, float y) {
    if (!isMultiLevel) {
      return LEVEL_1;
    }

    int xCenter = getWidth() / 2;
    int yCenter = getHeight() / 2;
    float selectionRadius = (float) Math.hypot(x - xCenter, y - yCenter);
    int level2CircleRadius = getLeveledCircleRadius(LEVEL_2);
    float buffer = ViewUtils.dpToPx(getContext(), 12);
    return selectionRadius <= level2CircleRadius + buffer ? LEVEL_2 : LEVEL_1;
  }

  private void dispatchOnActionUp(
      @FloatRange(from = 0f, to = 360f) float degrees,
      @Level int level) {
    if (onActionUpListener != null) {
      onActionUpListener.onActionUp(degrees, level);
    }
  }

  @Level
  int getCurrentLevel() {
    return currentLevel;
  }

  void setCurrentLevel(@Level int level) {
    if (this.currentLevel == level) {
      return;
    }

    this.currentLevel = level;
    updateClockHandXY();
    invalidate();
  }

  void setMultiLevel(boolean isMultiLevel) {
    if (this.isMultiLevel && !isMultiLevel) {
      currentLevel = LEVEL_1; // reset
    }
    this.isMultiLevel = isMultiLevel;
    updateClockHandXY();
    invalidate();
  }

  @Dimension
  private int getLeveledCircleRadius(@Level int level) {
    return level == LEVEL_2 ? Math.round(circleRadius * LEVEL_RADIUS_RATIO) : circleRadius;
  }
}
