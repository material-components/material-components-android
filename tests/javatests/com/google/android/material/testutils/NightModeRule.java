/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.appcompat.app.AppCompatDelegate;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Runs tests for multiple night modes.
 */
public final class NightModeRule implements TestRule {

  private static final int[] DEFAULT_NIGHT_MODES_TO_TEST = {
    AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_NO
  };

  private final int[] nightModesToTest;

  /**
   * Creates a rule to run the test for {@link AppCompatDelegate#MODE_NIGHT_YES} and {@link
   * AppCompatDelegate#MODE_NIGHT_NO}.
   */
  public NightModeRule() {
    this(DEFAULT_NIGHT_MODES_TO_TEST);
  }

  /**
   * Creates a rule to run the test for each of the specified night modes..
   *
   * @param nightModesToTest Tests will be run for each of these night modes. The night modes should
   *     use "MODE_NIGHT" constants from {@link AppCompatDelegate} such as {@link
   *     AppCompatDelegate#MODE_NIGHT_YES}.
   */
  public NightModeRule(int... nightModesToTest) {
    this.nightModesToTest = nightModesToTest;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        for (int nightMode : nightModesToTest) {
          getInstrumentation()
              .runOnMainSync(() -> AppCompatDelegate.setDefaultNightMode(nightMode));
          getInstrumentation().waitForIdleSync();
          base.evaluate();
        }
      }
    };
  }
}
