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

package com.google.android.material.textview;

import com.google.android.material.R;

import android.content.Context;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.ListPopupWindow;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filterable;
import android.widget.ListAdapter;
import com.google.android.material.textfield.TextInputLayout;

/**
 * A special sub-class of {@link android.widget.AutoCompleteTextView} that is auto-inflated so that
 * non-editable auto-complete text fields (e.g., for an Exposed Dropdown Menu) are accessible when
 * being interacted through a screen reader.
 *
 * <p>The {@link ListPopupWindow} of the {@link android.widget.AutoCompleteTextView} is not modal,
 * so it does not grab accessibility focus. The {@link MaterialAutoCompleteTextView} changes that
 * by having a modal {@link ListPopupWindow} that is displayed instead of the non-modal one when the
 * {@link MaterialAutoCompleteTextView} is not editable, so that the first item of the popup is
 * automatically focused. This simulates the behavior of the {@link android.widget.Spinner}.
 */
public class MaterialAutoCompleteTextView extends AppCompatAutoCompleteTextView {

  @NonNull private final ListPopupWindow modalListPopup;
  @Nullable private final AccessibilityManager accessibilityManager;

  public MaterialAutoCompleteTextView(@NonNull Context context) {
    this(context, null);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet) {
    this(context, attributeSet, R.attr.autoCompleteTextViewStyle);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
    super(context, attributeSet, defStyleAttr);

    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    modalListPopup = new ListPopupWindow(getContext());
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
  }

  @Override
  public void showDropDown() {
    if (getInputType() == EditorInfo.TYPE_NULL
        && accessibilityManager != null
        && accessibilityManager.isTouchExplorationEnabled()) {
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
