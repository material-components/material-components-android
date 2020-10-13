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

package com.google.android.material.chip;

import com.google.android.material.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.TintAwareDrawable;
import androidx.core.view.ViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.AnimatorRes;
import androidx.annotation.AttrRes;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.XmlRes;
import androidx.core.text.BidiFormatter;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.canvas.CanvasCompat;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.TextDrawableHelper;
import com.google.android.material.internal.TextDrawableHelper.TextDrawableDelegate;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * ChipDrawable contains all the layout and draw logic for {@link Chip}.
 *
 * <p>You can use ChipDrawable directly in contexts that require a Drawable. For example, an
 * auto-complete enabled EditText can replace snippets of text with a ChipDrawable to represent it
 * as a semantic entity. To create an instance of ChipDrawable, use {@link
 * ChipDrawable#createFromResource(Context, int)} and pass in an XML resource in this form:
 *
 * <pre>{@code
 * <chip xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:text="Hello, World!"/>
 * }</pre>
 *
 * <p>The basic attributes you can set are:
 *
 * <ul>
 *   <li>{@link android.R.attr#checkable android:checkable} - If true, the chip can be toggled. If
 *       false, the chip acts like a button.
 *   <li>{@link android.R.attr#text android:text} - Sets the text of the chip.
 *   <li>{@link R.attr#chipIcon app:chipIcon} - Sets the icon of the chip, or use @null to display
 *       no icon. Usually on the left.
 *   <li>{@link R.attr#checkedIcon app:checkedIcon} - Sets a custom icon to use when checked, or
 *       use @null to display no icon. Usually on the left.
 *   <li>{@link R.attr#closeIcon app:closeIcon} - Sets a custom icon that the user can click to
 *       close, or use @null to display no icon. Usually on the right.
 *   <li>{@link android.R.attr#ellipsize} - Does not support {@link
 *       android.text.TextUtils.TruncateAt#MARQUEE} because chip text should not scroll.
 * </ul>
 *
 * <p>When used in this stand-alone mode, the host view must explicitly manage the ChipDrawable's
 * state:
 *
 * <ul>
 *   <li>{@link ChipDrawable#setBounds(int, int, int, int)}, taking into account {@link
 *       ChipDrawable#getIntrinsicHeight()} and {@link ChipDrawable#getIntrinsicWidth()}.
 *   <li>{@link ChipDrawable#draw(Canvas)}
 *   <li>{@link ChipDrawable#setCallback(Callback)}, to support invalidations on the chip drawable
 *       or any of its child drawables. This includes animations.
 *   <li>{@link ChipDrawable#setState(int[])}, to support checking the chip, and
 *       touch/mouse/keyboard interactions on the chip.
 *   <li>{@link ChipDrawable#setCloseIconState(int[])}, to support touch, mouse, or keyboard
 *       interactions on the close icon.
 *   <li>{@link ChipDrawable#setHotspot(float, float)}
 *   <li>{@link ChipDrawable#setLayoutDirection(int)}, to support RTL mode.
 * </ul>
 *
 * <p>ChipDrawable's horizontal layout is as follows:
 *
 * <pre>
 *   chipStartPadding     iconEndPadding     closeIconStartPadding         chipEndPadding
 *    +                    +                                    +                      +
 *    |                    |                                    |                      |
 *    |  iconStartPadding  |  textStartPadding   textEndPadding | closeIconEndPadding  |
 *    |   +                |    +                            +  |                  +   |
 *    |   |                |    |                            |  |                  |   |
 *    v   v                v    v                            v  v                  v   v
 * +-----+----+-----------+----+----+---------------------+----+----+----------+----+-----+
 * |     |    |       XX  |    |    |  XX   X  X  X  XXX  |    |    | X      X |    |     |
 * |     |    |      XX   |    |    | X  X  X  X  X  X  X |    |    |  XX  XX  |    |     |
 * |     |    |  XX XX    |    |    | X     XXXX  X  XXX  |    |    |    XX    |    |     |
 * |     |    |   XXX     |    |    | X  X  X  X  X  X    |    |    |  XX  XX  |    |     |
 * |     |    |    X      |    |    |  XX   X  X  X  X    |    |    | X      X |    |     |
 * +-----+----+-----------+----+----+---------------------+----+----+----------+----+-----+
 *                  ^                           ^                         ^
 *                  |                           |                         |
 *                  +                           +                         +
 *             chipIconSize                  *dynamic*              closeIconSize
 * </pre>
 *
 * <p>ChipDrawable contains three child drawables: {@code chipIcon}, {@code checkedIcon}, and {@code
 * closeIcon}. chipIcon and checkedIcon inherit the state of this drawable, but closeIcon contains
 * its own state that you can set with {@link #setCloseIconState(int[])}.
 *
 * @see Chip
 */
public class ChipDrawable extends MaterialShapeDrawable
    implements TintAwareDrawable, Callback, TextDrawableDelegate {

  private static final boolean DEBUG = false;
  private static final int[] DEFAULT_STATE = new int[] {android.R.attr.state_enabled};
  private static final String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";
  private static final int MAX_CHIP_ICON_HEIGHT = 24; // dp

  private static final ShapeDrawable closeIconRippleMask = new ShapeDrawable(new OvalShape());

  // Visuals
  @Nullable private ColorStateList chipSurfaceColor;
  @Nullable private ColorStateList chipBackgroundColor;
  private float chipMinHeight;
  private float chipCornerRadius = -1;
  @Nullable private ColorStateList chipStrokeColor;
  private float chipStrokeWidth;
  @Nullable private ColorStateList rippleColor;

  // Text
  @Nullable private CharSequence text;

  // Chip icon
  private boolean chipIconVisible;
  @Nullable private Drawable chipIcon;
  @Nullable private ColorStateList chipIconTint;
  private float chipIconSize;
  private boolean hasChipIconTint;

  // Close icon
  private boolean closeIconVisible;
  @Nullable private Drawable closeIcon;
  @Nullable private Drawable closeIconRipple;
  @Nullable private ColorStateList closeIconTint;
  private float closeIconSize;
  @Nullable private CharSequence closeIconContentDescription;

  // Checkable
  private boolean checkable;
  private boolean checkedIconVisible;
  @Nullable private Drawable checkedIcon;
  @Nullable private ColorStateList checkedIconTint;

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

  @NonNull private final Context context;
  private final Paint chipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  @Nullable private final Paint debugPaint;
  private final FontMetrics fontMetrics = new FontMetrics();
  private final RectF rectF = new RectF();
  private final PointF pointF = new PointF();
  private final Path shapePath = new Path();
  @NonNull private final TextDrawableHelper textDrawableHelper;

  @ColorInt private int currentChipSurfaceColor;
  @ColorInt private int currentChipBackgroundColor;
  @ColorInt private int currentCompositeSurfaceBackgroundColor;
  @ColorInt private int currentChipStrokeColor;
  @ColorInt private int currentCompatRippleColor;
  @ColorInt private int currentTextColor;
  private boolean currentChecked;
  @ColorInt private int currentTint;

  private int alpha = 255;
  @Nullable private ColorFilter colorFilter;
  @Nullable private PorterDuffColorFilter tintFilter;
  @Nullable private ColorStateList tint;
  @Nullable private Mode tintMode = Mode.SRC_IN;
  private int[] closeIconStateSet;
  private boolean useCompatRipple;
  @Nullable private ColorStateList compatRippleColor;
  @NonNull private WeakReference<Delegate> delegate = new WeakReference<>(null);
  private TruncateAt truncateAt;
  private boolean shouldDrawText;
  private int maxWidth;
  private boolean isShapeThemingEnabled;

  /** Returns a ChipDrawable from the given attributes. */
  @NonNull
  public static ChipDrawable createFromAttributes(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    ChipDrawable chip = new ChipDrawable(context, attrs, defStyleAttr, defStyleRes);
    chip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return chip;
  }

  /**
   * Returns a ChipDrawable from the given XML resource. All attributes from {@link
   * R.styleable#Chip} and a custom <code>style</code> attribute are supported. A chip resource may
   * look like:
   *
   * <pre>{@code
   * <chip
   *     xmlns:app="http://schemas.android.com/apk/res-auto"
   *     style="@style/Widget.MaterialComponents.Chip.Entry"
   *     app:chipIcon="@drawable/custom_icon"/>
   * }</pre>
   */
  @NonNull
  public static ChipDrawable createFromResource(@NonNull Context context, @XmlRes int id) {
    AttributeSet attrs = DrawableUtils.parseDrawableXml(context, id, "chip");
    @StyleRes int style = attrs.getStyleAttribute();
    if (style == 0) {
      style = R.style.Widget_MaterialComponents_Chip_Entry;
    }
    return createFromAttributes(context, attrs, R.attr.chipStandaloneStyle, style);
  }

  private ChipDrawable(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initializeElevationOverlay(context);

    this.context = context;
    textDrawableHelper = new TextDrawableHelper(/* delegate= */ this);

    text = "";

    textDrawableHelper.getTextPaint().density = context.getResources().getDisplayMetrics().density;
    debugPaint = DEBUG ? new Paint(Paint.ANTI_ALIAS_FLAG) : null;
    if (debugPaint != null) {
      debugPaint.setStyle(Style.STROKE);
    }

    setState(DEFAULT_STATE);
    setCloseIconState(DEFAULT_STATE);
    shouldDrawText = true;

    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
      //noinspection NewApi
      closeIconRippleMask.setTint(Color.WHITE);
    }
  }

  private void loadFromAttributes(
      @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Chip, defStyleAttr, defStyleRes);

    isShapeThemingEnabled = a.hasValue(R.styleable.Chip_shapeAppearance);
    setChipSurfaceColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipSurfaceColor));
    setChipBackgroundColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipBackgroundColor));
    setChipMinHeight(a.getDimension(R.styleable.Chip_chipMinHeight, 0f));
    if (a.hasValue(R.styleable.Chip_chipCornerRadius)) {
      setChipCornerRadius(a.getDimension(R.styleable.Chip_chipCornerRadius, 0f));
    }
    setChipStrokeColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipStrokeColor));
    setChipStrokeWidth(a.getDimension(R.styleable.Chip_chipStrokeWidth, 0f));
    setRippleColor(MaterialResources.getColorStateList(context, a, R.styleable.Chip_rippleColor));

    setText(a.getText(R.styleable.Chip_android_text));
    TextAppearance textAppearance =
        MaterialResources.getTextAppearance(context, a, R.styleable.Chip_android_textAppearance);
    float textSize = a.getDimension(R.styleable.Chip_android_textSize, textAppearance.textSize);
    textAppearance.textSize = textSize;
    setTextAppearance(textAppearance);

    int ellipsize = a.getInt(R.styleable.Chip_android_ellipsize, 0);
    // Convert to supported TextUtils.TruncateAt values
    switch (ellipsize) {
      case 1:
        setEllipsize(TextUtils.TruncateAt.START);
        break;
      case 2:
        setEllipsize(TextUtils.TruncateAt.MIDDLE);
        break;
      case 3:
        setEllipsize(TextUtils.TruncateAt.END);
        break;
      case 4: // fall through
        // Does not support TextUtils.TruncateAt.MARQUEE, chip text should not scroll.
      default: // fall out
        break;
    }

    setChipIconVisible(a.getBoolean(R.styleable.Chip_chipIconVisible, false));
    // If the user explicitly sets the deprecated attribute (chipIconEnabled) but NOT the
    // replacement attribute (chipIconVisible), use the value specified in the deprecated attribute.
    if (attrs != null
        && attrs.getAttributeValue(NAMESPACE_APP, "chipIconEnabled") != null
        && attrs.getAttributeValue(NAMESPACE_APP, "chipIconVisible") == null) {
      setChipIconVisible(a.getBoolean(R.styleable.Chip_chipIconEnabled, false));
    }
    setChipIcon(MaterialResources.getDrawable(context, a, R.styleable.Chip_chipIcon));
    if (a.hasValue(R.styleable.Chip_chipIconTint)) {
      setChipIconTint(
          MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipIconTint));
    }
    setChipIconSize(a.getDimension(R.styleable.Chip_chipIconSize, -1f));

    setCloseIconVisible(a.getBoolean(R.styleable.Chip_closeIconVisible, false));
    // If the user explicitly sets the deprecated attribute (closeIconEnabled) but NOT the
    // replacement attribute (closeIconVisible), use the value specified in the deprecated
    // attribute.
    if (attrs != null
        && attrs.getAttributeValue(NAMESPACE_APP, "closeIconEnabled") != null
        && attrs.getAttributeValue(NAMESPACE_APP, "closeIconVisible") == null) {
      setCloseIconVisible(a.getBoolean(R.styleable.Chip_closeIconEnabled, false));
    }
    setCloseIcon(MaterialResources.getDrawable(context, a, R.styleable.Chip_closeIcon));
    setCloseIconTint(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_closeIconTint));
    setCloseIconSize(a.getDimension(R.styleable.Chip_closeIconSize, 0f));

    setCheckable(a.getBoolean(R.styleable.Chip_android_checkable, false));
    setCheckedIconVisible(a.getBoolean(R.styleable.Chip_checkedIconVisible, false));
    // If the user explicitly sets the deprecated attribute (checkedIconEnabled) but NOT the
    // replacement attribute (checkedIconVisible), use the value specified in the deprecated
    // attribute.
    if (attrs != null
        && attrs.getAttributeValue(NAMESPACE_APP, "checkedIconEnabled") != null
        && attrs.getAttributeValue(NAMESPACE_APP, "checkedIconVisible") == null) {
      setCheckedIconVisible(a.getBoolean(R.styleable.Chip_checkedIconEnabled, false));
    }
    setCheckedIcon(MaterialResources.getDrawable(context, a, R.styleable.Chip_checkedIcon));
    if (a.hasValue(R.styleable.Chip_checkedIconTint)) {
      setCheckedIconTint(
          MaterialResources.getColorStateList(context, a, R.styleable.Chip_checkedIconTint));
    }

    setShowMotionSpec(MotionSpec.createFromAttribute(context, a, R.styleable.Chip_showMotionSpec));
    setHideMotionSpec(MotionSpec.createFromAttribute(context, a, R.styleable.Chip_hideMotionSpec));

    setChipStartPadding(a.getDimension(R.styleable.Chip_chipStartPadding, 0f));
    setIconStartPadding(a.getDimension(R.styleable.Chip_iconStartPadding, 0f));
    setIconEndPadding(a.getDimension(R.styleable.Chip_iconEndPadding, 0f));
    setTextStartPadding(a.getDimension(R.styleable.Chip_textStartPadding, 0f));
    setTextEndPadding(a.getDimension(R.styleable.Chip_textEndPadding, 0f));
    setCloseIconStartPadding(a.getDimension(R.styleable.Chip_closeIconStartPadding, 0f));
    setCloseIconEndPadding(a.getDimension(R.styleable.Chip_closeIconEndPadding, 0f));
    setChipEndPadding(a.getDimension(R.styleable.Chip_chipEndPadding, 0f));

    setMaxWidth(a.getDimensionPixelSize(R.styleable.Chip_android_maxWidth, Integer.MAX_VALUE));

    a.recycle();
  }

  /** Sets whether this ChipDrawable should draw its own compatibility ripples. */
  public void setUseCompatRipple(boolean useCompatRipple) {
    if (this.useCompatRipple != useCompatRipple) {
      this.useCompatRipple = useCompatRipple;
      updateCompatRippleColor();
      onStateChange(getState());
    }
  }

  /** Returns whether this ChipDrawable should draw its own compatibility ripples. */
  public boolean getUseCompatRipple() {
    return useCompatRipple;
  }

  /** Sets the View delegate that owns this ChipDrawable. */
  public void setDelegate(@Nullable Delegate delegate) {
    this.delegate = new WeakReference<>(delegate);
  }

  /** Attempts to call {@link Delegate#onChipDrawableSizeChange()} on the delegate. */
  protected void onSizeChange() {
    Delegate delegate = this.delegate.get();
    if (delegate != null) {
      delegate.onChipDrawableSizeChange();
    }
  }

  /**
   * Returns the chip's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  public void getChipTouchBounds(@NonNull RectF bounds) {
    calculateChipTouchBounds(getBounds(), bounds);
  }

  /**
   * Returns the close icon's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  public void getCloseIconTouchBounds(@NonNull RectF bounds) {
    calculateCloseIconTouchBounds(getBounds(), bounds);
  }

  /** Returns the width at which the chip would like to be laid out. */
  @Override
  public int getIntrinsicWidth() {
    int calculatedWidth =
        Math.round(
            (chipStartPadding
                + calculateChipIconWidth()
                + textStartPadding
                + textDrawableHelper.getTextWidth(getText().toString())
                + textEndPadding
                + calculateCloseIconWidth()
                + chipEndPadding));
    return Math.min(calculatedWidth, maxWidth);
  }

  /** Returns the height at which the chip would like to be laid out. */
  @Override
  public int getIntrinsicHeight() {
    return (int) chipMinHeight;
  }

  /** Returns whether we will show the chip icon. */
  private boolean showsChipIcon() {
    return chipIconVisible && chipIcon != null;
  }

  /** Returns whether we will show the checked icon. */
  private boolean showsCheckedIcon() {
    return checkedIconVisible && checkedIcon != null && currentChecked;
  }

  /** Returns whether we will show the close icon. */
  private boolean showsCloseIcon() {
    return closeIconVisible && closeIcon != null;
  }

  /** Returns whether we can show the checked icon if our drawable state changes. */
  private boolean canShowCheckedIcon() {
    return checkedIconVisible && checkedIcon != null && checkable;
  }

  /** Returns the width of the chip icon plus padding, which only apply if the chip icon exists. */
  float calculateChipIconWidth() {
    if (showsChipIcon() || (showsCheckedIcon())) {
      return iconStartPadding + getCurrentChipIconWidth() + iconEndPadding;
    }
    return 0f;
  }

  /**
   * Return the chip's leading icon width. If chipIconSize > 0, return chipIconSize, else, return
   * the current icon drawable width.
   */
  private float getCurrentChipIconWidth() {
    Drawable iconDrawable = currentChecked ? checkedIcon : chipIcon;
    if (chipIconSize <= 0 && iconDrawable != null) {
      return iconDrawable.getIntrinsicWidth();
    }
    return chipIconSize;
  }

  /**
   * Return the chip's leading icon height. If chipIconSize > 0, return chipIconSize, else, return
   * the current icon drawable height up to MAX_CHIP_ICON_HEIGHT
   */
  private float getCurrentChipIconHeight() {
    Drawable icon = currentChecked ? checkedIcon : chipIcon;
    if (chipIconSize <= 0 && icon != null) {
      float maxChipIconHeight = (float) Math.ceil(ViewUtils.dpToPx(context, MAX_CHIP_ICON_HEIGHT));
      if (icon.getIntrinsicHeight() <= maxChipIconHeight) {
        return icon.getIntrinsicHeight();
      } else {
        return maxChipIconHeight;
      }
    }
    return chipIconSize;
  }

  /**
   * Returns the width of the chip close icon plus padding, which only apply if the chip close icon
   * exists.
   */
  float calculateCloseIconWidth() {
    if (showsCloseIcon()) {
      return closeIconStartPadding + closeIconSize + closeIconEndPadding;
    }
    return 0f;
  }

  boolean isShapeThemingEnabled() {
    return isShapeThemingEnabled;
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

    // 0. Draw 100% opaque surface layer underneath partial transparent background.
    drawChipSurface(canvas, bounds);

    // 1. Draw chip background.
    drawChipBackground(canvas, bounds);

    if (isShapeThemingEnabled) {
      super.draw(canvas);
    }
    // 2. Draw chip stroke.
    drawChipStroke(canvas, bounds);

    // 3. Draw compat ripple.
    drawCompatRipple(canvas, bounds);

    // 4. Draw chip icon.
    drawChipIcon(canvas, bounds);

    // 5. Draw checked icon.
    drawCheckedIcon(canvas, bounds);

    // 6. Draw chip text.
    if (shouldDrawText) {
      drawText(canvas, bounds);
    }

    // 7. Draw close icon.
    drawCloseIcon(canvas, bounds);

    // Debug.
    drawDebug(canvas, bounds);

    if (alpha < 255) {
      canvas.restoreToCount(saveCount);
    }
  }

  private void drawChipSurface(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (!isShapeThemingEnabled) {
      chipPaint.setColor(currentChipSurfaceColor);
      chipPaint.setStyle(Style.FILL);
      rectF.set(bounds);
      canvas.drawRoundRect(rectF, getChipCornerRadius(), getChipCornerRadius(), chipPaint);
    }
  }

  private void drawChipBackground(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (!isShapeThemingEnabled) {
      chipPaint.setColor(currentChipBackgroundColor);
      chipPaint.setStyle(Style.FILL);
      chipPaint.setColorFilter(getTintColorFilter());
      rectF.set(bounds);
      canvas.drawRoundRect(rectF, getChipCornerRadius(), getChipCornerRadius(), chipPaint);
    }
  }

  /**
   * Draws the chip stroke. Draw the stroke <code>chipStrokeWidth / 2f</code> away from the edges so
   * that the stroke perfectly fills the bounds of the chip.
   */
  private void drawChipStroke(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (chipStrokeWidth > 0 && !isShapeThemingEnabled) {
      chipPaint.setColor(currentChipStrokeColor);
      chipPaint.setStyle(Style.STROKE);
      if (!isShapeThemingEnabled) {
        chipPaint.setColorFilter(getTintColorFilter());
      }
      rectF.set(
          bounds.left + chipStrokeWidth / 2f,
          bounds.top + chipStrokeWidth / 2f,
          bounds.right - chipStrokeWidth / 2f,
          bounds.bottom - chipStrokeWidth / 2f);
      // We need to adjust stroke's corner radius so that the corners of the background are not
      // drawn outside stroke
      float strokeCornerRadius = chipCornerRadius - chipStrokeWidth / 2f;
      canvas.drawRoundRect(rectF, strokeCornerRadius, strokeCornerRadius, chipPaint);
    }
  }

  private void drawCompatRipple(@NonNull Canvas canvas, @NonNull Rect bounds) {
    chipPaint.setColor(currentCompatRippleColor);
    chipPaint.setStyle(Style.FILL);
    rectF.set(bounds);
    if (!isShapeThemingEnabled) {
      canvas.drawRoundRect(rectF, getChipCornerRadius(), getChipCornerRadius(), chipPaint);
    } else {
      calculatePathForSize(new RectF(bounds), shapePath);
      super.drawShape(canvas, chipPaint, shapePath, getBoundsAsRectF());
    }
  }

  private void drawChipIcon(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (showsChipIcon()) {
      calculateChipIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      chipIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());
      chipIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  private void drawCheckedIcon(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (showsCheckedIcon()) {
      calculateChipIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      checkedIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());
      checkedIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  /** Draws the chip text, which should appear centered vertically in the chip. */
  private void drawText(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (text != null) {
      Align align = calculateTextOriginAndAlignment(bounds, pointF);
      // If bounds are smaller than intrinsic size. Ellipsize or clip the text depending on
      // ellipsize attribute.
      calculateTextBounds(bounds, rectF);

      if (textDrawableHelper.getTextAppearance() != null) {
        textDrawableHelper.getTextPaint().drawableState = getState();
        textDrawableHelper.updateTextPaintDrawState(context);
      }
      textDrawableHelper.getTextPaint().setTextAlign(align);

      boolean clip =
          Math.round(textDrawableHelper.getTextWidth(getText().toString()))
              > Math.round(rectF.width());
      int saveCount = 0;
      if (clip) {
        saveCount = canvas.save();
        canvas.clipRect(rectF);
      }

      CharSequence finalText = text;
      if (clip && truncateAt != null) {
        finalText =
            TextUtils.ellipsize(text, textDrawableHelper.getTextPaint(), rectF.width(), truncateAt);
      }
      canvas.drawText(
          finalText, 0, finalText.length(), pointF.x, pointF.y, textDrawableHelper.getTextPaint());
      if (clip) {
        canvas.restoreToCount(saveCount);
      }
    }
  }

  private void drawCloseIcon(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (showsCloseIcon()) {
      calculateCloseIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      closeIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());

      if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
        closeIconRipple.setBounds(closeIcon.getBounds());
        closeIconRipple.jumpToCurrentState();
        closeIconRipple.draw(canvas);
      } else {
        closeIcon.draw(canvas);
      }

      canvas.translate(-tx, -ty);
    }
  }

  private void drawDebug(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (debugPaint != null) {
      debugPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 255 / 2));

      // Background.
      canvas.drawRect(bounds, debugPaint);

      // Chip and checked icon.
      if (showsChipIcon() || (showsCheckedIcon())) {
        calculateChipIconBounds(bounds, rectF);
        canvas.drawRect(rectF, debugPaint);
      }

      // Chip text.
      if (text != null) {
        canvas.drawLine(
            bounds.left, bounds.exactCenterY(), bounds.right, bounds.exactCenterY(), debugPaint);
      }

      // Close icon.
      if (showsCloseIcon()) {
        calculateCloseIconBounds(bounds, rectF);
        canvas.drawRect(rectF, debugPaint);
      }

      // Chip touch bounds.
      debugPaint.setColor(ColorUtils.setAlphaComponent(Color.RED, 255 / 2));
      calculateChipTouchBounds(bounds, rectF);
      canvas.drawRect(rectF, debugPaint);

      // Close icon touch bounds.
      debugPaint.setColor(ColorUtils.setAlphaComponent(Color.GREEN, 255 / 2));
      calculateCloseIconTouchBounds(bounds, rectF);
      canvas.drawRect(rectF, debugPaint);
    }
  }

  /**
   * Calculates the chip icon's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  private void calculateChipIconBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (showsChipIcon() || showsCheckedIcon()) {
      float offsetFromStart = chipStartPadding + iconStartPadding;
      float chipWidth = getCurrentChipIconWidth();

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.left = bounds.left + offsetFromStart;
        outBounds.right = outBounds.left + chipWidth;
      } else {
        outBounds.right = bounds.right - offsetFromStart;
        outBounds.left = outBounds.right - chipWidth;
      }

      float chipHeight = getCurrentChipIconHeight();
      outBounds.top = bounds.exactCenterY() - chipHeight / 2f;
      outBounds.bottom = outBounds.top + chipHeight;
    }
  }

  /** Calculates the chip text's origin and alignment based on the ChipDrawable-absolute bounds. */
  @NonNull
  Align calculateTextOriginAndAlignment(@NonNull Rect bounds, @NonNull PointF pointF) {
    pointF.set(0, 0);
    Align align = Align.LEFT;

    if (text != null) {
      float offsetFromStart = chipStartPadding + calculateChipIconWidth() + textStartPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        pointF.x = bounds.left + offsetFromStart;
        align = Align.LEFT;
      } else {
        pointF.x = bounds.right - offsetFromStart;
        align = Align.RIGHT;
      }

      pointF.y = bounds.centerY() - calculateTextCenterFromBaseline();
    }

    return align;
  }

  /**
   * Calculates the offset from the visual center of the chip text to its baseline.
   *
   * <p>To draw the chip text, we provide the origin to {@link Canvas#drawText(CharSequence, int,
   * int, float, float, Paint)}. This origin always corresponds vertically to the text's baseline.
   * Because we need to vertically center the text, we need to calculate this offset.
   *
   * <p>Note that chips that share the same font must have consistent text baselines despite having
   * different text strings. This is why we calculate the vertical center using {@link
   * Paint#getFontMetrics(FontMetrics)} rather than {@link Paint#getTextBounds(String, int, int,
   * Rect)}.
   */
  private float calculateTextCenterFromBaseline() {
    textDrawableHelper.getTextPaint().getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }

  /**
   * Calculates the chip text's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  private void calculateTextBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (text != null) {
      float offsetFromStart = chipStartPadding + calculateChipIconWidth() + textStartPadding;
      float offsetFromEnd = chipEndPadding + calculateCloseIconWidth() + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.left = bounds.left + offsetFromStart;
        outBounds.right = bounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
        outBounds.right = bounds.right - offsetFromStart;
      }

      // Top and bottom included for completion. Don't position the chip text vertically based on
      // these bounds. Instead, use #calculateTextOriginAndAlignment().
      outBounds.top = bounds.top;
      outBounds.bottom = bounds.bottom;
    }
  }

  /**
   * Calculates the close icon's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  private void calculateCloseIconBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (showsCloseIcon()) {
      float offsetFromEnd = chipEndPadding + closeIconEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right - offsetFromEnd;
        outBounds.left = outBounds.right - closeIconSize;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
        outBounds.right = outBounds.left + closeIconSize;
      }

      outBounds.top = bounds.exactCenterY() - closeIconSize / 2f;
      outBounds.bottom = outBounds.top + closeIconSize;
    }
  }

  private void calculateChipTouchBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.set(bounds);

    if (showsCloseIcon()) {
      float offsetFromEnd =
          chipEndPadding
              + closeIconEndPadding
              + closeIconSize
              + closeIconStartPadding
              + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
      }
    }
  }

  private void calculateCloseIconTouchBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (showsCloseIcon()) {
      float offsetFromEnd =
          chipEndPadding
              + closeIconEndPadding
              + closeIconSize
              + closeIconStartPadding
              + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right;
        outBounds.left = outBounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left;
        outBounds.right = bounds.left + offsetFromEnd;
      }

      outBounds.top = bounds.top;
      outBounds.bottom = bounds.bottom;
    }
  }

  /** Indicates whether this chip drawable will change its appearance based on state. */
  @Override
  public boolean isStateful() {
    // The logic here and #isCloseIconStateful() must match #onStateChange(int[],int[]).
    return isStateful(chipSurfaceColor)
        || isStateful(chipBackgroundColor)
        || isStateful(chipStrokeColor)
        || (useCompatRipple && isStateful(compatRippleColor))
        || isStateful(textDrawableHelper.getTextAppearance())
        || canShowCheckedIcon()
        || isStateful(chipIcon)
        || isStateful(checkedIcon)
        || isStateful(tint);
  }

  /** Indicates whether the close icon drawable will change its appearance based on state. */
  public boolean isCloseIconStateful() {
    // The logic here and #isStateful() must match #onStateChange(int[], int[]).
    return isStateful(closeIcon);
  }

  /**
   * Specify a set of states for the close icon. This is a separate state set than the one used for
   * the rest of the chip.
   */
  public boolean setCloseIconState(@NonNull int[] stateSet) {
    if (!Arrays.equals(closeIconStateSet, stateSet)) {
      closeIconStateSet = stateSet;
      if (showsCloseIcon()) {
        return onStateChange(getState(), stateSet);
      }
    }
    return false;
  }

  /** Describes the current state of the close icon. */
  @NonNull
  public int[] getCloseIconState() {
    return closeIconStateSet;
  }

  @Override
  public void onTextSizeChange() {
    onSizeChange();
    invalidateSelf();
  }

  @Override
  public boolean onStateChange(@NonNull int[] state) {
    if (isShapeThemingEnabled) {
      super.onStateChange(state);
    }
    return onStateChange(state, getCloseIconState());
  }

  /** Changes appearance in response to the specified state. */
  private boolean onStateChange(@NonNull int[] chipState, @NonNull int[] closeIconState) {
    // The logic here must match #isStateful()} and #isCloseIconStateful()}.
    boolean invalidate = super.onStateChange(chipState);
    boolean sizeChanged = false;

    int newChipSurfaceColor =
        chipSurfaceColor != null
            ? chipSurfaceColor.getColorForState(chipState, currentChipSurfaceColor)
            : 0;
    newChipSurfaceColor = compositeElevationOverlayIfNeeded(newChipSurfaceColor);
    if (currentChipSurfaceColor != newChipSurfaceColor) {
      currentChipSurfaceColor = newChipSurfaceColor;
      invalidate = true;
    }

    int newChipBackgroundColor =
        chipBackgroundColor != null
            ? chipBackgroundColor.getColorForState(chipState, currentChipBackgroundColor)
            : 0;
    newChipBackgroundColor = compositeElevationOverlayIfNeeded(newChipBackgroundColor);
    if (currentChipBackgroundColor != newChipBackgroundColor) {
      currentChipBackgroundColor = newChipBackgroundColor;
      invalidate = true;
    }

    int newCompositeSurfaceBackgroundColor =
        MaterialColors.layer(newChipSurfaceColor, newChipBackgroundColor);
    boolean shouldUpdate =
        currentCompositeSurfaceBackgroundColor != newCompositeSurfaceBackgroundColor;
    shouldUpdate |= getFillColor() == null;
    if (shouldUpdate) {
      currentCompositeSurfaceBackgroundColor = newCompositeSurfaceBackgroundColor;
      setFillColor(ColorStateList.valueOf(currentCompositeSurfaceBackgroundColor));
      invalidate = true;
    }

    int newChipStrokeColor =
        chipStrokeColor != null
            ? chipStrokeColor.getColorForState(chipState, currentChipStrokeColor)
            : 0;
    if (currentChipStrokeColor != newChipStrokeColor) {
      currentChipStrokeColor = newChipStrokeColor;
      invalidate = true;
    }

    int newCompatRippleColor =
        compatRippleColor != null && RippleUtils.shouldDrawRippleCompat(chipState)
            ? compatRippleColor.getColorForState(chipState, currentCompatRippleColor)
            : 0;
    if (currentCompatRippleColor != newCompatRippleColor) {
      currentCompatRippleColor = newCompatRippleColor;
      if (useCompatRipple) {
        invalidate = true;
      }
    }

    int newTextColor =
        textDrawableHelper.getTextAppearance() != null
                && textDrawableHelper.getTextAppearance().textColor != null
            ? textDrawableHelper
                .getTextAppearance()
                .textColor
                .getColorForState(chipState, currentTextColor)
            : 0;
    if (currentTextColor != newTextColor) {
      currentTextColor = newTextColor;
      invalidate = true;
    }

    boolean newChecked = hasState(getState(), android.R.attr.state_checked) && checkable;
    if (currentChecked != newChecked && checkedIcon != null) {
      float oldChipIconWidth = calculateChipIconWidth();
      currentChecked = newChecked;
      float newChipIconWidth = calculateChipIconWidth();
      invalidate = true;

      if (oldChipIconWidth != newChipIconWidth) {
        sizeChanged = true;
      }
    }

    int newTint = tint != null ? tint.getColorForState(chipState, currentTint) : 0;
    if (currentTint != newTint) {
      currentTint = newTint;
      tintFilter = DrawableUtils.updateTintFilter(this, tint, tintMode);
      invalidate = true;
    }

    if (isStateful(chipIcon)) {
      invalidate |= chipIcon.setState(chipState);
    }
    if (isStateful(checkedIcon)) {
      invalidate |= checkedIcon.setState(chipState);
    }
    if (isStateful(closeIcon)) {
      // Merge the chipState and closeIconState so that when the Chip is pressed, focused, or
      // hovered, the close icon acts like it is pressed, focused, or hovered. Do not use the merged
      // state for the closeIconRipple as that should only behave pressed, focused, or hovered when
      // the closeIcon is pressed, focused, or hovered.
      int[] closeIconMergedState = new int[chipState.length + closeIconState.length];
      System.arraycopy(chipState, 0, closeIconMergedState, 0, chipState.length);
      System.arraycopy(
          closeIconState, 0, closeIconMergedState, chipState.length, closeIconState.length);
      invalidate |= closeIcon.setState(closeIconMergedState);
    }
    //noinspection NewApi
    if (RippleUtils.USE_FRAMEWORK_RIPPLE && isStateful(closeIconRipple)) {
      invalidate |= closeIconRipple.setState(closeIconState);
    }

    if (invalidate) {
      invalidateSelf();
    }
    if (sizeChanged) {
      onSizeChange();
    }
    return invalidate;
  }

  private static boolean isStateful(@Nullable ColorStateList colorStateList) {
    return colorStateList != null && colorStateList.isStateful();
  }

  private static boolean isStateful(@Nullable Drawable drawable) {
    return drawable != null && drawable.isStateful();
  }

  private static boolean isStateful(@Nullable TextAppearance textAppearance) {
    return textAppearance != null
        && textAppearance.textColor != null
        && textAppearance.textColor.isStateful();
  }

  @Override
  public boolean onLayoutDirectionChanged(int layoutDirection) {
    boolean invalidate = super.onLayoutDirectionChanged(layoutDirection);

    if (showsChipIcon()) {
      invalidate |= DrawableCompat.setLayoutDirection(chipIcon, layoutDirection);
    }
    if (showsCheckedIcon()) {
      invalidate |= DrawableCompat.setLayoutDirection(checkedIcon, layoutDirection);
    }
    if (showsCloseIcon()) {
      invalidate |= DrawableCompat.setLayoutDirection(closeIcon, layoutDirection);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return true;
  }

  @Override
  protected boolean onLevelChange(int level) {
    boolean invalidate = super.onLevelChange(level);

    if (showsChipIcon()) {
      invalidate |= chipIcon.setLevel(level);
    }
    if (showsCheckedIcon()) {
      invalidate |= checkedIcon.setLevel(level);
    }
    if (showsCloseIcon()) {
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

    if (showsChipIcon()) {
      invalidate |= chipIcon.setVisible(visible, restart);
    }
    if (showsCheckedIcon()) {
      invalidate |= checkedIcon.setVisible(visible, restart);
    }
    if (showsCloseIcon()) {
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
    if (this.colorFilter != colorFilter) {
      this.colorFilter = colorFilter;
      invalidateSelf();
    }
  }

  @Nullable
  @Override
  public ColorFilter getColorFilter() {
    return colorFilter;
  }

  @Override
  public void setTintList(@Nullable ColorStateList tint) {
    if (this.tint != tint) {
      this.tint = tint;
      onStateChange(getState());
    }
  }

  @Override
  public void setTintMode(@NonNull Mode tintMode) {
    if (this.tintMode != tintMode) {
      this.tintMode = tintMode;
      tintFilter = DrawableUtils.updateTintFilter(this, tint, tintMode);
      invalidateSelf();
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public void getOutline(@NonNull Outline outline) {
    if (isShapeThemingEnabled) {
      super.getOutline(outline);
      return;
    }
    Rect bounds = getBounds();
    if (!bounds.isEmpty()) {
      outline.setRoundRect(bounds, chipCornerRadius);
    } else {
      outline.setRoundRect(0, 0, getIntrinsicWidth(), getIntrinsicHeight(), chipCornerRadius);
    }

    outline.setAlpha(getAlpha() / 255f);
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.invalidateDrawable(this);
    }
  }

  @Override
  public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.scheduleDrawable(this, what, when);
    }
  }

  @Override
  public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.unscheduleDrawable(this, what);
    }
  }

  private void unapplyChildDrawable(@Nullable Drawable drawable) {
    if (drawable != null) {
      drawable.setCallback(null);
    }
  }

  /** Note: This should not change the size of the drawable. */
  private void applyChildDrawable(@Nullable Drawable drawable) {
    if (drawable == null) {
      return;
    }
    drawable.setCallback(this);
    DrawableCompat.setLayoutDirection(drawable, DrawableCompat.getLayoutDirection(this));
    drawable.setLevel(getLevel());
    drawable.setVisible(isVisible(), false);

    if (drawable == closeIcon) {
      if (drawable.isStateful()) {
        drawable.setState(getCloseIconState());
      }
      DrawableCompat.setTintList(drawable, closeIconTint);
      return;
    }
    if (drawable.isStateful()) {
      drawable.setState(getState());
    }
    if (drawable == chipIcon && hasChipIconTint) {
      DrawableCompat.setTintList(chipIcon, chipIconTint);
    }
  }

  /**
   * Returns the color filter used for tinting this ChipDrawable. {@link
   * #setColorFilter(ColorFilter)} takes priority over {@link #setTintList(ColorStateList)}.
   */
  @Nullable
  private ColorFilter getTintColorFilter() {
    return colorFilter != null ? colorFilter : tintFilter;
  }

  private void updateCompatRippleColor() {
    compatRippleColor =
        useCompatRipple ? RippleUtils.sanitizeRippleDrawableColor(rippleColor) : null;
  }

  private void setChipSurfaceColor(@Nullable ColorStateList chipSurfaceColor) {
    if (this.chipSurfaceColor != chipSurfaceColor) {
      this.chipSurfaceColor = chipSurfaceColor;
      onStateChange(getState());
    }
  }

  /** Returns whether the drawable state set contains the given state. */
  private static boolean hasState(@Nullable int[] stateSet, @AttrRes int state) {
    if (stateSet == null) {
      return false;
    }

    for (int s : stateSet) {
      if (s == state) {
        return true;
      }
    }
    return false;
  }

  public void setTextSize(@Dimension float size) {
    TextAppearance textAppearance = getTextAppearance();
    if (textAppearance != null) {
      textAppearance.textSize = size;
      textDrawableHelper.getTextPaint().setTextSize(size);
      onTextSizeChange();
    }
  }

  /** Delegate interface to be implemented by Views that own a ChipDrawable. */
  public interface Delegate {

    /** Handles a change in the ChipDrawable's size. */
    void onChipDrawableSizeChange();
  }

  // Getters and setters for attributes.

  /**
   * Returns this chip's background color.
   *
   * @see #setChipBackgroundColor(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Chip_chipBackgroundColor
   */
  @Nullable
  public ColorStateList getChipBackgroundColor() {
    return chipBackgroundColor;
  }

  /**
   * Sets this chip's background color using a resource id.
   *
   * @param id The resource id of this chip's background color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipBackgroundColor
   */
  public void setChipBackgroundColorResource(@ColorRes int id) {
    setChipBackgroundColor(AppCompatResources.getColorStateList(context, id));
  }

  /**
   * Sets this chip's background color.
   *
   * @param chipBackgroundColor This chip's background color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipBackgroundColor
   */
  public void setChipBackgroundColor(@Nullable ColorStateList chipBackgroundColor) {
    if (this.chipBackgroundColor != chipBackgroundColor) {
      this.chipBackgroundColor = chipBackgroundColor;
      onStateChange(getState());
    }
  }

  /**
   * Returns this chip's minimum height.
   *
   * @see #setChipMinHeight(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipMinHeight
   */
  public float getChipMinHeight() {
    return chipMinHeight;
  }

  /**
   * Sets this chip's minimum height using a resource id.
   *
   * @param id The resource id of this chip's mininum height.
   * @attr ref com.google.android.material.R.styleable#Chip_chipMinHeight
   */
  public void setChipMinHeightResource(@DimenRes int id) {
    setChipMinHeight(context.getResources().getDimension(id));
  }

  /**
   * Sets this chip's minimum height.
   *
   * @param chipMinHeight This chip's mininum height.
   * @attr ref com.google.android.material.R.styleable#Chip_chipMinHeight
   */
  public void setChipMinHeight(float chipMinHeight) {
    if (this.chipMinHeight != chipMinHeight) {
      this.chipMinHeight = chipMinHeight;
      invalidateSelf();
      onSizeChange();
    }
  }

  /**
   * Returns this chip's corner radius.
   *
   * @see #setChipCornerRadius(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipCornerRadius
   */
  public float getChipCornerRadius() {
    return isShapeThemingEnabled ? getTopLeftCornerResolvedSize() : chipCornerRadius;
  }

  /**
   * @deprecated call {@link ShapeAppearanceModel#withCornerSize(float)} or call {@link
   *     ShapeAppearanceModel#toBuilder()} on the {@link #getShapeAppearanceModel()}, modify the
   *     shape using the builder and then call {@link
   *     #setShapeAppearanceModel(ShapeAppearanceModel)}.
   */
  @Deprecated
  public void setChipCornerRadiusResource(@DimenRes int id) {
    setChipCornerRadius(context.getResources().getDimension(id));
  }

  /**
   * @deprecated call {@link ShapeAppearanceModel#withCornerSize(float)} or call {@link
   *     ShapeAppearanceModel#toBuilder()} on the {@link #getShapeAppearanceModel()}, modify the
   *     shape using the builder and then call {@link
   *     #setShapeAppearanceModel(ShapeAppearanceModel)}.
   */
  @Deprecated
  public void setChipCornerRadius(float chipCornerRadius) {
    if (this.chipCornerRadius != chipCornerRadius) {
      this.chipCornerRadius = chipCornerRadius;

      setShapeAppearanceModel(getShapeAppearanceModel().withCornerSize(chipCornerRadius));
    }
  }

  /**
   * Returns this chip's stroke color.
   *
   * @see #setChipStrokeColor(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeColor
   */
  @Nullable
  public ColorStateList getChipStrokeColor() {
    return chipStrokeColor;
  }

  /**
   * Sets this chip's stroke color using a resource id.
   *
   * @param id The resource id of this chip's stroke color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeColor
   */
  public void setChipStrokeColorResource(@ColorRes int id) {
    setChipStrokeColor(AppCompatResources.getColorStateList(context, id));
  }

  /**
   * Sets this chip's stroke color.
   *
   * @param chipStrokeColor This chip's stroke color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeColor
   */
  public void setChipStrokeColor(@Nullable ColorStateList chipStrokeColor) {
    if (this.chipStrokeColor != chipStrokeColor) {
      this.chipStrokeColor = chipStrokeColor;
      if (isShapeThemingEnabled) {
        setStrokeColor(chipStrokeColor);
      }
      onStateChange(getState());
    }
  }

  /**
   * Returns this chip's stroke width.
   *
   * @see #setChipStrokeWidth(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeWidth
   */
  public float getChipStrokeWidth() {
    return chipStrokeWidth;
  }

  /**
   * Sets this chip's stroke width using a resource id.
   *
   * @param id The resource id of this chip's stroke width.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeWidth
   */
  public void setChipStrokeWidthResource(@DimenRes int id) {
    setChipStrokeWidth(context.getResources().getDimension(id));
  }

  /**
   * Sets this chip's stroke width.
   *
   * @param chipStrokeWidth This chip's stroke width.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeWidth
   */
  public void setChipStrokeWidth(float chipStrokeWidth) {
    if (this.chipStrokeWidth != chipStrokeWidth) {
      this.chipStrokeWidth = chipStrokeWidth;

      chipPaint.setStrokeWidth(chipStrokeWidth);
      if (isShapeThemingEnabled) {
        super.setStrokeWidth(chipStrokeWidth);
      }
      invalidateSelf();
    }
  }

  /**
   * Returns this chip's ripple color.
   *
   * @see #setRippleColor(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Chip_rippleColor
   */
  @Nullable
  public ColorStateList getRippleColor() {
    return rippleColor;
  }

  /**
   * Sets this chip's ripple color using a resource id.
   *
   * @param id The resource id of this chip's ripple color.
   * @attr ref com.google.android.material.R.styleable#Chip_rippleColor
   */
  public void setRippleColorResource(@ColorRes int id) {
    setRippleColor(AppCompatResources.getColorStateList(context, id));
  }

  /**
   * Sets this chip's ripple color.
   *
   * @param rippleColor This chip's ripple color.
   * @attr ref com.google.android.material.R.styleable#Chip_rippleColor
   */
  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (this.rippleColor != rippleColor) {
      this.rippleColor = rippleColor;
      updateCompatRippleColor();
      onStateChange(getState());
    }
  }

  @Nullable
  public CharSequence getText() {
    return text;
  }

  public void setTextResource(@StringRes int id) {
    setText(context.getResources().getString(id));
  }

  public void setText(@Nullable CharSequence text) {
    if (text == null) {
      text = "";
    }
    if (!TextUtils.equals(this.text, text)) {
      this.text = text;
      textDrawableHelper.setTextWidthDirty(true);
      invalidateSelf();
      onSizeChange();
    }
  }

  @Nullable
  public TextAppearance getTextAppearance() {
    return textDrawableHelper.getTextAppearance();
  }

  public void setTextAppearanceResource(@StyleRes int id) {
    setTextAppearance(new TextAppearance(context, id));
  }

  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    textDrawableHelper.setTextAppearance(textAppearance, context);
  }

  public TruncateAt getEllipsize() {
    return truncateAt;
  }

  public void setEllipsize(@Nullable TruncateAt truncateAt) {
    this.truncateAt = truncateAt;
  }

  public boolean isChipIconVisible() {
    return chipIconVisible;
  }

  /** @deprecated Use {@link ChipDrawable#isChipIconVisible()} instead. */
  @Deprecated
  public boolean isChipIconEnabled() {
    return isChipIconVisible();
  }

  public void setChipIconVisible(@BoolRes int id) {
    setChipIconVisible(context.getResources().getBoolean(id));
  }

  public void setChipIconVisible(boolean chipIconVisible) {
    if (this.chipIconVisible != chipIconVisible) {
      boolean oldShowsChipIcon = showsChipIcon();
      this.chipIconVisible = chipIconVisible;
      boolean newShowsChipIcon = showsChipIcon();

      boolean changed = oldShowsChipIcon != newShowsChipIcon;
      if (changed) {
        if (newShowsChipIcon) {
          applyChildDrawable(chipIcon);
        } else {
          unapplyChildDrawable(chipIcon);
        }

        invalidateSelf();
        onSizeChange();
      }
    }
  }

  /** @deprecated Use {@link ChipDrawable#setChipIconVisible(int)} instead. */
  @Deprecated
  public void setChipIconEnabledResource(@BoolRes int id) {
    setChipIconVisible(id);
  }

  /** @deprecated Use {@link ChipDrawable#setChipIconVisible(boolean)} instead. */
  @Deprecated
  public void setChipIconEnabled(boolean chipIconEnabled) {
    setChipIconVisible(chipIconEnabled);
  }

  @Nullable
  public Drawable getChipIcon() {
    return chipIcon != null ? DrawableCompat.unwrap(chipIcon) : null;
  }

  public void setChipIconResource(@DrawableRes int id) {
    setChipIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setChipIcon(@Nullable Drawable chipIcon) {
    Drawable oldChipIcon = getChipIcon();
    if (oldChipIcon != chipIcon) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.chipIcon = chipIcon != null ? DrawableCompat.wrap(chipIcon).mutate() : null;
      float newChipIconWidth = calculateChipIconWidth();

      unapplyChildDrawable(oldChipIcon);
      if (showsChipIcon()) {
        applyChildDrawable(this.chipIcon);
      }

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  /** Returns the {@link android.content.res.ColorStateList} used to tint the chip icon. */
  @Nullable
  public ColorStateList getChipIconTint() {
    return chipIconTint;
  }

  /**
   * Sets the chip icon's color tint using a resource ID.
   *
   * @param id Resource id of a {@link android.content.res.ColorStateList} to tint the chip icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  public void setChipIconTintResource(@ColorRes int id) {
    setChipIconTint(AppCompatResources.getColorStateList(context, id));
  }

  /**
   * Sets the chip icon's color tint using the specified {@link android.content.res.ColorStateList}.
   *
   * @param chipIconTint ColorStateList to tint the chip icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  public void setChipIconTint(@Nullable ColorStateList chipIconTint) {
    hasChipIconTint = true;
    if (this.chipIconTint != chipIconTint) {
      this.chipIconTint = chipIconTint;
      if (showsChipIcon()) {
        DrawableCompat.setTintList(chipIcon, chipIconTint);
      }

      onStateChange(getState());
    }
  }

  /**
   * Returns this chip's icon size. If a non-positive value is returned, the icon drawable's width
   * and height (up to 24dp) will be used to measure its bounds intead.
   *
   * @see #setChipIconSize(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  public float getChipIconSize() {
    return chipIconSize;
  }

  /**
   * Sets this chip icon's size using a resource id. If the value is zero (@null) or negative, the
   * icon drawable's width and height (up to 24dp) will be used instead.
   *
   * @param id The resource id of this chip's icon size.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconSize
   */
  public void setChipIconSizeResource(@DimenRes int id) {
    setChipIconSize(context.getResources().getDimension(id));
  }

  /**
   * Sets this chip icon's size. If the value is zero or negative, the icon drawable's width and
   * height (up to 24dp) will be used instead.
   *
   * @param chipIconSize This chip's icon size.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconSize
   */
  public void setChipIconSize(float chipIconSize) {
    if (this.chipIconSize != chipIconSize) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.chipIconSize = chipIconSize;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  public boolean isCloseIconVisible() {
    return closeIconVisible;
  }

  /** @deprecated Use {@link ChipDrawable#isCloseIconVisible()} instead. */
  @Deprecated
  public boolean isCloseIconEnabled() {
    return isCloseIconVisible();
  }

  public void setCloseIconVisible(@BoolRes int id) {
    setCloseIconVisible(context.getResources().getBoolean(id));
  }

  public void setCloseIconVisible(boolean closeIconVisible) {
    if (this.closeIconVisible != closeIconVisible) {
      boolean oldShowsCloseIcon = showsCloseIcon();
      this.closeIconVisible = closeIconVisible;
      boolean newShowsCloseIcon = showsCloseIcon();

      boolean changed = oldShowsCloseIcon != newShowsCloseIcon;
      if (changed) {
        if (newShowsCloseIcon) {
          applyChildDrawable(closeIcon);
        } else {
          unapplyChildDrawable(closeIcon);
        }

        invalidateSelf();
        onSizeChange();
      }
    }
  }

  /** @deprecated Use {@link ChipDrawable#setCloseIconVisible(int)} instead. */
  @Deprecated
  public void setCloseIconEnabledResource(@BoolRes int id) {
    setCloseIconVisible(id);
  }

  /** @deprecated Use {@link ChipDrawable#setCloseIconVisible(int)} instead. */
  @Deprecated
  public void setCloseIconEnabled(boolean closeIconEnabled) {
    setCloseIconVisible(closeIconEnabled);
  }

  @Nullable
  public Drawable getCloseIcon() {
    return closeIcon != null ? DrawableCompat.unwrap(closeIcon) : null;
  }

  public void setCloseIconResource(@DrawableRes int id) {
    setCloseIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setCloseIcon(@Nullable Drawable closeIcon) {
    Drawable oldCloseIcon = getCloseIcon();
    if (oldCloseIcon != closeIcon) {
      float oldCloseIconWidth = calculateCloseIconWidth();
      this.closeIcon = closeIcon != null ? DrawableCompat.wrap(closeIcon).mutate() : null;
      if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
        updateFrameworkCloseIconRipple();
      }
      float newCloseIconWidth = calculateCloseIconWidth();

      unapplyChildDrawable(oldCloseIcon);
      if (showsCloseIcon()) {
        applyChildDrawable(this.closeIcon);
      }

      invalidateSelf();
      if (oldCloseIconWidth != newCloseIconWidth) {
        onSizeChange();
      }
    }
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  private void updateFrameworkCloseIconRipple() {
    closeIconRipple =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(getRippleColor()),
            closeIcon,
            // A separate drawable with a solid background is needed for the mask because by
            // default, the close icon has a transparent background.
            closeIconRippleMask);
  }

  @Nullable
  public ColorStateList getCloseIconTint() {
    return closeIconTint;
  }

  public void setCloseIconTintResource(@ColorRes int id) {
    setCloseIconTint(AppCompatResources.getColorStateList(context, id));
  }

  public void setCloseIconTint(@Nullable ColorStateList closeIconTint) {
    if (this.closeIconTint != closeIconTint) {
      this.closeIconTint = closeIconTint;

      if (showsCloseIcon()) {
        DrawableCompat.setTintList(closeIcon, closeIconTint);
      }

      onStateChange(getState());
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
      if (showsCloseIcon()) {
        onSizeChange();
      }
    }
  }

  public void setCloseIconContentDescription(@Nullable CharSequence closeIconContentDescription) {
    if (this.closeIconContentDescription != closeIconContentDescription) {
      this.closeIconContentDescription =
          BidiFormatter.getInstance().unicodeWrap(closeIconContentDescription);

      invalidateSelf();
    }
  }

  @Nullable
  public CharSequence getCloseIconContentDescription() {
    return closeIconContentDescription;
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

      float oldChipIconWidth = calculateChipIconWidth();
      if (!checkable && currentChecked) {
        currentChecked = false;
      }
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  public boolean isCheckedIconVisible() {
    return checkedIconVisible;
  }

  /** @deprecated Use {@link ChipDrawable#isCheckedIconVisible()} instead. */
  @Deprecated
  public boolean isCheckedIconEnabled() {
    return isCheckedIconVisible();
  }

  public void setCheckedIconVisible(@BoolRes int id) {
    setCheckedIconVisible(context.getResources().getBoolean(id));
  }

  public void setCheckedIconVisible(boolean checkedIconVisible) {
    if (this.checkedIconVisible != checkedIconVisible) {
      boolean oldShowsCheckedIcon = showsCheckedIcon();
      this.checkedIconVisible = checkedIconVisible;
      boolean newShowsCheckedIcon = showsCheckedIcon();

      boolean changed = oldShowsCheckedIcon != newShowsCheckedIcon;
      if (changed) {
        if (newShowsCheckedIcon) {
          applyChildDrawable(checkedIcon);
        } else {
          unapplyChildDrawable(checkedIcon);
        }

        invalidateSelf();
        onSizeChange();
      }
    }
  }

  /** @deprecated Use {@link ChipDrawable#setCheckedIconVisible(int)} instead. */
  @Deprecated
  public void setCheckedIconEnabledResource(@BoolRes int id) {
    setCheckedIconVisible(context.getResources().getBoolean(id));
  }

  /** @deprecated Use {@link ChipDrawable#setCheckedIconVisible(boolean)} instead. */
  @Deprecated
  public void setCheckedIconEnabled(boolean checkedIconEnabled) {
    setCheckedIconVisible(checkedIconEnabled);
  }

  /**
   * Returns this chip's checked icon.
   *
   * @see #setCheckedIcon(Drawable)
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIcon
   */
  @Nullable
  public Drawable getCheckedIcon() {
    return checkedIcon;
  }

  /**
   * Sets this chip's checked icon using a resource id.
   *
   * @param id The resource id of this chip's checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIcon
   */
  public void setCheckedIconResource(@DrawableRes int id) {
    setCheckedIcon(AppCompatResources.getDrawable(context, id));
  }

  /**
   * Sets this chip's checked icon.
   *
   * @param checkedIcon This chip's checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIcon
   */
  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    Drawable oldCheckedIcon = this.checkedIcon;
    if (oldCheckedIcon != checkedIcon) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.checkedIcon = checkedIcon;
      float newChipIconWidth = calculateChipIconWidth();

      unapplyChildDrawable(this.checkedIcon);
      applyChildDrawable(this.checkedIcon);

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  /** Returns the {@link android.content.res.ColorStateList} used to tint the checked icon. */
  @Nullable
  public ColorStateList getCheckedIconTint() {
    return checkedIconTint;
  }

  /**
   * Sets the checked icon's color tint using a resource ID.
   *
   * @param id Resource id of a {@link android.content.res.ColorStateList} to tint the checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconTint
   */
  public void setCheckedIconTintResource(@ColorRes int id) {
    setCheckedIconTint(AppCompatResources.getColorStateList(context, id));
  }

  /**
   * Sets the checked icon's color tint using the specified {@link
   * android.content.res.ColorStateList}.
   *
   * @param checkedIconTint ColorStateList to tint the checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconTint
   */
  public void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    if (this.checkedIconTint != checkedIconTint) {
      this.checkedIconTint = checkedIconTint;

      if (canShowCheckedIcon()) {
        DrawableCompat.setTintList(checkedIcon, checkedIconTint);
      }

      onStateChange(getState());
    }
  }

  /**
   * Returns this chip's show motion spec.
   *
   * @see #setShowMotionSpec(MotionSpec)
   * @attr ref com.google.android.material.R.styleable#Chip_showMotionSpec
   */
  @Nullable
  public MotionSpec getShowMotionSpec() {
    return showMotionSpec;
  }

  /**
   * Sets this chip's show motion spec using a resource id.
   *
   * @param id The resource id of this chip's show motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_showMotionSpec
   */
  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(context, id));
  }

  /**
   * Sets this chip's show motion spec.
   *
   * @param showMotionSpec This chip's show motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_showMotionSpec
   */
  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    this.showMotionSpec = showMotionSpec;
  }

  /**
   * Returns this chip's hide motion spec.
   *
   * @see #setHideMotionSpec(MotionSpec)
   * @attr ref com.google.android.material.R.styleable#Chip_hideMotionSpec
   */
  @Nullable
  public MotionSpec getHideMotionSpec() {
    return hideMotionSpec;
  }

  /**
   * Sets this chip's hide motion spec using a resource id.
   *
   * @param id The resource id of this chip's hide motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_hideMotionSpec
   */
  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(context, id));
  }

  /**
   * Sets this chip's hide motion spec.
   *
   * @param hideMotionSpec This chip's hide motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_hideMotionSpec
   */
  public void setHideMotionSpec(@Nullable MotionSpec hideMotionSpec) {
    this.hideMotionSpec = hideMotionSpec;
  }

  /**
   * Returns this chip's start padding.
   *
   * @see #setChipStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipStartPadding
   */
  public float getChipStartPadding() {
    return chipStartPadding;
  }

  /**
   * Sets this chip's start padding using a resource id.
   *
   * @param id The resource id of this chip's start padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStartPadding
   */
  public void setChipStartPaddingResource(@DimenRes int id) {
    setChipStartPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets this chip's start padding.
   *
   * @param chipStartPadding This chip's start padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStartPadding
   */
  public void setChipStartPadding(float chipStartPadding) {
    if (this.chipStartPadding != chipStartPadding) {
      this.chipStartPadding = chipStartPadding;
      invalidateSelf();
      onSizeChange();
    }
  }

  /**
   * Returns the start padding for this chip's icon.
   *
   * @see #setIconStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_iconStartPadding
   */
  public float getIconStartPadding() {
    return iconStartPadding;
  }

  /**
   * Sets the start padding for this chip's icon using a resource id.
   *
   * @param id The resource id for the start padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconStartPadding
   */
  public void setIconStartPaddingResource(@DimenRes int id) {
    setIconStartPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets this chip's icon start padding.
   *
   * @param iconStartPadding The start padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconStartPadding
   */
  public void setIconStartPadding(float iconStartPadding) {
    if (this.iconStartPadding != iconStartPadding) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.iconStartPadding = iconStartPadding;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  /**
   * Returns the end padding for this chip's icon.
   *
   * @see #setIconEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_iconEndPadding
   */
  public float getIconEndPadding() {
    return iconEndPadding;
  }

  /**
   * Sets the end padding for this chip's icon using a resource id.
   *
   * @param id The resource id for the end padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconEndPadding
   */
  public void setIconEndPaddingResource(@DimenRes int id) {
    setIconEndPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets the end padding for this chip's icon.
   *
   * @param iconEndPadding The end padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconEndPadding
   */
  public void setIconEndPadding(float iconEndPadding) {
    if (this.iconEndPadding != iconEndPadding) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.iconEndPadding = iconEndPadding;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  /**
   * Returns the start padding for this chip's text.
   *
   * @see #setTextStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public float getTextStartPadding() {
    return textStartPadding;
  }

  /**
   * Sets the start padding for this chip's text using a resource id.
   *
   * @param id The resource id for the start padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public void setTextStartPaddingResource(@DimenRes int id) {
    setTextStartPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets the start padding for this chip's text.
   *
   * @param textStartPadding The start padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public void setTextStartPadding(float textStartPadding) {
    if (this.textStartPadding != textStartPadding) {
      this.textStartPadding = textStartPadding;
      invalidateSelf();
      onSizeChange();
    }
  }

  /**
   * Returns the end padding for this chip's text.
   *
   * @see #setTextEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_textEndPadding
   */
  public float getTextEndPadding() {
    return textEndPadding;
  }

  /**
   * Sets the end padding for this chip's text using a resource id.
   *
   * @param id The resource id for the end padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textEndPadding
   */
  public void setTextEndPaddingResource(@DimenRes int id) {
    setTextEndPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets the end padding for this chip's text.
   *
   * @param textEndPadding The end padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public void setTextEndPadding(float textEndPadding) {
    if (this.textEndPadding != textEndPadding) {
      this.textEndPadding = textEndPadding;
      invalidateSelf();
      onSizeChange();
    }
  }

  /**
   * Returns the start padding for this chip's close icon.
   *
   * @see #setCloseIconStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconStartPadding
   */
  public float getCloseIconStartPadding() {
    return closeIconStartPadding;
  }

  /**
   * Sets the start padding for this chip's close icon using a resource id.
   *
   * @param id The resource id for the start padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconStartPadding
   */
  public void setCloseIconStartPaddingResource(@DimenRes int id) {
    setCloseIconStartPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets the start padding for this chip's close icon.
   *
   * @param closeIconStartPadding The start padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconStartPadding
   */
  public void setCloseIconStartPadding(float closeIconStartPadding) {
    if (this.closeIconStartPadding != closeIconStartPadding) {
      this.closeIconStartPadding = closeIconStartPadding;
      invalidateSelf();
      if (showsCloseIcon()) {
        onSizeChange();
      }
    }
  }

  /**
   * Returns the end padding for this chip's close icon.
   *
   * @see #setCloseIconEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconEndPadding
   */
  public float getCloseIconEndPadding() {
    return closeIconEndPadding;
  }

  /**
   * Sets the end padding for this chip's close icon using a resource id.
   *
   * @param id The resource id for the end padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconEndPadding
   */
  public void setCloseIconEndPaddingResource(@DimenRes int id) {
    setCloseIconEndPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets the end padding for this chip's close icon.
   *
   * @param closeIconEndPadding The end padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconEndPadding
   */
  public void setCloseIconEndPadding(float closeIconEndPadding) {
    if (this.closeIconEndPadding != closeIconEndPadding) {
      this.closeIconEndPadding = closeIconEndPadding;
      invalidateSelf();
      if (showsCloseIcon()) {
        onSizeChange();
      }
    }
  }

  /**
   * Returns this chip's end padding.
   *
   * @see #setChipEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipEndPadding
   */
  public float getChipEndPadding() {
    return chipEndPadding;
  }

  /**
   * Sets this chip's end padding using a resource id.
   *
   * @param id The resource id for this chip's end padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipEndPadding
   */
  public void setChipEndPaddingResource(@DimenRes int id) {
    setChipEndPadding(context.getResources().getDimension(id));
  }

  /**
   * Sets this chip's end padding.
   *
   * @param chipEndPadding This chip's end padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipEndPadding
   */
  public void setChipEndPadding(float chipEndPadding) {
    if (this.chipEndPadding != chipEndPadding) {
      this.chipEndPadding = chipEndPadding;
      invalidateSelf();
      onSizeChange();
    }
  }

  /**
   * Returns the maximum width of TextView in terms of pixels.
   *
   * @see #setMaxWidth(int)
   */
  @Px
  public int getMaxWidth() {
    return maxWidth;
  }

  /**
   * Sets the width of the TextView to be exactly {@code pixels} wide.
   *
   * @param maxWidth maximum width of the textview.
   */
  public void setMaxWidth(@Px int maxWidth) {
    this.maxWidth = maxWidth;
  }

  boolean shouldDrawText() {
    return shouldDrawText;
  }

  /**
   * Specifies whether ChipDrawable should draw text to the canvas during the draw call. This is
   * intended to be used by {@link Chip} only.
   *
   * @param shouldDrawText whether to draw the text.
   */
  void setShouldDrawText(boolean shouldDrawText) {
    this.shouldDrawText = shouldDrawText;
  }
}
