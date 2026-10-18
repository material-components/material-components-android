package com.google.android.material.sidesheet;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class SideSheetDialogFragment extends AppCompatDialogFragment {

  public SideSheetDialogFragment() {
    super();
  }

  public SideSheetDialogFragment(@LayoutRes int contentLayoutId) {
    super(contentLayoutId);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    return new SideSheetDialog(requireContext(), getTheme());
  }

  @Override
  public void dismiss() {
    hideSheetIfNeededThenRun(super::dismiss);
  }

  @Override
  public void dismissAllowingStateLoss() {
    hideSheetIfNeededThenRun(super::dismissAllowingStateLoss);
  }

  void hideSheetIfNeededThenRun(Runnable action) {
    final Dialog dialog = getDialog();
    if (dialog instanceof SideSheetDialog) {
      final SideSheetDialog sheetDialog = (SideSheetDialog) dialog;
      sheetDialog.hideSheetIfNeededThenRun(action);
    } else {
      action.run();
    }
  }
}
