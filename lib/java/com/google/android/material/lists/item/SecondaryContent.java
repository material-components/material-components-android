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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.R;

public class SecondaryContent extends FrameLayout implements TotalLinesListener {

  private TextView metadata;
  private FrameLayout action;

  @NonNull
  public TextView getMetadata() {
    return metadata;
  }

  @NonNull
  public FrameLayout getAction() {
    return action;
  }

  public SecondaryContent(@NonNull Context context) {
    this(context, null);
  }

  public SecondaryContent(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SecondaryContent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    metadata = (TextView) layoutInflater.inflate(R.layout.material_list_item_metadata, this, false);
    action = (FrameLayout) layoutInflater.inflate(R.layout.material_list_item_action, this, false);
    addView(metadata);
    addView(action);
  }

  @Override
  public void onTotalLinesChange(int totalLines) {

    int[] layoutMargins = calculateLayoutMargins(totalLines);
    int gravity = calculateGravity(totalLines);

    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
    layoutParams.setMargins(layoutMargins[0], layoutMargins[1], layoutMargins[2], layoutMargins[3]);
    layoutParams.gravity = gravity;
  }

  private int[] calculateLayoutMargins(int totalLines) {
    int layoutMarginSmall = getDimensionInt(R.dimen.mtrl_list_item_secondary_content_layout_margin_small);
    int layoutMarginNormal = getDimensionInt(R.dimen.mtrl_list_item_secondary_content_layout_margin_normal);
    int layoutMarginLarge = getDimensionInt(R.dimen.mtrl_list_item_secondary_content_layout_margin_large);

    int[] layoutMargins = {
        layoutMarginNormal,
        layoutMarginNormal,
        layoutMarginNormal,
        layoutMarginNormal};

    int verticalMargin = layoutMarginNormal;

    if (totalLines <= 1) {
      verticalMargin = layoutMarginSmall;
    } else if (totalLines == 2) {
      verticalMargin = layoutMarginLarge;
    }

    layoutMargins[1] = verticalMargin;
    layoutMargins[3] = verticalMargin;

    return layoutMargins;
  }

  private int calculateGravity(int totalLines) {
    int gravity = Gravity.CENTER_VERTICAL;
    if (totalLines >= 2) {
      gravity = Gravity.TOP;
    }
    return gravity;
  }

  @Dimension
  private int getDimensionInt(@DimenRes int resource) {
    return (int) getContext().getResources().getDimension(resource);
  }
}
