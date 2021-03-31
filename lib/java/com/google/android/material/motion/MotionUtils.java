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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.TimeInterpolator;
import android.content.Context;
import androidx.core.graphics.PathParser;
import androidx.core.view.animation.PathInterpolatorCompat;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.resources.MaterialAttributes;

/**
 * A utility class for motion system functions.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MotionUtils {

  // Constants corresponding to motionEasing* theme attr values.
  private static final String EASING_TYPE_CUBIC_BEZIER = "cubic-bezier";
  private static final String EASING_TYPE_PATH = "path";
  private static final String EASING_TYPE_FORMAT_START = "(";
  private static final String EASING_TYPE_FORMAT_END = ")";

  private MotionUtils() {}

  public static int resolveThemeDuration(
      @NonNull Context context, @AttrRes int attrResId, int defaultDuration) {
    return MaterialAttributes.resolveInteger(context, attrResId, defaultDuration);
  }

  @NonNull
  public static TimeInterpolator resolveThemeInterpolator(
      @NonNull Context context,
      @AttrRes int attrResId,
      @NonNull TimeInterpolator defaultInterpolator) {
    TypedValue easingValue = new TypedValue();
    if (context.getTheme().resolveAttribute(attrResId, easingValue, true)) {
      if (easingValue.type != TypedValue.TYPE_STRING) {
        throw new IllegalArgumentException("Motion easing theme attribute must be a string");
      }

      String easingString = String.valueOf(easingValue.string);

      if (isEasingType(easingString, EASING_TYPE_CUBIC_BEZIER)) {
        String controlPointsString = getEasingContent(easingString, EASING_TYPE_CUBIC_BEZIER);
        String[] controlPoints = controlPointsString.split(",");
        if (controlPoints.length != 4) {
          throw new IllegalArgumentException(
              "Motion easing theme attribute must have 4 control points if using bezier curve"
                  + " format; instead got: "
                  + controlPoints.length);
        }

        float controlX1 = getControlPoint(controlPoints, 0);
        float controlY1 = getControlPoint(controlPoints, 1);
        float controlX2 = getControlPoint(controlPoints, 2);
        float controlY2 = getControlPoint(controlPoints, 3);
        return PathInterpolatorCompat.create(controlX1, controlY1, controlX2, controlY2);
      } else if (isEasingType(easingString, EASING_TYPE_PATH)) {
        String path = getEasingContent(easingString, EASING_TYPE_PATH);
        return PathInterpolatorCompat.create(PathParser.createPathFromPathData(path));
      } else {
        throw new IllegalArgumentException("Invalid motion easing type: " + easingString);
      }
    }
    return defaultInterpolator;
  }

  private static boolean isEasingType(String easingString, String easingType) {
    return easingString.startsWith(easingType + EASING_TYPE_FORMAT_START)
        && easingString.endsWith(EASING_TYPE_FORMAT_END);
  }

  private static String getEasingContent(String easingString, String easingType) {
    return easingString.substring(
        easingType.length() + EASING_TYPE_FORMAT_START.length(),
        easingString.length() - EASING_TYPE_FORMAT_END.length());
  }

  private static float getControlPoint(String[] controlPoints, int index) {
    float controlPoint = Float.parseFloat(controlPoints[index]);
    if (controlPoint < 0 || controlPoint > 1) {
      throw new IllegalArgumentException(
          "Motion easing control point value must be between 0 and 1; instead got: "
              + controlPoint);
    }
    return controlPoint;
  }
}
