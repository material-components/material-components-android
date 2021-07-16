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
import android.widget.LinearLayout;

import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

public class Visual extends ShapeableImageView implements TotalLinesListener {

  protected Visual(Context context) {
    super(context);
  }

  public Visual(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public Visual(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public static final int NONE = 0;
  public static final int ICON = 1;
  public static final int CIRCLE = 2;
  public static final int SQUARE = 3;
  public static final int RECTANGLE = 4;

  private int type = NONE;

  @NonNull
  public int getType() {
    return type;
  }

  public void setType(int type) {

    this.type = type;
    onTotalLinesChange(DEFAULT);
  }

  private int DEFAULT = -2;
  private int defaultTotalLines = 0;

  @Override
  public void onTotalLinesChange(int totalLines) {

    if (totalLines == DEFAULT) {
      totalLines = defaultTotalLines;
    } else {
      defaultTotalLines = totalLines;
    }

    int gravity = calculateGravity(totalLines);
    int[] bounds = calculateBounds(totalLines);
    int[] layoutMargins = calculateLayoutMargins(totalLines);

    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
    layoutParams.gravity = gravity;
    layoutParams.width = bounds[0];
    layoutParams.height = bounds[1];
    layoutParams.setMargins(layoutMargins[0], layoutMargins[1], layoutMargins[2], layoutMargins[3]);

    setShapeAppearanceModel(calculateShapeAppearanceModel());
  }

  private int calculateGravity(int totalLines) {
    int gravity = Gravity.CENTER_VERTICAL;
    if (type == ICON || totalLines >= 3) {
      gravity = Gravity.TOP;
    }
    return gravity;
  }

  private int[] calculateBounds(int totalLines) {
    int[] bounds = new int[2];

    if (type == RECTANGLE) {
      int rectangleWidth = getDimensionInt(R.dimen.mtrl_list_item_visual_rectangle_width);
      int rectangleHeight = getDimensionInt(R.dimen.mtrl_list_item_visual_rectangle_height);
      bounds[0] = rectangleWidth;
      bounds[1] = rectangleHeight;
    } else {
      int size = 0;
      switch (type) {
        case ICON:
          size = getDimensionInt(R.dimen.mtrl_list_item_visual_icon_size);
          break;
        case CIRCLE:
          size = getDimensionInt(R.dimen.mtrl_list_item_visual_circle_size);
          break;
        case SQUARE:
          if (totalLines == 1) {
            size = getDimensionInt(R.dimen.mtrl_list_item_visual_square_large_size);
          } else {
            size = getDimensionInt(R.dimen.mtrl_list_item_visual_square_size);
          }
      }
      bounds[0] = size;
      bounds[1] = size;
    }
    return bounds;
  }

  private int[] calculateLayoutMargins(int totalLines) {
    int smallLayoutMargin = getDimensionInt(R.dimen.mtrl_list_item_visual_layout_margin_small);
    int normalLayoutMargin = getDimensionInt(R.dimen.mtrl_list_item_visual_layout_margin_normal);
    int largeLayoutMargin = getDimensionInt(R.dimen.mtrl_list_item_visual_layout_margin_large);

    int[] layoutMargins = {
        normalLayoutMargin,
        normalLayoutMargin,
        normalLayoutMargin,
        normalLayoutMargin};

    switch (type) {
      case ICON:
        layoutMargins[2] = largeLayoutMargin;
        break;
      case CIRCLE:
      case SQUARE:
        if (totalLines == 1) {
          layoutMargins[1] = smallLayoutMargin;
          layoutMargins[3] = smallLayoutMargin;
        }
        break;
      case RECTANGLE:
        layoutMargins[0] = 0;
        if (totalLines >= 3) {
          layoutMargins[2] = 20;
        } else {
          layoutMargins[1] = smallLayoutMargin;
          layoutMargins[3] = smallLayoutMargin;
        }
        break;
    }

    if (ViewUtils.isLayoutRtl(this)) {
      int temp = layoutMargins[0];
      layoutMargins[0] = layoutMargins[2];
      layoutMargins[2] = temp;
    }
    return layoutMargins;
  }

  private ShapeAppearanceModel calculateShapeAppearanceModel() {
    float cornerSize = 0;
    if (type == CIRCLE) {
      cornerSize = ViewUtils.dpToPx(getContext(), 20);
    }
    return ShapeAppearanceModel
        .builder()
        .setAllCorners(CornerFamily.ROUNDED, cornerSize)
        .build();
  }

  @Dimension
  private int getDimensionInt(@DimenRes int resource) {

    return (int) getContext().getResources().getDimension(resource);
  }
}
