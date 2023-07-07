/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.material.bottomsheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.View;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Modal bottom sheet. This is a version of {@link androidx.fragment.app.DialogFragment} that shows
 * a bottom sheet using {@link BottomSheetDialog} instead of a floating dialog.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/BottomSheet.md">component
 * developer guidance</a> and <a
 * href="https://material.io/components/bottom-sheets/overview">design guidelines</a>.
 */
public class BottomSheetDialogFragment extends AppCompatDialogFragment {

  /**
   * Tracks if we are waiting for a dismissAllowingStateLoss or a regular dismiss once the
   * BottomSheet is hidden and onStateChanged() is called.
   */
  private boolean waitingForDismissAllowingStateLoss;

  public BottomSheetDialogFragment() {}

  @SuppressLint("ValidFragment")
  public BottomSheetDialogFragment(@LayoutRes int contentLayoutId) {
    super(contentLayoutId);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    return new BottomSheetDialog(getContext(), getTheme());
  }

  @Override
  public void dismiss() {
    if (!tryDismissWithAnimation(false)) {
      super.dismiss();
    }
  }

  @Override
  public void dismissAllowingStateLoss() {
    if (!tryDismissWithAnimation(true)) {
      super.dismissAllowingStateLoss();
    }
  }

  /**
   * Tries to dismiss the dialog fragment with the bottom sheet animation. Returns true if possible,
   * false otherwise.
   */
  private boolean tryDismissWithAnimation(boolean allowingStateLoss) {
    Dialog baseDialog = getDialog();
    if (baseDialog instanceof BottomSheetDialog) {
      BottomSheetDialog dialog = (BottomSheetDialog) baseDialog;
      BottomSheetBehavior<?> behavior = dialog.getBehavior();
      if (behavior.isHideable() && dialog.getDismissWithAnimation()) {
        dismissWithAnimation(behavior, allowingStateLoss);
        return true;
      }
    }

    return false;
  }

  private void dismissWithAnimation(
      @NonNull BottomSheetBehavior<?> behavior, boolean allowingStateLoss) {
    waitingForDismissAllowingStateLoss = allowingStateLoss;

    if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
      dismissAfterAnimation();
    } else {
      if (getDialog() instanceof BottomSheetDialog) {
        ((BottomSheetDialog) getDialog()).removeDefaultCallback();
      }
      behavior.addBottomSheetCallback(new BottomSheetDismissCallback());
      behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
  }

  private void dismissAfterAnimation() {
    if (waitingForDismissAllowingStateLoss) {
      super.dismissAllowingStateLoss();
    } else {
      super.dismiss();
    }
  }

  private class BottomSheetDismissCallback extends BottomSheetBehavior.BottomSheetCallback {

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
      if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        dismissAfterAnimation();
      }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
  }
}
