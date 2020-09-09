/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.google.android.material.testutils;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.ViewStructure;
import android.view.ViewStructure.HtmlInfo.Builder;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;

/**
 * Simple implementation of {@link ViewStructure} that's easier to use than a Mockito mock.
 *
 * <p>Currently supports only {@code hint}, {@code className}, and child-related methods.
 */
public class ViewStructureImpl extends ViewStructure {

  private CharSequence hint;
  private String className;
  private ViewStructureImpl[] children;
  private AutofillId autofillId;

  @Override
  public void setHint(CharSequence hint) {
    this.hint = hint;
  }

  // Supported methods
  @Override
  public CharSequence getHint() {
    return hint;
  }

  @Override
  public void setChildCount(int num) {
    children = new ViewStructureImpl[num];
  }

  @Override
  public void setClassName(String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public int addChildCount(int num) {
    if (children == null) {
      setChildCount(num);
      return 0;
    }
    final int start = children.length;
    ViewStructureImpl[] newArray = new ViewStructureImpl[start + num];
    System.arraycopy(children, 0, newArray, 0, start);
    children = newArray;
    return start;
  }

  @Override
  public int getChildCount() {
    if (children == null) {
      return 0;
    }
    return children.length;
  }

  public ViewStructureImpl getChildAt(int index) {
    return children[index];
  }

  @Override
  public ViewStructure newChild(int index) {
    final ViewStructureImpl child = new ViewStructureImpl();
    children[index] = child;
    return child;
  }

  @Override
  public ViewStructure asyncNewChild(int index) {
    return newChild(index);
  }

  // Unsupported methods
  @Override
  public void setId(int id, String packageName, String typeName, String entryName) {}

  @Override
  public void setDimens(int left, int top, int scrollX, int scrollY, int width, int height) {}

  @Override
  public void setTransformation(Matrix matrix) {}

  @Override
  public void setElevation(float elevation) {}

  @Override
  public void setAlpha(float alpha) {}

  @Override
  public void setVisibility(int visibility) {}

  public void setAssistBlocked(boolean state) {}

  @Override
  public void setEnabled(boolean state) {}

  @Override
  public void setClickable(boolean state) {}

  @Override
  public void setLongClickable(boolean state) {}

  @Override
  public void setContextClickable(boolean state) {}

  @Override
  public void setFocusable(boolean state) {}

  @Override
  public void setFocused(boolean state) {}

  @Override
  public void setAccessibilityFocused(boolean state) {}

  @Override
  public void setCheckable(boolean state) {}

  @Override
  public void setChecked(boolean state) {}

  @Override
  public void setSelected(boolean state) {}

  @Override
  public void setActivated(boolean state) {}

  @Override
  public void setOpaque(boolean opaque) {}

  @Override
  public void setContentDescription(CharSequence contentDescription) {}

  @Override
  public void setText(CharSequence text) {}

  @Override
  public void setText(CharSequence text, int selectionStart, int selectionEnd) {}

  @Override
  public void setTextStyle(float size, int fgColor, int bgColor, int style) {}

  @Override
  public void setTextLines(int[] charOffsets, int[] baselines) {}

  @Override
  public CharSequence getText() {
    return null;
  }

  @Override
  public int getTextSelectionStart() {
    return 0;
  }

  @Override
  public int getTextSelectionEnd() {
    return 0;
  }

  @Override
  public Bundle getExtras() {
    return null;
  }

  @Override
  public boolean hasExtras() {
    return false;
  }

  @Override
  public AutofillId getAutofillId() {
    return autofillId;
  }

  @Override
  public void setAutofillId(AutofillId id) {
    this.autofillId = id;
  }

  public void setAutofillId(ViewStructure parent, int virtualId) {}

  @Override
  public void setAutofillId(AutofillId parentId, int virtualId) {}

  @Override
  public void setAutofillType(int type) {}

  @Override
  public void setAutofillHints(String[] hint) {}

  @Override
  public void setAutofillValue(AutofillValue value) {}

  @Override
  public void setAutofillOptions(CharSequence[] options) {}

  @Override
  public void setInputType(int inputType) {}

  @Override
  public void setDataIsSensitive(boolean sensitive) {}

  @Override
  public void asyncCommit() {}

  public Rect getTempRect() {
    return null;
  }

  @Override
  public void setWebDomain(String domain) {}

  @Override
  public void setLocaleList(LocaleList localeList) {}

  @Override
  public Builder newHtmlInfoBuilder(String tagName) {
    return null;
  }

  @Override
  public void setHtmlInfo(HtmlInfo htmlInfo) {}
}
