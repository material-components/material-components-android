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

package io.material.catalog.feature;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link ViewScheduler}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
public class ViewSchedulerTest {

  private ViewScheduler viewScheduler;

  @Before
  public void createScheduler() {
    viewScheduler = new ViewScheduler();
  }

  @Test
  public void runnableRuns_whenSchedulerStarted() {
    viewScheduler.start(() -> {}, 100);

    assertThat(viewScheduler.isRunning()).isTrue();
  }

  @Test
  public void runnableTaskIsDone_whenCancelled() {
    viewScheduler.start(() -> {}, 100);
    viewScheduler.cancel();

    assertThat(viewScheduler.isRunning()).isFalse();
  }
}
