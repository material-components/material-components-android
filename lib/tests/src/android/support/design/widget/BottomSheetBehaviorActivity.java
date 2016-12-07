/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Intent;
import android.support.design.test.R;
import android.widget.LinearLayout;


public class BottomSheetBehaviorActivity extends BaseTestActivity {

    public static String EXTRA_INITIAL_STATE = "initial_state";

    CoordinatorLayout mCoordinatorLayout;

    LinearLayout mBottomSheet;

    BottomSheetBehavior mBehavior;

    FloatingActionButton mFab;

    @Override
    protected int getContentViewLayoutResId() {
        return R.layout.test_design_bottom_sheet_behavior;
    }

    @Override
    protected void onContentViewSet() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        mBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        Intent intent = getIntent();
        if (intent != null) {
            int initialState = intent.getIntExtra(EXTRA_INITIAL_STATE, -1);
            if (initialState != -1) {
                //noinspection ResourceType
                mBehavior.setState(initialState);
            }
        }
    }

}
