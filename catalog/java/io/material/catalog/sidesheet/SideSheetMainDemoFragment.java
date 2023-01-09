/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.sidesheet;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.view.ViewCompat;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.sidesheet.SideSheetCallback;
import com.google.android.material.sidesheet.SideSheetDialog;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.preferences.CatalogPreferencesHelper;
import io.material.catalog.windowpreferences.WindowPreferencesManager;

/** A fragment that displays the main Side Sheet demo for the Catalog app. */
public final class SideSheetMainDemoFragment extends DemoFragment {

  @Nullable private CatalogPreferencesHelper catalogPreferencesHelper;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    // The preferences helper is used in an adhoc way with the toolbar since the demo draws its own
    // action bar, in order to allow the side sheet to be 100% of the screen's height.
    catalogPreferencesHelper = new CatalogPreferencesHelper(getParentFragmentManager());
  }

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);
    setUpToolbar(view);

    // Set up standard side sheet.
    View standardRightSideSheet =
        setUpSideSheet(
            view,
            R.id.standard_side_sheet_container,
            R.id.show_standard_side_sheet_button,
            R.id.close_icon_button);

    setSideSheetCallback(
        standardRightSideSheet, R.id.side_sheet_state_text, R.id.side_sheet_slide_offset_text);

    // Set up detached standard side sheet.
    View detachedStandardSideSheet =
        setUpSideSheet(
            view,
            R.id.standard_detached_side_sheet_container,
            R.id.show_standard_detached_side_sheet_button,
            R.id.detached_close_icon_button);

    setSideSheetCallback(
        detachedStandardSideSheet,
        R.id.detached_side_sheet_state_text,
        R.id.detached_side_sheet_slide_offset_text);

    // Set up vertically scrolling side sheet.
    View verticallyScrollingSideSheet =
        setUpSideSheet(
            view,
            R.id.vertically_scrolling_side_sheet_container,
            R.id.show_vertically_scrolling_side_sheet_button,
            R.id.vertically_scrolling_side_sheet_close_icon_button);

    setSideSheetCallback(
        verticallyScrollingSideSheet,
        R.id.vertically_scrolling_side_sheet_state_text,
        R.id.vertically_scrolling_side_sheet_slide_offset_text);

    // Set up modal side sheet.
    SideSheetDialog sideSheetDialog = new SideSheetDialog(requireContext());
    setUpModalSheet(
        sideSheetDialog,
        R.layout.cat_sidesheet_content,
        R.id.m3_side_sheet,
        R.id.side_sheet_title_text,
        R.string.cat_sidesheet_modal_title);

    View showModalSideSheetButton = view.findViewById(R.id.show_modal_side_sheet_button);
    showModalSideSheetButton.setOnClickListener(v -> sideSheetDialog.show());

    sideSheetDialog
        .getBehavior()
        .addCallback(
            createSideSheetCallback(
                sideSheetDialog.findViewById(R.id.side_sheet_state_text),
                sideSheetDialog.findViewById(R.id.side_sheet_slide_offset_text)));

    View modalSideSheetCloseIconButton = sideSheetDialog.findViewById(R.id.close_icon_button);
    if (modalSideSheetCloseIconButton != null) {
      modalSideSheetCloseIconButton.setOnClickListener(v -> sideSheetDialog.hide());
    }

    // Set up detached modal side sheet.
    SideSheetDialog detachedSideSheetDialog =
        new SideSheetDialog(requireContext(), getDetachedModalThemeOverlayResId());

    setUpModalSheet(
        detachedSideSheetDialog,
        R.layout.cat_sidesheet_content,
        R.id.m3_side_sheet,
        R.id.side_sheet_title_text,
        R.string.cat_sidesheet_modal_detached_title);

    View showDetachedModalSideSheetButton =
        view.findViewById(R.id.show_modal_detached_side_sheet_button);
    showDetachedModalSideSheetButton.setOnClickListener(v -> detachedSideSheetDialog.show());

    detachedSideSheetDialog
        .getBehavior()
        .addCallback(
            createSideSheetCallback(
                detachedSideSheetDialog.findViewById(R.id.side_sheet_state_text),
                detachedSideSheetDialog.findViewById(R.id.side_sheet_slide_offset_text)));

    View detachedModalSideSheetCloseIconButton =
        detachedSideSheetDialog.findViewById(R.id.close_icon_button);
    if (detachedModalSideSheetCloseIconButton != null) {
      detachedModalSideSheetCloseIconButton.setOnClickListener(v -> detachedSideSheetDialog.hide());
    }

    // Set up coplanar side sheet.
    View coplanarSideSheet =
        setUpSideSheet(
            view,
            R.id.coplanar_side_sheet_container,
            R.id.show_coplanar_side_sheet_button,
            R.id.coplanar_side_sheet_close_icon_button);

    setSideSheetCallback(
        coplanarSideSheet,
        R.id.coplanar_side_sheet_state_text,
        R.id.coplanar_side_sheet_slide_offset_text);

    // Set up detached coplanar side sheet.
    View detachedCoplanarSideSheet =
        setUpSideSheet(
            view,
            R.id.coplanar_detached_side_sheet_container,
            R.id.show_coplanar_detached_side_sheet_button,
            R.id.coplanar_detached_side_sheet_close_icon_button);

    setSideSheetCallback(
        detachedCoplanarSideSheet,
        R.id.coplanar_detached_side_sheet_state_text,
        R.id.coplanar_detached_side_sheet_slide_offset_text);

    return view;
  }

  private View setUpSideSheet(
      @NonNull View view,
      @IdRes int sideSheetContainerId,
      @IdRes int showSideSheetButtonId,
      @IdRes int closeIconButtonId) {
    View sideSheet = view.findViewById(sideSheetContainerId);
    SideSheetBehavior<View> sideSheetBehavior = SideSheetBehavior.from(sideSheet);

    Button showSideSheetButton = view.findViewById(showSideSheetButtonId);
    showSideSheetButton.setOnClickListener(unusedView -> showSideSheet(sideSheetBehavior));

    View standardSideSheetCloseIconButton = sideSheet.findViewById(closeIconButtonId);
    standardSideSheetCloseIconButton.setOnClickListener(v -> hideSideSheet(sideSheetBehavior));

    return sideSheet;
  }

  private void setSideSheetCallback(
      View sideSheet, @IdRes int stateTextViewId, @IdRes int slideOffsetTextId) {
    SideSheetBehavior<View> sideSheetBehavior = SideSheetBehavior.from(sideSheet);
    sideSheetBehavior.addCallback(
        createSideSheetCallback(
            sideSheet.findViewById(stateTextViewId), sideSheet.findViewById(slideOffsetTextId)));
  }

  private void setUpModalSheet(
      @NonNull AppCompatDialog sheetDialog,
      @LayoutRes int sheetContentLayoutRes,
      @IdRes int sheetContentRootIdRes,
      @IdRes int sheetTitleIdRes,
      @StringRes int sheetTitleStringRes) {
    sheetDialog.setContentView(sheetContentLayoutRes);
    View modalSheetContent = sheetDialog.findViewById(sheetContentRootIdRes);
    if (modalSheetContent != null) {
      TextView modalSideSheetTitle = modalSheetContent.findViewById(sheetTitleIdRes);
      modalSideSheetTitle.setText(sheetTitleStringRes);
    }
    new WindowPreferencesManager(requireContext())
        .applyEdgeToEdgePreference(sheetDialog.getWindow());
  }

  private void setUpToolbar(@NonNull View view) {
    @NonNull Toolbar toolbar = ViewCompat.requireViewById(view, R.id.toolbar);
    @Nullable AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity != null) {
      toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
      if (catalogPreferencesHelper != null) {
        catalogPreferencesHelper.onCreateOptionsMenu(toolbar.getMenu(), activity.getMenuInflater());
        toolbar.setOnMenuItemClickListener(catalogPreferencesHelper::onOptionsItemSelected);
      }
    }
  }

  private void showSideSheet(SideSheetBehavior<View> sheetBehavior) {
    sheetBehavior.expand();
  }

  private void hideSideSheet(SideSheetBehavior<View> sheetBehavior) {
    sheetBehavior.hide();
  }

  @LayoutRes
  int getDemoContent() {
    return R.layout.cat_sidesheet_fragment;
  }

  @StyleRes
  private int getDetachedModalThemeOverlayResId() {
    return R.style.ThemeOverlay_Catalog_SideSheet_Modal_Detached;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  private SideSheetCallback createSideSheetCallback(
      TextView stateTextView, TextView slideOffsetTextView) {
    return new SideSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View sheet, int newState) {
        switch (newState) {
          case SideSheetBehavior.STATE_DRAGGING:
            stateTextView.setText(R.string.cat_sidesheet_state_dragging);
            break;
          case SideSheetBehavior.STATE_EXPANDED:
            stateTextView.setText(R.string.cat_sidesheet_state_expanded);
            break;
          case SideSheetBehavior.STATE_SETTLING:
            stateTextView.setText(R.string.cat_sidesheet_state_settling);
            break;
          case SideSheetBehavior.STATE_HIDDEN:
          default:
            break;
        }
      }

      @Override
      public void onSlide(@NonNull View sheet, float slideOffset) {
        slideOffsetTextView.setText(
            getResources().getString(R.string.cat_sidesheet_slide_offset_text, slideOffset));
      }
    };
  }
}
