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

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.R;
import android.support.v7.app.AppCompatDialog;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Base class for {@link android.app.Dialog}s styled as a bottom sheet.
 */
public class BottomSheetDialog extends AppCompatDialog {

    public BottomSheetDialog(@NonNull Context context) {
        this(context, 0);
    }

    public BottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, getThemeResId(context, theme));
        // We hide the title bar for any style configuration. Otherwise, there will be a gap
        // above the bottom sheet when it is expanded.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    protected BottomSheetDialog(@NonNull Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(wrapInBottomSheet(layoutResId, null, null));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(wrapInBottomSheet(0, view, null));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(wrapInBottomSheet(0, view, params));
    }

    private View wrapInBottomSheet(int layoutResId, View view, ViewGroup.LayoutParams params) {
        final CoordinatorLayout coordinator = (CoordinatorLayout) View.inflate(getContext(),
                R.layout.design_bottom_sheet_dialog, null);
        if (layoutResId != 0 && view == null) {
            view = getLayoutInflater().inflate(layoutResId, coordinator, false);
        }
        FrameLayout bottomSheet = (FrameLayout) coordinator.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(mBottomSheetCallback);
        if (params == null) {
            bottomSheet.addView(view);
        } else {
            bottomSheet.addView(view, params);
        }
        // We treat the CoordinatorLayout as outside the dialog though it is technically inside
        if (shouldWindowCloseOnTouchOutside()) {
            coordinator.findViewById(R.id.touch_outside).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isShowing()) {
                                cancel();
                            }
                        }
                    });
        }
        return coordinator;
    }

    private boolean shouldWindowCloseOnTouchOutside() {
        if (Build.VERSION.SDK_INT < 11) {
            return true;
        }
        TypedValue value = new TypedValue();
        //noinspection SimplifiableIfStatement
        if (getContext().getTheme()
                .resolveAttribute(android.R.attr.windowCloseOnTouchOutside, value, true)) {
            return value.data != 0;
        }
        return false;
    }

    private static int getThemeResId(Context context, int themeId) {
        if (themeId == 0) {
            // If the provided theme is 0, then retrieve the dialogTheme from our theme
            TypedValue outValue = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    R.attr.bottomSheetDialogTheme, outValue, true)) {
                themeId = outValue.resourceId;
            } else {
                // bottomSheetDialogTheme is not provided; we default to our light theme
                themeId = R.style.Theme_Design_Light_BottomSheetDialog;
            }
        }
        return themeId;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback
            = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet,
                @BottomSheetBehavior.State int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

}
