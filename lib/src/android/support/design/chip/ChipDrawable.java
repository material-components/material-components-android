/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.chip;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.animation.MotionSpec;
import android.support.design.resources.MaterialResources;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;

/**
 * ChipDrawable contains all the layout and draw logic for {@link Chip}.
 *
 * <p>Use ChipDrawable directly in contexts that require a Drawable. For example, an auto-complete
 * enabled EditText can replace snippets of text with a ChipDrawable to represent it as a semantic
 * entity.
 *
 * @see Chip
 */
public class ChipDrawable extends Drawable {

  private static final boolean DEBUG = false;

  // Visuals
  private ColorStateList chipBackgroundColor;
  private float minHeight;
  private float chipCornerRadius;
  private ColorStateList chipStrokeColor;
  private float chipStrokeWidth;
  private ColorStateList rippleColor;
  private ColorStateList rippleAlpha;

  // Text
  @Nullable private CharSequence chipText;
  private TextAppearanceSpan textAppearanceSpan;

  // Chip icon
  @Nullable private Drawable chipIcon;
  private float chipIconSize;

  // Close icon
  @Nullable private Drawable closeIcon;
  private float closeIconSize;

  // Checkable
  private boolean checkable;
  @Nullable private Drawable checkedIcon;

  // Animations
  private MotionSpec showMotionSpec;
  private MotionSpec hideMotionSpec;

  // The following attributes are adjustable padding on the chip, listed from start to end.

  // Chip starts here.

  /** Padding at the start of the chip, before the icon. */
  private float chipStartPadding;
  /** Padding at the start of the icon, after the start of the chip. If icon exists. */
  private float iconStartPadding;

  // Icon is here.

  /** Padding at the end of the icon, before the text. If icon exists. */
  private float iconEndPadding;
  /** Padding at the start of the text, after the icon. */
  private float textStartPadding;

  // Text is here.

  /** Padding at the end of the text, before the close icon. */
  private float textEndPadding;
  /** Padding at the start of the close icon, after the text. If close icon exists. */
  private float closeIconStartPadding;

  // Close icon is here.

  /** Padding at the end of the close icon, before the end of the chip. If close icon exists. */
  private float closeIconEndPadding;
  /** Padding at the end of the chip, after the close icon. */
  private float chipEndPadding;

  // Chip ends here.

  private final Context context;
  private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  private final Paint chipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint debugPaint;
  private final FontMetrics fontMetrics = new FontMetrics();

  @ColorInt private int currentChipBackgroundColor;
  @ColorInt private int currentChipStrokeColor;
  private boolean currentChecked;

  /** Returns a ChipDrawable from the given attributes. */
  public static ChipDrawable createFromAttributes(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    ChipDrawable chip = new ChipDrawable(context);
    chip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return chip;
  }

  private ChipDrawable(Context context) {
    this.context = context;

    textPaint.density = context.getResources().getDisplayMetrics().density;
    debugPaint = DEBUG ? new Paint(Paint.ANTI_ALIAS_FLAG) : null;
  }

  private void loadFromAttributes(
      AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.ChipDrawable, defStyleAttr, defStyleRes);

    chipBackgroundColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.ChipDrawable_chipBackgroundColor);
    minHeight = a.getDimension(R.styleable.ChipDrawable_android_minHeight, 0f);
    chipCornerRadius = a.getDimension(R.styleable.ChipDrawable_chipCornerRadius, 0f);
    chipStrokeColor =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_chipStrokeColor);
    chipStrokeWidth = a.getDimension(R.styleable.ChipDrawable_chipStrokeWidth, 0f);
    rippleColor =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleColor);
    rippleAlpha =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleAlpha);

    chipText = a.getText(R.styleable.ChipDrawable_chipText);
    textAppearanceSpan =
        MaterialResources.getTextAppearanceSpan(
            context, a, R.styleable.ChipDrawable_android_textAppearance);

    chipIcon = MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_chipIcon);
    chipIconSize = a.getDimension(R.styleable.ChipDrawable_chipIconSize, 0f);

    closeIcon = MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_closeIcon);
    closeIconSize = a.getDimension(R.styleable.ChipDrawable_closeIconSize, 0f);

    checkable = a.getBoolean(R.styleable.ChipDrawable_android_checkable, false);
    checkedIcon = MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_checkedIcon);

    showMotionSpec =
        MotionSpec.loadFromAttribute(context, a, R.styleable.ChipDrawable_showMotionSpec);
    hideMotionSpec =
        MotionSpec.loadFromAttribute(context, a, R.styleable.ChipDrawable_hideMotionSpec);

    chipStartPadding = a.getDimension(R.styleable.ChipDrawable_chipStartPadding, 0f);
    iconStartPadding = a.getDimension(R.styleable.ChipDrawable_iconStartPadding, 0f);
    iconEndPadding = a.getDimension(R.styleable.ChipDrawable_iconEndPadding, 0f);
    textStartPadding = a.getDimension(R.styleable.ChipDrawable_textStartPadding, 0f);
    textEndPadding = a.getDimension(R.styleable.ChipDrawable_textEndPadding, 0f);
    closeIconStartPadding = a.getDimension(R.styleable.ChipDrawable_closeIconStartPadding, 0f);
    closeIconEndPadding = a.getDimension(R.styleable.ChipDrawable_closeIconEndPadding, 0f);
    chipEndPadding = a.getDimension(R.styleable.ChipDrawable_chipEndPadding, 0f);

    a.recycle();
  }

  /**
   * Returns the width that the chip would like to be laid out.
   *
   * <p>The chip stroke is centered on the background shape's edge, so it contributes <code>
   * chipStrokeWidth / 2f</code> pixels on each side.
   */
  @Override
  public int getIntrinsicWidth() {
    return (int)
        (chipStrokeWidth / 2f
            + chipStartPadding
            + measureChipIconWidth()
            + textStartPadding
            + measureChipTextWidth(chipText)
            + textEndPadding
            + measureCloseIconWidth()
            + chipEndPadding
            + chipStrokeWidth / 2f);
  }

  /**
   * Returns the height that the chip would like to be laid out.
   *
   * <p>The chip stroke is centered on the background shape's edge, so it contributes <code>
   * chipStrokeWidth / 2f</code> pixels on each side.
   */
  @Override
  public int getIntrinsicHeight() {
    return (int) (chipStrokeWidth / 2f + minHeight + chipStrokeWidth / 2f);
  }

  @Override
  public int getMinimumWidth() {
    return (int)
        (chipStrokeWidth / 2f
            + chipStartPadding
            + measureChipIconWidth()
            + textStartPadding
            + measureChipTextWidth("M") // Show one character at minimum.
            + textEndPadding
            + measureCloseIconWidth()
            + chipEndPadding
            + chipStrokeWidth / 2f);
  }

  /** Returns the width of the chip icon plus padding, which only apply if the chip icon exists. */
  private float measureChipIconWidth() {
    if (chipIcon != null || (checkedIcon != null && currentChecked)) {
      return iconStartPadding + chipIconSize + iconEndPadding;
    }
    return 0f;
  }

  private float measureChipTextWidth(@Nullable CharSequence charSequence) {
    if (charSequence == null) {
      return 0f;
    }

    textAppearanceSpan.updateMeasureState(textPaint); // TODO: Optimize.
    return textPaint.measureText(charSequence, 0, charSequence.length());
  }

  /**
   * Returns the width of the chip close icon plus padding, which only apply if the chip close icon
   * exists.
   */
  private float measureCloseIconWidth() {
    if (closeIcon != null) {
      return closeIconStartPadding + closeIconSize + closeIconEndPadding;
    }
    return 0f;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect bounds = getBounds();
    if (bounds.isEmpty() || getAlpha() == 0) {
      return;
    }

    // 1. Draw chip background.
    drawChipBackground(canvas, bounds);

    // 2. Draw chip stroke.
    drawChipStroke(canvas, bounds);

    // 3. Draw chip icon.
    drawChipIcon(canvas, bounds);

    // 4. Draw checked icon.
    drawCheckedIcon(canvas, bounds);

    // 5. Draw chip text.
    drawChipText(canvas, bounds);

    // 6. Draw close icon.
    drawCloseIcon(canvas, bounds);

    // Debug.
    drawDebug(canvas, bounds);
  }

  /**
   * Draws the chip background, which fills the bounds except for <code>chipStrokeWidth / 2f</code>
   * pixels on each side.
   */
  private void drawChipBackground(@NonNull Canvas canvas, Rect bounds) {
    chipPaint.setColor(currentChipBackgroundColor);
    chipPaint.setStyle(Style.FILL);
    canvas.drawRoundRect(
        bounds.left + chipStrokeWidth / 2f,
        bounds.top + chipStrokeWidth / 2f,
        bounds.right - chipStrokeWidth / 2f,
        bounds.bottom - chipStrokeWidth / 2f,
        chipCornerRadius,
        chipCornerRadius,
        chipPaint);
  }

  /**
   * Draws the chip stroke, which is centered on the background shape's edge and contributes <code>
   * chipStrokeWidth / 2f</code> pixels on each side. So, the stroke perfectly fills the bounds.
   */
  private void drawChipStroke(@NonNull Canvas canvas, Rect bounds) {
    chipPaint.setColor(currentChipStrokeColor);
    chipPaint.setStyle(Style.STROKE);
    chipPaint.setStrokeWidth(chipStrokeWidth); // TODO: Optimize.
    canvas.drawRoundRect(
        bounds.left + chipStrokeWidth / 2f,
        bounds.top + chipStrokeWidth / 2f,
        bounds.right - chipStrokeWidth / 2f,
        bounds.bottom - chipStrokeWidth / 2f,
        chipCornerRadius,
        chipCornerRadius,
        chipPaint);
  }

  private void drawChipIcon(@NonNull Canvas canvas, Rect bounds) {
    if (chipIcon != null) {
      float tx = bounds.left + chipStrokeWidth / 2f + chipStartPadding + iconStartPadding;
      float ty = bounds.exactCenterY() - chipIconSize / 2f;
      canvas.translate(tx, ty);

      chipIcon.setBounds(0, 0, (int) chipIconSize, (int) chipIconSize); // TODO: Optimize?
      chipIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  private void drawCheckedIcon(@NonNull Canvas canvas, Rect bounds) {
    if (currentChecked && checkedIcon != null) {
      float tx = bounds.left + chipStrokeWidth / 2f + chipStartPadding + iconStartPadding;
      float ty = bounds.exactCenterY() - chipIconSize / 2f;
      canvas.translate(tx, ty);

      checkedIcon.setBounds(0, 0, (int) chipIconSize, (int) chipIconSize); // TODO: Optimize?
      checkedIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  /** Draws the chip text, which should appear centered vertically in the chip. */
  private void drawChipText(@NonNull Canvas canvas, Rect bounds) {
    // TODO: Bounds may be smaller than intrinsic size. Ellipsize, clip, or multiline the text.
    float x =
        bounds.left
            + chipStrokeWidth / 2f
            + chipStartPadding
            + measureChipIconWidth()
            + textStartPadding;
    float y = bounds.centerY() - measureChipTextCenterFromBaseline();
    textPaint.drawableState = getState();
    textAppearanceSpan.updateDrawState(textPaint);
    canvas.drawText(chipText, 0, chipText.length(), x, y, textPaint);
  }

  private void drawCloseIcon(@NonNull Canvas canvas, Rect bounds) {
    if (closeIcon != null) {
      float tx =
          bounds.right
              - chipStrokeWidth / 2f
              - chipEndPadding
              - closeIconEndPadding
              - closeIconSize;
      float ty = bounds.exactCenterY() - closeIconSize / 2f;
      canvas.translate(tx, ty);

      closeIcon.setBounds(0, 0, (int) closeIconSize, (int) closeIconSize); // TODO: Optimize?
      closeIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  private void drawDebug(@NonNull Canvas canvas, Rect bounds) {
    if (DEBUG) {
      canvas.drawLine(
          bounds.left, bounds.exactCenterY(), bounds.right, bounds.exactCenterY(), debugPaint);
    }
  }

  /**
   * Returns the offset from the visual center of the chip text to its baseline.
   *
   * <p>We calculate this offset because {@link Canvas#drawText(CharSequence, int, int, float,
   * float, Paint)} always draws from the text's baseline.
   */
  private float measureChipTextCenterFromBaseline() {
    textAppearanceSpan.updateMeasureState(textPaint); // TODO: Optimize.
    textPaint.getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }

  @Override
  public boolean isStateful() {
    return true;
  }

  @Override
  protected boolean onStateChange(int[] state) {
    boolean invalidate = false;

    int newChipBackgroundColor =
        chipBackgroundColor.getColorForState(state, currentChipBackgroundColor);
    if (currentChipBackgroundColor != newChipBackgroundColor) {
      currentChipBackgroundColor = newChipBackgroundColor;
      invalidate = true;
    }

    int newChipStrokeColor = chipStrokeColor.getColorForState(state, currentChipStrokeColor);
    if (currentChipStrokeColor != newChipStrokeColor) {
      currentChipStrokeColor = newChipStrokeColor;
      invalidate = true;
    }

    boolean newChecked = hasState(getState(), android.R.attr.state_checked);
    if (currentChecked != newChecked && checkedIcon != null) {
      currentChecked = newChecked;
      invalidate = true;
    }

    if (invalidate) {
      invalidateSelf();
    }

    return super.onStateChange(state) || invalidate;
  }

  @Override
  public void setAlpha(int alpha) {
    // TODO.
  }

  @Override
  public int getAlpha() {
    // TODO.
    return super.getAlpha();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    // TODO.
  }

  @Nullable
  @Override
  public ColorFilter getColorFilter() {
    // TODO.
    return super.getColorFilter();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  /** Returns whether the drawable state set contains the given state. */
  private static boolean hasState(int[] stateSet, @AttrRes int state) {
    for (int s : stateSet) {
      if (s == state) {
        return true;
      }
    }
    return false;
  }
}
