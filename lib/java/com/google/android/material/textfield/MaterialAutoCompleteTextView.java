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

package com.google.android.material.textfield;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.ListPopupWindow;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ManufacturerUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import java.util.List;

/**
 * A special sub-class of {@link android.widget.AutoCompleteTextView} that is auto-inflated so that
 * auto-complete text fields (e.g., for an Exposed Dropdown Menu) are accessible when being
 * interacted through a screen reader.
 *
 * <p>The {@link ListPopupWindow} of the {@link android.widget.AutoCompleteTextView} is not modal,
 * so it does not grab accessibility focus. The {@link MaterialAutoCompleteTextView} changes that by
 * having a modal {@link ListPopupWindow} that is displayed instead of the non-modal one, so that
 * the first item of the popup is automatically focused. This simulates the behavior of the {@link
 * android.widget.Spinner}.
 */
public class MaterialAutoCompleteTextView extends AppCompatAutoCompleteTextView {

  private static final int MAX_ITEMS_MEASURED = 15;
  private static final String SWITCH_ACCESS_ACTIVITY_NAME = "SwitchAccess";

  @NonNull private final ListPopupWindow modalListPopup;
  @Nullable private final AccessibilityManager accessibilityManager;
  @NonNull private final Rect tempRect = new Rect();
  @LayoutRes private final int simpleItemLayout;
  private final float popupElevation;
  @Nullable private ColorStateList dropDownBackgroundTint;
  private int simpleItemSelectedColor;
  @Nullable private ColorStateList simpleItemSelectedRippleColor;

  public MaterialAutoCompleteTextView(@NonNull Context context) {
    this(context, null);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet) {
    this(context, attributeSet, androidx.appcompat.R.attr.autoCompleteTextViewStyle);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
    super(wrap(context, attributeSet, defStyleAttr, 0), attributeSet, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attributeSet,
            R.styleable.MaterialAutoCompleteTextView,
            defStyleAttr,
            androidx.appcompat.R.style.Widget_AppCompat_AutoCompleteTextView);

    // Due to a framework bug, setting android:inputType="none" on xml has no effect. Therefore,
    // we check it here in case the autoCompleteTextView should be non-editable.
    if (attributes.hasValue(R.styleable.MaterialAutoCompleteTextView_android_inputType)) {
      int inputType =
          attributes.getInt(
              R.styleable.MaterialAutoCompleteTextView_android_inputType, InputType.TYPE_NULL);
      if (inputType == InputType.TYPE_NULL) {
        setKeyListener(null);
      }
    }

    simpleItemLayout =
        attributes.getResourceId(
            R.styleable.MaterialAutoCompleteTextView_simpleItemLayout,
            R.layout.mtrl_auto_complete_simple_item);
    popupElevation =
        attributes.getDimensionPixelOffset(
            R.styleable.MaterialAutoCompleteTextView_android_popupElevation,
            R.dimen.mtrl_exposed_dropdown_menu_popup_elevation);

    if (attributes.hasValue(R.styleable.MaterialAutoCompleteTextView_dropDownBackgroundTint)) {
      dropDownBackgroundTint =
          ColorStateList.valueOf(
              attributes.getColor(
                  R.styleable.MaterialAutoCompleteTextView_dropDownBackgroundTint,
                  Color.TRANSPARENT));
    }

    simpleItemSelectedColor =
        attributes.getColor(
            R.styleable.MaterialAutoCompleteTextView_simpleItemSelectedColor, Color.TRANSPARENT);
    simpleItemSelectedRippleColor =
        MaterialResources.getColorStateList(
            context,
            attributes,
            R.styleable.MaterialAutoCompleteTextView_simpleItemSelectedRippleColor);

    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    modalListPopup = new ListPopupWindow(context);
    modalListPopup.setModal(true);
    modalListPopup.setAnchorView(this);
    modalListPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
    modalListPopup.setAdapter(getAdapter());
    modalListPopup.setOnItemClickListener(
        new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View selectedView, int position, long id) {
            Object selectedItem =
                position < 0 ? modalListPopup.getSelectedItem() : getAdapter().getItem(position);

            setText(convertSelectionToString(selectedItem), false);

            OnItemClickListener userOnItemClickListener = getOnItemClickListener();
            if (userOnItemClickListener != null) {
              if (selectedView == null || position < 0) {
                selectedView = modalListPopup.getSelectedView();
                position = modalListPopup.getSelectedItemPosition();
                id = modalListPopup.getSelectedItemId();
              }
              userOnItemClickListener.onItemClick(
                  modalListPopup.getListView(), selectedView, position, id);
            }

            modalListPopup.dismiss();
          }
        });

    if (attributes.hasValue(R.styleable.MaterialAutoCompleteTextView_simpleItems)) {
      setSimpleItems(
          attributes.getResourceId(R.styleable.MaterialAutoCompleteTextView_simpleItems, 0));
    }

    attributes.recycle();
  }

  @Override
  public void showDropDown() {
    if (isPopupRequired()) {
      modalListPopup.show();
    } else {
      super.showDropDown();
    }
  }

  @Override
  public void dismissDropDown() {
    if (isPopupRequired()) {
      modalListPopup.dismiss();
    } else {
      super.dismissDropDown();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    if (shouldShowPopup(keyCode)) {
      TextInputLayout textInputLayout = findTextInputLayoutAncestor();
      if (textInputLayout != null) {
        // A click on the end icon will show the dropdown and animate the icon
        // Note that View.performClick() is a programmatic action that works even if the view is
        // not clickable.
        textInputLayout.getEndIconView().performClick();
      }
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * Determines whether the dropdown should be shown based on the key press.
   *
   * <p>If the view is editable and single-line, the dropdown is shown only for the Enter or D-pad
   * Center keys.
   *
   * <p>If the view is not editable, the dropdown is shown if the user presses the Enter, D-pad
   * Center, or Space keys.
   */
  @VisibleForTesting
  boolean shouldShowPopup(int keyCode) {
    boolean isEnterKey =
        keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
    boolean isSpaceKey = keyCode == KeyEvent.KEYCODE_SPACE;
    boolean isEditable = getKeyListener() != null;
    if (isEditable) {
      return isEnterKey && getMaxLines() == 1;
    } else {
      return isEnterKey || isSpaceKey;
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    if (isPopupRequired()) {
      // Do not dismissDropDown if touch exploration or switch access is enabled, in case the window
      // lost focus in favor of the modalListPopup.
      return;
    }
    super.onWindowFocusChanged(hasWindowFocus);
  }

  private boolean isPopupRequired() {
    return isTouchExplorationEnabled() || isSwitchAccessEnabled();
  }

  private boolean isTouchExplorationEnabled() {
    return accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled();
  }

  private boolean isSwitchAccessEnabled() {
    if (accessibilityManager == null || !accessibilityManager.isEnabled()) {
      return false;
    }
    List<AccessibilityServiceInfo> accessibilityServiceInfos =
        accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC);
    if (accessibilityServiceInfos != null) {
      for (AccessibilityServiceInfo info : accessibilityServiceInfos) {
        if (info.getSettingsActivityName() != null
            && info.getSettingsActivityName().contains(SWITCH_ACCESS_ACTIVITY_NAME)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public <T extends ListAdapter & Filterable> void setAdapter(@Nullable T adapter) {
    super.setAdapter(adapter);
    modalListPopup.setAdapter(getAdapter());
  }

  @Override
  public void setRawInputType(int type) {
    super.setRawInputType(type);
    onInputTypeChanged();
  }

  @Override
  public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
    super.setOnItemSelectedListener(listener);
    modalListPopup.setOnItemSelectedListener(getOnItemSelectedListener());
  }

  /**
   * Sets the simple string items of auto-completion with the given string array resource. This
   * method will create a default {@link ArrayAdapter} with a default item layout specified by
   * {@code R.attr.simpleItemLayout} to display auto-complete items.
   *
   * @see #setSimpleItems(String[])
   * @see #setAdapter(ListAdapter)
   */
  public void setSimpleItems(@ArrayRes int stringArrayResId) {
    setSimpleItems(getResources().getStringArray(stringArrayResId));
  }

  /**
   * Sets the simple string items of auto-completion with the given string array. This method will
   * create a default {@link ArrayAdapter} with a default item layout specified by
   * {@code R.attr.simpleItemLayout} to display auto-complete items.
   *
   * @see #setSimpleItems(int)
   * @see #setAdapter(ListAdapter)
   */
  public void setSimpleItems(@NonNull String[] stringArray) {
    setAdapter(new MaterialArrayAdapter<>(getContext(), simpleItemLayout, stringArray));
  }

  /**
   * Sets the color of the popup dropdown container. It will take effect only if the popup
   * background is a {@link MaterialShapeDrawable}, which is the default when using a Material
   * theme.
   *
   * @param dropDownBackgroundColor the popup dropdown container color
   * @see #setDropDownBackgroundTintList(ColorStateList)
   * @see #getDropDownBackgroundTintList()
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_dropDownBackgroundTint
   */
  public void setDropDownBackgroundTint(@ColorInt int dropDownBackgroundColor) {
    setDropDownBackgroundTintList(ColorStateList.valueOf(dropDownBackgroundColor));
  }

  /**
   * Sets the color of the popup dropdown container. It will take effect only if the popup
   * background is a {@link MaterialShapeDrawable}, which is the default when using a Material
   * theme.
   *
   * @param dropDownBackgroundTint the popup dropdown container tint as a {@link ColorStateList}
   *     object.
   * @see #setDropDownBackgroundTint(int)
   * @see #getDropDownBackgroundTintList()
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_dropDownBackgroundTint
   */
  public void setDropDownBackgroundTintList(@Nullable ColorStateList dropDownBackgroundTint) {
    this.dropDownBackgroundTint = dropDownBackgroundTint;
    Drawable dropDownBackground = getDropDownBackground();
    if (dropDownBackground instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) dropDownBackground).setFillColor(this.dropDownBackgroundTint);
    }
  }

  /**
   * Returns the color of the popup dropdown container.
   *
   * @see #setDropDownBackgroundTint(int)
   * @see #setDropDownBackgroundTintList(ColorStateList)
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_dropDownBackgroundTint
   */
  @Nullable
  public ColorStateList getDropDownBackgroundTintList() {
    return dropDownBackgroundTint;
  }

  /**
   * Sets the color of the default selected popup dropdown item to be used along with
   * {@code R.attr.simpleItemLayout}.
   *
   * @param simpleItemSelectedColor the selected item color
   * @see #getSimpleItemSelectedColor()
   * @see #setSimpleItems(int)
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_simpleItemSelectedColor
   */
  public void setSimpleItemSelectedColor(int simpleItemSelectedColor) {
    this.simpleItemSelectedColor = simpleItemSelectedColor;
    if (getAdapter() instanceof MaterialArrayAdapter) {
      ((MaterialArrayAdapter) getAdapter()).updateSelectedItemColorStateList();
    }
  }

  /**
   * Returns the color of the default selected popup dropdown item.
   *
   * @see #setSimpleItemSelectedColor(int)
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_simpleItemSelectedColor
   */
  public int getSimpleItemSelectedColor() {
    return simpleItemSelectedColor;
  }

  /**
   * Sets the ripple color of the selected popup dropdown item to be used along with
   * {@code R.attr.simpleItemLayout}.
   *
   * @param simpleItemSelectedRippleColor the ripple color state list
   * @see #getSimpleItemSelectedRippleColor()
   * @see #setSimpleItems(int)
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_simpleItemSelectedRippleColor
   */
  public void setSimpleItemSelectedRippleColor(
      @Nullable ColorStateList simpleItemSelectedRippleColor) {
    this.simpleItemSelectedRippleColor = simpleItemSelectedRippleColor;
    if (getAdapter() instanceof MaterialArrayAdapter) {
      ((MaterialArrayAdapter) getAdapter()).updateSelectedItemColorStateList();
    }
  }

  /**
   * Returns the ripple color of the default selected popup dropdown item, or null if not set.
   *
   * @see #setSimpleItemSelectedRippleColor(ColorStateList)
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_simpleItemSelectedRippleColor
   */
  @Nullable
  public ColorStateList getSimpleItemSelectedRippleColor() {
    return simpleItemSelectedRippleColor;
  }

  @Override
  public void setDropDownBackgroundDrawable(Drawable d) {
    super.setDropDownBackgroundDrawable(d);
    if (modalListPopup != null) {
      modalListPopup.setBackgroundDrawable(d);
    }
  }

  /**
   * Returns the elevation of the dropdown popup.
   *
   * @attr ref
   *     com.google.android.material.R.styleable#MaterialAutoCompleteTextView_android_popupElevation
   */
  public float getPopupElevation() {
    return popupElevation;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    // Meizu devices expect TextView#mHintLayout to be non-null if TextView#getHint() is non-null.
    // In order to avoid crashing, we force the creation of the layout by setting an empty non-null
    // hint.
    TextInputLayout layout = findTextInputLayoutAncestor();
    if (layout != null
        && layout.isProvidingHint()
        && super.getHint() == null
        && ManufacturerUtils.isMeizuDevice()) {
      setHint("");
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    modalListPopup.dismiss();
  }

  @Nullable
  @Override
  public CharSequence getHint() {
    // Certain test frameworks expect the actionable element to expose its hint as a label. Retrieve
    // the hint from the TextInputLayout when it's providing it.
    TextInputLayout textInputLayout = findTextInputLayoutAncestor();
    if (textInputLayout != null && textInputLayout.isProvidingHint()) {
      return textInputLayout.getHint();
    }
    return super.getHint();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    // Similar to a Spinner, make sure the view's width is at minimum the width of the largest
    // dropdown item.
    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
      final int measuredWidth = getMeasuredWidth();
      setMeasuredDimension(
          Math.min(
              Math.max(measuredWidth, measureContentWidth()),
              MeasureSpec.getSize(widthMeasureSpec)),
          getMeasuredHeight());
    }
  }

  private int measureContentWidth() {
    ListAdapter adapter = getAdapter();
    TextInputLayout textInputLayout = findTextInputLayoutAncestor();
    if (adapter == null || textInputLayout == null) {
      return 0;
    }

    int width = 0;
    View itemView = null;
    int itemType = 0;
    final int widthMeasureSpec =
        MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
    final int heightMeasureSpec =
        MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

    // Cap the number of items that will be measured.
    int start = Math.max(0, modalListPopup.getSelectedItemPosition());
    final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
    start = Math.max(0, end - MAX_ITEMS_MEASURED);
    for (int i = start; i < end; i++) {
      final int positionType = adapter.getItemViewType(i);
      if (positionType != itemType) {
        itemType = positionType;
        itemView = null;
      }
      itemView = adapter.getView(i, itemView, textInputLayout);
      if (itemView.getLayoutParams() == null) {
        itemView.setLayoutParams(new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT));
      }
      itemView.measure(widthMeasureSpec, heightMeasureSpec);
      width = Math.max(width, itemView.getMeasuredWidth());
    }
    // Add background padding to measured width.
    Drawable background = modalListPopup.getBackground();
    if (background != null) {
      background.getPadding(tempRect);
      width += tempRect.left + tempRect.right;
    }
    // Add icon width to measured width.
    int iconWidth = textInputLayout.getEndIconView().getMeasuredWidth();
    width += iconWidth;

    return width;
  }

  private void onInputTypeChanged() {
    TextInputLayout textInputLayout = findTextInputLayoutAncestor();
    if (textInputLayout != null) {
      textInputLayout.updateEditTextBoxBackgroundIfNeeded();
    }
  }

  @Nullable
  private TextInputLayout findTextInputLayoutAncestor() {
    ViewParent parent = getParent();
    while (parent != null) {
      if (parent instanceof TextInputLayout) {
        return (TextInputLayout) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  /** ArrayAdapter for the {@link MaterialAutoCompleteTextView}. */
  private class MaterialArrayAdapter<T> extends ArrayAdapter<String> {

    @Nullable private ColorStateList selectedItemRippleOverlaidColor;
    @Nullable private ColorStateList pressedRippleColor;

    MaterialArrayAdapter(
        @NonNull Context context, int resource, @NonNull String[] objects) {
      super(context, resource, objects);
      updateSelectedItemColorStateList();
    }

    void updateSelectedItemColorStateList() {
      pressedRippleColor = sanitizeDropdownItemSelectedRippleColor();
      selectedItemRippleOverlaidColor = createItemSelectedColorStateList();
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);

      if (view instanceof TextView) {
        TextView textView = (TextView) view;
        boolean isSelectedItem = getText().toString().contentEquals(textView.getText());
        textView.setBackground(isSelectedItem ? getSelectedItemDrawable() : null);
      }

      return view;
    }

    @Nullable
    private Drawable getSelectedItemDrawable() {
      if (!hasSelectedColor()) {
        return null;
      }

      // The adapter calls getView with the same position multiple times with different views,
      // meaning we can't know which view is actually being used to show the list item. We need to
      // create the drawable on every call, otherwise there can be a race condition causing the
      // background color to not be updated to the right state.
      Drawable colorDrawable = new ColorDrawable(simpleItemSelectedColor);
      if (pressedRippleColor != null) {
        // The ListPopupWindow takes over the states of its list items in order to implement its
        // own ripple. That makes the RippleDrawable not work as expected, i.e. it will respond to
        // pressed states, but not to other states like focused and hovered. To solve that, we
        // create the selectedItemRippleOverlaidColor that will work in those missing states, making
        // the selected list item stateful as expected.
        colorDrawable.setTintList(selectedItemRippleOverlaidColor);
        return new RippleDrawable(pressedRippleColor, colorDrawable, null);
      } else {
        return colorDrawable;
      }
    }

    @Nullable
    private ColorStateList createItemSelectedColorStateList() {
      if (!hasSelectedColor() || !hasSelectedRippleColor()) {
        return null;
      }
      int[] stateHovered = new int[] {android.R.attr.state_hovered, -android.R.attr.state_pressed};
      int[] stateSelected =
          new int[] {android.R.attr.state_selected, -android.R.attr.state_pressed};
      int colorSelected =
          simpleItemSelectedRippleColor.getColorForState(stateSelected, Color.TRANSPARENT);
      int colorHovered =
          simpleItemSelectedRippleColor.getColorForState(stateHovered, Color.TRANSPARENT);
      // Use ripple colors overlaid over selected color.
      int[] colors =
          new int[] {
            MaterialColors.layer(simpleItemSelectedColor, colorSelected),
            MaterialColors.layer(simpleItemSelectedColor, colorHovered),
            simpleItemSelectedColor
          };
      int[][] states = new int[][] {stateSelected, stateHovered, new int[] {}};

      return new ColorStateList(states, colors);
    }

    private ColorStateList sanitizeDropdownItemSelectedRippleColor() {
      if (!hasSelectedRippleColor()) {
        return null;
      }

      // We need to ensure that the ripple drawable we create will show a color only for the pressed
      // state so that the final ripple over the item view will be the correct color.
      int[] statePressed = new int[] {android.R.attr.state_pressed};
      int[] colors =
          new int[] {
              simpleItemSelectedRippleColor.getColorForState(statePressed, Color.TRANSPARENT),
              Color.TRANSPARENT
          };
      int[][] states = new int[][] {statePressed, new int[] {}};
      return new ColorStateList(states, colors);
    }

    private boolean hasSelectedColor() {
      return simpleItemSelectedColor != Color.TRANSPARENT;
    }

    private boolean hasSelectedRippleColor() {
      return simpleItemSelectedRippleColor != null;
    }
  }
}
