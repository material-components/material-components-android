/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.chip;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.R;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
public class SeslExpansionButton extends ImageView {
    public boolean mAutoDisappear;
    public boolean mExpanded;
    public boolean mFloated;
    public final CountDownTimer mTimer;

    public SeslExpansionButton(Context context) {
        this(context, null);
    }

    public SeslExpansionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, -1);
    }

    public SeslExpansionButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mAutoDisappear = false;
        setElevation(getResources().getDimension(R.dimen.expansion_button_elevation));
        long integer = context.getResources().getInteger(R.integer.expansion_button_duration);
        mTimer = new CountDownTimer(integer, integer) {
            @Override
            public void onTick(long j) {
            }

            @Override
            public void onFinish() {
                if (mAutoDisappear && getVisibility() == View.VISIBLE) {
                    setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            startDisappearTimer();
        }
    }

    public void startDisappearTimer() {
        mTimer.cancel();
        mTimer.start();
    }

    public void setAutomaticDisappear(boolean autohide) {
        mAutoDisappear = autohide;
        if (autohide) {
            return;
        }
        mTimer.cancel();
    }

    public void setExpanded(boolean z) {
        mExpanded = z;
        refreshDrawableState();
    }

    public void setFloated(boolean z) {
        mFloated = z;
        refreshDrawableState();
    }

    @Override
    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 2);
        if (mExpanded) {
            ImageView.mergeDrawableStates(onCreateDrawableState, new int[]{R.attr.state_expansion_button_expanded});
        }
        if (mFloated) {
            ImageView.mergeDrawableStates(onCreateDrawableState, new int[]{R.attr.state_expansion_button_floated});
        }
        return onCreateDrawableState;
    }
}
