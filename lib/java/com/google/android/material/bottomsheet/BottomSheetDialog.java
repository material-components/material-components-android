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

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.appcompat.app.AppCompatDialog;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

/** Base class for {@link android.app.Dialog}s styled as a bottom sheet. */
public class BottomSheetDialog extends AppCompatDialog {

  private BottomSheetBehavior<FrameLayout> behavior;

  private FrameLayout container;

  boolean dismissWithAnimation;

  boolean cancelable = true;
  private boolean canceledOnTouchOutside = true;
  private boolean canceledOnTouchOutsideSet;

  public BottomSheetDialog(@NonNull Context context) {
    this(context, 0);
  }

  public BottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
    super(context, getThemeResId(context, theme));
    // We hide the title bar for any style configuration. Otherwise, there will be a gap
    // above the bottom sheet when it is expanded.
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  protected BottomSheetDialog(
      @NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
    super(context, cancelable, cancelListener);
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    this.cancelable = cancelable;
  }

  @Override
  public void setContentView(@LayoutRes int layoutResId) {
    super.setContentView(wrapInBottomSheet(layoutResId, null, null));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      }
      window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
  }

  @Override
  public void setContentView(View view) {
    super.setContentView(wrapInBottomSheet(0, view, null));
  }

  @Override
  public void setContentView(View view, ViewGroup.LayoutParams params) {
    super.setContentView(wrapInBottomSheet(0, view, params));
  }

  @Override
  public void setCancelable(boolean cancelable) {
    super.setCancelable(cancelable);
    if (this.cancelable != cancelable) {
      this.cancelable = cancelable;
      if (behavior != null) {
        behavior.setHideable(cancelable);
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (behavior != null && behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
      behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  /**
   * This function can be called from a few different use cases, including Swiping the dialog down
   * or calling `dismiss()` from a `BottomSheetDialogFragment`, tapping outside a dialog, etc...
   *
   * <p>The default animation to dismiss this dialog is a fade-out transition through a
   * windowAnimation. Call {@link setDismissWithAnimation(true)} if you want to utilize the
   * BottomSheet animation instead.
   *
   * <p>If this function is called from a swipe down interaction, or dismissWithAnimation is false,
   * then keep the default behavior.
   *
   * <p>Else, since this is a terminal event which will finish this dialog, we override the attached
   * {@link BottomSheetBehavior.BottomSheetCallback} to call this function, after {@link
   * BottomSheetBehavior#STATE_HIDDEN} is set. This will enforce the swipe down animation before
   * canceling this dialog.
   */
  @Override
  public void cancel() {
    BottomSheetBehavior<FrameLayout> behavior = getBehavior();

    if (!dismissWithAnimation || behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
      super.cancel();
    } else {
      behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
  }

  @Override
  public void setCanceledOnTouchOutside(boolean cancel) {
    super.setCanceledOnTouchOutside(cancel);
    if (cancel && !cancelable) {
      cancelable = true;
    }
    canceledOnTouchOutside = cancel;
    canceledOnTouchOutsideSet = true;
  }

  @NonNull
  public BottomSheetBehavior<FrameLayout> getBehavior() {
    if (behavior == null) {
      // The content hasn't been set, so the behavior doesn't exist yet. Let's create it.
      ensureContainerAndBehavior();
    }
    return behavior;
  }

  /**
   * Set to perform the swipe down animation when dismissing instead of the window animation for the
   * dialog.
   *
   * @param dismissWithAnimation True if swipe down animation should be used when dismissing.
   */
  public void setDismissWithAnimation(boolean dismissWithAnimation) {
    this.dismissWithAnimation = dismissWithAnimation;
  }

  /**
   * Returns if dismissing will perform the swipe down animation on the bottom sheet, rather than
   * the window animation for the dialog.
   */
  public boolean getDismissWithAnimation() {
    return dismissWithAnimation;
  }

  /** Creates the container layout which must exist to find the behavior */
  private FrameLayout ensureContainerAndBehavior() {
    if (container == null) {
      container =
          (FrameLayout) View.inflate(getContext(), R.layout.design_bottom_sheet_dialog, null);

      FrameLayout bottomSheet = (FrameLayout) container.findViewById(R.id.design_bottom_sheet);
      behavior = BottomSheetBehavior.from(bottomSheet);
      behavior.addBottomSheetCallback(bottomSheetCallback);
      behavior.setHideable(cancelable);
    }
    return container;
  }

  private View wrapInBottomSheet(
      int layoutResId, @Nullable View view, @Nullable ViewGroup.LayoutParams params) {
    ensureContainerAndBehavior();
    CoordinatorLayout coordinator = (CoordinatorLayout) container.findViewById(R.id.coordinator);
    if (layoutResId != 0 && view == null) {
      view = getLayoutInflater().inflate(layoutResId, coordinator, false);
    }

    FrameLayout bottomSheet = (FrameLayout) container.findViewById(R.id.design_bottom_sheet);
    if (params == null) {
      bottomSheet.addView(view);
    } else {
      bottomSheet.addView(view, params);
    }
    // We treat the CoordinatorLayout as outside the dialog though it is technically inside
    coordinator
        .findViewById(R.id.touch_outside)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                if (cancelable && isShowing() && shouldWindowCloseOnTouchOutside()) {
                  cancel();
                }
              }
            });
    // Handle accessibility events
    ViewCompat.setAccessibilityDelegate(
        bottomSheet,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            if (cancelable) {
              info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS);
              info.setDismissable(true);
            } else {
              info.setDismissable(false);
            }
          }

          @Override
          public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && cancelable) {
              cancel();
              return true;
            }
            return super.performAccessibilityAction(host, action, args);
          }
        });
    bottomSheet.setOnTouchListener(
        new View.OnTouchListener() {
          @Override
          public boolean onTouch(View view, MotionEvent event) {
            // Consume the event and prevent it from falling through
            return true;
          }
        });
    return container;
  }

  boolean shouldWindowCloseOnTouchOutside() {
    if (!canceledOnTouchOutsideSet) {
      TypedArray a =
          getContext()
              .obtainStyledAttributes(new int[] {android.R.attr.windowCloseOnTouchOutside});
      canceledOnTouchOutside = a.getBoolean(0, true);
      a.recycle();
      canceledOnTouchOutsideSet = true;
    }
    return canceledOnTouchOutside;
  }

  private static int getThemeResId(@NonNull Context context, int themeId) {
    if (themeId == 0) {
      // If the provided theme is 0, then retrieve the dialogTheme from our theme
      TypedValue outValue = new TypedValue();
      if (context.getTheme().resolveAttribute(R.attr.bottomSheetDialogTheme, outValue, true)) {
        themeId = outValue.resourceId;
      } else {
        // bottomSheetDialogTheme is not provided; we default to our light theme
        themeId = R.style.Theme_Design_Light_BottomSheetDialog;
      }
    }
    return themeId;
  }

  void removeDefaultCallback() {
    behavior.removeBottomSheetCallback(bottomSheetCallback);
  }

  @NonNull
  private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
      new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(
            @NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
          if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            cancel();
          }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
      };
}
