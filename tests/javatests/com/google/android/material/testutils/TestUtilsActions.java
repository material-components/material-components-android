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

package com.google.android.material.testutils;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static org.hamcrest.Matchers.any;

import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.expandable.ExpandableWidget;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.hamcrest.Matcher;

public class TestUtilsActions {
  /**
   * Replaces an existing {@link TabLayout} with a new one inflated from the specified layout
   * resource.
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
            final LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
            final TabLayout newTabLayout =
                (TabLayout) layoutInflater.inflate(tabLayoutResId, viewGroup, false);
            // Make sure we're adding the new TabLayout at the same index
            viewGroup.addView(newTabLayout, i);
            break;
          }
        }

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets layout direction on the view. */
  public static ViewAction setLayoutDirection(final int layoutDirection) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "set layout direction";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewCompat.setLayoutDirection(view, layoutDirection);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets title on the {@link CollapsingToolbarLayout}. */
  public static ViewAction setTitle(final CharSequence title) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(CollapsingToolbarLayout.class);
      }

      @Override
      public String getDescription() {
        return "set toolbar title";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view;
        collapsingToolbarLayout.setTitle(title);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets text content on {@link TextView} */
  public static ViewAction setText(final @Nullable CharSequence text) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextView.class);
      }

      @Override
      public String getDescription() {
        return "TextView set text";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TextView textView = (TextView) view;
        textView.setText(text);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Adds tabs to {@link TabLayout} */
  public static ViewAction addTabs(final String... tabs) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TabLayout.class);
      }

      @Override
      public String getDescription() {
        return "TabLayout add tabs";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TabLayout tabLayout = (TabLayout) view;
        for (int i = 0; i < tabs.length; i++) {
          tabLayout.addTab(tabLayout.newTab().setText(tabs[i]));
        }

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Dummy Espresso action that waits until the UI thread is idle. This action can be performed on
   * the root view to wait for an ongoing animation to be completed.
   */
  public static ViewAction waitUntilIdle() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override
      public String getDescription() {
        return "wait for idle";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Dummy Espresso action that waits for at least the given amount of milliseconds. This action can
   * be performed on the root view to wait for an ongoing animation to be completed.
   */
  public static ViewAction waitFor(final long ms) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override
      public String getDescription() {
        return "wait for " + ms + " ms";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadForAtLeast(ms);
      }
    };
  }

  public static ViewAction setEnabled(final boolean enabled) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "set enabled";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        view.setEnabled(enabled);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  public static ViewAction setPressed(final boolean pressed) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "set pressed";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        view.setPressed(pressed);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  public static ViewAction setClickable(final boolean clickable) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "set clickable";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        view.setClickable(clickable);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  public static ViewAction setSelected(final boolean selected) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "set selected";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        view.setSelected(selected);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets compound drawables on {@link TextView} */
  public static ViewAction setCompoundDrawablesRelative(
      final @Nullable Drawable start,
      final @Nullable Drawable top,
      final @Nullable Drawable end,
      final @Nullable Drawable bottom) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextView.class);
      }

      @Override
      public String getDescription() {
        return "TextView set compound drawables relative";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TextView textView = (TextView) view;
        TextViewCompat.setCompoundDrawablesRelative(textView, start, top, end, bottom);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Restores the saved hierarchy state.
   *
   * @param container The saved hierarchy state.
   */
  public static ViewAction restoreHierarchyState(final SparseArray<Parcelable> container) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(View.class);
      }

      @Override
      public String getDescription() {
        return "restore the saved state";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        view.restoreHierarchyState(container);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Clears and inflates the menu.
   *
   * @param menuResId The menu resource XML to be used.
   */
  public static ViewAction reinflateMenu(final @MenuRes int menuResId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(NavigationView.class);
      }

      @Override
      public String getDescription() {
        return "clear and inflate menu " + menuResId;
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        final NavigationView nv = (NavigationView) view;
        nv.getMenu().clear();
        nv.inflateMenu(menuResId);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  public static ViewAction setExpanded(final boolean expanded) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return any(View.class);
      }

      @Override
      public String getDescription() {
        return "set expanded";
      }

      @Override
      public void perform(UiController uiController, View view) {
        ((ExpandableWidget) view).setExpanded(expanded);
      }
    };
  }

  public static ViewAction setTabMode(final int tabMode) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TabLayout.class);
      }

      @Override
      public String getDescription() {
        return "set tab mode";
      }

      @Override
      public void perform(UiController uiController, View view) {
        ((TabLayout) view).setTabMode(tabMode);
      }
    };
  }

  /** Returns a {@link ViewAction} that requests focus on the {@link View}. */
  public static ViewAction requestFocus() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return any(View.class);
      }

      @Override
      public String getDescription() {
        return "focus";
      }

      @Override
      public void perform(UiController uiController, View view) {
        view.requestFocus();
      }
    };
  }
}
