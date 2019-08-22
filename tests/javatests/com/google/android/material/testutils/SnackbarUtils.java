/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.google.android.material.testutils;

import android.os.SystemClock;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {
  public interface TransientBottomBarAction {
    void perform() throws Throwable;
  }

  private static class TransientBottomBarCallback<B extends BaseTransientBottomBar<B>>
      extends BaseTransientBottomBar.BaseCallback<B> {

    private boolean shown = false;
    private boolean dismissed = false;

    @Override
    public void onShown(B transientBottomBar) {
      shown = true;
    }

    @Override
    public void onDismissed(B transientBottomBar, @DismissEvent int event) {
      dismissed = true;
    }
  }

  private static final int SLEEP_MILLIS = 250;

  /**
   * Helper method that shows that specified {@link Snackbar} and waits until it has been fully
   * shown.
   */
  public static <B extends BaseTransientBottomBar<B>>
      void showTransientBottomBarAndWaitUntilFullyShown(@NonNull B transientBottomBar) {
    if (transientBottomBar.isShown()) {
      return;
    }
    TransientBottomBarCallback<B> callback = new TransientBottomBarCallback<>();
    transientBottomBar.addCallback(callback);
    transientBottomBar.show();
    waitForCallbackShown(callback);
  }

  /** Helper method that waits until the given bar has been fully shown. */
  public static <B extends BaseTransientBottomBar<B>> void waitUntilFullyShown(
      @NonNull B transientBottomBar) {
    if (transientBottomBar.isShown()) {
      return;
    }
    TransientBottomBarCallback<B> callback = new TransientBottomBarCallback<>();
    transientBottomBar.addCallback(callback);
    waitForCallbackShown(callback);
  }

  /**
   * Helper method that dismissed that specified {@link Snackbar} and waits until it has been fully
   * dismissed.
   */
  public static <B extends BaseTransientBottomBar<B>>
      void dismissTransientBottomBarAndWaitUntilFullyDismissed(@NonNull final B transientBottomBar)
          throws Throwable {
    performActionAndWaitUntilFullyDismissed(transientBottomBar, transientBottomBar::dismiss);
  }

  /**
   * Helper method that dismissed that specified {@link Snackbar} and waits until it has been fully
   * dismissed.
   */
  public static <B extends BaseTransientBottomBar<B>> void performActionAndWaitUntilFullyDismissed(
      @NonNull B transientBottomBar, @NonNull TransientBottomBarAction action) throws Throwable {
    if (!transientBottomBar.isShown()) {
      return;
    }
    TransientBottomBarCallback<B> callback = new TransientBottomBarCallback<>();
    transientBottomBar.addCallback(callback);
    action.perform();
    waitForCallbackDismissed(callback);
  }

  /** Helper method that waits until the given bar has been fully dismissed. */
  public static <B extends BaseTransientBottomBar<B>> void waitUntilFullyDismissed(
      @NonNull B transientBottomBar) {
    if (!transientBottomBar.isShown()) {
      return;
    }
    TransientBottomBarCallback<B> callback = new TransientBottomBarCallback<>();
    transientBottomBar.addCallback(callback);
    waitForCallbackDismissed(callback);
  }

  private static <B extends BaseTransientBottomBar<B>> void waitForCallbackShown(
      TransientBottomBarCallback<B> transientBottomBarCallback) {
    waitForCallback(transientBottomBarCallback, true);
  }

  private static <B extends BaseTransientBottomBar<B>> void waitForCallbackDismissed(
      TransientBottomBarCallback<B> transientBottomBarCallback) {
    waitForCallback(transientBottomBarCallback, false);
  }

  private static <B extends BaseTransientBottomBar<B>> void waitForCallback(
      TransientBottomBarCallback<B> transientBottomBarCallback, boolean waitForShown) {
    while ((waitForShown && !transientBottomBarCallback.shown)
        || (!waitForShown && !transientBottomBarCallback.dismissed)) {
      SystemClock.sleep(SLEEP_MILLIS);
    }
    SystemClock.sleep(SLEEP_MILLIS);
  }
}
