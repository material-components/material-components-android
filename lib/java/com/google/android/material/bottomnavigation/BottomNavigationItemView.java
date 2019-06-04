/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.bottomnavigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.TooltipCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationItemView extends FrameLayout implements MenuView.ItemView {
  public static final int INVALID_ITEM_POSITION = -1;

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

  private final int defaultMargin;
  private float shiftAmount;
  private float scaleUpFactor;
  private float scaleDownFactor;

  private int labelVisibilityMode;
  private boolean isShifting;

  private ImageView icon;
  private final TextView smallLabel;
  private final TextView largeLabel;
  private int itemPosition = INVALID_ITEM_POSITION;

  private MenuItemImpl itemData;

  private ColorStateList iconTint;
  private Drawable originalIconDrawable;
  private Drawable wrappedIconDrawable;

  @Nullable private BadgeDrawable badgeDrawable;

  public BottomNavigationItemView(@NonNull Context context) {
    this(context, null);
  }

  public BottomNavigationItemView(@NonNull Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomNavigationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    final Resources res = getResources();

    LayoutInflater.from(context).inflate(R.layout.design_bottom_navigation_item, this, true);
    setBackgroundResource(R.drawable.design_bottom_navigation_item_background);
    defaultMargin = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_margin);

    icon = findViewById(R.id.icon);
    smallLabel = findViewById(R.id.smallLabel);
    largeLabel = findViewById(R.id.largeLabel);
    // The labels used aren't always visible, so they are unreliable for accessibility. Instead,
    // the content description of the BottomNavigationItemView should be used for accessibility.
    ViewCompat.setImportantForAccessibility(smallLabel, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    ViewCompat.setImportantForAccessibility(largeLabel, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    setFocusable(true);
    calculateTextScaleFactors(smallLabel.getTextSize(), largeLabel.getTextSize());

    // TODO: Support displaying a badge on label-only bottom navigation views.
    if (icon != null) {
      icon.addOnLayoutChangeListener(
          new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                View v,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {
              if (icon.getVisibility() == VISIBLE) {
                tryUpdateBadgeBounds(icon);
              }
            }
          });
    }
    ViewCompat.setAccessibilityDelegate(this, null);
  }

  @Override
  public void initialize(MenuItemImpl itemData, int menuType) {
    this.itemData = itemData;
    setCheckable(itemData.isCheckable());
    setChecked(itemData.isChecked());
    setEnabled(itemData.isEnabled());
    setIcon(itemData.getIcon());
    setTitle(itemData.getTitle());
    setId(itemData.getItemId());
    if (!TextUtils.isEmpty(itemData.getContentDescription())) {
      setContentDescription(itemData.getContentDescription());
    }
    TooltipCompat.setTooltipText(this, itemData.getTooltipText());
    setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
  }

  public void setItemPosition(int position) {
    itemPosition = position;
  }

  public int getItemPosition() {
    return itemPosition;
  }

  public void setShifting(boolean shifting) {
    if (isShifting != shifting) {
      isShifting = shifting;

      boolean initialized = itemData != null;
      if (initialized) {
        setChecked(itemData.isChecked());
      }
    }
  }

  public void setLabelVisibilityMode(@LabelVisibilityMode int mode) {
    if (labelVisibilityMode != mode) {
      labelVisibilityMode = mode;

      boolean initialized = itemData != null;
      if (initialized) {
        setChecked(itemData.isChecked());
      }
    }
  }

  @Override
  public MenuItemImpl getItemData() {
    return itemData;
  }

  @Override
  public void setTitle(CharSequence title) {
    smallLabel.setText(title);
    largeLabel.setText(title);
    if (itemData == null || TextUtils.isEmpty(itemData.getContentDescription())) {
      setContentDescription(title);
    }
  }

  @Override
  public void setCheckable(boolean checkable) {
    refreshDrawableState();
  }

  @Override
  public void setChecked(boolean checked) {
    largeLabel.setPivotX(largeLabel.getWidth() / 2);
    largeLabel.setPivotY(largeLabel.getBaseline());
    smallLabel.setPivotX(smallLabel.getWidth() / 2);
    smallLabel.setPivotY(smallLabel.getBaseline());

    switch (labelVisibilityMode) {
      case LabelVisibilityMode.LABEL_VISIBILITY_AUTO:
        if (isShifting) {
          if (checked) {
            setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewValues(largeLabel, 1f, 1f, VISIBLE);
          } else {
            setViewLayoutParams(icon, defaultMargin, Gravity.CENTER);
            setViewValues(largeLabel, 0.5f, 0.5f, INVISIBLE);
          }
          smallLabel.setVisibility(INVISIBLE);
        } else {
          if (checked) {
            setViewLayoutParams(
                icon, (int) (defaultMargin + shiftAmount), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewValues(largeLabel, 1f, 1f, VISIBLE);
            setViewValues(smallLabel, scaleUpFactor, scaleUpFactor, INVISIBLE);
          } else {
            setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewValues(largeLabel, scaleDownFactor, scaleDownFactor, INVISIBLE);
            setViewValues(smallLabel, 1f, 1f, VISIBLE);
          }
        }
        break;

      case LabelVisibilityMode.LABEL_VISIBILITY_SELECTED:
        if (checked) {
          setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewValues(largeLabel, 1f, 1f, VISIBLE);
        } else {
          setViewLayoutParams(icon, defaultMargin, Gravity.CENTER);
          setViewValues(largeLabel, 0.5f, 0.5f, INVISIBLE);
        }
        smallLabel.setVisibility(INVISIBLE);
        break;

      case LabelVisibilityMode.LABEL_VISIBILITY_LABELED:
        if (checked) {
          setViewLayoutParams(
              icon, (int) (defaultMargin + shiftAmount), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewValues(largeLabel, 1f, 1f, VISIBLE);
          setViewValues(smallLabel, scaleUpFactor, scaleUpFactor, INVISIBLE);
        } else {
          setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewValues(largeLabel, scaleDownFactor, scaleDownFactor, INVISIBLE);
          setViewValues(smallLabel, 1f, 1f, VISIBLE);
        }
        break;

      case LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED:
        setViewLayoutParams(icon, defaultMargin, Gravity.CENTER);
        largeLabel.setVisibility(GONE);
        smallLabel.setVisibility(GONE);
        break;

      default:
        break;
    }

    refreshDrawableState();

    // Set the item as selected to send an AccessibilityEvent.TYPE_VIEW_SELECTED from View, so that
    // the item is read out as selected.
    setSelected(checked);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    if (badgeDrawable != null && badgeDrawable.isVisible()) {
      CharSequence customContentDescription = itemData.getTitle();
      if (!TextUtils.isEmpty(itemData.getContentDescription())) {
        customContentDescription = itemData.getContentDescription();
      }
      info.setContentDescription(
          customContentDescription + ", " + badgeDrawable.getContentDescription());
    }
  }

  private void setViewLayoutParams(@NonNull View view, int topMargin, int gravity) {
    LayoutParams viewParams = (LayoutParams) view.getLayoutParams();
    viewParams.topMargin = topMargin;
    viewParams.gravity = gravity;
    view.setLayoutParams(viewParams);
  }

  private void setViewValues(@NonNull View view, float scaleX, float scaleY, int visibility) {
    view.setScaleX(scaleX);
    view.setScaleY(scaleY);
    view.setVisibility(visibility);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    smallLabel.setEnabled(enabled);
    largeLabel.setEnabled(enabled);
    icon.setEnabled(enabled);

    if (enabled) {
      ViewCompat.setPointerIcon(
          this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
    } else {
      ViewCompat.setPointerIcon(this, null);
    }
  }

  @Override
  public int[] onCreateDrawableState(final int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (itemData != null && itemData.isCheckable() && itemData.isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }
    return drawableState;
  }

  @Override
  public void setShortcut(boolean showShortcut, char shortcutKey) {}

  @Override
  public void setIcon(Drawable iconDrawable) {
    if (iconDrawable == originalIconDrawable) {
      return;
    }

    // Save the original icon to check if it has changed in future calls of this method.
    originalIconDrawable = iconDrawable;
    if (iconDrawable != null) {
      Drawable.ConstantState state = iconDrawable.getConstantState();
      iconDrawable =
          DrawableCompat.wrap(state == null ? iconDrawable : state.newDrawable()).mutate();
      wrappedIconDrawable = iconDrawable;
      DrawableCompat.setTintList(wrappedIconDrawable, iconTint);
    }
    this.icon.setImageDrawable(iconDrawable);
  }

  @Override
  public boolean prefersCondensedTitle() {
    return false;
  }

  @Override
  public boolean showsIcon() {
    return true;
  }

  public void setIconTintList(ColorStateList tint) {
    iconTint = tint;
    if (itemData != null && wrappedIconDrawable != null) {
      DrawableCompat.setTintList(wrappedIconDrawable, iconTint);
      wrappedIconDrawable.invalidateSelf();
    }
  }

  public void setIconSize(int iconSize) {
    LayoutParams iconParams = (LayoutParams) icon.getLayoutParams();
    iconParams.width = iconSize;
    iconParams.height = iconSize;
    icon.setLayoutParams(iconParams);
  }

  public void setTextAppearanceInactive(@StyleRes int inactiveTextAppearance) {
    TextViewCompat.setTextAppearance(smallLabel, inactiveTextAppearance);
    calculateTextScaleFactors(smallLabel.getTextSize(), largeLabel.getTextSize());
  }

  public void setTextAppearanceActive(@StyleRes int activeTextAppearance) {
    TextViewCompat.setTextAppearance(largeLabel, activeTextAppearance);
    calculateTextScaleFactors(smallLabel.getTextSize(), largeLabel.getTextSize());
  }

  public void setTextColor(@Nullable ColorStateList color) {
    if (color != null) {
      smallLabel.setTextColor(color);
      largeLabel.setTextColor(color);
    }
  }

  private void calculateTextScaleFactors(float smallLabelSize, float largeLabelSize) {
    shiftAmount = smallLabelSize - largeLabelSize;
    scaleUpFactor = 1f * largeLabelSize / smallLabelSize;
    scaleDownFactor = 1f * smallLabelSize / largeLabelSize;
  }

  public void setItemBackground(int background) {
    Drawable backgroundDrawable =
        background == 0 ? null : ContextCompat.getDrawable(getContext(), background);
    setItemBackground(backgroundDrawable);
  }

  public void setItemBackground(@Nullable Drawable background) {
    if (background != null && background.getConstantState() != null) {
      background = background.getConstantState().newDrawable().mutate();
    }
    ViewCompat.setBackground(this, background);
  }

  void setBadge(@NonNull BadgeDrawable badgeDrawable) {
    this.badgeDrawable = badgeDrawable;
    if (icon != null) {
      tryAttachBadgeToAnchor(icon);
    }
  }

  @Nullable
  BadgeDrawable getBadge() {
    return this.badgeDrawable;
  }

  void removeBadge() {
    tryRemoveBadgeFromAnchor(icon);
  }

  private boolean hasBadge() {
    return badgeDrawable != null;
  }

  private void tryUpdateBadgeBounds(View anchorView) {
    if (!hasBadge()) {
      return;
    }
    BadgeUtils.setBadgeDrawableBounds(
        badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
  }

  private void tryAttachBadgeToAnchor(View anchorView) {
    if (!hasBadge()) {
      return;
    }
    if (anchorView != null) {
      // Avoid clipping a badge if it's displayed.
      setClipChildren(false);
      setClipToPadding(false);

      BadgeUtils.attachBadgeDrawable(
          badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
    }
  }

  private void tryRemoveBadgeFromAnchor(View anchorView) {
    if (!hasBadge()) {
      return;
    }
    if (anchorView != null) {
      // Clip children / view to padding when no badge is displayed.
      setClipChildren(true);
      setClipToPadding(true);

      BadgeUtils.detachBadgeDrawable(
          badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
    }
    badgeDrawable = null;
  }

  @Nullable
  private FrameLayout getCustomParentForBadge(View anchorView) {
    if (anchorView == icon) {
      return BadgeUtils.USE_COMPAT_PARENT ? ((FrameLayout) icon.getParent()) : null;
    }
    // TODO: Support displaying a badge on label-only bottom navigation views.
    return null;
  }
}
