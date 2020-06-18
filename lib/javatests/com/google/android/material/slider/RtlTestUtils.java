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

import static android.os.Build.VERSION.SDK_INT;
import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import androidx.annotation.RequiresApi;
import androidx.test.core.app.ApplicationProvider;
import java.util.Locale;

public final class RtlTestUtils {

  public static void checkAppSupportsRtl() {
    Application application = ApplicationProvider.getApplicationContext();
    ApplicationInfo info = application.getApplicationInfo();
    boolean supportsRtl = (info.flags & ApplicationInfo.FLAG_SUPPORTS_RTL) != 0;
    assertThat(supportsRtl).isTrue();
    assertThat(info.targetSdkVersion).isGreaterThan(16);
  }

  public static void checkPlatformSupportsRtl() {
    assertThat(SDK_INT).isGreaterThan(16);
  }

  @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
  public static void applyRtlPseudoLocale() {
    setLocale(new Locale("ar", "XB"));
  }

  /**
   * @see org.robolectric.RuntimeEnvironment#setQualifiers(String)
   */
  @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
  @SuppressWarnings("deprecation")
  private static void setLocale(Locale locale) {
    Configuration configuration = new Configuration(Resources.getSystem().getConfiguration());
    DisplayMetrics displayMetrics = new DisplayMetrics();
    displayMetrics.setTo(Resources.getSystem().getDisplayMetrics());

    configuration.setLocale(locale);

    Resources systemResources = Resources.getSystem();
    systemResources.updateConfiguration(configuration, displayMetrics);
    ApplicationProvider
        .getApplicationContext()
        .getResources()
        .updateConfiguration(configuration, displayMetrics);
  }

  private RtlTestUtils() {
    throw new AssertionError();
  }
}
