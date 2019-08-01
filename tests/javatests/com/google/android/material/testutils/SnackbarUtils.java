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

import static com.google.android.material.testutils.TestUtilsActions.waitUntilIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;

public class SnackbarUtils {
  public interface TransientBottomBarAction {
    void perform() throws Throwable;
  }

  private static class TransientBottomBarShownCallback<B extends BaseTransientBottomBar<B>>
      extends BaseTransientBottomBar.BaseCallback<B> implements IdlingResource {
    private boolean isShown = false;

    @Nullable private IdlingResource.ResourceCallback callback;

    private boolean needsIdle = false;

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
      callback = resourceCallback;
    }

    @Override
    public String getName() {
      return "Transient bottom bar shown callback";
    }

    @Override
    public boolean isIdleNow() {
      if (!needsIdle) {
        return true;
      } else {
        return isShown;
      }
    }

    @Override
    public void onShown(B transientBottomBar) {
      isShown = true;
      if (callback != null) {
        callback.onTransitionToIdle();
      }
    }
  }

  private static class TransientBottomBarDismissedCallback<B extends BaseTransientBottomBar<B>>
      extends BaseTransientBottomBar.BaseCallback<B> implements IdlingResource {
    private boolean isDismissed = false;

    @Nullable private IdlingResource.ResourceCallback callback;

    private boolean needsIdle = false;

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
      callback = resourceCallback;
    }

    @Override
    public String getName() {
      return "Transient bottom bar dismissed callback";
    }

    @Override
    public boolean isIdleNow() {
      if (!needsIdle) {
        return true;
      } else {
        return isDismissed;
      }
    }

    @Override
    public void onDismissed(B transientBottomBar, @DismissEvent int event) {
      isDismissed = true;
      if (callback != null) {
        callback.onTransitionToIdle();
      }
    }
  }

  /**
   * Helper method that shows that specified {@link Snackbar} and waits until it has been fully
   * shown.
   */
  public static <B extends BaseTransientBottomBar<B>>
      void showTransientBottomBarAndWaitUntilFullyShown(@NonNull B transientBottomBar) {
    TransientBottomBarShownCallback<B> callback = new TransientBottomBarShownCallback<B>();
    transientBottomBar.addCallback(callback);
    try {
      // Register our listener as idling resource so that Espresso waits until the
      // the bar has been fully shown
      Espresso.registerIdlingResources(callback);
      // Show the bar
      transientBottomBar.show();
      // Mark the callback to require waiting for idle state
      callback.needsIdle = true;
      // Perform a dummy Espresso action that loops until the UI thread is idle. This
      // effectively blocks us until the Snackbar has completed its sliding animation.
      onView(isRoot()).perform(waitUntilIdle());
      callback.needsIdle = false;
    } finally {
      // Unregister our idling resource
      Espresso.unregisterIdlingResources(callback);
      // And remove our tracker listener from Snackbar
      transientBottomBar.removeCallback(callback);
    }
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
    TransientBottomBarDismissedCallback<B> callback = new TransientBottomBarDismissedCallback<B>();
    transientBottomBar.addCallback(callback);
    try {
      // Register our listener as idling resource so that Espresso waits until the
      // the bar has been fully dismissed
      Espresso.registerIdlingResources(callback);
      // Run the action
      action.perform();
      // Mark the callback to require waiting for idle state
      callback.needsIdle = true;
      // Perform a dummy Espresso action that loops until the UI thread is idle. This
      // effectively blocks us until the Snackbar has completed its sliding animation.
      onView(isRoot()).perform(waitUntilIdle());
      callback.needsIdle = false;
    } finally {
      // Unregister our idling resource
      Espresso.unregisterIdlingResources(callback);
      // And remove our tracker listener from Snackbar
      transientBottomBar.removeCallback(null);
    }
  }

  /** Helper method that waits until the given bar has been fully dismissed. */
  public static <B extends BaseTransientBottomBar<B>> void waitUntilFullyDismissed(
      @NonNull B transientBottomBar) {
    TransientBottomBarDismissedCallback<B> callback = new TransientBottomBarDismissedCallback<B>();
    transientBottomBar.addCallback(callback);
    try {
      // Register our listener as idling resource so that Espresso waits until the
      // the bar has been fully dismissed
      Espresso.registerIdlingResources(callback);
      // Mark the callback to require waiting for idle state
      callback.needsIdle = true;
      // Perform a dummy Espresso action that loops until the UI thread is idle. This
      // effectively blocks us until the Snackbar has completed its sliding animation.
      onView(isRoot()).perform(waitUntilIdle());
      callback.needsIdle = false;
    } finally {
      // Unregister our idling resource
      Espresso.unregisterIdlingResources(callback);
      // And remove our tracker listener from Snackbar
      transientBottomBar.removeCallback(null);
    }
  }
}
