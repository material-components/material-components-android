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

import static android.support.design.widget.InsetDividerLinearLayout.INSET_DIVIDER_BEGINNING;
import static android.support.design.widget.InsetDividerLinearLayout.INSET_DIVIDER_END;
import static android.support.design.widget.InsetDividerLinearLayout.INSET_DIVIDER_MIDDLE;
import static android.support.design.widget.InsetDividerLinearLayout.INSET_DIVIDER_NONE;
import static android.support.design.widget.InsetDividerLinearLayout.InsetMode;
import static android.support.v4.view.ViewCompat.LAYOUT_DIRECTION_LTR;
import static android.support.v4.view.ViewCompat.LAYOUT_DIRECTION_RTL;
import static android.support.v7.widget.LinearLayoutCompat.HORIZONTAL;
import static android.support.v7.widget.LinearLayoutCompat.OrientationMode;
import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.design.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.v4.view.ViewCompat;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InsetDividerLinearLayoutApplyInsetsTest
    extends BaseInstrumentationTestCase<InsetDividerLinearLayoutWithItemsActivity> {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    // All permutations of inset mode, LinearLayout orientation, and horizontal layout direction.
    return Arrays.asList(
        new Object[][]{
            // Vertical orientation, LTR
            {INSET_DIVIDER_NONE, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_MIDDLE, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_END, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_END, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, VERTICAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, VERTICAL,
                LAYOUT_DIRECTION_LTR},

            // Horizontal orientation, LTR
            {INSET_DIVIDER_NONE, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_MIDDLE, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_END, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_END, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, HORIZONTAL, LAYOUT_DIRECTION_LTR},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, HORIZONTAL,
                LAYOUT_DIRECTION_LTR},

            // Vertical orientation, RTL
            {INSET_DIVIDER_NONE, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_MIDDLE, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_END, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_END, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, VERTICAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, VERTICAL,
                LAYOUT_DIRECTION_RTL},

            // Horizontal orientation, RTL
            {INSET_DIVIDER_NONE, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_MIDDLE, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_END, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_END, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, HORIZONTAL, LAYOUT_DIRECTION_RTL},
            {INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END, HORIZONTAL,
                LAYOUT_DIRECTION_RTL},
        });
  }

  @InsetMode
  private int mInsetMode;

  @OrientationMode
  private int mOrientation;

  private int mLayoutDirection;

  private int mInsetStart;

  private int mInsetEnd;

  private int mItemSize;

  public InsetDividerLinearLayoutApplyInsetsTest(@InsetMode int insetMode,
      @OrientationMode int orientation, int layoutDirection) {
    super(InsetDividerLinearLayoutWithItemsActivity.class);
    mInsetMode = insetMode;
    mOrientation = orientation;
    mLayoutDirection = layoutDirection;
  }

  @Before
  public void setUp() throws Exception {
    final Resources res = mActivityTestRule.getActivity().getResources();
    mInsetStart = res.getDimensionPixelSize(R.dimen.divider_inset_start);
    mInsetEnd = res.getDimensionPixelSize(R.dimen.divider_inset_end);
    mItemSize = res.getDimensionPixelSize(R.dimen.divider_item_size);
  }

  @Test
  @MediumTest
  public void testApplyInsets() throws Throwable {
    final InsetDividerLinearLayout layout = mActivityTestRule.getActivity().mLinearLayout;
    final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    final int count = layout.getChildCount();
    final int size = count * mItemSize;

    // We'll only test for on/off pixels, which assumes our view and child backgrounds are
    // transparent, and the divider drawable is non-transparent.
    final Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);

    mActivityTestRule.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        layout.setApplyInsets(mInsetMode);
        layout.setOrientation(mOrientation);
        ViewCompat.setLayoutDirection(layout, mLayoutDirection);

        // Manually rendering this view is a bit wasteful, but it eliminates the problem of
        // variable test device screen size.
        layout.measure(View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY));
        layout.layout(0, 0, size, size);
        final Canvas c = new Canvas(b);
        layout.draw(c);
      }
    });

    instrumentation.waitForIdleSync();

    if ((mInsetMode & INSET_DIVIDER_BEGINNING) == 0) {
      assertDividerInset("beginning divider is not inset", layout, b, 0, false);
    } else {
      assertDividerInset("beginning divider is inset", layout, b, 0, true);
    }

    if ((mInsetMode & INSET_DIVIDER_MIDDLE) == 0) {
      for (int i = 1; i < count - 1; i++) {
        assertDividerInset("middle divider is not inset", layout, b, mItemSize * i, false);
      }
    } else {
      for (int i = 1; i < count - 1; i++) {
        assertDividerInset("middle divider is inset", layout, b, mItemSize * i, true);
      }
    }

    if ((mInsetMode & INSET_DIVIDER_END) == 0) {
      assertDividerInset("end divider is not inset", layout, b, mItemSize * count - 1, false);
    } else {
      assertDividerInset("end divider is inset", layout, b, mItemSize * count - 1, true);
    }
  }

  /**
   * Test four pixels per divider to assert our inset behavior based on inset rule, orientation,
   * and layout direction.
   */
  private void assertDividerInset(String message, View layout, Bitmap b, int offset,
      boolean expectedInset) {
    if (mOrientation == VERTICAL) {
      final int left = 0;
      final int right = b.getWidth() - 1;
      final int insetLeft;
      final int insetRight;

      if (ViewCompat.getLayoutDirection(layout) == LAYOUT_DIRECTION_LTR) {
        insetLeft = mInsetStart;
        insetRight = right - mInsetEnd;
      } else {
        insetLeft = mInsetEnd;
        insetRight = right - mInsetStart;
      }

      assertPixel(message, b, left, offset, !expectedInset);
      assertPixel(message, b, insetLeft, offset, true);
      assertPixel(message, b, insetRight, offset, true);
      assertPixel(message, b, right, offset, !expectedInset);
    } else {
      final int top = 0;
      final int bottom = b.getHeight() - 1;
      final int insetTop = mInsetStart;
      final int insetBottom = bottom - mInsetEnd;

      if (ViewCompat.getLayoutDirection(layout) == LAYOUT_DIRECTION_RTL) {
        offset = b.getWidth() - offset - 1;
      }

      assertPixel(message, b, offset, top, !expectedInset);
      assertPixel(message, b, offset, insetTop, true);
      assertPixel(message, b, offset, insetBottom, true);
      assertPixel(message, b, offset, bottom, !expectedInset);
    }
  }

  private static void assertPixel(String message, Bitmap b, int x, int y, boolean expected) {
    final int pixel = b.getPixel(x, y);
    if (expected) {
      assertTrue(message, pixel != 0);
    } else {
      assertTrue(message, pixel == 0);
    }
  }
}
