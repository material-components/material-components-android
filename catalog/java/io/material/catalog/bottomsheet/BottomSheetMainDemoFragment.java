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

package io.material.catalog.bottomsheet;

import io.material.catalog.R;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.getDefaultBottomGradientProtection;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.BackEventCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.insets.ProtectionLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.windowpreferences.WindowPreferencesManager;
import java.util.Collections;

/** A fragment that displays the main BottomSheet demo for the Catalog app. */
public class BottomSheetMainDemoFragment extends DemoFragment {

  private final OnBackPressedCallback persistentBottomSheetBackCallback =
      new OnBackPressedCallback(/* enabled= */ false) {

        @Override
        public void handleOnBackStarted(@NonNull BackEventCompat backEvent) {
          persistentBottomSheetBehavior.startBackProgress(backEvent);
        }

        @Override
        public void handleOnBackProgressed(@NonNull BackEventCompat backEvent) {
          persistentBottomSheetBehavior.updateBackProgress(backEvent);
        }

        @Override
        public void handleOnBackPressed() {
          persistentBottomSheetBehavior.handleBackInvoked();
        }

        @Override
        public void handleOnBackCancelled() {
          persistentBottomSheetBehavior.cancelBackProgress();
        }
      };

  private WindowPreferencesManager windowPreferencesManager;
  private BottomSheetDialog bottomSheetDialog;
  private BottomSheetBehavior<View> persistentBottomSheetBehavior;
  private WindowInsetsCompat windowInsets;
  private int peekHeightPx;

  private MaterialSwitch fullScreenSwitch;
  private MaterialSwitch restrictExpansionSwitch;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    windowPreferencesManager = new WindowPreferencesManager(requireContext());
    peekHeightPx = getResources().getDimensionPixelSize(R.dimen.cat_bottom_sheet_peek_height);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.cat_bottomsheet_coordinator_layout);
    content.addView(layoutInflater.inflate(getStandardBottomSheetLayout(), content, false));

    // Set up BottomSheetDialog
    bottomSheetDialog = new BottomSheetDialog(requireContext());
    bottomSheetDialog.setContentView(R.layout.cat_bottomsheet_content);
    // Opt in to perform swipe to dismiss animation when dismissing bottom sheet dialog.
    bottomSheetDialog.setDismissWithAnimation(true);
    windowPreferencesManager.applyEdgeToEdgePreference(bottomSheetDialog.getWindow());
    View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
    bottomSheetDialog.setProtections(
        Collections.singletonList(
            getDefaultBottomGradientProtection(getContext())));
    BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(peekHeightPx);
    View button = view.findViewById(R.id.bottomsheet_button);
    button.setOnClickListener(
        v -> {
          bottomSheetDialog.show();
          bottomSheetDialog.setTitle(getText(R.string.cat_bottomsheet_title));
          Button button0 = bottomSheetInternal.findViewById(R.id.cat_bottomsheet_modal_button);
          button0.setOnClickListener(
              v0 ->
                  Toast.makeText(
                          v.getContext(),
                          R.string.cat_bottomsheet_button_clicked,
                          Toast.LENGTH_SHORT)
                      .show());

          MaterialSwitch enabledSwitch =
              bottomSheetInternal.findViewById(R.id.cat_bottomsheet_modal_enabled_switch);
          enabledSwitch.setOnCheckedChangeListener(
              (buttonSwitch, isSwitchChecked) -> {
                CharSequence updatedText =
                    getText(
                        isSwitchChecked
                            ? R.string.cat_bottomsheet_button_label_enabled
                            : R.string.cat_bottomsheet_button_label_disabled);
                button0.setText(updatedText);
                button0.setEnabled(isSwitchChecked);
              });
        });

    fullScreenSwitch = view.findViewById(R.id.cat_fullscreen_switch);
    fullScreenSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          restrictExpansionSwitch.setEnabled(!isChecked);
          updateBottomSheetHeights();
        });

    restrictExpansionSwitch = view.findViewById(R.id.cat_bottomsheet_expansion_switch);
    restrictExpansionSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          fullScreenSwitch.setEnabled(!isChecked);
          view.findViewById(R.id.drag_handle).setEnabled(!isChecked);
          bottomSheetInternal.findViewById(R.id.drag_handle).setEnabled(!isChecked);
          updateBottomSheetHeights();
        });

    TextView dialogText = bottomSheetInternal.findViewById(R.id.bottomsheet_state);
    BottomSheetBehavior.from(bottomSheetInternal)
        .addBottomSheetCallback(createBottomSheetCallback(dialogText));
    TextView bottomSheetText = view.findViewById(R.id.cat_persistent_bottomsheet_state);
    View bottomSheetPersistent = view.findViewById(R.id.bottom_drawer);
    ProtectionLayout protectionLayout = view.findViewById(R.id.cat_bottomsheet_protection_layout);
    persistentBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetPersistent);
    persistentBottomSheetBehavior.addBottomSheetCallback(
        createBottomSheetCallback(bottomSheetText));
    bottomSheetPersistent.post(
        () -> {
          int state = persistentBottomSheetBehavior.getState();
          updateStateTextView(bottomSheetPersistent, bottomSheetText, state);
          updateBackHandlingEnabled(state);
          protectionLayout.setProtections(
              Collections.singletonList(
                  getDefaultBottomGradientProtection(
                      requireContext())));
        });
    setupBackHandling(persistentBottomSheetBehavior);

    Button button1 = view.findViewById(R.id.cat_bottomsheet_button);
    button1.setOnClickListener(
        v ->
            Toast.makeText(
                    v.getContext(), R.string.cat_bottomsheet_button_clicked, Toast.LENGTH_SHORT)
                .show());

    MaterialSwitch enabledSwitch = view.findViewById(R.id.cat_bottomsheet_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          CharSequence updatedText =
              getText(
                  isChecked
                      ? R.string.cat_bottomsheet_button_label_enabled
                      : R.string.cat_bottomsheet_button_label_disabled);
          button1.setText(updatedText);
          button1.setEnabled(isChecked);
        });

    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (ignored, insets) -> {
          windowInsets = insets;
          updateBottomSheetHeights();
          return insets;
        });

    return view;
  }

  private int getBottomSheetDialogDefaultHeight() {
    return getWindowHeight() * 2 / 3;
  }

  private int getBottomSheetPersistentDefaultHeight() {
    return getWindowHeight() * 3 / 5;
  }

  private void updateBottomSheetHeights() {
    View view = getView();
    View bottomSheetChildView = view.findViewById(R.id.bottom_drawer);
    ViewGroup.LayoutParams params = bottomSheetChildView.getLayoutParams();
    BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetChildView);
    bottomSheetBehavior.setUpdateImportantForAccessibilityOnSiblings(fullScreenSwitch.isChecked());
    View modalBottomSheetChildView = bottomSheetDialog.findViewById(R.id.bottom_drawer_2);
    ViewGroup.LayoutParams layoutParams = modalBottomSheetChildView.getLayoutParams();
    BottomSheetBehavior<FrameLayout> modalBottomSheetBehavior = bottomSheetDialog.getBehavior();
    boolean fitToContents = true;
    float halfExpandedRatio = 0.5f;
    int windowHeight = getWindowHeight();
    if (params != null && layoutParams != null) {
      if (fullScreenSwitch.isEnabled() && fullScreenSwitch.isChecked()) {
        params.height = windowHeight;
        layoutParams.height = windowHeight;
        fitToContents = false;
        halfExpandedRatio = 0.7f;
      } else if (restrictExpansionSwitch.isEnabled() && restrictExpansionSwitch.isChecked()) {
        params.height = peekHeightPx;
        layoutParams.height = peekHeightPx;
      } else {
        params.height = getBottomSheetPersistentDefaultHeight();
        layoutParams.height = getBottomSheetDialogDefaultHeight();
      }
      bottomSheetChildView.setLayoutParams(params);
      modalBottomSheetChildView.setLayoutParams(layoutParams);
      bottomSheetBehavior.setFitToContents(fitToContents);
      modalBottomSheetBehavior.setFitToContents(fitToContents);
      bottomSheetBehavior.setHalfExpandedRatio(halfExpandedRatio);
      modalBottomSheetBehavior.setHalfExpandedRatio(halfExpandedRatio);
    }
  }

  private int getWindowHeight() {
    // Calculate window height for fullscreen use
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    // Allow Fullscreen BottomSheet to expand beyond system windows and draw under status bar.
    int height = displayMetrics.heightPixels;
    if (windowInsets != null) {
      height += windowInsets.getSystemWindowInsetTop();
      height += windowInsets.getSystemWindowInsetBottom();
    }
    return height;
  }

  @LayoutRes
  protected int getDemoContent() {
    return R.layout.cat_bottomsheet_fragment;
  }

  @LayoutRes
  protected int getStandardBottomSheetLayout() {
    return R.layout.cat_bottomsheet_standard;
  }

  private BottomSheetCallback createBottomSheetCallback(@NonNull TextView text) {
    return new BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        updateStateTextView(bottomSheet, text, newState);
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
    };
  }

  private void updateStateTextView(@NonNull View bottomSheet, @NonNull TextView text, int state) {
    switch (state) {
      case BottomSheetBehavior.STATE_DRAGGING:
        text.setText(R.string.cat_bottomsheet_state_dragging);
        break;
      case BottomSheetBehavior.STATE_EXPANDED:
        text.setText(R.string.cat_bottomsheet_state_expanded);
        break;
      case BottomSheetBehavior.STATE_COLLAPSED:
        text.setText(R.string.cat_bottomsheet_state_collapsed);
        break;
      case BottomSheetBehavior.STATE_HALF_EXPANDED:
        BottomSheetBehavior<View> bottomSheetBehavior =
            BottomSheetBehavior.from(bottomSheet);
        text.setText(
            getString(
                R.string.cat_bottomsheet_state_half_expanded,
                bottomSheetBehavior.getHalfExpandedRatio()));
        break;
      default:
        break;
    }
  }

  private void setupBackHandling(BottomSheetBehavior<View> behavior) {
    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this, persistentBottomSheetBackCallback);
    behavior.addBottomSheetCallback(
        new BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            updateBackHandlingEnabled(newState);
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
  }

  private void updateBackHandlingEnabled(int state) {
    switch (state) {
      case BottomSheetBehavior.STATE_EXPANDED:
      case BottomSheetBehavior.STATE_HALF_EXPANDED:
        persistentBottomSheetBackCallback.setEnabled(true);
        break;
      case BottomSheetBehavior.STATE_COLLAPSED:
      case BottomSheetBehavior.STATE_HIDDEN:
        persistentBottomSheetBackCallback.setEnabled(false);
        break;
      case BottomSheetBehavior.STATE_DRAGGING:
      case BottomSheetBehavior.STATE_SETTLING:
      default:
        // Do nothing, only change callback enabled for "stable" states.
        break;
    }
  }
}
