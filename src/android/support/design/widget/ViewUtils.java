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

import android.os.Build;
import android.view.View;

class ViewUtils {

    static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR
            = new ValueAnimatorCompat.Creator() {
        @Override
        public ValueAnimatorCompat createAnimator() {
            return new ValueAnimatorCompat(Build.VERSION.SDK_INT >= 12
                    ? new ValueAnimatorCompatImplHoneycombMr1()
                    : new ValueAnimatorCompatImplEclairMr1());
        }
    };

    private interface ViewUtilsImpl {
        void setBoundsViewOutlineProvider(View view);
    }

    private static class ViewUtilsImplBase implements ViewUtilsImpl {
        @Override
        public void setBoundsViewOutlineProvider(View view) {
            // no-op
        }
    }

    private static class ViewUtilsImplLollipop implements ViewUtilsImpl {
        @Override
        public void setBoundsViewOutlineProvider(View view) {
            ViewUtilsLollipop.setBoundsViewOutlineProvider(view);
        }
    }

    private static final ViewUtilsImpl IMPL;

    static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 21) {
            IMPL = new ViewUtilsImplLollipop();
        } else {
            IMPL = new ViewUtilsImplBase();
        }
    }

    static void setBoundsViewOutlineProvider(View view) {
        IMPL.setBoundsViewOutlineProvider(view);
    }

    static ValueAnimatorCompat createAnimator() {
        return DEFAULT_ANIMATOR_CREATOR.createAnimator();
    }

}
