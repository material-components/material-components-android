/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a Toolbar Top App Bar demo for the Catalog app. */
public class TopAppBarToolbarDemoFragment extends DemoFragment {

  private static final int NAVIGATION_ICON_RES_ID = R.drawable.ic_close_vd_theme_24px;
  private static final int MENU_RES_ID = R.menu.cat_topappbar_menu;

  private final ConfigureViewData configureViewData = new ConfigureViewData();

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(getToolbarLayoutResId(), viewGroup, /* attachToRoot= */ false);

    List<MaterialToolbar> toolbars = DemoUtils.findViewsWithType(view, MaterialToolbar.class);
    for (MaterialToolbar toolbar : toolbars) {
      initToolbar(view, toolbar);
    }

    return view;
  }

  @LayoutRes
  protected int getToolbarLayoutResId() {
    return R.layout.cat_topappbar_toolbar_fragment;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_configure_toolbars_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.configure_toolbars) {
      BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
      bottomSheetDialog.setContentView(createConfigureToolbarsView(bottomSheetDialog));
      bottomSheetDialog.show();
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  private void initToolbar(View view, MaterialToolbar toolbar) {
    toolbar.setNavigationIcon(NAVIGATION_ICON_RES_ID);
    toolbar.setNavigationContentDescription(R.string.cat_topappbar_close_button);
    toolbar.setNavigationOnClickListener(
        v -> showSnackbar(view, toolbar.getSubtitle() + " " + toolbar.getTitle()));
    toolbar.inflateMenu(MENU_RES_ID);
    toolbar.setOnMenuItemClickListener(
        menuItem -> {
          showSnackbar(view, menuItem.getTitle());
          return true;
        });
  }

  private void showSnackbar(View view, CharSequence text) {
    Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
  }

  private View createConfigureToolbarsView(BottomSheetDialog bottomSheetDialog) {
    View configureView =
        LayoutInflater.from(requireContext())
            .inflate(R.layout.cat_topappbar_configure_toolbars, (ViewGroup) requireView(), false);

    ConfigureViewHolder configureViewHolder = new ConfigureViewHolder(configureView);
    configureViewHolder.titleEditText.setText(configureViewData.titleText);
    configureViewHolder.titleCenteredCheckBox.setChecked(configureViewData.titleCentered);
    configureViewHolder.subtitleEditText.setText(configureViewData.subtitleText);
    configureViewHolder.subtitleCenteredCheckBox.setChecked(configureViewData.subtitleCentered);
    configureViewHolder.navigationIconCheckBox.setChecked(configureViewData.navigationIcon);
    configureViewHolder.menuItemsCheckBox.setChecked(configureViewData.menuItems);

    configureView
        .findViewById(R.id.apply_button)
        .setOnClickListener(
            v -> {
              applyToolbarConfigurations(configureViewHolder);
              bottomSheetDialog.dismiss();
            });

    configureView
        .findViewById(R.id.cancel_button)
        .setOnClickListener(v -> bottomSheetDialog.dismiss());
    return configureView;
  }

  private void applyToolbarConfigurations(ConfigureViewHolder holder) {
    configureViewData.titleText = holder.titleEditText.getText();
    configureViewData.titleCentered = holder.titleCenteredCheckBox.isChecked();
    configureViewData.subtitleText = holder.subtitleEditText.getText();
    configureViewData.subtitleCentered = holder.subtitleCenteredCheckBox.isChecked();
    configureViewData.navigationIcon = holder.navigationIconCheckBox.isChecked();
    configureViewData.menuItems = holder.menuItemsCheckBox.isChecked();

    List<MaterialToolbar> toolbars =
        DemoUtils.findViewsWithType(requireView(), MaterialToolbar.class);
    for (MaterialToolbar toolbar : toolbars) {
      if (!TextUtils.isEmpty(configureViewData.titleText)) {
        toolbar.setTitle(configureViewData.titleText);
      }
      toolbar.setTitleCentered(configureViewData.titleCentered);

      if (!TextUtils.isEmpty(configureViewData.subtitleText)) {
        toolbar.setSubtitle(configureViewData.subtitleText);
      }
      toolbar.setSubtitleCentered(configureViewData.subtitleCentered);

      if (configureViewData.navigationIcon) {
        toolbar.setNavigationIcon(NAVIGATION_ICON_RES_ID);
      } else {
        toolbar.setNavigationIcon(null);
      }

      toolbar.getMenu().clear();
      if (configureViewData.menuItems) {
        toolbar.inflateMenu(MENU_RES_ID);
      }
    }
  }

  private static class ConfigureViewHolder {
    private final EditText titleEditText;
    private final CheckBox titleCenteredCheckBox;
    private final EditText subtitleEditText;
    private final CheckBox subtitleCenteredCheckBox;
    private final CheckBox navigationIconCheckBox;
    private final CheckBox menuItemsCheckBox;

    private ConfigureViewHolder(View view) {
      titleEditText = view.findViewById(R.id.toolbar_title_edittext);
      titleCenteredCheckBox = view.findViewById(R.id.toolbar_title_centered_checkbox);
      subtitleEditText = view.findViewById(R.id.toolbar_subtitle_edittext);
      subtitleCenteredCheckBox = view.findViewById(R.id.toolbar_subtitle_centered_checkbox);
      navigationIconCheckBox = view.findViewById(R.id.toolbar_navigation_icon_checkbox);
      menuItemsCheckBox = view.findViewById(R.id.toolbar_menu_items_checkbox);
    }
  }

  private static class ConfigureViewData {
    private CharSequence titleText = "";
    private boolean titleCentered = false;
    private CharSequence subtitleText = "";
    private boolean subtitleCentered = false;
    private boolean navigationIcon = true;
    private boolean menuItems = true;
  }
}
