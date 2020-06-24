/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.google.android.material.snackbar;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.color.MaterialColors;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class SnackbarContentLayout extends LinearLayout implements ContentViewCallback {
  private TextView messageView;
  private Button actionView;

  private int maxWidth;
  private int maxInlineActionWidth;

  public SnackbarContentLayout(@NonNull Context context) {
    this(context, null);
  }

  public SnackbarContentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
    maxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
    maxInlineActionWidth =
        a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
    a.recycle();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    messageView = findViewById(R.id.snackbar_text);
    actionView = findViewById(R.id.snackbar_action);
  }

  public TextView getMessageView() {
    return messageView;
  }

  public Button getActionView() {
    return actionView;
  }

  void updateActionTextColorAlphaIfNeeded(float actionTextColorAlpha) {
    if (actionTextColorAlpha != 1) {
      int originalActionTextColor = actionView.getCurrentTextColor();
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int actionTextColor =
          MaterialColors.layer(colorSurface, originalActionTextColor, actionTextColorAlpha);
      actionView.setTextColor(actionTextColor);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (maxWidth > 0 && getMeasuredWidth() > maxWidth) {
      widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY);
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    final int multiLineVPadding =
        getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical_2lines);
    final int singleLineVPadding =
        getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical);
    final boolean isMultiLine = messageView.getLayout().getLineCount() > 1;

    boolean remeasure = false;
    if (isMultiLine
        && maxInlineActionWidth > 0
        && actionView.getMeasuredWidth() > maxInlineActionWidth) {
      if (updateViewsWithinLayout(
          VERTICAL, multiLineVPadding, multiLineVPadding - singleLineVPadding)) {
        remeasure = true;
      }
    } else {
      final int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
      if (updateViewsWithinLayout(HORIZONTAL, messagePadding, messagePadding)) {
        remeasure = true;
      }
    }

    if (remeasure) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  private boolean updateViewsWithinLayout(
      final int orientation, final int messagePadTop, final int messagePadBottom) {
    boolean changed = false;
    if (orientation != getOrientation()) {
      setOrientation(orientation);
      changed = true;
    }
    if (messageView.getPaddingTop() != messagePadTop
        || messageView.getPaddingBottom() != messagePadBottom) {
      updateTopBottomPadding(messageView, messagePadTop, messagePadBottom);
      changed = true;
    }
    return changed;
  }

  private static void updateTopBottomPadding(
      @NonNull View view, int topPadding, int bottomPadding) {
    if (ViewCompat.isPaddingRelative(view)) {
      ViewCompat.setPaddingRelative(
          view,
          ViewCompat.getPaddingStart(view),
          topPadding,
          ViewCompat.getPaddingEnd(view),
          bottomPadding);
    } else {
      view.setPadding(view.getPaddingLeft(), topPadding, view.getPaddingRight(), bottomPadding);
    }
  }

  @Override
  public void animateContentIn(int delay, int duration) {
    messageView.setAlpha(0f);
    messageView.animate().alpha(1f).setDuration(duration).setStartDelay(delay).start();

    if (actionView.getVisibility() == VISIBLE) {
      actionView.setAlpha(0f);
      actionView.animate().alpha(1f).setDuration(duration).setStartDelay(delay).start();
    }
  }

  @Override
  public void animateContentOut(int delay, int duration) {
    messageView.setAlpha(1f);
    messageView.animate().alpha(0f).setDuration(duration).setStartDelay(delay).start();

    if (actionView.getVisibility() == VISIBLE) {
      actionView.setAlpha(1f);
      actionView.animate().alpha(0f).setDuration(duration).setStartDelay(delay).start();
    }
  }

  public void setMaxInlineActionWidth(int width) {
    maxInlineActionWidth = width;
  }
}
