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
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import com.google.android.material.internal.Experimental;
import com.google.android.material.shadow.ShadowRenderer;
import com.google.android.material.shape.ShapePath.ShadowCompatOperation;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.util.AttributeSet;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Base drawable class for Material Shapes that handles shadows, elevation, scale and color for a
 * generated path.
 */
@Experimental("The shapes API is currently experimental and subject to change")
public class MaterialShapeDrawable extends Drawable implements TintAwareDrawable {

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

  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private ColorStateList fillColor = null;
  private ColorStateList strokeColor = null;

  private final MaterialShapeDrawableState state = new MaterialShapeDrawableState();

  // Inter-method state.
  private final Matrix[] cornerTransforms = new Matrix[4];
  private final Matrix[] edgeTransforms = new Matrix[4];
  private final ShapePath[] cornerPaths = new ShapePath[4];
  private final ShadowCompatOperation[] cornerShadowOperation = new ShadowCompatOperation[4];
  private final ShadowCompatOperation[] edgeShadowOperation = new ShadowCompatOperation[4];
  // Pre-allocated objects that are re-used several times during path computation and rendering.
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();
  private final Path pathInsetByStroke = new Path();
  private final PointF pointF = new PointF();
  private final RectF rectF = new RectF();
  private final RectF insetRectF = new RectF();
  private final ShapePath shapePath = new ShapePath();
  private final Region transparentRegion = new Region();
  private final Region scratchRegion = new Region();
  private final float[] scratch = new float[2];
  private final float[] scratch2 = new float[2];

  private ShapeAppearanceModel shapeAppearanceModel;
  private int shadowCompatMode = SHADOW_COMPAT_MODE_DEFAULT;
  private boolean paintShadowEnabled = false;
  private boolean useTintColorForShadow = false;
  private float interpolation = 1f;
  private int shadowCompatElevation = 0;
  private int shadowCompatRadius = 0;
  private int shadowCompatOffset = 0;
  private int shadowCompatRotation = 0;
  private int alpha = 255;
  private float scale = 1f;
  private Style paintStyle = Style.FILL_AND_STROKE;

  @Nullable private PorterDuffColorFilter tintFilter;
  private PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;
  private ColorStateList tintList = null;

  @Nullable private PorterDuffColorFilter strokeTintFilter;
  private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private ColorStateList strokeTintList = null;

  private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final ShadowRenderer shadowRenderer = new ShadowRenderer();

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
    this.shapeAppearanceModel = shapeAppearanceModel;
    strokePaint.setStyle(Style.STROKE);
    fillPaint.setStyle(Style.FILL);
    clearPaint.setColor(Color.WHITE);
    clearPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));

    for (int i = 0; i < 4; i++) {
      cornerTransforms[i] = new Matrix();
      edgeTransforms[i] = new Matrix();
      cornerPaths[i] = new ShapePath();
    }
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return state;
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
    this.shapeAppearanceModel = shapeAppearanceModel;
    invalidateSelf();
  }

  /**
   * Get the {@link ShapeAppearanceModel} containing the path that will be rendered in this
   * drawable.
   *
   * @return the current model.
   */
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
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
    if (this.fillColor != fillColor) {
      this.fillColor = fillColor;
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
    return fillColor;
  }

  /**
   * Set the color used for the stroke.
   *
   * @param strokeColor the color set on the {@link Paint} object responsible for the stroke.
   */
  public void setStrokeColor(@Nullable ColorStateList strokeColor) {
    if (this.strokeColor != strokeColor) {
      this.strokeColor = strokeColor;
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
    return strokeColor;
  }

  @Override
  public void setTintMode(PorterDuff.Mode tintMode) {
    this.tintMode = tintMode;
    updateTintFilter();
    invalidateSelf();
  }

  @Override
  public void setTintList(ColorStateList tintList) {
    this.tintList = tintList;
    updateTintFilter();
    invalidateSelf();
  }

  /** Get the tint list used by the shape's paint. */
  public ColorStateList getTintList() {
    return tintList;
  }

  /**
   * Get the stroke's current {@link ColorStateList}.
   *
   * @return the stroke's current {@link ColorStateList}.
   */
  public ColorStateList getStrokeTintList() {
    return strokeTintList;
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
    return strokeTintList.getColorForState(getState(), Color.TRANSPARENT);
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
    this.strokeTintList = tintList;
    updateTintFilter();
    invalidateSelf();
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
    return strokePaint.getStrokeWidth();
  }

  /**
   * Set the stroke width used by the shape's paint.
   *
   * @param strokeWidth desired stroke width.
   */
  public void setStrokeWidth(float strokeWidth) {
    strokePaint.setStrokeWidth(strokeWidth);
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
    this.alpha = alpha;
    invalidateSelf();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    fillPaint.setColorFilter(colorFilter);
    strokePaint.setColorFilter(colorFilter);
    invalidateSelf();
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

  private RectF getBoundsAsRectF() {
    Rect bounds = getBounds();
    rectF.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
    return rectF;
  }

  public void setCornerRadius(float cornerRadius) {
    shapeAppearanceModel.setCornerRadius(cornerRadius);
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
    return shadowCompatMode;
  }

  /**
   * Set the shadow compatibility mode. This allows control over when fake shadows should drawn
   * instead of native elevation shadows.
   */
  public void setShadowCompatibilityMode(@CompatibilityShadowMode int mode) {
    shadowCompatMode = mode;
    invalidateSelf();
  }

  /**
   * Get shadow rendering status for shadows when {@link #requiresCompatShadow()} is true.
   *
   * @return true if fake shadows should be drawn, false otherwise.
   * @deprecated use {@link #getShadowCompatibilityMode()} instead
   */
  @Deprecated
  public boolean isShadowEnabled() {
    return shadowCompatMode == SHADOW_COMPAT_MODE_DEFAULT
        || shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS;
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

  /** TODO: Remove the paint shadow */
  public void setPaintShadowEnabled(boolean paintShadowEnabled) {
    this.paintShadowEnabled = paintShadowEnabled;
    shadowCompatMode = SHADOW_COMPAT_MODE_NEVER;
    // Backwards compatible defaults.
    shadowCompatElevation = 5;
    shadowCompatRadius = 10;
  }

  /**
   * Get the interpolation of the path, between 0 and 1. Ranges between 0 (none) and 1 (fully)
   * interpolated.
   *
   * @return the interpolation of the path.
   */
  public float getInterpolation() {
    return interpolation;
  }

  /**
   * Set the interpolation of the path, between 0 and 1. Ranges between 0 (none) and 1 (fully)
   * interpolated. An interpolation of 1 generally indicates a fully rendered path, while an
   * interpolation of 0 generally indicates a fully healed path, which is usually a rectangle.
   *
   * @param interpolation the desired interpolation.
   */
  public void setInterpolation(float interpolation) {
    this.interpolation = interpolation;
    invalidateSelf();
  }

  /**
   * Returns the elevation used to render fake shadows when {@link #requiresCompatShadow()} is true.
   * This value is the same as the native elevation that would be used to render shadows on API 21
   * and up.
   */
  public int getShadowElevation() {
    return shadowCompatElevation;
  }

  /**
   * Sets the elevation used to render shadows when {@link #requiresCompatShadow()} is true. This
   * value is the same as the native elevation that would be used to render shadows on API 21 and
   * up.
   *
   * <p>TODO: The shadow radius should be the actual radius drawn, shadowElevation
   * should be the height of the closest equivalent native elevation which produces a similar
   * shadow.
   */
  public void setShadowElevation(int shadowElevation) {
    this.shadowCompatRadius = shadowElevation;
    this.shadowCompatElevation = shadowElevation;
    invalidateSelf();
  }

  /**
   * Returns the shadow vertical offset rendered for shadows when {@link #requiresCompatShadow()} is
   * true.
   */
  public int getShadowVerticalOffset() {
    return shadowCompatOffset;
  }

  /**
   * Sets the shadow offset rendered by the fake shadow when {@link #requiresCompatShadow()} is
   * true. This can make the shadow appear more on the bottom or top of the view to make a more
   * realistic looking shadow depending on the placement of the view on the screen. Normally, if the
   * View is positioned further down on the screen, less shadow appears above the View, and more
   * shadow appears below it.
   */
  public void setShadowVerticalOffset(int shadowOffset) {
    this.shadowCompatOffset = shadowOffset;
    invalidateSelf();
  }

  /**
   * Returns the rotation offset applied to the fake shadow which is drawn when {@link
   * #requiresCompatShadow()} is true.
   */
  public int getShadowCompatRotation() {
    return shadowCompatRotation;
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
    this.shadowCompatRotation = shadowRotation;
    invalidateSelf();
  }

  /**
   * Get the shadow radius rendered by the path.
   *
   * @return the shadow radius rendered by the path.
   * @deprecated use {@link #getShadowElevation()} instead.
   */
  @Deprecated
  public int getShadowRadius() {
    return shadowCompatRadius;
  }

  /**
   * Set the shadow radius rendered by the path.
   *
   * @param shadowRadius the desired shadow radius.
   * @deprecated use {@link #setShadowElevation(int)} instead.
   */
  @Deprecated
  public void setShadowRadius(int shadowRadius) {
    this.shadowCompatRadius = shadowRadius;
  }

  /**
   * Returns true if fake shadows should be drawn. Native elevation shadows can't be drawn on API <
   * 21 or when the shape is concave.
   */
  private boolean requiresCompatShadow() {
    return VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || !pathInsetByStroke.isConvex();
  }

  /**
   * Get the scale of the rendered path. A value of 1 renders it at 100% size.
   *
   * @return the scale of the path.
   */
  public float getScale() {
    return scale;
  }

  /**
   * Set the scale of the rendered path. A value of 1 renders it at 100% size.
   *
   * @param scale the desired scale.
   */
  public void setScale(float scale) {
    this.scale = scale;
    invalidateSelf();
  }

  /**
   * Set whether fake shadow color should match next set tint color. This will only be drawn when
   * {@link #requiresCompatShadow()} is true, otherwise native elevation shadows will be drawn which
   * don't support colored shadows.
   *
   * @param useTintColorForShadow true if color should match; false otherwise.
   */
  public void setUseTintColorForShadow(boolean useTintColorForShadow) {
    this.useTintColorForShadow = useTintColorForShadow;
    invalidateSelf();
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
    useTintColorForShadow = false;
    invalidateSelf();
  }

  /**
   * Get the current flags used by the shape's fill paint.
   *
   * @return current paint flags.
   */
  public int getPaintFlags() {
    return fillPaint.getFlags();
  }

  /**
   * Sets the flags used by the shape's paint.
   *
   * @param flags the desired flags.
   */
  public void setPaintFlags(int flags) {
    fillPaint.setFlags(flags);
    strokePaint.setFlags(flags);
    invalidateSelf();
  }

  /**
   * Set the shader used by the shape's border paint.
   *
   * @param shader the new shader to be installed in the paint
   */
  public void setStrokePaintShader(Shader shader) {
    strokePaint.setShader(shader);
    invalidateSelf();
  }

  /**
   * Get the current style used by the shape's paint.
   *
   * @return current used paint style.
   */
  public Style getPaintStyle() {
    return paintStyle;
  }

  /**
   * Set the style used by the shape's paint.
   *
   * @param paintStyle the desired style.
   */
  public void setPaintStyle(Style paintStyle) {
    this.paintStyle = paintStyle;
    invalidateSelf();
  }

  /** Returns whether the shape should draw the compatibility shadow. */
  private boolean hasCompatShadow() {
    return shadowCompatMode != SHADOW_COMPAT_MODE_NEVER
        && shadowCompatRadius > 0
        && (shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS || requiresCompatShadow());
  }

  /** Returns whether the shape has a fill. */
  private boolean hasFill() {
    return paintStyle == Style.FILL_AND_STROKE || paintStyle == Style.FILL;
  }

  /** Returns whether the shape has a stroke with a positive width. */
  private boolean hasStroke() {
    return (paintStyle == Style.FILL_AND_STROKE || paintStyle == Style.STROKE)
        && strokePaint.getStrokeWidth() > 0;
  }

  @Override
  public void draw(Canvas canvas) {
    fillPaint.setColorFilter(tintFilter);
    final int prevAlpha = fillPaint.getAlpha();
    fillPaint.setAlpha(modulateAlpha(prevAlpha, alpha));

    strokePaint.setColorFilter(strokeTintFilter);
    final int prevStrokeAlpha = strokePaint.getAlpha();
    strokePaint.setAlpha(modulateAlpha(prevStrokeAlpha, alpha));

    if (shadowCompatElevation > 0 && paintShadowEnabled) {
      fillPaint.setShadowLayer(shadowCompatRadius, 0, shadowCompatElevation, Color.BLACK);
    }

    calculatePath(getBoundsInsetByStroke(), pathInsetByStroke);
    if (hasCompatShadow()) {
      // Save the canvas before changing the clip bounds.
      canvas.save();

      prepareCanvasForShadow(canvas);

      // Drawing the shadow in a bitmap lets us use the clear paint rather than using clipPath to
      // prevent drawing shadow under the shape. clipPath has problems :-/
      Bitmap shadowLayer =
          Bitmap.createBitmap(
              getBounds().width() + shadowCompatRadius * 2,
              getBounds().height() + shadowCompatRadius * 2,
              Bitmap.Config.ARGB_8888);
      Canvas shadowCanvas = new Canvas(shadowLayer);

      shadowCanvas.translate(shadowCompatRadius, shadowCompatRadius);

      drawCompatShadow(shadowCanvas);

      canvas.drawBitmap(shadowLayer, -shadowCompatRadius, -shadowCompatRadius, null);

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

  private void drawStrokeShape(Canvas canvas) {
    drawShape(canvas, strokePaint);
  }

  private void drawFillShape(Canvas canvas) {
    drawShape(canvas, fillPaint);
  }

  /**
   * Draw the path or try to draw a round rect if possible.
   */
  private void drawShape(Canvas canvas, Paint paint) {
    if (shapeAppearanceModel.isRoundRect()) {
      float cornerSize = shapeAppearanceModel.getTopRightCorner().getCornerSize();
      canvas.drawRoundRect(getBoundsInsetByStroke(), cornerSize, cornerSize, paint);
    } else {
      canvas.drawPath(pathInsetByStroke, paint);
    }
  }

  private void prepareCanvasForShadow(Canvas canvas) {
    // Calculate the translation to offset the canvas for the given offset and rotation.
    int shadowOffsetX = (int) (shadowCompatOffset * Math.sin(Math.toRadians(shadowCompatRotation)));
    int shadowOffsetY = (int) (shadowCompatOffset * Math.cos(Math.toRadians(shadowCompatRotation)));

    // Add space and offset the canvas for the shadows. Otherwise any shadows drawn outside would
    // be clipped and not visible.
    Rect canvasClipBounds = canvas.getClipBounds();
    canvasClipBounds.inset(-shadowCompatRadius, -shadowCompatRadius);
    //TODO: double check that offset doesn't work for sure
    canvasClipBounds.inset(-Math.abs(shadowOffsetX), -Math.abs(shadowOffsetY));
    canvas.clipRect(canvasClipBounds, Region.Op.REPLACE);

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
    if (shadowCompatOffset != 0) {
      canvas.drawPath(pathInsetByStroke, shadowRenderer.getShadowPaint());
    }

    // Draw the fake shadow for each of the corners and edges.
    for (int index = 0; index < 4; index++) {
      cornerShadowOperation[index].draw(
          cornerTransforms[index], shadowRenderer, shadowCompatRadius, canvas);
      edgeShadowOperation[index].draw(
          edgeTransforms[index], shadowRenderer, shadowCompatRadius, canvas);
    }

    int shadowOffsetX =
        (int) (shadowCompatOffset * Math.sin(Math.toRadians(shadowCompatRotation)));
    int shadowOffsetY =
        (int) (shadowCompatOffset * Math.cos(Math.toRadians(shadowCompatRotation)));

    canvas.translate(-shadowOffsetX, -shadowOffsetY);
    canvas.drawPath(pathInsetByStroke, clearPaint);
    canvas.translate(shadowOffsetX, shadowOffsetY);
  }

  @Deprecated
  public void getPathForSize(Rect bounds, Path path) {
    calculatePathForSize(new RectF(bounds), path);
  }

  /**
   * Writes to the given {@link Path} for the current edge and corner treatments at the specified
   * size.
   *
   * @param bounds bounds of target path.
   * @param path the returned path out-var.
   * @return the generated path.
   */
  private void calculatePathForSize(RectF bounds, Path path) {
    path.rewind();

    // Calculate the transformations (rotations and translations) necessary for each edge and
    // corner treatment.
    for (int index = 0; index < 4; index++) {
      setCornerPathAndTransform(index, bounds);
      setEdgePathAndTransform(index);
    }

    // Apply corners and edges to the path in clockwise interleaving sequence: top-right corner,
    // right edge, bottom-right corner, bottom edge, bottom-left corner etc. We start from the top
    // right corner rather than the top left to work around a bug in API level 21 and 22 in which
    // rounding error causes the path to incorrectly be marked as concave.
    for (int index = 0; index < 4; index++) {
      appendCornerPath(index, path);
      appendEdgePath(index, path);
    }

    path.close();
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void getOutline(Outline outline) {
    boolean isRoundRect = shapeAppearanceModel.isRoundRect();

    if (isRoundRect) {
      float radius = shapeAppearanceModel.getTopLeftCorner().getCornerSize();
      outline.setRoundRect(getBounds(), radius);
      return;
    }

    calculatePath(getBoundsAsRectF(), path);
    if (path.isConvex()) {
      outline.setConvexPath(path);
    }
  }

  private void setCornerPathAndTransform(int index, RectF bounds) {
    getCornerTreatmentForIndex(index).getCornerPath(90, interpolation, cornerPaths[index]);

    float edgeAngle = angleOfEdge(index);
    cornerTransforms[index].reset();
    getCoordinatesOfCorner(index, bounds, pointF);
    cornerTransforms[index].setTranslate(pointF.x, pointF.y);
    cornerTransforms[index].preRotate(edgeAngle);
  }

  private void setEdgePathAndTransform(int index) {
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);
    float edgeAngle = angleOfEdge(index);
    edgeTransforms[index].reset();
    edgeTransforms[index].setTranslate(scratch[0], scratch[1]);
    edgeTransforms[index].preRotate(edgeAngle);
  }

  private void appendCornerPath(int index, Path path) {
    scratch[0] = cornerPaths[index].startX;
    scratch[1] = cornerPaths[index].startY;
    cornerTransforms[index].mapPoints(scratch);
    if (index == 0) {
      path.moveTo(scratch[0], scratch[1]);
    } else {
      path.lineTo(scratch[0], scratch[1]);
    }
    cornerPaths[index].applyToPath(cornerTransforms[index], path);
    cornerShadowOperation[index] = cornerPaths[index].createShadowCompatOperation();
  }

  private void appendEdgePath(int index, Path path) {
    int nextIndex = (index + 1) % 4;
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);

    scratch2[0] = cornerPaths[nextIndex].startX;
    scratch2[1] = cornerPaths[nextIndex].startY;
    cornerTransforms[nextIndex].mapPoints(scratch2);

    float edgeLength = (float) Math.hypot(scratch[0] - scratch2[0], scratch[1] - scratch2[1]);
    float center = getEdgeCenterForIndex(index);
    shapePath.reset(0, 0);
    getEdgeTreatmentForIndex(index).getEdgePath(edgeLength, center, interpolation, shapePath);
    shapePath.applyToPath(edgeTransforms[index], path);
    edgeShadowOperation[index] = shapePath.createShadowCompatOperation();
  }

  private float getEdgeCenterForIndex(int index) {
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);
    switch (index) {
      case 1:
      case 3:
        return Math.abs(getBoundsAsRectF().centerX() - scratch[0]);
      case 2:
      case 0:
      default:
        return Math.abs(getBoundsAsRectF().centerY() - scratch[1]);
    }
  }

  private CornerTreatment getCornerTreatmentForIndex(int index) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomRightCorner();
      case 2:
        return shapeAppearanceModel.getBottomLeftCorner();
      case 3:
        return shapeAppearanceModel.getTopLeftCorner();
      case 0:
      default:
        return shapeAppearanceModel.getTopRightCorner();
    }
  }

  private EdgeTreatment getEdgeTreatmentForIndex(int index) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomEdge();
      case 2:
        return shapeAppearanceModel.getLeftEdge();
      case 3:
        return shapeAppearanceModel.getTopEdge();
      case 0:
      default:
        return shapeAppearanceModel.getRightEdge();
    }
  }

  private void getCoordinatesOfCorner(int index, RectF bounds, PointF pointF) {
    switch (index) {
      case 1: // bottom-right
        pointF.set(bounds.right, bounds.bottom);
        break;
      case 2: // bottom-left
        pointF.set(bounds.left, bounds.bottom);
        break;
      case 3: // top-left
        pointF.set(bounds.left, bounds.top);
        break;
      case 0: // top-right
      default:
        pointF.set(bounds.right, bounds.top);
        break;
    }
  }

  private float angleOfEdge(int index) {
    return 90 * (index + 1 % 4);
  }

  private void calculatePath(RectF bounds, Path path) {
    calculatePathForSize(bounds, path);
    if (scale == 1f) {
      return;
    }
    matrix.reset();
    matrix.setScale(scale, scale, bounds.width() / 2.0f, bounds.height() / 2.0f);
    path.transform(matrix);
  }

  private void updateTintFilter() {
    tintFilter = calculateTintFilter(tintList, tintMode);
    strokeTintFilter = calculateTintFilter(strokeTintList, tintMode);
    if (useTintColorForShadow) {
      shadowRenderer.setShadowColor(tintList.getColorForState(getState(), Color.TRANSPARENT));
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
    return (tintList != null && tintList.isStateful())
        || (strokeTintList != null && strokeTintList.isStateful());
  }

  @Override
  protected boolean onStateChange(int[] state) {
    boolean invalidateSelf = super.onStateChange(state);

    updateColorsForState(state, invalidateSelf);
    updateTintFilter();

    return invalidateSelf;
  }

  private boolean updateColorsForState(int[] state, boolean invalidateSelf) {
    if (fillColor != null) {
      final int previousFillColor = fillPaint.getColor();
      final int newFillColor = fillColor.getColorForState(state, previousFillColor);
      if (previousFillColor != newFillColor) {
        fillPaint.setColor(newFillColor);
        invalidateSelf = true;
      }
    }

    if (strokeColor != null) {
      final int previousStrokeColor = strokePaint.getColor();
      final int newStrokeColor = strokeColor.getColorForState(state, previousStrokeColor);
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

  /**
   * Dummy implementation of constant state. This drawable doesn't have shared state. Implementing
   * so that calls to getConstantState().newDrawable() don't crash on L and M.
   */
  private class MaterialShapeDrawableState extends ConstantState {

    @Override
    public Drawable newDrawable() {
      return MaterialShapeDrawable.this;
    }

    @Override
    public int getChangingConfigurations() {
      return 0;
    }
  }
}
