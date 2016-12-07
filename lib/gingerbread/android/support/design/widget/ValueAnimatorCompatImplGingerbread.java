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
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * A 'fake' ValueAnimator implementation which uses a Runnable.
 */
class ValueAnimatorCompatImplGingerbread extends ValueAnimatorCompat.Impl {

    private static final int HANDLER_DELAY = 10;
    private static final int DEFAULT_DURATION = 200;

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private long mStartTime;
    private boolean mIsRunning;
    private float mAnimatedFraction;

    private final int[] mIntValues = new int[2];
    private final float[] mFloatValues = new float[2];

    private long mDuration = DEFAULT_DURATION;
    private Interpolator mInterpolator;
    private ArrayList<AnimatorListenerProxy> mListeners;
    private ArrayList<AnimatorUpdateListenerProxy> mUpdateListeners;

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            update();
        }
    };

    @Override
    public void start() {
        if (mIsRunning) {
            // If we're already running, ignore
            return;
        }
        if (mInterpolator == null) {
            mInterpolator = new AccelerateDecelerateInterpolator();
        }
        mIsRunning = true;

        // Reset the animated fraction
        mAnimatedFraction = 0f;

        startInternal();
    }

    final void startInternal() {
        mStartTime = SystemClock.uptimeMillis();
        dispatchAnimationUpdate();
        dispatchAnimationStart();
        // Now start our animation ticker
        sHandler.postDelayed(mRunnable, HANDLER_DELAY);
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    @Override
    public void addListener(AnimatorListenerProxy listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    @Override
    public void addUpdateListener(AnimatorUpdateListenerProxy updateListener) {
        if (mUpdateListeners == null) {
            mUpdateListeners = new ArrayList<>();
        }
        mUpdateListeners.add(updateListener);
    }

    @Override
    public void setIntValues(int from, int to) {
        mIntValues[0] = from;
        mIntValues[1] = to;
    }

    @Override
    public int getAnimatedIntValue() {
        return AnimationUtils.lerp(mIntValues[0], mIntValues[1], getAnimatedFraction());
    }

    @Override
    public void setFloatValues(float from, float to) {
        mFloatValues[0] = from;
        mFloatValues[1] = to;
    }

    @Override
    public float getAnimatedFloatValue() {
        return AnimationUtils.lerp(mFloatValues[0], mFloatValues[1], getAnimatedFraction());
    }

    @Override
    public void setDuration(long duration) {
        mDuration = duration;
    }

    @Override
    public void cancel() {
        mIsRunning = false;
        sHandler.removeCallbacks(mRunnable);

        dispatchAnimationCancel();
        dispatchAnimationEnd();
    }

    @Override
    public float getAnimatedFraction() {
        return mAnimatedFraction;
    }

    @Override
    public void end() {
        if (mIsRunning) {
            mIsRunning = false;
            sHandler.removeCallbacks(mRunnable);
            // Set our animated fraction to 1
            mAnimatedFraction = 1f;
            dispatchAnimationUpdate();
            dispatchAnimationEnd();
        }
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    final void update() {
        if (mIsRunning) {
            // Update the animated fraction
            final long elapsed = SystemClock.uptimeMillis() - mStartTime;
            final float linearFraction = MathUtils.constrain(elapsed / (float) mDuration, 0f, 1f);
            mAnimatedFraction = mInterpolator != null
                    ? mInterpolator.getInterpolation(linearFraction)
                    : linearFraction;

            // If we're running, dispatch to the update listeners
            dispatchAnimationUpdate();

            // Check to see if we've passed the animation duration
            if (SystemClock.uptimeMillis() >= (mStartTime + mDuration)) {
                mIsRunning = false;

                dispatchAnimationEnd();
            }
        }

        if (mIsRunning) {
            // If we're still running, post another delayed runnable
            sHandler.postDelayed(mRunnable, HANDLER_DELAY);
        }
    }

    private void dispatchAnimationUpdate() {
        if (mUpdateListeners != null) {
            for (int i = 0, count = mUpdateListeners.size(); i < count; i++) {
                mUpdateListeners.get(i).onAnimationUpdate();
            }
        }
    }

    private void dispatchAnimationStart() {
        if (mListeners != null) {
            for (int i = 0, count = mListeners.size(); i < count; i++) {
                mListeners.get(i).onAnimationStart();
            }
        }
    }

    private void dispatchAnimationCancel() {
        if (mListeners != null) {
            for (int i = 0, count = mListeners.size(); i < count; i++) {
                mListeners.get(i).onAnimationCancel();
            }
        }
    }

    private void dispatchAnimationEnd() {
        if (mListeners != null) {
            for (int i = 0, count = mListeners.size(); i < count; i++) {
                mListeners.get(i).onAnimationEnd();
            }
        }
    }
}
