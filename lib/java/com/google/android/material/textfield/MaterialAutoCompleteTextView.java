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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.ListPopupWindow;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filterable;
import android.widget.ListAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ManufacturerUtils;
import com.google.android.material.internal.ThemeEnforcement;

/**
 * A special sub-class of {@link android.widget.AutoCompleteTextView} that is auto-inflated so that
 * auto-complete text fields (e.g., for an Exposed Dropdown Menu) are accessible when being
 * interacted through a screen reader.
 *
 * <p>The {@link ListPopupWindow} of the {@link android.widget.AutoCompleteTextView} is not modal,
 * so it does not grab accessibility focus. The {@link MaterialAutoCompleteTextView} changes that
 * by having a modal {@link ListPopupWindow} that is displayed instead of the non-modal one, so that
 * the first item of the popup is automatically focused. This simulates the behavior of the
 * {@link android.widget.Spinner}.
 */
public class MaterialAutoCompleteTextView extends AppCompatAutoCompleteTextView {

  private static final int MAX_ITEMS_MEASURED = 15;

  @NonNull private final ListPopupWindow modalListPopup;
  @Nullable private final AccessibilityManager accessibilityManager;
  @NonNull private final Rect tempRect = new Rect();

  public MaterialAutoCompleteTextView(@NonNull Context context) {
    this(context, null);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet) {
    this(context, attributeSet, R.attr.autoCompleteTextViewStyle);
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
            R.style.Widget_AppCompat_AutoCompleteTextView);

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

            updateText(selectedItem);

            OnItemClickListener userOnitemClickListener = getOnItemClickListener();
            if (userOnitemClickListener != null) {
              if (selectedView == null || position < 0) {
                selectedView = modalListPopup.getSelectedView();
                position = modalListPopup.getSelectedItemPosition();
                id = modalListPopup.getSelectedItemId();
              }
              userOnitemClickListener.onItemClick(
                  modalListPopup.getListView(), selectedView, position, id);
            }

            modalListPopup.dismiss();
          }
        });

    attributes.recycle();
  }

  @Override
  public void showDropDown() {
    if (accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled()) {
      modalListPopup.show();
    } else {
      super.showDropDown();
    }
  }

  @Override
  public <T extends ListAdapter & Filterable> void setAdapter(@Nullable T adapter) {
    super.setAdapter(adapter);
    modalListPopup.setAdapter(getAdapter());
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

  @SuppressWarnings("unchecked")
  private <T extends ListAdapter & Filterable> void updateText(Object selectedItem) {
    if (VERSION.SDK_INT >= 17) {
      setText(convertSelectionToString(selectedItem), false);
    } else {
      ListAdapter adapter = getAdapter();
      setAdapter(null);
      setText(convertSelectionToString(selectedItem));
      setAdapter((T) adapter);
    }
  }
}
