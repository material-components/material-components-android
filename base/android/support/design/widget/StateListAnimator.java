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

import android.util.StateSet;
import android.view.View;
import android.view.animation.Animation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

final class StateListAnimator {

    private final ArrayList<Tuple> mTuples = new ArrayList<>();

    private Tuple mLastMatch = null;
    private Animation mRunningAnimation = null;
    private WeakReference<View> mViewRef;

    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRunningAnimation == animation) {
                mRunningAnimation = null;
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // no-op
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // no-op
        }
    };

    /**
     * Associates the given Animation with the provided drawable state specs so that it will be run
     * when the View's drawable state matches the specs.
     *
     * @param specs    The drawable state specs to match against
     * @param animation The Animation to run when the specs match
     */
    public void addState(int[] specs, Animation animation) {
        Tuple tuple = new Tuple(specs, animation);
        animation.setAnimationListener(mAnimationListener);
        mTuples.add(tuple);
    }

    /**
     * Returns the current {@link Animation} which is started because of a state
     * change.
     *
     * @return The currently running Animation or null if no Animation is running
     */
    Animation getRunningAnimation() {
        return mRunningAnimation;
    }


    View getTarget() {
        return mViewRef == null ? null : mViewRef.get();
    }

    void setTarget(View view) {
        final View current = getTarget();
        if (current == view) {
            return;
        }
        if (current != null) {
            clearTarget();
        }
        if (view != null) {
            mViewRef = new WeakReference<>(view);
        }
    }

    private void clearTarget() {
        final View view = getTarget();
        final int size = mTuples.size();
        for (int i = 0; i < size; i++) {
            Animation anim = mTuples.get(i).mAnimation;
            if (view.getAnimation() == anim) {
                view.clearAnimation();
            }
        }
        mViewRef = null;
        mLastMatch = null;
        mRunningAnimation = null;
    }

    /**
     * Called by View
     */
    void setState(int[] state) {
        Tuple match = null;
        final int count = mTuples.size();
        for (int i = 0; i < count; i++) {
            final Tuple tuple = mTuples.get(i);
            if (StateSet.stateSetMatches(tuple.mSpecs, state)) {
                match = tuple;
                break;
            }
        }
        if (match == mLastMatch) {
            return;
        }
        if (mLastMatch != null) {
            cancel();
        }

        mLastMatch = match;

        View view = mViewRef.get();
        if (match != null && view != null && view.getVisibility() == View.VISIBLE ) {
            start(match);
        }
    }

    private void start(Tuple match) {
        mRunningAnimation = match.mAnimation;

        View view = getTarget();
        if (view != null) {
            view.startAnimation(mRunningAnimation);
        }
    }

    private void cancel() {
        if (mRunningAnimation != null) {
            final View view = getTarget();
            if (view != null && view.getAnimation() == mRunningAnimation) {
                view.clearAnimation();
            }
            mRunningAnimation = null;
        }
    }

    /**
     * @hide
     */
    ArrayList<Tuple> getTuples() {
        return mTuples;
    }

    /**
     * If there is an animation running for a recent state change, ends it. <p> This causes the
     * animation to assign the end value(s) to the View.
     */
    public void jumpToCurrentState() {
        if (mRunningAnimation != null) {
            final View view = getTarget();
            if (view != null && view.getAnimation() == mRunningAnimation) {
                view.clearAnimation();
            }
        }
    }

    static class Tuple {
        final int[] mSpecs;
        final Animation mAnimation;

        private Tuple(int[] specs, Animation Animation) {
            mSpecs = specs;
            mAnimation = Animation;
        }

        int[] getSpecs() {
            return mSpecs;
        }

        Animation getAnimation() {
            return mAnimation;
        }
    }
}