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
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.animation.MotionSpec;
import android.support.design.resources.MaterialResources;
import android.support.v7.widget.DrawableUtils;
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

  private final Context context;

  // Visuals
  private ColorStateList buttonTint;
  @Nullable private Mode buttonTintMode;
  private float buttonHeight;
  private float cornerRadius;
  private ColorStateList strokeColor;
  private float buttonStrokeWidth;
  private ColorStateList rippleColor;
  private ColorStateList rippleAlpha;

  // Text
  private CharSequence buttonText;
  private TextAppearanceSpan textAppearanceSpan;

  // Icon
  private Drawable icon;
  private float iconSize;

  // Close icon
  private Drawable closeIcon;
  private float closeIconSize;

  // Checkable
  private boolean checkable;
  private Drawable checkedIcon;

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

  public static ChipDrawable createFromAttributes(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    ChipDrawable chip = new ChipDrawable(context);
    chip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return chip;
  }

  private ChipDrawable(Context context) {
    this.context = context;
  }

  private void loadFromAttributes(
      AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.ChipDrawable, defStyleAttr, defStyleRes);

    buttonTint =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_buttonTint);
    buttonTintMode =
        DrawableUtils.parseTintMode(a.getInt(R.styleable.ChipDrawable_buttonTintMode, -1), null);
    buttonHeight = a.getDimension(R.styleable.ChipDrawable_buttonHeight, 0f);
    cornerRadius = a.getDimension(R.styleable.ChipDrawable_cornerRadius, 0f);
    strokeColor =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_strokeColor);
    buttonStrokeWidth = a.getDimension(R.styleable.ChipDrawable_buttonStrokeWidth, 0f);
    rippleColor =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleColor);
    rippleAlpha =
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleAlpha);

    buttonText = a.getText(R.styleable.ChipDrawable_buttonText);
    textAppearanceSpan =
        MaterialResources.getTextAppearanceSpan(
            context, a, R.styleable.ChipDrawable_android_textAppearance);

    icon = MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_icon);
    iconSize = a.getDimension(R.styleable.ChipDrawable_iconSize, 0f);

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

  @Override
  public void draw(@NonNull Canvas canvas) {}

  @Override
  public void setAlpha(int alpha) {}

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {}

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}
