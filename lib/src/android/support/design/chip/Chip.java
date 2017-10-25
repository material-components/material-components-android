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
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.theme.ThemeUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;
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
 * <p>You can register a listener on the main chip with {@link #setOnClickListener(OnClickListener)}
 * or {@link #setOnCheckedChangeListener(OnCheckedChangeListener)}. You can register a listener on
 * the close icon with {@link #setOnCloseIconClickListener(OnClickListener)}.
 *
 * <p>Do not use the {@code android:button} attribute. It will be ignored because Chip manages its
 * own button Drawable.
 *
 * @see ChipDrawable
 */
public class Chip extends AppCompatCheckBox {

  private static final int CLOSE_ICON_VIRTUAL_ID = 0;

  @Nullable private ChipDrawable chipDrawable;

  @Nullable private OnClickListener onCloseIconClickListener;
  private boolean deferredCheckedValue;

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

    ThemeUtils.checkAppCompatTheme(context);

    ChipDrawable drawable =
        ChipDrawable.createFromAttributes(context, attrs, defStyleAttr, R.style.Widget_Design_Chip);
    setButtonDrawable(drawable);

    touchHelper = new ChipTouchHelper(this);
    ViewCompat.setAccessibilityDelegate(this, touchHelper);
    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

    initOutlineProvider();
    setChecked(deferredCheckedValue);
  }

  private void initOutlineProvider() {
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              Drawable button = CompoundButtonCompat.getButtonDrawable((CompoundButton) view);
              if (button != null) {
                button.getOutline(outline);
              } else {
                outline.setRect(0, 0, view.getWidth(), view.getHeight());
                outline.setAlpha(0.0f);
              }
            }
          });
    }
  }

  @Override
  public void setButtonDrawable(Drawable buttonDrawable) {
    super.setButtonDrawable(buttonDrawable);

    if ((buttonDrawable instanceof ChipDrawable)) {
      chipDrawable = (ChipDrawable) buttonDrawable;
    } else {
      throw new IllegalArgumentException("Button drawable must be an instance of ChipDrawable.");
    }
  }

  @Override
  public void setChecked(boolean checked) {
    if (chipDrawable == null) {
      // Defer the setChecked() call until after initialization.
      deferredCheckedValue = checked;
    } else if (chipDrawable.isCheckable()) {
      super.setChecked(checked);
    }
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
  protected boolean dispatchHoverEvent(MotionEvent event) {
    return touchHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
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
      virtualViewIds.add(HOST_ID);
      if (hasCloseIcon()) {
        virtualViewIds.add(CLOSE_ICON_VIRTUAL_ID);
      }
    }

    @Override
    protected void onPopulateNodeForVirtualView(
        int virtualViewId, AccessibilityNodeInfoCompat node) {
      if (hasCloseIcon()) {
        node.setContentDescription(
            getContext().getString(R.string.mtrl_chip_close_icon_content_description));
        node.setBoundsInParent(getCloseIconTouchBoundsInt());
        node.addAction(AccessibilityActionCompat.ACTION_CLICK);
        node.setEnabled(isEnabled());
      } else {
        node.setContentDescription("");
      }
    }

    @Override
    protected void onPopulateNodeForHost(AccessibilityNodeInfoCompat node) {
      node.setCheckable(chipDrawable != null && chipDrawable.isCheckable());
      node.setClassName(Chip.class.getName());
      node.setContentDescription(
          Chip.class.getSimpleName()
              + ". "
              + (chipDrawable != null ? chipDrawable.getChipText() : ""));
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
}
