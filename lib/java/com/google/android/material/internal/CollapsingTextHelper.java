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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import com.google.android.material.animation.AnimationUtils;
import android.support.v4.math.MathUtils;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

/** Helper class for rendering and animating collapsed text. */
@RestrictTo(LIBRARY_GROUP)
public final class CollapsingTextHelper {

  // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
  // by using our own texture
  private static final boolean USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18;

  private static final boolean DEBUG_DRAW = false;
  private static final Paint DEBUG_DRAW_PAINT;

  static {
    DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
    if (DEBUG_DRAW_PAINT != null) {
      DEBUG_DRAW_PAINT.setAntiAlias(true);
      DEBUG_DRAW_PAINT.setColor(Color.MAGENTA);
    }
  }

  private final View view;

  private boolean drawTitle;
  private float expandedFraction;

  private final Rect expandedBounds;
  private final Rect collapsedBounds;
  private final RectF currentBounds;
  private int expandedTextGravity = Gravity.CENTER_VERTICAL;
  private int collapsedTextGravity = Gravity.CENTER_VERTICAL;
  private float expandedTextSize = 15;
  private float collapsedTextSize = 15;
  private ColorStateList expandedTextColor;
  private ColorStateList collapsedTextColor;

  private float expandedDrawY;
  private float collapsedDrawY;
  private float expandedDrawX;
  private float collapsedDrawX;
  private float currentDrawX;
  private float currentDrawY;
  private Typeface collapsedTypeface;
  private Typeface expandedTypeface;
  private Typeface currentTypeface;

  private CharSequence text;
  private CharSequence textToDraw;
  private boolean isRtl;

  private boolean useTexture;
  private Bitmap expandedTitleTexture;
  private Paint texturePaint;
  private float textureAscent;
  private float textureDescent;

  private float scale;
  private float currentTextSize;

  private int[] state;

  private boolean boundsChanged;

  private final TextPaint textPaint;
  private final TextPaint tmpPaint;

  private TimeInterpolator positionInterpolator;
  private TimeInterpolator textSizeInterpolator;

  private float collapsedShadowRadius;
  private float collapsedShadowDx;
  private float collapsedShadowDy;
  private int collapsedShadowColor;

  private float expandedShadowRadius;
  private float expandedShadowDx;
  private float expandedShadowDy;
  private int expandedShadowColor;

  public CollapsingTextHelper(View view) {
    this.view = view;

    textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    tmpPaint = new TextPaint(textPaint);

    collapsedBounds = new Rect();
    expandedBounds = new Rect();
    currentBounds = new RectF();
  }

  public void setTextSizeInterpolator(TimeInterpolator interpolator) {
    textSizeInterpolator = interpolator;
    recalculate();
  }

  public void setPositionInterpolator(TimeInterpolator interpolator) {
    positionInterpolator = interpolator;
    recalculate();
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

  public void setExpandedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(expandedBounds, left, top, right, bottom)) {
      expandedBounds.set(left, top, right, bottom);
      boundsChanged = true;
      onBoundsChanged();
    }
  }

  public void setCollapsedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
      collapsedBounds.set(left, top, right, bottom);
      boundsChanged = true;
      onBoundsChanged();
    }
  }

  public float calculateCollapsedTextWidth() {
    if (text == null) {
      return 0;
    }
    getTextPaintCollapsed(tmpPaint);
    return tmpPaint.measureText(text, 0, text.length());
  }

  public float getCollapsedTextHeight() {
    getTextPaintCollapsed(tmpPaint);
    // Return collapsed height measured from the baseline.
    return -tmpPaint.ascent();
  }

  public void getCollapsedTextActualBounds(RectF bounds) {
    boolean isRtl = calculateIsRtl(text);

    bounds.left =
        !isRtl ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedTextWidth();
    bounds.top = collapsedBounds.top;
    bounds.right = !isRtl ? bounds.left + calculateCollapsedTextWidth() : collapsedBounds.right;
    bounds.bottom = collapsedBounds.top + getCollapsedTextHeight();
  }

  private void getTextPaintCollapsed(TextPaint textPaint) {
    textPaint.setTextSize(collapsedTextSize);
    textPaint.setTypeface(collapsedTypeface);
  }

  void onBoundsChanged() {
    drawTitle =
        collapsedBounds.width() > 0
            && collapsedBounds.height() > 0
            && expandedBounds.width() > 0
            && expandedBounds.height() > 0;
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
    TintTypedArray a =
        TintTypedArray.obtainStyledAttributes(
            view.getContext(), resId, android.support.v7.appcompat.R.styleable.TextAppearance);
    if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
      collapsedTextColor =
          a.getColorStateList(
              android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
    }
    if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
      collapsedTextSize =
          a.getDimensionPixelSize(
              android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
              (int) collapsedTextSize);
    }
    collapsedShadowColor =
        a.getInt(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
    collapsedShadowDx =
        a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
    collapsedShadowDy =
        a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
    collapsedShadowRadius =
        a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
    a.recycle();

    if (Build.VERSION.SDK_INT >= 16) {
      collapsedTypeface = readFontFamilyTypeface(resId);
    }

    recalculate();
  }

  public void setExpandedTextAppearance(int resId) {
    TintTypedArray a =
        TintTypedArray.obtainStyledAttributes(
            view.getContext(), resId, android.support.v7.appcompat.R.styleable.TextAppearance);
    if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
      expandedTextColor =
          a.getColorStateList(
              android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
    }
    if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
      expandedTextSize =
          a.getDimensionPixelSize(
              android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
              (int) expandedTextSize);
    }
    expandedShadowColor =
        a.getInt(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
    expandedShadowDx =
        a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
    expandedShadowDy =
        a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
    expandedShadowRadius =
        a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
    a.recycle();

    if (Build.VERSION.SDK_INT >= 16) {
      expandedTypeface = readFontFamilyTypeface(resId);
    }

    recalculate();
  }

  private Typeface readFontFamilyTypeface(int resId) {
    final TypedArray a =
        view.getContext().obtainStyledAttributes(resId, new int[] {android.R.attr.fontFamily});
    try {
      final String family = a.getString(0);
      if (family != null) {
        return Typeface.create(family, Typeface.NORMAL);
      }
    } finally {
      a.recycle();
    }
    return null;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  public void setCollapsedTypeface(Typeface typeface) {
    if (collapsedTypeface != typeface) {
      collapsedTypeface = typeface;
      recalculate();
    }
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  public void setExpandedTypeface(Typeface typeface) {
    if (expandedTypeface != typeface) {
      expandedTypeface = typeface;
      recalculate();
    }
  }

  public void setTypefaces(Typeface typeface) {
    collapsedTypeface = expandedTypeface = typeface;
    recalculate();
  }

  public Typeface getCollapsedTypeface() {
    return collapsedTypeface != null ? collapsedTypeface : Typeface.DEFAULT;
  }

  public Typeface getExpandedTypeface() {
    return expandedTypeface != null ? expandedTypeface : Typeface.DEFAULT;
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

  public float getExpansionFraction() {
    return expandedFraction;
  }

  public float getCollapsedTextSize() {
    return collapsedTextSize;
  }

  public float getExpandedTextSize() {
    return expandedTextSize;
  }

  private void calculateCurrentOffsets() {
    calculateOffsets(expandedFraction);
  }

  private void calculateOffsets(final float fraction) {
    interpolateBounds(fraction);
    currentDrawX = lerp(expandedDrawX, collapsedDrawX, fraction, positionInterpolator);
    currentDrawY = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);

    setInterpolatedTextSize(
        lerp(expandedTextSize, collapsedTextSize, fraction, textSizeInterpolator));

    if (collapsedTextColor != expandedTextColor) {
      // If the collapsed and expanded text colors are different, blend them based on the
      // fraction
      textPaint.setColor(
          blendColors(getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), fraction));
    } else {
      textPaint.setColor(getCurrentCollapsedTextColor());
    }

    textPaint.setShadowLayer(
        lerp(expandedShadowRadius, collapsedShadowRadius, fraction, null),
        lerp(expandedShadowDx, collapsedShadowDx, fraction, null),
        lerp(expandedShadowDy, collapsedShadowDy, fraction, null),
        blendColors(expandedShadowColor, collapsedShadowColor, fraction));

    ViewCompat.postInvalidateOnAnimation(view);
  }

  @ColorInt
  private int getCurrentExpandedTextColor() {
    if (state != null) {
      return expandedTextColor.getColorForState(state, 0);
    } else {
      return expandedTextColor.getDefaultColor();
    }
  }

  @ColorInt
  @VisibleForTesting
  public int getCurrentCollapsedTextColor() {
    if (state != null) {
      return collapsedTextColor.getColorForState(state, 0);
    } else {
      return collapsedTextColor.getDefaultColor();
    }
  }

  private void calculateBaseOffsets() {
    final float currentTextSize = this.currentTextSize;

    // We then calculate the collapsed text size, using the same logic
    calculateUsingTextSize(collapsedTextSize);
    float width =
        textToDraw != null ? textPaint.measureText(textToDraw, 0, textToDraw.length()) : 0;
    final int collapsedAbsGravity =
        GravityCompat.getAbsoluteGravity(
            collapsedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
    switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
      case Gravity.BOTTOM:
        collapsedDrawY = collapsedBounds.bottom;
        break;
      case Gravity.TOP:
        collapsedDrawY = collapsedBounds.top - textPaint.ascent();
        break;
      case Gravity.CENTER_VERTICAL:
      default:
        float textHeight = textPaint.descent() - textPaint.ascent();
        float textOffset = (textHeight / 2) - textPaint.descent();
        collapsedDrawY = collapsedBounds.centerY() + textOffset;
        break;
    }
    switch (collapsedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        collapsedDrawX = collapsedBounds.centerX() - (width / 2);
        break;
      case Gravity.RIGHT:
        collapsedDrawX = collapsedBounds.right - width;
        break;
      case Gravity.LEFT:
      default:
        collapsedDrawX = collapsedBounds.left;
        break;
    }

    calculateUsingTextSize(expandedTextSize);
    width = textToDraw != null ? textPaint.measureText(textToDraw, 0, textToDraw.length()) : 0;
    final int expandedAbsGravity =
        GravityCompat.getAbsoluteGravity(
            expandedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
    switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
      case Gravity.BOTTOM:
        expandedDrawY = expandedBounds.bottom;
        break;
      case Gravity.TOP:
        expandedDrawY = expandedBounds.top - textPaint.ascent();
        break;
      case Gravity.CENTER_VERTICAL:
      default:
        float textHeight = textPaint.descent() - textPaint.ascent();
        float textOffset = (textHeight / 2) - textPaint.descent();
        expandedDrawY = expandedBounds.centerY() + textOffset;
        break;
    }
    switch (expandedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        expandedDrawX = expandedBounds.centerX() - (width / 2);
        break;
      case Gravity.RIGHT:
        expandedDrawX = expandedBounds.right - width;
        break;
      case Gravity.LEFT:
      default:
        expandedDrawX = expandedBounds.left;
        break;
    }

    // The bounds have changed so we need to clear the texture
    clearTexture();
    // Now reset the text size back to the original
    setInterpolatedTextSize(currentTextSize);
  }

  private void interpolateBounds(float fraction) {
    currentBounds.left =
        lerp(expandedBounds.left, collapsedBounds.left, fraction, positionInterpolator);
    currentBounds.top = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);
    currentBounds.right =
        lerp(expandedBounds.right, collapsedBounds.right, fraction, positionInterpolator);
    currentBounds.bottom =
        lerp(expandedBounds.bottom, collapsedBounds.bottom, fraction, positionInterpolator);
  }

  public void draw(Canvas canvas) {
    final int saveCount = canvas.save();

    if (textToDraw != null && drawTitle) {
      float x = currentDrawX;
      float y = currentDrawY;

      final boolean drawTexture = useTexture && expandedTitleTexture != null;

      final float ascent;
      final float descent;
      if (drawTexture) {
        ascent = textureAscent * scale;
        descent = textureDescent * scale;
      } else {
        ascent = textPaint.ascent() * scale;
        descent = textPaint.descent() * scale;
      }

      if (DEBUG_DRAW) {
        // Just a debug tool, which drawn a magenta rect in the text bounds
        canvas.drawRect(
            currentBounds.left, y + ascent, currentBounds.right, y + descent, DEBUG_DRAW_PAINT);
      }

      if (drawTexture) {
        y += ascent;
      }

      if (scale != 1f) {
        canvas.scale(scale, scale, x, y);
      }

      if (drawTexture) {
        // If we should use a texture, draw it instead of text
        canvas.drawBitmap(expandedTitleTexture, x, y, texturePaint);
      } else {
        canvas.drawText(textToDraw, 0, textToDraw.length(), x, y, textPaint);
      }
    }

    canvas.restoreToCount(saveCount);
  }

  private boolean calculateIsRtl(CharSequence text) {
    final boolean defaultIsRtl =
        ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    return (defaultIsRtl
            ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR)
        .isRtl(text, 0, text.length());
  }

  private void setInterpolatedTextSize(float textSize) {
    calculateUsingTextSize(textSize);

    // Use our texture if the scale isn't 1.0
    useTexture = USE_SCALING_TEXTURE && scale != 1f;

    if (useTexture) {
      // Make sure we have an expanded texture if needed
      ensureExpandedTexture();
    }

    ViewCompat.postInvalidateOnAnimation(view);
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private void calculateUsingTextSize(final float textSize) {
    if (text == null) {
      return;
    }

    final float collapsedWidth = collapsedBounds.width();
    final float expandedWidth = expandedBounds.width();

    final float availableWidth;
    final float newTextSize;
    boolean updateDrawText = false;

    if (isClose(textSize, collapsedTextSize)) {
      newTextSize = collapsedTextSize;
      scale = 1f;
      if (currentTypeface != collapsedTypeface) {
        currentTypeface = collapsedTypeface;
        updateDrawText = true;
      }
      availableWidth = collapsedWidth;
    } else {
      newTextSize = expandedTextSize;
      if (currentTypeface != expandedTypeface) {
        currentTypeface = expandedTypeface;
        updateDrawText = true;
      }
      if (isClose(textSize, expandedTextSize)) {
        // If we're close to the expanded text size, snap to it and use a scale of 1
        scale = 1f;
      } else {
        // Else, we'll scale down from the expanded text size
        scale = textSize / expandedTextSize;
      }

      final float textSizeRatio = collapsedTextSize / expandedTextSize;
      // This is the size of the expanded bounds when it is scaled to match the
      // collapsed text size
      final float scaledDownWidth = expandedWidth * textSizeRatio;

      if (scaledDownWidth > collapsedWidth) {
        // If the scaled down size is larger than the actual collapsed width, we need to
        // cap the available width so that when the expanded text scales down, it matches
        // the collapsed width
        availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
      } else {
        // Otherwise we'll just use the expanded width
        availableWidth = expandedWidth;
      }
    }

    if (availableWidth > 0) {
      updateDrawText = (currentTextSize != newTextSize) || boundsChanged || updateDrawText;
      currentTextSize = newTextSize;
      boundsChanged = false;
    }

    if (textToDraw == null || updateDrawText) {
      textPaint.setTextSize(currentTextSize);
      textPaint.setTypeface(currentTypeface);
      // Use linear text scaling if we're scaling the canvas
      textPaint.setLinearText(scale != 1f);

      // If we don't currently have text to draw, or the text size has changed, ellipsize...
      final CharSequence title =
          TextUtils.ellipsize(text, textPaint, availableWidth, TextUtils.TruncateAt.END);
      if (!TextUtils.equals(title, textToDraw)) {
        textToDraw = title;
        isRtl = calculateIsRtl(textToDraw);
      }
    }
  }

  private void ensureExpandedTexture() {
    if (expandedTitleTexture != null || expandedBounds.isEmpty() || TextUtils.isEmpty(textToDraw)) {
      return;
    }

    calculateOffsets(0f);
    textureAscent = textPaint.ascent();
    textureDescent = textPaint.descent();

    final int w = Math.round(textPaint.measureText(textToDraw, 0, textToDraw.length()));
    final int h = Math.round(textureDescent - textureAscent);

    if (w <= 0 || h <= 0) {
      return; // If the width or height are 0, return
    }

    expandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

    Canvas c = new Canvas(expandedTitleTexture);
    c.drawText(textToDraw, 0, textToDraw.length(), 0, h - textPaint.descent(), textPaint);

    if (texturePaint == null) {
      // Make sure we have a paint
      texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }
  }

  public void recalculate() {
    if (view.getHeight() > 0 && view.getWidth() > 0) {
      // If we've already been laid out, calculate everything now otherwise we'll wait
      // until a layout
      calculateBaseOffsets();
      calculateCurrentOffsets();
    }
  }

  /**
   * Set the title to display
   *
   * @param text
   */
  public void setText(CharSequence text) {
    if (text == null || !text.equals(this.text)) {
      this.text = text;
      textToDraw = null;
      clearTexture();
      recalculate();
    }
  }

  public CharSequence getText() {
    return text;
  }

  private void clearTexture() {
    if (expandedTitleTexture != null) {
      expandedTitleTexture.recycle();
      expandedTitleTexture = null;
    }
  }

  /**
   * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
   * defined as it's difference being < 0.001.
   */
  private static boolean isClose(float value, float targetValue) {
    return Math.abs(value - targetValue) < 0.001f;
  }

  public ColorStateList getExpandedTextColor() {
    return expandedTextColor;
  }

  public ColorStateList getCollapsedTextColor() {
    return collapsedTextColor;
  }

  /**
   * Blend {@code color1} and {@code color2} using the given ratio.
   *
   * @param ratio of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
   *     1.0 will return {@code color2}.
   */
  private static int blendColors(int color1, int color2, float ratio) {
    final float inverseRatio = 1f - ratio;
    float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
    float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
    float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
    float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
    return Color.argb((int) a, (int) r, (int) g, (int) b);
  }

  private static float lerp(
      float startValue, float endValue, float fraction, TimeInterpolator interpolator) {
    if (interpolator != null) {
      fraction = interpolator.getInterpolation(fraction);
    }
    return AnimationUtils.lerp(startValue, endValue, fraction);
  }

  private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
    return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
  }
}
