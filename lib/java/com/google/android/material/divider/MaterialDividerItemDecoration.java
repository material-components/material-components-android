/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.divider;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;

/**
 * MaterialDividerItemDecoration is a {@link RecyclerView.ItemDecoration}, similar to a {@link
 * androidx.recyclerview.widget.DividerItemDecoration}, that can be used as a divider between items of
 * a {@link LinearLayoutManager}. It supports both {@link #HORIZONTAL} and {@link #VERTICAL}
 * orientations.
 *
 * <pre>
 *     dividerItemDecoration = new MaterialDividerItemDecoration(recyclerView.getContext(),
 *             layoutManager.getOrientation());
 *     recyclerView.addItemDecoration(dividerItemDecoration);
 * </pre>
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Divider.md">component
 * developer guidance</a> and <a href="https://material.io/components/divider/overview">design
 * guidelines</a>.
 */
public class MaterialDividerItemDecoration extends ItemDecoration {
  public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
  public static final int VERTICAL = LinearLayout.VERTICAL;

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_MaterialDivider;

  @NonNull private Drawable dividerDrawable;
  private int thickness;
  @ColorInt private int color;
  private int orientation;
  private int insetStart;
  private int insetEnd;
  private boolean lastItemDecorated;

  private final Rect tempRect = new Rect();

  public MaterialDividerItemDecoration(@NonNull Context context, int orientation) {
    this(context, null, orientation);
  }

  public MaterialDividerItemDecoration(
      @NonNull Context context, @Nullable AttributeSet attrs, int orientation) {
    this(context, attrs, R.attr.materialDividerStyle, orientation);
  }

  public MaterialDividerItemDecoration(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int orientation) {
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialDivider, defStyleAttr, DEF_STYLE_RES);

    color =
        MaterialResources.getColorStateList(
                context, attributes, R.styleable.MaterialDivider_dividerColor)
            .getDefaultColor();
    thickness =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialDivider_dividerThickness,
            context.getResources().getDimensionPixelSize(R.dimen.material_divider_thickness));
    insetStart =
        attributes.getDimensionPixelOffset(R.styleable.MaterialDivider_dividerInsetStart, 0);
    insetEnd = attributes.getDimensionPixelOffset(R.styleable.MaterialDivider_dividerInsetEnd, 0);
    lastItemDecorated =
        attributes.getBoolean(R.styleable.MaterialDivider_lastItemDecorated, true);

    attributes.recycle();

    dividerDrawable = new ShapeDrawable();
    setDividerColor(color);
    setOrientation(orientation);
  }

  /**
   * Sets the orientation for this divider. This should be called if {@link
   * RecyclerView.LayoutManager} changes orientation.
   *
   * <p>A {@link #HORIZONTAL} orientation will draw a vertical divider, and a {@link #VERTICAL}
   * orientation a horizontal divider.
   *
   * @param orientation The orientation of the {@link RecyclerView} this divider is associated with:
   *     {@link #HORIZONTAL} or {@link #VERTICAL}
   */
  public void setOrientation(int orientation) {
    if (orientation != HORIZONTAL && orientation != VERTICAL) {
      throw new IllegalArgumentException(
          "Invalid orientation: " + orientation + ". It should be either HORIZONTAL or VERTICAL");
    }
    this.orientation = orientation;
  }

  public int getOrientation() {
    return orientation;
  }

  /**
   * Sets the thickness of the divider.
   *
   * @param thickness The thickness value to be set.
   * @see #getDividerThickness()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerThickness
   */
  public void setDividerThickness(@Px int thickness) {
    this.thickness = thickness;
  }

  /**
   * Sets the thickness of the divider.
   *
   * @param thicknessId The id of the thickness dimension resource to be set.
   * @see #getDividerThickness()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerThickness
   */
  public void setDividerThicknessResource(@NonNull Context context, @DimenRes int thicknessId) {
    setDividerThickness(context.getResources().getDimensionPixelSize(thicknessId));
  }

  /**
   * Returns the thickness set on the divider.
   *
   * @see #setDividerThickness(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerThickness
   */
  @Px
  public int getDividerThickness() {
    return thickness;
  }

  /**
   * Sets the color of the divider.
   *
   * @param color The color to be set.
   * @see #getDividerColor()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerColor
   */
  public void setDividerColor(@ColorInt int color) {
    this.color = color;
    dividerDrawable = DrawableCompat.wrap(dividerDrawable);
    dividerDrawable.setTint(color);
  }

  /**
   * Sets the color of the divider.
   *
   * @param colorId The id of the color resource to be set.
   * @see #getDividerColor()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerColor
   */
  public void setDividerColorResource(@NonNull Context context, @ColorRes int colorId) {
    setDividerColor(ContextCompat.getColor(context, colorId));
  }

  /**
   * Returns the divider color.
   *
   * @see #setDividerColor(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerColor
   */
  @ColorInt
  public int getDividerColor() {
    return color;
  }

  /**
   * Sets the start inset of the divider.
   *
   * @param insetStart The start inset to be set.
   * @see #getDividerInsetStart()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetStart
   */
  public void setDividerInsetStart(@Px int insetStart) {
    this.insetStart = insetStart;
  }

  /**
   * Sets the start inset of the divider.
   *
   * @param insetStartId The id of the inset dimension resource to be set.
   * @see #getDividerInsetStart()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetStart
   */
  public void setDividerInsetStartResource(@NonNull Context context, @DimenRes int insetStartId) {
    setDividerInsetStart(context.getResources().getDimensionPixelOffset(insetStartId));
  }

  /**
   * Returns the divider's start inset.
   *
   * @see #setDividerInsetStart(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetStart
   */
  @Px
  public int getDividerInsetStart() {
    return insetStart;
  }

  /**
   * Sets the end inset of the divider.
   *
   * @param insetEnd The end inset to be set.
   * @see #getDividerInsetEnd()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetEnd
   */
  public void setDividerInsetEnd(@Px int insetEnd) {
    this.insetEnd = insetEnd;
  }

  /**
   * Sets the end inset of the divider.
   *
   * @param insetEndId The id of the inset dimension resource to be set.
   * @see #getDividerInsetEnd()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetEnd
   */
  public void setDividerInsetEndResource(@NonNull Context context, @DimenRes int insetEndId) {
    setDividerInsetEnd(context.getResources().getDimensionPixelOffset(insetEndId));
  }

  /**
   * Returns the divider's end inset.
   *
   * @see #setDividerInsetEnd(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetEnd
   */
  @Px
  public int getDividerInsetEnd() {
    return insetEnd;
  }

  /**
   * Sets whether the class should draw a divider after the last item of a {@link RecyclerView}.
   *
   * @param lastItemDecorated whether there's a divider after the last item of a recycler view.
   * @see #isLastItemDecorated()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_lastItemDecorated
   */
  public void setLastItemDecorated(boolean lastItemDecorated) {
    this.lastItemDecorated = lastItemDecorated;
  }

  /**
   * Whether there's a divider after the last item of a {@link RecyclerView}.
   *
   * @see #setLastItemDecorated(boolean)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_shouldDecorateLastItem
   */
  public boolean isLastItemDecorated() {
    return lastItemDecorated;
  }

  @Override
  public void onDraw(
      @NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
    if (parent.getLayoutManager() == null) {
      return;
    }
    if (orientation == VERTICAL) {
      drawForVerticalOrientation(canvas, parent);
    } else {
      drawForHorizontalOrientation(canvas, parent);
    }
  }

  /**
   * Draws a divider for the vertical orientation of the recycler view. The divider itself will be
   * horizontal.
   */
  private void drawForVerticalOrientation(@NonNull Canvas canvas, @NonNull RecyclerView parent) {
    canvas.save();
    int left;
    int right;
    if (parent.getClipToPadding()) {
      left = parent.getPaddingLeft();
      right = parent.getWidth() - parent.getPaddingRight();
      canvas.clipRect(
          left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
    } else {
      left = 0;
      right = parent.getWidth();
    }
    boolean isRtl = ViewUtils.isLayoutRtl(parent);
    left += isRtl ? insetEnd : insetStart;
    right -= isRtl ? insetStart : insetEnd;

    int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = parent.getChildAt(i);
      if (shouldDrawDivider(parent, child)) {
        parent.getLayoutManager().getDecoratedBoundsWithMargins(child, tempRect);
        // Take into consideration any translationY added to the view.
        int bottom = tempRect.bottom + Math.round(child.getTranslationY());
        int top = bottom - thickness;
        dividerDrawable.setBounds(left, top, right, bottom);
        int alpha = Math.round(child.getAlpha() * 255);
        dividerDrawable.setAlpha(alpha);
        dividerDrawable.draw(canvas);
      }
    }
    canvas.restore();
  }

  /**
   * Draws a divider for the horizontal orientation of the recycler view. The divider itself will be
   * vertical.
   */
  private void drawForHorizontalOrientation(@NonNull Canvas canvas, @NonNull RecyclerView parent) {
    canvas.save();
    int top;
    int bottom;
    if (parent.getClipToPadding()) {
      top = parent.getPaddingTop();
      bottom = parent.getHeight() - parent.getPaddingBottom();
      canvas.clipRect(
          parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
    } else {
      top = 0;
      bottom = parent.getHeight();
    }
    top += insetStart;
    bottom -= insetEnd;

    boolean isRtl = ViewUtils.isLayoutRtl(parent);

    int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = parent.getChildAt(i);
      if (shouldDrawDivider(parent, child)) {
        parent.getLayoutManager().getDecoratedBoundsWithMargins(child, tempRect);
        // Take into consideration any translationX added to the view.
        int translationX = Math.round(child.getTranslationX());
        int left;
        int right;
        if (isRtl) {
          left = tempRect.left + translationX;
          right = left + thickness;
        } else {
          right = tempRect.right + translationX;
          left = right - thickness;
        }
        dividerDrawable.setBounds(left, top, right, bottom);
        int alpha = Math.round(child.getAlpha() * 255);
        dividerDrawable.setAlpha(alpha);
        dividerDrawable.draw(canvas);
      }
    }
    canvas.restore();
  }

  @Override
  public void getItemOffsets(
      @NonNull Rect outRect,
      @NonNull View view,
      @NonNull RecyclerView parent,
      @NonNull RecyclerView.State state) {
    outRect.set(0, 0, 0, 0);
    // Only add offset if there's a divider displayed.
    if (shouldDrawDivider(parent, view)) {
      if (orientation == VERTICAL) {
        outRect.bottom = thickness;
      } else {
        if (ViewUtils.isLayoutRtl(parent)) {
          outRect.left = thickness;
        } else {
          outRect.right = thickness;
        }
      }
    }
  }

  private boolean shouldDrawDivider(@NonNull RecyclerView parent, @NonNull View child) {
    int position = parent.getChildAdapterPosition(child);
    RecyclerView.Adapter<?> adapter = parent.getAdapter();
    boolean isLastItem = adapter != null && position == adapter.getItemCount() - 1;

    return position != RecyclerView.NO_POSITION
        && (!isLastItem || lastItemDecorated)
        && shouldDrawDivider(position, adapter);
  }

  /**
   * Whether a divider should be drawn below the current item that is being drawn.
   *
   * <p>Note: if lasItemDecorated is false, the divider below the last item will never be drawn even
   * if this method returns true.
   *
   * @param position the position of the current item being drawn.
   * @param adapter the {@link RecyclerView.Adapter} associated with the item being drawn.
   */
  protected boolean shouldDrawDivider(int position, @Nullable RecyclerView.Adapter<?> adapter) {
    return true;
  }
}
