/*
 * Copyright 2019 The Android Open Source Project
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
package com.google.android.material.badge;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.res.Resources;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Test for {@link BadgeUtils} */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class BadgeUtilsTest {

  private static final int TEST_BADGE_HORIZONTAL_OFFSET = 10;
  private static final int TEST_BADGE_VERTICAL_OFFSET = 5;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
  }

  @Test
  public void testSetToolbarOffset() {
    BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
    badgeDrawable.setHorizontalOffset(TEST_BADGE_HORIZONTAL_OFFSET);
    badgeDrawable.setVerticalOffset(TEST_BADGE_VERTICAL_OFFSET);

    assertThat(badgeDrawable.getHorizontalOffset()).isEqualTo(TEST_BADGE_HORIZONTAL_OFFSET);
    assertThat(badgeDrawable.getVerticalOffset()).isEqualTo(TEST_BADGE_VERTICAL_OFFSET);

    Resources resources = context.getResources();
    BadgeUtils.setToolbarOffset(badgeDrawable, resources);

    int toolbarHorizontalOffset =
        resources.getDimensionPixelOffset(
            R.dimen.mtrl_badge_toolbar_action_menu_item_horizontal_offset);
    int toolbarVerticalOffset =
        resources.getDimensionPixelOffset(
            R.dimen.mtrl_badge_toolbar_action_menu_item_vertical_offset);
    assertThat(badgeDrawable.getHorizontalOffset()).isEqualTo(TEST_BADGE_HORIZONTAL_OFFSET);
    assertThat(badgeDrawable.getVerticalOffset()).isEqualTo(TEST_BADGE_VERTICAL_OFFSET);

    assertThat(badgeDrawable.getAdditionalHorizontalOffset()).isEqualTo(toolbarHorizontalOffset);
    assertThat(badgeDrawable.getAdditionalVerticalOffset()).isEqualTo(toolbarVerticalOffset);
  }

  @Test
  public void testRemoveToolbarOffset() {
    BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
    badgeDrawable.setHorizontalOffset(TEST_BADGE_HORIZONTAL_OFFSET);
    badgeDrawable.setVerticalOffset(TEST_BADGE_VERTICAL_OFFSET);
    Resources resources = context.getResources();
    BadgeUtils.setToolbarOffset(badgeDrawable, resources);
    BadgeUtils.removeToolbarOffset(badgeDrawable);

    assertThat(badgeDrawable.getHorizontalOffset()).isEqualTo(TEST_BADGE_HORIZONTAL_OFFSET);
    assertThat(badgeDrawable.getVerticalOffset()).isEqualTo(TEST_BADGE_VERTICAL_OFFSET);

    assertThat(badgeDrawable.getAdditionalHorizontalOffset()).isEqualTo(0);
    assertThat(badgeDrawable.getAdditionalVerticalOffset()).isEqualTo(0);
  }
}
