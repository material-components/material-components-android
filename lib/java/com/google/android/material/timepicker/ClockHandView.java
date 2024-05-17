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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.math.MathUtils;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.timepicker.RadialViewGroup.Level;
import java.util.ArrayList;
import java.util.List;

/** A Class to draw the hand on a Clock face. */
class ClockHandView extends View {

  private static final int DEFAULT_ANIMATION_DURATION = 200;
  private final int animationDuration;
  private final TimeInterpolator animationInterpolator;
  @NonNull private final ValueAnimator rotationAnimator = new ValueAnimator();
  private boolean animatingOnTouchUp;
  private float downX;
  private float downY;
  private boolean isInTapRegion;
  private final int scaledTouchSlop;
  private boolean isMultiLevel;

  /** A listener whenever the hand is rotated. */
  public interface OnRotateListener {
    void onRotate(@FloatRange(from = 0f, to = 360f) float rotation, boolean animating);
  }

  /** A listener called whenever the hand is released, after a touch event stream. */
  public interface OnActionUpListener {
    void onActionUp(@FloatRange(from = 0f, to = 360f) float rotation, boolean moveInEventStream);
  }

  private final List<OnRotateListener> listeners = new ArrayList<>();

  private final int selectorRadius;
  private final float centerDotRadius;
  private final Paint paint = new Paint();
  // Since the selector moves, overlapping views may need information about
  // its current position
  private final RectF selectorBox = new RectF();

  @Px private final int selectorStrokeWidth;

  private float originalDeg;

  private boolean changedDuringTouch;
  private OnActionUpListener onActionUpListener;

  private double degRad;
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
    rotationAnimator.cancel();

    if (!animate) {
      setHandRotationInternal(degrees, false);
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

  private void setHandRotationInternal(
      @FloatRange(from = 0f, to = 360f) float degrees, boolean animate) {
    degrees = degrees % 360;
    originalDeg = degrees;
    // Subtract 90f so that 0 degrees is at number 12.
    float angDeg = originalDeg - 90f;

    degRad = Math.toRadians(angDeg);
    int yCenter = getHeight() / 2;
    int xCenter = getWidth() / 2;
    int leveledCircleRadius = getLeveledCircleRadius(currentLevel);
    float selCenterX = xCenter + leveledCircleRadius * (float) Math.cos(degRad);
    float selCenterY = yCenter + leveledCircleRadius * (float) Math.sin(degRad);
    selectorBox.set(
        selCenterX - selectorRadius,
        selCenterY - selectorRadius,
        selCenterX + selectorRadius,
        selCenterY + selectorRadius);

    for (OnRotateListener listener : listeners) {
      listener.onRotate(degrees, animate);
    }

    invalidate();
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

  @FloatRange(from = 0f, to = 360f)
  public float getHandRotation() {
    return originalDeg;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    drawSelector(canvas);
  }

  private void drawSelector(Canvas canvas) {
    int yCenter = getHeight() / 2;
    int xCenter = getWidth() / 2;

    // Calculate the current radius at which to place the selection circle.
    int leveledCircleRadius = getLeveledCircleRadius(currentLevel);
    float selCenterX = xCenter + leveledCircleRadius * (float) Math.cos(degRad);
    float selCenterY = yCenter + leveledCircleRadius * (float) Math.sin(degRad);

    // Draw the selection circle.
    paint.setStrokeWidth(0);
    canvas.drawCircle(selCenterX, selCenterY, selectorRadius, paint);

    // Shorten the line to only go from the edge of the center dot to the
    // edge of the selection circle.
    double sin = Math.sin(degRad);
    double cos = Math.cos(degRad);
    float lineLength = leveledCircleRadius - selectorRadius;
    float linePointX = xCenter + (int) (lineLength * cos);
    float linePointY = yCenter + (int) (lineLength * sin);

    // Draw the line.
    paint.setStrokeWidth(selectorStrokeWidth);
    canvas.drawLine(xCenter, yCenter, linePointX, linePointY, paint);
    canvas.drawCircle(xCenter, yCenter, centerDotRadius, paint);
  }

  /** Returns the current bounds of the selector, relative to the this view. */
  public RectF getCurrentSelectorBox() {
    return selectorBox;
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
    this.circleRadius = circleRadius;
    invalidate();
  }

  @Override
  @SuppressLint("ClickableViewAccessibility")
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    boolean forceSelection = false;
    boolean actionDown = false;
    boolean actionUp = false;
    float x = event.getX();
    float y = event.getY();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        downX = x;
        downY = y;
        isInTapRegion = true;
        // This is a new event stream.
        changedDuringTouch = false;
        actionDown = true;
        break;
      case MotionEvent.ACTION_MOVE:
      case MotionEvent.ACTION_UP:
        final int deltaX = (int) (x - downX);
        final int deltaY = (int) (y - downY);
        int distance = (deltaX * deltaX) + (deltaY * deltaY);
        isInTapRegion = distance > scaledTouchSlop;
        // If we saw a down/up pair without the value changing, assume
        // this is a single-tap selection and force a change.
        if (changedDuringTouch) {
          forceSelection = true;
        }
        actionUp = action == MotionEvent.ACTION_UP;
        if (isMultiLevel) {
          adjustLevel(x, y);
        }
        break;
      default:
        break;
    }

    changedDuringTouch |= handleTouchInput(x, y, forceSelection, actionDown, actionUp);
    if (changedDuringTouch && actionUp && onActionUpListener != null) {
      onActionUpListener.onActionUp(getDegreesFromXY(x, y), /* moveInEventStream= */ isInTapRegion);
    }

    return true;
  }

  private void adjustLevel(float x, float y) {
    int xCenter = getWidth() / 2;
    int yCenter = getHeight() / 2;
    float selectionRadius = MathUtils.dist(xCenter, yCenter, x, y);
    int level2CircleRadius = getLeveledCircleRadius(LEVEL_2);
    float buffer = ViewUtils.dpToPx(getContext(), 12);
    currentLevel = selectionRadius <= level2CircleRadius + buffer ? LEVEL_2 : LEVEL_1;
  }

  private boolean handleTouchInput(
      float x, float y, boolean forceSelection, boolean touchDown, boolean actionUp) {
    int degrees = getDegreesFromXY(x, y);
    boolean valueChanged = getHandRotation() != degrees;
    if (touchDown && valueChanged) {
      return true;
    }

    if (valueChanged || forceSelection) {
      setHandRotation(degrees, actionUp && animatingOnTouchUp);
      return true;
    }

    return false;
  }

  private int getDegreesFromXY(float x, float y) {
    int xCenter = getWidth() / 2;
    int yCenter = getHeight() / 2;
    double dX = x - xCenter;
    double dY = y - yCenter;
    int degrees = (int) Math.toDegrees(Math.atan2(dY, dX)) + 90;
    if (degrees < 0) {
      degrees += 360;
    }
    return degrees;
  }

  @Level
  int getCurrentLevel() {
    return currentLevel;
  }

  void setCurrentLevel(@Level int level) {
    currentLevel = level;
    invalidate();
  }

  void setMultiLevel(boolean isMultiLevel) {
    if (this.isMultiLevel && !isMultiLevel) {
      currentLevel = LEVEL_1; // reset
    }
    this.isMultiLevel = isMultiLevel;
    invalidate();
  }

  @Dimension
  private int getLeveledCircleRadius(@Level int level) {
    return level == LEVEL_2 ? Math.round(circleRadius * LEVEL_RADIUS_RATIO) : circleRadius;
  }
}
