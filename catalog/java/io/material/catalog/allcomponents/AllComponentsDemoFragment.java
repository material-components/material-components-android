/*
 * Copyright 2026 The Android Open Source Project
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

package io.material.catalog.allcomponents;

import io.material.catalog.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import io.material.catalog.feature.DemoFragment;

/** A demo fragment which shows all components. */
public class AllComponentsDemoFragment extends DemoFragment {

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_all_components_fragment, viewGroup, false);

    ((Toolbar) view.findViewById(R.id.bottom_app_bar))
        .setNavigationOnClickListener(
            v -> ((DrawerLayout) view.findViewById(R.id.drawer_layout)).open());

    view.findViewById(R.id.bottom_navigation).setOnApplyWindowInsetsListener(null);

    setUpSplitButton(view);
    setUpDialogs(view);
    setUpSheets(view);

    return view;
  }

  private void setUpDialogs(View view) {
    view.findViewById(R.id.alert_dialog_button)
        .setOnClickListener(
            v ->
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Message")
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show());

    view.findViewById(R.id.date_picker_button)
        .setOnClickListener(
            v -> {
              MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
              datePicker.addOnPositiveButtonClickListener(
                  selection -> showSnackbar("Date Result: " + datePicker.getHeaderText()));
              datePicker.show(getChildFragmentManager(), "DATE_PICKER");
            });

    view.findViewById(R.id.date_range_picker_button)
        .setOnClickListener(
            v -> {
              MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                  MaterialDatePicker.Builder.dateRangePicker().build();
              dateRangePicker.addOnPositiveButtonClickListener(
                  selection ->
                      showSnackbar("Date Range Result: " + dateRangePicker.getHeaderText()));
              dateRangePicker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
            });

    view.findViewById(R.id.time_picker_button)
        .setOnClickListener(
            v -> {
              MaterialTimePicker timePicker =
                  new MaterialTimePicker.Builder()
                      .setTimeFormat(TimeFormat.CLOCK_12H)
                      .setHour(8)
                      .setMinute(12)
                      .build();
              timePicker.show(getChildFragmentManager(), "TIME_PICKER");
            });
  }

  private void setUpSheets(View view) {
    view.findViewById(R.id.bottom_sheet_button).setOnClickListener(v -> showBottomSheet());
    view.findViewById(R.id.modal_side_sheet_button).setOnClickListener(v -> showSideSheet());
  }

  private void showBottomSheet() {
    NavigationView navigationView = new NavigationView(requireContext());
    navigationView.inflateMenu(R.menu.cat_all_components_menu);
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
    bottomSheetDialog.setContentView(navigationView);
    bottomSheetDialog.show();
  }

  private void showSideSheet() {
    SideSheetDialog sideSheetDialog = new SideSheetDialog(requireContext());
    sideSheetDialog.setContentView(R.layout.cat_all_components_fragment);
    sideSheetDialog.show();
  }

  private void showSnackbar(CharSequence text) {
    Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).setAction("Dismiss", null).show();
  }

  @SuppressLint("WrongViewCast")
  private void setUpSplitButton(View view) {
    MaterialButton button = view.findViewById(R.id.split_button_expand);
    button.addOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            PopupMenu popup = new PopupMenu(requireContext(), button);
            MenuInflater menuInflater = popup.getMenuInflater();
            menuInflater.inflate(R.menu.cat_all_components_menu, popup.getMenu());
            popup.setOnDismissListener(popupMenu -> button.setChecked(false));
            popup.show();
          }
        });
  }

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_all_components_title;
  }
}
