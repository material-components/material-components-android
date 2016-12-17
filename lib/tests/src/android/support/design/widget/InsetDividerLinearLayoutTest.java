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
import static org.junit.Assert.assertEquals;

import android.app.Instrumentation;
import android.content.res.Resources;
import android.support.design.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.v4.widget.Space;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import org.junit.Before;
import org.junit.Test;

public class InsetDividerLinearLayoutTest
    extends BaseInstrumentationTestCase<InsetDividerLinearLayoutActivity> {

  private InsetDividerLinearLayout mLayout;

  private int mInsetStart;

  private int mInsetEnd;

  private int mItemSize;

  public InsetDividerLinearLayoutTest() {
    super(InsetDividerLinearLayoutActivity.class);
  }

  @Before
  public void setUp() throws Exception {
    final InsetDividerLinearLayoutActivity activity = mActivityTestRule.getActivity();
    mLayout = (InsetDividerLinearLayout) activity.findViewById(R.id.inset_divider_linear_layout);

    final Resources res = activity.getResources();
    mInsetStart = res.getDimensionPixelSize(R.dimen.divider_inset_start);
    mInsetEnd = res.getDimensionPixelSize(R.dimen.divider_inset_end);
    mItemSize = res.getDimensionPixelSize(R.dimen.divider_item_size);
  }

  @Test
  @SmallTest
  public void testBasics() {
    // Defaults
    assertEquals("Should have no start inset", mLayout.getDividerInsetStart(), 0);
    assertEquals("Should have no end inset", mLayout.getDividerInsetEnd(), 0);
    assertEquals("Should apply all insets", mLayout.getApplyInsets(),
        INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END);
  }

  @Test
  @SmallTest
  public void testDividerPaddingCompatibility() {
    mLayout.setDividerPadding(mInsetStart);

    assertEquals("Padding should apply start inset", mLayout.getDividerInsetStart(), mInsetStart);
    assertEquals("Padding should apply end inset", mLayout.getDividerInsetEnd(), mInsetStart);
    assertEquals("Padding should be set on super", mLayout.getDividerPadding(), mInsetStart);

    mLayout.setDividerInsetEnd(mInsetEnd);
    assertEquals("Insets should override padding", mLayout.getDividerInsetEnd(), mInsetEnd);
    assertEquals("Insets should clear padding", mLayout.getDividerPadding(), 0);
  }

  @Test
  @SmallTest
  public void testChildVisibility() throws Throwable {
    final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    mActivityTestRule.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        addChildWithVisibility(View.GONE);
        addChildWithVisibility(View.VISIBLE);
        addChildWithVisibility(View.GONE);
        addChildWithVisibility(View.VISIBLE);
        addChildWithVisibility(View.GONE);
      }
    });

    instrumentation.waitForIdleSync();

    assertEquals("first visible child index", mLayout.getFirstVisibleChildIndex(), 1);
    assertEquals("last visible child index", mLayout.getLastVisibleChildIndex(), 3);

    mActivityTestRule.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mLayout.removeAllViews();
      }
    });

    instrumentation.waitForIdleSync();
  }

  private void addChildWithVisibility(int visibility) {
    View child = new Space(mLayout.getContext());
    child.setVisibility(visibility);
    mLayout.addView(child, new LinearLayoutCompat.LayoutParams(mItemSize, mItemSize));
  }
}
