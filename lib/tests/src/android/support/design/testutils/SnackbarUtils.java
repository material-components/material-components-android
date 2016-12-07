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

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import org.hamcrest.Matcher;

import static android.support.design.testutils.TestUtilsActions.waitUntilIdle;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class SnackbarUtils {
    private static class SnackbarShownCallback extends Snackbar.Callback
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
            return "Snackbar shown callback";
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
        public void onShown(Snackbar snackbar) {
            mIsShown = true;
            if (mCallback != null) {
                mCallback.onTransitionToIdle();
            }
        }
    }

    private static class SnackbarDismissedCallback extends Snackbar.Callback
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
            return "Snackbar dismissed callback";
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
        public void onDismissed(Snackbar snackbar, @DismissEvent int event) {
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
    public static void showSnackbarAndWaitUntilFullyShown(Snackbar snackbar) {
        SnackbarShownCallback snackbarCallback = new SnackbarShownCallback();
        snackbar.setCallback(snackbarCallback);
        try {
            // Register our listener as idling resource so that Espresso waits until the
            // the snackbar has been fully shown
            Espresso.registerIdlingResources(snackbarCallback);
            // Show the snackbar
            snackbar.show();
            // Mark the callback to require waiting for idle state
            snackbarCallback.mNeedsIdle = true;
            // Perform a dummy Espresso action that loops until the UI thread is idle. This
            // effectively blocks us until the Snackbar has completed its sliding animation.
            onView(isRoot()).perform(waitUntilIdle());
            snackbarCallback.mNeedsIdle = false;
        } finally {
            // Unregister our idling resource
            Espresso.unregisterIdlingResources(snackbarCallback);
            // And remove our tracker listener from Snackbar
            snackbar.setCallback(null);
        }
    }

    /**
     * Helper method that dismissed that specified {@link Snackbar} and waits until
     * it has been fully dismissed. Note that calling this method will reset the currently
     * set {@link Snackbar.Callback}.
     */
    public static void dismissSnackbarAndWaitUntilFullyDismissed(Snackbar snackbar) {
        SnackbarDismissedCallback snackbarCallback = new SnackbarDismissedCallback();
        snackbar.setCallback(snackbarCallback);
        try {
            // Register our listener as idling resource so that Espresso waits until the
            // the snackbar has been fully dismissed
            Espresso.registerIdlingResources(snackbarCallback);
            // Dismiss the snackbar
            snackbar.dismiss();
            // Mark the callback to require waiting for idle state
            snackbarCallback.mNeedsIdle = true;
            // Perform a dummy Espresso action that loops until the UI thread is idle. This
            // effectively blocks us until the Snackbar has completed its sliding animation.
            onView(isRoot()).perform(waitUntilIdle());
            snackbarCallback.mNeedsIdle = false;
        } finally {
            // Unregister our idling resource
            Espresso.unregisterIdlingResources(snackbarCallback);
            // And remove our tracker listener from Snackbar
            snackbar.setCallback(null);
        }
    }

    /**
     * Helper method that waits until the given Snackbar has been fully dismissed. Note that
     * calling this method will reset the currently set {@link Snackbar.Callback}.
     */
    public static void waitUntilFullyDismissed(Snackbar snackbar) {
        SnackbarDismissedCallback snackbarCallback = new SnackbarDismissedCallback();
        snackbar.setCallback(snackbarCallback);
        try {
            // Register our listener as idling resource so that Espresso waits until the
            // the snackbar has been fully dismissed
            Espresso.registerIdlingResources(snackbarCallback);
            // Mark the callback to require waiting for idle state
            snackbarCallback.mNeedsIdle = true;
            // Perform a dummy Espresso action that loops until the UI thread is idle. This
            // effectively blocks us until the Snackbar has completed its sliding animation.
            onView(isRoot()).perform(waitUntilIdle());
            snackbarCallback.mNeedsIdle = false;
        } finally {
            // Unregister our idling resource
            Espresso.unregisterIdlingResources(snackbarCallback);
            // And remove our tracker listener from Snackbar
            snackbar.setCallback(null);
        }
    }
}
