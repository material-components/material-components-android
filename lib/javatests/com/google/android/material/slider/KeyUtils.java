/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.slider;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;

/** Contains helpers for sending key events to a view */
public class KeyUtils {

  public static class KeyEventBuilder {
    int keyCode;
    int meta;

    public KeyEventBuilder(int keyCode) {
      this(keyCode, 0);
    }

    public KeyEventBuilder(int keyCode, int meta) {
      this.keyCode = keyCode;
      this.meta = meta;
    }

    public KeyEvent buildDown() {
      return build(KeyEvent.ACTION_DOWN);
    }

    public KeyEvent buildUp() {
      return build(KeyEvent.ACTION_UP);
    }

    public KeyEvent build(int keyEvent) {
      return new KeyEvent(
          SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), keyEvent, keyCode, 0, meta);
    }

    public void dispatchEvent(View view) {
      view.dispatchKeyEvent(buildDown());
      view.dispatchKeyEvent(buildUp());
    }
  }

  /** Quick way to dispatch the down and up key events to a view */
  public static void dispatchKeyEvent(View view, int keyCode) {
    new KeyEventBuilder(keyCode).dispatchEvent(view);
  }
}
