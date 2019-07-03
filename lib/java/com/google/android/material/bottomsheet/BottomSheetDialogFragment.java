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

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.R;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Modal bottom sheet. This is a version of {@link DialogFragment} that shows a bottom sheet using
 * {@link BottomSheetDialog} instead of a floating dialog.
 */
public class BottomSheetDialogFragment extends AppCompatDialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new BottomSheetDialog(getContext(), getTheme());
  }

  @Override
  public void dismiss() {
    Dialog dialog = getDialog();
    if (dialog != null) {
      View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
      if (bottomSheet != null) {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

        WeakReference<AppCompatDialogFragment> dialogFragmentWeakReference = new WeakReference<>(this);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
              AppCompatDialogFragment dialogFragment = dialogFragmentWeakReference.get();
              if (dialogFragment != null) {
                dialogFragment.dismiss();
              }
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });

        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      } else {
        super.dismiss();
      }
    } else {
      super.dismiss();
    }
  }
}
