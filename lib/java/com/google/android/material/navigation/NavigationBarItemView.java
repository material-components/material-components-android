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
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import androidx.annotation.FloatRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import com.google.android.material.animation.AnimationUtils;
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

  private int itemPaddingTop;
  private int itemPaddingBottom;
  private float shiftAmount;
  private float scaleUpFactor;
  private float scaleDownFactor;

  private int labelVisibilityMode;
  private boolean isShifting;

  @Nullable private final FrameLayout iconContainer;
  @Nullable private final View activeIndicatorView;
  private final ImageView icon;
  private final ViewGroup labelGroup;
  private final TextView smallLabel;
  private final TextView largeLabel;
  private int itemPosition = INVALID_ITEM_POSITION;

  @Nullable private MenuItemImpl itemData;

  @Nullable private ColorStateList iconTint;
  @Nullable private Drawable originalIconDrawable;
  @Nullable private Drawable wrappedIconDrawable;

  private ValueAnimator activeIndicatorAnimator;
  private float activeIndicatorProgress = 0F;
  private boolean activeIndicatorEnabled = false;
  // The desired width of the indicator. This is not necessarily the actual size of the rendered
  // indicator depending on whether the width of this view is wide enough to accommodate the full
  // desired width.
  private int activeIndicatorDesiredWidth = 0;
  // The margin from the start and end of this view which the active indicator should respect. If
  // the indicator width is greater than the total width minus the horizontal margins, the active
  // indicator will assume the max width of the view's total width minus horizontal margins.
  private int activeIndicatorMarginHorizontal = 0;

  @Nullable private BadgeDrawable badgeDrawable;

  public NavigationBarItemView(@NonNull Context context) {
    super(context);

    LayoutInflater.from(context).inflate(getItemLayoutResId(), this, true);
    iconContainer = findViewById(R.id.navigation_bar_item_icon_container);
    activeIndicatorView = findViewById(R.id.navigation_bar_item_active_indicator_view);
    icon = findViewById(R.id.navigation_bar_item_icon_view);
    labelGroup = findViewById(R.id.navigation_bar_item_labels_group);
    smallLabel = findViewById(R.id.navigation_bar_item_small_label_view);
    largeLabel = findViewById(R.id.navigation_bar_item_large_label_view);

    setBackgroundResource(getItemBackgroundResId());

    itemPaddingTop = getResources().getDimensionPixelSize(getItemDefaultMarginResId());
    itemPaddingBottom = labelGroup.getPaddingBottom();

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
  protected int getSuggestedMinimumWidth() {
    LayoutParams labelGroupParams = (LayoutParams) labelGroup.getLayoutParams();
    int labelWidth =
        labelGroupParams.leftMargin + labelGroup.getMeasuredWidth() + labelGroupParams.rightMargin;

    return max(getSuggestedIconWidth(), labelWidth);
  }

  @Override
  protected int getSuggestedMinimumHeight() {
    LayoutParams labelGroupParams = (LayoutParams) labelGroup.getLayoutParams();
    return getSuggestedIconHeight()
        + labelGroupParams.topMargin
        + labelGroup.getMeasuredHeight()
        + labelGroupParams.bottomMargin;
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

    // Avoid calling tooltip for L and M devices because long pressing twuice may freeze devices.
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || VERSION.SDK_INT > VERSION_CODES.M) {
      TooltipCompat.setTooltipText(this, tooltipText);
    }
    setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
  }

  /**
   * If this item's layout contains a container which holds the icon and active indicator, return
   * the container. Otherwise, return the icon image view.
   *
   * This is needed for clients who subclass this view and set their own item layout resource which
   * might not container an icon container or active indicator view.
   */
  private View getIconOrContainer() {
    return iconContainer != null ? iconContainer : icon;
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
      refreshChecked();
    }
  }

  public void setLabelVisibilityMode(@NavigationBarView.LabelVisibility int mode) {
    if (labelVisibilityMode != mode) {
      labelVisibilityMode = mode;
      refreshChecked();
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
    // Avoid calling tooltip for L and M devices because long pressing twuice may freeze devices.
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || VERSION.SDK_INT > VERSION_CODES.M) {
      TooltipCompat.setTooltipText(this, tooltipText);
    }
  }

  @Override
  public void setCheckable(boolean checkable) {
    refreshDrawableState();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    // Update the width of the active indicator to fit within the bounds of its parent. This is
    // needed when there is not enough width to accommodate the desired width of the indicator. Post
    // this update in order to wait for parent layout changes to actually take effect before
    // setting the new width.
    final int width = w;
    post(
        new Runnable() {
          @Override
          public void run() {
            updateActiveIndicatorAvailableWidth(width);
          }
        });
  }

  /**
   * Update the active indicator for a given 0-1 value.
   *
   * @param progress 0 when the indicator should communicate an unselected state (typically gone), 1
   *     when the indicator should communicate a selected state (typically showing at its full width
   *     and height).
   * @param target The final value towards which progress is animating. This can be used to
   *     determine if the indicator is being unselected or selected.
   */
  private void setActiveIndicatorProgress(
      @FloatRange(from = 0F, to = 1F) float progress, float target) {
    // TODO: Consider refactoring into a transform class.
    if (activeIndicatorView != null) {
      activeIndicatorView.setScaleX(AnimationUtils.lerp(.4f, 1f, progress));
      // Animate the alpha of the indicator over the first 1/5th of the animation
      float startAlphaFraction = target == 0F ? .8F : 0F;
      float endAlphaFraction = target == 0F ? 1F : .2F;
      activeIndicatorView.setAlpha(
          AnimationUtils.lerp(0f, 1f, startAlphaFraction, endAlphaFraction, progress));
    }
    activeIndicatorProgress = progress;
  }

  /** If the active indicator is enabled, animate from it's current state to it's new state. */
  private void maybeAnimateActiveIndicatorToProgress(
      @FloatRange(from = 0F, to = 1F) final float newProgress) {
    if (!activeIndicatorEnabled) {
      setActiveIndicatorProgress(newProgress, newProgress);
      return;
    }

    if (activeIndicatorAnimator != null) {
      activeIndicatorAnimator.cancel();
      activeIndicatorAnimator = null;
    }
    activeIndicatorAnimator = ValueAnimator.ofFloat(activeIndicatorProgress, newProgress);
    activeIndicatorAnimator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            setActiveIndicatorProgress(progress, newProgress);
          }
        });
    // TODO: Use motion theming values for duration and interpolation
    // Currently, these values match the values used by the label animation when showing/hiding
    // label visibility.
    activeIndicatorAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    activeIndicatorAnimator.setDuration(115L);
    activeIndicatorAnimator.start();
  }

  /**
   * Refresh the state of this item if it has been initialized.
   *
   * This is useful if parameters calculated based on this item's checked state (label visibility,
   * indicator state, iconContainer position) have changed and should be recalculated.
   */
  private void refreshChecked() {
    if (itemData != null) {
      setChecked(itemData.isChecked());
    }
  }

  @Override
  public void setChecked(boolean checked) {
    largeLabel.setPivotX(largeLabel.getWidth() / 2);
    largeLabel.setPivotY(largeLabel.getBaseline());
    smallLabel.setPivotX(smallLabel.getWidth() / 2);
    smallLabel.setPivotY(smallLabel.getBaseline());

    float newIndicatorProgress = checked ? 1F : 0F;
    maybeAnimateActiveIndicatorToProgress(newIndicatorProgress);

    switch (labelVisibilityMode) {
      case NavigationBarView.LABEL_VISIBILITY_AUTO:
        if (isShifting) {
          if (checked) {
            // Show icon and large label
            setViewTopMarginAndGravity(
                getIconOrContainer(), itemPaddingTop, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            updateViewPaddingBottom(labelGroup, itemPaddingBottom);
            largeLabel.setVisibility(VISIBLE);
          } else {
            // Show icon
            setViewTopMarginAndGravity(getIconOrContainer(), itemPaddingTop, Gravity.CENTER);
            updateViewPaddingBottom(labelGroup, 0);
            largeLabel.setVisibility(INVISIBLE);
          }
          smallLabel.setVisibility(INVISIBLE);
        } else {
          updateViewPaddingBottom(labelGroup, itemPaddingBottom);
          if (checked) {
            // Show icon and large label
            setViewTopMarginAndGravity(
                getIconOrContainer(),
                (int) (itemPaddingTop + shiftAmount),
                Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewScaleValues(largeLabel, 1f, 1f, VISIBLE);
            setViewScaleValues(smallLabel, scaleUpFactor, scaleUpFactor, INVISIBLE);
          } else {
            // Show icon and small label
            setViewTopMarginAndGravity(
                getIconOrContainer(), itemPaddingTop, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            setViewScaleValues(largeLabel, scaleDownFactor, scaleDownFactor, INVISIBLE);
            setViewScaleValues(smallLabel, 1f, 1f, VISIBLE);
          }
        }
        break;

      case NavigationBarView.LABEL_VISIBILITY_SELECTED:
        if (checked) {
          // Show icon and large label
          setViewTopMarginAndGravity(
              getIconOrContainer(), itemPaddingTop, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          updateViewPaddingBottom(labelGroup, itemPaddingBottom);
          largeLabel.setVisibility(VISIBLE);
        } else {
          // Show icon only
          setViewTopMarginAndGravity(getIconOrContainer(), itemPaddingTop, Gravity.CENTER);
          updateViewPaddingBottom(labelGroup, 0);
          largeLabel.setVisibility(INVISIBLE);
        }
        smallLabel.setVisibility(INVISIBLE);
        break;

      case NavigationBarView.LABEL_VISIBILITY_LABELED:
        updateViewPaddingBottom(labelGroup, itemPaddingBottom);
        if (checked) {
          // Show icon and large label
          setViewTopMarginAndGravity(
              getIconOrContainer(),
              (int) (itemPaddingTop + shiftAmount),
              Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewScaleValues(largeLabel, 1f, 1f, VISIBLE);
          setViewScaleValues(smallLabel, scaleUpFactor, scaleUpFactor, INVISIBLE);
        } else {
          // Show icon and small label
          setViewTopMarginAndGravity(
              getIconOrContainer(), itemPaddingTop, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
          setViewScaleValues(largeLabel, scaleDownFactor, scaleDownFactor, INVISIBLE);
          setViewScaleValues(smallLabel, 1f, 1f, VISIBLE);
        }
        break;

      case NavigationBarView.LABEL_VISIBILITY_UNLABELED:
        // Show icon only
        setViewTopMarginAndGravity(getIconOrContainer(), itemPaddingTop, Gravity.CENTER);
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

  private static void setViewTopMarginAndGravity(@NonNull View view, int topMargin, int gravity) {
    LayoutParams viewParams = (LayoutParams) view.getLayoutParams();
    viewParams.topMargin = topMargin;
    // Set the bottom margin to be equal to the top margin so this view can be centered in it's
    // parent if gravity is set to CENTER.
    viewParams.bottomMargin = topMargin;
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

  /**
   * Set the padding applied to the icon/active indicator container from the top of the item view.
   */
  public void setItemPaddingTop(int paddingTop) {
    this.itemPaddingTop = paddingTop;
    refreshChecked();
  }

  /** Set the padding applied to the labels from the bottom of the item view. */
  public void setItemPaddingBottom(int paddingBottom) {
    this.itemPaddingBottom = paddingBottom;
    refreshChecked();
  }

  /** Set whether or not this item should show an active indicator when checked. */
  public void setActiveIndicatorEnabled(boolean enabled) {
    this.activeIndicatorEnabled = enabled;
    if (activeIndicatorView != null) {
      activeIndicatorView.setVisibility(enabled ? View.VISIBLE : View.GONE);
      requestLayout();
    }
  }

  /**
   * Set the desired width of the active indicator.
   *
   * <p>If the item view is not wide enough to accommodate the given {@code width} plus any
   * horizontal margin, the width will be set to the width of the item view minus any horizontal
   * margin.
   *
   * @param width The desired width of the active indicator.
   */
  public void setActiveIndicatorWidth(int width) {
    this.activeIndicatorDesiredWidth = width;
    updateActiveIndicatorAvailableWidth(getWidth());
  }

  /**
   * Update the active indicator to always be within the width of this item layout.
   *
   * @param availableWidth The total width of this item layout.
   */
  private void updateActiveIndicatorAvailableWidth(int availableWidth) {
    // Set width to the min of either the desired indicator width or the available width minus
    // a horizontal margin.
    if (activeIndicatorView == null) {
      return;
    }

    int newWidth =
        min(activeIndicatorDesiredWidth, availableWidth - (activeIndicatorMarginHorizontal * 2));
    LayoutParams indicatorParams = (LayoutParams) activeIndicatorView.getLayoutParams();
    indicatorParams.width = newWidth;
    activeIndicatorView.setLayoutParams(indicatorParams);
  }

  /**
   * Set the desired height of the active indicator.
   *
   * <p>TODO: Consider adjusting based on available height.
   *
   * @param height The desired height of the active indicator.
   */
  public void setActiveIndicatorHeight(int height) {
    if (activeIndicatorView == null) {
      return;
    }

    LayoutParams indicatorParams = (LayoutParams) activeIndicatorView.getLayoutParams();
    indicatorParams.height = height;
    activeIndicatorView.setLayoutParams(indicatorParams);
  }

  /**
   * Set the horizontal margin that will be maintained at the start and end of the active indicator,
   * making sure the indicator remains the given distance from the edge of this item view.
   *
   * @see #updateActiveIndicatorAvailableWidth(int)
   * @param marginHorizontal The horizontal margin, in pixels.
   */
  public void setActiveIndicatorMarginHorizontal(@Px int marginHorizontal) {
    this.activeIndicatorMarginHorizontal = marginHorizontal;
    updateActiveIndicatorAvailableWidth(getWidth());
  }

  /** Get the drawable used as the active indicator. */
  @Nullable
  public Drawable getActiveIndicatorDrawable() {
    if (activeIndicatorView == null) {
      return null;
    }

    return activeIndicatorView.getBackground();
  }

  /** Set the drawable to be used as the active indicator. */
  public void setActiveIndicatorDrawable(@Nullable Drawable activeIndicatorDrawable) {
    if (activeIndicatorView == null) {
      return;
    }

    activeIndicatorView.setBackgroundDrawable(activeIndicatorDrawable);
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

  private int getSuggestedIconWidth() {
    int badgeWidth =
        badgeDrawable == null
            ? 0
            : badgeDrawable.getMinimumWidth() - badgeDrawable.getHorizontalOffset();

    // Account for the fact that the badge may fit within the left or right margin. Give the same
    // space of either side so that icon position does not move if badge gravity is changed.
    LayoutParams iconContainerParams = (LayoutParams) getIconOrContainer().getLayoutParams();
    return max(badgeWidth, iconContainerParams.leftMargin)
        + icon.getMeasuredWidth()
        + max(badgeWidth, iconContainerParams.rightMargin);
  }

  private int getSuggestedIconHeight() {
    int badgeHeight = 0;
    if (badgeDrawable != null) {
      badgeHeight = badgeDrawable.getMinimumHeight() / 2;
    }

    // Account for the fact that the badge may fit within the top margin. Bottom margin is ignored
    // because the icon view will be aligned to the baseline of the label group. But give space for
    // the badge at the bottom as well, so that icon does not move if badge gravity is changed.
    LayoutParams iconContainerParams = (LayoutParams) getIconOrContainer().getLayoutParams();
    return max(badgeHeight, iconContainerParams.topMargin) + icon.getMeasuredWidth() + badgeHeight;
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
