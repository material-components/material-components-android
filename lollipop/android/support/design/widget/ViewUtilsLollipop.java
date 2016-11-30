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

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.R;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

class ViewUtilsLollipop {

    private static final int[] STATE_LIST_ANIM_ATTRS = new int[] {android.R.attr.stateListAnimator};

    static void setBoundsViewOutlineProvider(View view) {
        view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

    static void setStateListAnimatorFromAttrs(View view, AttributeSet attrs,
           int defStyleAttr,  int defStyleRes) {
        final Context context = view.getContext();
        final TypedArray a = context.obtainStyledAttributes(attrs, STATE_LIST_ANIM_ATTRS,
                defStyleAttr, defStyleRes);
        try {
            if (a.hasValue(0)) {
                StateListAnimator sla = AnimatorInflater.loadStateListAnimator(context,
                        a.getResourceId(0, 0));
                view.setStateListAnimator(sla);
            }
        } finally {
            a.recycle();
        }
    }

    /**
     * Creates and sets a {@link StateListAnimator} with a custom elevation value
     */
    static void setDefaultAppBarLayoutStateListAnimator(final View view,
            final float targetElevation) {
        final StateListAnimator sla = new StateListAnimator();

        // Enabled, collapsible and collapsed == elevated
        sla.addState(new int[]{android.R.attr.enabled, R.attr.state_collapsible,
                        R.attr.state_collapsed},
                ObjectAnimator.ofFloat(view, "elevation", targetElevation));

        // Enabled and collapsible, but not collapsed != elevated
        sla.addState(new int[]{android.R.attr.enabled, R.attr.state_collapsible,
                        -R.attr.state_collapsed},
                ObjectAnimator.ofFloat(view, "elevation", 0f));

        // Enabled but not collapsible == elevated
        sla.addState(new int[]{android.R.attr.enabled, -R.attr.state_collapsible},
                ObjectAnimator.ofFloat(view, "elevation", targetElevation));

        // Default, none elevated state
        sla.addState(new int[0], ObjectAnimator.ofFloat(view, "elevation", 0));

        view.setStateListAnimator(sla);
    }

}
