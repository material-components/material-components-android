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
import androidx.annotation.RequiresApi;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.google.android.material.shape.Shapeable;

/** An ImageView that draws the bitmap with the provided Shape. */
public class ShapeableImageView extends AppCompatImageView implements Shapeable {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ShapeableImageView;

  private static final int UNDEFINED_PADDING = Integer.MIN_VALUE;

  private final ShapeAppearancePathProvider pathProvider =
      ShapeAppearancePathProvider.getInstance();
  private final RectF destination;
  private final RectF maskRect;
  private final Paint borderPaint;
  private final Paint clearPaint;
  private final Path path = new Path();

  @Nullable private ColorStateList strokeColor;
  @Nullable private MaterialShapeDrawable shadowDrawable;

  private ShapeAppearanceModel shapeAppearanceModel;
  @Dimension private float strokeWidth;
  private Path maskPath;

  @Dimension private int leftContentPadding;
  @Dimension private int topContentPadding;
  @Dimension private int rightContentPadding;
  @Dimension private int bottomContentPadding;
  @Dimension private int startContentPadding;
  @Dimension private int endContentPadding;
  private boolean hasAdjustedPaddingAfterLayoutDirectionResolved = false;

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

    // First set all 4 contentPadding values from the `app:contentPadding` attribute:
    int contentPadding = attributes
        .getDimensionPixelSize(R.styleable.ShapeableImageView_contentPadding, 0);
    leftContentPadding = contentPadding;
    topContentPadding = contentPadding;
    rightContentPadding = contentPadding;
    bottomContentPadding = contentPadding;

    // Update each contentPadding value individually from the `app:contentPadding<Side>`
    leftContentPadding = attributes.getDimensionPixelSize(
        R.styleable.ShapeableImageView_contentPaddingLeft, contentPadding);
    topContentPadding = attributes.getDimensionPixelSize(
        R.styleable.ShapeableImageView_contentPaddingTop, contentPadding);
    rightContentPadding = attributes.getDimensionPixelSize(
        R.styleable.ShapeableImageView_contentPaddingRight, contentPadding);
    bottomContentPadding = attributes.getDimensionPixelSize(
        R.styleable.ShapeableImageView_contentPaddingBottom, contentPadding);

    // Update the relative start and end contentPadding values from those attributes:
    startContentPadding = attributes.getDimensionPixelSize(
        R.styleable.ShapeableImageView_contentPaddingStart, UNDEFINED_PADDING);
    endContentPadding = attributes.getDimensionPixelSize(
        R.styleable.ShapeableImageView_contentPaddingEnd, UNDEFINED_PADDING);

    attributes.recycle();

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
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (hasAdjustedPaddingAfterLayoutDirectionResolved) {
      return;
    }

    if (VERSION.SDK_INT > 19 && !isLayoutDirectionResolved()) {
      return;
    }

    hasAdjustedPaddingAfterLayoutDirectionResolved = true;

    // Update the super padding to be the combined `android:padding` and
    // `app:contentPadding`, keeping with ShapeableImageView's internal padding contract:
    if (VERSION.SDK_INT >= 21 && (isPaddingRelative() || isContentPaddingRelative())) {
      setPaddingRelative(
          super.getPaddingStart(),
          super.getPaddingTop(),
          super.getPaddingEnd(),
          super.getPaddingBottom());
      return;
    }

    setPadding(
        super.getPaddingLeft(),
        super.getPaddingTop(),
        super.getPaddingRight(),
        super.getPaddingBottom());
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

  /**
   * Set additional padding on the image that is not applied to the background.
   *
   * @param left   the padding on the left of the image in pixels
   * @param top    the padding on the top of the image in pixels
   * @param right  the padding on the right of the image in pixels
   * @param bottom the padding on the bottom of the image in pixels
   */
  public void setContentPadding(
      @Dimension int left, @Dimension int top, @Dimension int right, @Dimension int bottom) {
    startContentPadding = UNDEFINED_PADDING;
    endContentPadding = UNDEFINED_PADDING;

    // Super padding is equal to background padding + content padding. Adjust the content padding
    //  portion of the super padding here:
    super.setPadding(
        super.getPaddingLeft() - leftContentPadding + left,
        super.getPaddingTop() - topContentPadding + top,
        super.getPaddingRight() - rightContentPadding + right,
        super.getPaddingBottom() - bottomContentPadding + bottom);

    leftContentPadding = left;
    topContentPadding = top;
    rightContentPadding = right;
    bottomContentPadding = bottom;
  }

  /**
   * Set additional relative padding on the image that is not applied to the background.
   *
   * @param start  the padding on the start of the image in pixels
   * @param top    the padding on the top of the image in pixels
   * @param end    the padding on the end of the image in pixels
   * @param bottom the padding on the bottom of the image in pixels
   */
  @RequiresApi(17)
  public void setContentPaddingRelative(
      @Dimension int start, @Dimension int top, @Dimension int end, @Dimension int bottom) {
    // Super padding is equal to background padding + content padding. Adjust the content padding
    //  portion of the super padding here:
    super.setPaddingRelative(
        super.getPaddingStart() - getContentPaddingStart() + start,
        super.getPaddingTop() - topContentPadding + top,
        super.getPaddingEnd() - getContentPaddingEnd() + end,
        super.getPaddingBottom() - bottomContentPadding + bottom);

    leftContentPadding = isRtl() ? end : start;
    topContentPadding = top;
    rightContentPadding = isRtl() ? start : end;
    bottomContentPadding = bottom;
  }

  private boolean isContentPaddingRelative() {
    return startContentPadding != UNDEFINED_PADDING || endContentPadding != UNDEFINED_PADDING;
  }

  /**
   * The additional padding on the bottom of the image, which is not applied to the background.
   *
   * @return the bottom padding on the image
   */
  @Dimension
  public int getContentPaddingBottom() {
    return bottomContentPadding;
  }

  /**
   * The additional relative padding on the end of the image, which is not applied to the
   * background.
   *
   * @return the end padding on the image
   */
  @Dimension
  public final int getContentPaddingEnd() {
    if (endContentPadding != UNDEFINED_PADDING) {
      return endContentPadding;
    } else {
      return isRtl() ? leftContentPadding : rightContentPadding;
    }
  }

  /**
   * The additional padding on the left of the image, which is not applied to the background.
   *
   * @return the left padding on the image
   */
  @Dimension
  public int getContentPaddingLeft() {
    if (isContentPaddingRelative()) {
      if (isRtl() && endContentPadding != UNDEFINED_PADDING) {
        return endContentPadding;
      } else if (!isRtl() && startContentPadding != UNDEFINED_PADDING) {
        return startContentPadding;
      }
    }

    return leftContentPadding;
  }

  /**
   * The additional padding on the right of the image, which is not applied to the background.
   *
   * @return the right padding on the image
   */
  @Dimension
  public int getContentPaddingRight() {
    if (isContentPaddingRelative()) {
      if (isRtl() && startContentPadding != UNDEFINED_PADDING) {
        return startContentPadding;
      } else if (!isRtl() && endContentPadding != UNDEFINED_PADDING) {
        return endContentPadding;
      }
    }

    return rightContentPadding;
  }

  /**
   * The additional relative padding on the start of the image, which is not applied to the
   * background.
   *
   * @return the start padding on the image
   */
  @Dimension
  public final int getContentPaddingStart() {
    if (startContentPadding != UNDEFINED_PADDING) {
      return startContentPadding;
    } else {
      return isRtl() ? rightContentPadding : leftContentPadding;
    }
  }

  /**
   * The additional padding on the top of the image, which is not applied to the background.
   *
   * @return the top padding on the image
   */
  @Dimension
  public int getContentPaddingTop() {
    return topContentPadding;
  }

  private boolean isRtl() {
    return VERSION.SDK_INT >= 17 && getLayoutDirection() == LAYOUT_DIRECTION_RTL;
  }

  /**
   * Set the padding. This is applied to both the background and the image, and does not affect the
   * content padding differentiating the image from the background.
   *
   * @param left the left padding in pixels
   * @param top the top padding in pixels
   * @param right the right padding in pixels
   * @param bottom the bottom padding in pixels
   */
  @Override
  public void setPadding(
      @Dimension int left, @Dimension int top, @Dimension int right, @Dimension int bottom) {
    super.setPadding(
        left + getContentPaddingLeft(),
        top + getContentPaddingTop(),
        right + getContentPaddingRight(),
        bottom + getContentPaddingBottom());
  }

  /**
   * Set the relative padding. This is applied to both the background and the image, and does not
   * affect the content padding differentiating the image from the background.
   *
   * @param start the start padding in pixels
   * @param top the top padding in pixels
   * @param end the end padding in pixels
   * @param bottom the bottom padding in pixels
   */
  @Override
  public void setPaddingRelative(
      @Dimension int start, @Dimension int top, @Dimension int end, @Dimension int bottom) {
    super.setPaddingRelative(
        start + getContentPaddingStart(),
        top + getContentPaddingTop(),
        end + getContentPaddingEnd(),
        bottom + getContentPaddingBottom());
  }

  /**
   * The padding on the bottom of the View, applied to both the image and the background.
   *
   * @return the bottom padding
   */
  @Override
  @Dimension
  public int getPaddingBottom() {
    return super.getPaddingBottom() - getContentPaddingBottom();
  }

  /**
   * The relative padding on the end of the View, applied to both the image and the background.
   *
   * @return the end padding
   */
  @Override
  @Dimension
  public int getPaddingEnd() {
    return super.getPaddingEnd() - getContentPaddingEnd();
  }

  /**
   * The padding on the left of the View, applied to both the image and the background.
   *
   * @return the left padding
   */
  @Override
  @Dimension
  public int getPaddingLeft() {
    return super.getPaddingLeft() - getContentPaddingLeft();
  }

  /**
   * The padding on the right of the View, applied to both the image and the background.
   *
   * @return the right padding
   */
  @Override
  @Dimension
  public int getPaddingRight() {
    return super.getPaddingRight() - getContentPaddingRight();
  }

  /**
   * The relative padding on the start of the View, applied to both the image and the background.
   *
   * @return the start padding
   */
  @Override
  @Dimension
  public int getPaddingStart() {
    return super.getPaddingStart() - getContentPaddingStart();
  }

  /**
   * The padding on the top of the View, applied to both the image and the background.
   *
   * @return the top padding
   */
  @Override
  @Dimension
  public int getPaddingTop() {
    return super.getPaddingTop() - getContentPaddingTop();
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    if (shadowDrawable != null) {
      shadowDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    }
    updateShapeMask(getWidth(), getHeight());
    invalidate();
    if (VERSION.SDK_INT >= 21) {
      invalidateOutline();
    }
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
  }

  private void updateShapeMask(int width, int height) {
    destination.set(
        getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());
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

    private final Rect rect = new Rect();

    @Override
    public void getOutline(View view, Outline outline) {
      if (shapeAppearanceModel == null) {
        return;
      }

      if (shadowDrawable == null) {
        shadowDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
      }

      destination.round(rect);
      shadowDrawable.setBounds(rect);
      shadowDrawable.getOutline(outline);
    }
  }
}
