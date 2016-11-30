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

package android.support.design.testutils;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

public class TestUtilsActions {
    /**
     * Replaces an existing <code>TabLayout</code> with a new one inflated from the specified
     * layout resource.
     */
    public static ViewAction replaceTabLayout(final @LayoutRes int tabLayoutResId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(90);
            }

            @Override
            public String getDescription() {
                return "Replace TabLayout";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                final ViewGroup viewGroup = (ViewGroup) view;
                final int childCount = viewGroup.getChildCount();
                // Iterate over children and find TabLayout
                for (int i = 0; i < childCount; i++) {
                    View child = viewGroup.getChildAt(i);
                    if (child instanceof TabLayout) {
                        // Remove the existing TabLayout
                        viewGroup.removeView(child);
                        // Create a new one
                        final LayoutInflater layoutInflater =
                                LayoutInflater.from(view.getContext());
                        final TabLayout newTabLayout =  (TabLayout) layoutInflater.inflate(
                                tabLayoutResId, viewGroup, false);
                        // Make sure we're adding the new TabLayout at the same index
                        viewGroup.addView(newTabLayout, i);
                        break;
                    }
                }

                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
