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
import android.content.res.TypedArray;
import android.os.Parcel;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import androidx.annotation.StyleRes;
import androidx.annotation.XmlRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.drawable.DrawableUtils;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Test for {@link BadgeDrawable} */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class BadgeDrawableTest {

  private static final String TEST_BADGE_TEXT = "Test";
  private static final int TEST_BADGE_NUMBER = 26;
  private static final Locale TEST_BADGE_NUMBER_LOCALE = new Locale("ar");

  private static final int TEST_BADGE_HORIZONTAL_OFFSET = 10;
  private static final int TEST_BADGE_VERTICAL_OFFSET = 5;

  private static final int TEST_BADGE_HORIZONTAL_PADDING = 10;
  private static final int TEST_BADGE_VERTICAL_PADDING = 10;

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
    BadgeState.State drawableState = badgeDrawable.getSavedState();
    badgeDrawable.setText(TEST_BADGE_TEXT);
    badgeDrawable.setNumber(TEST_BADGE_NUMBER);
    badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_START);

    badgeDrawable.setHorizontalPadding(TEST_BADGE_HORIZONTAL_PADDING);
    badgeDrawable.setVerticalPadding(TEST_BADGE_VERTICAL_PADDING);

    badgeDrawable.setHorizontalOffset(TEST_BADGE_HORIZONTAL_OFFSET);
    badgeDrawable.setVerticalOffset(TEST_BADGE_VERTICAL_OFFSET);

    int additionalHorizontalOffset =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.mtrl_badge_toolbar_action_menu_item_horizontal_offset);
    badgeDrawable.setAdditionalHorizontalOffset(additionalHorizontalOffset);
    int additionalVerticalOffset =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.mtrl_badge_toolbar_action_menu_item_vertical_offset);
    badgeDrawable.setAdditionalVerticalOffset(additionalVerticalOffset);

    badgeDrawable.setBackgroundColor(testBackgroundColor);
    badgeDrawable.setBadgeTextColor(testBadgeTextColor);
    badgeDrawable.setVisible(false);
    badgeDrawable.setMaxNumber(999);
    badgeDrawable.setMaxCharacterCount(4);
    badgeDrawable.setBadgeNumberLocale(TEST_BADGE_NUMBER_LOCALE);

    Parcel parcel = Parcel.obtain();
    drawableState.writeToParcel(parcel, drawableState.describeContents());
    parcel.setDataPosition(0);

    BadgeState.State createdFromParcel = BadgeState.State.CREATOR.createFromParcel(parcel);
    BadgeDrawable restoredBadgeDrawable =
        BadgeDrawable.createFromSavedState(context, createdFromParcel);
    assertThat(restoredBadgeDrawable.getText()).isEqualTo(TEST_BADGE_TEXT);
    assertThat(restoredBadgeDrawable.getNumber()).isEqualTo(TEST_BADGE_NUMBER);
    assertThat(restoredBadgeDrawable.getBackgroundColor()).isEqualTo(testBackgroundColor);
    assertThat(restoredBadgeDrawable.getBadgeTextColor()).isEqualTo(testBadgeTextColor);
    // Values based on the default badge style.
    assertThat(restoredBadgeDrawable.getAlpha()).isEqualTo(255);
    assertThat(restoredBadgeDrawable.getMaxCharacterCount()).isEqualTo(4);
    assertThat(restoredBadgeDrawable.getMaxNumber()).isEqualTo(999);
    assertThat(restoredBadgeDrawable.getBadgeGravity()).isEqualTo(BadgeDrawable.TOP_START);
    // badge padding
    assertThat(restoredBadgeDrawable.getHorizontalPadding()).isEqualTo(TEST_BADGE_HORIZONTAL_PADDING);
    assertThat(restoredBadgeDrawable.getVerticalPadding()).isEqualTo(TEST_BADGE_VERTICAL_PADDING);
    // badge offsets
    assertThat(restoredBadgeDrawable.getHorizontalOffset()).isEqualTo(TEST_BADGE_HORIZONTAL_OFFSET);
    assertThat(restoredBadgeDrawable.getVerticalOffset()).isEqualTo(TEST_BADGE_VERTICAL_OFFSET);
    // Additional badge offsets
    assertThat(restoredBadgeDrawable.getAdditionalHorizontalOffset())
        .isEqualTo(additionalHorizontalOffset);
    assertThat(restoredBadgeDrawable.getAdditionalVerticalOffset())
        .isEqualTo(additionalVerticalOffset);

    // badge visibility
    assertThat(restoredBadgeDrawable.isVisible()).isFalse();

    // badge number locale
    assertThat(restoredBadgeDrawable.getBadgeNumberLocale()).isEqualTo(TEST_BADGE_NUMBER_LOCALE);
  }

  // Verify that the hardcoded badge gravity attribute values match their piped Gravity counter
  // parts.
  @Test
  public void testBadgeGravityAttributeValue_topEnd() {
    testBadgeGravityValueHelper(R.xml.standalone_badge, Gravity.TOP | Gravity.END);
  }

  @Test
  public void testBadgeGravityAttributeValue_topStart() {
    testBadgeGravityValueHelper(
        R.xml.standalone_badge_gravity_top_start, Gravity.TOP | Gravity.START);
  }

  @Test
  public void testBadgeGravityAttributeValue_bottomEnd() {
    testBadgeGravityValueHelper(
        R.xml.standalone_badge_gravity_bottom_end, Gravity.BOTTOM | Gravity.END);
  }

  @Test
  public void testBadgeGravityAttributeValue_bottomStart() {
    testBadgeGravityValueHelper(
        R.xml.standalone_badge_gravity_bottom_start, Gravity.BOTTOM | Gravity.START);
  }

  @Test
  @Config(qualifiers = "w360dp-h640dp-xhdpi")
  public void testHorizontalOffset() {
    BadgeDrawable badgeDrawable =
        BadgeDrawable.createFromResource(context, R.xml.standalone_badge_offset);
    assertThat(badgeDrawable.getHorizontalOffset()).isEqualTo(dpToPx(TEST_BADGE_HORIZONTAL_OFFSET));
  }

  @Test
  @Config(qualifiers = "w360dp-h640dp-xhdpi")
  public void testVerticalOffset() {
    BadgeDrawable badgeDrawable =
        BadgeDrawable.createFromResource(context, R.xml.standalone_badge_offset);
    assertThat(badgeDrawable.getVerticalOffset()).isEqualTo(dpToPx(TEST_BADGE_VERTICAL_OFFSET));
  }

  private void testBadgeGravityValueHelper(@XmlRes int xmlId, int expectedValue) {
    TypedArray a = getTypedArray(xmlId);

    int value = 0;
    if (a.hasValue(R.styleable.Badge_badgeGravity)) {
      value = a.getInt(R.styleable.Badge_badgeGravity, 0);
    }
    assertThat(value).isEqualTo(expectedValue);
    a.recycle();
  }

  private TypedArray getTypedArray(@XmlRes int xmlId) {
    AttributeSet attrs = DrawableUtils.parseDrawableXml(context, xmlId, "badge");
    @StyleRes int style = attrs.getStyleAttribute();
    if (style == 0) {
      style = R.style.Widget_MaterialComponents_Badge;
    }
    return context
        .getTheme()
        .obtainStyledAttributes(attrs, R.styleable.Badge, R.attr.badgeStyle, style);
  }

  private int dpToPx(float dp) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
  }
}
