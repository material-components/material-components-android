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
import android.support.v4.view.ViewCompat;
import android.view.View;

class FloatingActionButtonHoneycombMr1 extends FloatingActionButtonEclairMr1 {

    private boolean mIsHiding;

    FloatingActionButtonHoneycombMr1(View view, ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);
    }

    @Override
    void hide() {
        if (mIsHiding || mView.getVisibility() != View.VISIBLE) {
            // A hide animation is in progress, or we're already hidden. Skip the call
            return;
        }

        if (!ViewCompat.isLaidOut(mView) || mView.isInEditMode()) {
            // If the view isn't laid out, or we're in the editor, don't run the animation
            mView.setVisibility(View.GONE);
        } else {
            mView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mIsHiding = true;
                            mView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mIsHiding = false;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsHiding = false;
                            mView.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    void show() {
        if (mView.getVisibility() != View.VISIBLE) {
            if (ViewCompat.isLaidOut(mView) && !mView.isInEditMode()) {
                mView.setAlpha(0f);
                mView.setScaleY(0f);
                mView.setScaleX(0f);
                mView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(SHOW_HIDE_ANIM_DURATION)
                        .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mView.setVisibility(View.VISIBLE);
                            }
                        });
            } else {
                mView.setVisibility(View.VISIBLE);
                mView.setAlpha(1f);
                mView.setScaleY(1f);
                mView.setScaleX(1f);
            }
        }
    }
}
