/*
 * Copyright (C) 2019 The Android Open Source Project
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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.BoolRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton.OnPressedChangeListener;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.view.MarginLayoutParamsCompat;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A common container for a set of related, toggleable {@link MaterialButton}s. The {@link
 * MaterialButton}s in this group will be shown on a single line.
 *
 * <p>This layout currently only supports child views of type {@link MaterialButton}. Buttons can be
 * added to this view group via XML, as follows:
 *
 * <pre>
 * &lt;com.google.android.material.button.MaterialButtonToggleGroup
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@+id/toggle_button_group"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" /&gt;
 *
 *     &lt;com.google.android.material.button.MaterialButton
 *         style="?attr/materialButtonOutlinedStyle"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_private"/&gt;
 *     &lt;com.google.android.material.button.MaterialButton
 *         style="?attr/materialButtonOutlinedStyle"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_team"/&gt;
 *     &lt;com.google.android.material.button.MaterialButton
 *         style="?attr/materialButtonOutlinedStyle"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_everyone"/&gt;
 *     &lt;com.google.android.material.button.MaterialButton
 *         style="?attr/materialButtonOutlinedStyle"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/button_label_custom"/&gt;
 *
 * &lt;com.google.android.material.button.MaterialButtonToggleGroup /&gt;
 * </pre>
 *
 * <p>Buttons can also be added to this view group programmatically via the {@link #addView(View)}
 * methods.
 *
 * <p>Note: Styling must applied to each child button individually. It is recommended to use the
 * {@link R.attr#materialButtonOutlinedStyle} attribute for all child buttons. {@link
 * R.attr#materialButtonOutlinedStyle} will most closely match the Material Design guidelines for
 * this component, and supports the checked state for child buttons.
 *
 * <p>Any {@link MaterialButton}s added to this view group are automatically marked as {@code
 * checkable}, and by default multiple buttons within the same group can be checked. To enforce that
 * only one button can be checked at a time, set the {@link R.attr#singleSelection
 * app:singleSelection} attribute to {@code true} on the MaterialButtonToggleGroup or call {@link
 * #setSingleSelection(boolean) setSingleSelection(true)}.
 *
 * <p>MaterialButtonToggleGroup is a {@link RelativeLayout}, and positions children to be aligned to
 * the end of the previous child in the order they are added.
 *
 * <p>In order to cohesively group multiple buttons together, MaterialButtonToggleGroup overrides
 * the start and end margins of any children added to this layout such that child buttons are placed
 * directly adjacent to one another.
 *
 * <p>MaterialButtonToggleGroup also overrides any {@code shapeAppearance}, {@code
 * shapeAppearanceOverlay}, or {@code cornerRadius} attribute set on MaterialButton children such
 * that only the left-most corners of the first child and the right-most corners of the last child
 * retain their shape appearance or corner size.
 */
public class MaterialButtonToggleGroup extends RelativeLayout {
  /**
   * Interface definition for a callback to be invoked when a {@link MaterialButton} is checked or
   * unchecked in this group.
   */
  public interface OnButtonCheckedListener {
    /**
     * Called when a {@link MaterialButton} in this group is checked or unchecked.
     *
     * @param group The group in which the MaterialButton's checked state was changed
     * @param checkedId The unique identifier of the MaterialButton whose check state changed
     * @param isChecked Whether the MaterialButton is currently checked
     */
    void onButtonChecked(MaterialButtonToggleGroup group, @IdRes int checkedId, boolean isChecked);
  }

  private static final String LOG_TAG = MaterialButtonToggleGroup.class.getSimpleName();

  private final ArrayList<MaterialButton> childrenInOrder = new ArrayList<>();
  private final ArrayList<CornerData> originalCornerData = new ArrayList<>();

  private final CheckedStateTracker checkedStateTracker = new CheckedStateTracker();
  private final PressedStateTracker pressedStateTracker = new PressedStateTracker();
  private final LinkedHashSet<OnButtonCheckedListener> onButtonCheckedListeners =
      new LinkedHashSet<>();

  private boolean skipCheckedStateTracker = false;
  private boolean singleSelection;
  @IdRes private int checkedId;

  public MaterialButtonToggleGroup(Context context) {
    this(context, null);
  }

  public MaterialButtonToggleGroup(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonToggleGroupStyle);
  }

  public MaterialButtonToggleGroup(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.MaterialButtonToggleGroup,
            defStyleAttr,
            R.style.Widget_MaterialComponents_MaterialButtonToggleGroup);

    setSingleSelection(
        attributes.getBoolean(R.styleable.MaterialButtonToggleGroup_singleSelection, false));
    checkedId =
        attributes.getResourceId(R.styleable.MaterialButtonToggleGroup_checkedButton, View.NO_ID);

    attributes.recycle();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    // Checks the appropriate button as requested via XML
    if (checkedId != View.NO_ID) {
      checkForced(checkedId);
    }
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (!(child instanceof MaterialButton)) {
      Log.e(LOG_TAG, "Child views must be of type MaterialButton.");
      return;
    }

    final MaterialButton buttonChild = (MaterialButton) child;
    setGeneratedIdIfNeeded(buttonChild);

    // If index < 0, adds to the end of the layout
    int indexToAdd = index >= 0 ? index : getChildCount();

    super.addView(buttonChild, index, new RelativeLayout.LayoutParams(params.width, params.height));

    // Saves child data locally for visual ordering
    childrenInOrder.add(indexToAdd, buttonChild);

    // Sets sensible default values and an internal checked change listener for this child
    setupButtonChild(buttonChild);

    // Reorders children if a checked child was added to this layout
    if (buttonChild.isChecked()) {
      updateCheckedStates(buttonChild.getId(), true);
      setCheckedId(buttonChild.getId());
    }

    // Saves original corner data
    ShapeAppearanceModel shapeAppearanceModel = buttonChild.getShapeAppearanceModel();
    originalCornerData.add(
        new CornerData(
            shapeAppearanceModel.getTopLeftCorner().getCornerSize(),
            shapeAppearanceModel.getTopRightCorner().getCornerSize(),
            shapeAppearanceModel.getBottomRightCorner().getCornerSize(),
            shapeAppearanceModel.getBottomLeftCorner().getCornerSize()));
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);

    if (child instanceof MaterialButton) {
      ((MaterialButton) child).removeOnCheckedChangeListener(checkedStateTracker);
      ((MaterialButton) child).setOnPressedChangeListenerInternal(null);
    }

    int indexOfChild = childrenInOrder.indexOf(child);
    if (indexOfChild >= 0) {
      childrenInOrder.remove(child);
      originalCornerData.remove(indexOfChild);
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
  public CharSequence getAccessibilityClassName() {
    return MaterialButtonToggleGroup.class.getName();
  }

  /**
   * Sets the {@link MaterialButton} whose id is passed in to the checked state. If this
   * MaterialButtonToggleGroup is in {@link #isSingleSelection() single selection mode}, then all
   * other MaterialButtons in this group will be unchecked. Otherwise, other MaterialButtons will
   * retain their checked state.
   *
   * @param id View ID of {@link MaterialButton} to set checked
   * @see #uncheck(int)
   * @see #clearChecked()
   * @see #getCheckedButtonIds()
   * @see #getCheckedButtonId()
   */
  public void check(@IdRes int id) {
    if (id == checkedId) {
      return;
    }

    checkForced(id);
  }

  /**
   * Sets the {@link MaterialButton} whose id is passed in to the unchecked state.
   *
   * @param id View ID of {@link MaterialButton} to set unchecked
   * @see #check(int)
   * @see #clearChecked()
   * @see #getCheckedButtonIds()
   * @see #getCheckedButtonId()
   */
  public void uncheck(@IdRes int id) {
    setCheckedStateForView(id, false);
    updateCheckedStates(id, false);
    this.checkedId = View.NO_ID;
    dispatchOnButtonChecked(id, false);
  }

  /**
   * Clears the selections. When the selections are cleared, no {@link MaterialButton} in this group
   * is checked and {@link #getCheckedButtonIds()} returns an empty list.
   *
   * @see #check(int)
   * @see #uncheck(int)
   * @see #getCheckedButtonIds()
   * @see #getCheckedButtonId()
   */
  public void clearChecked() {
    skipCheckedStateTracker = true;
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton child = childrenInOrder.get(i);
      child.setChecked(false);

      dispatchOnButtonChecked(child.getId(), false);
    }
    skipCheckedStateTracker = false;

    setCheckedId(View.NO_ID);
  }

  /**
   * When in {@link #isSingleSelection() single selection mode}, returns the identifier of the
   * selected button in this group. Upon empty selection, the returned value is {@link View#NO_ID}.
   * If not in single selection mode, the return value is {@link View#NO_ID}.
   *
   * @return The unique id of the selected {@link MaterialButton} in this group in {@link
   *     #isSingleSelection() single selection mode}. When not in {@link #isSingleSelection() single
   *     selection mode}, returns {@link View#NO_ID}.
   * @see #check(int)
   * @see #uncheck(int)
   * @see #clearChecked()
   * @see #getCheckedButtonIds()
   * @attr ref R.styleable#MaterialButtonToggleGroup_checkedButton
   */
  @IdRes
  public int getCheckedButtonId() {
    return singleSelection ? checkedId : View.NO_ID;
  }

  /**
   * Returns the identifiers of the selected {@link MaterialButton}s in this group. Upon empty
   * selection, the returned value is an empty list.
   *
   * @return The unique IDs of the selected {@link MaterialButton}s in this group. When in {@link
   *     #isSingleSelection() single selection mode}, returns a list with a single ID. When no
   *     {@link MaterialButton}s are selected, returns an empty list.
   * @see #check(int)
   * @see #uncheck(int)
   * @see #clearChecked()
   * @see #getCheckedButtonId()
   */
  public List<Integer> getCheckedButtonIds() {
    ArrayList<Integer> checkedIds = new ArrayList<>();
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton child = childrenInOrder.get(i);
      if (child.isChecked()) {
        checkedIds.add(child.getId());
      }
    }

    return checkedIds;
  }

  /**
   * Add a listener that will be invoked when the check state of a {@link MaterialButton} in this
   * group changes. See {@link OnButtonCheckedListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnButtonCheckedListener(OnButtonCheckedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnButtonCheckedListener(@NonNull OnButtonCheckedListener listener) {
    onButtonCheckedListeners.add(listener);
  }

  /**
   * Remove a listener that was previously added via {@link
   * #addOnButtonCheckedListener(OnButtonCheckedListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnButtonCheckedListener(@NonNull OnButtonCheckedListener listener) {
    onButtonCheckedListeners.remove(listener);
  }

  /** Remove all previously added {@link OnButtonCheckedListener}s. */
  public void clearOnButtonCheckedListeners() {
    onButtonCheckedListeners.clear();
  }

  /**
   * Returns whether this group only allows a single button to be checked.
   *
   * @return whether this group only allows a single button to be checked
   * @attr ref R.styleable#MaterialButtonToggleGroup_singleSelection
   */
  public boolean isSingleSelection() {
    return singleSelection;
  }

  /**
   * Sets whether this group only allows a single button to be checked.
   *
   * <p>Calling this method results in all the buttons in this group to become unchecked.
   *
   * @param singleSelection whether this group only allows a single button to be checked
   * @attr ref R.styleable#MaterialButtonToggleGroup_singleSelection
   */
  public void setSingleSelection(boolean singleSelection) {
    if (this.singleSelection != singleSelection) {
      this.singleSelection = singleSelection;

      clearChecked();
    }
  }

  /**
   * Sets whether this group only allows a single button to be checked.
   *
   * <p>Calling this method results in all the buttons in this group to become unchecked.
   *
   * @param id boolean resource ID of whether this group only allows a single button to be checked
   * @attr ref R.styleable#MaterialButtonToggleGroup_singleSelection
   */
  public void setSingleSelection(@BoolRes int id) {
    setSingleSelection(getResources().getBoolean(id));
  }

  private void setCheckedStateForView(@IdRes int viewId, boolean checked) {
    View checkedView = findViewById(viewId);
    if (checkedView instanceof MaterialButton) {
      skipCheckedStateTracker = true;
      ((MaterialButton) checkedView).setChecked(checked);
      skipCheckedStateTracker = false;
    }
  }

  private void setCheckedId(int checkedId) {
    this.checkedId = checkedId;

    dispatchOnButtonChecked(checkedId, true);
  }

  /**
   * Sets a negative marginStart on all but the first child, if two adjacent children both have a
   * stroke width greater than 0. This prevents a double-width stroke from being drawn for two
   * adjacent stroked children, and instead draws the adjacent strokes directly on top of each
   * another.
   *
   * <p>The negative margin adjustment amount will be equal to the smaller of the two adjacent
   * stroke widths.
   *
   * <p>Also rearranges children such that they are shown in the correct visual order.
   */
  private void adjustChildMarginsAndUpdateLayout() {
    for (int i = 1; i < getChildCount(); i++) {
      // Only adjusts margins if both adjacent children are MaterialButtons
      MaterialButton currentButton = childrenInOrder.get(i);
      MaterialButton previousButton = childrenInOrder.get(i - 1);

      // Calculates the margin adjustment to be the smaller of the two adjacent stroke widths
      int smallestStrokeWidth =
          Math.min(currentButton.getStrokeWidth(), previousButton.getStrokeWidth());

      LayoutParams params = buildEndAlignLayoutParams(previousButton, currentButton);

      MarginLayoutParamsCompat.setMarginEnd(params, 0);
      if (MarginLayoutParamsCompat.getMarginStart(params) != -1 * smallestStrokeWidth) {
        MarginLayoutParamsCompat.setMarginStart(params, -1 * smallestStrokeWidth);
      }

      currentButton.setLayoutParams(params);
    }

    resetFirstChildMargin();
  }

  private void resetFirstChildMargin() {
    if (!childrenInOrder.isEmpty()) {
      MaterialButton currentButton = childrenInOrder.get(0);
      LayoutParams params = (LayoutParams) currentButton.getLayoutParams();
      MarginLayoutParamsCompat.setMarginEnd(params, 0);
      MarginLayoutParamsCompat.setMarginStart(params, 0);
      currentButton.setLayoutParams(params);
    }
  }

  /** Sets all corner radii to 0 except for leftmost and rightmost corners. */
  private void updateChildShapes() {
    int numChildren = getChildCount();
    if (numChildren >= 1) {
      for (int i = 0; i < numChildren; i++) {
        MaterialButton button = childrenInOrder.get(i);
        if (button.getShapeAppearanceModel() != null) {
          ShapeAppearanceModel shapeAppearanceModel = button.getShapeAppearanceModel();
          CornerData cornerData = originalCornerData.get(i);
          if (numChildren == 1) {
            // If there is only one child, sets its original corners
            shapeAppearanceModel.setCornerRadii(
                cornerData.topLeft,
                cornerData.topRight,
                cornerData.bottomRight,
                cornerData.bottomLeft);
          } else {
            if (i == (ViewUtils.isLayoutRtl(this) ? (numChildren - 1) : 0)) {
              // Keeps the left corners of the first child in LTR, or the last child in RTL
              shapeAppearanceModel.setCornerRadii(cornerData.topLeft, 0, 0, cornerData.bottomLeft);
            } else if (i != 0 && i < numChildren - 1) {
              // Sets corner radii of all middle children to 0
              shapeAppearanceModel.setCornerRadius(0);
            } else if (i == (ViewUtils.isLayoutRtl(this) ? 0 : (numChildren - 1))) {
              // Keeps the right corners of the last child in LTR, or the first child in RTL
              shapeAppearanceModel.setCornerRadii(
                  0, cornerData.topRight, cornerData.bottomRight, 0);
            }
          }
          button.setShapeAppearanceModel(shapeAppearanceModel);
        }
      }
    }
  }

  /**
   * When a checked child is added, or a child is clicked, updates checked state and draw order of
   * children to draw all checked children on top of all unchecked children.
   *
   * <p>If {@code singleSelection} is true, this will unselect any other children as well.
   *
   * @param childId ID of child whose checked state may have changed
   * @param childIsChecked Whether the child is checked
   */
  private void updateCheckedStates(int childId, boolean childIsChecked) {
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton button = childrenInOrder.get(i);
      if (button.isChecked()) {
        if (singleSelection && childIsChecked && button.getId() != childId) {
          setCheckedStateForView(button.getId(), false);

          dispatchOnButtonChecked(button.getId(), false);
        } else {
          button.bringToFront();
        }
      }
    }
  }

  private void dispatchOnButtonChecked(@IdRes int buttonId, boolean checked) {
    for (OnButtonCheckedListener listener : onButtonCheckedListeners) {
      listener.onButtonChecked(this, buttonId, checked);
    }
  }

  private void checkForced(int checkedId) {
    setCheckedStateForView(checkedId, true);
    updateCheckedStates(checkedId, true);
    setCheckedId(checkedId);
  }

  private void setGeneratedIdIfNeeded(MaterialButton materialButton) {
    int id = materialButton.getId();

    // Generates an ID if none is set, for relative positioning purposes
    if (id == View.NO_ID) {
      if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
        id = View.generateViewId();
      } else {
        id = materialButton.hashCode();
      }
      materialButton.setId(id);
    }
  }

  /**
   * Sets sensible default values for {@link MaterialButton} child of this group, set child to
   * {@code checkable}, and set internal checked change listener for this child.
   *
   * @param buttonChild {@link MaterialButton} child to set up to be added to this {@link
   *     MaterialButtonToggleGroup}
   */
  private void setupButtonChild(MaterialButton buttonChild) {
    buttonChild.setMaxLines(1);
    buttonChild.setEllipsize(TruncateAt.END);
    buttonChild.setCheckable(true);

    buttonChild.addOnCheckedChangeListener(checkedStateTracker);
    buttonChild.setOnPressedChangeListenerInternal(pressedStateTracker);

    // Enables surface layer drawing for semi-opaque strokes
    buttonChild.setShouldDrawSurfaceColorStroke(true);
  }

  private RelativeLayout.LayoutParams buildEndAlignLayoutParams(
      @Nullable View startChild, View endChild) {
    ViewGroup.LayoutParams layoutParams = endChild.getLayoutParams();
    RelativeLayout.LayoutParams endAlignedLayoutParams =
        new RelativeLayout.LayoutParams(layoutParams.width, layoutParams.height);

    if (startChild == null) {
      return endAlignedLayoutParams;
    }

    endAlignedLayoutParams.addRule(
        ViewUtils.isLayoutRtl(this) ? LEFT_OF : RIGHT_OF, startChild.getId());
    return endAlignedLayoutParams;
  }

  private class CheckedStateTracker implements MaterialButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(MaterialButton button, boolean isChecked) {
      // Prevents infinite recursion
      if (skipCheckedStateTracker) {
        return;
      }

      if (singleSelection) {
        checkedId = isChecked ? button.getId() : View.NO_ID;
      }

      dispatchOnButtonChecked(button.getId(), isChecked);

      updateCheckedStates(button.getId(), isChecked);
    }
  }

  private class PressedStateTracker implements OnPressedChangeListener {
    @Override
    public void onPressedChanged(MaterialButton button, boolean isPressed) {
      if (isPressed) {
        button.bringToFront();
      } else {
        updateCheckedStates(button.getId(), button.isChecked());
      }
    }
  }

  private static class CornerData {
    final float topLeft;
    final float topRight;
    final float bottomRight;
    final float bottomLeft;

    CornerData(float topLeft, float topRight, float bottomRight, float bottomLeft) {
      this.topLeft = topLeft;
      this.topRight = topRight;
      this.bottomRight = bottomRight;
      this.bottomLeft = bottomLeft;
    }
  }
}
