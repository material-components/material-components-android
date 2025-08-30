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

import static org.junit.Assert.assertEquals;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.appcompat.view.menu.MenuItemImpl;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.test.espresso.matcher.BoundedMatcher;
import com.google.android.material.expandable.ExpandableWidget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TestUtilsMatchers {
  /** Returns a matcher that matches Views that are not narrower than specified width in pixels. */
  public static Matcher<View> isNotNarrowerThan(final int minWidth) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        final int viewWidth = view.getWidth();
        if (viewWidth < minWidth) {
          failedCheckDescription = "width " + viewWidth + " is less than minimum " + minWidth;
          return false;
        }
        return true;
      }
    };
  }

  /** Returns a matcher that matches Views that are not wider than specified width in pixels. */
  public static Matcher<View> isNotWiderThan(final int maxWidth) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        final int viewWidth = view.getWidth();
        if (viewWidth > maxWidth) {
          failedCheckDescription = "width " + viewWidth + " is more than maximum " + maxWidth;
          return false;
        }
        return true;
      }
    };
  }

  /** Returns a matcher that matches TextViews with the specified text size. */
  public static Matcher<View> withTextSize(final float textSize) {
    return new BoundedMatcher<View, TextView>(TextView.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final TextView view) {
        final float ourTextSize = view.getTextSize();
        if (Math.abs(textSize - ourTextSize) > 1.0f) {
          failedCheckDescription =
              "text size " + ourTextSize + " is different than expected " + textSize;
          return false;
        }
        return true;
      }
    };
  }

  /** Returns a matcher that matches TextViews with the specified text color. */
  public static Matcher<View> withTextColor(final @ColorInt int textColor) {
    return new BoundedMatcher<View, TextView>(TextView.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final TextView view) {
        final @ColorInt int ourTextColor = view.getCurrentTextColor();
        if (ourTextColor != textColor) {
          int ourAlpha = Color.alpha(ourTextColor);
          int ourRed = Color.red(ourTextColor);
          int ourGreen = Color.green(ourTextColor);
          int ourBlue = Color.blue(ourTextColor);

          int expectedAlpha = Color.alpha(textColor);
          int expectedRed = Color.red(textColor);
          int expectedGreen = Color.green(textColor);
          int expectedBlue = Color.blue(textColor);

          failedCheckDescription =
              "expected color to be ["
                  + expectedAlpha
                  + ","
                  + expectedRed
                  + ","
                  + expectedGreen
                  + ","
                  + expectedBlue
                  + "] but found ["
                  + ourAlpha
                  + ","
                  + ourRed
                  + ","
                  + ourGreen
                  + ","
                  + ourBlue
                  + "]";
          return false;
        }
        return true;
      }
    };
  }

  /**
   * Returns a matcher that matches TextViews whose start drawable is filled with the specified fill
   * color.
   */
  public static Matcher<View> withStartDrawableFilledWith(
      final @ColorInt int fillColor, final int allowedComponentVariance) {
    return new BoundedMatcher<View, TextView>(TextView.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final TextView view) {
        final Drawable[] compoundDrawables = view.getCompoundDrawables();
        final boolean isRtl =
            (ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL);
        final Drawable startDrawable = isRtl ? compoundDrawables[2] : compoundDrawables[0];
        if (startDrawable == null) {
          failedCheckDescription = "no start drawable";
          return false;
        }
        try {
          final Rect bounds = startDrawable.getBounds();
          TestUtils.assertAllPixelsOfColor(
              "",
              startDrawable,
              bounds.width(),
              bounds.height(),
              true,
              fillColor,
              allowedComponentVariance,
              true);
        } catch (Throwable t) {
          failedCheckDescription = t.getMessage();
          return false;
        }
        return true;
      }
    };
  }

  /**
   * Returns a matcher that matches <code>ImageView</code>s which have drawable flat-filled with the
   * specific color.
   */
  public static Matcher<View> drawable(
      @ColorInt final int color, final int allowedComponentVariance) {
    return new BoundedMatcher<View, ImageView>(ImageView.class) {
      private String failedComparisonDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText("with drawable of color: ");

        description.appendText(failedComparisonDescription);
      }

      @Override
      public boolean matchesSafely(final ImageView view) {
        Drawable drawable = view.getDrawable();
        if (drawable == null) {
          return false;
        }

        // One option is to check if we have a ColorDrawable and then call getColor
        // but that API is v11+. Instead, we call our helper method that checks whether
        // all pixels in a Drawable are of the same specified color.
        try {
          TestUtils.assertAllPixelsOfColor(
              "",
              drawable,
              view.getWidth(),
              view.getHeight(),
              true,
              color,
              allowedComponentVariance,
              true);
          // If we are here, the color comparison has passed.
          failedComparisonDescription = null;
          return true;
        } catch (Throwable t) {
          // If we are here, the color comparison has failed.
          failedComparisonDescription = t.getMessage();
          return false;
        }
      }
    };
  }

  /** Returns a matcher that matches Views with the specified background fill color. */
  public static Matcher<View> withBackgroundFill(final @ColorInt int fillColor) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        Drawable background = view.getBackground();
        try {
          TestUtils.assertAllPixelsOfColor(
              "", background, view.getWidth(), view.getHeight(), true, fillColor, 0, true);
        } catch (Throwable t) {
          failedCheckDescription = t.getMessage();
          return false;
        }
        return true;
      }
    };
  }

  /** Returns a matcher that matches FloatingActionButtons with the specified custom size. */
  public static Matcher<View> withFabCustomSize(final int customSize) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        if (!(view instanceof FloatingActionButton)) {
          return false;
        }

        final FloatingActionButton fab = (FloatingActionButton) view;
        if (Math.abs(fab.getCustomSize() - customSize) > 1.0f) {
          failedCheckDescription =
              "Custom size " + fab.getCustomSize() + " is different than expected " + customSize;
          return false;
        }

        return true;
      }
    };
  }

  /**
   * Returns a matcher that matches FloatingActionButtons with the specified background fill color.
   */
  public static Matcher<View> withFabBackgroundFill(final @ColorInt int fillColor) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        if (!(view instanceof FloatingActionButton)) {
          return false;
        }

        final FloatingActionButton fab = (FloatingActionButton) view;

        // Since the FAB background is round, and may contain the shadow, we'll look at
        // just the center half rect of the content area
        final Rect area = new Rect();
        fab.getMeasuredContentRect(area);

        final int rectHeightQuarter = area.height() / 4;
        final int rectWidthQuarter = area.width() / 4;
        area.left += rectWidthQuarter;
        area.top += rectHeightQuarter;
        area.right -= rectWidthQuarter;
        area.bottom -= rectHeightQuarter;

        try {
          TestUtils.assertAllPixelsOfColor(
              "",
              fab.getBackground(),
              view.getWidth(),
              view.getHeight(),
              false,
              fillColor,
              area,
              0,
              true);
        } catch (Throwable t) {
          failedCheckDescription = t.getMessage();
          return false;
        }
        return true;
      }
    };
  }

  /**
   * Returns a matcher that matches {@link View}s based on the given parent type.
   *
   * @param parentMatcher the type of the parent to match on
   */
  public static Matcher<View> isChildOfA(final Matcher<View> parentMatcher) {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("is child of a: ");
        parentMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(View view) {
        final ViewParent viewParent = view.getParent();
        if (!(viewParent instanceof View)) {
          return false;
        }
        if (parentMatcher.matches(viewParent)) {
          return true;
        }
        return false;
      }
    };
  }

  /** Returns a matcher that matches FloatingActionButtons with the specified content height */
  public static Matcher<View> withFabContentHeight(final int size) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        if (!(view instanceof FloatingActionButton)) {
          return false;
        }

        final FloatingActionButton fab = (FloatingActionButton) view;
        final Rect area = new Rect();
        fab.getMeasuredContentRect(area);

        if (area.height() != size) {
          failedCheckDescription =
              "Content height " + area.height() + " is different than expected " + size;
          return false;
        }

        return true;
      }
    };
  }

  /** Returns a matcher that matches FloatingActionButtons with the specified gravity. */
  public static Matcher<View> withFabContentAreaOnMargins(final int gravity) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        if (!(view instanceof FloatingActionButton)) {
          return false;
        }

        final FloatingActionButton fab = (FloatingActionButton) view;
        final ViewGroup.MarginLayoutParams lp =
            (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        final ViewGroup parent = (ViewGroup) view.getParent();

        final Rect area = new Rect();
        fab.getMeasuredContentRect(area);

        final int absGravity =
            GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(view));

        try {
          switch (absGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
              assertEquals(lp.topMargin, fab.getTop() + area.top);
              break;
            case Gravity.BOTTOM:
              assertEquals(parent.getHeight() - lp.bottomMargin, fab.getTop() + area.bottom);
              break;
          }
          switch (absGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
              assertEquals(lp.leftMargin, fab.getLeft() + area.left);
              break;
            case Gravity.RIGHT:
              assertEquals(parent.getWidth() - lp.rightMargin, fab.getLeft() + area.right);
              break;
          }
          return true;
        } catch (Throwable t) {
          failedCheckDescription = t.getMessage();
          return false;
        }
      }
    };
  }

  /** Returns a matcher that matches FloatingActionButtons with the specified content height */
  public static Matcher<View> withCompoundDrawable(final int index, final Drawable expected) {
    return new BoundedMatcher<View, View>(View.class) {
      private String failedCheckDescription;

      @Override
      public void describeTo(final Description description) {
        description.appendText(failedCheckDescription);
      }

      @Override
      public boolean matchesSafely(final View view) {
        if (!(view instanceof TextView)) {
          return false;
        }

        final TextView textView = (TextView) view;
        Drawable actual = TextViewCompat.getCompoundDrawablesRelative(textView)[index];
        if (expected != TextViewCompat.getCompoundDrawablesRelative(textView)[index]) {
          failedCheckDescription =
              "Drawable " + actual + "at index " + index + " does not match expected " + expected;
          return false;
        }

        return true;
      }
    };
  }

  /** Returns a matcher that matches {@link View}s that are pressed. */
  public static Matcher<View> isPressed() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("is pressed");
      }

      @Override
      public boolean matchesSafely(View view) {
        return view.isPressed();
      }
    };
  }

  /** Returns a matcher that matches {@link View}s that are long-clickable. */
  public static Matcher<View> isLongClickable() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("is long-clickable");
      }

      @Override
      public boolean matchesSafely(View view) {
        return view.isLongClickable();
      }
    };
  }

  /**
   * Returns a matcher that matches views which have a z-value greater than 0. Also matches if the
   * platform we're running on does not support z-values.
   */
  public static Matcher<View> hasZ() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has a z value greater than 0");
      }

      @Override
      public boolean matchesSafely(View view) {
        return Build.VERSION.SDK_INT < 21 || ViewCompat.getZ(view) > 0f;
      }
    };
  }
  /**
   * Returns a matcher that matches views which have a translationZ-value greater than 0. Also
   * matches if the platform we're running on does not support translationZ-values.
   */
  public static Matcher<View> hasTranslationZ() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has a translationZ value greater than 0");
      }

      @Override
      public boolean matchesSafely(View view) {
        return Build.VERSION.SDK_INT < 21 || ViewCompat.getTranslationZ(view) > 0f;
      }
    };
  }

  /** Returns a matcher that matches TextViews with the specified typeface. */
  public static Matcher<View> withTypeface(@NonNull final Typeface typeface) {
    return new TypeSafeMatcher<View>(TextView.class) {
      @Override
      public void describeTo(final Description description) {
        description.appendText("view with typeface: " + typeface);
      }

      @Override
      public boolean matchesSafely(final View view) {
        return typeface.equals(((TextView) view).getTypeface());
      }
    };
  }

  /** Returns a matcher that matches TextViews with the specified typeface style. */
  public static Matcher<View> withTypefaceStyle(final int typefaceStyle) {
    return new TypeSafeMatcher<View>(TextView.class) {
      @Override
      public void describeTo(final Description description) {
        description.appendText("view with typeface style: " + typefaceStyle);
      }

      @Override
      public boolean matchesSafely(final View view) {
        return typefaceStyle == ((TextView) view).getTypeface().getStyle();
      }
    };
  }

  /**
   * Returns a matcher that matches the action view of the specified menu item.
   *
   * @param menu The menu
   * @param id The ID of the menu item
   */
  public static Matcher<View> isActionViewOf(@NonNull final Menu menu, @IdRes final int id) {
    return new TypeSafeMatcher<View>() {

      private Resources resources;

      @Override
      protected boolean matchesSafely(View view) {
        resources = view.getResources();
        MenuItemImpl item = (MenuItemImpl) menu.findItem(id);
        return item != null && item.getActionView() == view;
      }

      @Override
      public void describeTo(Description description) {
        String name;
        if (resources != null) {
          name = resources.getResourceName(id);
        } else {
          name = Integer.toString(id);
        }
        description.appendText("is action view of menu item " + name);
      }
    };
  }

  /** Returns a matcher that matches {@link View}s that are expanded. */
  public static Matcher<View> isExpanded() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("is expanded");
      }

      @Override
      public boolean matchesSafely(View view) {
        return ((ExpandableWidget) view).isExpanded();
      }
    };
  }

  /**
   * Returns a matcher that matches {@link View}s with the specified tooltip text. Only works for
   * API 26+
   */
  public static Matcher<View> withTooltipText(@Nullable final CharSequence text) {
    return new BoundedMatcher<View, View>(View.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("with tooltip text: ");
        if (text == null) {
          description.appendText("null");
        } else {
          description.appendText(text.toString());
        }
      }

      @Override
      protected boolean matchesSafely(View item) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
          return false;
        }
        return TextUtils.equals(item.getTooltipText(), text);
      }
    };
  }
}
