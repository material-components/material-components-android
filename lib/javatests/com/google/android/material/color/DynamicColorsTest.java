/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.color;

import com.google.android.material.test.R;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.material.color.DynamicColors.Precondition;
import com.google.android.material.resources.MaterialAttributes;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.Robolectric;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowContextThemeWrapper;
import org.robolectric.util.ReflectionHelpers;

/** Tests the logic of {@link com.google.android.material.color.DynamicColors} utility class. */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class DynamicColorsTest {
  @Parameter(0)
  public String testName;

  @Parameter(1)
  public int baseTheme;

  private final MockApplication mockApplication = new MockApplication();

  private Activity mockActivity;

  @Parameters(name = "{0}")
  public static ImmutableList<Object[]> getTestData() {
    return ImmutableList.<Object[]>builder()
        .add(new Object[] {"Test dynamic colors with Light Theme", R.style.Theme_Material3_Light})
        .build();
  }

  @Before
  public void prepareTestActivity() {
    setDynamicColorAvailability(true);
    setSdkVersion(VERSION_CODES.S);
    mockActivity = Robolectric.buildActivity(Activity.class).get();
    mockActivity.setTheme(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithDefaultTheme() {
    DynamicColors.applyToActivitiesIfAvailable(mockApplication);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithCustomTheme() {
    final int mockThemeOverlay = 0xABCDABCD;
    DynamicColors.applyToActivitiesIfAvailable(mockApplication, mockThemeOverlay);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithPreconditionFalse() {
    final MockPrecondition mockPrecondition = new MockPrecondition();
    DynamicColors.applyToActivitiesIfAvailable(mockApplication, mockPrecondition);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithPreconditionTrue() {
    final MockPrecondition mockPrecondition = new MockPrecondition();
    mockPrecondition.shouldApplyDynamicColors = true;
    DynamicColors.applyToActivitiesIfAvailable(mockApplication, mockPrecondition);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithCustomThemeAndPrecondition() {
    final int mockThemeOverlay = 0xABCDABCD;
    final MockPrecondition mockPrecondition = new MockPrecondition();
    mockPrecondition.shouldApplyDynamicColors = true;
    DynamicColors.applyToActivitiesIfAvailable(
        mockApplication, mockThemeOverlay, mockPrecondition);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithNoDynamicColorAvailable() {
    setDynamicColorAvailability(false);
    final int mockThemeOverlay = 0xABCDABCD;
    final MockPrecondition mockPrecondition = new MockPrecondition();
    mockPrecondition.shouldApplyDynamicColors = true;
    DynamicColors.applyToActivitiesIfAvailable(
        mockApplication, mockThemeOverlay, mockPrecondition);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnApplicationWithLowSdkVersion() {
    setSdkVersion(VERSION_CODES.Q);
    final int mockThemeOverlay = 0xABCDABCD;
    final MockPrecondition mockPrecondition = new MockPrecondition();
    mockPrecondition.shouldApplyDynamicColors = true;
    DynamicColors.applyToActivitiesIfAvailable(
        mockApplication, mockThemeOverlay, mockPrecondition);
    mockApplication.capturedCallbacks.onActivityPreCreated(mockActivity, new Bundle());

    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnActivityWithDefaultTheme() {
    DynamicColors.applyIfAvailable(mockActivity);

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnActivityWithCustomTheme() {
    final int mockThemeOverlay = 0xABCDABCD;
    DynamicColors.applyIfAvailable(mockActivity, mockThemeOverlay);

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnActivityWithPreconditionFalse() {
    final MockPrecondition mockPrecondition = new MockPrecondition();
    DynamicColors.applyIfAvailable(mockActivity, mockPrecondition);

    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnActivityWithPreconditionTrue() {
    final MockPrecondition mockPrecondition = new MockPrecondition();
    mockPrecondition.shouldApplyDynamicColors = true;
    DynamicColors.applyIfAvailable(mockActivity, mockPrecondition);

    // TODO(b/230848477): Update tests to make sure dynamic colors theme overlay is indeed applied.
    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testApplyOnActivityWithNoDynamicColorAvailable() {
    setDynamicColorAvailability(false);
    final int mockThemeOverlay = 0xABCDABCD;
    DynamicColors.applyIfAvailable(mockActivity, mockThemeOverlay);

    assertThat(getThemeResId(mockActivity)).isEqualTo(baseTheme);
  }

  @Test
  public void testIsDynamicColorAvailable() {
    setDynamicColorAvailability(true);
    setSdkVersion(VERSION_CODES.S);
    assertThat(DynamicColors.isDynamicColorAvailable()).isTrue();
  }

  @Test
  public void testIsDynamicColorAvailableWithNoDynamicColorAvailable() {
    setDynamicColorAvailability(false);
    assertThat(DynamicColors.isDynamicColorAvailable()).isFalse();
  }

  @Test
  public void testIsDynamicColorAvailableWithLowSdkVersion() {
    setSdkVersion(VERSION_CODES.Q);
    assertThat(DynamicColors.isDynamicColorAvailable()).isFalse();
  }

  @Test
  public void testWrapContextWithDefaultTheme() {
    Context context = DynamicColors.wrapContextIfAvailable(mockActivity);

    assertThat(getThemeResId(context))
        .isEqualTo(resolveAttribute(baseTheme, R.attr.dynamicColorThemeOverlay));
  }

  @Test
  public void testWrapContextWithCustomTheme() {
    final int mockThemeOverlay = 0xABCDABCD;
    Context context = DynamicColors.wrapContextIfAvailable(mockActivity, mockThemeOverlay);

    assertThat(getThemeResId(context)).isEqualTo(mockThemeOverlay);
  }

  @Test
  public void testWrapContextWithNoDynamicColorAvailable() {
    setDynamicColorAvailability(false);
    final int mockThemeOverlay = 0xABCDABCD;
    Context context = DynamicColors.wrapContextIfAvailable(mockActivity, mockThemeOverlay);

    assertThat(getThemeResId(context)).isEqualTo(baseTheme);
  }

  private void setDynamicColorAvailability(boolean available) {
    ReflectionHelpers.setStaticField(
        Build.class, "MANUFACTURER", available ? "Google" : "Unsupported OEM");
  }

  private void setSdkVersion(int sdkVersion) {
    ReflectionHelpers.setStaticField(
        Build.VERSION.class, "SDK_INT", sdkVersion);
  }

  @SuppressWarnings("RestrictTo")
  private static int resolveAttribute(int theme, int attribute) {
    return MaterialAttributes.resolveOrThrow(
        new ContextThemeWrapper(getApplicationContext(), theme),
        attribute,
        "missing in theme");
  }

  private static int getThemeResId(Context activity) {
    ShadowContextThemeWrapper shadowContextThemeWrapper = Shadow.extract(activity);
    return shadowContextThemeWrapper.callGetThemeResId();
  }

  private static class MockApplication extends Application {
    private ActivityLifecycleCallbacks capturedCallbacks;

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
      capturedCallbacks = callback;
    }
  }

  private static class MockPrecondition implements Precondition {
    private boolean shouldApplyDynamicColors;

    @Override
    public boolean shouldApplyDynamicColors(Activity activity, int theme) {
      return shouldApplyDynamicColors;
    }
  }
}
