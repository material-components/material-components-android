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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.Interpolator;

class ValueAnimatorCompatImplHoneycombMr1 extends ValueAnimatorCompat.Impl {

    final ValueAnimator mValueAnimator;

    ValueAnimatorCompatImplHoneycombMr1() {
        mValueAnimator = new ValueAnimator();
    }

    @Override
    public void start() {
        mValueAnimator.start();
    }

    @Override
    public boolean isRunning() {
        return mValueAnimator.isRunning();
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        mValueAnimator.setInterpolator(interpolator);
    }

    @Override
    public void setUpdateListener(final AnimatorUpdateListenerProxy updateListener) {
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                updateListener.onAnimationUpdate();
            }
        });
    }

    @Override
    public void setListener(final AnimatorListenerProxy listener) {
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                listener.onAnimationCancel();
            }
        });
    }

    @Override
    public void setIntValues(int from, int to) {
        mValueAnimator.setIntValues(from, to);
    }

    @Override
    public int getAnimatedIntValue() {
        return (int) mValueAnimator.getAnimatedValue();
    }

    @Override
    public void setFloatValues(float from, float to) {
        mValueAnimator.setFloatValues(from, to);
    }

    @Override
    public float getAnimatedFloatValue() {
        return (float) mValueAnimator.getAnimatedValue();
    }

    @Override
    public void setDuration(int duration) {
        mValueAnimator.setDuration(duration);
    }

    @Override
    public void cancel() {
        mValueAnimator.cancel();
    }

    @Override
    public float getAnimatedFraction() {
        return mValueAnimator.getAnimatedFraction();
    }

    @Override
    public void end() {
        mValueAnimator.end();
    }

    @Override
    public long getDuration() {
        return mValueAnimator.getDuration();
    }
}
