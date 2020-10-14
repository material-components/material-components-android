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

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class implements the circular type progress indicators.
 *
 * <p>With the default style {@link R.style#Widget_MaterialComponents_CircularProgressIndicator},
 * 4dp indicator/track size and no animation is used for visibility change. Without customization,
 * primaryColor will be used as the indicator color; the track is transparent. The following
 * attributes can be used to customize the component's appearance:
 *
 * <ul>
 *   <li>{@code indicatorSize}: the stroke width of the indicator and track.
 *   <li>{@code indicatorColor}: the color of the indicator.
 *   <li>{@code trackColor}: the color of the track.
 *   <li>{@code indicatorCornerRadius}: the radius of the rounded corner of the indicator stroke.
 *   <li>{@code indicatorRadius}: the radius of the central line of the spinner.
 *   <li>{@code indicatorInset}: the inset from component's bound to the spinner's outer edge.
 *   <li>{@code indicatorDirectionCircular}: the rotation direction of the spinner or indicator.
 *   <li>{@code showBehaviorCircular}: the animation direction to show the indicator and track.
 *   <li>{@code hideBehaviorCircular}: the animation direction to hide the indicator and track.
 * </ul>
 */
public class CircularProgressIndicator extends BaseProgressIndicator {
  public static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_CircularProgressIndicator;

  public static final int INDICATOR_DIRECTION_CLOCKWISE = 0;
  public static final int INDICATOR_DIRECTION_COUNTERCLOCKWISE = 1;

  public static final int SHOW_NONE = 0;
  public static final int SHOW_OUTWARD = 1;
  public static final int SHOW_INWARD = 2;
  public static final int HIDE_NONE = 0;
  public static final int HIDE_OUTWARD = 1;
  public static final int HIDE_INWARD = 2;

  protected final CircularProgressIndicatorSpec spec;

  // **************** Constructors ****************

  public CircularProgressIndicator(@NonNull Context context) {
    this(context, null);
  }

  public CircularProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.circularProgressIndicatorStyle);
  }

  public CircularProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // Ensures that we are using the correctly themed context rather than the context that was
    // passed in.
    context = getContext();

    spec = new CircularProgressIndicatorSpec(context, attrs, baseSpec);
  }

  // **************** Getters and setters ****************

  /** Returns the spec of this progress indicator. */
  @NonNull
  public CircularProgressIndicatorSpec getSpec() {
    return spec;
  }

  /**
   * Returns the inset of circular progress indicator.
   *
   * @see #setIndicatorInset(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_indicatorInset
   */
  @Px
  public int getIndicatorInset() {
    return spec.indicatorInset;
  }

  /**
   * Sets the inset of this progress indicator, if it's circular type.
   *
   * @param indicatorInset The new inset in pixels.
   * @see #getIndicatorInset()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularInset
   */
  public void setIndicatorInset(@Px int indicatorInset) {
    if (spec.indicatorInset != indicatorInset) {
      spec.indicatorInset = indicatorInset;
      invalidate();
    }
  }

  /**
   * Returns the radius of circular progress indicator.
   *
   * @see #setIndicatorRadius(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularRadius
   */
  @Px
  public int getIndicatorrRadius() {
    return spec.indicatorRadius;
  }

  /**
   * Sets the radius of this progress indicator, if it's circular type.
   *
   * @param indicatorRadius The new radius in pixels.
   * @see #getIndicatorrRadius()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#ProgressIndicator_circularRadius
   */
  public void setIndicatorRadius(@Px int indicatorRadius) {
    if (spec.indicatorRadius != indicatorRadius) {
      spec.indicatorRadius = indicatorRadius;
      invalidate();
    }
  }

  /**
   * Returns the indicator animating direction used in this progress indicator.
   *
   * @see #setIndicatorDirection(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_indicatorDirection
   */
  @IndicatorDirection
  public int getIndicatorDirection() {
    return spec.indicatorDirection;
  }

  /**
   * Sets the indicator animatiing direction used in this progress indicator.
   *
   * @param indicatorDirection The new indicator direction.
   * @see #getIndicatorDirection()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_indicatorDirection
   */
  public void setIndicatorDirection(@IndicatorDirection int indicatorDirection) {
    spec.indicatorDirection = indicatorDirection;
    invalidate();
  }

  /**
   * Returns the show behavior used in this progress indicator.
   *
   * @see #setShowBehavior(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_showBehavior
   */
  @ShowBehavior
  public int getShowBehavior() {
    return spec.showBehavior;
  }

  /**
   * Sets the show behavior used in this progress indicator.
   *
   * @param showBehavior The new behavior of show animation.
   * @see #getShowBehavior()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_showBehavior
   */
  public void setShowBehavior(@ShowBehavior int showBehavior) {
    spec.showBehavior = showBehavior;
    invalidate();
  }

  /**
   * Returns the hide behavior used in this progress indicator.
   *
   * @see #setHideBehavior(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_hideBehavior
   */
  @HideBehavior
  public int getHideBehavior() {
    return spec.hideBehavior;
  }

  /**
   * Sets the hide behavior used in this progress indicator.
   *
   * @param hideBehavior The new behavior of hide animation.
   * @see #getHideBehavior()
   * @attr ref
   *     com.google.android.material.progressindicator.R.stylable#CircularProgressIndicator_hideBehavior
   */
  public void setHideBehavior(@HideBehavior int hideBehavior) {
    spec.hideBehavior = hideBehavior;
    invalidate();
  }

  // **************** Interface ****************

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({INDICATOR_DIRECTION_CLOCKWISE, INDICATOR_DIRECTION_COUNTERCLOCKWISE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface IndicatorDirection {}

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({SHOW_NONE, SHOW_OUTWARD, SHOW_INWARD})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ShowBehavior {}

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({HIDE_NONE, HIDE_OUTWARD, HIDE_INWARD})
  @Retention(RetentionPolicy.SOURCE)
  public @interface HideBehavior {}
}
