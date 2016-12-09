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
package android.support.design.testutils;

import static android.support.design.testutils.TestUtilsActions.waitUntilIdle;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;

public class SnackbarUtils {
    private static class TransientBottomBarShownCallback
            extends BaseTransientBottomBar.BaseCallback<BaseTransientBottomBar>
            implements IdlingResource {
        private boolean mIsShown = false;

        @Nullable
        private IdlingResource.ResourceCallback mCallback;

        private boolean mNeedsIdle = false;

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            mCallback = resourceCallback;
        }

        @Override
        public String getName() {
            return "Transient bottom bar shown callback";
        }

        @Override
        public boolean isIdleNow() {
            if (!mNeedsIdle) {
                return true;
            } else {
                return mIsShown;
            }
        }

        @Override
        public void onShown(BaseTransientBottomBar transientBottomBar) {
            mIsShown = true;
            if (mCallback != null) {
                mCallback.onTransitionToIdle();
            }
        }
    }

    private static class TransientBottomBarDismissedCallback
            extends BaseTransientBottomBar.BaseCallback<BaseTransientBottomBar>
            implements IdlingResource {
        private boolean mIsDismissed = false;

        @Nullable
        private IdlingResource.ResourceCallback mCallback;

        private boolean mNeedsIdle = false;

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            mCallback = resourceCallback;
        }

        @Override
        public String getName() {
            return "Transient bottom bar dismissed callback";
        }

        @Override
        public boolean isIdleNow() {
            if (!mNeedsIdle) {
                return true;
            } else {
                return mIsDismissed;
            }
        }

        @Override
        public void onDismissed(BaseTransientBottomBar transientBottomBar,
                @DismissEvent int event) {
            mIsDismissed = true;
            if (mCallback != null) {
                mCallback.onTransitionToIdle();
            }
        }
    }

    /**
     * Helper method that shows that specified {@link Snackbar} and waits until
     * it has been fully shown. Note that calling this method will reset the currently
     * set {@link Snackbar.Callback}.
     */
    public static void showTransientBottomBarAndWaitUntilFullyShown(
            BaseTransientBottomBar transientBottomBar) {
        TransientBottomBarShownCallback callback = new TransientBottomBarShownCallback();
        transientBottomBar.setCallback(callback);
        try {
            // Register our listener as idling resource so that Espresso waits until the
            // the bar has been fully shown
            Espresso.registerIdlingResources(callback);
            // Show the bar
            transientBottomBar.show();
            // Mark the callback to require waiting for idle state
            callback.mNeedsIdle = true;
            // Perform a dummy Espresso action that loops until the UI thread is idle. This
            // effectively blocks us until the Snackbar has completed its sliding animation.
            onView(isRoot()).perform(waitUntilIdle());
            callback.mNeedsIdle = false;
        } finally {
            // Unregister our idling resource
            Espresso.unregisterIdlingResources(callback);
            // And remove our tracker listener from Snackbar
            transientBottomBar.setCallback(null);
        }
    }

    /**
     * Helper method that dismissed that specified {@link Snackbar} and waits until
     * it has been fully dismissed. Note that calling this method will reset the currently
     * set {@link Snackbar.Callback}.
     */
    public static void dismissTransientBottomBarAndWaitUntilFullyDismissed(
            BaseTransientBottomBar transientBottomBar) {
        TransientBottomBarDismissedCallback callback = new TransientBottomBarDismissedCallback();
        transientBottomBar.setCallback(callback);
        try {
            // Register our listener as idling resource so that Espresso waits until the
            // the bar has been fully dismissed
            Espresso.registerIdlingResources(callback);
            // Dismiss the bar
            transientBottomBar.dismiss();
            // Mark the callback to require waiting for idle state
            callback.mNeedsIdle = true;
            // Perform a dummy Espresso action that loops until the UI thread is idle. This
            // effectively blocks us until the Snackbar has completed its sliding animation.
            onView(isRoot()).perform(waitUntilIdle());
            callback.mNeedsIdle = false;
        } finally {
            // Unregister our idling resource
            Espresso.unregisterIdlingResources(callback);
            // And remove our tracker listener from Snackbar
            transientBottomBar.setCallback(null);
        }
    }

    /**
     * Helper method that waits until the given bar has been fully dismissed. Note that
     * calling this method will reset the currently set {@link Snackbar.Callback}.
     */
    public static void waitUntilFullyDismissed(BaseTransientBottomBar transientBottomBar) {
        TransientBottomBarDismissedCallback callback = new TransientBottomBarDismissedCallback();
        transientBottomBar.setCallback(callback);
        try {
            // Register our listener as idling resource so that Espresso waits until the
            // the bar has been fully dismissed
            Espresso.registerIdlingResources(callback);
            // Mark the callback to require waiting for idle state
            callback.mNeedsIdle = true;
            // Perform a dummy Espresso action that loops until the UI thread is idle. This
            // effectively blocks us until the Snackbar has completed its sliding animation.
            onView(isRoot()).perform(waitUntilIdle());
            callback.mNeedsIdle = false;
        } finally {
            // Unregister our idling resource
            Espresso.unregisterIdlingResources(callback);
            // And remove our tracker listener from Snackbar
            transientBottomBar.setCallback(null);
        }
    }
}
