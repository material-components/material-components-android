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

import android.app.UiAutomation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DisableAnimationsRule  implements TestRule {

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        uiAutomation.executeShellCommand("settings put global window_animation_scale 0");
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 0");
        base.evaluate();
      }
    };
  }
}

