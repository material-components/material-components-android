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

import io.material.catalog.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import com.google.common.collect.EvictingQueue;
import java.util.Queue;

/**
 * A widget that shows the memory usage of the app.
 */
public final class MemoryView extends AppCompatTextView {

  private static final int PLOT_MARGIN = 10;
  private static final int STROKE_WIDTH = 5;
  private static final int MIN_PIXELS_FOR_GRAPH = 60;

  private final Queue<Long> memSnapshots = EvictingQueue.create(5);
  private final Paint paint = new Paint();
  private final Path path = new Path();

  private long maxMemoryInBytes;


  public MemoryView(Context context) {
    this(context, null);
  }

  public MemoryView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MemoryView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setGravity(TEXT_ALIGNMENT_CENTER);

    paint.setStyle(Style.FILL_AND_STROKE);
    paint.setStrokeWidth(STROKE_WIDTH);
    paint.setAntiAlias(true);
  }

  /**
   * Draw a chart next to the text based on the last memory snapshots data points.
   */
  @Override
  protected void onDraw(Canvas canvas) {
    System.out.println("marian draw");
    super.onDraw(canvas);
    plotMemoryUsage();

    canvas.drawPath(path, paint);
  }

  private void plotMemoryUsage() {
    path.reset();
    int textWidth = (int) getPaint().measureText(getText().toString());
    int availableWidth = getMeasuredWidth() - textWidth - getPaddingLeft() - getPaddingRight();
    if (availableWidth < MIN_PIXELS_FOR_GRAPH || memSnapshots.isEmpty()) {
      // don't show chart
      return;
    }

    int startX = textWidth + getPaddingLeft() + PLOT_MARGIN;
    int availableHeight = getMeasuredHeight() / 2;
    int startY = availableHeight;
    path.moveTo(startX, startY);

    float prevPercentage = 0;
    byte index = 0;
    for (long snapshot : memSnapshots) {
      index++;
      float percentage = snapshot / (float) maxMemoryInBytes;
      float x = startX + (availableWidth / (float) memSnapshots.size()) * index;
      float amplificationFactor = 1;
      // exaggerate differences to see the change in the chart
      if (prevPercentage > percentage) {
        amplificationFactor = 5f;
      } else if (prevPercentage < percentage) {
        amplificationFactor = .2f;
      }

      float y = startY - (percentage * availableHeight) * amplificationFactor;

      prevPercentage = percentage;
      path.lineTo(x, y);
      path.addCircle(x, y, 4, Direction.CCW);
    }

    System.out.println("marian path");
  }

  /**
   * Load color for chart now that we are going to start drawing.
   */
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    TypedValue typedValue = new TypedValue();
    getContext()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);

    int colorPrimary = typedValue.data;
    paint.setColor(colorPrimary);
  }

  /** A wrapper around {@link Runtime} to allow mocking by tests. */
  interface RuntimeWrapper {
    long maxMemory();

    long totalMemory();

    long freeMemory();
  }

  public void refreshMemStats(@NonNull RuntimeWrapper runtime) {
    maxMemoryInBytes = runtime.maxMemory();
    long availableMemInBytes = maxMemoryInBytes - (runtime.totalMemory() - runtime.freeMemory());
    long usedMemInBytes = maxMemoryInBytes - availableMemInBytes;
    long usedMemInPercentage = usedMemInBytes * 100 / maxMemoryInBytes;

    memSnapshots.add(usedMemInBytes);

    Context context = getContext();
    setText(context.getString(
        R.string.cat_demo_memory_usage,
        Formatter.formatShortFileSize(context, usedMemInBytes),
        Formatter.formatShortFileSize(context, maxMemoryInBytes),
        usedMemInPercentage));
  }
}

