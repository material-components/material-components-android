/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.material.internal;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link StaticLayoutBuilderCompat} */
@RunWith(RobolectricTestRunner.class)
public class StaticLayoutBuilderCompatTest {

  private static final String LONG_STRING =
      "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. "
          + "Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur "
          + "ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. "
          + "Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, "
          + "vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a";

  @Config(minSdk = Config.OLDEST_SDK, maxSdk = VERSION_CODES.P)
  @Test
  public void createStaticLayout_withMaxLines_LongString() throws Exception {
    int maxLines = 3;

    StaticLayout staticLayout =
        StaticLayoutBuilderCompat.obtain(LONG_STRING, new TextPaint(), 100)
            .setAlignment(Alignment.ALIGN_NORMAL)
            .setIncludePad(true)
            .setEllipsize(TruncateAt.END)
            .setMaxLines(maxLines)
            .build();

    assertThat(staticLayout).isNotNull();
  }
}
