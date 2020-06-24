/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.material.imageview;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.google.android.material.shape.Shapeable;

/** An ImageView that draws the bitmap with the provided Shape. */
public class ShapeableImageView extends AppCompatImageView implements Shapeable {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ShapeableImageView;

  private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();
  private final RectF destination;
  private final RectF maskRect;
  private final Paint borderPaint;
  private final Paint clearPaint;
  private final Path path = new Path();

  @Nullable private ColorStateList strokeColor;
  private ShapeAppearanceModel shapeAppearanceModel;
  @Dimension private float strokeWidth;
  private Path maskPath;

  public ShapeableImageView(Context context) {
    this(context, null, 0);
  }

  public ShapeableImageView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ShapeableImageView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(wrap(context, attrs, defStyle, DEF_STYLE_RES), attrs, defStyle);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    clearPaint = new Paint();
    clearPaint.setAntiAlias(true);
    clearPaint.setColor(Color.WHITE);
    clearPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
    destination = new RectF();
    maskRect = new RectF();
    maskPath = new Path();
    TypedArray attributes =
        context.obtainStyledAttributes(
            attrs, R.styleable.ShapeableImageView, defStyle, DEF_STYLE_RES);

    strokeColor =
        MaterialResources.getColorStateList(
            context, attributes, R.styleable.ShapeableImageView_strokeColor);

    strokeWidth = attributes.getDimensionPixelSize(R.styleable.ShapeableImageView_strokeWidth, 0);

    borderPaint = new Paint();
    borderPaint.setStyle(Style.STROKE);
    borderPaint.setAntiAlias(true);
    shapeAppearanceModel =
        ShapeAppearanceModel.builder(context, attrs, defStyle, DEF_STYLE_RES).build();
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      setOutlineProvider(new OutlineProvider());
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    setLayerType(LAYER_TYPE_NONE, null);
    super.onDetachedFromWindow();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    setLayerType(LAYER_TYPE_HARDWARE, null);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawPath(maskPath, clearPaint);
    drawStroke(canvas);
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    updateShapeMask(width, height);
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    updateShapeMask(getWidth(), getHeight());
    invalidate();
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
  }

  private void updateShapeMask(int width, int height) {
    destination.set(
        getPaddingLeft(),
        getPaddingTop(),
        width - getPaddingRight(),
        height - getPaddingBottom());
    pathProvider.calculatePath(shapeAppearanceModel, 1f /*interpolation*/, destination, path);
    // Remove path from rect to draw with clear paint.
    maskPath.rewind();
    maskPath.addPath(path);
    // Do not include padding to clip the background too.
    maskRect.set(0, 0, width, height);
    maskPath.addRect(maskRect, Direction.CCW);
  }

  private void drawStroke(Canvas canvas) {
    if (strokeColor == null) {
      return;
    }

    borderPaint.setStrokeWidth(strokeWidth);
    int colorForState =
        strokeColor.getColorForState(getDrawableState(), strokeColor.getDefaultColor());

    if (strokeWidth > 0 && colorForState != Color.TRANSPARENT) {
      borderPaint.setColor(colorForState);
      canvas.drawPath(path, borderPaint);
    }
  }

  /**
   * Sets the stroke color resource for this ImageView. Both stroke color and stroke width must be
   * set for a stroke to be drawn.
   *
   * @param strokeColorResourceId Color resource to use for the stroke.
   * @attr ref com.google.android.material.R.styleable#ShapeableImageView_strokeColor
   * @see #setStrokeColor(ColorStateList)
   * @see #getStrokeColor()
   */
  public void setStrokeColorResource(@ColorRes int strokeColorResourceId) {
    setStrokeColor(AppCompatResources.getColorStateList(getContext(), strokeColorResourceId));
  }

  /**
   * Returns the stroke color for this ImageView.
   *
   * @attr ref com.google.android.material.R.styleable#ShapeableImageView_strokeColor
   * @see #setStrokeColor(ColorStateList)
   * @see #setStrokeColorResource(int)
   */
  @Nullable
  public ColorStateList getStrokeColor() {
    return strokeColor;
  }

  /**
   * Sets the stroke width for this ImageView. Both stroke color and stroke width must be set for a
   * stroke to be drawn.
   *
   * @param strokeWidth Stroke width for this ImageView.
   * @attr ref com.google.android.material.R.styleable#ShapeableImageView_strokeWidth
   * @see #setStrokeWidthResource(int)
   * @see #getStrokeWidth()
   */
  public void setStrokeWidth(@Dimension float strokeWidth) {
    if (this.strokeWidth != strokeWidth) {
      this.strokeWidth = strokeWidth;
      invalidate();
    }
  }

  /**
   * Sets the stroke width dimension resource for this ImageView. Both stroke color and stroke width
   * must be set for a stroke to be drawn.
   *
   * @param strokeWidthResourceId Stroke width dimension resource for this ImageView.
   * @attr ref com.google.android.material.R.styleable#ShapeableImageView_strokeWidth
   * @see #setStrokeWidth(float)
   * @see #getStrokeWidth()
   */
  public void setStrokeWidthResource(@DimenRes int strokeWidthResourceId) {
    setStrokeWidth(getResources().getDimensionPixelSize(strokeWidthResourceId));
  }

  /**
   * Gets the stroke width for this ImageView.
   *
   * @return Stroke width for this ImageView.
   * @attr ref com.google.android.material.R.styleable#ShapeableImageView_strokeWidth
   * @see #setStrokeWidth(float)
   * @see #setStrokeWidthResource(int)
   */
  @Dimension
  public float getStrokeWidth() {
    return strokeWidth;
  }

  public void setStrokeColor(@Nullable ColorStateList strokeColor) {
    this.strokeColor = strokeColor;
    invalidate();
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  class OutlineProvider extends ViewOutlineProvider {

    private Rect rect = new Rect();

    @Override
    public void getOutline(View view, Outline outline) {
      if (shapeAppearanceModel != null && shapeAppearanceModel.isRoundRect(destination)) {
        destination.round(rect);
        float cornerSize =
            shapeAppearanceModel.getBottomLeftCornerSize().getCornerSize(destination);
        outline.setRoundRect(rect, cornerSize);
      }
    }
  }
}
