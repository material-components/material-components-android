/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.feature;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A class to periodically trigger a callback on a view.
 */
public final class ViewScheduler {

  private final ScheduledExecutorService scheduledExecutorService =
      Executors.newSingleThreadScheduledExecutor();

  private WeakReference<Runnable> listenerRef;

  private ScheduledFuture<?> task;

  /**
   * Run the {@code runnable} at a fixed period of {@code pollingIntervalMs}.
   * The runnable will be kept with a weak reference to avoid leaking any context.
   * Keep a reference to the runnable so it's not recycled.
   */
  public void start(Runnable runnable, long pollingIntervalMs) {
    cancel();
    this.listenerRef = new WeakReference<>(runnable);
    task = scheduledExecutorService.scheduleAtFixedRate(
        () -> {
          if (listenerRef == null || listenerRef.get() == null) {
            cancel();
            return;
          }

          listenerRef.get().run();
        },
        0,
        pollingIntervalMs,
        TimeUnit.MILLISECONDS
    );
  }

  /**
   * Cancel any scheduled tasks.
   */
  public void cancel() {
    listenerRef = null;
    if (task != null) {
      task.cancel(true);
    }
  }

  /** Whether the task is running or not. */
  public boolean isRunning() {
    return !task.isDone();
  }
}
