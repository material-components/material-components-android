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

import org.junit.Assert;

/**
 * Utility used for testing that allows to poll for a certain condition to happen within a timeout.
 */
public abstract class PollingCheck {
  private static final long DEFAULT_TIMEOUT = 3000;
  private static final long TIME_SLICE = 50;
  private final long timeout;

  /** The condition that the PollingCheck should use to proceed successfully. */
  public interface PollingCheckCondition {
    /** @return Whether the polling condition has been met. */
    boolean canProceed();
  }

  public PollingCheck(long timeout) {
    this.timeout = timeout;
  }

  protected abstract boolean check();

  /** Start running the polling check. */
  public void run() {
    if (check()) {
      return;
    }

    long timeoutLeft = timeout;
    while (timeoutLeft > 0) {
      try {
        Thread.sleep(TIME_SLICE);
      } catch (InterruptedException e) {
        Assert.fail("unexpected InterruptedException");
      }

      if (check()) {
        return;
      }

      timeoutLeft -= TIME_SLICE;
    }

    Assert.fail("unexpected timeout");
  }

  /**
   * Instantiate and start polling for a given condition with a default 3000ms timeout.
   *
   * @param condition The condition to check for success.
   */
  public static void waitFor(final PollingCheckCondition condition) {
    new PollingCheck(DEFAULT_TIMEOUT) {
      @Override
      protected boolean check() {
        return condition.canProceed();
      }
    }.run();
  }

  /**
   * Instantiate and start polling for a given condition.
   *
   * @param timeout Time out in ms
   * @param condition The condition to check for success.
   */
  public static void waitFor(long timeout, final PollingCheckCondition condition) {
    new PollingCheck(timeout) {
      @Override
      protected boolean check() {
        return condition.canProceed();
      }
    }.run();
  }
}
