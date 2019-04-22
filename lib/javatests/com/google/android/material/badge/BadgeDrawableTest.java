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

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Parcel;
import com.google.android.material.badge.BadgeDrawable.SavedState;
import androidx.core.content.res.ResourcesCompat;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Test for {@link BadgeDrawable} */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class BadgeDrawableTest {

  private static final int TEST_BADGE_NUMBER = 26;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
  }

  @Test
  public void testSavedState() {
    int testBackgroundColor =
        ResourcesCompat.getColor(
            context.getResources(), android.R.color.holo_purple, context.getTheme());
    int testBadgeTextColor =
        ResourcesCompat.getColor(context.getResources(), android.R.color.white, context.getTheme());
    BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
    SavedState drawableState = badgeDrawable.getSavedState();
    badgeDrawable.setNumber(TEST_BADGE_NUMBER);

    badgeDrawable.setBackgroundColor(testBackgroundColor);
    badgeDrawable.setBadgeTextColor(testBadgeTextColor);

    Parcel parcel = Parcel.obtain();
    drawableState.writeToParcel(parcel, drawableState.describeContents());
    parcel.setDataPosition(0);

    SavedState createdFromParcel = SavedState.CREATOR.createFromParcel(parcel);
    BadgeDrawable restoredBadgeDrawable =
        BadgeDrawable.createFromSavedState(context, createdFromParcel);
    assertThat(restoredBadgeDrawable.getNumber()).isEqualTo(TEST_BADGE_NUMBER);
    assertThat(restoredBadgeDrawable.getBackgroundColor()).isEqualTo(testBackgroundColor);
    assertThat(restoredBadgeDrawable.getBadgeTextColor()).isEqualTo(testBadgeTextColor);
    // Values based on the default badge style.
    assertThat(restoredBadgeDrawable.getAlpha()).isEqualTo(255);
    assertThat(restoredBadgeDrawable.getMaxCharacterCount()).isEqualTo(4);
  }
}
