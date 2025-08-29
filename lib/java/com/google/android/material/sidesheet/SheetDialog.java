/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.sidesheet;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.GravityInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.internal.EdgeToEdgeUtils;
import com.google.android.material.motion.MaterialBackOrchestrator;
import com.google.android.material.sidesheet.Sheet.StableSheetState;

/**
 * Base class for {@link android.app.Dialog}s styled as a sheet, to be used by sheet dialog
 * implementations such as side sheets and bottom sheets.
 */
abstract class SheetDialog<C extends SheetCallback> extends AppCompatDialog {

  private static final int COORDINATOR_LAYOUT_ID = R.id.coordinator;
  private static final int TOUCH_OUTSIDE_ID = R.id.touch_outside;

  @Nullable private Sheet<C> behavior;
  @Nullable private FrameLayout container;
  @Nullable private CoordinatorLayout coordinator;
  @Nullable private FrameLayout sheet;

  boolean dismissWithAnimation;

  boolean cancelable = true;
  private boolean canceledOnTouchOutside = true;
  private boolean canceledOnTouchOutsideSet;
  private boolean fitsSystemWindows = true;

  @Nullable private MaterialBackOrchestrator backOrchestrator;

  SheetDialog(
      @NonNull Context context,
      @StyleRes int theme,
      @AttrRes int themeAttr,
      @StyleRes int defaultThemeAttr) {
    super(context, getThemeResId(context, theme, themeAttr, defaultThemeAttr));
    // We hide the title bar for any style configuration. Otherwise, there will be a gap
    // above the sheet when it is expanded.
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  @Override
  public void setContentView(@LayoutRes int layoutResId) {
    super.setContentView(wrapInSheet(layoutResId, null, null));
  }

  @Override
  public void setContentView(@Nullable View view) {
    super.setContentView(wrapInSheet(0, view, null));
  }

  @Override
  public void setContentView(@Nullable View view, @Nullable ViewGroup.LayoutParams params) {
    super.setContentView(wrapInSheet(0, view, params));
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      // The status bar should always be transparent because of the window animation.
      EdgeToEdgeUtils.setStatusBarColor(window, 0);

      window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      if (VERSION.SDK_INT < VERSION_CODES.M) {
        // It can be transparent for API 23 and above because we will handle switching the status
        // bar icons to light or dark as appropriate. For API 21 and API 22 we just set the
        // translucent status bar.
        window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
      }
      window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
  }

  @Override
  public void setCancelable(boolean cancelable) {
    super.setCancelable(cancelable);
    if (this.cancelable != cancelable) {
      this.cancelable = cancelable;
    }
    if (getWindow() != null) {
      updateListeningForBackCallbacks();
    }
  }

  private void updateFitsSystemWindows() {
    if (container != null) {
      container.setFitsSystemWindows(fitsSystemWindows);
    }
    if (coordinator != null) {
      coordinator.setFitsSystemWindows(fitsSystemWindows);
    }
    Window window = getWindow();
    if (window != null) {
      WindowCompat.setDecorFitsSystemWindows(window, fitsSystemWindows);
    }
  }

  private void updateListeningForBackCallbacks() {
    if (backOrchestrator == null) {
      return;
    }
    if (cancelable) {
      backOrchestrator.startListeningForBackCallbacks();
    } else {
      backOrchestrator.stopListeningForBackCallbacks();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (behavior != null && behavior.getState() == Sheet.STATE_HIDDEN) {
      behavior.setState(getStateOnStart());
    }
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    maybeUpdateWindowAnimationsBasedOnLayoutDirection();
    updateFitsSystemWindows();
    updateListeningForBackCallbacks();
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (backOrchestrator != null) {
      backOrchestrator.stopListeningForBackCallbacks();
    }
  }

  /**
   * This function can be called from a few different use cases, including swiping the dialog away
   * or calling `dismiss()` from a `SideSheetDialogFragment`, tapping outside a dialog, etc...
   *
   * <p>The default animation to dismiss this dialog is a fade-out transition through a
   * windowAnimation. Set {@link #setDismissWithSheetAnimationEnabled(boolean)} to true if you want
   * to utilize the sheet animation instead.
   *
   * <p>If this function is called from a swipe interaction, or dismissWithAnimation is false, then
   * keep the default behavior.
   */
  @Override
  public void cancel() {
    Sheet<C> behavior = getBehavior();

    if (!dismissWithAnimation || behavior.getState() == Sheet.STATE_HIDDEN) {
      super.cancel();
    } else {
      behavior.setState(Sheet.STATE_HIDDEN);
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

  /**
   * Set whether to perform the swipe away animation on the sheet when dismissing, rather than the
   * window animation for the dialog.
   *
   * @param dismissWithAnimation True if swipe away animation should be used when dismissing.
   */
  public void setDismissWithSheetAnimationEnabled(boolean dismissWithAnimation) {
    this.dismissWithAnimation = dismissWithAnimation;
  }

  /**
   * Returns whether dismissing will perform the swipe away animation on the sheet, rather than the
   * window animation for the dialog.
   */
  public boolean isDismissWithSheetAnimationEnabled() {
    return dismissWithAnimation;
  }

  /**
   * Sets whether or not this sheet dialog should account for system screen decorations
   * such as the status bar and inset its content. Default is true.
   *
   * <p>This will forward the {@code fitsSystemWindows} to the sheet dialog's child views via
   * {@link View#setFitsSystemWindows(boolean)}, as well as the sheet dialog's window via
   * {@link WindowCompat#setDecorFitsSystemWindows(Window, boolean)}.
   */
  public void setFitsSystemWindows(boolean fitsSystemWindows) {
    this.fitsSystemWindows = fitsSystemWindows;
    updateFitsSystemWindows();
  }

  /** Creates the container layout which must exist to find the behavior */
  private void ensureContainerAndBehavior() {
    if (container == null) {
      container = (FrameLayout) View.inflate(getContext(), getLayoutResId(), null);
      sheet = container.findViewById(getDialogId());
      behavior = getBehaviorFromSheet(sheet);
      addSheetCancelOnHideCallback(behavior);
      backOrchestrator = new MaterialBackOrchestrator(behavior, sheet);
    }
  }

  abstract void addSheetCancelOnHideCallback(Sheet<C> behavior);

  @NonNull
  private FrameLayout getContainer() {
    if (this.container == null) {
      ensureContainerAndBehavior();
    }
    return container;
  }

  @NonNull
  private FrameLayout getSheet() {
    if (this.sheet == null) {
      ensureContainerAndBehavior();
    }
    return sheet;
  }

  @NonNull
  Sheet<C> getBehavior() {
    if (this.behavior == null) {
      // The content hasn't been set, so the behavior doesn't exist yet. Let's create it.
      ensureContainerAndBehavior();
    }
    return behavior;
  }

  private View wrapInSheet(
      int layoutResId, @Nullable View view, @Nullable ViewGroup.LayoutParams params) {
    ensureContainerAndBehavior();
    coordinator = getContainer().findViewById(COORDINATOR_LAYOUT_ID);

    if (layoutResId != 0 && view == null) {
      view = getLayoutInflater().inflate(layoutResId, coordinator, false);
    }

    FrameLayout sheet = getSheet();
    sheet.removeAllViews();
    if (params == null) {
      sheet.addView(view);
    } else {
      sheet.addView(view, params);
    }
    // We treat the CoordinatorLayout as outside the dialog though it is technically inside.
    coordinator
        .findViewById(TOUCH_OUTSIDE_ID)
        .setOnClickListener(
            v -> {
              if (cancelable && isShowing() && shouldWindowCloseOnTouchOutside()) {
                cancel();
              }
            });

    // Handle accessibility events.
    ViewCompat.setAccessibilityDelegate(
        getSheet(),
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
    return container;
  }

  /**
   * Set the edge which the sheet should originate from.
   *
   * <p>Note: This method should be called when the sheet is initialized, before it is shown.
   * Runtime sheet edge changes are not supported.
   *
   * @throws IllegalStateException if the sheet is null or has already been laid out
   * @param gravity the edge from which the sheet and its animations should originate.
   */
  public void setSheetEdge(@GravityInt int gravity) {
    if (sheet == null) {
      throw new IllegalStateException(
          "Sheet view reference is null; sheet edge cannot be changed if the sheet view is null.");
    }
    if (sheet.isLaidOut()) {
      throw new IllegalStateException(
          "Sheet view has been laid out; sheet edge cannot be changed once the sheet has been laid"
              + " out.");
    }
    ViewGroup.LayoutParams layoutParams = sheet.getLayoutParams();
    if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
      ((CoordinatorLayout.LayoutParams) layoutParams).gravity = gravity;
      maybeUpdateWindowAnimationsBasedOnLayoutDirection();
    }
  }

  private void maybeUpdateWindowAnimationsBasedOnLayoutDirection() {
    Window window = getWindow();
    if (window != null
        && sheet != null
        && sheet.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
      CoordinatorLayout.LayoutParams layoutParams =
          (CoordinatorLayout.LayoutParams) sheet.getLayoutParams();
      int absoluteGravity =
          Gravity.getAbsoluteGravity(layoutParams.gravity, sheet.getLayoutDirection());
      window.setWindowAnimations(
          absoluteGravity == Gravity.LEFT
              ? R.style.Animation_Material3_SideSheetDialog_Left
              : R.style.Animation_Material3_SideSheetDialog_Right);
    }
  }

  private boolean shouldWindowCloseOnTouchOutside() {
    if (!canceledOnTouchOutsideSet) {
      TypedArray a =
          getContext().obtainStyledAttributes(new int[] {android.R.attr.windowCloseOnTouchOutside});
      canceledOnTouchOutside = a.getBoolean(0, true);
      a.recycle();
      canceledOnTouchOutsideSet = true;
    }
    return canceledOnTouchOutside;
  }

  private static int getThemeResId(
      @NonNull Context context,
      @StyleRes int themeId,
      @AttrRes int themeAttr,
      @StyleRes int defaultTheme) {
    if (themeId == 0) {
      // If the provided theme is 0, retrieve the dialog theme from our theme.
      TypedValue outValue = new TypedValue();
      if (context.getTheme().resolveAttribute(themeAttr, outValue, true)) {
        themeId = outValue.resourceId;
      } else {
        // Dialog theme is not provided; we default to our light theme.
        themeId = defaultTheme;
      }
    }
    return themeId;
  }

  @LayoutRes
  abstract int getLayoutResId();

  @IdRes
  abstract int getDialogId();

  @NonNull
  abstract Sheet<C> getBehaviorFromSheet(@NonNull FrameLayout sheet);

  @StableSheetState
  abstract int getStateOnStart();
}
