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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Build.VERSION_CODES;
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
import android.support.annotation.XmlRes;
import android.support.design.animation.MotionSpec;
import android.support.design.canvas.CanvasCompat;
import android.support.design.drawable.DrawableUtils;
import android.support.design.resources.MaterialResources;
import android.support.design.resources.TextAppearance;
import android.support.design.ripple.RippleUtils;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.support.v4.text.BidiFormatter;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
 *     app:chipText="Hello, World!"/>
 * }</pre>
 *
 * <p>The basic attributes you can set are:
 *
 * <ul>
 *   <li>{@link android.R.attr#checkable android:checkable} - If true, the chip can be toggled. If
 *       false, the chip acts like a button.
 *   <li>{@link R.attr#chipText app:chipText} - Sets the text of the chip.
 *   <li>{@link R.attr#chipIcon app:chipIcon} - Sets the icon of the chip, or use @null to display
 *       no icon. Usually on the left.
 *   <li>{@link R.attr#checkedIcon app:checkedIcon} - Sets a custom icon to use when checked, or
 *       use @null to display no icon. Usually on the left.
 *   <li>{@link R.attr#closeIcon app:closeIcon} - Sets a custom icon that the user can click to
 *       close, or use @null to display no icon. Usually on the right.
 * </ul>
 *
 * <p>When used in this stand-alone mode, the host view must explicitly manage the ChipDrawable's
 * state:
 *
 * <ul>
 *   <li>{@link ChipDrawable#setBounds(int, int, int, int)}, taking into account {@link
 *       ChipDrawable#getIntrinsicWidth()} and {@link ChipDrawable#getIntrinsicWidth()}.
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
 * <p>ChipDrawable contains three child drawables: {@link #chipIcon}, {@link #checkedIcon}, and
 * {@link #closeIcon}. chipIcon and checkedIcon inherit the state of this drawable, but closeIcon
 * contains its own state that you can set with {@link #setCloseIconState(int[])}.
 *
 * @see Chip
 */
public class ChipDrawable extends Drawable implements TintAwareDrawable, Callback {

  private static final boolean DEBUG = false;
  private static final int[] DEFAULT_STATE = new int[] {android.R.attr.state_enabled};

  // Visuals
  @Nullable private ColorStateList chipBackgroundColor;
  private float chipMinHeight;
  private float chipCornerRadius;
  @Nullable private ColorStateList chipStrokeColor;
  private float chipStrokeWidth;
  @Nullable private ColorStateList rippleColor;

  // Text
  @Nullable private CharSequence chipText;
  @Nullable private TextAppearance textAppearance;

  // Chip icon
  private boolean chipIconEnabled;
  @Nullable private Drawable chipIcon;
  private float chipIconSize;

  // Close icon
  private boolean closeIconEnabled;
  @Nullable private Drawable closeIcon;
  @Nullable private ColorStateList closeIconTint;
  private float closeIconSize;

  // Checkable
  private boolean checkable;
  private boolean checkedIconEnabled;
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
  private final RectF rectF = new RectF();
  private final PointF pointF = new PointF();

  @ColorInt private int currentChipBackgroundColor;
  @ColorInt private int currentChipStrokeColor;
  @ColorInt private int currentCompatRippleColor;
  @ColorInt private int currentChipTextColor;
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
  private WeakReference<Delegate> delegate = new WeakReference<>(null);
  private boolean chipTextWidthDirty = true;
  private float chipTextWidth;

  /** Returns a ChipDrawable from the given attributes. */
  public static ChipDrawable createFromAttributes(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    ChipDrawable chip = new ChipDrawable(context);
    chip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return chip;
  }

  /**
   * Returns a ChipDrawable from the given XML resource. All attributes from {@link
   * R.styleable#ChipDrawable} and a custom <code>style</code> attribute are supported. A chip
   * resource may look like:
   *
   * <pre>{@code
   * <chip
   *     xmlns:app="http://schemas.android.com/apk/res-auto"
   *     style="@style/Widget.MaterialComponents.Chip.Entry"
   *     app:chipIcon="@drawable/custom_icon"/>
   * }</pre>
   */
  public static ChipDrawable createFromResource(Context context, @XmlRes int id) {
    try {
      XmlPullParser parser = context.getResources().getXml(id);

      int type;
      do {
        type = parser.next();
      } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
      if (type != XmlPullParser.START_TAG) {
        throw new XmlPullParserException("No start tag found");
      }

      if (!TextUtils.equals(parser.getName(), "chip")) {
        throw new XmlPullParserException("Must have a <chip> start tag");
      }

      AttributeSet attrs = Xml.asAttributeSet(parser);
      @StyleRes int style = attrs.getStyleAttribute();
      if (style == 0) {
        style = R.style.Widget_MaterialComponents_Chip_Entry;
      }

      return createFromAttributes(context, attrs, R.attr.chipStandaloneStyle, style);
    } catch (XmlPullParserException | IOException e) {
      Resources.NotFoundException exception =
          new NotFoundException("Can't load chip resource ID #0x" + Integer.toHexString(id));
      exception.initCause(e);
      throw exception;
    }
  }

  private ChipDrawable(Context context) {
    this.context = context;

    textPaint.density = context.getResources().getDisplayMetrics().density;
    debugPaint = DEBUG ? new Paint(Paint.ANTI_ALIAS_FLAG) : null;
    if (debugPaint != null) {
      debugPaint.setStyle(Style.STROKE);
    }

    setState(DEFAULT_STATE);
    setCloseIconState(DEFAULT_STATE);
  }

  private void loadFromAttributes(
      AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.ChipDrawable, defStyleAttr, defStyleRes);

    setChipBackgroundColor(
        MaterialResources.getColorStateList(
            context, a, R.styleable.ChipDrawable_chipBackgroundColor));
    setChipMinHeight(a.getDimension(R.styleable.ChipDrawable_chipMinHeight, 0f));
    setChipCornerRadius(a.getDimension(R.styleable.ChipDrawable_chipCornerRadius, 0f));
    setChipStrokeColor(
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_chipStrokeColor));
    setChipStrokeWidth(a.getDimension(R.styleable.ChipDrawable_chipStrokeWidth, 0f));
    setRippleColor(
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_rippleColor));

    setChipText(a.getText(R.styleable.ChipDrawable_chipText));
    setTextAppearance(
        MaterialResources.getTextAppearance(
            context, a, R.styleable.ChipDrawable_android_textAppearance));

    setChipIconEnabled(a.getBoolean(R.styleable.ChipDrawable_chipIconEnabled, false));
    setChipIcon(MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_chipIcon));
    setChipIconSize(a.getDimension(R.styleable.ChipDrawable_chipIconSize, 0f));

    setCloseIconEnabled(a.getBoolean(R.styleable.ChipDrawable_closeIconEnabled, false));
    setCloseIcon(MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_closeIcon));
    setCloseIconTint(
        MaterialResources.getColorStateList(context, a, R.styleable.ChipDrawable_closeIconTint));
    setCloseIconSize(a.getDimension(R.styleable.ChipDrawable_closeIconSize, 0f));

    setCheckable(a.getBoolean(R.styleable.ChipDrawable_android_checkable, false));
    setCheckedIconEnabled(a.getBoolean(R.styleable.ChipDrawable_checkedIconEnabled, false));
    setCheckedIcon(MaterialResources.getDrawable(context, a, R.styleable.ChipDrawable_checkedIcon));

    setShowMotionSpec(
        MotionSpec.createFromAttribute(context, a, R.styleable.ChipDrawable_showMotionSpec));
    setHideMotionSpec(
        MotionSpec.createFromAttribute(context, a, R.styleable.ChipDrawable_hideMotionSpec));

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
  public void getChipTouchBounds(RectF bounds) {
    calculateChipTouchBounds(getBounds(), bounds);
  }

  /**
   * Returns the close icon's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  public void getCloseIconTouchBounds(RectF bounds) {
    calculateCloseIconTouchBounds(getBounds(), bounds);
  }

  /** Returns the width at which the chip would like to be laid out. */
  @Override
  public int getIntrinsicWidth() {
    return (int)
        (chipStartPadding
            + calculateChipIconWidth()
            + textStartPadding
            + getChipTextWidth()
            + textEndPadding
            + calculateCloseIconWidth()
            + chipEndPadding);
  }

  /** Returns the height at which the chip would like to be laid out. */
  @Override
  public int getIntrinsicHeight() {
    return (int) chipMinHeight;
  }

  /** Returns whether we will show the chip icon. */
  private boolean showsChipIcon() {
    return chipIconEnabled && chipIcon != null;
  }

  /** Returns whether we will show the checked icon. */
  private boolean showsCheckedIcon() {
    return checkedIconEnabled && checkedIcon != null && currentChecked;
  }

  /** Returns whether we will show the close icon. */
  private boolean showsCloseIcon() {
    return closeIconEnabled && closeIcon != null;
  }

  /** Returns whether we can show the checked icon if our drawable state changes. */
  private boolean canShowCheckedIcon() {
    return checkedIconEnabled && checkedIcon != null && checkable;
  }

  /** Returns the width of the chip icon plus padding, which only apply if the chip icon exists. */
  private float calculateChipIconWidth() {
    if (showsChipIcon() || (showsCheckedIcon())) {
      return iconStartPadding + chipIconSize + iconEndPadding;
    }
    return 0f;
  }

  private float getChipTextWidth() {
    if (!chipTextWidthDirty) {
      return chipTextWidth;
    }

    chipTextWidth = calculateChipTextWidth(chipText);

    chipTextWidthDirty = false;
    return chipTextWidth;
  }

  private float calculateChipTextWidth(@Nullable CharSequence charSequence) {
    if (charSequence == null) {
      return 0f;
    }

    return textPaint.measureText(charSequence, 0, charSequence.length());
  }

  /**
   * Returns the width of the chip close icon plus padding, which only apply if the chip close icon
   * exists.
   */
  private float calculateCloseIconWidth() {
    if (showsCloseIcon()) {
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

    // 3. Draw compat ripple.
    drawCompatRipple(canvas, bounds);

    // 4. Draw chip icon.
    drawChipIcon(canvas, bounds);

    // 5. Draw checked icon.
    drawCheckedIcon(canvas, bounds);

    // 6. Draw chip text.
    drawChipText(canvas, bounds);

    // 7. Draw close icon.
    drawCloseIcon(canvas, bounds);

    // Debug.
    drawDebug(canvas, bounds);

    if (alpha < 255) {
      canvas.restoreToCount(saveCount);
    }
  }

  private void drawChipBackground(@NonNull Canvas canvas, Rect bounds) {
    chipPaint.setColor(currentChipBackgroundColor);
    chipPaint.setStyle(Style.FILL);
    chipPaint.setColorFilter(getTintColorFilter());
    rectF.set(bounds);
    canvas.drawRoundRect(rectF, chipCornerRadius, chipCornerRadius, chipPaint);
  }

  /**
   * Draws the chip stroke. Draw the stroke <code>chipStrokeWidth / 2f</code> away from the edges so
   * that the stroke perfectly fills the bounds of the chip.
   */
  private void drawChipStroke(@NonNull Canvas canvas, Rect bounds) {
    if (chipStrokeWidth > 0) {
      chipPaint.setColor(currentChipStrokeColor);
      chipPaint.setStyle(Style.STROKE);
      chipPaint.setColorFilter(getTintColorFilter());
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

  private void drawCompatRipple(@NonNull Canvas canvas, Rect bounds) {
    chipPaint.setColor(currentCompatRippleColor);
    chipPaint.setStyle(Style.FILL);
    rectF.set(bounds);
    canvas.drawRoundRect(rectF, chipCornerRadius, chipCornerRadius, chipPaint);
  }

  private void drawChipIcon(@NonNull Canvas canvas, Rect bounds) {
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

  private void drawCheckedIcon(@NonNull Canvas canvas, Rect bounds) {
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
  private void drawChipText(@NonNull Canvas canvas, Rect bounds) {
    if (chipText != null) {
      // TODO: Bounds may be smaller than intrinsic size. Ellipsize, clip, or multiline the text.
      Align align = calculateChipTextOrigin(bounds, pointF);
      calculateChipTextBounds(bounds, rectF);

      if (textAppearance != null) {
        textPaint.drawableState = getState();
        textAppearance.updateDrawState(context, textPaint);
      }
      textPaint.setTextAlign(align);

      boolean clip = getChipTextWidth() > rectF.width();
      int saveCount = 0;
      if (clip) {
        saveCount = canvas.save();
        canvas.clipRect(rectF);
      }
      canvas.drawText(chipText, 0, chipText.length(), pointF.x, pointF.y, textPaint);
      if (clip) {
        canvas.restoreToCount(saveCount);
      }
    }
  }

  private void drawCloseIcon(@NonNull Canvas canvas, Rect bounds) {
    if (showsCloseIcon()) {
      calculateCloseIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      closeIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());
      closeIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  private void drawDebug(@NonNull Canvas canvas, Rect bounds) {
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
      if (chipText != null) {
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
  private void calculateChipIconBounds(Rect bounds, RectF outBounds) {
    outBounds.setEmpty();

    if (showsChipIcon() || showsCheckedIcon()) {
      float offsetFromStart = chipStartPadding + iconStartPadding;

      if (DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_LTR) {
        outBounds.left = bounds.left + offsetFromStart;
        outBounds.right = outBounds.left + chipIconSize;
      } else {
        outBounds.right = bounds.right - offsetFromStart;
        outBounds.left = outBounds.right - chipIconSize;
      }

      outBounds.top = bounds.exactCenterY() - chipIconSize / 2f;
      outBounds.bottom = outBounds.top + chipIconSize;
    }
  }

  /**
   * Calculates the chip text's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  private Align calculateChipTextOrigin(Rect bounds, PointF pointF) {
    pointF.set(0, 0);
    Align align = Align.LEFT;

    if (chipText != null) {
      float offsetFromStart = chipStartPadding + calculateChipIconWidth() + textStartPadding;

      if (DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_LTR) {
        pointF.x = bounds.left + offsetFromStart;
        align = Align.LEFT;
      } else {
        pointF.x = bounds.right - offsetFromStart;
        align = Align.RIGHT;
      }

      pointF.y = bounds.centerY() - calculateChipTextCenterFromBaseline();
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
  private float calculateChipTextCenterFromBaseline() {
    textPaint.getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }

  /**
   * Calculates the chip text's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  private void calculateChipTextBounds(Rect bounds, RectF outBounds) {
    outBounds.setEmpty();

    if (chipText != null) {
      float offsetFromStart = chipStartPadding + calculateChipIconWidth() + textStartPadding;
      float offsetFromEnd = chipEndPadding + calculateCloseIconWidth() + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_LTR) {
        outBounds.left = bounds.left + offsetFromStart;
        outBounds.right = bounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
        outBounds.right = bounds.right - offsetFromStart;
      }

      // Top and bottom included for completion. Don't position the chip text vertically based on
      // these bounds. Instead, use #calculateChipTextOrigin().
      outBounds.top = bounds.top;
      outBounds.bottom = bounds.bottom;
    }
  }

  /**
   * Calculates the close icon's ChipDrawable-absolute bounds (top-left is <code>
   * [ChipDrawable.getBounds().left, ChipDrawable.getBounds().top]</code>).
   */
  private void calculateCloseIconBounds(Rect bounds, RectF outBounds) {
    outBounds.setEmpty();

    if (showsCloseIcon()) {
      float offsetFromEnd = chipEndPadding + closeIconEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_LTR) {
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

  private void calculateChipTouchBounds(Rect bounds, RectF outBounds) {
    outBounds.set(bounds);

    if (showsCloseIcon()) {
      float offsetFromEnd =
          chipEndPadding
              + closeIconEndPadding
              + closeIconSize
              + closeIconStartPadding
              + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
      }
    }
  }

  private void calculateCloseIconTouchBounds(Rect bounds, RectF outBounds) {
    outBounds.setEmpty();

    if (showsCloseIcon()) {
      float offsetFromEnd =
          chipEndPadding
              + closeIconEndPadding
              + closeIconSize
              + closeIconStartPadding
              + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_LTR) {
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

  /**
   * Indicates whether this chip drawable will change its appearance based on state.
   *
   * <p>The logic here and {@link #isCloseIconStateful()} must match {@link #onStateChange(int[],
   * int[])}.
   */
  @Override
  public boolean isStateful() {
    return isStateful(chipBackgroundColor)
        || isStateful(chipStrokeColor)
        || (useCompatRipple && isStateful(compatRippleColor))
        || isStateful(textAppearance)
        || canShowCheckedIcon()
        || isStateful(chipIcon)
        || isStateful(checkedIcon)
        || isStateful(tint);
  }

  /**
   * Indicates whether the close icon drawable will change its appearance based on state.
   *
   * <p>The logic here and {@link #isStateful()} must match {@link #onStateChange(int[], int[])}.
   */
  public boolean isCloseIconStateful() {
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
  protected boolean onStateChange(int[] state) {
    return onStateChange(state, getCloseIconState());
  }

  /**
   * Changes appearance in response to the specified state.
   *
   * <p>The logic here must match {@link #isStateful()} and {@link #isCloseIconStateful()}.
   */
  private boolean onStateChange(int[] chipState, int[] closeIconState) {
    boolean invalidate = super.onStateChange(chipState);
    boolean sizeChanged = false;

    int newChipBackgroundColor =
        chipBackgroundColor != null
            ? chipBackgroundColor.getColorForState(chipState, currentChipBackgroundColor)
            : 0;
    if (currentChipBackgroundColor != newChipBackgroundColor) {
      currentChipBackgroundColor = newChipBackgroundColor;
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
        compatRippleColor != null
            ? compatRippleColor.getColorForState(chipState, currentCompatRippleColor)
            : 0;
    if (currentCompatRippleColor != newCompatRippleColor) {
      currentCompatRippleColor = newCompatRippleColor;
      if (useCompatRipple) {
        invalidate = true;
      }
    }

    int newChipTextColor =
        textAppearance != null && textAppearance.textColor != null
            ? textAppearance.textColor.getColorForState(chipState, currentChipTextColor)
            : 0;
    if (currentChipTextColor != newChipTextColor) {
      currentChipTextColor = newChipTextColor;
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
      invalidate |= closeIcon.setState(closeIconState);
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
  @TargetApi(VERSION_CODES.M)
  public boolean onLayoutDirectionChanged(int layoutDirection) {
    boolean invalidate = super.onLayoutDirectionChanged(layoutDirection);

    if (showsChipIcon()) {
      invalidate |= chipIcon.setLayoutDirection(layoutDirection);
    }
    if (showsCheckedIcon()) {
      invalidate |= checkedIcon.setLayoutDirection(layoutDirection);
    }
    if (showsCloseIcon()) {
      invalidate |= closeIcon.setLayoutDirection(layoutDirection);
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
    if (drawable != null) {
      drawable.setCallback(this);
      DrawableCompat.setLayoutDirection(drawable, DrawableCompat.getLayoutDirection(this));
      drawable.setLevel(getLevel());
      drawable.setVisible(isVisible(), false);

      if (drawable == closeIcon) {
        if (drawable.isStateful()) {
          drawable.setState(getCloseIconState());
        }
        DrawableCompat.setTintList(drawable, closeIconTint);
      } else {
        if (drawable.isStateful()) {
          drawable.setState(getState());
        }
      }
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
        useCompatRipple ? RippleUtils.convertToRippleDrawableColor(rippleColor) : null;
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

  /** Delegate interface to be implemented by Views that own a ChipDrawable. */
  public interface Delegate {

    /** Handles a change in the ChipDrawable's size. */
    void onChipDrawableSizeChange();
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

  public float getChipMinHeight() {
    return chipMinHeight;
  }

  public void setChipMinHeightResource(@DimenRes int id) {
    setChipMinHeight(context.getResources().getDimension(id));
  }

  public void setChipMinHeight(float chipMinHeight) {
    if (this.chipMinHeight != chipMinHeight) {
      this.chipMinHeight = chipMinHeight;
      invalidateSelf();
      onSizeChange();
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
      updateCompatRippleColor();
      onStateChange(getState());
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
      this.chipText = BidiFormatter.getInstance().unicodeWrap(chipText);
      chipTextWidthDirty = true;

      invalidateSelf();
      onSizeChange();
    }
  }

  @Nullable
  public TextAppearance getTextAppearance() {
    return textAppearance;
  }

  public void setTextAppearanceResource(@StyleRes int id) {
    setTextAppearance(new TextAppearance(context, id));
  }

  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (this.textAppearance != textAppearance) {
      this.textAppearance = textAppearance;

      if (textAppearance != null) {
        textAppearance.updateMeasureState(context, textPaint);
        chipTextWidthDirty = true;
      }

      onStateChange(getState());
      onSizeChange();
    }
  }

  public boolean isChipIconEnabled() {
    return chipIconEnabled;
  }

  public void setChipIconEnabledResource(@BoolRes int id) {
    setChipIconEnabled(context.getResources().getBoolean(id));
  }

  public void setChipIconEnabled(boolean chipIconEnabled) {
    if (this.chipIconEnabled != chipIconEnabled) {
      boolean oldShowsChipIcon = showsChipIcon();
      this.chipIconEnabled = chipIconEnabled;
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

  @Nullable
  public Drawable getChipIcon() {
    return chipIcon;
  }

  public void setChipIconResource(@DrawableRes int id) {
    setChipIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setChipIcon(@Nullable Drawable chipIcon) {
    Drawable oldChipIcon = this.chipIcon;
    if (oldChipIcon != chipIcon) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.chipIcon = chipIcon;
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

  public float getChipIconSize() {
    return chipIconSize;
  }

  public void setChipIconSizeResource(@DimenRes int id) {
    setChipIconSize(context.getResources().getDimension(id));
  }

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

  public boolean isCloseIconEnabled() {
    return closeIconEnabled;
  }

  public void setCloseIconEnabledResource(@BoolRes int id) {
    setCloseIconEnabled(context.getResources().getBoolean(id));
  }

  public void setCloseIconEnabled(boolean closeIconEnabled) {
    if (this.closeIconEnabled != closeIconEnabled) {
      boolean oldShowsCloseIcon = showsCloseIcon();
      this.closeIconEnabled = closeIconEnabled;
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

  @Nullable
  public Drawable getCloseIcon() {
    return closeIcon;
  }

  public void setCloseIconResource(@DrawableRes int id) {
    setCloseIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setCloseIcon(@Nullable Drawable closeIcon) {
    Drawable oldCloseIcon = this.closeIcon != null ? DrawableCompat.unwrap(this.closeIcon) : null;
    if (oldCloseIcon != closeIcon) {
      float oldCloseIconWidth = calculateCloseIconWidth();
      this.closeIcon = closeIcon != null ? DrawableCompat.wrap(closeIcon).mutate() : null;
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

  public boolean isCheckedIconEnabled() {
    return checkedIconEnabled;
  }

  public void setCheckedIconEnabledResource(@BoolRes int id) {
    setCheckedIconEnabled(context.getResources().getBoolean(id));
  }

  public void setCheckedIconEnabled(boolean checkedIconEnabled) {
    if (this.checkedIconEnabled != checkedIconEnabled) {
      boolean oldShowsCheckedIcon = showsCheckedIcon();
      this.checkedIconEnabled = checkedIconEnabled;
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

  @Nullable
  public Drawable getCheckedIcon() {
    return checkedIcon;
  }

  public void setCheckedIconResource(@DrawableRes int id) {
    setCheckedIcon(AppCompatResources.getDrawable(context, id));
  }

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

  @Nullable
  public MotionSpec getShowMotionSpec() {
    return showMotionSpec;
  }

  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(context, id));
  }

  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    this.showMotionSpec = showMotionSpec;
  }

  @Nullable
  public MotionSpec getHideMotionSpec() {
    return hideMotionSpec;
  }

  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(context, id));
  }

  public void setHideMotionSpec(@Nullable MotionSpec hideMotionSpec) {
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
      onSizeChange();
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
      float oldChipIconWidth = calculateChipIconWidth();
      this.iconStartPadding = iconStartPadding;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
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
      float oldChipIconWidth = calculateChipIconWidth();
      this.iconEndPadding = iconEndPadding;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
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
      onSizeChange();
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
      onSizeChange();
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
      if (showsCloseIcon()) {
        onSizeChange();
      }
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
      if (showsCloseIcon()) {
        onSizeChange();
      }
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
      onSizeChange();
    }
  }
}
