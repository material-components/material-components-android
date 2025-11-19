/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.material.button;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.widget.PopupMenu;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton.OnPressedChangeListener;
import com.google.android.material.button.MaterialButton.WidthChangeDirection;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.ShapeAppearance;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.StateListCornerSize;
import com.google.android.material.shape.StateListShapeAppearanceModel;
import com.google.android.material.shape.StateListSizeChange;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A common container for a set of related {@link MaterialButton}s. The {@link MaterialButton}s in
 * this group will be shown on a single line.
 *
 * <p>This layout currently only supports child views of type {@link MaterialButton}. Buttons can be
 * added to this view group via XML, as follows:
 *
 * <pre>
 * &lt;com.google.android.material.button.MaterialButtonGroup
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@+id/toggle_button_group"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"&gt;
 *
 *     &lt;com.google.android.material.button.MaterialButton
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_private"/&gt;
 *     &lt;com.google.android.material.button.MaterialButton
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_team"/&gt;
 *     &lt;com.google.android.material.button.MaterialButton
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_everyone"/&gt;
 *     &lt;com.google.android.material.button.MaterialButton
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_custom"/&gt;
 *
 * &lt;/com.google.android.material.button.MaterialButtonGroup&gt;
 * </pre>
 *
 * <p>Buttons can also be added to this view group programmatically via the {@link #addView(View)}
 * methods.
 *
 * <p>MaterialButtonGroup is a {@link LinearLayout}. Using {@code
 * android:layout_width="MATCH_PARENT"} and removing {@code android:insetBottom} {@code
 * android:insetTop} on the children is recommended if using {@code VERTICAL}.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Button.md">component
 * developer guidance</a> and <a href="https://material.io/components/buttons/overview">design
 * guidelines</a>.
 */
public class MaterialButtonGroup extends LinearLayout {

  private static final String LOG_TAG = "MButtonGroup";
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_MaterialButtonGroup;
  public static final Object OVERFLOW_BUTTON_TAG = new Object();

  /**
   * A value for {@code overflowMode}. It indicates that there's no handling to the buttons that
   * don't fit to the group size.
   */
  public static final int OVERFLOW_MODE_NONE = 0;

  /**
   * A value for {@code overflowMode}. It indicates that the buttons that don't fit to the group
   * size will be hidden in the group and contained in a popup menu.
   */
  public static final int OVERFLOW_MODE_MENU = 1;

  /**
   * A value for {@code overflowMode}. It indicates that the buttons that don't fit to the group
   * size will be displayed in another row under it.
   */
  public static final int OVERFLOW_MODE_WRAP = 2;

  /**
   * The interface for the overflow mode attribute.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({OVERFLOW_MODE_NONE, OVERFLOW_MODE_MENU, OVERFLOW_MODE_WRAP})
  public @interface OverflowMode {}

  private int overflowMode = OVERFLOW_MODE_NONE;

  private final List<ShapeAppearance> originalChildShapeAppearanceModels = new ArrayList<>();

  private final PressedStateTracker pressedStateTracker = new PressedStateTracker();
  private final Comparator<MaterialButton> childOrderComparator =
      (v1, v2) -> {
        int checked = Boolean.valueOf(v1.isChecked()).compareTo(v2.isChecked());
        if (checked != 0) {
          return checked;
        }

        int stateful = Boolean.valueOf(v1.isPressed()).compareTo(v2.isPressed());
        if (stateful != 0) {
          return stateful;
        }

        // don't return 0s
        return Integer.compare(indexOfChild(v1), indexOfChild(v2));
      };

  private Integer[] childOrder;

  @Nullable StateListCornerSize innerCornerSize;
  @Nullable private StateListShapeAppearanceModel groupStateListShapeAppearance;
  @Px private int spacing;
  @Nullable private StateListSizeChange buttonSizeChange;

  private boolean childShapesDirty = true;

  // Variables for overflow menu mode.
  private final int overflowMenuItemIconPadding;
  private boolean buttonOverflowInitialized;
  private MaterialButton overflowButton;
  private PopupMenu popupMenu;
  private final Map<Integer, Button> popupMenuItemToButtonMapping = new HashMap<>();
  private final Map<Button, MenuItem> buttonToMenuItemMapping = new HashMap<>();
  private final List<Button> tempOverflowButtonsList = new ArrayList<>();
  private final List<Button> overflowButtonsList = new ArrayList<>();

  // Variables for overflow wrap mode.
  private final List<Integer> rowButtonFirstIndices = new ArrayList<>();

  public MaterialButtonGroup(@NonNull Context context) {
    this(context, null);
  }

  public MaterialButtonGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonGroupStyle);
  }

  public MaterialButtonGroup(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialButtonGroup, defStyleAttr, DEF_STYLE_RES);

    if (attributes.hasValue(R.styleable.MaterialButtonGroup_buttonSizeChange)) {
      buttonSizeChange =
          StateListSizeChange.create(
              context, attributes, R.styleable.MaterialButtonGroup_buttonSizeChange);
    }

    if (attributes.hasValue(R.styleable.MaterialButtonGroup_shapeAppearance)) {
      groupStateListShapeAppearance =
          StateListShapeAppearanceModel.create(
              context, attributes, R.styleable.MaterialButtonGroup_shapeAppearance);
      if (groupStateListShapeAppearance == null) {
        groupStateListShapeAppearance =
            new StateListShapeAppearanceModel.Builder(
                    ShapeAppearanceModel.builder(
                            context,
                            attributes.getResourceId(
                                R.styleable.MaterialButtonGroup_shapeAppearance, 0),
                            attributes.getResourceId(
                                R.styleable.MaterialButtonGroup_shapeAppearanceOverlay, 0))
                        .build())
                .build();
      }
    }
    if (attributes.hasValue(R.styleable.MaterialButtonGroup_innerCornerSize)) {
      innerCornerSize =
          StateListCornerSize.create(
              context,
              attributes,
              R.styleable.MaterialButtonGroup_innerCornerSize,
              new AbsoluteCornerSize(0));
    }

    spacing = attributes.getDimensionPixelSize(R.styleable.MaterialButtonGroup_android_spacing, 0);

    setChildrenDrawingOrderEnabled(true);
    setEnabled(attributes.getBoolean(R.styleable.MaterialButtonGroup_android_enabled, true));
    setOverflowMode(
        attributes.getInt(R.styleable.MaterialButtonGroup_overflowMode, OVERFLOW_MODE_NONE));
    // Initializes the overflow menu mode.
    overflowMenuItemIconPadding =
        getResources()
            .getDimensionPixelOffset(R.dimen.m3_btn_group_overflow_item_icon_horizontal_padding);
    if (isOverflowMenuSupported()) {
      initializeButtonOverflow(context, attributes);
    }
    attributes.recycle();
  }

  boolean isOverflowMenuSupported() {
    return true;
  }

  void initializeButtonOverflow(@NonNull Context context, @NonNull TypedArray attributes) {
    Drawable overflowButtonDrawable =
        attributes.getDrawable(R.styleable.MaterialButtonGroup_overflowButtonIcon);
    overflowButton =
        (MaterialButton)
            LayoutInflater.from(context)
                .inflate(R.layout.m3_button_group_overflow_button, this, false);
    overflowButton.setTag(OVERFLOW_BUTTON_TAG);
    setOverflowButtonIcon(overflowButtonDrawable);
    if (overflowButton.getContentDescription() == null) {
      overflowButton.setContentDescription(
          getResources().getString(R.string.mtrl_button_overflow_icon_content_description));
    }
    overflowButton.setVisibility(GONE);

    int overflowMenuStyle =
        MaterialAttributes.resolveOrThrow(this, R.attr.materialButtonGroupPopupMenuStyle);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
      popupMenu = new PopupMenu(getContext(), overflowButton, Gravity.CENTER, 0, overflowMenuStyle);
      popupMenu.setForceShowIcon(true);
    } else {
      popupMenu = new PopupMenu(getContext(), overflowButton, Gravity.CENTER);
    }
    overflowButton.setOnClickListener(
        v -> {
          updateOverflowMenuItemsState();
          popupMenu.show();
        });
    addView(overflowButton);
    buttonOverflowInitialized = true;
  }

  @Override
  protected void dispatchDraw(@NonNull Canvas canvas) {
    updateChildOrder();
    super.dispatchDraw(canvas);
  }

  /**
   * This override prohibits Views other than {@link MaterialButton} to be added. It also makes
   * updates to the add button shape and margins.
   */
  @Override
  public void addView(@NonNull View child, int index, @Nullable ViewGroup.LayoutParams params) {
    if (!(child instanceof MaterialButton)) {
      Log.e(LOG_TAG, "Child views must be of type MaterialButton.");
      return;
    }

    // Recover the original layout params of all children before adding the new child.
    recoverAllChildrenLayoutParams();
    childShapesDirty = true;
    // If overflow button has been added, the new child button will be always added before the
    // overflow button.
    int overflowButtonIndex = indexOfChild(overflowButton);
    if (overflowButtonIndex >= 0 && index == -1) {
      super.addView(child, overflowButtonIndex, params);
    } else {
      super.addView(child, index, params);
    }
    MaterialButton buttonChild = (MaterialButton) child;
    setGeneratedIdIfNeeded(buttonChild);
    buttonChild.setOnPressedChangeListenerInternal(pressedStateTracker);

    // Saves original child shape appearance.
    originalChildShapeAppearanceModels.add(buttonChild.getShapeAppearance());

    // Enable children based on the MaterialButtonToggleGroup own isEnabled
    buttonChild.setEnabled(isEnabled());
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);

    if (child instanceof MaterialButton) {
      ((MaterialButton) child).setOnPressedChangeListenerInternal(null);
    }

    int indexOfChild = indexOfChild(child);
    if (indexOfChild >= 0) {
      originalChildShapeAppearanceModels.remove(indexOfChild);
    }

    childShapesDirty = true;
    updateChildShapes();

    // Recover the original layout params of all children before updating the child layout.
    recoverAllChildrenLayoutParams();
    adjustChildMarginsAndUpdateLayout();
  }

  /**
   * Returns the original {@link ShapeAppearanceModel} for the {@link MaterialButton} child at the
   * given index.
   */
  @NonNull
  public ShapeAppearanceModel getChildOriginalShapeAppearanceModel(int index) {
    return originalChildShapeAppearanceModels.get(index).getDefaultShape();
  }

  private void recoverAllChildrenLayoutParams() {
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton child = getChildButton(i);
      child.recoverOriginalLayoutParams();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    adjustChildMarginsAndUpdateLayout();
    int wrappedHeight = 0;
    if (overflowMode == OVERFLOW_MODE_WRAP) {
      if (getOrientation() == VERTICAL) {
        throw new IllegalArgumentException(
            "The wrap overflow mode is not compatible to the vertical orientation.");
      }
      if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
        throw new IllegalArgumentException(
            "The wrap overflow mode is not compatible with wrap_content layout width.");
      }
      wrappedHeight = maybeWrapButtons(widthMeasureSpec, heightMeasureSpec);
    }
    maybeUpdateOverflowMenu(widthMeasureSpec, heightMeasureSpec);
    updateChildShapes();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (overflowMode == OVERFLOW_MODE_WRAP && wrappedHeight != getMeasuredHeight()) {
      setMeasuredDimension(getMeasuredWidth(), wrappedHeight);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (changed) {
      recoverAllChildrenLayoutParams();
      adjustChildSizeChange();
    }
  }

  // =================== Utility functions for overflow wrap mode. =======================

  private int maybeWrapButtons(int widthMeasureSpec, int heightMeasureSpec) {
    rowButtonFirstIndices.clear();
    int availableWidth = MeasureSpec.getSize(widthMeasureSpec);
    List<Integer> currentRowButtonIndices = new ArrayList<>();
    int rowWidth = 0;
    int rowHeight = 0;
    List<Integer> rowWidthList = new ArrayList<>();
    int prevRowsHeight = 0;
    for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
      if (!isChildVisible(childIndex)) {
        continue;
      }
      MaterialButton child = getChildButton(childIndex);
      measureChild(child, widthMeasureSpec, heightMeasureSpec);
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();
      if (childWidth <= 0) {
        continue;
      }
      LinearLayout.LayoutParams params = buildLayoutParams(child);
      int rowWidthWithCurrentChild =
          rowWidth + childWidth + (currentRowButtonIndices.isEmpty() ? 0 : spacing);
      // Saves the info of the current row and resets it, if the current row is empty or the
      // current button doesn't fit in the current row.
      if (rowWidthWithCurrentChild > availableWidth || currentRowButtonIndices.isEmpty()) {
        // Saves the width of the current row (the current button is excluded).
        if (!currentRowButtonIndices.isEmpty()) {
          rowWidthList.add(rowWidth);
        }
        // Updates the total height of all rows above the next row.
        prevRowsHeight += rowHeight + (rowButtonFirstIndices.isEmpty() ? 0 : spacing);
        // Saves the first button index of the current row.
        rowButtonFirstIndices.add(childIndex);
        // Adjusts the start margin of the first child in the row, as a part of the linear layout
        // behavior, the start margin will be also applied to the rest children in the same row.
        params.setMarginStart(-rowWidth);
        // Resets for the next row.
        currentRowButtonIndices.clear();
        rowWidth = 0;
        rowHeight = 0;
      }
      // Adds the current button in the current row.
      rowWidth += childWidth + (rowWidth == 0 ? 0 : spacing);
      rowHeight = max(rowHeight, childHeight);
      currentRowButtonIndices.add(childIndex);
      params.topMargin += prevRowsHeight;
      child.setLayoutParams(params);
    }
    // Add the width of the last row.
    rowWidthList.add(rowWidth);
    // At this point, all rows are aligned at the start edge. We need to offset each row to make
    // them aligned properly according to the gravity.
    int lastOffset = 0;
    int maxRowWidth = Collections.max(rowWidthList);
    for (int i = 0; i < rowButtonFirstIndices.size(); i++) {
      int rowFirstButtonIndex = rowButtonFirstIndices.get(i);
      rowWidth = rowWidthList.get(i);
      MaterialButton childButton = getChildButton(rowFirstButtonIndex);

      LinearLayout.LayoutParams params = buildLayoutParams(childButton);
      int horizontalRelativeGravity = params.gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
      int horizontalAbsoluteGravity =
          Gravity.getAbsoluteGravity(horizontalRelativeGravity, getLayoutDirection());
      int offset = maxRowWidth - rowWidth;
      // If no offset or expecting left aligned, skip to adjust.
      if (horizontalRelativeGravity == Gravity.START) {
        continue;
      }
      // Half the difference if expecting center aligned.
      if (horizontalAbsoluteGravity == Gravity.CENTER_HORIZONTAL) {
        offset /= 2;
      }
      // The offset of the previous row will be applied to the current row. So we need to counter
      // measure it from the current row offset.
      params.setMarginStart(params.getMarginStart() + offset - lastOffset);
      childButton.setLayoutParams(params);
      lastOffset = offset;
    }

    return prevRowsHeight + rowHeight + getPaddingTop() + getPaddingBottom();
  }

  // =================== Utility functions for overflow menu mode. =======================

  private void maybeUpdateOverflowMenu(int widthMeasureSpec, int heightMeasureSpec) {
    if (!buttonOverflowInitialized) {
      return;
    }
    if (overflowMode != OVERFLOW_MODE_MENU) {
      overflowButton.setVisibility(GONE);
      return;
    }

    boolean isHorizontal = getOrientation() == HORIZONTAL;
    tempOverflowButtonsList.clear();

    int availableSize =
        isHorizontal
            ? MeasureSpec.getSize(widthMeasureSpec)
            : MeasureSpec.getSize(heightMeasureSpec);
    int overflowButtonSize =
        measureAndGetChildButtonSize(
            isHorizontal, overflowButton, widthMeasureSpec, heightMeasureSpec);
    int currentDisplayedSize = 0;
    boolean shouldShowOverflow = false;

    for (int childIndex = 0; childIndex < getChildCount() - 1; childIndex++) {
      Button child = getChildButton(childIndex);
      int childSize =
          measureAndGetChildButtonSize(isHorizontal, child, widthMeasureSpec, heightMeasureSpec);
      // Mark the child to be overflowed if the current total size of the buttons to be displayed
      // in the group exceeds the available size with the overflow button.
      if (currentDisplayedSize + childSize + overflowButtonSize > availableSize) {
        tempOverflowButtonsList.add(child);
      }
      if (currentDisplayedSize + childSize > availableSize) {
        // If it ends up that we need to show an overflow, mark the rest of the children to be
        // overflowed.
        shouldShowOverflow = true;
        childIndex++;
        while (childIndex < getChildCount() - 1) {
          tempOverflowButtonsList.add(getChildButton(childIndex++));
        }
        break;
      } else {
        currentDisplayedSize += childSize;
      }
    }

    if (shouldShowOverflow) {
      overflowButton.setVisibility(VISIBLE);
    } else {
      overflowButton.setVisibility(GONE);
      // Resets the overflow button list for updating the buttons visibility below.
      tempOverflowButtonsList.clear();
    }
    // Updates the overflow menu items. This will hide buttons overflowed, and show the other
    // buttons.
    maybeUpdateOverflowMenuItemsAndChildVisibility();
  }

  private void maybeUpdateOverflowMenuItemsAndChildVisibility() {
    if (tempOverflowButtonsList.equals(overflowButtonsList)) {
      return;
    }
    // Recover the buttons visibility in the current overflow menu.
    for (int i = 0; i < getChildCount() - 1; i++) {
      Button child = getChildButton(i);
      if (buttonToMenuItemMapping.containsKey(child)) {
        child.setVisibility(VISIBLE);
      }
    }
    overflowButtonsList.clear();
    overflowButtonsList.addAll(tempOverflowButtonsList);
    // Creates the menu items for the overflow buttons.
    Menu menu = popupMenu.getMenu();
    // Clears the popup menu first.
    popupMenuItemToButtonMapping.clear();
    buttonToMenuItemMapping.clear();
    menu.clear();
    // Adds the buttons to the overflow menu and hide them.
    for (Button child : overflowButtonsList) {
      MenuItem item = addMenuItemForButton(menu, child);
      if (item == null) {
        continue;
      }
      popupMenuItemToButtonMapping.put(item.getItemId(), child);
      buttonToMenuItemMapping.put(child, item);
      child.setVisibility(GONE);
    }
    // Updates the overflow menu items state.
    updateOverflowMenuItemsState();
  }

  private int measureAndGetChildButtonSize(
      boolean isHorizontal, Button button, int widthMeasureSpec, int heightMeasureSpec) {
    measureChild(button, widthMeasureSpec, heightMeasureSpec);
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) button.getLayoutParams();
    int containerSize = isHorizontal ? button.getMeasuredWidth() : button.getMeasuredHeight();
    int margins = isHorizontal ? lp.leftMargin + lp.rightMargin : lp.topMargin + lp.bottomMargin;
    // Child measured size may be zero in some cases, like if its final size is being determined by
    // layout weight, so use minimum size instead for such cases.
    if (containerSize == 0) {
      containerSize = isHorizontal ? button.getMinimumWidth() : button.getMinimumHeight();
    }
    return containerSize + margins;
  }

  @Nullable
  private MenuItem addMenuItemForButton(@NonNull Menu menu, @NonNull Button button) {
    if (!(button.getLayoutParams() instanceof MaterialButtonGroup.LayoutParams)) {
      return null;
    }
    MaterialButtonGroup.LayoutParams lp =
        (MaterialButtonGroup.LayoutParams) button.getLayoutParams();
    CharSequence text = OverflowUtils.getMenuItemText(button, lp.overflowText);
    Drawable icon = lp.overflowIcon;
    MenuItem item = menu.add(text);
    if (icon != null) {
      item.setIcon(
          new InsetDrawable(icon, overflowMenuItemIconPadding, 0, overflowMenuItemIconPadding, 0));
    }
    item.setOnMenuItemClickListener(
        menuItem -> {
          button.performClick();
          return true;
        });
    return item;
  }

  private void updateOverflowMenuItemsState() {
    for (Map.Entry<Button, MenuItem> entry : buttonToMenuItemMapping.entrySet()) {
      Button button = entry.getKey();
      MenuItem item = entry.getValue();
      if (entry.getKey() instanceof MaterialButton) {
        MaterialButton materialButton = (MaterialButton) button;
        item.setCheckable(materialButton.isCheckable());
        item.setChecked(materialButton.isChecked());
      }
      item.setEnabled(button.isEnabled());
    }
  }

  // =================== Utility functions for connected shape morph. =======================

  /**
   * Sets all corner radii override to inner corner size except for leftmost and rightmost corners.
   */
  @VisibleForTesting
  void updateChildShapes() {
    // No need to update shape if no inside corners or outer corners are specified.
    if ((innerCornerSize == null && groupStateListShapeAppearance == null) || !childShapesDirty) {
      return;
    }
    childShapesDirty = false;
    int childCount = getChildCount();
    int firstVisibleChildIndex = getFirstVisibleChildIndex();
    int lastVisibleChildIndex = getLastVisibleChildIndex();
    for (int i = 0; i < childCount; i++) {
      MaterialButton button = getChildButton(i);
      if (button.getVisibility() == GONE) {
        continue;
      }
      boolean isFirstVisible = i == firstVisibleChildIndex;
      boolean isLastVisible = i == lastVisibleChildIndex;

      StateListShapeAppearanceModel.Builder originalStateListShapeBuilder =
          getOriginalStateListShapeBuilder(isFirstVisible, isLastVisible, i);
      // Determines which corners of the original shape should be kept.
      boolean isHorizontal = getOrientation() == HORIZONTAL;
      boolean isRtl = ViewUtils.isLayoutRtl(this);
      int cornerPositionBitsToKeep = 0;
      if (isHorizontal) {
        // When horizontal (ltr), keeps the left two original corners for the first button.
        if (isFirstVisible) {
          cornerPositionBitsToKeep |=
              ShapeAppearanceModel.CORNER_TOP_LEFT | ShapeAppearanceModel.CORNER_BOTTOM_LEFT;
        }
        // When horizontal (ltr), keeps the right two original corners for the last button.
        if (isLastVisible) {
          cornerPositionBitsToKeep |=
              ShapeAppearanceModel.CORNER_TOP_RIGHT | ShapeAppearanceModel.CORNER_BOTTOM_RIGHT;
        }
        // If rtl, swap the position bits of left corners and right corners.
        if (isRtl) {
          cornerPositionBitsToKeep =
              StateListShapeAppearanceModel.swapCornerPositionRtl(cornerPositionBitsToKeep);
        }
      } else {
        // When vertical, keeps the top two original corners for the first button.
        if (isFirstVisible) {
          cornerPositionBitsToKeep |=
              ShapeAppearanceModel.CORNER_TOP_LEFT | ShapeAppearanceModel.CORNER_TOP_RIGHT;
        }
        // When vertical, keeps the bottom two original corners for the last button.
        if (isLastVisible) {
          cornerPositionBitsToKeep |=
              ShapeAppearanceModel.CORNER_BOTTOM_LEFT | ShapeAppearanceModel.CORNER_BOTTOM_RIGHT;
        }
      }
      // Overrides the corners that don't need to keep with unary operator.
      int cornerPositionBitsToOverride = ~cornerPositionBitsToKeep;
      StateListShapeAppearanceModel newStateListShape =
          originalStateListShapeBuilder
              .setCornerSizeOverride(innerCornerSize, cornerPositionBitsToOverride)
              .build();
      button.setShapeAppearance(
          newStateListShape.isStateful()
              ? newStateListShape
              : newStateListShape.getDefaultShape(/* withCornerSizeOverrides= */ true));
    }
  }

  /**
   * Returns a {@link StateListShapeAppearanceModel.Builder} as the original shape of a child
   * button.
   *
   * <p>It takes the group shape, if specified, as the original state list shape for the first and
   * last buttons. Otherwise, it takes the state list shape (or build one from the shape appearance
   * model, if state list shape is not specified) in the child button.
   *
   * @param isFirstVisible Whether this is the first visible child button regardless its index.
   * @param isLastVisible Whether this is the last visible child button regardless its index.
   * @param index The index of the child button.
   */
  @NonNull
  private StateListShapeAppearanceModel.Builder getOriginalStateListShapeBuilder(
      boolean isFirstVisible, boolean isLastVisible, int index) {
    ShapeAppearance originalStateList =
        groupStateListShapeAppearance != null && (isFirstVisible || isLastVisible)
            ? groupStateListShapeAppearance
            : originalChildShapeAppearanceModels.get(index);
    // If the state list shape is not specified, creates one from the shape appearance model.
    return !(originalStateList instanceof StateListShapeAppearanceModel)
        ? new StateListShapeAppearanceModel.Builder(
            (ShapeAppearanceModel) originalChildShapeAppearanceModels.get(index))
        : ((StateListShapeAppearanceModel) originalStateList).toBuilder();
  }

  // =================== Utility functions for spacing. =======================

  /**
   * We keep track of which views are pressed and checked to draw them last. This prevents visual
   * issues with overlapping strokes.
   */
  @Override
  protected int getChildDrawingOrder(int childCount, int i) {
    if (childOrder == null || i >= childOrder.length) {
      Log.w(LOG_TAG, "Child order wasn't updated");
      return i;
    }

    return childOrder[i];
  }

  /**
   * Sets a negative marginStart on all but the first child, if two adjacent children both have a
   * stroke width greater than 0. This prevents a double-width stroke from being drawn for two
   * adjacent stroked children, and instead draws the adjacent strokes directly on top of each
   * other.
   *
   * <p>The negative margin adjustment amount will be equal to the smaller of the two adjacent
   * stroke widths.
   *
   * <p>Also rearranges children such that they are shown in the correct visual order.
   */
  private void adjustChildMarginsAndUpdateLayout() {
    int firstVisibleChildIndex = getFirstVisibleChildIndex();
    if (firstVisibleChildIndex == -1) {
      return;
    }

    for (int i = firstVisibleChildIndex + 1; i < getChildCount(); i++) {
      int smallestStrokeWidth = 0;
      View currentChild = getChildAt(i);
      View previousChild = getChildAt(i - 1);
      if (currentChild instanceof MaterialButton && previousChild instanceof MaterialButton) {
        // Only adjusts margins for the outline, if both adjacent children are MaterialButtons
        MaterialButton currentButton = (MaterialButton) currentChild;
        MaterialButton previousButton = (MaterialButton) previousChild;
        // Calculates the margin adjustment to be the smaller of the two adjacent stroke widths
        if (spacing <= 0) {
          smallestStrokeWidth =
              min(currentButton.getStrokeWidth(), previousButton.getStrokeWidth());
          // Enables the flag to draw additional layer of surface color under the stroke, which may
          // overlap with its neighbors and results unintended color when the stroke color is
          // semi-transparent.
          currentButton.setShouldDrawSurfaceColorStroke(true);
          previousButton.setShouldDrawSurfaceColorStroke(true);
        } else {
          currentButton.setShouldDrawSurfaceColorStroke(false);
          previousButton.setShouldDrawSurfaceColorStroke(false);
        }
      }

      LinearLayout.LayoutParams params = buildLayoutParams(currentChild);
      if (getOrientation() == HORIZONTAL) {
        params.setMarginEnd(0);
        params.setMarginStart(spacing - smallestStrokeWidth);
        params.topMargin = 0;
      } else {
        params.bottomMargin = 0;
        params.topMargin = spacing - smallestStrokeWidth;
        params.setMarginStart(0);
      }

      currentChild.setLayoutParams(params);
    }

    resetChildMargins(firstVisibleChildIndex);
  }

  private void resetChildMargins(int childIndex) {
    if (getChildCount() == 0 || childIndex == -1) {
      return;
    }

    MaterialButton currentButton = getChildButton(childIndex);
    LinearLayout.LayoutParams params = buildLayoutParams(currentButton);
    if (getOrientation() == VERTICAL) {
      params.topMargin = 0;
      params.bottomMargin = 0;
      return;
    }

    params.setMarginEnd(0);
    params.setMarginStart(0);
    params.leftMargin = 0;
    params.rightMargin = 0;
  }

  // ================== Utility functions for width morph. =======================

  void onButtonWidthChanged(@NonNull MaterialButton button, int increaseSize) {
    int buttonIndex = indexOfChild(button);
    if (buttonIndex < 0) {
      return;
    }
    MaterialButton prevVisibleButton =
        getPrevVisibleChildButton(buttonIndex, /* inSameRow= */ true);
    MaterialButton nextVisibleButton =
        getNextVisibleChildButton(buttonIndex, /* inSameRow= */ true);
    if (prevVisibleButton == null && nextVisibleButton == null) {
      return;
    }
    if (prevVisibleButton == null) {
      nextVisibleButton.setDisplayedWidthDecrease(increaseSize);
    }
    if (nextVisibleButton == null) {
      prevVisibleButton.setDisplayedWidthDecrease(increaseSize);
    }
    if (prevVisibleButton != null && nextVisibleButton != null) {
      // If there are two neighbors, each neighbor will absorb half of the expanded amount.
      prevVisibleButton.setDisplayedWidthDecrease(increaseSize / 2);
      // We want to avoid one pixel missing due to the casting, when increaseSize is odd.
      nextVisibleButton.setDisplayedWidthDecrease((increaseSize + 1) / 2);
    }
  }

  /**
   * Adjusts the max amount of size to expand for each child button. So that it won't squeeze its
   * neighbors too much to cause text truncation; and the expansion amount per edge is same for all
   * buttons,
   */
  private void adjustChildSizeChange() {
    int firstVisibleChildIndex = getFirstVisibleChildIndex();
    int lastVisibleChildIndex = getLastVisibleChildIndex();
    if (firstVisibleChildIndex == -1 || buttonSizeChange == null) {
      return;
    }
    if (overflowMode == OVERFLOW_MODE_WRAP) {
      for (int i = 0; i < rowButtonFirstIndices.size(); i++) {
        adjustChildSizeChangeInRange(
            rowButtonFirstIndices.get(i),
            i == rowButtonFirstIndices.size() - 1
                ? getChildCount() - 1
                : rowButtonFirstIndices.get(i + 1) - 1);
      }
    } else {
      adjustChildSizeChangeInRange(firstVisibleChildIndex, lastVisibleChildIndex);
    }
  }

  private void adjustChildSizeChangeInRange(int start, int end) {
    if (start == end) {
      getChildButton(start).setWidthChangeDirection(WidthChangeDirection.NONE);
      return;
    }
    int widthIncreaseOnSingleEdge = Integer.MAX_VALUE;
    // First pass: find the max width increase on single edge.
    for (int i = start; i <= end; i++) {
      if (!isChildVisible(i)) {
        continue;
      }
      // Sets the width change direction for each child button.
      getChildButton(i)
          .setWidthChangeDirection(
              i == start
                  ? WidthChangeDirection.END
                  : i == end ? WidthChangeDirection.START : WidthChangeDirection.BOTH);
      // Calculates the allowed width increase for each child button with consideration of the max
      // allowed width decrease of its neighbors.
      int widthIncrease = getButtonAllowedWidthIncrease(i);

      // If the button expands on both edges, the width increase on each edge should be half of
      // the total width increase. Calculates the minimum width increase on each edge, so that all
      // buttons won't squeeze their neighbors too much.
      widthIncreaseOnSingleEdge =
          min(
              widthIncreaseOnSingleEdge,
              i != start && i != end ? widthIncrease / 2 : widthIncrease);
    }
    // Second pass: set the width change for each child button.
    for (int i = start; i <= end; i++) {
      if (!isChildVisible(i)) {
        continue;
      }
      MaterialButton child = getChildButton(i);
      child.setSizeChange(buttonSizeChange);
      // Assuming buttons can be expanded in both directions, the total width increase should be
      // double of the single edge increase.
      child.setWidthChangeMax(widthIncreaseOnSingleEdge * 2);
    }
  }

  /**
   * Returns the allowed width increase for a child button.
   *
   * <p>The allowed width increase is the smaller amount of the max width increase of the button in
   * all states and the total allowed width decrease of its neighbors.
   */
  private int getButtonAllowedWidthIncrease(int index) {
    if (!isChildVisible(index) || buttonSizeChange == null) {
      return 0;
    }
    MaterialButton currentButton = getChildButton(index);
    int widthIncrease = max(0, buttonSizeChange.getMaxWidthChange(currentButton.getWidth()));
    // Checking neighbors' allowed width decrease.
    MaterialButton prevVisibleButton = getPrevVisibleChildButton(index, /* inSameRow= */ true);
    int prevButtonAllowedWidthDecrease =
        prevVisibleButton == null ? 0 : prevVisibleButton.getAllowedWidthDecrease();
    MaterialButton nextVisibleButton = getNextVisibleChildButton(index, /* inSameRow= */ true);
    int nextButtonAllowedWidthDecrease =
        nextVisibleButton == null ? 0 : nextVisibleButton.getAllowedWidthDecrease();
    return min(widthIncrease, prevButtonAllowedWidthDecrease + nextButtonAllowedWidthDecrease);
  }

  // ================ Getters and setters ===================

  @Override
  public void setOrientation(int orientation) {
    if (getOrientation() != orientation) {
      childShapesDirty = true;
    }
    super.setOrientation(orientation);
  }

  /**
   * Returns the {@link StateListSizeChange} of child buttons on state changes.
   *
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @Nullable
  public StateListSizeChange getButtonSizeChange() {
    return buttonSizeChange;
  }

  /**
   * Sets the {@link StateListSizeChange} of child buttons on state changes.
   *
   * @param buttonSizeChange The new {@link StateListSizeChange} of child buttons.
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void setButtonSizeChange(@NonNull StateListSizeChange buttonSizeChange) {
    if (this.buttonSizeChange != buttonSizeChange) {
      this.buttonSizeChange = buttonSizeChange;
      adjustChildSizeChange();
      requestLayout();
      invalidate();
    }
  }

  /** Returns the spacing (in pixels) between each button in the group. */
  @Px
  public int getSpacing() {
    return spacing;
  }

  /**
   * Sets the spacing between each button in the group.
   *
   * @param spacing the spacing (in pixels) between each button in the group
   */
  public void setSpacing(@Px int spacing) {
    this.spacing = spacing;
    invalidate();
    requestLayout();
  }

  /** Returns the inner corner size of the group. */
  @NonNull
  public CornerSize getInnerCornerSize() {
    return innerCornerSize.getDefaultCornerSize();
  }

  /**
   * Sets the inner corner size of the group.
   *
   * <p>Can set as an {@link AbsoluteCornerSize} or {@link RelativeCornerSize}. Don't set relative
   * corner size larger than 50% or absolute corner size larger than half height to avoid corner
   * overlapping.
   *
   * @param cornerSize the inner corner size of the group
   */
  public void setInnerCornerSize(@NonNull CornerSize cornerSize) {
    innerCornerSize = StateListCornerSize.create(cornerSize);
    childShapesDirty = true;
    updateChildShapes();
    invalidate();
  }

  /**
   * Returns the inner corner size state list of the group.
   *
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public StateListCornerSize getInnerCornerSizeStateList() {
    return innerCornerSize;
  }

  /**
   * Sets the inner corner size state list of the group.
   *
   * <p>Can set as an {@link StateListCornerSize}. Don't set relative corner size larger than 50% or
   * absolute corner size larger than half height to the corner size in any state to avoid corner
   * overlapping.
   *
   * @param cornerSizeStateList the inner corner size state list of the group
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void setInnerCornerSizeStateList(@NonNull StateListCornerSize cornerSizeStateList) {
    innerCornerSize = cornerSizeStateList;
    childShapesDirty = true;
    updateChildShapes();
    invalidate();
  }

  /** Returns the {@link ShapeAppearanceModel} of the group. */
  @Nullable
  public ShapeAppearanceModel getShapeAppearance() {
    return groupStateListShapeAppearance == null
        ? null
        : groupStateListShapeAppearance.getDefaultShape(/* withCornerSizeOverrides= */ true);
  }

  /**
   * Sets the {@link ShapeAppearanceModel} of the group.
   *
   * @param shapeAppearance The new {@link ShapeAppearanceModel} of the group.
   */
  public void setShapeAppearance(@Nullable ShapeAppearanceModel shapeAppearance) {
    groupStateListShapeAppearance =
        new StateListShapeAppearanceModel.Builder(shapeAppearance).build();
    childShapesDirty = true;
    updateChildShapes();
    invalidate();
  }

  /**
   * Returns the {@link StateListShapeAppearanceModel} of the group.
   *
   * @hide
   */
  @Nullable
  @RestrictTo(Scope.LIBRARY_GROUP)
  public StateListShapeAppearanceModel getStateListShapeAppearance() {
    return groupStateListShapeAppearance;
  }

  /**
   * Sets the {@link StateListShapeAppearanceModel} of the group.
   *
   * @param stateListShapeAppearance The new {@link StateListShapeAppearanceModel} of the group.
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void setStateListShapeAppearance(
      @Nullable StateListShapeAppearanceModel stateListShapeAppearance) {
    groupStateListShapeAppearance = stateListShapeAppearance;
    childShapesDirty = true;
    updateChildShapes();
    invalidate();
  }

  /**
   * Sets the icon to show for the overflow button.
   *
   * @param icon Drawable to use for the overflow button's icon.
   * @attr ref com.google.android.material.R.styleable#MaterialButtonGroup_overflowButtonIcon
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
   * @attr ref com.google.android.material.R.styleable#MaterialButtonGroup_overflowButtonIcon
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
   * @attr ref com.google.android.material.R.styleable#MaterialButtonGroup_overflowButtonIcon
   * @see #setOverflowButtonIcon(Drawable)
   * @see #setOverflowButtonIconResource(int)
   */
  @Nullable
  public Drawable getOverflowButtonIcon() {
    return overflowButton.getIcon();
  }

  /** Sets the overflow mode. */
  public void setOverflowMode(@OverflowMode int overflowMode) {
    if (this.overflowMode != overflowMode) {
      this.overflowMode = overflowMode;
      requestLayout();
      invalidate();
    }
  }

  /** Returns the overflow mode. */
  @OverflowMode
  public int getOverflowMode() {
    return overflowMode;
  }

  // ================ Helper functions ===================

  @NonNull
  MaterialButton getChildButton(int index) {
    return (MaterialButton) getChildAt(index);
  }

  @NonNull
  LinearLayout.LayoutParams buildLayoutParams(@NonNull View child) {
    ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
    if (layoutParams instanceof LinearLayout.LayoutParams) {
      return (LinearLayout.LayoutParams) layoutParams;
    }

    return new LayoutParams(layoutParams.width, layoutParams.height);
  }

  private int getFirstVisibleChildIndex() {
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      if (isChildVisible(i)) {
        return i;
      }
    }

    return -1;
  }

  private int getLastVisibleChildIndex() {
    int childCount = getChildCount();
    for (int i = childCount - 1; i >= 0; i--) {
      if (isChildVisible(i)) {
        return i;
      }
    }

    return -1;
  }

  private boolean isChildVisible(int i) {
    View child = getChildAt(i);
    return child.getVisibility() != View.GONE;
  }

  private void setGeneratedIdIfNeeded(@NonNull MaterialButton materialButton) {
    // Generates an ID if none is set, for relative positioning purposes
    if (materialButton.getId() == View.NO_ID) {
      materialButton.setId(View.generateViewId());
    }
  }

  @Nullable
  private MaterialButton getNextVisibleChildButton(int index) {
    return getNextVisibleChildButton(index, /* inSameRow= */ false);
  }

  @Nullable
  private MaterialButton getNextVisibleChildButton(int index, boolean inSameRow) {
    int childCount = getChildCount();
    int nextVisibleButtonIndex = -1;
    for (int i = index + 1; i < childCount; i++) {
      if (isChildVisible(i)) {
        nextVisibleButtonIndex = i;
        break;
      }
    }
    if (inSameRow && !rowButtonFirstIndices.isEmpty()) {
      for (int i = 0; i < rowButtonFirstIndices.size(); i++) {
        int start = rowButtonFirstIndices.get(i);
        int end =
            i == rowButtonFirstIndices.size() - 1
                ? childCount - 1
                : rowButtonFirstIndices.get(i + 1) - 1;
        // Returns null if the current button and the found next visible button are not in the same
        // row as required.
        if (index >= start
            && index <= end
            && (nextVisibleButtonIndex < start || nextVisibleButtonIndex > end)) {
          return null;
        }
      }
    }

    return nextVisibleButtonIndex == -1 ? null : getChildButton(nextVisibleButtonIndex);
  }

  @Nullable
  private MaterialButton getPrevVisibleChildButton(int index) {
    return getPrevVisibleChildButton(index, /* inSameRow= */ false);
  }

  @Nullable
  private MaterialButton getPrevVisibleChildButton(int index, boolean inSameRow) {
    int childCount = getChildCount();
    int prevVisibleButtonIndex = -1;
    for (int i = index - 1; i >= 0; i--) {
      if (isChildVisible(i)) {
        prevVisibleButtonIndex = i;
        break;
      }
    }
    if (inSameRow && !rowButtonFirstIndices.isEmpty()) {
      for (int i = 0; i < rowButtonFirstIndices.size(); i++) {
        int start = rowButtonFirstIndices.get(i);
        int nextStart =
            i == rowButtonFirstIndices.size() - 1 ? childCount : rowButtonFirstIndices.get(i + 1);
        // Returns null if the current button and the found prev visible button are not in the same
        // row as required.
        if (index >= start
            && index < nextStart
            && (prevVisibleButtonIndex < start || prevVisibleButtonIndex >= nextStart)) {
          return null;
        }
      }
    }

    return prevVisibleButtonIndex == -1 ? null : getChildButton(prevVisibleButtonIndex);
  }

  private void updateChildOrder() {
    final SortedMap<MaterialButton, Integer> viewToIndexMap = new TreeMap<>(childOrderComparator);
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      viewToIndexMap.put(getChildButton(i), i);
    }

    childOrder = viewToIndexMap.values().toArray(new Integer[0]);
  }

  /**
   * Enables this {@link MaterialButtonGroup} and all its {@link MaterialButton} children
   *
   * @param enabled boolean to setEnable {@link MaterialButtonGroup}
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    // Enable or disable child buttons
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton childButton = getChildButton(i);
      childButton.setEnabled(enabled);
    }
  }

  private class PressedStateTracker implements OnPressedChangeListener {
    @Override
    public void onPressedChanged(@NonNull MaterialButton button, boolean isPressed) {
      invalidate();
    }
  }

  @Override
  @NonNull
  protected MaterialButtonGroup.LayoutParams generateDefaultLayoutParams() {
    return new MaterialButtonGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  @Override
  @NonNull
  public MaterialButtonGroup.LayoutParams generateLayoutParams(@Nullable AttributeSet attrs) {
    return new MaterialButtonGroup.LayoutParams(getContext(), attrs);
  }

  @Override
  @NonNull
  protected MaterialButtonGroup.LayoutParams generateLayoutParams(
      @NonNull ViewGroup.LayoutParams p) {
    if (p instanceof LinearLayout.LayoutParams) {
      return new MaterialButtonGroup.LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new MaterialButtonGroup.LayoutParams((MarginLayoutParams) p);
    }
    return new MaterialButtonGroup.LayoutParams(p);
  }

  @Override
  protected boolean checkLayoutParams(@NonNull ViewGroup.LayoutParams p) {
    return p instanceof MaterialButtonGroup.LayoutParams;
  }

  /** A {@link LinearLayout.LayoutParams} implementation for {@link MaterialButtonGroup}. */
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
          context.obtainStyledAttributes(attrs, R.styleable.MaterialButtonGroup_Layout);

      overflowIcon =
          attributes.getDrawable(R.styleable.MaterialButtonGroup_Layout_layout_overflowIcon);
      overflowText = attributes.getText(R.styleable.MaterialButtonGroup_Layout_layout_overflowText);

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
    public LayoutParams(@NonNull MaterialButtonGroup.LayoutParams source) {
      super(source);
      this.overflowText = source.overflowText;
      this.overflowIcon = source.overflowIcon;
    }
  }

  /**
   * Class for common logic between this MaterialButtonGroup and the OverflowLinearLayout overflow
   * features.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public static class OverflowUtils {
    private OverflowUtils() {}

    @Nullable
    public static CharSequence getMenuItemText(@NonNull View view, @Nullable CharSequence text) {
      if (!TextUtils.isEmpty(text)) {
        return text;
      }
      if (view instanceof MaterialButton && !TextUtils.isEmpty(((MaterialButton) view).getText())) {
        // Use button's text if overflow text is not specified or empty. We don't do this to icon,
        // since icon in menu item is optional.
        return ((MaterialButton) view).getText();
      }
      // As a last resort, use content description.
      return view.getContentDescription();
    }
  }
}
