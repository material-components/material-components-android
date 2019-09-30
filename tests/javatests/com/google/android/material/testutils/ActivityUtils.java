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
package com.google.android.material.testutils;

import static org.junit.Assert.assertTrue;

import android.os.Looper;
import androidx.test.rule.ActivityTestRule;
import com.google.android.material.testapp.base.RecreatableAppCompatActivity;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Utility methods for testing activities. */
public class ActivityUtils {
  private static final Runnable DO_NOTHING = () -> {};

  public static void waitForExecution(
      final ActivityTestRule<? extends RecreatableAppCompatActivity> rule) {
    // Wait for two cycles. When starting a postponed transition, it will post to
    // the UI thread and then the execution will be added onto the queue after that.
    // The two-cycle wait makes sure fragments have the opportunity to complete both
    // before returning.
    try {
      rule.runOnUiThread(DO_NOTHING);
      rule.runOnUiThread(DO_NOTHING);
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }

  private static void runOnUiThreadRethrow(
      ActivityTestRule<? extends RecreatableAppCompatActivity> rule, Runnable r) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
      r.run();
    } else {
      try {
        rule.runOnUiThread(r);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }
  /**
   * Restarts the RecreatedAppCompatActivity and waits for the new activity to be resumed.
   *
   * @return The newly-restarted RecreatedAppCompatActivity
   */
  @SuppressWarnings("unchecked") // The type of the recreated activity is guaranteed to be T
  public static <T extends RecreatableAppCompatActivity> T recreateActivity(
      ActivityTestRule<? extends RecreatableAppCompatActivity> rule, final T activity)
      throws InterruptedException {
    // Now switch the orientation
    RecreatableAppCompatActivity.resumedLatch = new CountDownLatch(1);
    RecreatableAppCompatActivity.destroyedLatch = new CountDownLatch(1);
    runOnUiThreadRethrow(rule, activity::recreate);
    assertTrue(RecreatableAppCompatActivity.resumedLatch.await(1, TimeUnit.SECONDS));
    assertTrue(RecreatableAppCompatActivity.destroyedLatch.await(1, TimeUnit.SECONDS));
    T newActivity = (T) RecreatableAppCompatActivity.activity;
    waitForExecution(rule);
    RecreatableAppCompatActivity.clearState();
    return newActivity;
  }
}
