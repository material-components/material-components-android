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

package com.google.android.material.testutils;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.content.res.ColorStateList;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.ShapeAppearanceModel;
import org.hamcrest.Matcher;

public class FloatingActionButtonActions {

  public static ViewAction setBackgroundTintColor(@ColorInt final int color) {
    return setBackgroundTintList(ColorStateList.valueOf(color));
  }

  public static ViewAction setBackgroundTintList(@ColorInt final ColorStateList tint) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "Sets FloatingActionButton background tint";
      }

      @Override
      public void perform(UiController uiController, View view) {
        final FloatingActionButton fab = (FloatingActionButton) view;
        fab.setBackgroundTintList(tint);
      }
    };
  }

  public static ViewAction setImageResource(@DrawableRes final int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "Sets FloatingActionButton image resource";
      }

      @Override
      public void perform(UiController uiController, View view) {
        final FloatingActionButton fab = (FloatingActionButton) view;
        fab.setImageResource(resId);
      }
    };
  }

  public static ViewAction setSize(@FloatingActionButton.Size final int size) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "Sets FloatingActionButton size";
      }

      @Override
      public void perform(UiController uiController, View view) {
        final FloatingActionButton fab = (FloatingActionButton) view;
        fab.setSize(size);
      }
    };
  }

  public static ViewAction setCustomSize(final int size) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "sets FloatingActionButton custom size";
      }

      @Override
      public void perform(UiController uiController, View view) {
        FloatingActionButton fab = (FloatingActionButton) view;
        fab.setCustomSize(size);
      }
    };
  }

  public static ViewAction setCompatElevation(final float size) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "Sets FloatingActionButton elevation";
      }

      @Override
      public void perform(UiController uiController, View view) {
        final FloatingActionButton fab = (FloatingActionButton) view;
        fab.setCompatElevation(size);
      }
    };
  }

  public static ViewAction setLayoutGravity(final int gravity) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(View.class);
      }

      @Override
      public String getDescription() {
        return "Sets Views layout_gravity";
      }

      @Override
      public void perform(UiController uiController, View view) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        lp.gravity = gravity;
        view.requestLayout();
      }
    };
  }

  public static ViewAction hideThenShow() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "Calls hide() then show()";
      }

      @Override
      public void perform(UiController uiController, View view) {
        FloatingActionButton fab = (FloatingActionButton) view;
        fab.hide();
        fab.show();

        long duration = fab.getShowMotionSpec().getTotalDuration();
        uiController.loopMainThreadForAtLeast(duration + 50);
      }
    };
  }

  public static ViewAction showThenHide() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "Calls show() then hide()";
      }

      @Override
      public void perform(UiController uiController, View view) {
        FloatingActionButton fab = (FloatingActionButton) view;
        fab.show();
        fab.hide();

        long duration = fab.getHideMotionSpec().getTotalDuration();
        uiController.loopMainThreadForAtLeast(duration + 50);
      }
    };
  }

  /**
   * Returns a {@link ViewAction} that requests will make the visual representation of the FAB fill
   * the {@link View}. It will remove corners, shadow padding, and stop ensuring min touch target
   * size. This should only be used to help with checking color contrast heuristics.
   */
  public static ViewAction fillView() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(FloatingActionButton.class);
      }

      @Override
      public String getDescription() {
        return "removing corner radii from FAB";
      }

      @Override
      public void perform(UiController uiController, View view) {
        FloatingActionButton fab = (FloatingActionButton) view;
        fab.setShapeAppearanceModel(new ShapeAppearanceModel());
        fab.setShadowPaddingEnabled(false);
        fab.setEnsureMinTouchTargetSize(false);
      }
    };
  }
}
