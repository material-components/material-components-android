/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.tooltip;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import androidx.annotation.AttrRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.TextDrawableHelper;
import com.google.android.material.internal.TextDrawableHelper.TextDrawableDelegate;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.MarkerEdgeTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.OffsetEdgeTreatment;

/**
 * A Tooltip that supports shape theming and draws a pointer on the bottom in the center of the
 * supplied bounds. Additional margin can be applied which will prevent the main bubble of the
 * Tooltip from being drawn too close to the edge of the window.
 *
 * <p>Note: {@link #setRelativeToView(View)} should be called so {@code TooltipDrawable} can
 * calculate where it is being drawn within the visible display.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class TooltipDrawable extends MaterialShapeDrawable implements TextDrawableDelegate {

  @StyleRes private static final int DEFAULT_STYLE = R.style.Widget_MaterialComponents_Tooltip;
  @AttrRes private static final int DEFAULT_THEME_ATTR = R.attr.tooltipStyle;

  @Nullable private CharSequence text;
  @NonNull private final Context context;
  @Nullable private final FontMetrics fontMetrics = new FontMetrics();

  @NonNull
  private final TextDrawableHelper textDrawableHelper =
      new TextDrawableHelper(/* delegate= */ this);

  @NonNull
  private final OnLayoutChangeListener attachedViewLayoutChangeListener =
      new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(
            View v,
            int left,
            int top,
            int right,
            int bottom,
            int oldLeft,
            int oldTop,
            int oldRight,
            int oldBottom) {
          updateLocationOnScreen(v);
        }
      };

  @NonNull private final Rect displayFrame = new Rect();

  private int padding;
  private int minWidth;
  private int minHeight;
  private int layoutMargin;
  private boolean showMarker;
  private int arrowSize;
  private int locationOnScreenX;

  private float tooltipScaleX = 1F;
  private float tooltipScaleY = 1F;
  private float tooltipPivotX = 0.5F;
  private float tooltipPivotY = 0.5F;
  private float labelOpacity = 1.0F;

  /** Returns a TooltipDrawable from the given attributes. */
  @NonNull
  public static TooltipDrawable createFromAttributes(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    TooltipDrawable tooltip = new TooltipDrawable(context, attrs, defStyleAttr, defStyleRes);
    tooltip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);

    return tooltip;
  }

  /** Returns a TooltipDrawable from the given attributes. */
  @NonNull
  public static TooltipDrawable createFromAttributes(
      @NonNull Context context, @Nullable AttributeSet attrs) {
    return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  @NonNull
  public static TooltipDrawable create(@NonNull Context context) {
    return createFromAttributes(context, null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  private TooltipDrawable(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.context = context;
    textDrawableHelper.getTextPaint().density = context.getResources().getDisplayMetrics().density;
    textDrawableHelper.getTextPaint().setTextAlign(Align.CENTER);
  }

  private void loadFromAttributes(
      @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Tooltip, defStyleAttr, defStyleRes);

    arrowSize = context.getResources().getDimensionPixelSize(R.dimen.mtrl_tooltip_arrowSize);
    showMarker = a.getBoolean(R.styleable.Tooltip_showMarker, true);
    if (showMarker) {
      setShapeAppearanceModel(
          getShapeAppearanceModel().toBuilder().setBottomEdge(createMarkerEdge()).build());
    } else {
      arrowSize = 0;
    }

    setText(a.getText(R.styleable.Tooltip_android_text));
    TextAppearance textAppearance = MaterialResources.getTextAppearance(
        context, a, R.styleable.Tooltip_android_textAppearance);
    if (textAppearance != null && a.hasValue(R.styleable.Tooltip_android_textColor)) {
      textAppearance.setTextColor(
          MaterialResources.getColorStateList(context, a, R.styleable.Tooltip_android_textColor));
    }
    setTextAppearance(textAppearance);

    int onBackground =
        MaterialColors.getColor(
            context, R.attr.colorOnBackground, TooltipDrawable.class.getCanonicalName());
    int background =
        MaterialColors.getColor(
            context, android.R.attr.colorBackground, TooltipDrawable.class.getCanonicalName());

    int backgroundTintDefault =
        MaterialColors.layer(
            ColorUtils.setAlphaComponent(background, (int) (0.9f * 255)),
            ColorUtils.setAlphaComponent(onBackground, (int) (0.6f * 255)));
    setFillColor(
        ColorStateList.valueOf(
            a.getColor(R.styleable.Tooltip_backgroundTint, backgroundTintDefault)));

    setStrokeColor(
        ColorStateList.valueOf(
            MaterialColors.getColor(
                context, R.attr.colorSurface, TooltipDrawable.class.getCanonicalName())));

    padding = a.getDimensionPixelSize(R.styleable.Tooltip_android_padding, 0);
    minWidth = a.getDimensionPixelSize(R.styleable.Tooltip_android_minWidth, 0);
    minHeight = a.getDimensionPixelSize(R.styleable.Tooltip_android_minHeight, 0);
    layoutMargin = a.getDimensionPixelSize(R.styleable.Tooltip_android_layout_margin, 0);

    a.recycle();
  }

  /**
   * Return the text that TooltipDrawable is displaying.
   *
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_text
   */
  @Nullable
  public CharSequence getText() {
    return text;
  }

  /**
   * Sets the text to be displayed using a string resource identifier.
   *
   * @param id the resource identifier of the string resource to be displayed
   * @see #setText(CharSequence)
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_text
   */
  public void setTextResource(@StringRes int id) {
    setText(context.getResources().getString(id));
  }

  /**
   * Sets the text to be displayed.
   *
   * @param text text to be displayed
   * @see #setTextResource(int)
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_text
   */
  public void setText(@Nullable CharSequence text) {
    if (!TextUtils.equals(this.text, text)) {
      this.text = text;
      textDrawableHelper.setTextWidthDirty(true);
      invalidateSelf();
    }
  }

  /**
   * Returns the TextAppearance used by this tooltip.
   *
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_textAppearance
   */
  @Nullable
  public TextAppearance getTextAppearance() {
    return textDrawableHelper.getTextAppearance();
  }

  /**
   * Sets this tooltip's text appearance using a resource id.
   *
   * @param id The resource id of this tooltip's text appearance.
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_textAppearance
   */
  public void setTextAppearanceResource(@StyleRes int id) {
    setTextAppearance(new TextAppearance(context, id));
  }

  /**
   * Sets this tooltip's text appearance.
   *
   * @param textAppearance This tooltip's text appearance.
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_textAppearance
   */
  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    textDrawableHelper.setTextAppearance(textAppearance, context);
  }

  /**
   * Returns the minimum width of TooltipDrawable in terms of pixels.
   *
   * @see #setMinWidth(int)
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_minWidth
   */
  public int getMinWidth() {
    return minWidth;
  }

  /**
   * Sets the width of the TooltipDrawable to be at least {@code minWidth} wide.
   *
   * @param minWidth the minimum width of TooltipDrawable in terms of pixels
   * @see #getMinWidth()
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_minWidth
   */
  public void setMinWidth(@Px int minWidth) {
    this.minWidth = minWidth;
    invalidateSelf();
  }

  /**
   * Returns the minimum height of TooltipDrawable in terms of pixels.
   *
   * @see #setMinHeight(int)
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_minHeight
   */
  public int getMinHeight() {
    return minHeight;
  }

  /**
   * Sets the height of the TooltipDrawable to be at least {@code minHeight} wide.
   *
   * @param minHeight the minimum height of TooltipDrawable in terms of pixels
   * @see #getMinHeight()
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_minHeight
   */
  public void setMinHeight(@Px int minHeight) {
    this.minHeight = minHeight;
    invalidateSelf();
  }

  /**
   * Returns the padding between the text of TooltipDrawable and the sides in terms of pixels.
   *
   * @see #setTextPadding(int)
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_padding
   */
  public int getTextPadding() {
    return padding;
  }

  /**
   * Sets the padding between the text of the TooltipDrawable and the sides to be {@code padding}.
   *
   * @param padding the padding to use around the text
   * @see #getTextPadding()
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_padding
   */
  public void setTextPadding(@Px int padding) {
    this.padding = padding;
    invalidateSelf();
  }

  /**
   * Returns the margin around the TooltipDrawable.
   *
   * @see #setLayoutMargin(int)
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_layout_margin
   */
  public int getLayoutMargin() {
    return layoutMargin;
  }

  /**
   * Sets the margin around the TooltipDrawable to be {@code margin}.
   *
   * @param layoutMargin the margin to use around the TooltipDrawable
   * @see #getLayoutMargin()
   * @attr ref com.google.android.material.R.styleable#Tooltip_android_layout_margin
   */
  public void setLayoutMargin(@Px int layoutMargin) {
    this.layoutMargin = layoutMargin;
    invalidateSelf();
  }

  /**
   * A fraction that controls the scale of the tooltip and the opacity of its text.
   *
   * <p>When fraction is 0.0, the tooltip will be completely hidden, as fraction approaches 1.0, the
   * tooltip will scale up from its pointer and animate in its text.
   *
   * <p>This method is typically called from within an animator's update callback. The animator in
   * this case is what is driving the animation while this method handles configuring the tooltip's
   * appearance at each frame in the animation.
   *
   * @param fraction A value between 0.0 and 1.0 that defines how "shown" the tooltip will be.
   */
  public void setRevealFraction(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    tooltipScaleX = fraction;
    tooltipScaleY = fraction;
    labelOpacity = AnimationUtils.lerp(0F, 1F, 0.19F, 1F, fraction);
    invalidateSelf();
  }

  /**
   * Set the pivot points for the tooltip.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setPivots(float pivotX, float pivotY) {
    this.tooltipPivotX = pivotX;
    this.tooltipPivotY = pivotY;
    invalidateSelf();
  }

  /**
   * Should be called to allow this drawable to calculate its position within the current display
   * frame. This allows it to apply to specified window padding.
   *
   * @see #detachView(View)
   */
  public void setRelativeToView(@Nullable View view) {
    if (view == null) {
      return;
    }
    updateLocationOnScreen(view);
    // Listen for changes that indicate the view has moved so the location can be updated
    view.addOnLayoutChangeListener(attachedViewLayoutChangeListener);
  }

  /**
   * Should be called when the view is detached from the screen.
   *
   * @see #setRelativeToView(View)
   */
  public void detachView(@Nullable View view) {
    if (view == null) {
      return;
    }
    view.removeOnLayoutChangeListener(attachedViewLayoutChangeListener);
  }

  @Override
  public int getIntrinsicWidth() {
    return (int) Math.max(2 * padding + getTextWidth(), minWidth);
  }

  @Override
  public int getIntrinsicHeight() {
    return (int) Math.max(textDrawableHelper.getTextPaint().getTextSize(), minHeight);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    canvas.save();

    // Translate the canvas by the same about that the pointer is offset to keep it pointing at the
    // same place relative to the bounds.
    float translateX = calculatePointerOffset();

    // Handle the extra space created by the arrow notch at the bottom of the tooltip by moving the
    // canvas. This allows the pointing part of the tooltip to align with the bottom of the bounds.
    float translateY = (float) -(arrowSize * Math.sqrt(2) - arrowSize);

    // Scale the tooltip. Use the bounds to set the pivot points relative to this drawable since
    // the supplied canvas is not necessarily the same size.
    canvas.scale(
        tooltipScaleX,
        tooltipScaleY,
        getBounds().left + (getBounds().width() * tooltipPivotX),
        getBounds().top + (getBounds().height() * tooltipPivotY));

    canvas.translate(translateX, translateY);

    // Draw the background.
    super.draw(canvas);

    // Draw the text.
    drawText(canvas);

    canvas.restore();
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);

    // Update the marker edge since the location of the marker arrow can move depending on the the
    // bounds.
    if (showMarker) {
      setShapeAppearanceModel(
          getShapeAppearanceModel().toBuilder().setBottomEdge(createMarkerEdge()).build());
    }
  }

  @Override
  public boolean onStateChange(int[] state) {
    // Exposed for TextDrawableDelegate.
    return super.onStateChange(state);
  }

  @Override
  public void onTextSizeChange() {
    invalidateSelf();
  }

  private void updateLocationOnScreen(@NonNull View v) {
    int[] locationOnScreen = new int[2];
    v.getLocationOnScreen(locationOnScreen);
    locationOnScreenX = locationOnScreen[0];
    v.getWindowVisibleDisplayFrame(displayFrame);
  }

  private float calculatePointerOffset() {
    float pointerOffset = 0;
    if (displayFrame.right - getBounds().right - locationOnScreenX - layoutMargin < 0) {
      pointerOffset = displayFrame.right - getBounds().right - locationOnScreenX - layoutMargin;
    } else if (displayFrame.left - getBounds().left - locationOnScreenX + layoutMargin > 0) {
      pointerOffset = displayFrame.left - getBounds().left - locationOnScreenX + layoutMargin;
    }
    return pointerOffset;
  }

  private EdgeTreatment createMarkerEdge() {
    float offset = -calculatePointerOffset();
    // The maximum distance the arrow can be offset before extends outside the bounds.
    float maxArrowOffset = (float) ((getBounds().width() - arrowSize * Math.sqrt(2)) / 2.0f);
    offset = Math.max(offset, -maxArrowOffset);
    offset = Math.min(offset, maxArrowOffset);
    return new OffsetEdgeTreatment(new MarkerEdgeTreatment(arrowSize), offset);
  }

  private void drawText(@NonNull Canvas canvas) {
    if (text == null) {
      // If text is null there's nothing to draw.
      return;
    }

    Rect bounds = getBounds();
    int y = (int) calculateTextOriginAndAlignment(bounds);

    if (textDrawableHelper.getTextAppearance() != null) {
      textDrawableHelper.getTextPaint().drawableState = getState();
      textDrawableHelper.updateTextPaintDrawState(context);
      textDrawableHelper.getTextPaint().setAlpha((int) (labelOpacity * 255));
    }

    canvas.drawText(text, 0, text.length(), bounds.centerX(), y, textDrawableHelper.getTextPaint());
  }

  private float getTextWidth() {
    if (text == null) {
      return 0;
    }
    return textDrawableHelper.getTextWidth(text.toString());
  }

  /** Calculates the text origin and alignment based on the bounds. */
  private float calculateTextOriginAndAlignment(@NonNull Rect bounds) {
    return bounds.centerY() - calculateTextCenterFromBaseline();
  }

  /**
   * Calculates the offset from the visual center of the text to its baseline.
   *
   * <p>To draw the text, we provide the origin to {@link Canvas#drawText(CharSequence, int, int,
   * float, float, Paint)}. This origin always corresponds vertically to the text's baseline.
   * Because we need to vertically center the text, we need to calculate this offset.
   *
   * <p>Note that tooltips that share the same font must have consistent text baselines despite
   * having different text strings. This is why we calculate the vertical center using {@link
   * Paint#getFontMetrics(FontMetrics)} rather than {@link Paint#getTextBounds(String, int, int,
   * Rect)}.
   */
  private float calculateTextCenterFromBaseline() {
    textDrawableHelper.getTextPaint().getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }
}
