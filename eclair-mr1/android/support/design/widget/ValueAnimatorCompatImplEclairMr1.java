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

/**
 * A 'fake' ValueAnimator implementation which uses a Runnable.
 */
class ValueAnimatorCompatImplEclairMr1 extends ValueAnimatorCompat.Impl {

    private static final int HANDLER_DELAY = 10;
    private static final int DEFAULT_DURATION = 200;

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private long mStartTime;
    private boolean mIsRunning;

    private final int[] mIntValues = new int[2];
    private final float[] mFloatValues = new float[2];

    private int mDuration = DEFAULT_DURATION;
    private Interpolator mInterpolator;
    private AnimatorListenerProxy mListener;
    private AnimatorUpdateListenerProxy mUpdateListener;

    private float mAnimatedFraction;

    @Override
    public void start() {
        if (mIsRunning) {
            // If we're already running, ignore
            return;
        }

        if (mInterpolator == null) {
            mInterpolator = new AccelerateDecelerateInterpolator();
        }

        mStartTime = SystemClock.uptimeMillis();
        mIsRunning = true;

        if (mListener != null) {
            mListener.onAnimationStart();
        }

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
    public void setListener(AnimatorListenerProxy listener) {
        mListener = listener;
    }

    @Override
    public void setUpdateListener(AnimatorUpdateListenerProxy updateListener) {
        mUpdateListener = updateListener;
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
    public void setDuration(int duration) {
        mDuration = duration;
    }

    @Override
    public void cancel() {
        mIsRunning = false;
        sHandler.removeCallbacks(mRunnable);

        if (mListener != null) {
            mListener.onAnimationCancel();
        }
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

            if (mUpdateListener != null) {
                mUpdateListener.onAnimationUpdate();
            }

            if (mListener != null) {
                mListener.onAnimationEnd();
            }
        }
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    private void update() {
        if (mIsRunning) {
            // Update the animated fraction
            final long elapsed = SystemClock.uptimeMillis() - mStartTime;
            final float linearFraction = elapsed / (float) mDuration;
            mAnimatedFraction = mInterpolator != null
                    ? mInterpolator.getInterpolation(linearFraction)
                    : linearFraction;

            // If we're running, dispatch tp the listener
            if (mUpdateListener != null) {
                mUpdateListener.onAnimationUpdate();
            }

            // Check to see if we've passed the animation duration
            if (SystemClock.uptimeMillis() >= (mStartTime + mDuration)) {
                mIsRunning = false;

                if (mListener != null) {
                    mListener.onAnimationEnd();
                }
            }
        }

        if (mIsRunning) {
            // If we're still running, post another delayed runnable
            sHandler.postDelayed(mRunnable, HANDLER_DELAY);
        }
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            update();
        }
    };
}
