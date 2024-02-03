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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.core.view.SeslTouchTargetDelegate;
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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.reflect.view.inputmethod.SeslInputMethodManagerReflector;

/**
 * <b>SESL variant</b><br><br>
 *
 * @hide */
@RestrictTo(LIBRARY_GROUP)
public class SnackbarContentLayout extends LinearLayout implements ContentViewCallback {
  //Sesl
  private static final boolean WIDGET_ONEUI_SNACKBAR = true;
  private InputMethodManager mImm;
  private SnackbarContentLayout mSnackBarLayout;
  private WindowManager mWindowManager;
  private int mLastOrientation = Configuration.ORIENTATION_UNDEFINED;
  private int mWidthWtihAction;
  private int maxWidth;
  //Sesl

  private TextView messageView;
  private Button actionView;

  private int maxInlineActionWidth;

  public SnackbarContentLayout(@NonNull Context context) {
    this(context, null);
  }

  public SnackbarContentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    //Sesl
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
    maxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
    maxInlineActionWidth =
        a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
    a.recycle();

    final Resources res = context.getResources();

    maxWidth
        = mWidthWtihAction
            = (int) res.getFraction(R.dimen.sesl_config_prefSnackWidth,
                res.getDisplayMetrics().widthPixels, res.getDisplayMetrics().widthPixels);

    mSnackBarLayout = findViewById(R.id.snackbar_layout);
    mImm = context.getSystemService(InputMethodManager.class);
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    seslSetTouchDelegateForSnackBar();
    //sesl
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
  protected void onMeasure(int var1, int var2) {
    /* var1 = widthMeasureSpec; var2 = heightMeasureSpec; */
    super.onMeasure(var1, var2);
    int var3;
    int var4;
    int var5;
    if (this.actionView.getVisibility() != VISIBLE) {
      var3 = var1;
      if (this.maxWidth > 0) {
        var4 = this.getMeasuredWidth();
        var5 = this.maxWidth;
        var3 = var1;
        if (var4 > var5) {
          var3 = MeasureSpec.makeMeasureSpec(var5, MeasureSpec.EXACTLY);
          super.onMeasure(var3, var2);
        }
      }
    } else {
      var3 = MeasureSpec.makeMeasureSpec(this.mWidthWtihAction, MeasureSpec.EXACTLY);
      super.onMeasure(var3, var2);
    }

    Resources var6 = this.getResources();
    int var7 = var6.getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical_2lines);
    int var8 = var6.getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical);
    var1 = this.messageView.getLayout().getLineCount();
    boolean var13 = false;
    boolean var12 = false;
    boolean var11;
    if (var1 > 1) {
      var11 = true;
    } else {
      var11 = false;
    }

    label84: {
      SnackbarContentLayout var9 = this.mSnackBarLayout;
      if (var9 != null) {
        float var10 = (float)(var9.getPaddingLeft() + this.mSnackBarLayout.getPaddingRight() + this.messageView.getMeasuredWidth() + this.actionView.getMeasuredWidth());
        if (this.maxInlineActionWidth == -1 && this.actionView.getVisibility() == VISIBLE) {
          if (!(var10 > (float)this.mWidthWtihAction) && !var11) {
            this.mSnackBarLayout.setOrientation(HORIZONTAL);
            this.actionView.setPadding(var6.getDimensionPixelSize(R.dimen.sesl_design_snackbar_action_padding_left), 0, var6.getDimensionPixelSize(R.dimen.sesl_design_snackbar_action_padding_right), 0);
          } else {
            this.mSnackBarLayout.setOrientation(VERTICAL);
            this.actionView.setPadding(var6.getDimensionPixelSize(R.dimen.sesl_design_snackbar_action_padding_left), var6.getDimensionPixelSize(R.dimen.sesl_design_snackbar_action_padding_top), var6.getDimensionPixelSize(R.dimen.sesl_design_snackbar_action_padding_right), 0);
          }

          var11 = true;
        } else {
          var11 = false;
        }

        var5 = this.mWindowManager.getDefaultDisplay().getRotation();
        if (var5 == 1 || var5 == 3) {
          var12 = true;
        }

        if (this.mImm == null || !var12) {
          break label84;
        }

        MarginLayoutParams var14 = (MarginLayoutParams)this.mSnackBarLayout.getLayoutParams();
        if (SeslInputMethodManagerReflector.isInputMethodShown(this.mImm)) {
          var14.bottomMargin = this.getResources().getDimensionPixelOffset(R.dimen.sesl_design_snackbar_layout_sip_padding_bottom);
        } else {
          var14.bottomMargin = this.getResources().getDimensionPixelOffset(R.dimen.sesl_design_snackbar_layout_padding_bottom);
        }

        this.mSnackBarLayout.setLayoutParams(var14);
      } else if (var11 && this.maxInlineActionWidth > 0 && this.actionView.getMeasuredWidth() > this.maxInlineActionWidth) {
        var11 = var13;
        if (!this.updateViewsWithinLayout(1, var7, var7 - var8)) {
          break label84;
        }
      } else {
        if (var11) {
          var4 = var7;
        } else {
          var4 = var8;
        }

        var11 = var13;
        if (!this.updateViewsWithinLayout(0, var4, var4)) {
          break label84;
        }
      }

      var11 = true;
    }

    if (var11) {
      super.onMeasure(var3, var2);
    }

  }
  // kang

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
    messageView.animate().alpha(1f).setDuration(duration)
        .setStartDelay(delay).start();;//sesl

    if (actionView.getVisibility() == VISIBLE) {
      actionView.setAlpha(0f);
      actionView.animate().alpha(1f).setDuration(duration)
          .setStartDelay(delay).start();;//sesl
    }
  }

  @Override
  public void animateContentOut(int delay, int duration) {
    messageView.setAlpha(1f);
    messageView.animate().alpha(0f).setDuration(duration)
        .setStartDelay(delay).start();;//sesl

    if (actionView.getVisibility() == VISIBLE) {
      actionView.setAlpha(1f);
      actionView.animate().alpha(0f).setDuration(duration)
          .setStartDelay(delay).start();//sesl
    }
  }

  public void setMaxInlineActionWidth(int width) {
    maxInlineActionWidth = width;
  }

  //Sesl
  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (mLastOrientation != newConfig.orientation) {
      final Resources res = getContext().getResources();
      maxWidth
          = mWidthWtihAction
          = (int) res.getFraction(R.dimen.sesl_config_prefSnackWidth,
          res.getDisplayMetrics().widthPixels, res.getDisplayMetrics().widthPixels);
      mLastOrientation = newConfig.orientation;
    }
  }

  private void seslSetTouchDelegateForSnackBar() {
    final ViewTreeObserver vto = getViewTreeObserver();
    if (vto != null) {
      vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
          getViewTreeObserver().removeOnGlobalLayoutListener(this);
          if (mSnackBarLayout != null) {
            if (actionView != null
                    && actionView.getVisibility() == VISIBLE) {
              mSnackBarLayout.post(new Runnable() {
                @Override
                public void run() {
                  SeslTouchTargetDelegate delegate = new SeslTouchTargetDelegate(mSnackBarLayout);
                  final int margin = actionView.getMeasuredHeight() / 2;
                  delegate.addTouchDelegate(actionView,
                          SeslTouchTargetDelegate.ExtraInsets.of(margin, margin, margin, margin));
                  mSnackBarLayout.setTouchDelegate(delegate);
                }
              });
            }
          }
        }
      });
    }
  }
  //sesl
}
