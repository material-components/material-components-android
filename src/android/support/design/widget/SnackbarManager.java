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

package android.support.design.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Manages {@link Snackbar}s.
 */
class SnackbarManager {

    private static final int MSG_TIMEOUT = 0;

    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;

    private static SnackbarManager sSnackbarManager;

    static SnackbarManager getInstance() {
        if (sSnackbarManager == null) {
            sSnackbarManager = new SnackbarManager();
        }
        return sSnackbarManager;
    }

    private final Object mLock;
    private final Handler mHandler;

    private SnackbarRecord mCurrentSnackbar;
    private SnackbarRecord mNextSnackbar;

    private SnackbarManager() {
        mLock = new Object();
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_TIMEOUT:
                        handleTimeout((SnackbarRecord) message.obj);
                        return true;
                }
                return false;
            }
        });
    }

    interface Callback {
        void show();
        void dismiss();
    }

    public void show(int duration, Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentSnackbar.duration = duration;

                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
                scheduleTimeoutLocked(mCurrentSnackbar);
                return;
            } else if (isNextSnackbar(callback)) {
                // We'll just update the duration
                mNextSnackbar.duration = duration;
            } else {
                // Else, we need to create a new record and queue it
                mNextSnackbar = new SnackbarRecord(duration, callback);
            }

            if (mCurrentSnackbar != null && cancelSnackbarLocked(mCurrentSnackbar)) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return;
            } else {
                // Clear out the current snackbar
                mCurrentSnackbar = null;
                // Otherwise, just show it now
                showNextSnackbarLocked();
            }
        }
    }

    public void dismiss(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                cancelSnackbarLocked(mCurrentSnackbar);
            }
            if (isNextSnackbar(callback)) {
                cancelSnackbarLocked(mNextSnackbar);
            }
        }
    }

    /**
     * Should be called when a Snackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    public void onDismissed(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentSnackbar = null;
                if (mNextSnackbar != null) {
                    showNextSnackbarLocked();
                }
            }
        }
    }

    /**
     * Should be called when a Snackbar is being shown. This is after any entrance animation has
     * finished.
     */
    public void onShown(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    public void cancelTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
            }
        }
    }

    public void restoreTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    private static class SnackbarRecord {
        private final WeakReference<Callback> callback;
        private int duration;

        SnackbarRecord(int duration, Callback callback) {
            this.callback = new WeakReference<>(callback);
            this.duration = duration;
        }

        boolean isSnackbar(Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }

    private void showNextSnackbarLocked() {
        if (mNextSnackbar != null) {
            mCurrentSnackbar = mNextSnackbar;
            mNextSnackbar = null;

            final Callback callback = mCurrentSnackbar.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentSnackbar = null;
            }
        }
    }

    private boolean cancelSnackbarLocked(SnackbarRecord record) {
        final Callback callback = record.callback.get();
        if (callback != null) {
            callback.dismiss();
            return true;
        }
        return false;
    }

    private boolean isCurrentSnackbar(Callback callback) {
        return mCurrentSnackbar != null && mCurrentSnackbar.isSnackbar(callback);
    }

    private boolean isNextSnackbar(Callback callback) {
        return mNextSnackbar != null && mNextSnackbar.isSnackbar(callback);
    }

    private void scheduleTimeoutLocked(SnackbarRecord r) {
        mHandler.removeCallbacksAndMessages(r);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r),
                r.duration == Snackbar.LENGTH_LONG
                        ? LONG_DURATION_MS
                        : SHORT_DURATION_MS);
    }

    private void handleTimeout(SnackbarRecord record) {
        synchronized (mLock) {
            if (mCurrentSnackbar == record || mNextSnackbar == record) {
                cancelSnackbarLocked(record);
            }
        }
    }

}
