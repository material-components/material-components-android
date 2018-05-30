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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.AnimatorRes;
import android.support.annotation.BoolRes;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.chip.ChipDrawable.Delegate;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.ripple.RippleUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
 * <p>All attributes from {@link R.styleable#ChipDrawable} are supported. Do not use the {@code
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
 * @see ChipDrawable
 */
public class Chip extends AppCompatCheckBox implements Delegate {

  private static final String TAG = "Chip";

  private static final int CLOSE_ICON_VIRTUAL_ID = 0;
  private static final Rect EMPTY_BOUNDS = new Rect();

  private static final int[] SELECTED_STATE = new int[] {android.R.attr.state_selected};

  private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ExploreByTouchHelper.INVALID_ID, ExploreByTouchHelper.HOST_ID, CLOSE_ICON_VIRTUAL_ID})
  private @interface VirtualId {}

  @Nullable private ChipDrawable chipDrawable;
  //noinspection NewApi
  @Nullable private RippleDrawable ripple;

  @Nullable private OnClickListener onCloseIconClickListener;
  @Nullable private OnCheckedChangeListener onCheckedChangeListenerInternal;
  private boolean deferredCheckedValue;
  @VirtualId private int focusedVirtualView = ExploreByTouchHelper.INVALID_ID;
  private boolean closeIconPressed;
  private boolean closeIconHovered;
  private boolean closeIconFocused;

  private final ChipTouchHelper touchHelper;
  private final Rect rect = new Rect();
  private final RectF rectF = new RectF();

  public Chip(Context context) {
    this(context, null);
  }

  public Chip(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.chipStyle);
  }

  public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    validateAttributes(attrs);
    ChipDrawable drawable =
        ChipDrawable.createFromAttributes(
            context, attrs, defStyleAttr, R.style.Widget_MaterialComponents_Chip_Action);
    setChipDrawable(drawable);
    // Clear out the text to prevent it from being drawn because ChipDrawable will handle text
    // rendering.
    setText(null);
    touchHelper = new ChipTouchHelper(this);
    ViewCompat.setAccessibilityDelegate(this, touchHelper);
    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

    initOutlineProvider();
    setChecked(deferredCheckedValue);
  }

  private void validateAttributes(@Nullable AttributeSet attributeSet) {
    if (attributeSet == null) {
      return;
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "background") != null) {
      throw new UnsupportedOperationException(
          "Do not set the background; Chip manages its own background drawable.");
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
  }

  private void initOutlineProvider() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void getOutline(View view, Outline outline) {
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
      applyChipDrawable(chipDrawable);

      if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
        //noinspection NewApi
        ripple =
            new RippleDrawable(
                RippleUtils.convertToRippleDrawableColor(chipDrawable.getRippleColor()),
                chipDrawable,
                null);
        chipDrawable.setUseCompatRipple(false);
        //noinspection NewApi
        ViewCompat.setBackground(this, ripple);
      } else {
        chipDrawable.setUseCompatRipple(true);
        ViewCompat.setBackground(this, chipDrawable);
      }
    }
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
    final int[] state = super.onCreateDrawableState(extraSpace + 1);
    if (isChecked()) {
      mergeDrawableStates(state, SELECTED_STATE);
    }
    return state;
  }

  @Override
  public void setBackgroundTintList(@Nullable ColorStateList tint) {
    throw new UnsupportedOperationException(
        "Do not set the background tint list; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundTintMode(@Nullable Mode tintMode) {
    throw new UnsupportedOperationException(
        "Do not set the background tint mode; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundColor(int color) {
    throw new UnsupportedOperationException(
        "Do not set the background color; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundResource(int resid) {
    throw new UnsupportedOperationException(
        "Do not set the background resource; Chip manages its own background drawable.");
  }

  @Override
  public void setBackground(Drawable background) {
    if (background != chipDrawable && background != ripple) {
      throw new UnsupportedOperationException(
          "Do not set the background; Chip manages its own background drawable.");
    } else {
      super.setBackground(background);
    }
  }

  @Override
  public void setBackgroundDrawable(Drawable background) {
    if (background != chipDrawable && background != ripple) {
      throw new UnsupportedOperationException(
          "Do not set the background drawable; Chip manages its own background drawable.");
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

  @Override
  public TruncateAt getEllipsize() {
    return chipDrawable != null ? chipDrawable.getEllipsize() : null;
  }

  @Override
  public void setEllipsize(TruncateAt where) {
    if (where == TruncateAt.MARQUEE) {
      throw new UnsupportedOperationException("Text within a chip are not allowed to scroll.");
    }
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
  public void onChipDrawableSizeChange() {
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
  }

  /**
   * Call this chip's {@link #onCloseIconClickListener}, if it is defined. Performs all normal
   * actions associated with clicking: reporting accessibility event, playing a sound, etc.
   *
   * @return True there was an assigned {@link #onCloseIconClickListener} that was called, false
   *     otherwise is returned.
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

  @Override
  public boolean onTouchEvent(MotionEvent event) {
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
  public boolean onHoverEvent(MotionEvent event) {
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
  private boolean handleAccessibilityExit(MotionEvent event) {
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
  protected boolean dispatchHoverEvent(MotionEvent event) {
    return handleAccessibilityExit(event)
        || touchHelper.dispatchHoverEvent(event)
        || super.dispatchHoverEvent(event);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return touchHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
  }

  @Override
  protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    if (focused) {
      // If we've gained focus from another view, always focus the chip first.
      setFocusedVirtualView(ExploreByTouchHelper.HOST_ID);
    } else {
      setFocusedVirtualView(ExploreByTouchHelper.INVALID_ID);
    }
    invalidate();

    super.onFocusChanged(focused, direction, previouslyFocusedRect);
    touchHelper.onFocusChanged(focused, direction, previouslyFocusedRect);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // We need to handle focus change within the Chip because we are simulating multiple Views. The
    // left/right arrow keys will move between the chip and the close icon. Focus
    // up/down/forward/back jumps out of the Chip to the next focusable View in the hierarchy.
    boolean focusChanged = false;
    switch (event.getKeyCode()) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        if (event.hasNoModifiers()) {
          focusChanged = moveFocus(ViewUtils.isLayoutRtl(this));
        }
        break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        if (event.hasNoModifiers()) {
          focusChanged = moveFocus(!ViewUtils.isLayoutRtl(this));
        }
        break;
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
        switch (focusedVirtualView) {
          case ExploreByTouchHelper.HOST_ID:
            performClick();
            return true;
          case CLOSE_ICON_VIRTUAL_ID:
            performCloseIconClick();
            return true;
          case ExploreByTouchHelper.INVALID_ID:
          default:
            break;
        }
        break;
      case KeyEvent.KEYCODE_TAB:
        int focusChangeDirection = 0;
        if (event.hasNoModifiers()) {
          focusChangeDirection = View.FOCUS_FORWARD;
        } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
          focusChangeDirection = View.FOCUS_BACKWARD;
        }
        if (focusChangeDirection != 0) {
          final ViewParent parent = getParent();
          // Move focus out of this view.
          View nextFocus = this;
          do {
            nextFocus = nextFocus.focusSearch(focusChangeDirection);
          } while (nextFocus != null && nextFocus != this && nextFocus.getParent() == parent);
          if (nextFocus != null) {
            nextFocus.requestFocus();
            return true;
          }
        }
        break;
      default:
        break;
    }
    if (focusChanged) {
      invalidate();
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  private boolean moveFocus(boolean positive) {
    ensureFocus();
    boolean focusChanged = false;
    if (positive) {
      if (focusedVirtualView == ExploreByTouchHelper.HOST_ID) {
        setFocusedVirtualView(CLOSE_ICON_VIRTUAL_ID);
        focusChanged = true;
      }
    } else {
      if (focusedVirtualView == CLOSE_ICON_VIRTUAL_ID) {
        setFocusedVirtualView(ExploreByTouchHelper.HOST_ID);
        focusChanged = true;
      }
    }
    return focusChanged;
  }

  private void ensureFocus() {
    if (focusedVirtualView == ExploreByTouchHelper.INVALID_ID) {
      setFocusedVirtualView(ExploreByTouchHelper.HOST_ID);
    }
  }

  @Override
  public void getFocusedRect(Rect r) {
    if (focusedVirtualView == CLOSE_ICON_VIRTUAL_ID) {
      r.set(getCloseIconTouchBoundsInt());
    } else {
      super.getFocusedRect(r);
    }
  }

  private void setFocusedVirtualView(@VirtualId int virtualView) {
    if (focusedVirtualView != virtualView) {
      if (focusedVirtualView == CLOSE_ICON_VIRTUAL_ID) {
        setCloseIconFocused(false);
      }
      focusedVirtualView = virtualView;
      if (virtualView == CLOSE_ICON_VIRTUAL_ID) {
        setCloseIconFocused(true);
      }
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

  private void setCloseIconFocused(boolean focused) {
    if (closeIconFocused != focused) {
      closeIconFocused = focused;
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

  private RectF getCloseIconTouchBounds() {
    rectF.setEmpty();

    if (hasCloseIcon()) {
      // noinspection ConstantConditions
      chipDrawable.getCloseIconTouchBounds(rectF);
    }

    return rectF;
  }

  private Rect getCloseIconTouchBoundsInt() {
    RectF bounds = getCloseIconTouchBounds();
    rect.set((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom);
    return rect;
  }

  @Override
  @TargetApi(VERSION_CODES.N)
  public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
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
          : HOST_ID;
    }

    @Override
    protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
      if (hasCloseIcon()) {
        virtualViewIds.add(CLOSE_ICON_VIRTUAL_ID);
      }
    }

    @Override
    protected void onPopulateNodeForVirtualView(
        int virtualViewId, AccessibilityNodeInfoCompat node) {
      if (hasCloseIcon()) {
        CharSequence closeIconContentDescription = getCloseIconContentDescription();
        if (closeIconContentDescription != null) {
          node.setContentDescription(closeIconContentDescription);
        } else {
          CharSequence chipText = getChipText();
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
    protected void onPopulateNodeForHost(AccessibilityNodeInfoCompat node) {
      node.setCheckable(chipDrawable != null && chipDrawable.isCheckable());
      node.setClassName(Chip.class.getName());
      CharSequence chipText = chipDrawable != null ? chipDrawable.getText() : "";
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
      if (action == AccessibilityNodeInfoCompat.ACTION_CLICK
          && virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
        return performCloseIconClick();
      }
      return false;
    }
  }

  // Getters and setters for attributes.

  @Nullable
  public ColorStateList getChipBackgroundColor() {
    return chipDrawable != null ? chipDrawable.getChipBackgroundColor() : null;
  }

  public void setChipBackgroundColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipBackgroundColorResource(id);
    }
  }

  public void setChipBackgroundColor(@Nullable ColorStateList chipBackgroundColor) {
    if (chipDrawable != null) {
      chipDrawable.setChipBackgroundColor(chipBackgroundColor);
    }
  }

  public float getChipMinHeight() {
    return chipDrawable != null ? chipDrawable.getChipMinHeight() : 0;
  }

  public void setChipMinHeightResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipMinHeightResource(id);
    }
  }

  public void setChipMinHeight(float minHeight) {
    if (chipDrawable != null) {
      chipDrawable.setChipMinHeight(minHeight);
    }
  }

  public float getChipCornerRadius() {
    return chipDrawable != null ? chipDrawable.getChipCornerRadius() : 0;
  }

  public void setChipCornerRadiusResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipCornerRadiusResource(id);
    }
  }

  public void setChipCornerRadius(float chipCornerRadius) {
    if (chipDrawable != null) {
      chipDrawable.setChipCornerRadius(chipCornerRadius);
    }
  }

  @Nullable
  public ColorStateList getChipStrokeColor() {
    return chipDrawable != null ? chipDrawable.getChipStrokeColor() : null;
  }

  public void setChipStrokeColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeColorResource(id);
    }
  }

  public void setChipStrokeColor(@Nullable ColorStateList chipStrokeColor) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeColor(chipStrokeColor);
    }
  }

  public float getChipStrokeWidth() {
    return chipDrawable != null ? chipDrawable.getChipStrokeWidth() : 0;
  }

  public void setChipStrokeWidthResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeWidthResource(id);
    }
  }

  public void setChipStrokeWidth(float chipStrokeWidth) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeWidth(chipStrokeWidth);
    }
  }

  @Nullable
  public ColorStateList getRippleColor() {
    return chipDrawable != null ? chipDrawable.getRippleColor() : null;
  }

  public void setRippleColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setRippleColorResource(id);
    }
  }

  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (chipDrawable != null) {
      chipDrawable.setRippleColor(rippleColor);
    }
  }

  @Nullable
  public CharSequence getChipText() {
    return chipDrawable != null ? chipDrawable.getText() : null;
  }

  public void setChipTextResource(@StringRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextResource(id);
    }
  }

  public void setChipText(@Nullable CharSequence chipText) {
    if (chipDrawable != null) {
      chipDrawable.setText(chipText);
    }
  }

  @Nullable
  public TextAppearance getTextAppearance() {
    return chipDrawable != null ? chipDrawable.getTextAppearance() : null;
  }

  public void setTextAppearanceResource(@StyleRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextAppearanceResource(id);
    }
  }

  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (chipDrawable != null) {
      chipDrawable.setTextAppearance(textAppearance);
    }
  }

  public boolean isChipIconEnabled() {
    return chipDrawable != null && chipDrawable.isChipIconEnabled();
  }

  public void setChipIconEnabledResource(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconEnabledResource(id);
    }
  }

  public void setChipIconEnabled(boolean chipIconEnabled) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconEnabled(chipIconEnabled);
    }
  }

  @Nullable
  public Drawable getChipIcon() {
    return chipDrawable != null ? chipDrawable.getChipIcon() : null;
  }

  public void setChipIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconResource(id);
    }
  }

  public void setChipIcon(@Nullable Drawable chipIcon) {
    if (chipDrawable != null) {
      chipDrawable.setChipIcon(chipIcon);
    }
  }

  public float getChipIconSize() {
    return chipDrawable != null ? chipDrawable.getChipIconSize() : 0;
  }

  public void setChipIconSizeResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconSizeResource(id);
    }
  }

  public void setChipIconSize(float chipIconSize) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconSize(chipIconSize);
    }
  }

  public boolean isCloseIconEnabled() {
    return chipDrawable != null && chipDrawable.isCloseIconEnabled();
  }

  public void setCloseIconEnabledResource(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEnabledResource(id);
    }
  }

  public void setCloseIconEnabled(boolean closeIconEnabled) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEnabled(closeIconEnabled);
    }
  }

  @Nullable
  public Drawable getCloseIcon() {
    return chipDrawable != null ? chipDrawable.getCloseIcon() : null;
  }

  public void setCloseIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconResource(id);
    }
  }

  public void setCloseIcon(@Nullable Drawable closeIcon) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIcon(closeIcon);
    }
  }

  @Nullable
  public ColorStateList getCloseIconTint() {
    return chipDrawable != null ? chipDrawable.getCloseIconTint() : null;
  }

  public void setCloseIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconTintResource(id);
    }
  }

  public void setCloseIconTint(@Nullable ColorStateList closeIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconTint(closeIconTint);
    }
  }

  public float getCloseIconSize() {
    return chipDrawable != null ? chipDrawable.getCloseIconSize() : 0;
  }

  public void setCloseIconSizeResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconSizeResource(id);
    }
  }

  public void setCloseIconSize(float closeIconSize) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconSize(closeIconSize);
    }
  }

  public void setCloseIconContentDescription(@Nullable CharSequence closeIconContentDescription) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconContentDescription(closeIconContentDescription);
    }
  }

  @Nullable
  public CharSequence getCloseIconContentDescription() {
    return chipDrawable != null ? chipDrawable.getCloseIconContentDescription() : null;
  }

  public boolean isCheckable() {
    return chipDrawable != null && chipDrawable.isCheckable();
  }

  public void setCheckableResource(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckableResource(id);
    }
  }

  public void setCheckable(boolean checkable) {
    if (chipDrawable != null) {
      chipDrawable.setCheckable(checkable);
    }
  }

  public boolean isCheckedIconEnabled() {
    return chipDrawable != null && chipDrawable.isCheckedIconEnabled();
  }

  public void setCheckedIconEnabledResource(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconEnabledResource(id);
    }
  }

  public void setCheckedIconEnabled(boolean checkedIconEnabled) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconEnabled(checkedIconEnabled);
    }
  }

  @Nullable
  public Drawable getCheckedIcon() {
    return chipDrawable != null ? chipDrawable.getCheckedIcon() : null;
  }

  public void setCheckedIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconResource(id);
    }
  }

  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIcon(checkedIcon);
    }
  }

  @Nullable
  public MotionSpec getShowMotionSpec() {
    return chipDrawable != null ? chipDrawable.getShowMotionSpec() : null;
  }

  public void setShowMotionSpecResource(@AnimatorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setShowMotionSpecResource(id);
    }
  }

  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    if (chipDrawable != null) {
      chipDrawable.setShowMotionSpec(showMotionSpec);
    }
  }

  @Nullable
  public MotionSpec getHideMotionSpec() {
    return chipDrawable != null ? chipDrawable.getHideMotionSpec() : null;
  }

  public void setHideMotionSpecResource(@AnimatorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setHideMotionSpecResource(id);
    }
  }

  public void setHideMotionSpec(@Nullable MotionSpec hideMotionSpec) {
    if (chipDrawable != null) {
      chipDrawable.setHideMotionSpec(hideMotionSpec);
    }
  }

  public float getChipStartPadding() {
    return chipDrawable != null ? chipDrawable.getChipStartPadding() : 0;
  }

  public void setChipStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStartPaddingResource(id);
    }
  }

  public void setChipStartPadding(float chipStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setChipStartPadding(chipStartPadding);
    }
  }

  public float getIconStartPadding() {
    return chipDrawable != null ? chipDrawable.getIconStartPadding() : 0;
  }

  public void setIconStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setIconStartPaddingResource(id);
    }
  }

  public void setIconStartPadding(float iconStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setIconStartPadding(iconStartPadding);
    }
  }

  public float getIconEndPadding() {
    return chipDrawable != null ? chipDrawable.getIconEndPadding() : 0;
  }

  public void setIconEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setIconEndPaddingResource(id);
    }
  }

  public void setIconEndPadding(float iconEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setIconEndPadding(iconEndPadding);
    }
  }

  public float getTextStartPadding() {
    return chipDrawable != null ? chipDrawable.getTextStartPadding() : 0;
  }

  public void setTextStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextStartPaddingResource(id);
    }
  }

  public void setTextStartPadding(float textStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setTextStartPadding(textStartPadding);
    }
  }

  public float getTextEndPadding() {
    return chipDrawable != null ? chipDrawable.getTextEndPadding() : 0;
  }

  public void setTextEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextEndPaddingResource(id);
    }
  }

  public void setTextEndPadding(float textEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setTextEndPadding(textEndPadding);
    }
  }

  public float getCloseIconStartPadding() {
    return chipDrawable != null ? chipDrawable.getCloseIconStartPadding() : 0;
  }

  public void setCloseIconStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconStartPaddingResource(id);
    }
  }

  public void setCloseIconStartPadding(float closeIconStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconStartPadding(closeIconStartPadding);
    }
  }

  public float getCloseIconEndPadding() {
    return chipDrawable != null ? chipDrawable.getCloseIconEndPadding() : 0;
  }

  public void setCloseIconEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEndPaddingResource(id);
    }
  }

  public void setCloseIconEndPadding(float closeIconEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEndPadding(closeIconEndPadding);
    }
  }

  public float getChipEndPadding() {
    return chipDrawable != null ? chipDrawable.getChipEndPadding() : 0;
  }

  public void setChipEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipEndPaddingResource(id);
    }
  }

  public void setChipEndPadding(float chipEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setChipEndPadding(chipEndPadding);
    }
  }
}
