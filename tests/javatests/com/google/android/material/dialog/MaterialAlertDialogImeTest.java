/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.google.android.material.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager.LayoutParams;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.MaterialAlertDialogActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests of the vertical background insets of a dialog created with {@link
 * MaterialAlertDialogBuilder} while the soft keyboard is shown.
 *
 * <p>The tests need a soft keyboard and are skipped when none is shown. To make the dialog content
 * conflict with the keyboard on any screen, the configured insets are a quarter of the window
 * height each. The keyboard visibility is read from the window metrics, available on API 30+.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 30)
public class MaterialAlertDialogImeTest {

  private static final long KEYBOARD_TIMEOUT_MS = 5000;
  private static final long SETTLE_TIMEOUT_MS = 5000;
  private static final long QUIET_PERIOD_MS = 1000;

  @Rule
  public final ActivityTestRule<MaterialAlertDialogActivity> activityTestRule =
      new ActivityTestRule<>(MaterialAlertDialogActivity.class);

  private final AtomicReference<AlertDialog> dialog = new AtomicReference<>();
  private final AtomicInteger decorLayouts = new AtomicInteger();

  @After
  public void dismissDialog() throws Throwable {
    activityTestRule.runOnUiThread(() -> {
      if (dialog.get() != null) {
        dialog.get().dismiss();
      }
    });
  }

  @Test
  public void adjustResize_keyboardShown_textFieldIsNotCollapsed() throws Throwable {
    EditText field = showDialogWithTextFields(1, LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    waitForKeyboard();
    waitUntilSettled();

    assertTrue("text field collapsed", field.getHeight() > 0);
    assertTrue("text field covered by the keyboard", bottomOnScreen(field) <= keyboardTop());
  }

  @Test
  public void adjustPan_keyboardShown_dialogFitsAboveKeyboardAndSettles() throws Throwable {
    EditText field = showDialogWithTextFields(2, LayoutParams.SOFT_INPUT_ADJUST_PAN);
    waitForKeyboard();
    waitUntilSettled();

    decorLayouts.set(0);
    SystemClock.sleep(QUIET_PERIOD_MS);
    assertEquals("dialog layout does not settle", 0, decorLayouts.get());
    assertTrue("text field collapsed", field.getHeight() > 0);
    assertTrue(
        "dialog covered by the keyboard",
        bottomOnScreen(dialog.get().getWindow().getDecorView()) <= keyboardTop());
  }

  @Test
  public void keyboardHidden_configuredInsetsAreRestored() throws Throwable {
    showDialogWithTextFields(2, LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    waitForKeyboard();
    waitUntilSettled();
    View decorView = dialog.get().getWindow().getDecorView();
    int configuredPadding = configuredInset();
    assertTrue("insets not shrunk", decorView.getPaddingTop() < configuredPadding);

    activityTestRule.runOnUiThread(() ->
        WindowCompat.getInsetsController(dialog.get().getWindow(), decorView)
            .hide(WindowInsetsCompat.Type.ime()));
    waitFor(() -> keyboardTop() == Integer.MAX_VALUE, KEYBOARD_TIMEOUT_MS, "keyboard not hidden");
    waitFor(
        () -> decorView.getPaddingTop() == configuredPadding,
        SETTLE_TIMEOUT_MS,
        "insets not restored");
  }

  @Test
  public void backgroundSetAfterCreate_keyboardShown_backgroundIsKept() throws Throwable {
    Drawable background = new ColorDrawable(Color.WHITE);
    showDialogWithTextFields(2, LayoutParams.SOFT_INPUT_ADJUST_RESIZE, background);
    waitForKeyboard();
    waitUntilSettled();

    assertSame(
        "background replaced", background, dialog.get().getWindow().getDecorView().getBackground());
  }

  /** Shows a dialog with text fields and the keyboard; returns the first, focused field. */
  private EditText showDialogWithTextFields(int count, int softInputMode) throws Throwable {
    return showDialogWithTextFields(count, softInputMode, null);
  }

  /**
   * Shows a dialog with text fields and the keyboard, with {@code background} set as the window
   * background after the dialog is created, if not null; returns the first, focused field.
   */
  private EditText showDialogWithTextFields(
      int count, int softInputMode, @Nullable Drawable background) throws Throwable {
    AtomicReference<EditText> first = new AtomicReference<>();
    activityTestRule.runOnUiThread(() -> {
      MaterialAlertDialogActivity activity = activityTestRule.getActivity();
      LinearLayout content = new LinearLayout(activity);
      content.setOrientation(LinearLayout.VERTICAL);
      for (int i = 0; i < count; i++) {
        TextInputLayout layout = new TextInputLayout(activity);
        layout.setHint("Field " + (i + 1));
        TextInputEditText editText = new TextInputEditText(layout.getContext());
        layout.addView(editText);
        content.addView(layout);
        if (i == 0) {
          first.set(editText);
        }
      }
      int inset = configuredInset();
      AlertDialog alertDialog =
          new MaterialAlertDialogBuilder(activity)
              .setTitle("Title")
              .setView(content)
              .setPositiveButton("OK", null)
              .setNegativeButton("Cancel", null)
              .setBackgroundInsetTop(inset)
              .setBackgroundInsetBottom(inset)
              .create();
      Window window = alertDialog.getWindow();
      if (background != null) {
        window.setBackgroundDrawable(background);
      }
      window.setSoftInputMode(softInputMode);
      dialog.set(alertDialog);
      alertDialog.show();
      window
          .getDecorView()
          .addOnLayoutChangeListener(
              (v, l, t, r, b, oldL, oldT, oldR, oldB) -> decorLayouts.incrementAndGet());
      first.get().requestFocus();
      WindowCompat.getInsetsController(window, first.get()).show(WindowInsetsCompat.Type.ime());
    });
    return first.get();
  }

  private int configuredInset() {
    return windowMetrics().getBounds().height() / 4;
  }

  private void waitForKeyboard() {
    long deadline = SystemClock.uptimeMillis() + KEYBOARD_TIMEOUT_MS;
    while (keyboardTop() == Integer.MAX_VALUE && SystemClock.uptimeMillis() < deadline) {
      SystemClock.sleep(50);
    }
    assumeTrue("no soft keyboard shown", keyboardTop() != Integer.MAX_VALUE);
  }

  /**
   * Waits until the dialog has not been laid out for a while. Does not wait for the main thread to
   * become idle: a layout that does not settle would block such a wait forever.
   */
  private void waitUntilSettled() {
    long deadline = SystemClock.uptimeMillis() + SETTLE_TIMEOUT_MS;
    int last = -1;
    while (SystemClock.uptimeMillis() < deadline) {
      int current = decorLayouts.get();
      if (current == last) {
        return;
      }
      last = current;
      SystemClock.sleep(300);
    }
  }

  /** Returns the top of the keyboard in screen coordinates, or MAX_VALUE without the keyboard. */
  private int keyboardTop() {
    WindowMetrics metrics = windowMetrics();
    WindowInsets insets = metrics.getWindowInsets();
    if (!insets.isVisible(WindowInsets.Type.ime())) {
      return Integer.MAX_VALUE;
    }
    return metrics.getBounds().bottom - insets.getInsets(WindowInsets.Type.ime()).bottom;
  }

  private WindowMetrics windowMetrics() {
    return activityTestRule.getActivity().getWindowManager().getCurrentWindowMetrics();
  }

  private static int bottomOnScreen(View view) {
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    return location[1] + view.getHeight();
  }

  private static void waitFor(Condition condition, long timeoutMs, String message) {
    long deadline = SystemClock.uptimeMillis() + timeoutMs;
    while (!condition.isMet() && SystemClock.uptimeMillis() < deadline) {
      SystemClock.sleep(50);
    }
    assertTrue(message, condition.isMet());
  }

  private interface Condition {
    boolean isMet();
  }
}
