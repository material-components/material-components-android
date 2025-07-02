/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.google.android.material.overflow;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.TintTypedArray;
import androidx.appcompat.widget.TooltipCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonGroup.OverflowUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialAttributes;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides an implementation of an overflow linear layout.
 *
 * <p>The OverflowLinearLayout will automatically hide/show its children depending on the current
 * available screen space and/or the max size of its parent layout. If there is not enough space to
 * show all children, the ones that do not fit will be put in an overflow menu, and an overflow
 * button will be automatically added as the last child of the layout.
 *
 * <p>Note: if you'd like to hide/show children independently from this layout's decisions, you'll
 * need to add/remove the desired view(s), instead of changing their visibility, as the
 * OverflowLinearLayout will determine the final visibility value of its children.
 *
 * <p>The OverflowLinearLayout is commonly used with the {@link
 * com.google.android.material.floatingtoolbar.FloatingToolbarLayout} and the {@link
 * com.google.android.material.dockedtoolbar.DockedToolbarLayout}.
 */
public class OverflowLinearLayout extends LinearLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Material3_OverflowLinearLayout;

  @NonNull private final MaterialButton overflowButton;
  private boolean overflowButtonAdded = false;

  private final Set<View> overflowViews = new LinkedHashSet<>();

  public OverflowLinearLayout(@NonNull Context context) {
    this(context, null);
  }

  public OverflowLinearLayout(@NonNull Context context, @Nullable AttributeSet attributeSet) {
    this(context, attributeSet, R.attr.overflowLinearLayoutStyle);
  }

  public OverflowLinearLayout(
      @NonNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
    super(wrap(context, attributeSet, defStyleAttr, DEF_STYLE_RES), attributeSet, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attributeSet, R.styleable.OverflowLinearLayout, defStyleAttr, DEF_STYLE_RES);

    Drawable overflowButtonDrawable =
        attributes.getDrawable(R.styleable.OverflowLinearLayout_overflowButtonIcon);

    attributes.recycle();

    // Configurations of the overflow button.
    overflowButton =
        (MaterialButton)
            LayoutInflater.from(context)
                .inflate(R.layout.m3_overflow_linear_layout_overflow_button, this, false);
    TooltipCompat.setTooltipText(overflowButton, getResources().getString(R.string.m3_overflow_linear_layout_button_tooltip_text));
    setOverflowButtonIcon(overflowButtonDrawable);
    if (overflowButton.getContentDescription() == null) {
      overflowButton.setContentDescription(
          context.getString(R.string.m3_overflow_linear_layout_button_content_description));
    }
    int overflowMenuStyle =
        MaterialAttributes.resolveOrThrow(this, R.attr.overflowLinearLayoutPopupMenuStyle);
    PopupMenu popupMenu;
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      popupMenu = new PopupMenu(getContext(), overflowButton, Gravity.CENTER, 0, overflowMenuStyle);
    } else {
      popupMenu = new PopupMenu(getContext(), overflowButton, Gravity.CENTER);
    }
    int overflowItemIconPadding =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.m3_overflow_item_icon_horizontal_padding);
    overflowButton.setOnClickListener(
        v -> handleOverflowButtonClick(popupMenu, overflowItemIconPadding));
  }

  /** Whether the OverflowLinearLayout currently has items overflowed. */
  public boolean isOverflowed() {
    return !overflowViews.isEmpty();
  }

  /** Returns the current set of overflowed views. */
  @NonNull
  public Set<View> getOverflowedViews() {
    return overflowViews;
  }

  /**
   * Sets the icon to show for the overflow button.
   *
   * @param icon Drawable to use for the overflow button's icon.
   * @attr ref com.google.android.material.R.styleable#OverflowLinearLayout_overflowButtonIcon
   * @see #setOverflowButtonIconResource(int)
   * @see #getOverflowButtonIcon()
   */
  public void setOverflowButtonIcon(@Nullable Drawable icon) {
    overflowButton.setIcon(icon);
  }

  /**
   * Sets the icon to show for the overflow button.
   *
   * @param iconResourceId drawable resource ID to use for the overflow button's icon.
   * @attr ref com.google.android.material.R.styleable#OverflowLinearLayout_overflowButtonIcon
   * @see #setOverflowButtonIcon(Drawable)
   * @see #getOverflowButtonIcon()
   */
  public void setOverflowButtonIconResource(@DrawableRes int iconResourceId) {
    overflowButton.setIconResource(iconResourceId);
  }

  /**
   * Returns the icon shown for the overflow button, if present.
   *
   * @return the overflow button icon, if present.
   * @attr ref com.google.android.material.R.styleable#OverflowLinearLayout_overflowButtonIcon
   * @see #setOverflowButtonIcon(Drawable)
   * @see #setOverflowButtonIconResource(int)
   */
  @Nullable
  public Drawable getOverflowButtonIcon() {
    return overflowButton.getIcon();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    boolean isHorizontal = getOrientation() == HORIZONTAL;
    int childCountWithoutOverflowButton =
        overflowButtonAdded ? getChildCount() - 1 : getChildCount();
    int atMostSize =
        isHorizontal
            ? MeasureSpec.getSize(widthMeasureSpec)
            : MeasureSpec.getSize(heightMeasureSpec);
    int childrenSize = 0;
    int overflowButtonSize =
        getOverflowButtonSize(isHorizontal, overflowButton, widthMeasureSpec, heightMeasureSpec);
    overflowButton.setVisibility(GONE);
    overflowViews.clear();
    boolean shouldShowOverflow = false;

    for (int childIndex = 0; childIndex < childCountWithoutOverflowButton; childIndex++) {
      View child = getChildAt(childIndex);
      child.setVisibility(VISIBLE);
      int childSize = getChildSize(isHorizontal, child, widthMeasureSpec, heightMeasureSpec);

      if (childrenSize + childSize + overflowButtonSize > atMostSize) {
        // Add views to be overflowed here in case overflow happens so that we don't have to loop
        // over the children again. Here we're also accounting for the overflow button size, to make
        // sure it'll fit in the layout we might have to remove extra buttons.
        overflowViews.add(child);
      }
      // Overflow actually happens if adding this child makes it go beyond the atMostSize.
      if (childrenSize + childSize > atMostSize) {
        shouldShowOverflow = true;
        int removedIndex = childIndex + 1;
        // Finish looping through the children and adding remaining overflowed views.
        while (removedIndex < childCountWithoutOverflowButton) {
          overflowViews.add(getChildAt(removedIndex));
          removedIndex++;
        }
        break;
      } else {
        childrenSize += childSize;
      }
    }

    if (shouldShowOverflow) {
      for (View view : overflowViews) {
        view.setVisibility(GONE);
      }
      if (!overflowButtonAdded) {
        // Add overflow button here so it's the last button of the layout.
        addView(overflowButton);
        overflowButtonAdded = true;
      }
      overflowButton.setVisibility(VISIBLE);
    } else {
      overflowButton.setVisibility(GONE);
      // Make sure overflowViews is empty.
      overflowViews.clear();
    }

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  private int getChildSize(
      boolean isHorizontal, View child, int widthMeasureSpec, int heightMeasureSpec) {
    measureChild(child, widthMeasureSpec, heightMeasureSpec);
    LayoutParams lp = (LayoutParams) child.getLayoutParams();
    int childSize =
        isHorizontal
            ? (child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin)
            : (child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
    // Child measured size may be zero in some cases, like if its final size is being determined by
    // layout weight, so use minimum size instead for such cases.
    if (childSize == 0) {
      childSize =
          isHorizontal
              ? (child.getMinimumWidth() + lp.leftMargin + lp.rightMargin)
              : (child.getMinimumHeight() + lp.topMargin + lp.bottomMargin);
    }
    return childSize;
  }

  private int getOverflowButtonSize(
      boolean isHorizontal, View button, int widthMeasureSpec, int heightMeasureSpec) {
    measureChild(button, widthMeasureSpec, heightMeasureSpec);
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) button.getLayoutParams();
    return isHorizontal
        ? (button.getMeasuredWidth() + lp.leftMargin + lp.rightMargin)
        : (button.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
  }

  private void handleOverflowButtonClick(PopupMenu popupMenu, int overflowItemIconPadding) {
    popupMenu.getMenu().clear();
    popupMenu.setForceShowIcon(true);
    // Set up each item of the overflow menu.
    for (View view : overflowViews) {
      OverflowLinearLayout.LayoutParams lp =
          (OverflowLinearLayout.LayoutParams) view.getLayoutParams();

      CharSequence text = OverflowUtils.getMenuItemText(view, lp.overflowText);
      MenuItem item = popupMenu.getMenu().add(text);
      Drawable icon = lp.overflowIcon;
      if (icon != null) {
        item.setIcon(
            new InsetDrawable(icon, overflowItemIconPadding, 0, overflowItemIconPadding, 0));
      }
      if (view instanceof MaterialButton) {
        MaterialButton button = (MaterialButton) view;
        item.setCheckable(button.isCheckable());
        item.setChecked(button.isChecked());
      }
      item.setEnabled(view.isEnabled());
      item.setOnMenuItemClickListener(
          menuItem -> {
            view.performClick();
            if (item.isCheckable()) {
              item.setChecked(!item.isChecked());
            }
            return true;
          });
    }
    popupMenu.show();
  }

  @Override
  @NonNull
  protected OverflowLinearLayout.LayoutParams generateDefaultLayoutParams() {
    if (getOrientation() == HORIZONTAL) {
      return new OverflowLinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    } else {
      return new OverflowLinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
  }

  @Override
  @NonNull
  public LayoutParams generateLayoutParams(@Nullable AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  @NonNull
  protected OverflowLinearLayout.LayoutParams generateLayoutParams(
      @NonNull ViewGroup.LayoutParams p) {
    if (p instanceof LayoutParams) {
      return new OverflowLinearLayout.LayoutParams(p);
    } else if (p instanceof LinearLayout.LayoutParams) {
      return new OverflowLinearLayout.LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new OverflowLinearLayout.LayoutParams((MarginLayoutParams) p);
    } else {
      return new OverflowLinearLayout.LayoutParams(p);
    }
  }

  @Override
  protected boolean checkLayoutParams(@NonNull ViewGroup.LayoutParams p) {
    return p instanceof OverflowLinearLayout.LayoutParams;
  }

  /** A {@link LinearLayout.LayoutParams} implementation for {@link OverflowLinearLayout}. */
  public static class LayoutParams extends LinearLayout.LayoutParams {
    @Nullable public Drawable overflowIcon = null;
    @Nullable public CharSequence overflowText = null;

    /**
     * Creates a new set of layout parameters. The values are extracted from the supplied attributes
     * set and context.
     *
     * @param context the application environment
     * @param attrs the set of attributes from which to extract the layout parameters' values
     */
    public LayoutParams(@NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
      TypedArray attributes =
          context.obtainStyledAttributes(attrs, R.styleable.OverflowLinearLayout_Layout);

      overflowIcon =
          attributes.getDrawable(R.styleable.OverflowLinearLayout_Layout_layout_overflowIcon);
      overflowText =
          attributes.getText(R.styleable.OverflowLinearLayout_Layout_layout_overflowText);

      attributes.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    /**
     * Creates a new set of layout parameters with the specified width, height, weight, overflow
     * icon and overflow text.
     *
     * @param width the width, either {@link #MATCH_PARENT}, {@link #WRAP_CONTENT} or a fixed size
     *     in pixels
     * @param height the height, either {@link #MATCH_PARENT}, {@link #WRAP_CONTENT} or a fixed size
     *     in pixels
     * @param weight the weight
     * @param overflowIcon the overflow icon drawable
     * @param overflowText the overflow text char sequence
     */
    public LayoutParams(
        int width,
        int height,
        float weight,
        @Nullable Drawable overflowIcon,
        @Nullable CharSequence overflowText) {
      super(width, height, weight);
      this.overflowIcon = overflowIcon;
      this.overflowText = overflowText;
    }

    public LayoutParams(@NonNull ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(@NonNull MarginLayoutParams source) {
      super(source);
    }

    public LayoutParams(@NonNull LinearLayout.LayoutParams source) {
      super(source);
    }

    /**
     * Copy constructor. Clones the values of the source.
     *
     * @param source The layout params to copy from.
     */
    public LayoutParams(@NonNull LayoutParams source) {
      super(source);
      this.overflowText = source.overflowText;
      this.overflowIcon = source.overflowIcon;
    }
  }
}
