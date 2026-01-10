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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowPath.Point.Type.MOVE_TO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION_CODES;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCanvas;
import org.robolectric.shadows.ShadowPath;
import org.robolectric.shadows.ShadowPath.Point;

/** Tests for {@link MemoryView}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.M)
public class MemoryViewTest {

  private static final int BYTES_IN_MB = 1024 * 1024;
  private MemoryView memoryView;
  private MemoryView.RuntimeWrapper runtime;

  @Before
  public void createAndMeasureMemoryView() {
    Context context = ApplicationProvider.getApplicationContext();
    context.setTheme(R.style.Theme_AppCompat);
    memoryView = new MemoryView(context);
    int spec = MeasureSpec.makeMeasureSpec(500, MeasureSpec.EXACTLY);
    memoryView.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    memoryView.measure(spec, spec);
    memoryView.layout(0, 0, 500, 500);
  }

  @Before
  public void setUpRuntime() {
    runtime = mock(MemoryView.RuntimeWrapper.class);
    when(runtime.maxMemory()).thenReturn(100L * BYTES_IN_MB);
    when(runtime.totalMemory()).thenReturn(100L * BYTES_IN_MB);
    when(runtime.freeMemory()).thenReturn(75L * BYTES_IN_MB);
  }

  @Test
  public void memoryView_correctText_withProvidedRuntime() {
    memoryView.refreshMemStats(runtime);

    assertThat(memoryView.getText().toString()).isEqualTo("used: 25 MB / 100 MB (25%)");
  }

  @Test
  public void memoryView_pathNotEmpty_afterRefreshing() {
    for (int i = 0; i < 5; ++i) {
      // increase memory usage
      when(runtime.freeMemory()).thenReturn((75L - 5 * i) * BYTES_IN_MB);
      memoryView.refreshMemStats(runtime);
    }

    ShadowCanvas shadowCanvas = drawAndGetShadowCanvas();
    assertThat(shadowCanvas.getPathPaintHistoryCount()).isEqualTo(1);

    ShadowPath drawnPath = shadowOf(shadowCanvas.getDrawnPath(0));
    assertThat(drawnPath.getPoints().get(0)).isEqualTo(new Point(36f, 250f, MOVE_TO));
  }

  @SuppressLint("WrongCall") // Testing onDraw
  private ShadowCanvas drawAndGetShadowCanvas() {
    Canvas canvas = new Canvas();
    memoryView.onDraw(canvas);

    return shadowOf(canvas);
  }

  @Test
  @SuppressLint("WrongCall") // Testing onDraw
  public void memoryView_emptyPath_withoutRefreshing() {
    ShadowCanvas shadowCanvas = drawAndGetShadowCanvas();

    assertThat(shadowCanvas.getPathPaintHistoryCount()).isEqualTo(1);
  }
}
