/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.appbar;

import com.google.android.material.test.R;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.google.android.material.appbar.AppBarLayout.LayoutParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppBarLayoutTest {

  private AppBarLayout appBarLayout;
  private View firstScrollableChild;
  private View secondScrollableChild;

  @Before
  public void setUp() {
    AppCompatActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    appBarLayout =
        (AppBarLayout) activity.getLayoutInflater().inflate(R.layout.test_appbarlayout, null);
    firstScrollableChild = appBarLayout.findViewById(R.id.firstScrollableChild);
    secondScrollableChild = appBarLayout.findViewById(R.id.secondScrollableChild);

    activity.setContentView(appBarLayout);

    // Wait until the layout is measured.
    getInstrumentation().waitForIdleSync();
  }

  @Test
  public void testTotalScrollRange_whenFirstChildScrollableAndVisible_onlyCountFirstChild() {
    assertThat(appBarLayout.getTotalScrollRange())
        .isEqualTo(getChildScrollRange(firstScrollableChild));
  }

  @Test
  public void testTotalScrollRange_whenFirstChildNotExitUntilCollapsed_countFirstTwoChildren() {
    // Total scroll range will include all children until the first exit-until-collapsed child
    setExitUntilCollapsed(firstScrollableChild, false);

    assertThat(appBarLayout.getTotalScrollRange())
        .isEqualTo(
            getChildScrollRange(firstScrollableChild) + getChildScrollRange(secondScrollableChild));
  }

  @Test
  public void testTotalScrollRange_whenFirstChildGone_onlyCountSecondChild() {
    firstScrollableChild.setVisibility(View.GONE);

    assertThat(appBarLayout.getTotalScrollRange())
        .isEqualTo(getChildScrollRange(secondScrollableChild));
  }

  @Test
  public void testTotalScrollRange_noVisibleScrollableChild_returnZero() {
    firstScrollableChild.setVisibility(View.GONE);
    secondScrollableChild.setVisibility(View.GONE);

    assertThat(appBarLayout.getTotalScrollRange()).isEqualTo(0);
  }

  @Test
  public void testTotalScrollRange_whenFirstChildNotScrollable_returnZero() {
    appBarLayout.removeView(firstScrollableChild);
    appBarLayout.removeView(secondScrollableChild);
    appBarLayout.addView(firstScrollableChild);

    assertThat(appBarLayout.getTotalScrollRange()).isEqualTo(0);
  }

  @Test
  public void testDownNestedPreScrollRange_noEnterAlwaysChild_returnZero() {
    assertThat(appBarLayout.getDownNestedPreScrollRange()).isEqualTo(0);
  }

  @Test
  public void testDownNestedPreScrollRange_whenFirstChildEnterAlways_onlyCountFirstChild() {
    setEnterAlways(firstScrollableChild, true);

    assertThat(appBarLayout.getDownNestedPreScrollRange())
        .isEqualTo(getChildDownNestedPreScrollRange(firstScrollableChild));
  }

  @Test
  public void testDownNestedPreScrollRange_whenFirstChildEnterAlwaysButGone_returnZero() {
    setEnterAlways(firstScrollableChild, true);
    firstScrollableChild.setVisibility(View.GONE);

    assertThat(appBarLayout.getDownNestedPreScrollRange()).isEqualTo(0);
  }

  @Test
  public void testDownNestedPreScrollRange_whenFirstChildGone_onlyCountSecondChild() {
    setEnterAlways(firstScrollableChild, true);
    firstScrollableChild.setVisibility(View.GONE);
    setEnterAlways(secondScrollableChild, true);

    assertThat(appBarLayout.getDownNestedPreScrollRange())
        .isEqualTo(getChildDownNestedPreScrollRange(secondScrollableChild));
  }

  @Test
  public void
      testDownNestedPreScrollRange_whenFirstChildEnterAlwaysCollapsed_onlyCountFirstChild() {
    setEnterAlways(firstScrollableChild, true);
    setEnterAlwaysCollapsed(firstScrollableChild, true);

    assertThat(appBarLayout.getDownNestedPreScrollRange())
        .isEqualTo(getChildDownNestedPreScrollRange(firstScrollableChild));
  }

  @Test
  public void testDownNestedScrollRange_whenFirstChildScrollableAndVisible_onlyCountFirstChild() {
    assertThat(appBarLayout.getDownNestedScrollRange())
        .isEqualTo(getChildDownNestedScrollRange(firstScrollableChild));
  }

  @Test
  public void testDownNestedRange_whenFirstChildNotExitUntilCollapsed_countFirstTwoChildren() {
    // Down nested scroll range will include all children until the first exit-until-collapsed child
    setExitUntilCollapsed(firstScrollableChild, false);

    assertThat(appBarLayout.getDownNestedScrollRange())
        .isEqualTo(
            getChildDownNestedScrollRange(firstScrollableChild)
                + getChildDownNestedScrollRange(secondScrollableChild));
  }

  @Test
  public void testDownNestedScrollRange_whenFirstChildGone_onlyCountSecondChild() {
    firstScrollableChild.setVisibility(View.GONE);

    assertThat(appBarLayout.getDownNestedScrollRange())
        .isEqualTo(getChildDownNestedScrollRange(secondScrollableChild));
  }

  @Test
  public void testDownNestedScrollRange_noVisibleScrollableChild_returnZero() {
    firstScrollableChild.setVisibility(View.GONE);
    secondScrollableChild.setVisibility(View.GONE);

    assertThat(appBarLayout.getDownNestedScrollRange()).isEqualTo(0);
  }

  @Test
  public void testDownNestedScrollRange_whenFirstChildNotScrollable_returnZero() {
    appBarLayout.removeView(firstScrollableChild);
    appBarLayout.removeView(secondScrollableChild);
    appBarLayout.addView(firstScrollableChild);

    assertThat(appBarLayout.getDownNestedScrollRange()).isEqualTo(0);
  }

  @Test
  public void testSetScrollEffectNone_returnsNull() {
    AppBarLayout.LayoutParams lp =
        (AppBarLayout.LayoutParams) firstScrollableChild.getLayoutParams();
    lp.setScrollEffect(LayoutParams.SCROLL_EFFECT_NONE);

    assertThat(lp.getScrollEffect()).isEqualTo(null);
  }

  @Test
  public void testSetScrollEffectCompress() {
    AppBarLayout.LayoutParams lp =
        (AppBarLayout.LayoutParams) firstScrollableChild.getLayoutParams();
    lp.setScrollEffect(LayoutParams.SCROLL_EFFECT_COMPRESS);

    assertThat(lp.getScrollEffect()).isInstanceOf(AppBarLayout.CompressChildScrollEffect.class);
  }

  private static int getChildScrollRange(View child) {
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    return getChildFullHeight(child, lp)
        - (isExitUntilCollapsed(lp) ? child.getMinimumHeight() : 0);
  }

  private static int getChildDownNestedPreScrollRange(View child) {
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    if (isEnterAlwaysCollapsed(lp)) {
      return child.getMinimumHeight() + lp.topMargin + lp.bottomMargin;
    }
    return getChildScrollRange(child);
  }

  private static int getChildDownNestedScrollRange(View child) {
    return getChildScrollRange(child);
  }

  private static int getChildFullHeight(View child, LayoutParams lp) {
    return child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
  }

  private static void setExitUntilCollapsed(View child, boolean exitUntilCollapsed) {
    enableFlag(child, LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED, exitUntilCollapsed);
  }

  private static void setEnterAlwaysCollapsed(View child, boolean enterAlwaysCollapsed) {
    enableFlag(child, LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED, enterAlwaysCollapsed);
  }

  private static void setEnterAlways(View child, boolean enterAlways) {
    enableFlag(child, LayoutParams.SCROLL_FLAG_ENTER_ALWAYS, enterAlways);
  }

  private static void enableFlag(View child, int flag, boolean enable) {
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    if (enable) {
      lp.scrollFlags = lp.scrollFlags | flag;
    } else {
      lp.scrollFlags = lp.scrollFlags & ~flag;
    }
  }

  private static boolean isExitUntilCollapsed(LayoutParams lp) {
    return (lp.scrollFlags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0;
  }

  private static boolean isEnterAlwaysCollapsed(LayoutParams lp) {
    return (lp.scrollFlags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0;
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light_NoActionBar);
    }
  }
}
