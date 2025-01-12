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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton.OnPressedChangeListener;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.StateListCornerSize;
import com.google.android.material.shape.StateListShapeAppearanceModel;
import com.google.android.material.shape.StateListSizeChange;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

  private final List<ShapeAppearanceModel> originalChildShapeAppearanceModels = new ArrayList<>();
  private final List<StateListShapeAppearanceModel> originalChildStateListShapeAppearanceModels =
      new ArrayList<>();

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
    attributes.recycle();
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

    super.addView(child, index, params);
    MaterialButton buttonChild = (MaterialButton) child;
    setGeneratedIdIfNeeded(buttonChild);
    buttonChild.setOnPressedChangeListenerInternal(pressedStateTracker);

    // Saves original child shape appearance.
    originalChildShapeAppearanceModels.add(buttonChild.getShapeAppearanceModel());
    originalChildStateListShapeAppearanceModels.add(buttonChild.getStateListShapeAppearanceModel());

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
      originalChildStateListShapeAppearanceModels.remove(indexOfChild);
    }

    updateChildShapes();
    adjustChildMarginsAndUpdateLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    updateChildShapes();
    adjustChildMarginsAndUpdateLayout();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (changed) {
      adjustChildSizeChange();
    }
  }

  /**
   * Sets all corner radii override to inner corner size except for leftmost and rightmost corners.
   */
  @VisibleForTesting
  void updateChildShapes() {
    // No need to update shape if no inside corners or outer corners are specified.
    if (innerCornerSize == null && groupStateListShapeAppearance == null) {
      return;
    }
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
              StateListShapeAppearanceModel.CORNER_TOP_LEFT
                  | StateListShapeAppearanceModel.CORNER_BOTTOM_LEFT;
        }
        // When horizontal (ltr), keeps the right two original corners for the last button.
        if (isLastVisible) {
          cornerPositionBitsToKeep |=
              StateListShapeAppearanceModel.CORNER_TOP_RIGHT
                  | StateListShapeAppearanceModel.CORNER_BOTTOM_RIGHT;
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
              StateListShapeAppearanceModel.CORNER_TOP_LEFT
                  | StateListShapeAppearanceModel.CORNER_TOP_RIGHT;
        }
        // When vertical, keeps the bottom two original corners for the last button.
        if (isLastVisible) {
          cornerPositionBitsToKeep |=
              StateListShapeAppearanceModel.CORNER_BOTTOM_LEFT
                  | StateListShapeAppearanceModel.CORNER_BOTTOM_RIGHT;
        }
      }
      // Overrides the corners that don't need to keep with unary operator.
      int cornerPositionBitsToOverride = ~cornerPositionBitsToKeep;
      StateListShapeAppearanceModel newStateListShape =
          originalStateListShapeBuilder
              .setCornerSizeOverride(innerCornerSize, cornerPositionBitsToOverride)
              .build();
      if (newStateListShape.isStateful()) {
        button.setStateListShapeAppearanceModel(newStateListShape);
      } else {
        button.setShapeAppearanceModel(
            newStateListShape.getDefaultShape(/* withCornerSizeOverrides= */ true));
      }
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
    StateListShapeAppearanceModel originalStateList =
        groupStateListShapeAppearance != null && (isFirstVisible || isLastVisible)
            ? groupStateListShapeAppearance
            : originalChildStateListShapeAppearanceModels.get(index);
    // If the state list shape is not specified, creates one from the shape appearance model.
    return originalStateList == null
        ? new StateListShapeAppearanceModel.Builder(originalChildShapeAppearanceModels.get(index))
        : originalStateList.toBuilder();
  }

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
      // Only adjusts margins if both adjacent children are MaterialButtons
      MaterialButton currentButton = getChildButton(i);
      MaterialButton previousButton = getChildButton(i - 1);

      // Calculates the margin adjustment to be the smaller of the two adjacent stroke widths
      int smallestStrokeWidth = 0;
      if (spacing <= 0) {
        smallestStrokeWidth = min(currentButton.getStrokeWidth(), previousButton.getStrokeWidth());
      }

      LayoutParams params = buildLayoutParams(currentButton);
      if (getOrientation() == HORIZONTAL) {
        params.setMarginEnd(0);
        params.setMarginStart(spacing - smallestStrokeWidth);
        params.topMargin = 0;
      } else {
        params.bottomMargin = 0;
        params.topMargin = spacing - smallestStrokeWidth;
        params.setMarginStart(0);
      }

      currentButton.setLayoutParams(params);
    }

    resetChildMargins(firstVisibleChildIndex);
  }

  private void resetChildMargins(int childIndex) {
    if (getChildCount() == 0 || childIndex == -1) {
      return;
    }

    MaterialButton currentButton = getChildButton(childIndex);
    LayoutParams params = (LayoutParams) currentButton.getLayoutParams();
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

  void onButtonWidthChanged(@NonNull MaterialButton button, int increaseSize) {
    int buttonIndex = indexOfChild(button);
    if (buttonIndex < 0) {
      return;
    }
    MaterialButton prevVisibleButton = getPrevVisibleChildButton(buttonIndex);
    MaterialButton nextVisibleButton = getNextVisibleChildButton(buttonIndex);
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
    if (buttonSizeChange == null) {
      return;
    }
    int firstVisibleChildIndex = getFirstVisibleChildIndex();
    int lastVisibleChildIndex = getLastVisibleChildIndex();
    int widthIncreaseOnSingleEdge = Integer.MAX_VALUE;
    for (int i = firstVisibleChildIndex; i <= lastVisibleChildIndex; i++) {
      if (!isChildVisible(i)) {
        continue;
      }
      // Calculates the allowed width increase for each child button with consideration of the max
      // allowed width decrease of its neighbors.
      int widthIncrease = getButtonAllowedWidthIncrease(i);

      // If the button expands on both edges, the width increase on each edge should be half of
      // the total width increase. Calculates the minimum width increase on each edge, so that all
      // buttons won't squeeze their neighbors too much.
      widthIncreaseOnSingleEdge =
          min(
              widthIncreaseOnSingleEdge,
              i != firstVisibleChildIndex && i != lastVisibleChildIndex
                  ? widthIncrease / 2
                  : widthIncrease);
    }
    for (int i = firstVisibleChildIndex; i <= lastVisibleChildIndex; i++) {
      if (!isChildVisible(i)) {
        continue;
      }
      getChildButton(i).setSizeChange(buttonSizeChange);
      // If the button expands on both edges, the total width increase should be double of the
      // width increase on each edge.
      getChildButton(i)
          .setWidthChangeMax(
              i == firstVisibleChildIndex || i == lastVisibleChildIndex
                  ? widthIncreaseOnSingleEdge
                  : widthIncreaseOnSingleEdge * 2);
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
    MaterialButton prevVisibleButton = getPrevVisibleChildButton(index);
    int prevButtonAllowedWidthDecrease =
        prevVisibleButton == null ? 0 : prevVisibleButton.getAllowedWidthDecrease();
    MaterialButton nextVisibleButton = getNextVisibleChildButton(index);
    int nextButtonAllowedWidthDecrease =
        nextVisibleButton == null ? 0 : nextVisibleButton.getAllowedWidthDecrease();
    return min(widthIncrease, prevButtonAllowedWidthDecrease + nextButtonAllowedWidthDecrease);
  }

  // ================ Getters and setters ===================

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
    updateChildShapes();
    invalidate();
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
      return (LayoutParams) layoutParams;
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
    int childCount = getChildCount();
    for (int i = index + 1; i < childCount; i++) {
      if (isChildVisible(i)) {
        return getChildButton(i);
      }
    }
    return null;
  }

  @Nullable
  private MaterialButton getPrevVisibleChildButton(int index) {
    for (int i = index - 1; i >= 0; i--) {
      if (isChildVisible(i)) {
        return getChildButton(i);
      }
    }
    return null;
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
}
