/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.lists.item;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.R;
import com.google.android.material.lists.item.adaptive.AdaptableLinearLayout;
import com.google.android.material.lists.item.adaptive.AdaptableTextView;

class TextCollection extends AdaptableLinearLayout implements TotalLinesListener {

  private AdaptableTextView overlineText;
  private AdaptableTextView primaryText;
  private AdaptableTextView secondaryText;

  @NonNull
  public TextView getOverlineText() {
    return overlineText;
  }

  @NonNull
  public TextView getPrimaryText() {
    return primaryText;
  }

  @NonNull
  public TextView getSecondaryText() {
    return secondaryText;
  }


  public TextCollection(Context context) {
    this(context, null);
  }

  public TextCollection(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TextCollection(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOrientation(VERTICAL);

    LayoutInflater layoutInflater = LayoutInflater.from(getContext());

    overlineText = (AdaptableTextView) layoutInflater.inflate(R.layout.material_list_item_overline_text, this, false);
    primaryText = (AdaptableTextView) layoutInflater.inflate(R.layout.material_list_item_primary_text, this, false);
    secondaryText = (AdaptableTextView) layoutInflater.inflate(R.layout.material_list_item_secondary_text, this, false);

    addView(overlineText);
    addView(primaryText);
    addView(secondaryText);

    overlineText.addTextChangedListener(singleLineWatcher);
    primaryText.addTextChangedListener(singleLineWatcher);
    secondaryText.addTextChangedListener(multiLineWatcher);
  }

  TextWatcher singleLineWatcher = new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      updateTotalLines();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
  };

  TextWatcher multiLineWatcher = new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (secondaryText.getVisibility() == View.VISIBLE) {
        secondaryText.getViewTreeObserver().addOnPreDrawListener(totalLinePreDrawListener);
      }
      updateTotalLines();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
  };

  int secondaryLines = 0;
  ViewTreeObserver.OnPreDrawListener totalLinePreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
    @Override
    public boolean onPreDraw() {

      secondaryLines = secondaryText.getLineCount();
      if (secondaryLines > 0) {
        secondaryText.getViewTreeObserver().removeOnPreDrawListener(this);
        updateTotalLines();
      }
      return true;
    }
  };

  private void updateTotalLines() {

    int overlineLines = overlineText.getText().length() > 0 ? 1 : 0;
    int primaryLines = primaryText.getText().length() > 0 ? 1 : 0;
    int secondaryLines = secondaryText.getVisibility() == GONE ? 0 : this.secondaryLines;
    int totalLines = overlineLines + primaryLines + secondaryLines;

    if (totalLinesListener != null) {
      totalLinesListener.onTotalLinesChange(totalLines);
    }
  }

  TotalLinesListener totalLinesListener = null;

  public void setTotalLinesListener(TotalLinesListener totalLinesListener) {
    this.totalLinesListener = totalLinesListener;
  }

  @Override
  public void onTotalLinesChange(int totalLines) {

    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
    int[] layoutMargins = calculateLayoutMargins(totalLines);
    layoutParams.setMargins(layoutMargins[0], layoutMargins[1], layoutMargins[2], layoutMargins[3]);
  }

  private int[] calculateLayoutMargins(int totalLines) {
    int layoutMarginSmall = getDimensionInt(R.dimen.mtrl_list_item_text_collection_layout_margin_small);
    int layoutMarginNormal = getDimensionInt(R.dimen.mtrl_list_item_text_collection_layout_margin_normal);

    int[] layoutMargins = {
        layoutMarginNormal,
        layoutMarginNormal,
        layoutMarginNormal,
        layoutMarginNormal};

    if (totalLines <= 1) {
      layoutMargins[1] = layoutMarginSmall;
      layoutMargins[3] = layoutMarginSmall;
    }

    return layoutMargins;
  }

  @Dimension
  private int getDimensionInt(@DimenRes int resource) {
    return (int) getContext().getResources().getDimension(resource);
  }
}
