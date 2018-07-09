/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.textfield;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.TextInputLayoutActivity;
import android.support.test.filters.MediumTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import java.util.Locale;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@SdkSuppress(minSdkVersion = 17) // Needed for Configuration#setLocale
@RunWith(AndroidJUnit4.class)
public class TextInputLayoutPseudoLocaleTest {
  @Rule
  public final ActivityTestRule<TextInputLayoutActivity> activityTestRule =
      new ActivityTestRule<>(TextInputLayoutActivity.class);

  private static final String ORIGINAL_LANGUAGE = Locale.getDefault().getLanguage();
  private static final String ORIGINAL_COUNTRY = Locale.getDefault().getLanguage();

  @TargetApi(17)
  private static void setLocale(String language, String country, Context context) {
    context = context.getApplicationContext();
    Resources resources = context.getResources();
    Configuration configuration = resources.getConfiguration();
    configuration.setLocale(new Locale(language, country));
    resources.updateConfiguration(configuration, resources.getDisplayMetrics());
  }

  @BeforeClass
  public static void setUp() {
    // Change language to pseudo locale.
    setLocale("ar", "XB", getTargetContext());
  }

  @AfterClass
  public static void cleanUp() {
    setLocale(ORIGINAL_LANGUAGE, ORIGINAL_COUNTRY, getTargetContext());
  }

  @Test
  public void testSimpleEdit() {
    // Type some text, if this was broken it should result in a crash
    onView(withId(R.id.textinput_edittext)).perform(typeText("123"));
    setLocale(ORIGINAL_LANGUAGE, ORIGINAL_COUNTRY, getTargetContext());
  }
}
