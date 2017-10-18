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
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.support.annotation.AnimatorRes;
import android.support.annotation.AttrRes;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.design.animation.MotionSpec;
import android.support.design.canvas.CanvasCompat;
import android.support.design.resources.MaterialResources;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;

/**
 * ChipDrawable contains all the layout and draw logic for {@link Chip}.
 *
 * <p>Use ChipDrawable directly in contexts that require a Drawable. For example, an auto-complete
 * enabled EditText can replace snippets of text with a ChipDrawable to represent it as a semantic
 * entity.
 *
 * @see Chip
 */
public class ChipDrawable extends Drawable implements TintAwareDrawable, Callback {

  private static final boolean DEBUG = false;

  // Visuals
  @Nullable private ColorStateList chipBackgroundColor;
  private float minHeight;
  private float chipCornerRadius;
  @Nullable private ColorStateList chipStrokeColor;
  private float chipStrokeWidth;
  @Nullable private ColorStateList rippleColor;
  @Nullable private ColorStateList rippleAlpha;

  // Text
  @Nullable private CharSequence chipText;
  @Nullable private TextAppearanceSpan textAppearanceSpan;

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
  @Nullable private MotionSpec showMotionSpec;
  @Nullable private MotionSpec hideMotionSpec;

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
  @Nullable private final Paint debugPaint;
  private final FontMetrics fontMetrics = new FontMetrics();

  @ColorInt private int currentChipBackgroundColor;
  @ColorInt private int currentChipStrokeColor;
  private boolean currentChecked;
  private int alpha = 255;

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

    setChipBackgroundColor(
        MaterialResources.getColorStateList(
            context, a, R.styleable.ChipDrawable_chipBackgroundColor));
    setMinHeight(a.getDimension(R.styleable.ChipDrawable_android_minHeight, 0f));
    setChipCornerRadius(a.getDimension(R.styleable.ChipDrawable_chipCornerRadius, 0f));
    setChipStrokeColor(
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_chipStrokeColor));
    setChipStrokeWidth(a.getDimension(R.styleable.ChipDrawable_chipStrokeWidth, 0f));
    setRippleColor(
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleColor));
    setRippleAlpha(
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleAlpha));

    setChipText(a.getText(R.styleable.ChipDrawable_chipText));
    setTextAppearanceSpan(
        MaterialResources.getTextAppearanceSpan(
            context, a, R.styleable.ChipDrawable_android_textAppearance));

    setChipIcon(MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_chipIcon));
    setChipIconSize(a.getDimension(R.styleable.ChipDrawable_chipIconSize, 0f));

    setCloseIcon(MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_closeIcon));
    setCloseIconSize(a.getDimension(R.styleable.ChipDrawable_closeIconSize, 0f));

    setCheckable(a.getBoolean(R.styleable.ChipDrawable_android_checkable, false));
    setCheckedIcon(MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_checkedIcon));

    setShowMotionSpec(
        MotionSpec.loadFromAttribute(context, a, R.styleable.ChipDrawable_showMotionSpec));
    setHideMotionSpec(
        MotionSpec.loadFromAttribute(context, a, R.styleable.ChipDrawable_hideMotionSpec));

    setChipStartPadding(a.getDimension(R.styleable.ChipDrawable_chipStartPadding, 0f));
    setIconStartPadding(a.getDimension(R.styleable.ChipDrawable_iconStartPadding, 0f));
    setIconEndPadding(a.getDimension(R.styleable.ChipDrawable_iconEndPadding, 0f));
    setTextStartPadding(a.getDimension(R.styleable.ChipDrawable_textStartPadding, 0f));
    setTextEndPadding(a.getDimension(R.styleable.ChipDrawable_textEndPadding, 0f));
    setCloseIconStartPadding(a.getDimension(R.styleable.ChipDrawable_closeIconStartPadding, 0f));
    setCloseIconEndPadding(a.getDimension(R.styleable.ChipDrawable_closeIconEndPadding, 0f));
    setChipEndPadding(a.getDimension(R.styleable.ChipDrawable_chipEndPadding, 0f));

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

    int saveCount = 0;
    if (alpha < 255) {
      saveCount =
          CanvasCompat.saveLayerAlpha(
              canvas, bounds.left, bounds.top, bounds.right, bounds.bottom, alpha);
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

    if (alpha < 255) {
      canvas.restoreToCount(saveCount);
    }
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
      // TODO: RTL.
      float tx = bounds.left + chipStrokeWidth / 2f + chipStartPadding + iconStartPadding;
      float ty = bounds.exactCenterY() - chipIconSize / 2f;
      canvas.translate(tx, ty);

      chipIcon.setBounds(0, 0, (int) chipIconSize, (int) chipIconSize);
      chipIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  private void drawCheckedIcon(@NonNull Canvas canvas, Rect bounds) {
    if (currentChecked && checkedIcon != null) {
      // TODO: RTL.
      float tx = bounds.left + chipStrokeWidth / 2f + chipStartPadding + iconStartPadding;
      float ty = bounds.exactCenterY() - chipIconSize / 2f;
      canvas.translate(tx, ty);

      checkedIcon.setBounds(0, 0, (int) chipIconSize, (int) chipIconSize);
      checkedIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  /** Draws the chip text, which should appear centered vertically in the chip. */
  private void drawChipText(@NonNull Canvas canvas, Rect bounds) {
    if (chipText != null) {
      // TODO: RTL.
      // TODO: Bounds may be smaller than intrinsic size. Ellipsize, clip, or multiline the text.
      float x =
          bounds.left
              + chipStrokeWidth / 2f
              + chipStartPadding
              + measureChipIconWidth()
              + textStartPadding;
      float y = bounds.centerY() - measureChipTextCenterFromBaseline();
      textPaint.drawableState = getState();
      if (textAppearanceSpan != null) {
        textAppearanceSpan.updateDrawState(textPaint);
      }
      canvas.drawText(chipText, 0, chipText.length(), x, y, textPaint);
    }
  }

  private void drawCloseIcon(@NonNull Canvas canvas, Rect bounds) {
    if (closeIcon != null) {
      // TODO: RTL.
      float tx =
          bounds.right
              - chipStrokeWidth / 2f
              - chipEndPadding
              - closeIconEndPadding
              - closeIconSize;
      float ty = bounds.exactCenterY() - closeIconSize / 2f;
      canvas.translate(tx, ty);

      closeIcon.setBounds(0, 0, (int) closeIconSize, (int) closeIconSize);
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
    textPaint.getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }

  @Override
  public boolean isStateful() {
    return true;
  }

  @Override
  protected boolean onStateChange(int[] state) {
    boolean invalidate = super.onStateChange(state);

    int newChipBackgroundColor =
        chipBackgroundColor != null
            ? chipBackgroundColor.getColorForState(state, currentChipBackgroundColor)
            : 0;
    if (currentChipBackgroundColor != newChipBackgroundColor) {
      currentChipBackgroundColor = newChipBackgroundColor;
      invalidate = true;
    }

    int newChipStrokeColor =
        chipStrokeColor != null
            ? chipStrokeColor.getColorForState(state, currentChipStrokeColor)
            : 0;
    if (currentChipStrokeColor != newChipStrokeColor) {
      currentChipStrokeColor = newChipStrokeColor;
      invalidate = true;
    }

    boolean newChecked = hasState(getState(), android.R.attr.state_checked) && checkable;
    if (currentChecked != newChecked && checkedIcon != null) {
      currentChecked = newChecked;
      invalidate = true;
    }

    if (chipIcon != null && chipIcon.isStateful()) {
      invalidate |= chipIcon.setState(state);
    }
    if (checkedIcon != null && checkedIcon.isStateful()) {
      invalidate |= checkedIcon.setState(state);
    }
    if (closeIcon != null && closeIcon.isStateful()) {
      invalidate |= closeIcon.setState(state);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return invalidate;
  }

  @Override
  public boolean onLayoutDirectionChanged(int layoutDirection) {
    super.onLayoutDirectionChanged(layoutDirection);

    if (chipIcon != null) {
      chipIcon.setLayoutDirection(layoutDirection);
    }
    if (checkedIcon != null) {
      checkedIcon.setLayoutDirection(layoutDirection);
    }
    if (closeIcon != null) {
      closeIcon.setLayoutDirection(layoutDirection);
    }

    invalidateSelf();
    return true;
  }

  @Override
  protected boolean onLevelChange(int level) {
    boolean invalidate = super.onLevelChange(level);

    if (chipIcon != null) {
      invalidate |= chipIcon.setLevel(level);
    }
    if (checkedIcon != null) {
      invalidate |= checkedIcon.setLevel(level);
    }
    if (closeIcon != null) {
      invalidate |= closeIcon.setLevel(level);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return invalidate;
  }

  @Override
  public boolean setVisible(boolean visible, boolean restart) {
    boolean invalidate = super.setVisible(visible, restart);

    if (chipIcon != null) {
      invalidate |= chipIcon.setVisible(visible, restart);
    }
    if (checkedIcon != null) {
      invalidate |= checkedIcon.setVisible(visible, restart);
    }
    if (closeIcon != null) {
      invalidate |= closeIcon.setVisible(visible, restart);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return invalidate;
  }

  /**
   * Sets the alpha of this ChipDrawable. This will drastically decrease draw performance. You are
   * highly encouraged to use {@link View#setAlpha(float)} instead.
   */
  @Override
  public void setAlpha(int alpha) {
    if (this.alpha != alpha) {
      this.alpha = alpha;
      invalidateSelf();
    }
  }

  @Override
  public int getAlpha() {
    return alpha;
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
  public void setTintList(@Nullable ColorStateList tint) {
    // TODO.
  }

  @Override
  public void setTintMode(@NonNull Mode tintMode) {
    // TODO.
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Nullable
  @Override
  public Region getTransparentRegion() {
    // TODO.
    return super.getTransparentRegion();
  }

  @Override
  public void getOutline(@NonNull Outline outline) {
    // TODO.
    super.getOutline(outline);
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.invalidateDrawable(who);
    }
  }

  @Override
  public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.scheduleDrawable(who, what, when);
    }
  }

  @Override
  public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.unscheduleDrawable(who, what);
    }
  }

  private void unapplyChildDrawable(@Nullable Drawable drawable) {
    if (drawable != null) {
      drawable.setCallback(null);
    }
  }

  private void applyChildDrawable(@Nullable Drawable drawable) {
    if (drawable != null) {
      drawable.setCallback(this);
      drawable.setLayoutDirection(getLayoutDirection());
      if (drawable.isStateful()) {
        drawable.setState(getState());
      }
      drawable.setLevel(getLevel());
      drawable.setVisible(isVisible(), false);
    }
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

  // Getters and setters for attributes.

  @Nullable
  public ColorStateList getChipBackgroundColor() {
    return chipBackgroundColor;
  }

  public void setChipBackgroundColorResource(@ColorRes int id) {
    setChipBackgroundColor(AppCompatResources.getColorStateList(context, id));
  }

  public void setChipBackgroundColor(@Nullable ColorStateList chipBackgroundColor) {
    if (this.chipBackgroundColor != chipBackgroundColor) {
      this.chipBackgroundColor = chipBackgroundColor;
      onStateChange(getState());
    }
  }

  public float getMinHeight() {
    return minHeight;
  }

  public void setMinHeightResource(@DimenRes int id) {
    setMinHeight(context.getResources().getDimension(id));
  }

  public void setMinHeight(float minHeight) {
    if (this.minHeight != minHeight) {
      this.minHeight = minHeight;
      invalidateSelf();
    }
  }

  public float getChipCornerRadius() {
    return chipCornerRadius;
  }

  public void setChipCornerRadiusResource(@DimenRes int id) {
    setChipCornerRadius(context.getResources().getDimension(id));
  }

  public void setChipCornerRadius(float chipCornerRadius) {
    if (this.chipCornerRadius != chipCornerRadius) {
      this.chipCornerRadius = chipCornerRadius;
      invalidateSelf();
    }
  }

  @Nullable
  public ColorStateList getChipStrokeColor() {
    return chipStrokeColor;
  }

  public void setChipStrokeColorResource(@ColorRes int id) {
    setChipStrokeColor(AppCompatResources.getColorStateList(context, id));
  }

  public void setChipStrokeColor(@Nullable ColorStateList chipStrokeColor) {
    if (this.chipStrokeColor != chipStrokeColor) {
      this.chipStrokeColor = chipStrokeColor;
      onStateChange(getState());
    }
  }

  public float getChipStrokeWidth() {
    return chipStrokeWidth;
  }

  public void setChipStrokeWidthResource(@DimenRes int id) {
    setChipStrokeWidth(context.getResources().getDimension(id));
  }

  public void setChipStrokeWidth(float chipStrokeWidth) {
    if (this.chipStrokeWidth != chipStrokeWidth) {
      this.chipStrokeWidth = chipStrokeWidth;

      chipPaint.setStrokeWidth(chipStrokeWidth);

      invalidateSelf();
    }
  }

  @Nullable
  public ColorStateList getRippleColor() {
    return rippleColor;
  }

  public void setRippleColorResource(@ColorRes int id) {
    setRippleColor(AppCompatResources.getColorStateList(context, id));
  }

  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (this.rippleColor != rippleColor) {
      this.rippleColor = rippleColor;
      invalidateSelf(); // TODO: Call onStateChange() instead once ripple is implemented.
    }
  }

  @Nullable
  public ColorStateList getRippleAlpha() {
    return rippleAlpha;
  }

  public void setRippleAlphaResource(@ColorRes int id) {
    setRippleAlpha(AppCompatResources.getColorStateList(context, id));
  }

  public void setRippleAlpha(@Nullable ColorStateList rippleAlpha) {
    if (this.rippleAlpha != rippleAlpha) {
      this.rippleAlpha = rippleAlpha;
      invalidateSelf(); // TODO: Call onStateChange() instead once ripple is implemented.
    }
  }

  @Nullable
  public CharSequence getChipText() {
    return chipText;
  }

  public void setChipTextResource(@StringRes int id) {
    setChipText(context.getResources().getString(id));
  }

  public void setChipText(@Nullable CharSequence chipText) {
    if (this.chipText != chipText) {
      this.chipText = chipText;
      invalidateSelf();
    }
  }

  @Nullable
  public TextAppearanceSpan getTextAppearanceSpan() {
    return textAppearanceSpan;
  }

  public void setTextAppearanceSpanResource(@StyleRes int id) {
    setTextAppearanceSpan(new TextAppearanceSpan(context, id));
  }

  public void setTextAppearanceSpan(@Nullable TextAppearanceSpan textAppearanceSpan) {
    if (this.textAppearanceSpan != textAppearanceSpan) {
      this.textAppearanceSpan = textAppearanceSpan;

      if (textAppearanceSpan != null) {
        textAppearanceSpan.updateMeasureState(textPaint);
      }

      invalidateSelf();
    }
  }

  @Nullable
  public Drawable getChipIcon() {
    return chipIcon;
  }

  public void setChipIconResource(@DrawableRes int id) {
    setChipIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setChipIcon(@Nullable Drawable chipIcon) {
    if (this.chipIcon != chipIcon) {
      unapplyChildDrawable(this.chipIcon);
      this.chipIcon = chipIcon;
      applyChildDrawable(chipIcon);

      invalidateSelf();
    }
  }

  public float getChipIconSize() {
    return chipIconSize;
  }

  public void setChipIconSizeResource(@DimenRes int id) {
    setChipIconSize(context.getResources().getDimension(id));
  }

  public void setChipIconSize(float chipIconSize) {
    if (this.chipIconSize != chipIconSize) {
      this.chipIconSize = chipIconSize;
      invalidateSelf();
    }
  }

  @Nullable
  public Drawable getCloseIcon() {
    return closeIcon;
  }

  public void setCloseIconResource(@DrawableRes int id) {
    setCloseIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setCloseIcon(@Nullable Drawable closeIcon) {
    if (this.closeIcon != closeIcon) {
      unapplyChildDrawable(this.closeIcon);
      this.closeIcon = closeIcon;
      applyChildDrawable(closeIcon);

      invalidateSelf();
    }
  }

  public float getCloseIconSize() {
    return closeIconSize;
  }

  public void setCloseIconSizeResource(@DimenRes int id) {
    setCloseIconSize(context.getResources().getDimension(id));
  }

  public void setCloseIconSize(float closeIconSize) {
    if (this.closeIconSize != closeIconSize) {
      this.closeIconSize = closeIconSize;
      invalidateSelf();
    }
  }

  public boolean isCheckable() {
    return checkable;
  }

  public void setCheckableResource(@BoolRes int id) {
    setCheckable(context.getResources().getBoolean(id));
  }

  public void setCheckable(boolean checkable) {
    if (this.checkable != checkable) {
      this.checkable = checkable;

      if (!checkable) {
        currentChecked = false;
      }

      invalidateSelf();
    }
  }

  @Nullable
  public Drawable getCheckedIcon() {
    return checkedIcon;
  }

  public void setCheckedIconResource(@DrawableRes int id) {
    setCheckedIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    if (this.checkedIcon != checkedIcon) {
      unapplyChildDrawable(this.checkedIcon);
      this.checkedIcon = checkedIcon;
      applyChildDrawable(checkedIcon);

      invalidateSelf();
    }
  }

  @Nullable
  public MotionSpec getShowMotionSpec() {
    return showMotionSpec;
  }

  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.loadFromResource(context, id));
  }

  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    this.showMotionSpec = showMotionSpec;
  }

  @Nullable
  public MotionSpec getHideMotionSpec() {
    return hideMotionSpec;
  }

  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.loadFromResource(context, id));
  }

  public void setHideMotionSpec(MotionSpec hideMotionSpec) {
    this.hideMotionSpec = hideMotionSpec;
  }

  public float getChipStartPadding() {
    return chipStartPadding;
  }

  public void setChipStartPaddingResource(@DimenRes int id) {
    setChipStartPadding(context.getResources().getDimension(id));
  }

  public void setChipStartPadding(float chipStartPadding) {
    if (this.chipStartPadding != chipStartPadding) {
      this.chipStartPadding = chipStartPadding;
      invalidateSelf();
    }
  }

  public float getIconStartPadding() {
    return iconStartPadding;
  }

  public void setIconStartPaddingResource(@DimenRes int id) {
    setIconStartPadding(context.getResources().getDimension(id));
  }

  public void setIconStartPadding(float iconStartPadding) {
    if (this.iconStartPadding != iconStartPadding) {
      this.iconStartPadding = iconStartPadding;
      invalidateSelf();
    }
  }

  public float getIconEndPadding() {
    return iconEndPadding;
  }

  public void setIconEndPaddingResource(@DimenRes int id) {
    setIconEndPadding(context.getResources().getDimension(id));
  }

  public void setIconEndPadding(float iconEndPadding) {
    if (this.iconEndPadding != iconEndPadding) {
      this.iconEndPadding = iconEndPadding;
      invalidateSelf();
    }
  }

  public float getTextStartPadding() {
    return textStartPadding;
  }

  public void setTextStartPaddingResource(@DimenRes int id) {
    setTextStartPadding(context.getResources().getDimension(id));
  }

  public void setTextStartPadding(float textStartPadding) {
    if (this.textStartPadding != textStartPadding) {
      this.textStartPadding = textStartPadding;
      invalidateSelf();
    }
  }

  public float getTextEndPadding() {
    return textEndPadding;
  }

  public void setTextEndPaddingResource(@DimenRes int id) {
    setTextEndPadding(context.getResources().getDimension(id));
  }

  public void setTextEndPadding(float textEndPadding) {
    if (this.textEndPadding != textEndPadding) {
      this.textEndPadding = textEndPadding;
      invalidateSelf();
    }
  }

  public float getCloseIconStartPadding() {
    return closeIconStartPadding;
  }

  public void setCloseIconStartPaddingResource(@DimenRes int id) {
    setCloseIconStartPadding(context.getResources().getDimension(id));
  }

  public void setCloseIconStartPadding(float closeIconStartPadding) {
    if (this.closeIconStartPadding != closeIconStartPadding) {
      this.closeIconStartPadding = closeIconStartPadding;
      invalidateSelf();
    }
  }

  public float getCloseIconEndPadding() {
    return closeIconEndPadding;
  }

  public void setCloseIconEndPaddingResource(@DimenRes int id) {
    setCloseIconEndPadding(context.getResources().getDimension(id));
  }

  public void setCloseIconEndPadding(float closeIconEndPadding) {
    if (this.closeIconEndPadding != closeIconEndPadding) {
      this.closeIconEndPadding = closeIconEndPadding;
      invalidateSelf();
    }
  }

  public float getChipEndPadding() {
    return chipEndPadding;
  }

  public void setChipEndPaddingResource(@DimenRes int id) {
    setChipEndPadding(context.getResources().getDimension(id));
  }

  public void setChipEndPadding(float chipEndPadding) {
    if (this.chipEndPadding != chipEndPadding) {
      this.chipEndPadding = chipEndPadding;
      invalidateSelf();
    }
  }
}
