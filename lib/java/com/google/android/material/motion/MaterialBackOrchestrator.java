/*
 * Copyright 2023 The Android Open Source Project
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
package com.google.android.material.motion;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.activity.BackEventCompat;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

/**
 * Utility class for views that support back handling via {@link MaterialBackHandler} which helps
 * with adding and removing back callbacks on API Level 33+.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class MaterialBackOrchestrator {

  @Nullable private final BackCallbackDelegate backCallbackDelegate = createBackCallbackDelegate();
  @NonNull private final MaterialBackHandler backHandler;
  @NonNull private final View view;

  public <T extends View & MaterialBackHandler> MaterialBackOrchestrator(
      @NonNull T backHandlerView) {
    this(backHandlerView, backHandlerView);
  }

  public MaterialBackOrchestrator(@NonNull MaterialBackHandler backHandler, @NonNull View view) {
    this.backHandler = backHandler;
    this.view = view;
  }

  public boolean shouldListenForBackCallbacks() {
    return backCallbackDelegate != null;
  }

  /**
   * Starts listening for back events with {@link OnBackInvokedDispatcher#PRIORITY_OVERLAY} on API
   * Level 33+.
   */
  public void startListeningForBackCallbacksWithPriorityOverlay() {
    startListeningForBackCallbacks(/* priorityOverlay= */ true);
  }

  /**
   * Starts listening for back events with {@link OnBackInvokedDispatcher#PRIORITY_DEFAULT} on API
   * Level 33+.
   */
  public void startListeningForBackCallbacks() {
    startListeningForBackCallbacks(/* priorityOverlay= */ false);
  }

  /**
   * Starts listening for back events on API Level 33+.
   *
   * <p>Note that this is just when we start listening for potential back gestures, not when the
   * swipe back gesture starts.
   *
   * @param priorityOverlay whether {@link OnBackInvokedDispatcher#PRIORITY_OVERLAY} should be used
   */
  private void startListeningForBackCallbacks(boolean priorityOverlay) {
    if (backCallbackDelegate != null) {
      backCallbackDelegate.startListeningForBackCallbacks(backHandler, view, priorityOverlay);
    }
  }

  /** Stops listening for back events on API Level 33+. */
  public void stopListeningForBackCallbacks() {
    if (backCallbackDelegate != null) {
      backCallbackDelegate.stopListeningForBackCallbacks(view);
    }
  }

  @Nullable
  private static BackCallbackDelegate createBackCallbackDelegate() {
    if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
      return new Api34BackCallbackDelegate();
    } else if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      return new Api33BackCallbackDelegate();
    } else {
      return null;
    }
  }

  private interface BackCallbackDelegate {
    void startListeningForBackCallbacks(
        @NonNull MaterialBackHandler backHandler, @NonNull View view, boolean priorityOverlay);

    void stopListeningForBackCallbacks(@NonNull View view);
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  private static class Api34BackCallbackDelegate extends Api33BackCallbackDelegate {

    @SuppressWarnings("Override")
    @Override
    OnBackInvokedCallback createOnBackInvokedCallback(@NonNull MaterialBackHandler backHandler) {
      return new OnBackAnimationCallback() {

        @Override
        public void onBackStarted(@NonNull BackEvent backEvent) {
          if (!isListeningForBackCallbacks()) {
            return;
          }
          backHandler.startBackProgress(new BackEventCompat(backEvent));
        }

        @Override
        public void onBackProgressed(@NonNull BackEvent backEvent) {
          if (!isListeningForBackCallbacks()) {
            return;
          }
          backHandler.updateBackProgress(new BackEventCompat(backEvent));
        }

        @Override
        public void onBackInvoked() {
          backHandler.handleBackInvoked();
        }

        @Override
        public void onBackCancelled() {
          if (!isListeningForBackCallbacks()) {
            return;
          }
          backHandler.cancelBackProgress();
        }
      };
    }
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  private static class Api33BackCallbackDelegate implements BackCallbackDelegate {

    @Nullable private OnBackInvokedCallback onBackInvokedCallback;

    boolean isListeningForBackCallbacks() {
      return onBackInvokedCallback != null;
    }

    @DoNotInline
    @Override
    public void startListeningForBackCallbacks(
        @NonNull MaterialBackHandler backHandler, @NonNull View view, boolean priorityOverlay) {
      if (onBackInvokedCallback != null) {
        return;
      }

      OnBackInvokedDispatcher onBackInvokedDispatcher = view.findOnBackInvokedDispatcher();
      if (onBackInvokedDispatcher == null) {
        return;
      }

      onBackInvokedCallback = createOnBackInvokedCallback(backHandler);
      int priority =
          priorityOverlay
              ? OnBackInvokedDispatcher.PRIORITY_OVERLAY
              : OnBackInvokedDispatcher.PRIORITY_DEFAULT;
      onBackInvokedDispatcher.registerOnBackInvokedCallback(priority, onBackInvokedCallback);
    }

    @DoNotInline
    @Override
    public void stopListeningForBackCallbacks(@NonNull View view) {
      if (onBackInvokedCallback == null) {
        return;
      }
      OnBackInvokedDispatcher onBackInvokedDispatcher = view.findOnBackInvokedDispatcher();
      if (onBackInvokedDispatcher == null) {
        return;
      }
      onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCallback);
      onBackInvokedCallback = null;
    }

    OnBackInvokedCallback createOnBackInvokedCallback(@NonNull MaterialBackHandler backHandler) {
      return backHandler::handleBackInvoked;
    }
  }
}
