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

package com.google.android.material.shape;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.google.android.material.shadow.ShadowRenderer;
import com.google.android.material.shape.ShapeAppearancePathProvider.PathListener;
import com.google.android.material.shape.ShapePath.ShadowCompatOperation;
import androidx.core.graphics.drawable.TintAwareDrawable;
import android.util.AttributeSet;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Base drawable class for Material Shapes that handles shadows, elevation, scale and color for a
 * generated path.
 */
public class MaterialShapeDrawable extends Drawable implements TintAwareDrawable {

  private static final float SHADOW_RADIUS_MULTIPLIER = .75f;

  private static final float SHADOW_OFFSET_MULTIPLIER = .25f;

  /**
   * Try to draw native elevation shadows if possible, otherwise use fake shadows. This is best for
   * paths which will always be convex. If the path might change to be concave, you should consider
   * using {@link #SHADOW_COMPAT_MODE_ALWAYS} otherwise the shadows could suddenly switch from
   * native to fake in the middle of an animation.
   */
  public static final int SHADOW_COMPAT_MODE_DEFAULT = 0;

  /**
   * Never draw fake shadows. You may want to enable this if backwards compatibility for shadows
   * isn't as important as performance. Native shadow elevation shadows will still be drawn if
   * possible.
   */
  public static final int SHADOW_COMPAT_MODE_NEVER = 1;

  /**
   * Always draw fake shadows, never draw native elevation shadows. If a path could be concave, this
   * will prevent the shadow from suddenly being rendered natively.
   */
  public static final int SHADOW_COMPAT_MODE_ALWAYS = 2;

  /** Determines when compatibility shadow is drawn vs. native elevation shadows. */
  @IntDef({SHADOW_COMPAT_MODE_DEFAULT, SHADOW_COMPAT_MODE_NEVER, SHADOW_COMPAT_MODE_ALWAYS})
  @Retention(RetentionPolicy.SOURCE)
  public @interface CompatibilityShadowMode {}

  private static final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private MaterialShapeDrawableState drawableState;

  // Inter-method state.
  private final ShadowCompatOperation[] cornerShadowOperation = new ShadowCompatOperation[4];
  private final ShadowCompatOperation[] edgeShadowOperation = new ShadowCompatOperation[4];
  private boolean pathDirty;

  // Pre-allocated objects that are re-used several times during path computation and rendering.
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();
  private final Path pathInsetByStroke = new Path();
  private final RectF rectF = new RectF();
  private final RectF insetRectF = new RectF();
  private final Region transparentRegion = new Region();
  private final Region scratchRegion = new Region();
  private ShapeAppearanceModel strokeShapeAppearance;

  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final ShadowRenderer shadowRenderer = new ShadowRenderer();
  private final PathListener pathShadowListener;
  private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();

  @Nullable private PorterDuffColorFilter tintFilter;
  @Nullable private PorterDuffColorFilter strokeTintFilter;

  public MaterialShapeDrawable() {
    this(new ShapeAppearanceModel());
  }

  public MaterialShapeDrawable(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    this(new ShapeAppearanceModel(context, attrs, defStyleAttr, defStyleRes));
  }

  /**
   * @param shapeAppearanceModel the {@link ShapeAppearanceModel} containing the path that will be
   *     rendered in this drawable.
   */
  public MaterialShapeDrawable(ShapeAppearanceModel shapeAppearanceModel) {
    this(new MaterialShapeDrawableState(shapeAppearanceModel));
  }

  private MaterialShapeDrawable(MaterialShapeDrawableState drawableState) {
    this.drawableState = drawableState;
    strokePaint.setStyle(Style.STROKE);
    fillPaint.setStyle(Style.FILL);
    clearPaint.setColor(Color.WHITE);
    clearPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
    updateTintFilter();
    updateColorsForState(getState(), false);
    // Listens to additions of corners and edges, to create the shadow operations.
    pathShadowListener =
        new PathListener() {
          @Override
          public void onCornerPathCreated(ShapePath cornerPath, Matrix transform, int count) {
            cornerShadowOperation[count] = cornerPath.createShadowCompatOperation(transform);
          }

          @Override
          public void onEdgePathCreated(ShapePath edgePath, Matrix transform, int count) {
            edgeShadowOperation[count] = edgePath.createShadowCompatOperation(transform);
          }
        };
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return drawableState;
  }

  @NonNull
  @Override
  public Drawable mutate() {
    MaterialShapeDrawableState newDrawableState = new MaterialShapeDrawableState(drawableState);
    drawableState = newDrawableState;
    return this;
  }

  private static int modulateAlpha(int paintAlpha, int alpha) {
    int scale = alpha + (alpha >>> 7); // convert to 0..256
    return (paintAlpha * scale) >>> 8;
  }

  /**
   * Set the {@link ShapeAppearanceModel} containing the path that will be rendered in this
   * drawable.
   *
   * @param shapeAppearanceModel the desired model.
   */
  public void setShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel) {
    drawableState.shapeAppearanceModel = shapeAppearanceModel;
    invalidateSelf();
  }

  /**
   * Get the {@link ShapeAppearanceModel} containing the path that will be rendered in this
   * drawable.
   *
   * @return the current model.
   */
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return drawableState.shapeAppearanceModel;
  }

  /**
   * Set the {@link ShapePathModel} containing the path that will be rendered in this drawable.
   *
   * @deprecated Use {@link #setShapeAppearanceModel(ShapeAppearanceModel)} instead.
   * @param shapedViewModel the desired model.
   */
  @Deprecated
  public void setShapedViewModel(ShapePathModel shapedViewModel) {
    setShapeAppearanceModel(shapedViewModel);
  }

  /**
   * Get the {@link ShapePathModel} containing the path that will be rendered in this drawable.
   *
   * @deprecated Use {@link #getShapeAppearanceModel()} instead.
   * @return the current model.
   */
  @Deprecated
  public ShapeAppearanceModel getShapedViewModel() {
    return getShapeAppearanceModel();
  }

  /**
   * Set the color used for the fill.
   *
   * @param fillColor the color set on the {@link Paint} object responsible for the fill.
   */
  public void setFillColor(@Nullable ColorStateList fillColor) {
    if (drawableState.fillColor != fillColor) {
      drawableState.fillColor = fillColor;
      onStateChange(getState());
    }
  }

  /**
   * Get the color used for the fill.
   *
   * @return the color set on the {@link Paint} object responsible for the fill.
   */
  @Nullable
  public ColorStateList getFillColor() {
    return drawableState.fillColor;
  }

  /**
   * Set the color used for the stroke.
   *
   * @param strokeColor the color set on the {@link Paint} object responsible for the stroke.
   */
  public void setStrokeColor(@Nullable ColorStateList strokeColor) {
    if (drawableState.strokeColor != strokeColor) {
      drawableState.strokeColor = strokeColor;
      onStateChange(getState());
    }
  }

  /**
   * Get the color used for the stroke.
   *
   * @return the color set on the {@link Paint} object responsible for the stroke.
   */
  @Nullable
  public ColorStateList getStrokeColor() {
    return drawableState.strokeColor;
  }

  @Override
  public void setTintMode(PorterDuff.Mode tintMode) {
    if (drawableState.tintMode != tintMode) {
      drawableState.tintMode = tintMode;
      updateTintFilter();
      invalidateSelfIgnoreShape();
    }
  }

  @Override
  public void setTintList(ColorStateList tintList) {
    drawableState.tintList = tintList;
    updateTintFilter();
    invalidateSelfIgnoreShape();
  }

  /** Get the tint list used by the shape's paint. */
  public ColorStateList getTintList() {
    return drawableState.tintList;
  }

  /**
   * Get the stroke's current {@link ColorStateList}.
   *
   * @return the stroke's current {@link ColorStateList}.
   */
  public ColorStateList getStrokeTintList() {
    return drawableState.strokeTintList;
  }

  /**
   * Get the int representing the Color of the shape's stroke in the current state.
   *
   * @deprecated Use {@link #getStrokeTintList()} instead.
   * @return the stroke's current color.
   */
  @Deprecated
  @ColorInt
  public int getStrokeTint() {
    return drawableState.strokeTintList.getColorForState(getState(), Color.TRANSPARENT);
  }

  @Override
  public void setTint(@ColorInt int tintColor) {
    setTintList(ColorStateList.valueOf(tintColor));
  }

  /**
   * Set the shape's stroke {@link ColorStateList}
   *
   * @param tintList the {@link ColorStateList} for the shape's stroke.
   */
  public void setStrokeTint(ColorStateList tintList) {
    drawableState.strokeTintList = tintList;
    updateTintFilter();
    invalidateSelfIgnoreShape();
  }

  /**
   * Set the shape's stroke color.
   *
   * @param tintColor an int representing the Color to use for the shape's stroke.
   */
  public void setStrokeTint(@ColorInt int tintColor) {
    setStrokeTint(ColorStateList.valueOf(tintColor));
  }

  /**
   * Set the shape's stroke width and stroke color.
   *
   * @param strokeWidth a float for the width of the stroke.
   * @param strokeColor an int representing the Color to use for the shape's stroke.
   */
  public void setStroke(float strokeWidth, @ColorInt int strokeColor) {
    setStrokeWidth(strokeWidth);
    setStrokeColor(ColorStateList.valueOf(strokeColor));
  }

  /**
   * Set the shape's stroke width and stroke color using a {@link ColorStateList}.
   *
   * @param strokeWidth a float for the width of the stroke.
   * @param strokeColor the {@link ColorStateList} for the shape's stroke.
   */
  public void setStroke(float strokeWidth, @Nullable ColorStateList strokeColor) {
    setStrokeWidth(strokeWidth);
    setStrokeColor(strokeColor);
  }

  /**
   * Get the stroke width used by the shape's paint.
   *
   * @return the stroke's current width.
   */
  public float getStrokeWidth() {
    return drawableState.strokeWidth;
  }

  /**
   * Set the stroke width used by the shape's paint.
   *
   * @param strokeWidth desired stroke width.
   */
  public void setStrokeWidth(float strokeWidth) {
    drawableState.strokeWidth = strokeWidth;
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    // OPAQUE or TRANSPARENT are possible, but the complexity of determining this based on the
    // shape model outweighs the optimizations gained.
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    if (drawableState.alpha != alpha) {
      drawableState.alpha = alpha;
      invalidateSelfIgnoreShape();
    }
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    drawableState.colorFilter = colorFilter;
    invalidateSelfIgnoreShape();
  }

  @Override
  public Region getTransparentRegion() {
    Rect bounds = getBounds();
    transparentRegion.set(bounds);
    calculatePath(getBoundsAsRectF(), path);
    scratchRegion.setPath(path, transparentRegion);
    transparentRegion.op(scratchRegion, Op.DIFFERENCE);
    return transparentRegion;
  }

  protected RectF getBoundsAsRectF() {
    Rect bounds = getBounds();
    rectF.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
    return rectF;
  }

  public void setCornerRadius(float cornerRadius) {
    drawableState.shapeAppearanceModel.setCornerRadius(cornerRadius);
    invalidateSelf();
  }

  /**
   * Determines whether a point is contained within the transparent region of the Drawable. A return
   * value of true generally suggests that the touched view should not process a touch event at that
   * point.
   *
   * @param x The X coordinate of the point.
   * @param y The Y coordinate of the point.
   * @return true iff the point is contained in the transparent region of the Drawable.
   */
  public boolean isPointInTransparentRegion(int x, int y) {
    return getTransparentRegion().contains(x, y);
  }

  @CompatibilityShadowMode
  public int getShadowCompatibilityMode() {
    return drawableState.shadowCompatMode;
  }

  /**
   * Set the shadow compatibility mode. This allows control over when fake shadows should drawn
   * instead of native elevation shadows.
   *
   * <p>Note: To prevent clipping of fake shadow for views on API levels above lollipop, the parent
   * view must disable clipping of children by calling {@link
   * android.view.ViewGroup#setClipChildren(boolean)}, or by setting `android:clipChildren="false"`
   * in xml. `clipToPadding` may also need to be false if there is any padding on the parent that
   * could intersect the shadow.
   */
  public void setShadowCompatibilityMode(@CompatibilityShadowMode int mode) {
    if (drawableState.shadowCompatMode != mode) {
      drawableState.shadowCompatMode = mode;
      invalidateSelfIgnoreShape();
    }
  }

  /**
   * Get shadow rendering status for shadows when {@link #requiresCompatShadow()} is true.
   *
   * @return true if fake shadows should be drawn, false otherwise.
   * @deprecated use {@link #getShadowCompatibilityMode()} instead
   */
  @Deprecated
  public boolean isShadowEnabled() {
    return drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_DEFAULT
        || drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS;
  }

  /**
   * Set shadow rendering to be enabled or disabled when {@link #requiresCompatShadow()} is true.
   * Setting this to false could provide some performance benefits on older devices if you don't
   * mind no shadows being drawn.
   *
   * <p>Note: native elevation shadows will still be drawn on API 21 and up if the shape is convex
   * and the view with this background has elevation.
   *
   * @param shadowEnabled true if fake shadows should be drawn; false if not.
   * @deprecated use {@link #setShadowCompatibilityMode(int)} instead.
   */
  @Deprecated
  public void setShadowEnabled(boolean shadowEnabled) {
    setShadowCompatibilityMode(
        shadowEnabled ? SHADOW_COMPAT_MODE_DEFAULT : SHADOW_COMPAT_MODE_NEVER);
  }

  /**
   * Get the interpolation of the path, between 0 and 1. Ranges between 0 (none) and 1 (fully)
   * interpolated.
   *
   * @return the interpolation of the path.
   */
  public float getInterpolation() {
    return drawableState.interpolation;
  }

  /**
   * Set the interpolation of the path, between 0 and 1. Ranges between 0 (none) and 1 (fully)
   * interpolated. An interpolation of 1 generally indicates a fully rendered path, while an
   * interpolation of 0 generally indicates a fully healed path, which is usually a rectangle.
   *
   * @param interpolation the desired interpolation.
   */
  public void setInterpolation(float interpolation) {
    if (drawableState.interpolation != interpolation) {
      drawableState.interpolation = interpolation;
      invalidateSelf();
    }
  }

  /**
   * Returns the elevation used to render fake shadows when {@link #requiresCompatShadow()} is true.
   * This value is the same as the native elevation that would be used to render shadows on API 21
   * and up.
   */
  public float getElevation() {
    return drawableState.elevation;
  }

  /**
   * Sets the elevation used to render shadows when {@link #requiresCompatShadow()} is true. This
   * value is the same as the native elevation that would be used to render shadows on API 21 and
   * up.
   */
  public void setElevation(float elevation) {
    if (drawableState.elevation != elevation) {
      drawableState.shadowCompatRadius = (int) Math.ceil(elevation * SHADOW_RADIUS_MULTIPLIER);
      drawableState.shadowCompatOffset = (int) Math.ceil(elevation * SHADOW_OFFSET_MULTIPLIER);
      drawableState.elevation = elevation;
      invalidateSelfIgnoreShape();
    }
  }

  /**
   * Get the shadow elevation rendered by the path.
   *
   * @deprecated use {@link #getElevation()} instead.
   */
  @Deprecated
  public int getShadowElevation() {
    return (int) getElevation();
  }

  /**
   * Set the shadow elevation rendered by the path.
   *
   * @param shadowElevation the desired elevation.
   * @deprecated use {@link #setElevation(float)} instead.
   */
  @Deprecated
  public void setShadowElevation(int shadowElevation) {
    setElevation(shadowElevation);
  }

  /**
   * Returns the rotation offset applied to the fake shadow which is drawn when {@link
   * #requiresCompatShadow()} is true.
   */
  public int getShadowCompatRotation() {
    return drawableState.shadowCompatRotation;
  }

  /**
   * Set the rotation offset applied to the fake shadow which is drawn when {@link
   * #requiresCompatShadow()} is true. 0 degrees will draw the shadow below the shape.
   *
   * <p>This allows for the Drawable to be wrapped in a {@link
   * android.graphics.drawable.RotateDrawable}, or rotated in a view while still having the fake
   * shadow to appear to be drawn from the bottom.
   */
  public void setShadowCompatRotation(int shadowRotation) {
    if (drawableState.shadowCompatRotation != shadowRotation) {
      drawableState.shadowCompatRotation = shadowRotation;
      invalidateSelfIgnoreShape();
    }
  }

  /**
   * Get the shadow radius rendered by the path.
   *
   * @return the shadow radius rendered by the path.
   * @deprecated use {@link #getElevation()} instead.
   */
  @Deprecated
  public int getShadowRadius() {
    return drawableState.shadowCompatRadius;
  }

  /**
   * Set the shadow radius rendered by the path.
   *
   * @param shadowRadius the desired shadow radius.
   * @deprecated use {@link #setElevation(float)} instead.
   */
  @Deprecated
  public void setShadowRadius(int shadowRadius) {
    drawableState.shadowCompatRadius = shadowRadius;
  }

  /**
   * Returns true if fake shadows should be drawn. Native elevation shadows can't be drawn on API <
   * 21 or when the shape is concave.
   */
  private boolean requiresCompatShadow() {
    return VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
        || (!drawableState.shapeAppearanceModel.isRoundRect() && !path.isConvex());
  }

  /**
   * Get the scale of the rendered path. A value of 1 renders it at 100% size.
   *
   * @return the scale of the path.
   */
  public float getScale() {
    return drawableState.scale;
  }

  /**
   * Set the scale of the rendered path. A value of 1 renders it at 100% size.
   *
   * @param scale the desired scale.
   */
  public void setScale(float scale) {
    if (drawableState.scale != scale) {
      drawableState.scale = scale;
      invalidateSelf();
    }
  }

  @Override
  public void invalidateSelf() {
    pathDirty = true;
    invalidateSelfIgnoreShape();
  }

  /**
   * Invalidate without recalculating the path associated with this shape. This is useful if the
   * shape has stayed the same but we still need to be redrawn, such as when the color has changed.
   */
  private void invalidateSelfIgnoreShape() {
    super.invalidateSelf();
  }

  /**
   * Set whether fake shadow color should match next set tint color. This will only be drawn when
   * {@link #requiresCompatShadow()} is true, otherwise native elevation shadows will be drawn which
   * don't support colored shadows.
   *
   * @param useTintColorForShadow true if color should match; false otherwise.
   */
  public void setUseTintColorForShadow(boolean useTintColorForShadow) {
    if (drawableState.useTintColorForShadow != useTintColorForShadow) {
      drawableState.useTintColorForShadow = useTintColorForShadow;
      invalidateSelf();
    }
  }

  /**
   * Set the color of fake shadow rendered behind the shape. This will only be drawn when {@link
   * #requiresCompatShadow()} is true, otherwise native elevation shadows will be drawn which don't
   * support colored shadows.
   *
   * <p>Setting a shadow color will prevent the tint color from being used.
   *
   * @param shadowColor desired color.
   */
  public void setShadowColor(int shadowColor) {
    shadowRenderer.setShadowColor(shadowColor);
    drawableState.useTintColorForShadow = false;
    invalidateSelfIgnoreShape();
  }

  /**
   * Get the current style used by the shape's paint.
   *
   * @return current used paint style.
   */
  public Style getPaintStyle() {
    return drawableState.paintStyle;
  }

  /**
   * Set the style used by the shape's paint.
   *
   * @param paintStyle the desired style.
   */
  public void setPaintStyle(Style paintStyle) {
    drawableState.paintStyle = paintStyle;
    invalidateSelfIgnoreShape();
  }

  /** Returns whether the shape should draw the compatibility shadow. */
  private boolean hasCompatShadow() {
    return drawableState.shadowCompatMode != SHADOW_COMPAT_MODE_NEVER
        && drawableState.shadowCompatRadius > 0
        && (drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS || requiresCompatShadow());
  }

  /** Returns whether the shape has a fill. */
  private boolean hasFill() {
    return drawableState.paintStyle == Style.FILL_AND_STROKE
        || drawableState.paintStyle == Style.FILL;
  }

  /** Returns whether the shape has a stroke with a positive width. */
  private boolean hasStroke() {
    return (drawableState.paintStyle == Style.FILL_AND_STROKE
            || drawableState.paintStyle == Style.STROKE)
        && strokePaint.getStrokeWidth() > 0;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    pathDirty = true;
    super.onBoundsChange(bounds);
  }

  @Override
  public void draw(Canvas canvas) {
    fillPaint.setColorFilter(tintFilter);
    final int prevAlpha = fillPaint.getAlpha();
    fillPaint.setAlpha(modulateAlpha(prevAlpha, drawableState.alpha));

    strokePaint.setColorFilter(strokeTintFilter);
    strokePaint.setStrokeWidth(drawableState.strokeWidth);

    final int prevStrokeAlpha = strokePaint.getAlpha();
    strokePaint.setAlpha(modulateAlpha(prevStrokeAlpha, drawableState.alpha));

    if (pathDirty) {
      calculateStrokePath();
      calculatePath(getBoundsAsRectF(), path);
      pathDirty = false;
    }

    if (hasCompatShadow()) {
      // Save the canvas before changing the clip bounds.
      canvas.save();

      prepareCanvasForShadow(canvas);

      // Drawing the shadow in a bitmap lets us use the clear paint rather than using clipPath to
      // prevent drawing shadow under the shape. clipPath has problems :-/
      Bitmap shadowLayer =
          Bitmap.createBitmap(
              getBounds().width() + drawableState.shadowCompatRadius * 2,
              getBounds().height() + drawableState.shadowCompatRadius * 2,
              Bitmap.Config.ARGB_8888);
      Canvas shadowCanvas = new Canvas(shadowLayer);

      // Top Left of shadow (left - shadowCompatRadius, top - shadowCompatRadius) should be drawn at
      // (0, 0) on shadowCanvas. Offset is handled by prepareCanvasForShadow and drawCompatShadow.
      float shadowLeft = getBounds().left - drawableState.shadowCompatRadius;
      float shadowTop = getBounds().top - drawableState.shadowCompatRadius;
      shadowCanvas.translate(-shadowLeft, -shadowTop);

      drawCompatShadow(shadowCanvas);

      canvas.drawBitmap(shadowLayer, shadowLeft, shadowTop, null);

      // Because we create the bitmap every time, we can recycle it. We may need to stop doing this
      // if we end up keeping the bitmap in memory for performance.
      shadowLayer.recycle();

      // Restore the canvas to the same size it was before drawing any shadows.
      canvas.restore();
    }

    if (hasFill()) {
      drawFillShape(canvas);
    }
    if (hasStroke()) {
      drawStrokeShape(canvas);
    }

    fillPaint.setAlpha(prevAlpha);
    strokePaint.setAlpha(prevStrokeAlpha);
  }

  /**
   * Draw the path or try to draw a round rect if possible.
   *
   * <p>This method is a protected version of the private method used internally. It is made
   * available to allow subclasses within the library to draw the shape directly.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  protected void drawShape(Canvas canvas, Paint paint, Path path, RectF bounds) {
    drawShape(canvas, paint, path, drawableState.shapeAppearanceModel, bounds);
  }

  /** Draw the path or try to draw a round rect if possible. */
  private void drawShape(
      Canvas canvas,
      Paint paint,
      Path path,
      ShapeAppearanceModel shapeAppearanceModel,
      RectF bounds) {
    if (shapeAppearanceModel.isRoundRect()) {
      float cornerSize = shapeAppearanceModel.getTopRightCorner().getCornerSize();
      canvas.drawRoundRect(bounds, cornerSize, cornerSize, paint);
    } else {
      canvas.drawPath(path, paint);
    }
  }

  private void drawFillShape(Canvas canvas) {
    drawShape(canvas, fillPaint, path, drawableState.shapeAppearanceModel, getBoundsAsRectF());
  }

  private void drawStrokeShape(Canvas canvas) {
    drawShape(
        canvas, strokePaint, pathInsetByStroke, strokeShapeAppearance, getBoundsInsetByStroke());
  }

  private void prepareCanvasForShadow(Canvas canvas) {
    // Calculate the translation to offset the canvas for the given offset and rotation.
    int shadowOffsetX =
        (int)
            (drawableState.shadowCompatOffset
                * Math.sin(Math.toRadians(drawableState.shadowCompatRotation)));
    int shadowOffsetY =
        (int)
            (drawableState.shadowCompatOffset
                * Math.cos(Math.toRadians(drawableState.shadowCompatRotation)));

    // We only handle clipping as a convenience for older apis where we are trying to seamlessly
    // provide fake shadows. On newer versions of android, we require that the parent is set so that
    // clipChildren is false.
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      // Add space and offset the canvas for the shadows. Otherwise any shadows drawn outside would
      // be clipped and not visible.
      Rect canvasClipBounds = canvas.getClipBounds();
      canvasClipBounds.inset(-drawableState.shadowCompatRadius, -drawableState.shadowCompatRadius);
      canvasClipBounds.offset(shadowOffsetX, shadowOffsetY);
      canvas.clipRect(canvasClipBounds, Region.Op.REPLACE);
    }

    // Translate the canvas by an amount specified by the shadowCompatOffset. This will make the
    // shadow appear at and angle from the shape.
    canvas.translate(shadowOffsetX, shadowOffsetY);
  }

  /**
   * Draws a shadow using gradients which can be used in the cases where native elevation can't.
   * This draws the shadow in multiple parts. It draws the shadow for each corner and edge
   * separately. Then it fills in the center space with the main shadow colored paint. If there is
   * no shadow offset, this will skip the drawing of the center filled shadow since that will be
   * completely covered by the shape.
   */
  private void drawCompatShadow(Canvas canvas) {
    if (drawableState.shadowCompatOffset != 0) {
      canvas.drawPath(path, shadowRenderer.getShadowPaint());
    }

    // Draw the fake shadow for each of the corners and edges.
    for (int index = 0; index < 4; index++) {
      cornerShadowOperation[index].draw(shadowRenderer, drawableState.shadowCompatRadius, canvas);
      edgeShadowOperation[index].draw(shadowRenderer, drawableState.shadowCompatRadius, canvas);
    }

    int shadowOffsetX =
        (int)
            (drawableState.shadowCompatOffset
                * Math.sin(Math.toRadians(drawableState.shadowCompatRotation)));
    int shadowOffsetY =
        (int)
            (drawableState.shadowCompatOffset
                * Math.cos(Math.toRadians(drawableState.shadowCompatRotation)));

    canvas.translate(-shadowOffsetX, -shadowOffsetY);
    canvas.drawPath(path, clearPaint);
    canvas.translate(shadowOffsetX, shadowOffsetY);
  }

  /** @deprecated see {@link ShapeAppearancePathProvider} */
  @Deprecated
  public void getPathForSize(int width, int height, Path path) {
    calculatePathForSize(new RectF(0, 0, width, height), path);
  }

  /** @deprecated see {@link ShapeAppearancePathProvider} */
  @Deprecated
  public void getPathForSize(Rect bounds, Path path) {
    calculatePathForSize(new RectF(bounds), path);
  }

  private void calculatePathForSize(RectF bounds, Path path) {
    pathProvider.calculatePath(
        drawableState.shapeAppearanceModel,
        drawableState.interpolation,
        bounds,
        pathShadowListener,
        path);
  }

  /** Calculates the path that can be used to draw the stroke entirely inside the shape */
  private void calculateStrokePath() {
    strokeShapeAppearance = new ShapeAppearanceModel(getShapeAppearanceModel());
    float cornerSizeTopLeft = strokeShapeAppearance.getTopLeftCorner().cornerSize;
    float cornerSizeTopRight = strokeShapeAppearance.getTopRightCorner().cornerSize;
    float cornerSizeBottomRight = strokeShapeAppearance.getBottomRightCorner().cornerSize;
    float cornerSizeBottomLeft = strokeShapeAppearance.getBottomLeftCorner().cornerSize;

    // Adjust corner radius in order to draw the stroke so that the corners of the background are
    // drawn on top of the edges.
    strokeShapeAppearance.setCornerRadii(
        adjustCornerSizeForStrokeSize(cornerSizeTopLeft),
        adjustCornerSizeForStrokeSize(cornerSizeTopRight),
        adjustCornerSizeForStrokeSize(cornerSizeBottomRight),
        adjustCornerSizeForStrokeSize(cornerSizeBottomLeft));

    pathProvider.calculatePath(
        strokeShapeAppearance,
        drawableState.interpolation,
        getBoundsInsetByStroke(),
        pathInsetByStroke);
  }

  private float adjustCornerSizeForStrokeSize(float cornerSize) {
    float adjustedCornerSize = cornerSize - getStrokeInsetLength();
    return Math.max(adjustedCornerSize, 0);
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void getOutline(Outline outline) {
    if (drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS) {
      // Don't draw the native shadow if we're always rendering with compat shadow.
      return;
    }

    boolean isRoundRect = drawableState.shapeAppearanceModel.isRoundRect();

    if (isRoundRect) {
      float radius = drawableState.shapeAppearanceModel.getTopLeftCorner().getCornerSize();
      outline.setRoundRect(getBounds(), radius);
      return;
    }

    calculatePath(getBoundsAsRectF(), path);
    if (path.isConvex()) {
      outline.setConvexPath(path);
    }
  }

  private void calculatePath(RectF bounds, Path path) {
    calculatePathForSize(bounds, path);
    if (drawableState.scale == 1f) {
      return;
    }
    matrix.reset();
    matrix.setScale(
        drawableState.scale, drawableState.scale, bounds.width() / 2.0f, bounds.height() / 2.0f);
    path.transform(matrix);
  }

  private void updateTintFilter() {
    tintFilter = calculateTintFilter(drawableState.tintList, drawableState.tintMode);
    strokeTintFilter = calculateTintFilter(drawableState.strokeTintList, drawableState.tintMode);
    if (drawableState.useTintColorForShadow) {
      shadowRenderer.setShadowColor(
          drawableState.tintList.getColorForState(getState(), Color.TRANSPARENT));
    }
  }

  @Nullable
  private PorterDuffColorFilter calculateTintFilter(
      ColorStateList tintList, PorterDuff.Mode tintMode) {
    if (tintList == null || tintMode == null) {
      return null;
    }
    return new PorterDuffColorFilter(
        tintList.getColorForState(getState(), Color.TRANSPARENT), tintMode);
  }

  @Override
  public boolean isStateful() {
    return super.isStateful()
        || (drawableState.tintList != null && drawableState.tintList.isStateful())
        || (drawableState.strokeTintList != null && drawableState.strokeTintList.isStateful())
        || (drawableState.strokeColor != null && drawableState.strokeColor.isStateful())
        || (drawableState.fillColor != null && drawableState.fillColor.isStateful());
  }

  @Override
  protected boolean onStateChange(int[] state) {
    boolean invalidateSelf = super.onStateChange(state);

    updateColorsForState(state, invalidateSelf);
    updateTintFilter();

    return invalidateSelf;
  }

  private boolean updateColorsForState(int[] state, boolean invalidateSelf) {
    if (drawableState.fillColor != null) {
      final int previousFillColor = fillPaint.getColor();
      final int newFillColor = drawableState.fillColor.getColorForState(state, previousFillColor);
      if (previousFillColor != newFillColor) {
        fillPaint.setColor(newFillColor);
        invalidateSelf = true;
      }
    }

    if (drawableState.strokeColor != null) {
      final int previousStrokeColor = strokePaint.getColor();
      final int newStrokeColor =
          drawableState.strokeColor.getColorForState(state, previousStrokeColor);
      if (previousStrokeColor != newStrokeColor) {
        strokePaint.setColor(newStrokeColor);
        invalidateSelf = true;
      }
    }

    return invalidateSelf;
  }

  private float getStrokeInsetLength() {
    if (hasStroke()) {
      return strokePaint.getStrokeWidth() / 2.0f;
    }
    return 0f;
  }

  private RectF getBoundsInsetByStroke() {
    RectF rectF = getBoundsAsRectF();
    float inset = getStrokeInsetLength();
    insetRectF.set(
        rectF.left + inset, rectF.top + inset, rectF.right - inset, rectF.bottom - inset);
    return insetRectF;
  }

  static final class MaterialShapeDrawableState extends ConstantState {

    @NonNull public ShapeAppearanceModel shapeAppearanceModel;

    public ColorFilter colorFilter;
    public ColorStateList fillColor = null;
    public ColorStateList strokeColor = null;
    public ColorStateList strokeTintList = null;
    public ColorStateList tintList = null;
    public PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;

    public float scale = 1f;
    public float interpolation = 1f;
    public float strokeWidth;

    public int alpha = 255;
    public float elevation = 0;
    public int shadowCompatMode = SHADOW_COMPAT_MODE_DEFAULT;
    public int shadowCompatRadius = 0;
    public int shadowCompatOffset = 0;
    public int shadowCompatRotation = 0;

    public boolean useTintColorForShadow = false;

    public Style paintStyle = Style.FILL_AND_STROKE;

    public MaterialShapeDrawableState(ShapeAppearanceModel shapeAppearanceModel) {
      this.shapeAppearanceModel = shapeAppearanceModel;
    }

    public MaterialShapeDrawableState(MaterialShapeDrawableState orig) {
      shapeAppearanceModel = new ShapeAppearanceModel(orig.shapeAppearanceModel);
      strokeWidth = orig.strokeWidth;
      colorFilter = orig.colorFilter;
      fillColor = orig.fillColor;
      strokeColor = orig.strokeColor;
      tintMode = orig.tintMode;
      tintList = orig.tintList;
      alpha = orig.alpha;
      scale = orig.scale;
      shadowCompatOffset = orig.shadowCompatOffset;
      shadowCompatMode = orig.shadowCompatMode;
      useTintColorForShadow = orig.useTintColorForShadow;
      interpolation = orig.interpolation;
      elevation = orig.elevation;
      shadowCompatRadius = orig.shadowCompatRadius;
      shadowCompatRotation = orig.shadowCompatRotation;
      strokeTintList = orig.strokeTintList;
      paintStyle = orig.paintStyle;
    }

    @Override
    public Drawable newDrawable() {
      return new MaterialShapeDrawable(this);
    }

    @Override
    public int getChangingConfigurations() {
      return 0;
    }
  }
}
