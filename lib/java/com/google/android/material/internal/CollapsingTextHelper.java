/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.material.internal;

import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.Layout.Alignment.ALIGN_NORMAL;
import static android.text.Layout.Alignment.ALIGN_OPPOSITE;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.core.util.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.core.math.MathUtils;
import androidx.core.text.TextDirectionHeuristicsCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.StaticLayoutBuilderCompat.StaticLayoutBuilderCompatException;
import com.google.android.material.resources.CancelableFontCallback;
import com.google.android.material.resources.CancelableFontCallback.ApplyFont;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.resources.TypefaceUtils;

/**
 * Helper class for rendering and animating collapsed text.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class CollapsingTextHelper {

  private static final String TAG = "CollapsingTextHelper";
  private static final String ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (...)

  private static final float FADE_MODE_THRESHOLD_FRACTION_RELATIVE = 0.5f;

  private static final boolean DEBUG_DRAW = false;
  @Nullable private static final Paint DEBUG_DRAW_PAINT;

  public static final int SEMITRANSPARENT_MAGENTA = 0x40FF00FF;

  static {
    DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
    if (DEBUG_DRAW_PAINT != null) {
      DEBUG_DRAW_PAINT.setAntiAlias(true);
      DEBUG_DRAW_PAINT.setColor(SEMITRANSPARENT_MAGENTA);
    }
  }

  private final View view;

  private float expandedFraction;
  private boolean fadeModeEnabled;
  private float fadeModeStartFraction;
  private float fadeModeThresholdFraction;
  private int currentOffsetY;

  @NonNull private final Rect expandedBounds;
  // collapsedBounds are valid bounds that text can be drawn inside.
  @NonNull private final Rect collapsedBounds;
  // collapsedBoundsForPlacement are collapsed bounds that are used for calculating the placement
  // of the collapsed text, but may not be valid bounds for text. If not set, collapsedBounds will
  // be used instead for the placement calculations.
  @Nullable private Rect collapsedBoundsForPlacement;
  @NonNull private final RectF currentBounds;
  private int expandedTextGravity = Gravity.CENTER_VERTICAL;
  private int collapsedTextGravity = Gravity.CENTER_VERTICAL;
  private float expandedTextSize = 15;
  private float collapsedTextSize = 15;
  private ColorStateList expandedTextColor;
  private ColorStateList collapsedTextColor;
  private int expandedLineCount;

  private float expandedDrawY;
  private float collapsedDrawY;
  private float expandedDrawX;
  private float collapsedDrawX;
  private float currentDrawX;
  private float currentDrawY;
  private Typeface collapsedTypeface;
  private Typeface collapsedTypefaceBold;
  private Typeface collapsedTypefaceDefault;
  private Typeface expandedTypeface;
  private Typeface expandedTypefaceBold;
  private Typeface expandedTypefaceDefault;
  private Typeface currentTypeface;
  private CancelableFontCallback expandedFontCallback;
  private CancelableFontCallback collapsedFontCallback;

  private TruncateAt titleTextEllipsize = TruncateAt.END;

  @Nullable private CharSequence text;
  @Nullable private CharSequence textToDraw;
  private boolean isRtl;
  private boolean isRtlTextDirectionHeuristicsEnabled = true;

  private float scale;
  private float currentTextSize;
  private float currentShadowRadius;
  private float currentShadowDx;
  private float currentShadowDy;
  private int currentShadowColor;
  private int currentMaxLines;

  private int[] state;

  private boolean boundsChanged;

  @NonNull private final TextPaint textPaint;
  @NonNull private final TextPaint tmpPaint;

  private TimeInterpolator positionInterpolator;
  private TimeInterpolator textSizeInterpolator;

  private float collapsedShadowRadius;
  private float collapsedShadowDx;
  private float collapsedShadowDy;
  private ColorStateList collapsedShadowColor;

  private float expandedShadowRadius;
  private float expandedShadowDx;
  private float expandedShadowDy;
  private ColorStateList expandedShadowColor;

  private float collapsedLetterSpacing;
  private float expandedLetterSpacing;
  private float currentLetterSpacing;

  private StaticLayout textLayout;
  private float collapsedTextWidth;
  private float collapsedTextBlend;
  private float expandedTextBlend;
  private CharSequence textToDrawCollapsed;

  private static final int ONE_LINE = 1;
  private int expandedMaxLines = ONE_LINE;
  private int collapsedMaxLines = ONE_LINE;
  private float lineSpacingAdd = StaticLayoutBuilderCompat.DEFAULT_LINE_SPACING_ADD;
  private float lineSpacingMultiplier = StaticLayoutBuilderCompat.DEFAULT_LINE_SPACING_MULTIPLIER;
  private int hyphenationFrequency = StaticLayoutBuilderCompat.DEFAULT_HYPHENATION_FREQUENCY;
  @Nullable private StaticLayoutBuilderConfigurer staticLayoutBuilderConfigurer;
  private int collapsedHeight = -1;
  private int expandedHeight = -1;
  private boolean alignBaselineAtBottom;

  public CollapsingTextHelper(View view) {
    this.view = view;

    textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    tmpPaint = new TextPaint(textPaint);

    collapsedBounds = new Rect();
    expandedBounds = new Rect();
    currentBounds = new RectF();

    fadeModeThresholdFraction = calculateFadeModeThresholdFraction();
    maybeUpdateFontWeightAdjustment(view.getContext().getResources().getConfiguration());
  }

  public void setCollapsedMaxLines(int collapsedMaxLines) {
    if (collapsedMaxLines != this.collapsedMaxLines) {
      this.collapsedMaxLines = collapsedMaxLines;
      recalculate();
    }
  }

  public void setTextSizeInterpolator(TimeInterpolator interpolator) {
    textSizeInterpolator = interpolator;
    recalculate();
  }

  public void setPositionInterpolator(TimeInterpolator interpolator) {
    positionInterpolator = interpolator;
    recalculate();
  }

  @Nullable
  public TimeInterpolator getPositionInterpolator() {
    return positionInterpolator;
  }

  public void setExpandedTextSize(float textSize) {
    if (expandedTextSize != textSize) {
      expandedTextSize = textSize;
      recalculate();
    }
  }

  public void setCollapsedTextSize(float textSize) {
    if (collapsedTextSize != textSize) {
      collapsedTextSize = textSize;
      recalculate();
    }
  }

  public void setCollapsedTextColor(ColorStateList textColor) {
    if (collapsedTextColor != textColor) {
      collapsedTextColor = textColor;
      recalculate();
    }
  }

  public void setExpandedTextColor(ColorStateList textColor) {
    if (expandedTextColor != textColor) {
      expandedTextColor = textColor;
      recalculate();
    }
  }

  public void setCollapsedAndExpandedTextColor(@Nullable ColorStateList textColor) {
    if (collapsedTextColor != textColor || expandedTextColor != textColor) {
      collapsedTextColor = textColor;
      expandedTextColor = textColor;
      recalculate();
    }
  }

  public void setExpandedLetterSpacing(float letterSpacing) {
    if (expandedLetterSpacing != letterSpacing) {
      expandedLetterSpacing = letterSpacing;
      recalculate();
    }
  }

  public void setExpandedBounds(
      int left, int top, int right, int bottom, boolean alignBaselineAtBottom) {
    if (!rectEquals(expandedBounds, left, top, right, bottom)
        || alignBaselineAtBottom != this.alignBaselineAtBottom) {
      expandedBounds.set(left, top, right, bottom);
      boundsChanged = true;
      this.alignBaselineAtBottom = alignBaselineAtBottom;
    }
  }

  public void setExpandedBounds(int left, int top, int right, int bottom) {
    setExpandedBounds(left, top, right, bottom, /* alignBaselineAtBottom= */ true);
  }

  public void setExpandedBounds(@NonNull Rect bounds) {
    setExpandedBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
  }

  public void setCollapsedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
      collapsedBounds.set(left, top, right, bottom);
      boundsChanged = true;
    }
  }

  public void setCollapsedBounds(@NonNull Rect bounds) {
    setCollapsedBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
  }

  public void setCollapsedBoundsForOffsets(int left, int top, int right, int bottom) {
    if (collapsedBoundsForPlacement == null) {
      collapsedBoundsForPlacement = new Rect(left, top, right, bottom);
      boundsChanged = true;
    }
    if (!rectEquals(collapsedBoundsForPlacement, left, top, right, bottom)) {
      collapsedBoundsForPlacement.set(left, top, right, bottom);
      boundsChanged = true;
    }
  }

  public void getCollapsedTextBottomTextBounds(
      @NonNull RectF bounds, int labelWidth, int textGravity) {
    isRtl = calculateIsRtl(text);
    bounds.left = max(getCollapsedTextLeftBound(labelWidth, textGravity), collapsedBounds.left);
    bounds.top = collapsedBounds.top;
    bounds.right =
        min(getCollapsedTextRightBound(bounds, labelWidth, textGravity), collapsedBounds.right);
    bounds.bottom = collapsedBounds.top + getCollapsedTextHeight();
    if (textLayout != null && !shouldTruncateCollapsedToSingleLine()) {
      // If the text is not truncated to one line when collapsed, we want to return the width of the
      // bottommost line, which is the textLayout's line width * the scale factor of the expanded
      // text size to the collapsed text size.
      float lineWidth =
          textLayout.getLineWidth(textLayout.getLineCount() - 1)
              * (collapsedTextSize / expandedTextSize);
      if (isRtl) {
        bounds.left = bounds.right - lineWidth;
      } else {
        bounds.right = bounds.left + lineWidth;
      }
    }
  }

  private float getCollapsedTextLeftBound(int width, int gravity) {
    if (gravity == Gravity.CENTER
        || (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
      return width / 2f - collapsedTextWidth / 2;
    } else if ((gravity & Gravity.END) == Gravity.END
        || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
      return isRtl ? collapsedBounds.left : (collapsedBounds.right - collapsedTextWidth);
    } else {
      return isRtl ? (collapsedBounds.right - collapsedTextWidth) : collapsedBounds.left;
    }
  }

  private float getCollapsedTextRightBound(@NonNull RectF bounds, int width, int gravity) {
    if (gravity == Gravity.CENTER
        || (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
      return width / 2f + collapsedTextWidth / 2;
    } else if ((gravity & Gravity.END) == Gravity.END
        || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
      return isRtl ? (bounds.left + collapsedTextWidth) : collapsedBounds.right;
    } else {
      return isRtl ? collapsedBounds.right : (bounds.left + collapsedTextWidth);
    }
  }

  public float getExpandedTextSingleLineHeight() {
    getTextPaintExpanded(tmpPaint);
    // Return expanded height measured from the baseline.
    return -tmpPaint.ascent();
  }

  public float getExpandedTextFullSingleLineHeight() {
    getTextPaintExpanded(tmpPaint);
    // Return expanded height measured from the baseline.
    return -tmpPaint.ascent() + tmpPaint.descent();
  }

  public void updateTextHeights(int availableWidth) {
    // Set collapsed height
    getTextPaintCollapsed(tmpPaint);
    StaticLayout textLayout =
        createStaticLayout(
            collapsedMaxLines,
            tmpPaint,
            text,
            availableWidth * (collapsedTextSize / expandedTextSize),
            isRtl);
    collapsedHeight = textLayout.getHeight();

    // Set expanded height
    getTextPaintExpanded(tmpPaint);
    textLayout = createStaticLayout(expandedMaxLines, tmpPaint, text, availableWidth, isRtl);
    expandedHeight = textLayout.getHeight();
  }

  public float getCollapsedTextHeight() {
    return collapsedHeight != -1 ? collapsedHeight : getCollapsedSingleLineHeight();
  }

  public float getExpandedTextHeight() {
    return expandedHeight != -1 ? expandedHeight : getExpandedTextSingleLineHeight();
  }

  public float getCollapsedSingleLineHeight() {
    getTextPaintCollapsed(tmpPaint);
    // Return collapsed height measured from the baseline.
    return -tmpPaint.ascent();
  }

  public float getCollapsedFullSingleLineHeight() {
    getTextPaintCollapsed(tmpPaint);
    // Return collapsed height measured from the baseline.
    return -tmpPaint.ascent() + tmpPaint.descent();
  }

  public void setCurrentOffsetY(int currentOffsetY) {
    this.currentOffsetY = currentOffsetY;
  }

  public void setFadeModeStartFraction(float fadeModeStartFraction) {
    this.fadeModeStartFraction = fadeModeStartFraction;
    fadeModeThresholdFraction = calculateFadeModeThresholdFraction();
  }

  private float calculateFadeModeThresholdFraction() {
    return fadeModeStartFraction
        + (1 - fadeModeStartFraction) * FADE_MODE_THRESHOLD_FRACTION_RELATIVE;
  }

  public void setFadeModeEnabled(boolean fadeModeEnabled) {
    this.fadeModeEnabled = fadeModeEnabled;
  }

  private void getTextPaintExpanded(@NonNull TextPaint textPaint) {
    textPaint.setTextSize(expandedTextSize);
    textPaint.setTypeface(expandedTypeface);
    textPaint.setLetterSpacing(expandedLetterSpacing);
  }

  private void getTextPaintCollapsed(@NonNull TextPaint textPaint) {
    textPaint.setTextSize(collapsedTextSize);
    textPaint.setTypeface(collapsedTypeface);
    textPaint.setLetterSpacing(collapsedLetterSpacing);
  }

  public void setExpandedTextGravity(int gravity) {
    if (expandedTextGravity != gravity) {
      expandedTextGravity = gravity;
      recalculate();
    }
  }

  public int getExpandedTextGravity() {
    return expandedTextGravity;
  }

  public void setCollapsedTextGravity(int gravity) {
    if (collapsedTextGravity != gravity) {
      collapsedTextGravity = gravity;
      recalculate();
    }
  }

  public int getCollapsedTextGravity() {
    return collapsedTextGravity;
  }

  public void setCollapsedTextAppearance(int resId) {
    TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);

    if (textAppearance.getTextColor() != null) {
      collapsedTextColor = textAppearance.getTextColor();
    }
    if (textAppearance.getTextSize() != 0) {
      collapsedTextSize = textAppearance.getTextSize();
    }
    if (textAppearance.shadowColor != null) {
      collapsedShadowColor = textAppearance.shadowColor;
    }
    collapsedShadowDx = textAppearance.shadowDx;
    collapsedShadowDy = textAppearance.shadowDy;
    collapsedShadowRadius = textAppearance.shadowRadius;
    collapsedLetterSpacing = textAppearance.letterSpacing;

    // Cancel pending async fetch, if any, and replace with a new one.
    if (collapsedFontCallback != null) {
      collapsedFontCallback.cancel();
    }
    collapsedFontCallback =
        new CancelableFontCallback(
            new ApplyFont() {
              @Override
              public void apply(Typeface font) {
                setCollapsedTypeface(font);
              }
            },
            textAppearance.getFallbackFont());
    textAppearance.getFontAsync(view.getContext(), collapsedFontCallback);

    recalculate();
  }

  public void setExpandedTextAppearance(int resId) {
    TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
    if (textAppearance.getTextColor() != null) {
      expandedTextColor = textAppearance.getTextColor();
    }
    if (textAppearance.getTextSize() != 0) {
      expandedTextSize = textAppearance.getTextSize();
    }
    if (textAppearance.shadowColor != null) {
      expandedShadowColor = textAppearance.shadowColor;
    }
    expandedShadowDx = textAppearance.shadowDx;
    expandedShadowDy = textAppearance.shadowDy;
    expandedShadowRadius = textAppearance.shadowRadius;
    expandedLetterSpacing = textAppearance.letterSpacing;

    // Cancel pending async fetch, if any, and replace with a new one.
    if (expandedFontCallback != null) {
      expandedFontCallback.cancel();
    }
    expandedFontCallback =
        new CancelableFontCallback(
            new ApplyFont() {
              @Override
              public void apply(Typeface font) {
                setExpandedTypeface(font);
              }
            },
            textAppearance.getFallbackFont());
    textAppearance.getFontAsync(view.getContext(), expandedFontCallback);

    recalculate();
  }

  public void setTitleTextEllipsize(@NonNull TruncateAt ellipsize) {
    titleTextEllipsize = ellipsize;
    recalculate();
  }

  @NonNull
  public TruncateAt getTitleTextEllipsize() {
    return titleTextEllipsize;
  }

  public void setCollapsedTypeface(Typeface typeface) {
    if (setCollapsedTypefaceInternal(typeface)) {
      recalculate();
    }
  }

  public void setExpandedTypeface(Typeface typeface) {
    if (setExpandedTypefaceInternal(typeface)) {
      recalculate();
    }
  }

  public void setTypefaces(Typeface typeface) {
    boolean collapsedFontChanged = setCollapsedTypefaceInternal(typeface);
    boolean expandedFontChanged = setExpandedTypefaceInternal(typeface);
    if (collapsedFontChanged || expandedFontChanged) {
      recalculate();
    }
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private boolean setCollapsedTypefaceInternal(Typeface typeface) {
    // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
    // already updated one when async op comes back after a while.
    if (collapsedFontCallback != null) {
      collapsedFontCallback.cancel();
    }
    if (collapsedTypefaceDefault != typeface) {
      collapsedTypefaceDefault = typeface;
      collapsedTypefaceBold =
          TypefaceUtils.maybeCopyWithFontWeightAdjustment(
              view.getContext().getResources().getConfiguration(), typeface);
      collapsedTypeface =
          collapsedTypefaceBold == null ? collapsedTypefaceDefault : collapsedTypefaceBold;
      return true;
    }
    return false;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private boolean setExpandedTypefaceInternal(Typeface typeface) {
    // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
    // already updated one when async op comes back after a while.
    if (expandedFontCallback != null) {
      expandedFontCallback.cancel();
    }
    if (expandedTypefaceDefault != typeface) {
      expandedTypefaceDefault = typeface;
      expandedTypefaceBold =
          TypefaceUtils.maybeCopyWithFontWeightAdjustment(
              view.getContext().getResources().getConfiguration(), typeface);
      expandedTypeface =
          expandedTypefaceBold == null ? expandedTypefaceDefault : expandedTypefaceBold;
      return true;
    }
    return false;
  }

  public Typeface getCollapsedTypeface() {
    return collapsedTypeface != null ? collapsedTypeface : Typeface.DEFAULT;
  }

  public Typeface getExpandedTypeface() {
    return expandedTypeface != null ? expandedTypeface : Typeface.DEFAULT;
  }

  public void maybeUpdateFontWeightAdjustment(@NonNull Configuration configuration) {
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      if (collapsedTypefaceDefault != null) {
        collapsedTypefaceBold =
            TypefaceUtils.maybeCopyWithFontWeightAdjustment(
                configuration, collapsedTypefaceDefault);
      }
      if (expandedTypefaceDefault != null) {
        expandedTypefaceBold =
            TypefaceUtils.maybeCopyWithFontWeightAdjustment(configuration, expandedTypefaceDefault);
      }
      collapsedTypeface =
          collapsedTypefaceBold != null ? collapsedTypefaceBold : collapsedTypefaceDefault;
      expandedTypeface =
          expandedTypefaceBold != null ? expandedTypefaceBold : expandedTypefaceDefault;
      recalculate(/* forceRecalculate= */ true);
    }
  }

  /**
   * Set the value indicating the current scroll value. This decides how much of the background will
   * be displayed, as well as the title metrics/positioning.
   *
   * <p>A value of {@code 0.0} indicates that the layout is fully expanded. A value of {@code 1.0}
   * indicates that the layout is fully collapsed.
   */
  public void setExpansionFraction(float fraction) {
    fraction = MathUtils.clamp(fraction, 0f, 1f);

    if (fraction != expandedFraction) {
      expandedFraction = fraction;
      calculateCurrentOffsets();
    }
  }

  public final boolean setState(final int[] state) {
    this.state = state;

    if (isStateful()) {
      recalculate();
      return true;
    }

    return false;
  }

  public final boolean isStateful() {
    return (collapsedTextColor != null && collapsedTextColor.isStateful())
        || (expandedTextColor != null && expandedTextColor.isStateful());
  }

  public float getFadeModeThresholdFraction() {
    return fadeModeThresholdFraction;
  }

  public float getExpansionFraction() {
    return expandedFraction;
  }

  public float getCollapsedTextSize() {
    return collapsedTextSize;
  }

  public float getExpandedTextSize() {
    return expandedTextSize;
  }

  public void setRtlTextDirectionHeuristicsEnabled(boolean rtlTextDirectionHeuristicsEnabled) {
    isRtlTextDirectionHeuristicsEnabled = rtlTextDirectionHeuristicsEnabled;
  }

  public boolean isRtlTextDirectionHeuristicsEnabled() {
    return isRtlTextDirectionHeuristicsEnabled;
  }

  private void calculateCurrentOffsets() {
    calculateOffsets(expandedFraction);
  }

  private void calculateOffsets(final float fraction) {
    interpolateBounds(fraction);
    float textBlendFraction;
    if (fadeModeEnabled) {
      if (fraction < fadeModeThresholdFraction) {
        textBlendFraction = 0F;
        currentDrawX = expandedDrawX;
        currentDrawY = expandedDrawY;

        setInterpolatedTextSize(/* fraction= */ 0);
      } else {
        textBlendFraction = 1F;
        currentDrawX = collapsedDrawX;
        currentDrawY = collapsedDrawY - max(0, currentOffsetY);

        setInterpolatedTextSize(/* fraction= */ 1);
      }
    } else {
      textBlendFraction = fraction;
      currentDrawX = lerp(expandedDrawX, collapsedDrawX, fraction, positionInterpolator);
      currentDrawY = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);

      setInterpolatedTextSize(fraction);

      if (collapsedLetterSpacing != expandedLetterSpacing) {
        textPaint.setLetterSpacing(
            lerp(
                expandedLetterSpacing,
                collapsedLetterSpacing,
                fraction,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
      } else {
        textPaint.setLetterSpacing(collapsedLetterSpacing);
      }
    }

    setCollapsedTextBlend(
        1 - lerp(0, 1, 1 - fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    setExpandedTextBlend(lerp(1, 0, fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));

    if (collapsedTextColor != expandedTextColor) {
      // If the collapsed and expanded text colors are different, blend them based on the
      // fraction
      textPaint.setColor(
          blendARGB(
              getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), textBlendFraction));
    } else {
      textPaint.setColor(getCurrentCollapsedTextColor());
    }

    // Calculates paint parameters for shadow layer.
    currentShadowRadius = lerp(expandedShadowRadius, collapsedShadowRadius, fraction, null);
    currentShadowDx = lerp(expandedShadowDx, collapsedShadowDx, fraction, null);
    currentShadowDy = lerp(expandedShadowDy, collapsedShadowDy, fraction, null);
    currentShadowColor =
        blendARGB(
            getCurrentColor(expandedShadowColor), getCurrentColor(collapsedShadowColor), fraction);
    textPaint.setShadowLayer(
        currentShadowRadius, currentShadowDx, currentShadowDy, currentShadowColor);

    if (fadeModeEnabled) {
      int originalAlpha = textPaint.getAlpha();

      // Calculates new alpha as a ratio of original alpha based on position.
      int textAlpha = (int) (calculateFadeModeTextAlpha(fraction) * originalAlpha);

      textPaint.setAlpha(textAlpha);
      // Workaround for API 31(+). Applying the shadow color for the painted text.
      if (VERSION.SDK_INT >= VERSION_CODES.S) {
        textPaint.setShadowLayer(
            currentShadowRadius,
            currentShadowDx,
            currentShadowDy,
            MaterialColors.compositeARGBWithAlpha(currentShadowColor, textPaint.getAlpha()));
      }
    }

    view.postInvalidateOnAnimation();
  }

  private float calculateFadeModeTextAlpha(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    if (fraction <= fadeModeThresholdFraction) {
      return AnimationUtils.lerp(
          /* startValue= */ 1,
          /* endValue= */ 0,
          /* startFraction= */ fadeModeStartFraction,
          /* endFraction= */ fadeModeThresholdFraction,
          fraction);
    } else {
      return AnimationUtils.lerp(
          /* startValue= */ 0,
          /* endValue= */ 1,
          /* startFraction= */ fadeModeThresholdFraction,
          /* endFraction= */ 1,
          fraction);
    }
  }

  @ColorInt
  private int getCurrentExpandedTextColor() {
    return getCurrentColor(expandedTextColor);
  }

  @ColorInt
  public int getCurrentCollapsedTextColor() {
    return getCurrentColor(collapsedTextColor);
  }

  @ColorInt
  private int getCurrentColor(@Nullable ColorStateList colorStateList) {
    if (colorStateList == null) {
      return 0;
    }
    if (state != null) {
      return colorStateList.getColorForState(state, 0);
    }
    return colorStateList.getDefaultColor();
  }

  private boolean shouldTruncateCollapsedToSingleLine() {
    return collapsedMaxLines == ONE_LINE;
  }

  private void calculateBaseOffsets(boolean forceRecalculate) {
    // We then calculate the collapsed text size, using the same logic
    calculateUsingTextSize(/* fraction= */ 1, forceRecalculate);
    if (textToDraw != null && textLayout != null) {
      textToDrawCollapsed = shouldTruncateCollapsedToSingleLine()
          ? TextUtils.ellipsize(
              textToDraw, textPaint, textLayout.getWidth(), titleTextEllipsize)
          : textToDraw;
    }
    if (textToDrawCollapsed != null) {
      collapsedTextWidth = measureTextWidth(textPaint, textToDrawCollapsed);
    } else {
      collapsedTextWidth = 0;
    }
    final int collapsedAbsGravity =
        Gravity.getAbsoluteGravity(
            collapsedTextGravity,
            isRtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

    Rect collapsedPlacementBounds = collapsedBoundsForPlacement != null
        ? collapsedBoundsForPlacement : collapsedBounds;

    switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
      case Gravity.BOTTOM:
        collapsedDrawY = collapsedPlacementBounds.bottom + textPaint.ascent();
        break;
      case Gravity.TOP:
        collapsedDrawY = collapsedPlacementBounds.top;
        break;
      case Gravity.CENTER_VERTICAL:
      default:
        float textOffset = (textPaint.descent() - textPaint.ascent()) / 2;
        collapsedDrawY = collapsedPlacementBounds.centerY() - textOffset;
        break;
    }

    switch (collapsedAbsGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        collapsedDrawX = collapsedPlacementBounds.centerX() - (collapsedTextWidth / 2);
        break;
      case Gravity.RIGHT:
        collapsedDrawX = collapsedPlacementBounds.right - collapsedTextWidth;
        break;
      case Gravity.LEFT:
      default:
        collapsedDrawX = collapsedPlacementBounds.left;
        break;
    }

    // If the collapsed text width and height can fit into the collapsed bounds, try to move it so
    // it will fit.
    if (collapsedTextWidth <= collapsedBounds.width()) {
      collapsedDrawX += max(0, collapsedBounds.left - collapsedDrawX);
      collapsedDrawX += min(0, collapsedBounds.right - (collapsedDrawX + collapsedTextWidth));
    }
    if (getCollapsedFullSingleLineHeight() <= collapsedBounds.height()) {
      collapsedDrawY += max(0, collapsedBounds.top - collapsedDrawY);
      collapsedDrawY +=
          min(0, collapsedBounds.bottom - (collapsedDrawY + getCollapsedTextHeight()));
    }

    calculateUsingTextSize(/* fraction= */ 0, forceRecalculate);
    float expandedTextHeight = textLayout != null ? textLayout.getHeight() : 0;
    float expandedTextWidth = 0;
    if (textLayout != null && expandedMaxLines > 1) {
      expandedTextWidth = textLayout.getWidth();
    } else if (textToDraw != null) {
      expandedTextWidth = measureTextWidth(textPaint, textToDraw);
    }
    expandedLineCount = textLayout != null ? textLayout.getLineCount() : 0;

    final int expandedAbsGravity =
        Gravity.getAbsoluteGravity(
            expandedTextGravity,
            isRtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
      case Gravity.BOTTOM:
        expandedDrawY =
            expandedBounds.bottom
                - expandedTextHeight
                + (alignBaselineAtBottom ? textPaint.descent() : 0);
        break;
      case Gravity.TOP:
        expandedDrawY = expandedBounds.top;
        break;
      case Gravity.CENTER_VERTICAL:
      default:
        float textOffset = expandedTextHeight / 2;
        expandedDrawY = expandedBounds.centerY() - textOffset;
        break;
    }

    switch (expandedAbsGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        expandedDrawX = expandedBounds.centerX() - (expandedTextWidth / 2);
        break;
      case Gravity.RIGHT:
        expandedDrawX = expandedBounds.right - expandedTextWidth;
        break;
      case Gravity.LEFT:
      default:
        expandedDrawX = expandedBounds.left;
        break;
    }

    // Now reset the text size back to the original
    setInterpolatedTextSize(expandedFraction);
  }

  private float measureTextWidth(TextPaint textPaint, CharSequence textToDraw) {
    return textPaint.measureText(textToDraw, 0, textToDraw.length());
  }

  private void interpolateBounds(float fraction) {
    if (fadeModeEnabled) {
      currentBounds.set(fraction < fadeModeThresholdFraction ? expandedBounds : collapsedBounds);
    } else {
      currentBounds.left =
          lerp(expandedBounds.left, collapsedBounds.left, fraction, positionInterpolator);
      currentBounds.top = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);
      currentBounds.right =
          lerp(expandedBounds.right, collapsedBounds.right, fraction, positionInterpolator);
      currentBounds.bottom =
          lerp(expandedBounds.bottom, collapsedBounds.bottom, fraction, positionInterpolator);
    }
  }

  private void setCollapsedTextBlend(float blend) {
    collapsedTextBlend = blend;
    view.postInvalidateOnAnimation();
  }

  private void setExpandedTextBlend(float blend) {
    expandedTextBlend = blend;
    view.postInvalidateOnAnimation();
  }

  public void draw(@NonNull Canvas canvas) {
    final int saveCount = canvas.save();
    // Compute where to draw textLayout for this frame
    if (textToDraw != null && currentBounds.width() > 0 && currentBounds.height() > 0) {
      textPaint.setTextSize(currentTextSize);
      float x = currentDrawX;
      float y = currentDrawY;

      if (DEBUG_DRAW) {
        // Just a debug tool, which draws semitransparent magenta rects in the expanded bounds and
        // text bounds.
        canvas.drawRect(expandedBounds, DEBUG_DRAW_PAINT);
        canvas.drawRect(
            x,
            y,
            x + textLayout.getWidth() * scale,
            y + textLayout.getHeight() * scale,
            DEBUG_DRAW_PAINT);
      }

      if (scale != 1f && !fadeModeEnabled) {
        canvas.scale(scale, scale, x, y);
      }

      if (shouldDrawMultiline()
          && shouldTruncateCollapsedToSingleLine()
          && (!fadeModeEnabled || expandedFraction > fadeModeThresholdFraction)) {
        drawMultilineTransition(canvas, currentDrawX - textLayout.getLineStart(0), y);
      } else {
        canvas.translate(x, y);
        textLayout.draw(canvas);
      }

      canvas.restoreToCount(saveCount);
    }
  }

  private boolean shouldDrawMultiline() {
    return (expandedMaxLines > 1 || collapsedMaxLines > 1) && (!isRtl || fadeModeEnabled);
  }

  private void drawMultilineTransition(@NonNull Canvas canvas, float currentExpandedX, float y) {
    int originalAlpha = textPaint.getAlpha();
    // position text appropriately
    canvas.translate(currentExpandedX, y);

    if (!fadeModeEnabled) {
      // Expanded text (when not in fade mode, because in fade mode at this point the expanded text
      // has been fully faded out, so there's no need to try to draw it again)
      textPaint.setAlpha((int) (expandedTextBlend * originalAlpha));
      // Workaround for API 31(+). Paint applies an inverse alpha of Paint object on the shadow
      // layer when collapsing mode is scale and shadow color is opaque. The workaround is to set
      // the shadow not opaque. Then Paint will respect to the color's alpha. Applying the shadow
      // color for expanded text.
      if (VERSION.SDK_INT >= VERSION_CODES.S) {
        textPaint.setShadowLayer(
            currentShadowRadius,
            currentShadowDx,
            currentShadowDy,
            MaterialColors.compositeARGBWithAlpha(currentShadowColor, textPaint.getAlpha()));
      }
      textLayout.draw(canvas);
    }

    // Collapsed text
    if (!fadeModeEnabled) {
      // Only change the collapsed text alpha when not in fade mode, because when in fade mode it
      // will be precalculated based on the current fraction in calculateOffsets()
      textPaint.setAlpha((int) (collapsedTextBlend * originalAlpha));
    }
    // Workaround for API 31(+). Applying the shadow color for collapsed text.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint.setShadowLayer(
          currentShadowRadius,
          currentShadowDx,
          currentShadowDy,
          MaterialColors.compositeARGBWithAlpha(currentShadowColor, textPaint.getAlpha()));
    }
    int lineBaseline = textLayout.getLineBaseline(0);
    canvas.drawText(
        textToDrawCollapsed,
        /* start= */ 0,
        textToDrawCollapsed.length(),
        /* x= */ 0,
        lineBaseline,
        textPaint);
    // Reverse workaround for API 31(+). Applying opaque shadow color after the expanded text and
    // the collapsed text are drawn.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint.setShadowLayer(
          currentShadowRadius, currentShadowDx, currentShadowDy, currentShadowColor);
    }

    if (!fadeModeEnabled) {
      // Remove ellipsis for Cross-section animation
      String tmp = textToDrawCollapsed.toString().trim();
      if (tmp.endsWith(ELLIPSIS_NORMAL)) {
        tmp = tmp.substring(0, tmp.length() - 1);
      }
      // Cross-section between both texts (should stay at original alpha)
      textPaint.setAlpha(originalAlpha);
      canvas.drawText(
          tmp,
          /* start= */ 0,
          min(textLayout.getLineEnd(0), tmp.length()),
          /* x= */ 0,
          lineBaseline,
          textPaint);
    }
  }

  private boolean calculateIsRtl(@NonNull CharSequence text) {
    final boolean defaultIsRtl = isDefaultIsRtl();
    return isRtlTextDirectionHeuristicsEnabled
        ? isTextDirectionHeuristicsIsRtl(text, defaultIsRtl)
        : defaultIsRtl;
  }

  private boolean isDefaultIsRtl() {
    return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

  private boolean isTextDirectionHeuristicsIsRtl(@NonNull CharSequence text, boolean defaultIsRtl) {
    return (defaultIsRtl
            ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR)
        .isRtl(text, 0, text.length());
  }

  private void setInterpolatedTextSize(float fraction) {
    calculateUsingTextSize(fraction);

    view.postInvalidateOnAnimation();
  }

  private void calculateUsingTextSize(final float fraction) {
    calculateUsingTextSize(fraction, /* forceRecalculate= */ false);
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private void calculateUsingTextSize(final float fraction, boolean forceRecalculate) {
    if (text == null) {
      return;
    }

    float collapsedWidth = collapsedBounds.width();
    float expandedWidth = expandedBounds.width();

    float availableWidth;
    float newTextSize;
    float newLetterSpacing;
    Typeface newTypeface;

    if (isClose(fraction, /* targetValue= */ 1)) {
      newTextSize = shouldTruncateCollapsedToSingleLine() ? collapsedTextSize : expandedTextSize;
      newLetterSpacing =
          shouldTruncateCollapsedToSingleLine() ? collapsedLetterSpacing : expandedLetterSpacing;
      scale =
          shouldTruncateCollapsedToSingleLine()
              ? 1f
              : lerp(expandedTextSize, collapsedTextSize, fraction, textSizeInterpolator)
                  / expandedTextSize;
      availableWidth = shouldTruncateCollapsedToSingleLine() ? collapsedWidth : expandedWidth;
      newTypeface = collapsedTypeface;
    } else {
      newTextSize = expandedTextSize;
      newLetterSpacing = expandedLetterSpacing;
      newTypeface = expandedTypeface;
      if (isClose(fraction, /* targetValue= */ 0)) {
        // If we're close to the expanded text size, snap to it and use a scale of 1
        scale = 1f;
      } else {
        // Else, we'll scale down from the expanded text size
        scale =
            lerp(expandedTextSize, collapsedTextSize, fraction, textSizeInterpolator)
                / expandedTextSize;
      }

      float textSizeRatio = collapsedTextSize / expandedTextSize;
      // This is the size of the expanded bounds when it is scaled to match the
      // collapsed text size
      float scaledDownWidth = expandedWidth * textSizeRatio;

      if (forceRecalculate || fadeModeEnabled) {
        // If we're forcing a recalculate during a measure pass, use the expanded width since the
        // collapsed width might not be ready yet
        // Or if the fade mode is enabled, we can also just use the expanded width because when
        // fading out/in there is not a continuous scale transition between expanded/collapsed text
        availableWidth = expandedWidth;
      } else {
        // If the scaled down size is larger than the actual collapsed width, we need to
        // cap the available width so that when the expanded text scales down, it matches
        // the collapsed width
        // Otherwise we'll just use the expanded width
        // If we are not truncating the collapsed text, when we are always scaling the expanded
        // text, so we will always use the expanded width as the available width
        availableWidth =
            scaledDownWidth > collapsedWidth && shouldTruncateCollapsedToSingleLine()
                ? min(collapsedWidth / textSizeRatio, expandedWidth)
                : expandedWidth;
      }
    }

    // Swap between the expanded and collapsed max lines depending on whether or not we're closer
    // to being expanded or collapsed.
    int maxLines = fraction < 0.5f ? expandedMaxLines : collapsedMaxLines;

    boolean updateDrawText;
    if (availableWidth > 0) {
      boolean textSizeChanged = currentTextSize != newTextSize;
      boolean letterSpacingChanged = currentLetterSpacing != newLetterSpacing;
      boolean typefaceChanged = currentTypeface != newTypeface;
      boolean availableWidthChanged = textLayout != null && availableWidth != textLayout.getWidth();
      boolean maxLinesChanged = currentMaxLines != maxLines;
      updateDrawText =
          textSizeChanged
              || letterSpacingChanged
              || availableWidthChanged
              || typefaceChanged
              || maxLinesChanged
              || boundsChanged;
      currentTextSize = newTextSize;
      currentLetterSpacing = newLetterSpacing;
      currentTypeface = newTypeface;
      boundsChanged = false;
      currentMaxLines = maxLines;
      // Use linear text scaling if we're scaling the canvas
      textPaint.setLinearText(scale != 1f);
    } else {
      updateDrawText = false;
    }

    if (textToDraw == null || updateDrawText) {
      textPaint.setTextSize(currentTextSize);
      textPaint.setTypeface(currentTypeface);
      textPaint.setLetterSpacing(currentLetterSpacing);

      isRtl = calculateIsRtl(text);
      textLayout =
          createStaticLayout(
              shouldDrawMultiline() ? maxLines : 1,
              textPaint,
              text,
              availableWidth * (shouldTruncateCollapsedToSingleLine() ? 1 : scale),
              isRtl);
      textToDraw = textLayout.getText();
    }
  }

  private StaticLayout createStaticLayout(
      int maxLines, TextPaint textPaint, CharSequence text, float availableWidth, boolean isRtl) {
    StaticLayout textLayout = null;
    try {
      // In multiline mode, the text alignment should be controlled by the static layout.
      Alignment textAlignment = maxLines == 1 ? ALIGN_NORMAL : getMultilineTextLayoutAlignment();
      textLayout =
          StaticLayoutBuilderCompat.obtain(text, textPaint, (int) availableWidth)
              .setEllipsize(titleTextEllipsize)
              .setIsRtl(isRtl)
              .setAlignment(textAlignment)
              .setIncludePad(false)
              .setMaxLines(maxLines)
              .setLineSpacing(lineSpacingAdd, lineSpacingMultiplier)
              .setHyphenationFrequency(hyphenationFrequency)
              .setStaticLayoutBuilderConfigurer(staticLayoutBuilderConfigurer)
              .build();
    } catch (StaticLayoutBuilderCompatException e) {
      Log.e(TAG, e.getCause().getMessage(), e);
    }

    return checkNotNull(textLayout);
  }

  private Alignment getMultilineTextLayoutAlignment() {
    int absoluteGravity =
        Gravity.getAbsoluteGravity(
            expandedTextGravity, isRtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        return ALIGN_CENTER;
      case Gravity.RIGHT:
        return isRtl ? ALIGN_NORMAL : ALIGN_OPPOSITE;
      default:
        return isRtl ? ALIGN_OPPOSITE : ALIGN_NORMAL;
    }
  }

  public void recalculate() {
    recalculate(/* forceRecalculate= */ false);
  }

  public void recalculate(boolean forceRecalculate) {
    if ((view.getHeight() > 0 && view.getWidth() > 0) || forceRecalculate) {
      // If we've already been laid out, calculate everything now otherwise we'll wait
      // until a layout
      calculateBaseOffsets(forceRecalculate);
      calculateCurrentOffsets();
    }
  }

  /**
   * Set the title to display
   *
   * @param text
   */
  public void setText(@Nullable CharSequence text) {
    if (text == null || !TextUtils.equals(this.text, text)) {
      this.text = text;
      textToDraw = null;
      recalculate();
    }
  }

  @Nullable
  public CharSequence getText() {
    return text;
  }

  public void setExpandedMaxLines(int expandedMaxLines) {
    if (expandedMaxLines != this.expandedMaxLines) {
      this.expandedMaxLines = expandedMaxLines;
      recalculate();
    }
  }

  public int getExpandedMaxLines() {
    return expandedMaxLines;
  }

  /**
   * Returns the current text line count.
   *
   * @return The current text line count.
   */
  public int getLineCount() {
    return textLayout != null ? textLayout.getLineCount() : 0;
  }

  /**
   * Returns the expanded text line count.
   *
   * @return The expanded text line count.
   */
  public int getExpandedLineCount() {
    return expandedLineCount;
  }

  @RequiresApi(VERSION_CODES.M)
  public void setLineSpacingAdd(float spacingAdd) {
    this.lineSpacingAdd = spacingAdd;
  }

  @RequiresApi(VERSION_CODES.M)
  public float getLineSpacingAdd() {
    return textLayout.getSpacingAdd();
  }

  @RequiresApi(VERSION_CODES.M)
  public void setLineSpacingMultiplier(@FloatRange(from = 0.0) float spacingMultiplier) {
    this.lineSpacingMultiplier = spacingMultiplier;
  }

  @RequiresApi(VERSION_CODES.M)
  public float getLineSpacingMultiplier() {
    return textLayout.getSpacingMultiplier();
  }

  @RequiresApi(VERSION_CODES.M)
  public void setHyphenationFrequency(int hyphenationFrequency) {
    this.hyphenationFrequency = hyphenationFrequency;
  }

  @RequiresApi(VERSION_CODES.M)
  public int getHyphenationFrequency() {
    return hyphenationFrequency;
  }

  @RequiresApi(VERSION_CODES.M)
  public void setStaticLayoutBuilderConfigurer(
      @Nullable StaticLayoutBuilderConfigurer staticLayoutBuilderConfigurer) {
    if (this.staticLayoutBuilderConfigurer != staticLayoutBuilderConfigurer) {
      this.staticLayoutBuilderConfigurer = staticLayoutBuilderConfigurer;
      recalculate(/* forceRecalculate= */ true);
    }
  }

  /**
   * Returns true if {@code value1} is 'close' to {@code value2}. Close is currently
   * defined as it's difference being < 0.00001.
   */
  private static boolean isClose(float value1, float value2) {
    return Math.abs(value1 - value2) < 0.00001f;
  }

  public ColorStateList getExpandedTextColor() {
    return expandedTextColor;
  }

  public ColorStateList getCollapsedTextColor() {
    return collapsedTextColor;
  }

  /**
   * Blend between two ARGB colors using the given ratio.
   *
   * <p>A blend ratio of 0.0 will result in {@code color1}, 0.5 will give an even blend, 1.0 will
   * result in {@code color2}.
   *
   * <p>This is different from the AndroidX implementation by rounding the blended channel values
   * with {@link Math#round(float)}.
   *
   * @param color1 the first ARGB color
   * @param color2 the second ARGB color
   * @param ratio the blend ratio of {@code color1} to {@code color2}
   */
  @ColorInt
  private static int blendARGB(
      @ColorInt int color1, @ColorInt int color2, @FloatRange(from = 0.0, to = 1.0) float ratio) {
    final float inverseRatio = 1 - ratio;
    float a = Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio;
    float r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio;
    float g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio;
    float b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio;
    return Color.argb(Math.round(a), Math.round(r), Math.round(g), Math.round(b));
  }

  private static float lerp(
      float startValue, float endValue, float fraction, @Nullable TimeInterpolator interpolator) {
    if (interpolator != null) {
      fraction = interpolator.getInterpolation(fraction);
    }
    return AnimationUtils.lerp(startValue, endValue, fraction);
  }

  private static boolean rectEquals(@NonNull Rect r, int left, int top, int right, int bottom) {
    return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
  }
}
