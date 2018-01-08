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

package android.support.design.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.TooltipCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationItemView extends FrameLayout implements MenuView.ItemView {
  public static final int INVALID_ITEM_POSITION = -1;

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

  private final int defaultMargin;
  private final int shiftAmount;
  private final float scaleUpFactor;
  private final float scaleDownFactor;

  private boolean shiftingMode;
  private int labelVisibilityMode;

  private ImageView icon;
  private final TextView smallLabel;
  private final TextView largeLabel;
  private int itemPosition = INVALID_ITEM_POSITION;

  private MenuItemImpl itemData;

  private ColorStateList iconTint;

  public BottomNavigationItemView(@NonNull Context context) {
    this(context, null);
  }

  public BottomNavigationItemView(@NonNull Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomNavigationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    final Resources res = getResources();
    int inactiveLabelSize = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_text_size);
    int activeLabelSize =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_text_size);
    defaultMargin = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_margin);
    shiftAmount = inactiveLabelSize - activeLabelSize;
    scaleUpFactor = 1f * activeLabelSize / inactiveLabelSize;
    scaleDownFactor = 1f * inactiveLabelSize / activeLabelSize;

    LayoutInflater.from(context).inflate(R.layout.design_bottom_navigation_item, this, true);
    setBackgroundResource(R.drawable.design_bottom_navigation_item_background);
    icon = findViewById(R.id.icon);
    smallLabel = findViewById(R.id.smallLabel);
    largeLabel = findViewById(R.id.largeLabel);
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
    setContentDescription(itemData.getContentDescription());
    TooltipCompat.setTooltipText(this, itemData.getTooltipText());
    setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
  }

  public void setItemPosition(int position) {
    itemPosition = position;
  }

  public int getItemPosition() {
    return itemPosition;
  }

  public void setShiftingMode(boolean enabled) {
    if (shiftingMode != enabled) {
      shiftingMode = enabled;

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
      case LabelVisibilityMode.LABEL_VISIBILITY_LEGACY:
        if (shiftingMode) {
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
                icon, defaultMargin + shiftAmount, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
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
              icon, defaultMargin + shiftAmount, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
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
  public void setIcon(Drawable icon) {
    if (icon != null) {
      Drawable.ConstantState state = icon.getConstantState();
      icon = DrawableCompat.wrap(state == null ? icon : state.newDrawable()).mutate();
      DrawableCompat.setTintList(icon, iconTint);
    }
    this.icon.setImageDrawable(icon);
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
    if (itemData != null) {
      // Update the icon so that the tint takes effect
      setIcon(itemData.getIcon());
    }
  }

  public void setTextColor(ColorStateList color) {
    smallLabel.setTextColor(color);
    largeLabel.setTextColor(color);
  }

  public void setItemBackground(int background) {
    Drawable backgroundDrawable =
        background == 0 ? null : ContextCompat.getDrawable(getContext(), background);
    ViewCompat.setBackground(this, backgroundDrawable);
  }
}
