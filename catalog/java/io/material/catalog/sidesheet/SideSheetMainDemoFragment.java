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

import static android.view.View.NO_ID;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.BackEventCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.GravityInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.sidesheet.SideSheetCallback;
import com.google.android.material.sidesheet.SideSheetDialog;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import io.material.catalog.preferences.CatalogPreferencesHelper;
import io.material.catalog.windowpreferences.WindowPreferencesManager;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays the main Side Sheet demo for the Catalog app. */
public class SideSheetMainDemoFragment extends DemoFragment {

  @Nullable private CatalogPreferencesHelper catalogPreferencesHelper;
  private final List<View> sideSheetViews = new ArrayList<>();

  private static final SparseIntArray GRAVITY_ID_RES_MAP = new SparseIntArray();

  static {
    GRAVITY_ID_RES_MAP.append(R.id.left_gravity_button, Gravity.LEFT);
    GRAVITY_ID_RES_MAP.append(R.id.right_gravity_button, Gravity.RIGHT);
    GRAVITY_ID_RES_MAP.append(R.id.start_gravity_button, Gravity.START);
    GRAVITY_ID_RES_MAP.append(R.id.end_gravity_button, Gravity.END);
  }

  private Button showModalSheetButton;
  private Button showDetachedModalSheetButton;
  private MaterialButtonToggleGroup sheetGravityButtonToggleGroup;

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

    ViewGroup sideSheetsContainer = view.findViewById(R.id.cat_sidesheet_coordinator_layout);
    View.inflate(getContext(), getSideSheetsContent(), sideSheetsContainer);

    setUpToolbar(view);
    setUpSheetGravityButtonToggleGroup(view);

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
    showModalSheetButton = view.findViewById(R.id.show_modal_side_sheet_button);
    setUpModalSheet();

    // Set up detached modal side sheet.
    showDetachedModalSheetButton = view.findViewById(R.id.show_modal_detached_side_sheet_button);
    setUpDetachedModalSheet();

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

  private void setUpSheetGravityButtonToggleGroup(@NonNull View view) {
    sheetGravityButtonToggleGroup = view.findViewById(R.id.sheet_gravity_button_toggle_group);
    // Check the button corresponding to end sheet gravity, which is the default.
    sheetGravityButtonToggleGroup.check(R.id.end_gravity_button);
    sheetGravityButtonToggleGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) {
            int sheetGravity = getGravityForIdRes(checkedId);

            for (View sideSheetView : sideSheetViews) {
              ViewGroup.LayoutParams layoutParams = sideSheetView.getLayoutParams();
              if (layoutParams instanceof LayoutParams) {
                ((LayoutParams) layoutParams).gravity = sheetGravity;
                sideSheetView.requestLayout();
              }
            }
          }
        });
  }

  private void setupBackHandling(View sideSheet, SideSheetBehavior<View> sideSheetBehavior) {
    OnBackPressedCallback nonModalOnBackPressedCallback =
        createNonModalOnBackPressedCallback(sideSheetBehavior);
    requireActivity().getOnBackPressedDispatcher().addCallback(this, nonModalOnBackPressedCallback);
    sideSheetBehavior.addCallback(
        new SideSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View sheet, int newState) {
            updateBackHandlingEnabled(nonModalOnBackPressedCallback, newState);
          }

          @Override
          public void onSlide(@NonNull View sheet, float slideOffset) {}
        });
    sideSheet.post(
        () ->
            updateBackHandlingEnabled(nonModalOnBackPressedCallback, sideSheetBehavior.getState()));
  }

  private void updateBackHandlingEnabled(OnBackPressedCallback onBackPressedCallback, int state) {
    switch (state) {
      case SideSheetBehavior.STATE_EXPANDED:
      case SideSheetBehavior.STATE_SETTLING:
        onBackPressedCallback.setEnabled(true);
        break;
      case SideSheetBehavior.STATE_HIDDEN:
        onBackPressedCallback.setEnabled(false);
        break;
      case SideSheetBehavior.STATE_DRAGGING:
      default:
        break;
    }
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
    TooltipCompat.setTooltipText(
        standardSideSheetCloseIconButton, standardSideSheetCloseIconButton.getContentDescription());

    setupBackHandling(sideSheet, sideSheetBehavior);

    DemoUtils.setupClickableContentText(sideSheet);

    sideSheetViews.add(sideSheet);

    return sideSheet;
  }

  private void setSideSheetCallback(
      View sideSheet, @IdRes int stateTextViewId, @IdRes int slideOffsetTextId) {
    SideSheetBehavior<View> sideSheetBehavior = SideSheetBehavior.from(sideSheet);
    TextView stateTextView = sideSheet.findViewById(stateTextViewId);
    sideSheetBehavior.addCallback(
        createSideSheetCallback(stateTextView, sideSheet.findViewById(slideOffsetTextId)));
    sideSheet.post(() -> updateStateTextView(stateTextView, sideSheetBehavior.getState()));
  }

  private void setUpDetachedModalSheet() {
    setUpModalSheet(
        getDetachedModalThemeOverlayResId(),
        R.layout.cat_sidesheet_content,
        com.google.android.material.R.id.m3_side_sheet,
        R.id.side_sheet_title_text,
        R.string.cat_sidesheet_modal_detached_title,
        showDetachedModalSheetButton,
        R.id.close_icon_button);
  }

  private void setUpModalSheet() {
    setUpModalSheet(
        R.layout.cat_sidesheet_content,
        com.google.android.material.R.id.m3_side_sheet,
        R.id.side_sheet_title_text,
        R.string.cat_sidesheet_modal_title,
        showModalSheetButton,
        R.id.close_icon_button);
  }

  private void setUpModalSheet(
      @LayoutRes int sheetContentLayoutRes,
      @IdRes int sheetContentRootIdRes,
      @IdRes int sheetTitleIdRes,
      @StringRes int sheetTitleStringRes,
      @NonNull Button showSheetButtonIdRes,
      @IdRes int closeIconButtonIdRes) {
    setUpModalSheet(
        NO_ID,
        sheetContentLayoutRes,
        sheetContentRootIdRes,
        sheetTitleIdRes,
        sheetTitleStringRes,
        showSheetButtonIdRes,
        closeIconButtonIdRes);
  }

  @SuppressWarnings("RestrictTo")
  private void setUpModalSheet(
      @StyleRes int sheetThemeOverlayRes,
      @LayoutRes int sheetContentLayoutRes,
      @IdRes int sheetContentRootIdRes,
      @IdRes int sheetTitleIdRes,
      @StringRes int sheetTitleStringRes,
      @NonNull Button showSheetButton,
      @IdRes int closeIconButtonIdRes) {
    showSheetButton.setOnClickListener(
        v1 -> {
          Context context = requireContext();
          SideSheetDialog sheetDialog =
              sheetThemeOverlayRes == NO_ID
                  ? new SideSheetDialog(context)
                  : new SideSheetDialog(context, sheetThemeOverlayRes);

          sheetDialog.setContentView(sheetContentLayoutRes);

          View modalSheetContent = sheetDialog.findViewById(sheetContentRootIdRes);
          if (modalSheetContent != null) {
            TextView modalSideSheetTitle = modalSheetContent.findViewById(sheetTitleIdRes);
            modalSideSheetTitle.setText(sheetTitleStringRes);
          }

          boolean edgeToEdgeEnabled = new WindowPreferencesManager(context).isEdgeToEdgeEnabled();
          boolean isLightTheme =
              MaterialAttributes.resolveBoolean(
                  context, androidx.appcompat.R.attr.isLightTheme, true);
          Window window = sheetDialog.getWindow();
          sheetDialog.setFitsSystemWindows(!edgeToEdgeEnabled);
          window.setNavigationBarColor(Color.TRANSPARENT);
          WindowCompat.getInsetsController(window, window.getDecorView())
              .setAppearanceLightStatusBars(edgeToEdgeEnabled && isLightTheme);

          sheetDialog
              .getBehavior()
              .addCallback(
                  createSideSheetCallback(
                      sheetDialog.findViewById(R.id.side_sheet_state_text),
                      sheetDialog.findViewById(R.id.side_sheet_slide_offset_text)));

          sheetDialog.setSheetEdge(
              getGravityForIdRes(sheetGravityButtonToggleGroup.getCheckedButtonId()));

          View modalSideSheetCloseIconButton = sheetDialog.findViewById(closeIconButtonIdRes);
          if (modalSideSheetCloseIconButton != null) {
            modalSideSheetCloseIconButton.setOnClickListener(v2 -> sheetDialog.hide());
            TooltipCompat.setTooltipText(
                modalSideSheetCloseIconButton,
                modalSideSheetCloseIconButton.getContentDescription());
          }

          sheetDialog.show();
        });
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

  @LayoutRes
  protected int getSideSheetsContent() {
    return R.layout.cat_sidesheets;
  }

  @StyleRes
  private int getDetachedModalThemeOverlayResId() {
    return R.style.ThemeOverlay_Catalog_SideSheet_Modal_Detached;
  }

  @GravityInt
  private static int getGravityForIdRes(@IdRes int gravityButtonIdRes) {
    return GRAVITY_ID_RES_MAP.get(gravityButtonIdRes);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  private SideSheetCallback createSideSheetCallback(
      @NonNull TextView stateTextView, @NonNull TextView slideOffsetTextView) {
    return new SideSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View sheet, int newState) {
        updateStateTextView(stateTextView, newState);
      }

      @Override
      public void onSlide(@NonNull View sheet, float slideOffset) {
        slideOffsetTextView.setVisibility(View.VISIBLE);
        slideOffsetTextView.setText(
            getResources().getString(R.string.cat_sidesheet_slide_offset_text, slideOffset));
      }
    };
  }

  private void updateStateTextView(@NonNull TextView stateTextView, int state) {
    stateTextView.setVisibility(View.VISIBLE);

    switch (state) {
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

  private OnBackPressedCallback createNonModalOnBackPressedCallback(
      SideSheetBehavior<View> behavior) {
    return new OnBackPressedCallback(/* enabled= */ false) {
      @Override
      public void handleOnBackStarted(@NonNull BackEventCompat backEvent) {
        behavior.startBackProgress(backEvent);
      }

      @Override
      public void handleOnBackProgressed(@NonNull BackEventCompat backEvent) {
        behavior.updateBackProgress(backEvent);
      }

      @Override
      public void handleOnBackPressed() {
        behavior.handleBackInvoked();
      }

      @Override
      public void handleOnBackCancelled() {
        behavior.cancelBackProgress();
      }
    };
  }
}
