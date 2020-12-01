/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.navigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.TooltipCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

/**
 * Provides a view that will be used to render destination items inside a {@link
 * NavigationBarMenuView}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class NavigationBarItemView extends FrameLayout implements MenuView.ItemView {
  private static final int INVALID_ITEM_POSITION = -1;
  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

  private final int defaultMargin;
  private float shiftAmount;
  private float scaleUpFactor;
  private float scaleDownFactor;

  private int labelVisibilityMode;
  private boolean isShifting;

  private ImageView icon;
  private final ViewGroup labelGroup;
  private final TextView smallLabel;
  private final TextView largeLabel;
  private int itemPosition = INVALID_ITEM_POSITION;

  @Nullable private MenuItemImpl itemData;

  @Nullable private ColorStateList iconTint;
  @Nullable private Drawable originalIconDrawable;
  @Nullable private Drawable wrappedIconDrawable;

  @Nullable private BadgeDrawable badgeDrawable;

  public NavigationBarItemView(@NonNull Context context) {
    super(context);

    LayoutInflater.from(context).inflate(getItemLayoutResId(), this, true);
    icon = findViewById(R.id.navigation_bar_item_icon_view);
    labelGroup = findViewById(R.id.navigation_bar_item_labels_group);
    smallLabel = findViewById(R.id.navigation_bar_item_small_label_view);
    largeLabel = findViewById(R.id.navigation_bar_item_large_label_view);

    setBackgroundResource(getItemBackgroundResId());

    defaultMargin = getResources().getDimensionPixelSize(getItemDefaultMarginResId());

    // Save the original bottom padding from the label group so it can be animated to and from
    // during label visibility changes.
    labelGroup.setTag(R.id.mtrl_view_tag_bottom_padding, labelGroup.getPaddingBottom());

    // The labels used aren't always visible, so they are unreliable for accessibility. Instead,
    // the content description of the NavigationBarItemView should be used for accessibility.
    ViewCompat.setImportantForAccessibility(smallLabel, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    ViewCompat.setImportantForAccessibility(largeLabel, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    setFocusable(true);
    calculateTextScaleFactors(smallLabel.getTextSize(), largeLabel.getTextSize());

    // TODO(b/138148581): Support displaying a badge on label-only bottom navigation views.
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
  }

  @Override
  public void initialize(@NonNull MenuItemImpl itemData, int menuType) {
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

    CharSequence tooltipText =
        !TextUtils.isEmpty(itemData.getTooltipText())
            ? itemData.getTooltipText()
            : itemData.getTitle();
    TooltipCompat.setTooltipText(this, tooltipText);
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

  public void setLabelVisibilityMode(@NavigationBarView.LabelVisibility int mode) {
    if (labelVisibilityMode != mode) {
      labelVisibilityMode = mode;

      boolean initialized = itemData != null;
      if (initialized) {
        setChecked(itemData.isChecked());
      }
    }
  }

  @Override
  @Nullable
  public MenuItemImpl getItemData() {
    return itemData;
  }

  @Override
  public void setTitle(@Nullable CharSequence title) {
    smallLabel.setText(title);
    largeLabel.setText(title);
    if (itemData == null || TextUtils.isEmpty(itemData.getContentDescription())) {
      setContentDescription(title);
    }

    CharSequence tooltipText =
        itemData == null || TextUtils.isEmpty(itemData.getTooltipText())
            ? title
            : itemData.getTooltipText();
    TooltipCompat.setTooltipText(this, tooltipText);
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
      case NavigationBarView.LABEL_VISIBILITY_AUTO:
        if (isShifting) {
          if (checked) {
            setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            updateViewPaddingBottom(
                labelGroup, (int) labelGroup.getTag(R.id.mtrl_view_tag_bottom_padding));
            largeLabel.setVisibility(VISIBLE);
          } else {
            setViewLayoutParams(icon, defaultMargin, Gravity.CENTER);
            updateViewPaddingBottom(labelGroup, 0);
            largeLabel.setVisibility(INVISIBLE);
          }
          smallLabel.setVisibility(INVISIBLE);
        } else {
          updateViewPaddingBottom(
              labelGroup, (int) labelGroup.getTag(R.id.mtrl_view_tag_bottom_padding));
          if (checked) {
            setViewLayoutParams(
                icon, (int) (defaultMargin + shiftAmount), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewScaleValues(largeLabel, 1f, 1f, VISIBLE);
            setViewScaleValues(smallLabel, scaleUpFactor, scaleUpFactor, INVISIBLE);
          } else {
            setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewScaleValues(largeLabel, scaleDownFactor, scaleDownFactor, INVISIBLE);
            setViewScaleValues(smallLabel, 1f, 1f, VISIBLE);
          }
        }
        break;

      case NavigationBarView.LABEL_VISIBILITY_SELECTED:
        if (checked) {
          setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          updateViewPaddingBottom(
              labelGroup, (int) labelGroup.getTag(R.id.mtrl_view_tag_bottom_padding));
          largeLabel.setVisibility(VISIBLE);
        } else {
          setViewLayoutParams(icon, defaultMargin, Gravity.CENTER);
          updateViewPaddingBottom(labelGroup, 0);
          largeLabel.setVisibility(INVISIBLE);
        }
        smallLabel.setVisibility(INVISIBLE);
        break;

      case NavigationBarView.LABEL_VISIBILITY_LABELED:
        updateViewPaddingBottom(
            labelGroup, (int) labelGroup.getTag(R.id.mtrl_view_tag_bottom_padding));
        if (checked) {
          setViewLayoutParams(
              icon, (int) (defaultMargin + shiftAmount), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewScaleValues(largeLabel, 1f, 1f, VISIBLE);
          setViewScaleValues(smallLabel, scaleUpFactor, scaleUpFactor, INVISIBLE);
        } else {
          setViewLayoutParams(icon, defaultMargin, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewScaleValues(largeLabel, scaleDownFactor, scaleDownFactor, INVISIBLE);
          setViewScaleValues(smallLabel, 1f, 1f, VISIBLE);
        }
        break;

      case NavigationBarView.LABEL_VISIBILITY_UNLABELED:
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
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    if (badgeDrawable != null && badgeDrawable.isVisible()) {
      CharSequence customContentDescription = itemData.getTitle();
      if (!TextUtils.isEmpty(itemData.getContentDescription())) {
        customContentDescription = itemData.getContentDescription();
      }
      info.setContentDescription(
          customContentDescription + ", " + badgeDrawable.getContentDescription());
    }
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionItemInfo(
        CollectionItemInfoCompat.obtain(
            /* rowIndex= */ 0,
            /* rowSpan= */ 1,
            /* columnIndex= */ getItemVisiblePosition(),
            /* columnSpan= */ 1,
            /* heading= */ false,
            /* selected= */ isSelected()));
    if (isSelected()) {
      infoCompat.setClickable(false);
      infoCompat.removeAction(AccessibilityActionCompat.ACTION_CLICK);
    }
    infoCompat.setRoleDescription(getResources().getString(R.string.item_view_role_description));
  }

  /**
   * Iterate through all the preceding bottom navigating items to determine this item's visible
   * position.
   *
   * @return This item's visible position in a bottom navigation.
   */
  private int getItemVisiblePosition() {
    ViewGroup parent = (ViewGroup) getParent();
    int index = parent.indexOfChild(this);
    int visiblePosition = 0;
    for (int i = 0; i < index; i++) {
      View child = parent.getChildAt(i);
      if (child instanceof NavigationBarItemView && child.getVisibility() == View.VISIBLE) {
        visiblePosition++;
      }
    }
    return visiblePosition;
  }

  private static void setViewLayoutParams(@NonNull View view, int topMargin, int gravity) {
    LayoutParams viewParams = (LayoutParams) view.getLayoutParams();
    viewParams.topMargin = topMargin;
    viewParams.gravity = gravity;
    view.setLayoutParams(viewParams);
  }

  private static void setViewScaleValues(
      @NonNull View view, float scaleX, float scaleY, int visibility) {
    view.setScaleX(scaleX);
    view.setScaleY(scaleY);
    view.setVisibility(visibility);
  }

  private static void updateViewPaddingBottom(@NonNull View view, int paddingBottom) {
    view.setPadding(
        view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), paddingBottom);
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
  @NonNull
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
  public void setIcon(@Nullable Drawable iconDrawable) {
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
      if (iconTint != null) {
        DrawableCompat.setTintList(wrappedIconDrawable, iconTint);
      }
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

  public void setIconTintList(@Nullable ColorStateList tint) {
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
  public BadgeDrawable getBadge() {
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

  private void tryAttachBadgeToAnchor(@Nullable View anchorView) {
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

  private void tryRemoveBadgeFromAnchor(@Nullable View anchorView) {
    if (!hasBadge()) {
      return;
    }
    if (anchorView != null) {
      // Clip children / view to padding when no badge is displayed.
      setClipChildren(true);
      setClipToPadding(true);

      BadgeUtils.detachBadgeDrawable(badgeDrawable, anchorView);
    }
    badgeDrawable = null;
  }

  @Nullable
  private FrameLayout getCustomParentForBadge(View anchorView) {
    if (anchorView == icon) {
      return BadgeUtils.USE_COMPAT_PARENT ? ((FrameLayout) icon.getParent()) : null;
    }
    // TODO(b/138148581): Support displaying a badge on label-only bottom navigation views.
    return null;
  }

  /**
   * Returns the unique identifier to the drawable resource that must be used to render background
   * of the menu item view. Override this if the subclassed menu item requires a different
   * background resource to be set.
   */
  @DrawableRes
  protected int getItemBackgroundResId() {
    return R.drawable.mtrl_navigation_bar_item_background;
  }

  /**
   * Returns the unique identifier to the dimension resource that will specify the default margin
   * this menu item view. Override this if the subclassed menu item requires a different default
   * margin value.
   */
  @DimenRes
  protected int getItemDefaultMarginResId() {
    return R.dimen.mtrl_navigation_bar_item_default_margin;
  }

  /**
   * Returns the unique identifier to the layout resource that must be used to render the items in
   * this menu item view.
   */
  @LayoutRes
  protected abstract int getItemLayoutResId();
}
