package com.google.android.material.slider;

import static android.os.Build.VERSION.SDK_INT;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import androidx.test.core.app.ApplicationProvider;
import java.util.Locale;

public final class RtlTestUtils {

  public static void checkAppSupportsRtl() {
    Application application = ApplicationProvider.getApplicationContext();
    ApplicationInfo info = application.getApplicationInfo();
    boolean supportsRtl = (info.flags & ApplicationInfo.FLAG_SUPPORTS_RTL) != 0;
    assertTrue("android:supportsRtl must be true.", supportsRtl);
    assertTrue("targetSdkVersion must be >= 17.", info.targetSdkVersion >= 17);
  }

  public static void checkPlatformSupportsRtl() {
    assertTrue("SDK_INT must be >= 17.", SDK_INT >= 17);
  }

  public static void applyRtlPseudoLocale() {
    setLocale(new Locale("ar", "XB"));
  }

  /**
   * @see org.robolectric.RuntimeEnvironment#setQualifiers(String)
   */
  @SuppressWarnings("deprecation")
  private static void setLocale(Locale locale) {
    Configuration configuration = new Configuration(Resources.getSystem().getConfiguration());
    DisplayMetrics displayMetrics = new DisplayMetrics();
    displayMetrics.setTo(Resources.getSystem().getDisplayMetrics());

    configuration.setLocale(locale);

    Resources systemResources = Resources.getSystem();
    systemResources.updateConfiguration(configuration, displayMetrics);

    if (application != null) {
      application.getResources().updateConfiguration(configuration, displayMetrics);
    }
  }

  private RtlTestUtils() {
    throw new AssertionError();
  }
}
