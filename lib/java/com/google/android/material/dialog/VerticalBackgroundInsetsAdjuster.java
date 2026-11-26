/*
 * Copyright 2026 The Android Open Source Project
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
package com.google.android.material.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnLayoutChangeListener;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.internal.ContextUtils;

/**
 * Shrinks the vertical background insets of a dialog window while the soft keyboard is shown and
 * the space left above it cannot hold both the configured insets and the dialog content.
 *
 * <p>The vertical insets are only a margin between the dialog and the edges of the screen. While
 * the keyboard takes up part of the screen, the margin is given up before the content is squeezed,
 * so that views such as a text field are not collapsed. The configured insets are restored when the
 * keyboard is hidden. Without the keyboard the insets are never changed, so tall dialogs keep
 * scrolling within the configured margins.
 *
 * <p>The insets are recalculated when the window is laid out and when the window insets report a
 * change of the keyboard visibility. On API 30+, the keyboard visibility is known from the window
 * insets and its height from the window metrics, independently of the size and position of the
 * dialog window, so the adjusted window does not change the input of the next adjustment. Below API
 * 30, the keyboard is detected from the visible display frame when the window is laid out, e.g.
 * when a window using adjustResize is resized for it. Hiding the keyboard may not lay out a window
 * that already fits, so the configured insets are then restored on its next layout pass; as the
 * insets are transparent, this does not change how a dialog that fits the screen looks. Windows
 * using adjustPan are reliably adjusted on API 30+ only.
 *
 * <p>A window background set by the caller after the dialog is created is kept: once the background
 * of the window is no longer the one applied here, the insets are no longer adjusted.
 */
final class VerticalBackgroundInsetsAdjuster implements OnLayoutChangeListener {

  @NonNull private final Dialog dialog;
  @NonNull private final Window window;
  @Nullable private final Drawable background;
  @NonNull private final Rect configuredInsets;
  @NonNull private final Rect currentInsets;
  @NonNull private final Rect visibleFrame = new Rect();
  @NonNull private final Rect activityFrame = new Rect();
  @Nullable private Drawable appliedBackground;
  private boolean lastImeVisible;
  private boolean stopped;

  VerticalBackgroundInsetsAdjuster(
      @NonNull Dialog dialog,
      @NonNull Window window,
      @Nullable Drawable background,
      @NonNull Rect configuredInsets) {
    this.dialog = dialog;
    this.window = window;
    this.background = background;
    this.configuredInsets = new Rect(configuredInsets);
    this.currentInsets = new Rect(configuredInsets);
  }

  /**
   * Applies the configured insets to the window and starts adjusting them on layout changes and on
   * changes of the keyboard visibility.
   */
  void install() {
    applyInsets();
    View decorView = window.getDecorView();
    decorView.addOnLayoutChangeListener(this);
    // Showing or hiding the keyboard does not always lay out the window: a window using
    // adjustPan is not resized, and a window that fits the screen is only moved. Request a layout
    // pass when the keyboard visibility changes; the default handling of the insets is kept.
    ViewCompat.setOnApplyWindowInsetsListener(
        decorView,
        (view, insets) -> {
          WindowInsetsCompat result = ViewCompat.onApplyWindowInsets(view, insets);
          if (stopped) {
            return result;
          }
          getVisibleFrame(visibleFrame);
          boolean imeVisible = isImeVisible(view, visibleFrame);
          if (imeVisible != lastImeVisible) {
            lastImeVisible = imeVisible;
            view.requestLayout();
          }
          return result;
        });
  }

  @Override
  public void onLayoutChange(
      @NonNull View decorView,
      int left,
      int top,
      int right,
      int bottom,
      int oldLeft,
      int oldTop,
      int oldRight,
      int oldBottom) {
    if (stopped) {
      return;
    }
    if (!isAppliedBackground(decorView.getBackground())) {
      stopped = true;
      decorView.removeOnLayoutChangeListener(this);
      return;
    }
    int[] vertical = {configuredInsets.top, configuredInsets.bottom};
    View content = decorView.findViewById(android.R.id.content);
    getVisibleFrame(visibleFrame);
    if (content != null && content.getWidth() > 0 && isImeVisible(decorView, visibleFrame)) {
      // Vertical padding of the decor view that does not come from the background insets.
      int otherVerticalPadding =
          Math.max(
              decorView.getPaddingTop()
                  + decorView.getPaddingBottom()
                  - currentInsets.top
                  - currentInsets.bottom,
              0);
      int maxContentHeight = getKeyboardTop(visibleFrame) - visibleFrame.top - otherVerticalPadding;
      if (maxContentHeight > 0) {
        int spareHeight = maxContentHeight - measureDesiredHeight(content, maxContentHeight);
        vertical =
            calculateVerticalInsets(configuredInsets.top, configuredInsets.bottom, spareHeight);
      }
    }

    if (vertical[0] != currentInsets.top || vertical[1] != currentInsets.bottom) {
      currentInsets.top = vertical[0];
      currentInsets.bottom = vertical[1];
      updateInsets();
    }
  }

  /**
   * Returns the top edge of the soft keyboard in screen coordinates.
   *
   * <p>The result must not depend on the size or position of the dialog window, which change with
   * the insets: a dialog that shrinks to fit above the keyboard no longer overlaps it, so values
   * derived from the window (its visible display frame or its IME insets) would report that the
   * keyboard is gone, the insets would be restored and the dialog would oscillate. On API 30+, the
   * keyboard height is therefore taken from the metrics of the whole window area, which do not
   * depend on the dialog. Below API 30, the bottom of the frame from {@link #getVisibleFrame} is
   * used.
   */
  private int getKeyboardTop(@NonNull Rect visibleFrame) {
    if (VERSION.SDK_INT >= VERSION_CODES.R) {
      WindowMetrics metrics = window.getWindowManager().getCurrentWindowMetrics();
      int imeHeight = metrics.getWindowInsets().getInsets(WindowInsets.Type.ime()).bottom;
      if (imeHeight > 0) {
        return metrics.getBounds().bottom - imeHeight;
      }
    }
    return visibleFrame.bottom;
  }

  /**
   * Gets the visible display frame used to detect the keyboard below API 30.
   *
   * <p>The visible display frame of a window only excludes the part of the keyboard that overlaps
   * the window, so the frame of the dialog window depends on the insets, see {@link
   * #getKeyboardTop}. The window of the activity that shows the dialog covers the area of the
   * keyboard, but it may be notified of the keyboard after the dialog window is laid out. The frame
   * is therefore the frame of the dialog window, limited by the frame of the activity window.
   */
  private void getVisibleFrame(@NonNull Rect outFrame) {
    window.getDecorView().getWindowVisibleDisplayFrame(outFrame);
    if (VERSION.SDK_INT >= VERSION_CODES.R) {
      return;
    }
    Activity activity = ContextUtils.getActivity(dialog.getContext());
    Window activityWindow = activity != null ? activity.getWindow() : null;
    View activityDecorView = activityWindow != null ? activityWindow.peekDecorView() : null;
    if (activityDecorView != null && activityDecorView.isAttachedToWindow()) {
      activityDecorView.getWindowVisibleDisplayFrame(activityFrame);
      if (!activityFrame.isEmpty() && activityFrame.bottom < outFrame.bottom) {
        outFrame.bottom = activityFrame.bottom;
      }
    }
  }

  /**
   * Returns whether the soft keyboard is shown.
   *
   * <p>On API 30+, the visibility is reported by the window insets. Below API 30, {@link
   * WindowInsetsCompat} infers it from the bottom system window inset, which stays zero for a
   * dialog window that is resized for the keyboard. There, the configured screen height, which
   * excludes the system bars but not the keyboard, is compared with the visible display frame,
   * which also excludes the keyboard. Without the keyboard they only differ by the rounding of the
   * screen height to whole dp. The visible display frame is empty until the window is attached.
   */
  private static boolean isImeVisible(@NonNull View decorView, @NonNull Rect visibleFrame) {
    if (VERSION.SDK_INT >= VERSION_CODES.R) {
      WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(decorView);
      return insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
    }
    if (visibleFrame.isEmpty()) {
      return false;
    }
    Resources resources = decorView.getResources();
    float density = resources.getDisplayMetrics().density;
    float screenHeight = resources.getConfiguration().screenHeightDp * density;
    return screenHeight - visibleFrame.height() > density;
  }

  /**
   * Returns the top and bottom insets that fit into {@code spareHeight}, never exceeding the
   * configured values and keeping their ratio.
   */
  @VisibleForTesting
  @NonNull
  static int[] calculateVerticalInsets(int configuredTop, int configuredBottom, int spareHeight) {
    int configuredTotal = configuredTop + configuredBottom;
    if (spareHeight >= configuredTotal) {
      return new int[] {configuredTop, configuredBottom};
    }
    if (spareHeight <= 0) {
      return new int[] {0, 0};
    }
    int top = (int) ((long) configuredTop * spareHeight / configuredTotal);
    return new int[] {top, spareHeight - top};
  }

  /**
   * Measures the height the content wants for its current width, up to {@code maxHeight}, then
   * restores its previous measurement.
   *
   * <p>An {@link MeasureSpec#AT_MOST} spec is used on purpose: with {@link
   * MeasureSpec#UNSPECIFIED}, {@code AlertDialogLayout} only accounts for the minimum height of the
   * button panel, which underestimates the height of stacked buttons.
   */
  private static int measureDesiredHeight(@NonNull View content, int maxHeight) {
    int width = content.getWidth();
    int height = content.getHeight();
    content.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(Math.max(maxHeight, 0), MeasureSpec.AT_MOST));
    int desiredHeight = content.getMeasuredHeight();
    content.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    return desiredHeight;
  }

  /**
   * Returns whether {@code drawable} is the background applied by {@link #applyInsets}. The decor
   * view may wrap the window background, e.g. in a {@link LayerDrawable} for a background blur on
   * API 31+.
   */
  private boolean isAppliedBackground(@Nullable Drawable drawable) {
    if (drawable == null) {
      return false;
    }
    if (drawable == appliedBackground) {
      return true;
    }
    if (drawable instanceof DrawableWrapper) {
      return isAppliedBackground(((DrawableWrapper) drawable).getDrawable());
    }
    if (drawable instanceof LayerDrawable) {
      LayerDrawable layers = (LayerDrawable) drawable;
      for (int i = 0; i < layers.getNumberOfLayers(); i++) {
        if (isAppliedBackground(layers.getDrawable(i))) {
          return true;
        }
      }
    }
    return false;
  }

  private void applyInsets() {
    Rect insets = new Rect(currentInsets);
    appliedBackground = MaterialDialogs.insetDrawable(background, insets);
    window.setBackgroundDrawable(appliedBackground);
    window.getDecorView().setOnTouchListener(new InsetDialogOnTouchListener(dialog, insets));
  }

  /**
   * Applies the current insets to a shown window and lets the window manager recalculate the size
   * of the window, which depends on the insets. A window using adjustPan is not resized for the
   * keyboard, so without this it would keep its previous size and pan offset.
   */
  private void updateInsets() {
    applyInsets();
    View decorView = window.getDecorView();
    decorView.post(
        () -> {
          if (dialog.isShowing() && decorView.isAttachedToWindow()) {
            window.setAttributes(window.getAttributes());
          }
        });
  }
}
