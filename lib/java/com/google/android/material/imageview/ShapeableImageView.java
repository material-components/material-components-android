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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.google.android.material.shape.Shapeable;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

/** An ImageView that draws the bitmap with the provided Shape. */
@ExperimentalImageView
public class ShapeableImageView extends AppCompatImageView implements Shapeable {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ShapeableImageView;

  private static final String TAG = "ShapeableImageView";

  private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();
  private final Matrix matrix;
  private final RectF source;
  private final RectF destination;
  private final Paint bitmapPaint;
  private final Paint borderPaint;
  private final Path path = new Path();

  private ColorStateList strokeColor;
  private ShapeAppearanceModel shapeAppearanceModel;
  @Px private int strokeWidth;

  private Bitmap bitmap;
  private BitmapShader bitmapShader;

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
    matrix = new Matrix();
    source = new RectF();
    destination = new RectF();
    bitmapPaint = new Paint();
    bitmapPaint.setAntiAlias(true);
    bitmapPaint.setFilterBitmap(true);
    bitmapPaint.setDither(true);

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

    updateShader();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (bitmap == null) {
      return;
    }

    source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    drawBitmap(canvas, source, destination);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    destination.set(
        getPaddingLeft(),
        getPaddingTop(),
        getMeasuredWidth() - getPaddingRight(),
        getMeasuredHeight() - getPaddingBottom());
    pathProvider.calculatePath(shapeAppearanceModel, 1f /*interpolation*/, destination, path);
  }

  private void drawBitmap(Canvas canvas, RectF source, RectF dest) {
    // Draw bitmap through shader first.
    matrix.reset();

    // Fit bitmap to bounds.
    matrix.setRectToRect(source, dest, ScaleToFit.FILL);

    bitmapShader.setLocalMatrix(matrix);
    bitmapPaint.setShader(bitmapShader);

    canvas.drawPath(path, bitmapPaint);
    borderPaint.setStrokeWidth(strokeWidth);
    int colorForState =
        strokeColor.getColorForState(getDrawableState(), strokeColor.getDefaultColor());

    if (strokeWidth > 0 && colorForState != Color.TRANSPARENT) {
      borderPaint.setColor(colorForState);
      canvas.drawPath(path, borderPaint);
    }
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    requestLayout();
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
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
  public void setStrokeWidth(@Px int strokeWidth) {
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
   * @see #setStrokeWidth(int)
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
   * @see #setStrokeWidth(int)
   * @see #setStrokeWidthResource(int)
   */
  @Px
  public int getStrokeWidth() {
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

  @Override
  public void setImageBitmap(Bitmap bitmap) {
    super.setImageBitmap(bitmap);
    updateShader();
  }

  @Override
  public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);
    updateShader();
  }

  @Override
  public void setImageResource(@DrawableRes int resId) {
    super.setImageResource(resId);
    updateShader();
  }

  @Override
  public void setImageURI(Uri uri) {
    super.setImageURI(uri);
    updateShader();
  }

  private void updateShader() {
    bitmap = createBitmap();
    if (bitmap != null) {
      bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }
  }

  @Nullable
  private Bitmap createBitmap() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return null;
    }

    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    Bitmap bitmap =
        Bitmap.createBitmap(
            drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }
}
