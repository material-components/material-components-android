/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.google.android.material.motion;

import com.google.android.material.R;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.graphics.PathParser;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.resources.MaterialAttributes;

/** A utility class for motion system functions. */
public class MotionUtils {

  // Constants corresponding to motionEasing* theme attr values.
  private static final String EASING_TYPE_CUBIC_BEZIER = "cubic-bezier";
  private static final String EASING_TYPE_PATH = "path";
  private static final String EASING_TYPE_FORMAT_START = "(";
  private static final String EASING_TYPE_FORMAT_END = ")";

  private MotionUtils() {}

  /**
   * Resolve a {@link SpringForce} object from a Material spring theme attribute.
   *
   * @param context the context from where the theme attribute will be resolved
   * @param attrResId the {@code motionSpring*} theme attribute to resolve
   * into a {@link SpringForce} object
   * @param defStyleRes a {@code MaterialSpring} style to load if attrResId cannot be resolved
   * @return a {@link SpringForce} object configured using the stiffness and damping from the
   * resolved Material spring attribute
   */
  @NonNull
  public static SpringForce resolveThemeSpringForce(
      @NonNull Context context, @AttrRes int attrResId, @StyleRes int defStyleRes) {

    TypedValue tv = MaterialAttributes.resolve(context, attrResId);
    TypedArray a;
    if (tv == null) {
      a = context.obtainStyledAttributes(null, R.styleable.MaterialSpring, 0, defStyleRes);
    } else {
      a = context.obtainStyledAttributes(tv.resourceId, R.styleable.MaterialSpring);
    }

    SpringForce springForce = new SpringForce();
    try {
      float stiffness = a.getFloat(R.styleable.MaterialSpring_stiffness, Float.MIN_VALUE);
      if (stiffness == Float.MIN_VALUE) {
        throw new IllegalArgumentException("A MaterialSpring style must have stiffness value.");
      }
      float damping = a.getFloat(R.styleable.MaterialSpring_damping, Float.MIN_VALUE);
      if (damping == Float.MIN_VALUE) {
        throw new IllegalArgumentException("A MaterialSpring style must have a damping value.");
      }

      springForce.setStiffness(stiffness);
      springForce.setDampingRatio(damping);
    } finally {
      a.recycle();
    }
    return springForce;
  }

  /**
   * Resolve a duration from a material duration theme attribute.
   *
   * @param context the context from where the theme attribute will be resolved.
   * @param attrResId the {@code motionDuration*} theme attribute to resolve
   * @param defaultDuration the duration to be returned if unable to resolve {@code attrResId}
   * @return the resolved {@code int} duration which {@code attrResId} points to or the {@code
   *     defaultDuration} if resolution was unsuccessful.
   */
  public static int resolveThemeDuration(
      @NonNull Context context, @AttrRes int attrResId, int defaultDuration) {
    return MaterialAttributes.resolveInteger(context, attrResId, defaultDuration);
  }

  /**
   * Load an interpolator from a material easing theme attribute.
   *
   * @param context context from where the theme attribute will be resolved
   * @param attrResId the {@code motionEasing*} theme attribute to resolve
   * @param defaultInterpolator the interpolator to be returned if unable to resolve {@code
   *     attrResId}.
   * @return the resolved {@link TimeInterpolator} which {@code attrResId} points to or the {@code
   *     defaultInterpolator} if resolution was unsuccessful.
   */
  @NonNull
  public static TimeInterpolator resolveThemeInterpolator(
      @NonNull Context context,
      @AttrRes int attrResId,
      @NonNull TimeInterpolator defaultInterpolator) {
    TypedValue easingValue = new TypedValue();
    if (!context.getTheme().resolveAttribute(attrResId, easingValue, true)) {
      return defaultInterpolator;
    }

    if (easingValue.type != TypedValue.TYPE_STRING) {
      throw new IllegalArgumentException(
          "Motion easing theme attribute must be an @interpolator resource for"
              + " ?attr/motionEasing*Interpolator attributes or a string for"
              + " ?attr/motionEasing* attributes.");
    }

    String easingString = String.valueOf(easingValue.string);
    if (isLegacyEasingAttribute(easingString)) {
      return getLegacyThemeInterpolator(easingString);
    }

    return AnimationUtils.loadInterpolator(context, easingValue.resourceId);
  }

  private static TimeInterpolator getLegacyThemeInterpolator(String easingString) {
    if (isLegacyEasingType(easingString, EASING_TYPE_CUBIC_BEZIER)) {
      String controlPointsString = getLegacyEasingContent(easingString, EASING_TYPE_CUBIC_BEZIER);
      String[] controlPoints = controlPointsString.split(",");
      if (controlPoints.length != 4) {
        throw new IllegalArgumentException(
            "Motion easing theme attribute must have 4 control points if using bezier curve"
                + " format; instead got: "
                + controlPoints.length);
      }

      float controlX1 = getLegacyControlPoint(controlPoints, 0);
      float controlY1 = getLegacyControlPoint(controlPoints, 1);
      float controlX2 = getLegacyControlPoint(controlPoints, 2);
      float controlY2 = getLegacyControlPoint(controlPoints, 3);
      return new PathInterpolator(controlX1, controlY1, controlX2, controlY2);
    } else if (isLegacyEasingType(easingString, EASING_TYPE_PATH)) {
      String path = getLegacyEasingContent(easingString, EASING_TYPE_PATH);
      return new PathInterpolator(PathParser.createPathFromPathData(path));
    } else {
      throw new IllegalArgumentException("Invalid motion easing type: " + easingString);
    }
  }

  private static boolean isLegacyEasingAttribute(String easingString) {
    return isLegacyEasingType(easingString, EASING_TYPE_CUBIC_BEZIER)
        || isLegacyEasingType(easingString, EASING_TYPE_PATH);
  }

  private static boolean isLegacyEasingType(String easingString, String easingType) {
    return easingString.startsWith(easingType + EASING_TYPE_FORMAT_START)
        && easingString.endsWith(EASING_TYPE_FORMAT_END);
  }

  private static String getLegacyEasingContent(String easingString, String easingType) {
    return easingString.substring(
        easingType.length() + EASING_TYPE_FORMAT_START.length(),
        easingString.length() - EASING_TYPE_FORMAT_END.length());
  }

  private static float getLegacyControlPoint(String[] controlPoints, int index) {
    float controlPoint = Float.parseFloat(controlPoints[index]);
    if (controlPoint < 0 || controlPoint > 1) {
      throw new IllegalArgumentException(
          "Motion easing control point value must be between 0 and 1; instead got: "
              + controlPoint);
    }
    return controlPoint;
  }
}
