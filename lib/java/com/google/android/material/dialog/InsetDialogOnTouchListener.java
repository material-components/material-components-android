/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.dialog;

import com.google.android.material.R;

import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Px;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Ensures that touches within the transparent region of the inset drawable used for Dialogs
 * are processed as touches outside the Dialog.
 */
class InsetDialogOnTouchListener implements OnTouchListener {

  private final AlertDialog dialog;
  private final int leftInset;
  private final int topInset;

  InsetDialogOnTouchListener(AlertDialog dialog, @Px int leftInset, @Px int topInset) {
    this.dialog = dialog;
    this.leftInset = leftInset;
    this.topInset = topInset;
  }

  @Override
  public boolean onTouch(View view, MotionEvent event) {
    View insetView = view.findViewById(R.id.parentPanel);
    int insetLeft = leftInset + insetView.getLeft();
    int insetRight = insetLeft + insetView.getWidth();
    int insetTop = topInset + insetView.getTop();
    int insetBottom = insetTop + insetView.getHeight();

    RectF dialogWindow = new RectF(insetLeft, insetTop, insetRight, insetBottom);
    if (dialogWindow.contains(event.getX(), event.getY())) {
      return false;
    }
    MotionEvent outsideEvent = MotionEvent.obtain(event);
    outsideEvent.setAction(MotionEvent.ACTION_OUTSIDE);
    // Window.shouldCloseOnTouch does not respect MotionEvent.ACTION_OUTSIDE until Pie
    view.performClick();
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      return dialog.onTouchEvent(outsideEvent);
    } else {
      dialog.onBackPressed();
      return true;
    }
  }
}
