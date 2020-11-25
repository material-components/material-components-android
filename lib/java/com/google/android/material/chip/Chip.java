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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.AnimatorRes;
import androidx.annotation.BoolRes;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.customview.widget.ExploreByTouchHelper;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.chip.ChipDrawable.Delegate;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.resources.TextAppearanceFontCallback;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Chips are compact elements that represent an attribute, text, entity, or action. They allow users
 * to enter information, select a choice, filter content, or trigger an action.
 *
 * <p>The Chip widget is a thin view wrapper around the {@link ChipDrawable}, which contains all of
 * the layout and draw logic. The extra logic exists to support touch, mouse, keyboard, and
 * accessibility navigation. The main chip and close icon are considered to be separate logical
 * sub-views, and contain their own navigation behavior and state.
 *
 * <p>All attributes from {@link R.styleable#Chip} are supported. Do not use the {@code
 * android:background} attribute. It will be ignored because Chip manages its own background
 * Drawable. Also do not use the {@code android:drawableStart} and {@code android:drawableEnd}
 * attributes. They will be ignored because Chip manages its own start ({@code app:chipIcon}) and
 * end ({@code app:closeIcon}) drawables. The basic attributes you can set are:
 *
 * <ul>
 *   <li>{@link android.R.attr#checkable android:checkable} - If true, the chip can be toggled. If
 *       false, the chip acts like a button.
 *   <li>{@link android.R.attr#text android:text} - Sets the text of the chip.
 *   <li>{@link R.attr#chipIcon app:chipIcon} and {@link R.attr#chipIconEnabled app:chipIconEnabled}
 *       - Sets the icon of the chip. Usually on the left.
 *   <li>{@link R.attr#checkedIcon app:checkedIcon} and {@link R.attr#checkedIconEnabled
 *       app:checkedIconEnabled} - Sets a custom icon to use when checked. Usually on the left.
 *   <li>{@link R.attr#closeIcon app:closeIcon} and {@link R.attr#closeIconEnabled
 *       app:closeIconEnabled} - Sets a custom icon that the user can click to close. Usually on the
 *       right.
 * </ul>
 *
 * <p>You can register a listener on the main chip with {@link #setOnClickListener(OnClickListener)}
 * or {@link #setOnCheckedChangeListener(OnCheckedChangeListener)}. You can register a listener on
 * the close icon with {@link #setOnCloseIconClickListener(OnClickListener)}.
 *
 * <p>For proper rendering of the ancestor TextView in RTL mode, call {@link
 * #setLayoutDirection(int)} with <code>View.LAYOUT_DIRECTION_LOCALE</code>. By default, TextView's
 * layout rendering sets the text padding in LTR on initial rendering and it only renders correctly
 * after the layout has been invalidated so you need to ensure that initial rendering has the
 * correct layout.
 *
 * @see ChipDrawable
 */
public class Chip extends AppCompatCheckBox implements Delegate, Shapeable {

  private static final String TAG = "Chip";

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Chip_Action;

  private static final int CHIP_BODY_VIRTUAL_ID = 0;
  private static final int CLOSE_ICON_VIRTUAL_ID = 1;
  private static final Rect EMPTY_BOUNDS = new Rect();

  private static final int[] SELECTED_STATE = new int[] {android.R.attr.state_selected};
  private static final int[] CHECKABLE_STATE_SET = {android.R.attr.state_checkable};

  private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";

  /** Value taken from Android Accessibility Guide */
  private static final int MIN_TOUCH_TARGET_DP = 48;

  @Nullable private ChipDrawable chipDrawable;
  @Nullable private InsetDrawable insetBackgroundDrawable;
  //noinspection NewApi
  @Nullable private RippleDrawable ripple;

  @Nullable private OnClickListener onCloseIconClickListener;
  @Nullable private OnCheckedChangeListener onCheckedChangeListenerInternal;
  private boolean deferredCheckedValue;
  private boolean closeIconPressed;
  private boolean closeIconHovered;
  private boolean closeIconFocused;
  private boolean ensureMinTouchTargetSize;
  private int lastLayoutDirection;

  @Dimension(unit = Dimension.PX)
  private int minTouchTargetSize;

  private static final String BUTTON_ACCESSIBILITY_CLASS_NAME = "android.widget.Button";
  private static final String COMPOUND_BUTTON_ACCESSIBILITY_CLASS_NAME =
      "android.widget.CompoundButton";
  private static final String GENERIC_VIEW_ACCESSIBILITY_CLASS_NAME = "android.view.View";

  @NonNull private final ChipTouchHelper touchHelper;
  private final Rect rect = new Rect();
  private final RectF rectF = new RectF();
  private final TextAppearanceFontCallback fontCallback =
      new TextAppearanceFontCallback() {
        @Override
        public void onFontRetrieved(@NonNull Typeface typeface, boolean fontResolvedSynchronously) {
          // Set text to re-trigger internal ellipsize width calculation.
          setText(chipDrawable.shouldDrawText() ? chipDrawable.getText() : getText());
          requestLayout();
          invalidate();
        }

        @Override
        public void onFontRetrievalFailed(int reason) {}
      };

  public Chip(Context context) {
    this(context, null);
  }

  public Chip(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.chipStyle);
  }

  public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    validateAttributes(attrs);
    ChipDrawable drawable =
        ChipDrawable.createFromAttributes(
            context, attrs, defStyleAttr, DEF_STYLE_RES);
    initMinTouchTarget(context, attrs, defStyleAttr);
    setChipDrawable(drawable);
    drawable.setElevation(ViewCompat.getElevation(this));
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.Chip,
            defStyleAttr,
            DEF_STYLE_RES);
    if (VERSION.SDK_INT < VERSION_CODES.M) {
      // This is necessary to work around a bug that doesn't support themed color referenced in
      // ColorStateList for API level < 23.
      setTextColor(
          MaterialResources.getColorStateList(context, a, R.styleable.Chip_android_textColor));
    }
    boolean hasShapeAppearanceAttribute = a.hasValue(R.styleable.Chip_shapeAppearance);
    a.recycle();

    touchHelper = new ChipTouchHelper(this);
    updateAccessibilityDelegate();
    if (!hasShapeAppearanceAttribute) {
      initOutlineProvider();
    }
    // Set deferred values
    setChecked(deferredCheckedValue);
    setText(drawable.getText());
    setEllipsize(drawable.getEllipsize());

    updateTextPaintDrawState();

    // Chip text should not extend to more than 1 line.
    if (!chipDrawable.shouldDrawText()) {
      setLines(1);
      setHorizontallyScrolling(true);
    }
    // Chip text should be vertically center aligned and start aligned.
    // Final horizontal text origin is set during the onDraw call via canvas translation.
    setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
    // Helps TextView calculate the available text width.
    updatePaddingInternal();
    if (shouldEnsureMinTouchTargetSize()) {
      setMinHeight(minTouchTargetSize);
    }
    lastLayoutDirection = ViewCompat.getLayoutDirection(this);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, chipDrawable);
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    if (chipDrawable != null) {
      chipDrawable.setElevation(elevation);
    }
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    if (isCheckable() || isClickable()) {
      info.setClassName(
          isCheckable()
              ? COMPOUND_BUTTON_ACCESSIBILITY_CLASS_NAME
              : BUTTON_ACCESSIBILITY_CLASS_NAME);
    } else {
      info.setClassName(GENERIC_VIEW_ACCESSIBILITY_CLASS_NAME);
    }
    info.setCheckable(isCheckable());
    info.setClickable(isClickable());

    if (getParent() instanceof ChipGroup) {
      ChipGroup chipGroup = ((ChipGroup) getParent());
      AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
      // -1 for unknown column indices in a reflowing layout
      int columnIndex = chipGroup.isSingleLine() ? chipGroup.getIndexOfChip(this) : -1;
      infoCompat.setCollectionItemInfo(
          CollectionItemInfoCompat.obtain(
              /* rowIndex= */ chipGroup.getRowIndex(this),
              /* rowSpan= */ 1,
              /* columnIndex= */ columnIndex,
              /* columnSpan= */ 1,
              /* heading= */ false,
              /* selected= */ isChecked()));
    }
  }

  // TODO(b/80452017): Due to a11y bug, avoid setting custom ExploreByTouchHelper as delegate
  // unless there's a close/trailing icon. Re-evaulate this once bug is fixed.
  private void updateAccessibilityDelegate() {
    if (hasCloseIcon() && isCloseIconVisible() && onCloseIconClickListener != null) {
      ViewCompat.setAccessibilityDelegate(this, touchHelper);
    } else {
      // Avoid setting custom ExploreByTouchHelper if the trailing icon is only decorative.
      ViewCompat.setAccessibilityDelegate(this, null);
    }
  }

  private void initMinTouchTarget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    // Checks if the Chip should meet Android's minimum touch target size.
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.Chip,
            defStyleAttr,
            DEF_STYLE_RES);
    ensureMinTouchTargetSize = a.getBoolean(R.styleable.Chip_ensureMinTouchTargetSize, false);

    float defaultMinTouchTargetSize =
        (float) Math.ceil(ViewUtils.dpToPx(getContext(), MIN_TOUCH_TARGET_DP));
    minTouchTargetSize =
        (int)
            Math.ceil(
                a.getDimension(R.styleable.Chip_chipMinTouchTargetSize, defaultMinTouchTargetSize));

    a.recycle();
  }

  /**
   * Updates the paddings to inform {@link android.widget.TextView} how much width the text can
   * occupy.
   */
  private void updatePaddingInternal() {
    if (TextUtils.isEmpty(getText()) || chipDrawable == null) {
      return;
    }
    int paddingEnd =
        (int)
            (chipDrawable.getChipEndPadding()
                + chipDrawable.getTextEndPadding()
                + chipDrawable.calculateCloseIconWidth());
    int paddingStart =
        (int)
            (chipDrawable.getChipStartPadding()
                + chipDrawable.getTextStartPadding()
                + chipDrawable.calculateChipIconWidth());
    if (insetBackgroundDrawable != null) {
      Rect padding = new Rect();
      insetBackgroundDrawable.getPadding(padding);
      paddingStart += padding.left;
      paddingEnd += padding.right;
    }

    ViewCompat.setPaddingRelative(
        this, paddingStart, getPaddingTop(), paddingEnd, getPaddingBottom());
  }

  @Override
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  public void onRtlPropertiesChanged(int layoutDirection) {
    super.onRtlPropertiesChanged(layoutDirection);

    // Layout direction can be updated via a parent (or ancestor) View, causing this property change
    // method to be called. Update text padding whenever a layout direction change is detected.
    if (lastLayoutDirection != layoutDirection) {
      lastLayoutDirection = layoutDirection;
      updatePaddingInternal();
    }
  }

  private void validateAttributes(@Nullable AttributeSet attributeSet) {
    if (attributeSet == null) {
      return;
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "background") != null) {
      Log.w(TAG, "Do not set the background; Chip manages its own background drawable.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableLeft") != null) {
      throw new UnsupportedOperationException("Please set left drawable using R.attr#chipIcon.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableStart") != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableEnd") != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableRight") != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }
    if (!attributeSet.getAttributeBooleanValue(NAMESPACE_ANDROID, "singleLine", true)
        || (attributeSet.getAttributeIntValue(NAMESPACE_ANDROID, "lines", 1) != 1)
        || (attributeSet.getAttributeIntValue(NAMESPACE_ANDROID, "minLines", 1) != 1)
        || (attributeSet.getAttributeIntValue(NAMESPACE_ANDROID, "maxLines", 1) != 1)) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }

    if (attributeSet.getAttributeIntValue(
            NAMESPACE_ANDROID, "gravity", (Gravity.CENTER_VERTICAL | Gravity.START))
        != (Gravity.CENTER_VERTICAL | Gravity.START)) {
      Log.w(TAG, "Chip text must be vertically center and start aligned");
    }
  }

  private void initOutlineProvider() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void getOutline(View view, @NonNull Outline outline) {
              if (chipDrawable != null) {
                chipDrawable.getOutline(outline);
              } else {
                outline.setAlpha(0.0f);
              }
            }
          });
    }
  }

  /** Returns the ChipDrawable backing this chip. */
  public Drawable getChipDrawable() {
    return chipDrawable;
  }

  /** Sets the ChipDrawable backing this chip. */
  public void setChipDrawable(@NonNull ChipDrawable drawable) {
    if (chipDrawable != drawable) {
      unapplyChipDrawable(chipDrawable);
      chipDrawable = drawable;
      // Defers to TextView to draw the text and ChipDrawable to render the
      // rest (e.g. chip / check / close icons).
      chipDrawable.setShouldDrawText(false);
      applyChipDrawable(chipDrawable);
      ensureAccessibleTouchTarget(minTouchTargetSize);
    }
  }

  private void updateBackgroundDrawable() {
    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
      updateFrameworkRippleBackground();
    } else {
      chipDrawable.setUseCompatRipple(true);
      ViewCompat.setBackground(this, getBackgroundDrawable());
      updatePaddingInternal();
      ensureChipDrawableHasCallback();
    }
  }

  private void ensureChipDrawableHasCallback() {
    if (getBackgroundDrawable() == insetBackgroundDrawable && chipDrawable.getCallback() == null) {
      // View#setBackground nulls out the callback of the previous background drawable, so we need
      // to reset it.
      chipDrawable.setCallback(insetBackgroundDrawable);
    }
  }

  @Nullable
  public Drawable getBackgroundDrawable() {
    if (insetBackgroundDrawable == null) {
      return chipDrawable;
    }
    return insetBackgroundDrawable;
  }

  private void updateFrameworkRippleBackground() {
    //noinspection NewApi
    ripple =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(chipDrawable.getRippleColor()),
            getBackgroundDrawable(),
            null);
    chipDrawable.setUseCompatRipple(false);
    //noinspection NewApi
    ViewCompat.setBackground(this, ripple);
    updatePaddingInternal();
  }

  private void unapplyChipDrawable(@Nullable ChipDrawable chipDrawable) {
    if (chipDrawable != null) {
      chipDrawable.setDelegate(null);
    }
  }

  private void applyChipDrawable(@NonNull ChipDrawable chipDrawable) {
    chipDrawable.setDelegate(this);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] state = super.onCreateDrawableState(extraSpace + 2);
    if (isChecked()) {
      mergeDrawableStates(state, SELECTED_STATE);
    }
    if (isCheckable()) {
      mergeDrawableStates(state, CHECKABLE_STATE_SET);
    }
    return state;
  }

  @Override
  public void setGravity(int gravity) {
    if (gravity != (Gravity.CENTER_VERTICAL | Gravity.START)) {
      Log.w(TAG, "Chip text must be vertically center and start aligned");
    } else {
      super.setGravity(gravity);
    }
  }

  public void setBackgroundTintList(@Nullable ColorStateList tint) {
    Log.w(TAG, "Do not set the background tint list; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundTintMode(@Nullable Mode tintMode) {
    Log.w(TAG, "Do not set the background tint mode; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundColor(int color) {
    Log.w(TAG, "Do not set the background color; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundResource(int resid) {
    Log.w(TAG, "Do not set the background resource; Chip manages its own background drawable.");
  }

  @Override
  public void setBackground(Drawable background) {
    if (background != getBackgroundDrawable() && background != ripple) {
      Log.w(TAG, "Do not set the background; Chip manages its own background drawable.");
    } else {
      super.setBackground(background);
    }
  }

  @Override
  public void setBackgroundDrawable(Drawable background) {
    if (background != getBackgroundDrawable() && background != ripple) {
      Log.w(TAG, "Do not set the background drawable; Chip manages its own background drawable.");
    } else {
      super.setBackgroundDrawable(background);
    }
  }

  @Override
  public void setCompoundDrawables(
      @Nullable Drawable left,
      @Nullable Drawable top,
      @Nullable Drawable right,
      @Nullable Drawable bottom) {
    if (left != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (right != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawables(left, top, right, bottom);
  }

  @Override
  public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
    if (left != 0) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (right != 0) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
  }

  @Override
  public void setCompoundDrawablesWithIntrinsicBounds(
      @Nullable Drawable left,
      @Nullable Drawable top,
      @Nullable Drawable right,
      @Nullable Drawable bottom) {
    if (left != null) {
      throw new UnsupportedOperationException("Please set left drawable using R.attr#chipIcon.");
    }
    if (right != null) {
      throw new UnsupportedOperationException("Please set right drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
  }

  @Override
  public void setCompoundDrawablesRelative(
      @Nullable Drawable start,
      @Nullable Drawable top,
      @Nullable Drawable end,
      @Nullable Drawable bottom) {
    if (start != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (end != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesRelative(start, top, end, bottom);
  }

  @Override
  public void setCompoundDrawablesRelativeWithIntrinsicBounds(
      int start, int top, int end, int bottom) {
    if (start != 0) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (end != 0) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
  }

  @Override
  public void setCompoundDrawablesRelativeWithIntrinsicBounds(
      @Nullable Drawable start,
      @Nullable Drawable top,
      @Nullable Drawable end,
      @Nullable Drawable bottom) {
    if (start != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (end != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }
    super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
  }

  @Nullable
  @Override
  public TruncateAt getEllipsize() {
    return chipDrawable != null ? chipDrawable.getEllipsize() : null;
  }

  @Override
  public void setEllipsize(TruncateAt where) {
    if (chipDrawable == null) {
      return;
    }
    if (where == TruncateAt.MARQUEE) {
      throw new UnsupportedOperationException("Text within a chip are not allowed to scroll.");
    }
    super.setEllipsize(where);
    if (chipDrawable != null) {
      chipDrawable.setEllipsize(where);
    }
  }

  @Override
  public void setSingleLine(boolean singleLine) {
    if (!singleLine) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setSingleLine(singleLine);
  }

  @Override
  public void setLines(int lines) {
    if (lines > 1) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setLines(lines);
  }

  @Override
  public void setMinLines(int minLines) {
    if (minLines > 1) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setMinLines(minLines);
  }

  @Override
  public void setMaxLines(int maxLines) {
    if (maxLines > 1) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setMaxLines(maxLines);
  }

  @Override
  public void setMaxWidth(@Px int maxWidth) {
    super.setMaxWidth(maxWidth);
    if (chipDrawable != null) {
      chipDrawable.setMaxWidth(maxWidth);
    }
  }

  @Override
  public void onChipDrawableSizeChange() {
    ensureAccessibleTouchTarget(minTouchTargetSize);
    requestLayout();
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      invalidateOutline();
    }
  }

  @Override
  public void setChecked(boolean checked) {
    if (chipDrawable == null) {
      // Defer the setChecked() call until after initialization.
      deferredCheckedValue = checked;
    } else if (chipDrawable.isCheckable()) {
      boolean wasChecked = isChecked();
      super.setChecked(checked);

      if (wasChecked != checked) {
        if (onCheckedChangeListenerInternal != null) {
          onCheckedChangeListenerInternal.onCheckedChanged(this, checked);
        }
      }
    }
  }

  /**
   * Register a callback to be invoked when the checked state of this chip changes. This callback is
   * used for internal purpose only.
   */
  void setOnCheckedChangeListenerInternal(OnCheckedChangeListener listener) {
    onCheckedChangeListenerInternal = listener;
  }

  /** Register a callback to be invoked when the close icon is clicked. */
  public void setOnCloseIconClickListener(OnClickListener listener) {
    this.onCloseIconClickListener = listener;
    updateAccessibilityDelegate();
  }

  /**
   * Call this chip's close icon click listener, if it is defined. Performs all normal actions
   * associated with clicking: reporting accessibility event, playing a sound, etc.
   *
   * @return True there was an assigned close icon click listener that was called, false otherwise
   *     is returned.
   * @see #setOnCloseIconClickListener(OnClickListener)
   */
  @CallSuper
  public boolean performCloseIconClick() {
    playSoundEffect(SoundEffectConstants.CLICK);

    boolean result;
    if (onCloseIconClickListener != null) {
      onCloseIconClickListener.onClick(this);
      result = true;
    } else {
      result = false;
    }

    touchHelper.sendEventForVirtualView(
        CLOSE_ICON_VIRTUAL_ID, AccessibilityEvent.TYPE_VIEW_CLICKED);
    return result;
  }

  @SuppressLint("ClickableViewAccessibility") // There's an accessibility delegate that will handle
  // interactions with the trailing chip icon.
  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    boolean handled = false;

    int action = event.getActionMasked();
    boolean eventInCloseIcon = getCloseIconTouchBounds().contains(event.getX(), event.getY());
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        if (eventInCloseIcon) {
          setCloseIconPressed(true);
          handled = true;
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (closeIconPressed) {
          if (!eventInCloseIcon) {
            setCloseIconPressed(false);
          }
          handled = true;
        }
        break;
      case MotionEvent.ACTION_UP:
        if (closeIconPressed) {
          performCloseIconClick();
          handled = true;
        }
        // Fall-through.
      case MotionEvent.ACTION_CANCEL:
        setCloseIconPressed(false);
        break;
      default:
        break;
    }
    return handled || super.onTouchEvent(event);
  }

  @Override
  public boolean onHoverEvent(@NonNull MotionEvent event) {
    int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_HOVER_MOVE:
        setCloseIconHovered(getCloseIconTouchBounds().contains(event.getX(), event.getY()));
        break;
      case MotionEvent.ACTION_HOVER_EXIT:
        setCloseIconHovered(false);
        break;
      default:
        break;
    }
    return super.onHoverEvent(event);
  }

  // There is a bug which causes the AccessibilityEvent.TYPE_VIEW_HOVER_ENTER and
  // AccessibilityEvent.TYPE_VIEW_HOVER_EXIT events to only fire the first time a chip gets focused.
  // Until the accessibility focus bug is fixed in ExploreByTouchHelper, we simulate the correct
  // behavior here. Once that bug is fixed we can remove this.
  @SuppressLint("PrivateApi")
  private boolean handleAccessibilityExit(@NonNull MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
      try {
        Field f = ExploreByTouchHelper.class.getDeclaredField("mHoveredVirtualViewId");
        f.setAccessible(true);
        int mHoveredVirtualViewId = (int) f.get(touchHelper);

        if (mHoveredVirtualViewId != ExploreByTouchHelper.INVALID_ID) {
          Method m =
              ExploreByTouchHelper.class.getDeclaredMethod("updateHoveredVirtualView", int.class);
          m.setAccessible(true);
          m.invoke(touchHelper, ExploreByTouchHelper.INVALID_ID);
          return true;
        }
      } catch (NoSuchMethodException e) {
        // Multi-catch for reflection requires API level 19
        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      } catch (IllegalAccessException e) {
        // Multi-catch for reflection requires API level 19
        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      } catch (InvocationTargetException e) {
        // Multi-catch for reflection requires API level 19
        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      } catch (NoSuchFieldException e) {
        // Multi-catch for reflection requires API level 19
        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      }
    }
    return false;
  }

  @Override
  protected boolean dispatchHoverEvent(@NonNull MotionEvent event) {
    return handleAccessibilityExit(event)
        || touchHelper.dispatchHoverEvent(event)
        || super.dispatchHoverEvent(event);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    boolean handled = touchHelper.dispatchKeyEvent(event);
    // If the key event moves focus one beyond the end of the virtual view hierarchy in the
    // traversal direction (i.e. beyond the last virtual view while moving forward or before the
    // first virtual view while traversing backward), ExploreByTouchHelper will erroneously report
    // that it consumed the key event even though it does not move focus to the next or previous
    // real view. In order to account for this, call through to super to move focus to the correct
    // real view.
    if (handled
        && touchHelper.getKeyboardFocusedVirtualViewId() != ExploreByTouchHelper.INVALID_ID) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect);
    touchHelper.onFocusChanged(focused, direction, previouslyFocusedRect);
  }

  @Override
  public void getFocusedRect(@NonNull Rect r) {
    if (touchHelper.getKeyboardFocusedVirtualViewId() == CLOSE_ICON_VIRTUAL_ID
        || touchHelper.getAccessibilityFocusedVirtualViewId() == CLOSE_ICON_VIRTUAL_ID) {
      r.set(getCloseIconTouchBoundsInt());
    } else {
      super.getFocusedRect(r);
    }
  }

  private void setCloseIconPressed(boolean pressed) {
    if (closeIconPressed != pressed) {
      closeIconPressed = pressed;
      refreshDrawableState();
    }
  }

  private void setCloseIconHovered(boolean hovered) {
    if (closeIconHovered != hovered) {
      closeIconHovered = hovered;
      refreshDrawableState();
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    boolean changed = false;

    if (chipDrawable != null && chipDrawable.isCloseIconStateful()) {
      changed = chipDrawable.setCloseIconState(createCloseIconDrawableState());
    }

    if (changed) {
      invalidate();
    }
  }

  @NonNull
  private int[] createCloseIconDrawableState() {
    int count = 0;
    if (isEnabled()) {
      count++;
    }
    if (closeIconFocused) {
      count++;
    }
    if (closeIconHovered) {
      count++;
    }
    if (closeIconPressed) {
      count++;
    }
    if (isChecked()) {
      count++;
    }

    int[] stateSet = new int[count];
    int i = 0;

    if (isEnabled()) {
      stateSet[i] = android.R.attr.state_enabled;
      i++;
    }
    if (closeIconFocused) {
      stateSet[i] = android.R.attr.state_focused;
      i++;
    }
    if (closeIconHovered) {
      stateSet[i] = android.R.attr.state_hovered;
      i++;
    }
    if (closeIconPressed) {
      stateSet[i] = android.R.attr.state_pressed;
      i++;
    }
    if (isChecked()) {
      stateSet[i] = android.R.attr.state_selected;
      i++;
    }
    return stateSet;
  }

  private boolean hasCloseIcon() {
    return chipDrawable != null && chipDrawable.getCloseIcon() != null;
  }

  @NonNull
  private RectF getCloseIconTouchBounds() {
    rectF.setEmpty();

    if (hasCloseIcon() && onCloseIconClickListener != null) {
      // noinspection ConstantConditions
      chipDrawable.getCloseIconTouchBounds(rectF);
    }

    return rectF;
  }

  @NonNull
  private Rect getCloseIconTouchBoundsInt() {
    RectF bounds = getCloseIconTouchBounds();
    rect.set((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom);
    return rect;
  }

  @Nullable
  @Override
  @TargetApi(VERSION_CODES.N)
  public PointerIcon onResolvePointerIcon(@NonNull MotionEvent event, int pointerIndex) {
    if (getCloseIconTouchBounds().contains(event.getX(), event.getY()) && isEnabled()) {
      return PointerIcon.getSystemIcon(getContext(), PointerIcon.TYPE_HAND);
    }
    return null;
  }

  /** Provides a virtual view hierarchy for the close icon. */
  private class ChipTouchHelper extends ExploreByTouchHelper {

    ChipTouchHelper(Chip view) {
      super(view);
    }

    @Override
    protected int getVirtualViewAt(float x, float y) {
      return (hasCloseIcon() && getCloseIconTouchBounds().contains(x, y))
          ? CLOSE_ICON_VIRTUAL_ID
          : CHIP_BODY_VIRTUAL_ID;
    }

    @Override
    protected void getVisibleVirtualViews(@NonNull List<Integer> virtualViewIds) {
      virtualViewIds.add(CHIP_BODY_VIRTUAL_ID);
      if (hasCloseIcon() && isCloseIconVisible() && onCloseIconClickListener != null) {
        virtualViewIds.add(CLOSE_ICON_VIRTUAL_ID);
      }
    }

    @Override
    protected void onVirtualViewKeyboardFocusChanged(int virtualViewId, boolean hasFocus) {
      if (virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
        closeIconFocused = hasFocus;
        refreshDrawableState();
      }
    }

    @Override
    protected void onPopulateNodeForVirtualView(
        int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
      if (virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
        CharSequence closeIconContentDescription = getCloseIconContentDescription();
        if (closeIconContentDescription != null) {
          node.setContentDescription(closeIconContentDescription);
        } else {
          CharSequence chipText = getText();
          node.setContentDescription(
              getContext()
                  .getString(
                      R.string.mtrl_chip_close_icon_content_description,
                      !TextUtils.isEmpty(chipText) ? chipText : "")
                  .trim());
        }
        node.setBoundsInParent(getCloseIconTouchBoundsInt());
        node.addAction(AccessibilityActionCompat.ACTION_CLICK);
        node.setEnabled(isEnabled());
      } else {
        node.setContentDescription("");
        node.setBoundsInParent(EMPTY_BOUNDS);
      }
    }

    @Override
    protected void onPopulateNodeForHost(@NonNull AccessibilityNodeInfoCompat node) {
      node.setCheckable(isCheckable());
      node.setClickable(isClickable());
      if (isCheckable() || isClickable()) {
        node.setClassName(
            isCheckable()
                ? COMPOUND_BUTTON_ACCESSIBILITY_CLASS_NAME
                : BUTTON_ACCESSIBILITY_CLASS_NAME);
      } else {
        node.setClassName(GENERIC_VIEW_ACCESSIBILITY_CLASS_NAME);
      }
      CharSequence chipText = getText();
      if (VERSION.SDK_INT >= VERSION_CODES.M) {
        node.setText(chipText);
      } else {
        // Before M, TalkBack doesn't get the text from setText, so we have to set the content
        // description instead.
        node.setContentDescription(chipText);
      }
    }

    @Override
    protected boolean onPerformActionForVirtualView(
        int virtualViewId, int action, Bundle arguments) {
      if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
        if (virtualViewId == CHIP_BODY_VIRTUAL_ID) {
          return performClick();
        } else if (virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
          return performCloseIconClick();
        }
      }
      return false;
    }
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
    return chipDrawable != null ? chipDrawable.getChipBackgroundColor() : null;
  }

  /**
   * Sets this chip's background color using a resource id.
   *
   * @param id The resource id of this chip's background color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipBackgroundColor
   */
  public void setChipBackgroundColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipBackgroundColorResource(id);
    }
  }

  /**
   * Sets this chip's background color.
   *
   * @param chipBackgroundColor This chip's background color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipBackgroundColor
   */
  public void setChipBackgroundColor(@Nullable ColorStateList chipBackgroundColor) {
    if (chipDrawable != null) {
      chipDrawable.setChipBackgroundColor(chipBackgroundColor);
    }
  }

  /**
   * Returns this chip's minimum height.
   *
   * @see #setChipMinHeight(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipMinHeight
   */
  public float getChipMinHeight() {
    return chipDrawable != null ? chipDrawable.getChipMinHeight() : 0;
  }

  /**
   * Sets this chip's minimum height using a resource id.
   *
   * @param id The resource id of this chip's mininum height.
   * @attr ref com.google.android.material.R.styleable#Chip_chipMinHeight
   */
  public void setChipMinHeightResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipMinHeightResource(id);
    }
  }

  /**
   * Sets this chip's minimum height.
   *
   * @param minHeight This chip's mininum height.
   * @attr ref com.google.android.material.R.styleable#Chip_chipMinHeight
   */
  public void setChipMinHeight(float minHeight) {
    if (chipDrawable != null) {
      chipDrawable.setChipMinHeight(minHeight);
    }
  }

  /**
   * Returns this chip's corner radius.
   *
   * @see #setChipCornerRadius(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipCornerRadius
   */
  public float getChipCornerRadius() {
    return chipDrawable != null ? Math.max(0, chipDrawable.getChipCornerRadius()) : 0;
  }

  /**
   * @deprecated call {@link ShapeAppearanceModel#withCornerSize(float)} or call {@link
   *     ShapeAppearanceModel#toBuilder()} on the {@link #getShapeAppearanceModel()}, modify the
   *     shape using the builder and then call {@link
   *     #setShapeAppearanceModel(ShapeAppearanceModel)}.
   */
  @Deprecated
  public void setChipCornerRadiusResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipCornerRadiusResource(id);
    }
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    chipDrawable.setShapeAppearanceModel(shapeAppearanceModel);
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return chipDrawable.getShapeAppearanceModel();
  }

  /**
   * @deprecated call {@link ShapeAppearanceModel#withCornerSize(float)} or call {@link
   *     ShapeAppearanceModel#toBuilder()} on the {@link #getShapeAppearanceModel()}, modify the
   *     shape using the builder and then call {@link
   *     #setShapeAppearanceModel(ShapeAppearanceModel)}.
   */
  @Deprecated
  public void setChipCornerRadius(float chipCornerRadius) {
    if (chipDrawable != null) {
      chipDrawable.setChipCornerRadius(chipCornerRadius);
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
    return chipDrawable != null ? chipDrawable.getChipStrokeColor() : null;
  }

  /**
   * Sets this chip's stroke color using a resource id.
   *
   * @param id The resource id of this chip's stroke color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeColor
   */
  public void setChipStrokeColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeColorResource(id);
    }
  }

  /**
   * Sets this chip's stroke color.
   *
   * @param chipStrokeColor This chip's stroke color.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeColor
   */
  public void setChipStrokeColor(@Nullable ColorStateList chipStrokeColor) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeColor(chipStrokeColor);
    }
  }

  /**
   * Returns this chip's stroke width.
   *
   * @see #setChipStrokeWidth(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeWidth
   */
  public float getChipStrokeWidth() {
    return chipDrawable != null ? chipDrawable.getChipStrokeWidth() : 0;
  }

  /**
   * Sets this chip's stroke width using a resource id.
   *
   * @param id The resource id of this chip's stroke width.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeWidth
   */
  public void setChipStrokeWidthResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeWidthResource(id);
    }
  }

  /**
   * Sets this chip's stroke width.
   *
   * @param chipStrokeWidth This chip's stroke width.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStrokeWidth
   */
  public void setChipStrokeWidth(float chipStrokeWidth) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeWidth(chipStrokeWidth);
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
    return chipDrawable != null ? chipDrawable.getRippleColor() : null;
  }

  /**
   * Sets this chip's ripple color using a resource id.
   *
   * @param id The resource id of this chip's ripple color.
   * @attr ref com.google.android.material.R.styleable#Chip_rippleColor
   */
  public void setRippleColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setRippleColorResource(id);
      if (!chipDrawable.getUseCompatRipple()) {
        updateFrameworkRippleBackground();
      }
    }
  }

  /**
   * Sets this chip's ripple color.
   *
   * @param rippleColor This chip's ripple color.
   * @attr ref com.google.android.material.R.styleable#Chip_rippleColor
   */
  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (chipDrawable != null) {
      chipDrawable.setRippleColor(rippleColor);
    }
    if (!chipDrawable.getUseCompatRipple()) {
      updateFrameworkRippleBackground();
    }
  }

  /**
   * Returns this chip's text.
   *
   * @deprecated Use {@link Chip#getText()} instead.
   */
  @Deprecated
  public CharSequence getChipText() {
    return getText();
  }

  @Override
  public void setLayoutDirection(int layoutDirection) {
    if (chipDrawable == null) {
      return;
    }
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      super.setLayoutDirection(layoutDirection);
    }
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    if (chipDrawable == null) {
      return;
    }
    if (text == null) {
      text = "";
    }
    super.setText(chipDrawable.shouldDrawText() ? null : text, type);
    if (chipDrawable != null) {
      chipDrawable.setText(text);
    }
  }

  /** @deprecated Use {@link Chip#setText(int)} instead. */
  @Deprecated
  public void setChipTextResource(@StringRes int id) {
    setText(getResources().getString(id));
  }

  /** @deprecated Use {@link Chip#setText(CharSequence)} instead. */
  @Deprecated
  public void setChipText(@Nullable CharSequence chipText) {
    setText(chipText);
  }

  /**
   * Sets this chip's text appearance using a resource id.
   *
   * @param id The resource id of this chip's text appearance.
   * @attr ref com.google.android.material.R.styleable#Chip_android_textAppearance
   */
  public void setTextAppearanceResource(@StyleRes int id) {
    this.setTextAppearance(getContext(), id);
  }

  /**
   * Sets this chip's text appearance.
   *
   * @param textAppearance This chip's text appearance.
   * @attr ref com.google.android.material.R.styleable#Chip_android_textAppearance
   */
  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (chipDrawable != null) {
      chipDrawable.setTextAppearance(textAppearance);
    }
    updateTextPaintDrawState();
  }

  @Override
  public void setTextAppearance(Context context, int resId) {
    super.setTextAppearance(context, resId);
    if (chipDrawable != null) {
      chipDrawable.setTextAppearanceResource(resId);
    }
    updateTextPaintDrawState();
  }

  @Override
  public void setTextAppearance(int resId) {
    super.setTextAppearance(resId);
    if (chipDrawable != null) {
      chipDrawable.setTextAppearanceResource(resId);
    }
    updateTextPaintDrawState();
  }

  private void updateTextPaintDrawState() {
    TextPaint textPaint = getPaint();
    if (chipDrawable != null) {
      textPaint.drawableState = chipDrawable.getState();
    }
    TextAppearance textAppearance = getTextAppearance();
    if (textAppearance != null) {
      textAppearance.updateDrawState(getContext(), textPaint, fontCallback);
    }
  }

  @Nullable
  private TextAppearance getTextAppearance() {
    return chipDrawable != null ? chipDrawable.getTextAppearance() : null;
  }

  /**
   * Returns whether this chip's icon is visible.
   *
   * @see #setChipIconVisible(boolean)
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconVisible
   */
  public boolean isChipIconVisible() {
    return chipDrawable != null && chipDrawable.isChipIconVisible();
  }

  /** @deprecated Use {@link Chip#isChipIconVisible()} instead. */
  @Deprecated
  public boolean isChipIconEnabled() {
    return isChipIconVisible();
  }

  /**
   * Sets the visibility of this chip's icon using a resource id.
   *
   * @param id The resource id for the visibility of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconVisible
   */
  public void setChipIconVisible(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconVisible(id);
    }
  }

  /**
   * Sets whether this chip's icon is visible.
   *
   * @param chipIconVisible The visibility of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconIsVisible
   */
  public void setChipIconVisible(boolean chipIconVisible) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconVisible(chipIconVisible);
    }
  }

  /** @deprecated Use {@link Chip#setChipIconVisible(int)} instead. */
  @Deprecated
  public void setChipIconEnabledResource(@BoolRes int id) {
    setChipIconVisible(id);
  }

  /** @deprecated Use {@link Chip#setChipIconVisible(boolean)} instead. */
  @Deprecated
  public void setChipIconEnabled(boolean chipIconEnabled) {
    setChipIconVisible(chipIconEnabled);
  }

  /**
   * Returns this chip's icon.
   *
   * @see #setChipIcon(Drawable)
   * @attr ref com.google.android.material.R.styleable#Chip_chipIcon
   */
  @Nullable
  public Drawable getChipIcon() {
    return chipDrawable != null ? chipDrawable.getChipIcon() : null;
  }

  /**
   * Sets this chip's icon using a resource id.
   *
   * @param id The resource id for this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIcon
   */
  public void setChipIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconResource(id);
    }
  }

  /**
   * Sets this chip's icon.
   *
   * @param chipIcon drawable of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIcon
   */
  public void setChipIcon(@Nullable Drawable chipIcon) {
    if (chipDrawable != null) {
      chipDrawable.setChipIcon(chipIcon);
    }
  }

  /**
   * Returns the {@link android.content.res.ColorStateList} used to tint the chip icon.
   *
   * @see #setChipIconTint(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  @Nullable
  public ColorStateList getChipIconTint() {
    return chipDrawable != null ? chipDrawable.getChipIconTint() : null;
  }

  /**
   * Sets this chip icon's color tint using a resource id.
   *
   * @param id The resource id for tinting the chip icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  public void setChipIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconTintResource(id);
    }
  }

  /**
   * Sets this chip icon's color tint using the specified {@link
   * android.content.res.ColorStateList}.
   *
   * @param chipIconTint The tint color of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  public void setChipIconTint(@Nullable ColorStateList chipIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconTint(chipIconTint);
    }
  }

  /**
   * Returns this chip's icon size.
   * If a non-positive value is set, the icon drawable's width and height (up to 24dp) will be used
   * instead.
   *
   * @see #setChipIconSize(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconTint
   */
  public float getChipIconSize() {
    return chipDrawable != null ? chipDrawable.getChipIconSize() : 0;
  }

  /**
   * Sets this chip icon's size using a resource id.
   * If the value is zero or negative, the icon drawable's width and height (up to 24dp) will be
   * used instead.
   *
   * @param id The resource id of this chip's icon size.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconSize
   */
  public void setChipIconSizeResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconSizeResource(id);
    }
  }

  /**
   * Sets this chip icon's size.
   * If the value is zero or negative, the icon drawable's width and height (up to 24dp) will be
   * used instead.
   *
   * @param chipIconSize This chip's icon size.
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconSize
   */
  public void setChipIconSize(float chipIconSize) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconSize(chipIconSize);
    }
  }

  /**
   * Returns whether this chip's close icon is visible.
   *
   * @see id #setCloseIconVisible(boolean)
   * @attr ref com.google.android.material.R.styleable#Chip_chipIconSize
   */
  public boolean isCloseIconVisible() {
    return chipDrawable != null && chipDrawable.isCloseIconVisible();
  }

  /** @deprecated Use {@link Chip#isCloseIconVisible()} instead. */
  @Deprecated
  public boolean isCloseIconEnabled() {
    return isCloseIconVisible();
  }

  /**
   * Sets whether this chip close icon is visible using a resource id.
   *
   * @param id The resource id of this chip's close icon visibility.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconVisible
   */
  public void setCloseIconVisible(@BoolRes int id) {
    setCloseIconVisible(getResources().getBoolean(id));
  }

  /**
   * Sets whether this chip close icon is visible.
   *
   * @param closeIconVisible This chip's close icon visibility.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconVisible
   */
  public void setCloseIconVisible(boolean closeIconVisible) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconVisible(closeIconVisible);
    }
    updateAccessibilityDelegate();
  }

  /** @deprecated Use {@link Chip#setCloseIconVisible(int)} instead. */
  @Deprecated
  public void setCloseIconEnabledResource(@BoolRes int id) {
    setCloseIconVisible(id);
  }

  /** @deprecated Use {@link Chip#setCloseIconVisible(boolean)} instead. */
  @Deprecated
  public void setCloseIconEnabled(boolean closeIconEnabled) {
    setCloseIconVisible(closeIconEnabled);
  }

  /**
   * Returns this chip's close icon.
   *
   * @see #setCloseIcon(Drawable).
   * @attr ref com.google.android.material.R.styleable#Chip_closeIcon
   */
  @Nullable
  public Drawable getCloseIcon() {
    return chipDrawable != null ? chipDrawable.getCloseIcon() : null;
  }

  /**
   * Sets this chip's close icon using a resource id.
   *
   * @param id The resource id of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIcon
   */
  public void setCloseIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconResource(id);
    }
    updateAccessibilityDelegate();
  }

  /**
   * Sets this chip's close icon.
   *
   * @param closeIcon This chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIcon
   */
  public void setCloseIcon(@Nullable Drawable closeIcon) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIcon(closeIcon);
    }
    updateAccessibilityDelegate();
  }

  /**
   * Returns the tint color for this chip's close icon.
   *
   * @see #setCloseIconTint(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconTint
   */
  @Nullable
  public ColorStateList getCloseIconTint() {
    return chipDrawable != null ? chipDrawable.getCloseIconTint() : null;
  }

  /**
   * Sets the tint color for this chip's close icon using a resource id.
   *
   * @param id The resource id of this chip's close icon tint.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconTint
   */
  public void setCloseIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconTintResource(id);
    }
  }

  /**
   * Sets the tint color for this chip's close icon.
   *
   * @param closeIconTint This chip's close icon tint.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconTint
   */
  public void setCloseIconTint(@Nullable ColorStateList closeIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconTint(closeIconTint);
    }
  }

  /**
   * Returns this chip's close icon size.
   *
   * @see #setCloseIconSize(float)
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconSize
   */
  public float getCloseIconSize() {
    return chipDrawable != null ? chipDrawable.getCloseIconSize() : 0;
  }

  /**
   * Sets this chip's close icon size using a resource id.
   *
   * @param id The resource id of this chip's close icon size.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconSize
   */
  public void setCloseIconSizeResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconSizeResource(id);
    }
  }

  /**
   * Sets this chip's close icon size.
   *
   * @param closeIconSize This chip's close icon size.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconSize
   */
  public void setCloseIconSize(float closeIconSize) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconSize(closeIconSize);
    }
  }

  /**
   * Sets the content description for this chip's close icon.
   *
   * @param closeIconContentDescription The content description for this chip's close icon.
   */
  public void setCloseIconContentDescription(@Nullable CharSequence closeIconContentDescription) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconContentDescription(closeIconContentDescription);
    }
  }

  /**
   * Returns this chip's close icon content description.
   *
   * @see #setCloseIconContentDescription(CharSequence)
   */
  @Nullable
  public CharSequence getCloseIconContentDescription() {
    return chipDrawable != null ? chipDrawable.getCloseIconContentDescription() : null;
  }

  /**
   * Returns whether this chip is checkable.
   *
   * @see #setCheckable(boolean)
   * @attr ref com.google.android.material.R.styleable#Chip_android_checkable
   */
  public boolean isCheckable() {
    return chipDrawable != null && chipDrawable.isCheckable();
  }

  /**
   * Sets whether this chip is checkable using a resource id.
   *
   * @param id The resource id of this chip is checkable.
   * @attr ref com.google.android.material.R.styleable#Chip_android_checkable
   */
  public void setCheckableResource(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckableResource(id);
    }
  }

  /**
   * Sets whether this chip is checkable.
   *
   * @param checkable Whether this chip is checkable.
   * @attr ref com.google.android.material.R.styleable#Chip_android_checkable
   */
  public void setCheckable(boolean checkable) {
    if (chipDrawable != null) {
      chipDrawable.setCheckable(checkable);
    }
  }

  /**
   * Returns whether this chip's checked icon is visible.
   *
   * @see #setCheckedIconVisible(boolean)
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconVisible
   */
  public boolean isCheckedIconVisible() {
    return chipDrawable != null && chipDrawable.isCheckedIconVisible();
  }

  /** @deprecated Use {@link Chip#isCheckedIconVisible()} instead. */
  @Deprecated
  public boolean isCheckedIconEnabled() {
    return isCheckedIconVisible();
  }

  /**
   * Sets whether this chip's checked icon is visible using a resource id.
   *
   * @param id The resource id of this chip's check icon visibility.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconVisible
   */
  public void setCheckedIconVisible(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconVisible(id);
    }
  }

  /**
   * Sets whether this chip's checked icon is visible.
   *
   * @param checkedIconVisible This chip's checked icon visibility.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconVisible
   */
  public void setCheckedIconVisible(boolean checkedIconVisible) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconVisible(checkedIconVisible);
    }
  }

  /** @deprecated Use {@link Chip#setCheckedIconVisible(int)} instead. */
  @Deprecated
  public void setCheckedIconEnabledResource(@BoolRes int id) {
    setCheckedIconVisible(id);
  }

  /** @deprecated Use {@link Chip#setCheckedIconVisible(boolean)} instead. */
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
    return chipDrawable != null ? chipDrawable.getCheckedIcon() : null;
  }

  /**
   * Sets this chip's checked icon using a resource id.
   *
   * @param id The resource id of this chip's checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIcon
   */
  public void setCheckedIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconResource(id);
    }
  }

  /**
   * Sets this chip's checked icon.
   *
   * @param checkedIcon This chip's checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIcon
   */
  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIcon(checkedIcon);
    }
  }

  /**
   * Returns the {@link android.content.res.ColorStateList} used to tint the checked icon.
   *
   * @see #setCheckedIconTint(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconTint
   */
  @Nullable
  public ColorStateList getCheckedIconTint() {
    return chipDrawable != null ? chipDrawable.getCheckedIconTint() : null;
  }

  /**
   * Sets this chip's checked icon's color tint using a resource id.
   *
   * @param id The resource id for tinting the checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconTint
   */
  public void setCheckedIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconTintResource(id);
    }
  }

  /**
   * Sets this chip's checked icon's color tint using the specified {@link
   * android.content.res.ColorStateList}.
   *
   * @param checkedIconTint The tint color of this chip's checked icon.
   * @attr ref com.google.android.material.R.styleable#Chip_checkedIconTint
   */
  public void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconTint(checkedIconTint);
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
    return chipDrawable != null ? chipDrawable.getShowMotionSpec() : null;
  }

  /**
   * Sets this chip's show motion spec using a resource id.
   *
   * @param id The resource id of this chip's show motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_showMotionSpec
   */
  public void setShowMotionSpecResource(@AnimatorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setShowMotionSpecResource(id);
    }
  }

  /**
   * Sets this chip's show motion spec.
   *
   * @param showMotionSpec This chip's show motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_showMotionSpec
   */
  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    if (chipDrawable != null) {
      chipDrawable.setShowMotionSpec(showMotionSpec);
    }
  }

  /**
   * Returns this chip's hide motion spec.
   *
   * @see #setHideMotionSpec(MotionSpec)
   * @attr ref com.google.android.material.R.styleable#Chip_hideMotionSpec
   */
  @Nullable
  public MotionSpec getHideMotionSpec() {
    return chipDrawable != null ? chipDrawable.getHideMotionSpec() : null;
  }

  /**
   * Sets this chip's hide motion spec using a resource id.
   *
   * @param id The resource id of this chip's hide motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_hideMotionSpec
   */
  public void setHideMotionSpecResource(@AnimatorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setHideMotionSpecResource(id);
    }
  }

  /**
   * Sets this chip's hide motion spec.
   *
   * @param hideMotionSpec This chip's hide motion spec.
   * @attr ref com.google.android.material.R.styleable#Chip_hideMotionSpec
   */
  public void setHideMotionSpec(@Nullable MotionSpec hideMotionSpec) {
    if (chipDrawable != null) {
      chipDrawable.setHideMotionSpec(hideMotionSpec);
    }
  }

  /**
   * Returns this chip's start padding.
   *
   * @see #setChipStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipStartPadding
   */
  public float getChipStartPadding() {
    return chipDrawable != null ? chipDrawable.getChipStartPadding() : 0;
  }

  /**
   * Sets this chip's start padding using a resource id.
   *
   * @param id The resource id of this chip's start padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStartPadding
   */
  public void setChipStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStartPaddingResource(id);
    }
  }

  /**
   * Sets this chip's start padding.
   *
   * @param chipStartPadding This chip's start padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipStartPadding
   */
  public void setChipStartPadding(float chipStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setChipStartPadding(chipStartPadding);
    }
  }

  /**
   * Returns the start padding for this chip's icon.
   *
   * @see #setIconStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_iconStartPadding
   */
  public float getIconStartPadding() {
    return chipDrawable != null ? chipDrawable.getIconStartPadding() : 0;
  }

  /**
   * Sets the start padding for this chip's icon using a resource id.
   *
   * @param id The resource id for the start padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconStartPadding
   */
  public void setIconStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setIconStartPaddingResource(id);
    }
  }

  /**
   * Sets this chip's icon start padding.
   *
   * @param iconStartPadding The start padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconStartPadding
   */
  public void setIconStartPadding(float iconStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setIconStartPadding(iconStartPadding);
    }
  }

  /**
   * Returns the end padding for this chip's icon.
   *
   * @see #setIconEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_iconEndPadding
   */
  public float getIconEndPadding() {
    return chipDrawable != null ? chipDrawable.getIconEndPadding() : 0;
  }

  /**
   * Sets the end padding for this chip's icon using a resource id.
   *
   * @param id The resource id for the end padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconEndPadding
   */
  public void setIconEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setIconEndPaddingResource(id);
    }
  }

  /**
   * Sets the end padding for this chip's icon.
   *
   * @param iconEndPadding The end padding of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#Chip_iconEndPadding
   */
  public void setIconEndPadding(float iconEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setIconEndPadding(iconEndPadding);
    }
  }

  /**
   * Returns the start padding for this chip's text.
   *
   * @see #setTextStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public float getTextStartPadding() {
    return chipDrawable != null ? chipDrawable.getTextStartPadding() : 0;
  }

  /**
   * Sets the start padding for this chip's text using a resource id.
   *
   * @param id The resource id for the start padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public void setTextStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextStartPaddingResource(id);
    }
  }

  /**
   * Sets the start padding for this chip's text.
   *
   * @param textStartPadding The start padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public void setTextStartPadding(float textStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setTextStartPadding(textStartPadding);
    }
  }

  /**
   * Returns the end padding for this chip's text.
   *
   * @see #setTextEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_textEndPadding
   */
  public float getTextEndPadding() {
    return chipDrawable != null ? chipDrawable.getTextEndPadding() : 0;
  }

  /**
   * Sets the end padding for this chip's text using a resource id.
   *
   * @param id The resource id for the end padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textEndPadding
   */
  public void setTextEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextEndPaddingResource(id);
    }
  }

  /**
   * Sets the end padding for this chip's text.
   *
   * @param textEndPadding The end padding of this chip's text.
   * @attr ref com.google.android.material.R.styleable#Chip_textStartPadding
   */
  public void setTextEndPadding(float textEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setTextEndPadding(textEndPadding);
    }
  }

  /**
   * Returns the start padding for this chip's close icon.
   *
   * @see #setCloseIconStartPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconStartPadding
   */
  public float getCloseIconStartPadding() {
    return chipDrawable != null ? chipDrawable.getCloseIconStartPadding() : 0;
  }

  /**
   * Sets the start padding for this chip's close icon using a resource id.
   *
   * @param id The resource id for the start padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconStartPadding
   */
  public void setCloseIconStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconStartPaddingResource(id);
    }
  }

  /**
   * Sets the start padding for this chip's close icon.
   *
   * @param closeIconStartPadding The start padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconStartPadding
   */
  public void setCloseIconStartPadding(float closeIconStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconStartPadding(closeIconStartPadding);
    }
  }

  /**
   * Returns the end padding for this chip's close icon.
   *
   * @see #setCloseIconEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconEndPadding
   */
  public float getCloseIconEndPadding() {
    return chipDrawable != null ? chipDrawable.getCloseIconEndPadding() : 0;
  }

  /**
   * Sets the end padding for this chip's close icon using a resource id.
   *
   * @param id The resource id for the end padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconEndPadding
   */
  public void setCloseIconEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEndPaddingResource(id);
    }
  }

  /**
   * Sets the end padding for this chip's close icon.
   *
   * @param closeIconEndPadding The end padding of this chip's close icon.
   * @attr ref com.google.android.material.R.styleable#Chip_closeIconEndPadding
   */
  public void setCloseIconEndPadding(float closeIconEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEndPadding(closeIconEndPadding);
    }
  }

  /**
   * Returns this chip's end padding.
   *
   * @see #setChipEndPadding(float)
   * @attr ref com.google.android.material.R.styleable#Chip_chipEndPadding
   */
  public float getChipEndPadding() {
    return chipDrawable != null ? chipDrawable.getChipEndPadding() : 0;
  }

  /**
   * Sets this chip's end padding using a resource id.
   *
   * @param id The resource id for this chip's end padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipEndPadding
   */
  public void setChipEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipEndPaddingResource(id);
    }
  }

  /**
   * Sets this chip's end padding.
   *
   * @param chipEndPadding This chip's end padding.
   * @attr ref com.google.android.material.R.styleable#Chip_chipEndPadding
   */
  public void setChipEndPadding(float chipEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setChipEndPadding(chipEndPadding);
    }
  }

  /**
   * Returns whether this chip will expand its bounds (if needed) to meet the minimum touch target
   * size.
   *
   * @see #setEnsureMinTouchTargetSize(boolean)
   * @attr ref com.google.android.material.R.styleable#Chip_ensureMinTouchTargetSize
   */
  public boolean shouldEnsureMinTouchTargetSize() {
    return ensureMinTouchTargetSize;
  }

  /**
   * Sets whether this chip should expand its bounds (if needed) to meet the minimum touch target
   * size.
   *
   * @param flag Whether this chip should meet the min touch target size.
   * @attr ref com.google.android.material.R.styleable#Chip_ensureMinTouchTargetSize
   */
  public void setEnsureMinTouchTargetSize(boolean flag) {
    ensureMinTouchTargetSize = flag;
    ensureAccessibleTouchTarget(minTouchTargetSize);
  }

  /**
   * Extends the touch target of this chip using a {@link InsetDrawable} if chip's intrinsic width /
   * height is smaller than the {@code minTargetPx}.
   *
   * @param minTargetPx minimum touch target size in pixel
   * @return whether the background was changed
   */
  public boolean ensureAccessibleTouchTarget(@Dimension int minTargetPx) {
    minTouchTargetSize = minTargetPx;
    if (!shouldEnsureMinTouchTargetSize()) {
      if (insetBackgroundDrawable != null) {
        removeBackgroundInset();
      } else {
        updateBackgroundDrawable();
      }
      return false;
    }

    int deltaHeight = Math.max(0, minTargetPx - chipDrawable.getIntrinsicHeight());
    int deltaWidth = Math.max(0, minTargetPx - chipDrawable.getIntrinsicWidth());

    if (deltaWidth <= 0 && deltaHeight <= 0) {
      if (insetBackgroundDrawable != null) {
        removeBackgroundInset();
      } else {
        updateBackgroundDrawable();
      }
      return false;
    }

    int deltaX = deltaWidth > 0 ? deltaWidth / 2 : 0;
    int deltaY = deltaHeight > 0 ? deltaHeight / 2 : 0;

    if (insetBackgroundDrawable != null) {
      Rect padding = new Rect();
      insetBackgroundDrawable.getPadding(padding);
      if (padding.top == deltaY
          && padding.bottom == deltaY
          && padding.left == deltaX
          && padding.right == deltaX) {
        updateBackgroundDrawable();
        return true;
      }
    }
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
      if (getMinHeight() != minTargetPx) {
        setMinHeight(minTargetPx);
      }
      if (getMinWidth() != minTargetPx) {
        setMinWidth(minTargetPx);
      }
    } else {
      setMinHeight(minTargetPx);
      setMinWidth(minTargetPx);
    }
    insetChipBackgroundDrawable(deltaX, deltaY, deltaX, deltaY);
    updateBackgroundDrawable();
    return true;
  }

  private void removeBackgroundInset() {
    if (insetBackgroundDrawable != null) {
      insetBackgroundDrawable = null;
      setMinWidth(0);
      setMinHeight((int) getChipMinHeight());
      updateBackgroundDrawable();
    }
  }

  private void insetChipBackgroundDrawable(
      int insetLeft, int insetTop, int insetRight, int insetBottom) {
    insetBackgroundDrawable =
        new InsetDrawable(chipDrawable, insetLeft, insetTop, insetRight, insetBottom);
  }
}
