/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.transition;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.PathParser;
import androidx.core.view.animation.PathInterpolatorCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.PathMotion;
import androidx.transition.PatternPathMotion;
import androidx.transition.Transition;
import androidx.transition.TransitionSet;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearanceModel.CornerSizeUnaryOperator;

class TransitionUtils {

  static final int NO_DURATION = -1;

  // Constants corresponding to motionEasing* theme attr values.
  private static final String EASING_TYPE_CUBIC_BEZIER = "cubic-bezier";
  private static final String EASING_TYPE_PATH = "path";
  private static final String EASING_TYPE_FORMAT_START = "(";
  private static final String EASING_TYPE_FORMAT_END = ")";

  // Constants corresponding to motionPath theme attr enum values.
  private static final int PATH_TYPE_LINEAR = 0;
  private static final int PATH_TYPE_ARC = 1;

  private TransitionUtils() {}

  static boolean applyThemeInterpolator(
      Transition transition,
      Context context,
      @AttrRes int attrResId,
      TimeInterpolator defaultInterpolator) {
    if (transition.getInterpolator() == null) {
      TimeInterpolator interpolator =
          TransitionUtils.resolveThemeInterpolator(context, attrResId, defaultInterpolator);
      transition.setInterpolator(interpolator);
      return true;
    }
    return false;
  }

  static boolean applyThemeDuration(
      Transition transition, Context context, @AttrRes int attrResId) {
    if (transition.getDuration() == NO_DURATION) {
      int duration = MaterialAttributes.resolveInteger(context, attrResId, NO_DURATION);
      if (duration != NO_DURATION) {
        transition.setDuration(duration);
        return true;
      }
    }
    return false;
  }

  static boolean applyThemePath(Transition transition, Context context, @AttrRes int attrResId) {
    PathMotion pathMotion = resolveThemePath(context, attrResId);
    if (pathMotion != null) {
      transition.setPathMotion(pathMotion);
      return true;
    }
    return false;
  }

  static TimeInterpolator resolveThemeInterpolator(
      Context context, @AttrRes int attrResId, @NonNull TimeInterpolator defaultInterpolator) {
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

  @Nullable
  static PathMotion resolveThemePath(Context context, @AttrRes int attrResId) {
    TypedValue pathValue = new TypedValue();
    if (context.getTheme().resolveAttribute(attrResId, pathValue, true)) {
      if (pathValue.type == TypedValue.TYPE_INT_DEC) {
        int pathInt = pathValue.data;
        if (pathInt == PATH_TYPE_LINEAR) {
          // Default Transition PathMotion is linear; no need to override with different PathMotion.
          return null;
        } else if (pathInt == PATH_TYPE_ARC) {
          return new MaterialArcMotion();
        } else {
          throw new IllegalArgumentException("Invalid motion path type: " + pathInt);
        }
      } else if (pathValue.type == TypedValue.TYPE_STRING) {
        String pathString = String.valueOf(pathValue.string);
        return new PatternPathMotion(PathParser.createPathFromPathData(pathString));
      } else {
        throw new IllegalArgumentException(
            "Motion path theme attribute must either be an enum value or path data string");
      }
    }
    return null;
  }

  static ShapeAppearanceModel convertToRelativeCornerSizes(
      ShapeAppearanceModel shapeAppearanceModel, final RectF bounds) {
    return shapeAppearanceModel.withTransformedCornerSizes(
        new CornerSizeUnaryOperator() {
          @NonNull
          @Override
          public CornerSize apply(@NonNull CornerSize cornerSize) {
            return cornerSize instanceof RelativeCornerSize
                ? cornerSize
                : new RelativeCornerSize(cornerSize.getCornerSize(bounds) / bounds.height());
          }
        });
  }

  // TODO: rethink how to interpolate more than just corner size
  static ShapeAppearanceModel transformCornerSizes(
      ShapeAppearanceModel shapeAppearanceModel1,
      ShapeAppearanceModel shapeAppearanceModel2,
      RectF shapeAppearanceModel1Bounds,
      CornerSizeBinaryOperator op) {

    // If all of shapeAppearanceModel's corner sizes are 0, consider the shape appearance
    // insignificant compared to shapeAppearanceModel2 and use shapeAppearanceModel2's
    // corner family instead.
    ShapeAppearanceModel shapeAppearanceModel =
        isShapeAppearanceSignificant(shapeAppearanceModel1, shapeAppearanceModel1Bounds)
            ? shapeAppearanceModel1
            : shapeAppearanceModel2;

    return shapeAppearanceModel.toBuilder()
        .setTopLeftCornerSize(
            op.apply(
                shapeAppearanceModel1.getTopLeftCornerSize(),
                shapeAppearanceModel2.getTopLeftCornerSize()))
        .setTopRightCornerSize(
            op.apply(
                shapeAppearanceModel1.getTopRightCornerSize(),
                shapeAppearanceModel2.getTopRightCornerSize()))
        .setBottomLeftCornerSize(
            op.apply(
                shapeAppearanceModel1.getBottomLeftCornerSize(),
                shapeAppearanceModel2.getBottomLeftCornerSize()))
        .setBottomRightCornerSize(
            op.apply(
                shapeAppearanceModel1.getBottomRightCornerSize(),
                shapeAppearanceModel2.getBottomRightCornerSize()))
        .build();
  }

  private static boolean isShapeAppearanceSignificant(
      ShapeAppearanceModel shapeAppearanceModel, RectF bounds) {
    return shapeAppearanceModel.getTopLeftCornerSize().getCornerSize(bounds) != 0
        || shapeAppearanceModel.getTopRightCornerSize().getCornerSize(bounds) != 0
        || shapeAppearanceModel.getBottomRightCornerSize().getCornerSize(bounds) != 0
        || shapeAppearanceModel.getBottomLeftCornerSize().getCornerSize(bounds) != 0;
  }

  interface CornerSizeBinaryOperator {
    @NonNull
    CornerSize apply(@NonNull CornerSize cornerSize1, @NonNull CornerSize cornerSize2);
  }

  static float lerp(float startValue, float endValue, float fraction) {
    return startValue + fraction * (endValue - startValue);
  }

  // TODO(b/169309512): Remove in favor of AnimationUtils implementation
  static float lerp(
      float startValue,
      float endValue,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @FloatRange(from = 0.0, to = 1.0) float fraction) {
    return lerp(
        startValue, endValue, startFraction, endFraction, fraction, /* allowOvershoot= */ false);
  }

  static float lerp(
      float startValue,
      float endValue,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @FloatRange(from = 0.0) float fraction,
      boolean allowOvershoot) {
    if (allowOvershoot && (fraction < 0 || fraction > 1)) {
      return lerp(startValue, endValue, fraction);
    }
    if (fraction < startFraction) {
      return startValue;
    }
    if (fraction > endFraction) {
      return endValue;
    }

    return lerp(startValue, endValue, (fraction - startFraction) / (endFraction - startFraction));
  }

  static int lerp(
      int startValue,
      int endValue,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @FloatRange(from = 0.0, to = 1.0) float fraction) {
    if (fraction < startFraction) {
      return startValue;
    }
    if (fraction > endFraction) {
      return endValue;
    }
    return (int)
        lerp(startValue, endValue, (fraction - startFraction) / (endFraction - startFraction));
  }

  static ShapeAppearanceModel lerp(
      ShapeAppearanceModel startValue,
      ShapeAppearanceModel endValue,
      final RectF startBounds,
      final RectF endBounds,
      final @FloatRange(from = 0.0, to = 1.0) float startFraction,
      final @FloatRange(from = 0.0, to = 1.0) float endFraction,
      final @FloatRange(from = 0.0, to = 1.0) float fraction) {
    if (fraction < startFraction) {
      return startValue;
    }
    if (fraction > endFraction) {
      return endValue;
    }

    return transformCornerSizes(
        startValue,
        endValue,
        startBounds,
        new CornerSizeBinaryOperator() {
          @NonNull
          @Override
          public CornerSize apply(
              @NonNull CornerSize cornerSize1, @NonNull CornerSize cornerSize2) {
            float startCornerSize = cornerSize1.getCornerSize(startBounds);
            float endCornerSize = cornerSize2.getCornerSize(endBounds);
            float cornerSize =
                lerp(startCornerSize, endCornerSize, startFraction, endFraction, fraction);

            return new AbsoluteCornerSize(cornerSize);
          }
        });
  }

  static Shader createColorShader(@ColorInt int color) {
    return new LinearGradient(0, 0, 0, 0, color, color, Shader.TileMode.CLAMP);
  }

  static View findDescendantOrAncestorById(View view, @IdRes int viewId) {
    View descendant = view.findViewById(viewId);
    if (descendant != null) {
      return descendant;
    }
    return findAncestorById(view, viewId);
  }

  static View findAncestorById(View view, @IdRes int ancestorId) {
    String resourceName = view.getResources().getResourceName(ancestorId);
    while (view != null) {
      if (view.getId() == ancestorId) {
        return view;
      }
      ViewParent parent = view.getParent();
      if (parent instanceof View) {
        view = (View) parent;
      } else {
        break;
      }
    }
    throw new IllegalArgumentException(resourceName + " is not a valid ancestor");
  }

  static RectF getRelativeBounds(View view) {
    return new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
  }

  static Rect getRelativeBoundsRect(View view) {
    return new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
  }

  static RectF getLocationOnScreen(View view) {
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    int left = location[0];
    int top = location[1];
    int right = left + view.getWidth();
    int bottom = top + view.getHeight();
    return new RectF(left, top, right, bottom);
  }

  @NonNull
  static <T> T defaultIfNull(@Nullable T value, @NonNull T defaultValue) {
    return value != null ? value : defaultValue;
  }

  static float calculateArea(@NonNull RectF bounds) {
    return bounds.width() * bounds.height();
  }

  private static final RectF transformAlphaRectF = new RectF();

  private static int saveLayerAlphaCompat(Canvas canvas, Rect bounds, int alpha) {
    transformAlphaRectF.set(bounds);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      return canvas.saveLayerAlpha(transformAlphaRectF, alpha);
    } else {
      return canvas.saveLayerAlpha(
          transformAlphaRectF.left,
          transformAlphaRectF.top,
          transformAlphaRectF.right,
          transformAlphaRectF.bottom,
          alpha,
          Canvas.ALL_SAVE_FLAG);
    }
  }

  /**
   * Helper method to translate, scale and set an alpha layer on a canvas, run any operations on the
   * transformed canvas and finally, restore the Canvas to it's original state.
   */
  static void transform(
      Canvas canvas, Rect bounds, float dx, float dy, float scale, int alpha, CanvasOperation op) {
    // Exit early and avoid drawing if what will be drawn is completely transparent.
    if (alpha <= 0) {
      return;
    }

    int checkpoint = canvas.save();
    canvas.translate(dx, dy);
    canvas.scale(scale, scale);
    if (alpha < 255) {
      saveLayerAlphaCompat(canvas, bounds, alpha);
    }
    op.run(canvas);
    canvas.restoreToCount(checkpoint);
  }

  interface CanvasOperation {
    void run(Canvas canvas);
  }

  static void maybeAddTransition(TransitionSet transitionSet, @Nullable Transition transition) {
    if (transition != null) {
      transitionSet.addTransition(transition);
    }
  }

  static void maybeRemoveTransition(TransitionSet transitionSet, @Nullable Transition transition) {
    if (transition != null) {
      transitionSet.removeTransition(transition);
    }
  }
}
