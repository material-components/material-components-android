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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;
import androidx.transition.PathMotion;
import androidx.transition.PatternPathMotion;
import androidx.transition.Transition;
import androidx.transition.TransitionSet;
import com.google.android.material.canvas.CanvasCompat.CanvasOperation;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;

class TransitionUtils {

  static final int NO_DURATION = -1;
  @AttrRes static final int NO_ATTR_RES_ID = 0;

  private static final int MAX_IMAGE_SIZE = 1024 * 1024;
  
  // Constants corresponding to motionPath theme attr enum values.
  private static final int PATH_TYPE_LINEAR = 0;
  private static final int PATH_TYPE_ARC = 1;

  private TransitionUtils() {}

  /**
     * Creates a View using the bitmap copy of <code>view</code>. If <code>view</code> is large,
     * the copy will use a scaled bitmap of the given view.
     *
     * @param sceneRoot The ViewGroup in which the view copy will be displayed.
     * @param view The view to create a copy of.
     * @param parent The parent of view.
     */
    public static View copyViewImage(ViewGroup sceneRoot, View view, View parent) {
        Matrix matrix = new Matrix();
        matrix.setTranslate(-parent.getScrollX(), -parent.getScrollY());
        view.transformMatrixToGlobal(matrix);
        sceneRoot.transformMatrixToLocal(matrix);
        RectF bounds = new RectF(0, 0, view.getWidth(), view.getHeight());
        matrix.mapRect(bounds);
        int left = Math.round(bounds.left);
        int top = Math.round(bounds.top);
        int right = Math.round(bounds.right);
        int bottom = Math.round(bounds.bottom);
        ImageView copy = new ImageView(view.getContext());
        copy.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Bitmap bitmap = createViewBitmap(view, matrix, bounds, sceneRoot);
        if (bitmap != null) {
            copy.setImageBitmap(bitmap);
        }
        int widthSpec = View.MeasureSpec.makeMeasureSpec(right - left, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(bottom - top, View.MeasureSpec.EXACTLY);
        copy.measure(widthSpec, heightSpec);
        copy.layout(left, top, right, bottom);
        return copy;
    }
    /**
     * Creates a Bitmap of the given view, using the Matrix matrix to transform to the local
     * coordinates. <code>matrix</code> will be modified during the bitmap creation.
     *
     * <p>If the bitmap is large, it will be scaled uniformly down to at most 1MB size.</p>
     * @param view The view to create a bitmap for.
     * @param matrix The matrix converting the view local coordinates to the coordinates that
     *               the bitmap will be displayed in. <code>matrix</code> will be modified before
     *               returning.
     * @param bounds The bounds of the bitmap in the destination coordinate system (where the
     *               view should be presented. Typically, this is matrix.mapRect(viewBounds);
     * @param sceneRoot A ViewGroup that is attached to the window to temporarily contain the view
     *                  if it isn't attached to the window.
     * @return A bitmap of the given view or null if bounds has no width or height.
     */
    public static Bitmap createViewBitmap(View view, Matrix matrix, RectF bounds,
            ViewGroup sceneRoot) {
        final boolean addToOverlay = !view.isAttachedToWindow();
        ViewGroup parent = null;
        int indexInParent = 0;
        if (addToOverlay) {
            if (sceneRoot == null || !sceneRoot.isAttachedToWindow()) {
                return null;
            }
            parent = (ViewGroup) view.getParent();
            indexInParent = parent.indexOfChild(view);
            sceneRoot.getOverlay().add(view);
        }
        Bitmap bitmap = null;
        int bitmapWidth = Math.round(bounds.width());
        int bitmapHeight = Math.round(bounds.height());
        if (bitmapWidth > 0 && bitmapHeight > 0) {
            float scale = Math.min(1f, ((float) MAX_IMAGE_SIZE) / (bitmapWidth * bitmapHeight));
            bitmapWidth *= scale;
            bitmapHeight *= scale;
            matrix.postTranslate(-bounds.left, -bounds.top);
            matrix.postScale(scale, scale);
            final Picture picture = new Picture();
            final Canvas canvas = picture.beginRecording(bitmapWidth, bitmapHeight);
            canvas.concat(matrix);
            view.draw(canvas);
            picture.endRecording();
            bitmap = Bitmap.createBitmap(picture);
        }
        if (addToOverlay) {
            sceneRoot.getOverlay().remove(view);
            parent.addView(view, indexInParent);
        }
        return bitmap;
    }
  
  static boolean maybeApplyThemeInterpolator(
      Transition transition,
      Context context,
      @AttrRes int attrResId,
      TimeInterpolator defaultInterpolator) {
    if (attrResId != NO_ATTR_RES_ID && transition.getInterpolator() == null) {
      TimeInterpolator interpolator =
          MotionUtils.resolveThemeInterpolator(context, attrResId, defaultInterpolator);
      transition.setInterpolator(interpolator);
      return true;
    }
    return false;
  }

  static boolean maybeApplyThemeDuration(
      Transition transition, Context context, @AttrRes int attrResId) {
    if (attrResId != NO_ATTR_RES_ID && transition.getDuration() == NO_DURATION) {
      int duration = MotionUtils.resolveThemeDuration(context, attrResId, NO_DURATION);
      if (duration != NO_DURATION) {
        transition.setDuration(duration);
        return true;
      }
    }
    return false;
  }

  static boolean maybeApplyThemePath(
      Transition transition, Context context, @AttrRes int attrResId) {
    if (attrResId != NO_ATTR_RES_ID) {
      PathMotion pathMotion = resolveThemePath(context, attrResId);
      if (pathMotion != null) {
        transition.setPathMotion(pathMotion);
        return true;
      }
    }
    return false;
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
        cornerSize -> RelativeCornerSize.createFromCornerSize(bounds, cornerSize));
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

  static RectF getLocationInWindow(View view) {
    int[] location = new int[2];
    view.getLocationInWindow(location);
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
    return canvas.saveLayerAlpha(transformAlphaRectF, alpha);
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
