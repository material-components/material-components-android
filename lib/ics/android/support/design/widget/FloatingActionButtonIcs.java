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
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;

class FloatingActionButtonIcs extends FloatingActionButtonGingerbread {

    private float mRotation;

    FloatingActionButtonIcs(VisibilityAwareImageButton view,
            ShadowViewDelegate shadowViewDelegate, ValueAnimatorCompat.Creator animatorCreator) {
        super(view, shadowViewDelegate, animatorCreator);
        mRotation = mView.getRotation();
    }

    @Override
    boolean requirePreDrawListener() {
        return true;
    }

    @Override
    void onPreDraw() {
        final float rotation = mView.getRotation();
        if (mRotation != rotation) {
            mRotation = rotation;
            updateFromViewRotation();
        }
    }

    @Override
    void hide(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (isOrWillBeHidden()) {
            // We either are or will soon be hidden, skip the call
            return;
        }

        mView.animate().cancel();

        if (shouldAnimateVisibilityChange()) {
            mAnimState = ANIM_STATE_HIDING;

            mView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        private boolean mCancelled;

                        @Override
                        public void onAnimationStart(Animator animation) {
                            mView.internalSetVisibility(View.VISIBLE, fromUser);
                            mCancelled = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mCancelled = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimState = ANIM_STATE_NONE;

                            if (!mCancelled) {
                                mView.internalSetVisibility(View.GONE, fromUser);
                                if (listener != null) {
                                    listener.onHidden();
                                }
                            }
                        }
                    });
        } else {
            // If the view isn't laid out, or we're in the editor, don't run the animation
            mView.internalSetVisibility(View.GONE, fromUser);
            if (listener != null) {
                listener.onHidden();
            }
        }
    }

    @Override
    void show(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (isOrWillBeShown()) {
            // We either are or will soon be visible, skip the call
            return;
        }

        mView.animate().cancel();

        if (shouldAnimateVisibilityChange()) {
            mAnimState = ANIM_STATE_SHOWING;

            if (mView.getVisibility() != View.VISIBLE) {
                // If the view isn't visible currently, we'll animate it from a single pixel
                mView.setAlpha(0f);
                mView.setScaleY(0f);
                mView.setScaleX(0f);
            }

            mView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mView.internalSetVisibility(View.VISIBLE, fromUser);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimState = ANIM_STATE_NONE;
                            if (listener != null) {
                                listener.onShown();
                            }
                        }
                    });
        } else {
            mView.internalSetVisibility(View.VISIBLE, fromUser);
            mView.setAlpha(1f);
            mView.setScaleY(1f);
            mView.setScaleX(1f);
            if (listener != null) {
                listener.onShown();
            }
        }
    }

    private boolean shouldAnimateVisibilityChange() {
        return ViewCompat.isLaidOut(mView) && !mView.isInEditMode();
    }

    private void updateFromViewRotation() {
        if (Build.VERSION.SDK_INT == 19) {
            // KitKat seems to have an issue with views which are rotated with angles which are
            // not divisible by 90. Worked around by moving to software rendering in these cases.
            if ((mRotation % 90) != 0) {
                if (mView.getLayerType() != View.LAYER_TYPE_SOFTWARE) {
                    mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            } else {
                if (mView.getLayerType() != View.LAYER_TYPE_NONE) {
                    mView.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            }
        }

        // Offset any View rotation
        if (mShadowDrawable != null) {
            mShadowDrawable.setRotation(-mRotation);
        }
        if (mBorderDrawable != null) {
            mBorderDrawable.setRotation(-mRotation);
        }
    }
}
