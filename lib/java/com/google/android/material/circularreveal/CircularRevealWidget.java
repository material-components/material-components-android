/*
 * Copyright 2017 The Android Open Source Project
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
package com.google.android.material.circularreveal;

import android.animation.TypeEvaluator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.View;
import android.view.ViewAnimationUtils;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.circularreveal.CircularRevealHelper.Delegate;
import com.google.android.material.math.MathUtils;

/**
 * Interface which denotes that a {@link View} supports a circular clip and scrim color, even for
 * pre-L APIs.
 *
 * <h1>Usage</h1>
 *
 * You should not have to interact with instances of this interface directly. To modify the circular
 * clip, use {@link CircularRevealCompat}. To modify the scrim color, use {@link
 * CircularRevealScrimColorProperty}.
 *
 * <h1>Implementation</h1>
 *
 * To support circular reveal for an arbitrary view, create a subclass of that view that implements
 * the {@link CircularRevealWidget} interface. The subclass should instantiate a {@link
 * CircularRevealHelper} and pass itself into the helper's constructor.
 *
 * <p>All unimplemented methods should be implemented as directed in the javadoc.
 *
 * <p>Only {@link View}s should implement this interface. Callers may expect an instance of this
 * interface to be a {@link View}.
 */
public interface CircularRevealWidget extends Delegate {

  /** Implementations should call the corresponding method in {@link CircularRevealHelper}. */
  void draw(Canvas canvas);

  /** Implementations should call the corresponding method in {@link CircularRevealHelper}. */
  boolean isOpaque();

  /**
   * Prepares the <info>reveal info</info> property to be modified. See the interface javadoc for
   * usage details.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  void buildCircularRevealCache();

  /**
   * Cleans up after the <info>reveal info</info> property is reset. See the interface javadoc for
   * usage details.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  void destroyCircularRevealCache();

  /**
   * Returns the current <info>reveal info</info> if one exists, or null. The radius of the
   * <info>reveal info</info> will never be greater than the distance to the furthest corner.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  @Nullable
  RevealInfo getRevealInfo();

  /**
   * Sets the current <info>reveal info</info>. Care should be taken to call {@link
   * #buildCircularRevealCache()} and {@link #destroyCircularRevealCache()} appropriately. See the
   * interface javadoc for usage details.
   *
   * <p>Note that on L+, calling this method doesn't result in any visual changes. You must use this
   * with {@link ViewAnimationUtils}.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  void setRevealInfo(@Nullable RevealInfo revealInfo);

  /** Implementations should call the corresponding method in {@link CircularRevealHelper}. */
  @ColorInt
  int getCircularRevealScrimColor();

  /**
   * Sets the <info>circular reveal scrim color</info>, which is a color that's drawn above this
   * widget's contents.
   *
   * <p>Because the scrim makes no assumptions about the shape of the view's background and content,
   * callers should ensure that the scrim is only visible when the circular reveal does not yet
   * extend to the edges of the view.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  void setCircularRevealScrimColor(@ColorInt int color);

  /**
   * Returns the <info>circular reveal overlay drawable</info> if one exists, or null.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  @Nullable
  Drawable getCircularRevealOverlayDrawable();

  /**
   * Sets the <info>circular reveal overlay drawable</info>, which is an icon that's drawn above
   * everything else, including the circular reveal scrim color.
   *
   * <p>Implementations should call the corresponding method in {@link CircularRevealHelper}.
   */
  void setCircularRevealOverlayDrawable(@Nullable Drawable drawable);

  /**
   * RevealInfo holds three values for a circular reveal. The circular reveal is represented by two
   * float coordinates for the center, and one float value for the radius.
   */
  class RevealInfo {

    /** Radius value representing a lack of a circular reveal clip. */
    public static final float INVALID_RADIUS = Float.MAX_VALUE;

    /** View-local float coordinate for the centerX of the reveal circular reveal. */
    public float centerX;
    /** View-local float coordinate for the centerY of the reveal circular reveal. */
    public float centerY;
    /** Float value for the radius of the reveal circular reveal, or {@link #INVALID_RADIUS}. */
    public float radius;

    private RevealInfo() {}

    public RevealInfo(float centerX, float centerY, float radius) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.radius = radius;
    }

    public RevealInfo(@NonNull RevealInfo other) {
      this(other.centerX, other.centerY, other.radius);
    }

    public void set(float centerX, float centerY, float radius) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.radius = radius;
    }

    public void set(@NonNull RevealInfo other) {
      set(other.centerX, other.centerY, other.radius);
    }

    /**
     * Returns whether this RevealInfo has an invalid radius, representing a lack of a circular
     * reveal clip.
     */
    public boolean isInvalid() {
      return radius == INVALID_RADIUS;
    }
  }

  /**
   * A Property wrapper around the compound <code>circularReveal</code> functionality on a {@link
   * CircularRevealWidget}.
   */
  class CircularRevealProperty extends Property<CircularRevealWidget, RevealInfo> {

    public static final Property<CircularRevealWidget, RevealInfo> CIRCULAR_REVEAL =
        new CircularRevealProperty("circularReveal");

    private CircularRevealProperty(String name) {
      super(RevealInfo.class, name);
    }

    @Nullable
    @Override
    public RevealInfo get(@NonNull CircularRevealWidget object) {
      return object.getRevealInfo();
    }

    @Override
    public void set(@NonNull CircularRevealWidget object, @Nullable RevealInfo value) {
      object.setRevealInfo(value);
    }
  }

  /**
   * A {@link TypeEvaluator} that performs type interpolation between two {@link RevealInfo}s. This
   * encapsulates an animated circular reveal.
   *
   * <p>Each value in the intermediary RevealInfo is simply interpolated from the corresponding
   * values from the start and end RevealInfo.
   */
  class CircularRevealEvaluator implements TypeEvaluator<RevealInfo> {

    public static final TypeEvaluator<RevealInfo> CIRCULAR_REVEAL = new CircularRevealEvaluator();
    private final RevealInfo revealInfo = new RevealInfo();

    @NonNull
    @Override
    public RevealInfo evaluate(
        float fraction, @NonNull RevealInfo startValue, @NonNull RevealInfo endValue) {
      revealInfo.set(
          MathUtils.lerp(startValue.centerX, endValue.centerX, fraction),
          MathUtils.lerp(startValue.centerY, endValue.centerY, fraction),
          MathUtils.lerp(startValue.radius, endValue.radius, fraction));
      return revealInfo;
    }
  }

  /**
   * A Property wrapper around the <code>circularRevealScrimColor</code> functionality on a {@link
   * CircularRevealWidget}.
   */
  class CircularRevealScrimColorProperty extends Property<CircularRevealWidget, Integer> {

    public static final Property<CircularRevealWidget, Integer> CIRCULAR_REVEAL_SCRIM_COLOR =
        new CircularRevealScrimColorProperty("circularRevealScrimColor");

    private CircularRevealScrimColorProperty(String name) {
      super(Integer.class, name);
    }

    @NonNull
    @Override
    public Integer get(@NonNull CircularRevealWidget object) {
      return object.getCircularRevealScrimColor();
    }

    @Override
    public void set(@NonNull CircularRevealWidget object, @NonNull Integer value) {
      object.setCircularRevealScrimColor(value);
    }
  }
}
