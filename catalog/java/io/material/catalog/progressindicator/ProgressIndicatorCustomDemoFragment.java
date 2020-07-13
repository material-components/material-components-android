/*
 * Copyright 2020 The Android Open Source Project
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
package io.material.catalog.progressindicator;

import io.material.catalog.R;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.progressindicator.DeterminateDrawable;
import com.google.android.material.progressindicator.DrawingDelegate;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.progressindicator.LinearIndeterminateNonSeamlessAnimatorDelegate;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.progressindicator.ProgressIndicatorSpec;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;

/** The fragment demos progress indicators with a custom (wavy) drawable. */
public class ProgressIndicatorCustomDemoFragment extends DemoFragment {

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_custom_fragment, viewGroup, false /* attachToRoot */);

    initialize(view);

    return view;
  }

  public void initialize(@NonNull View view) {
    ProgressIndicator indeterminateIndicator =
        view.findViewById(R.id.custom_indeterminate_indicator);
    ProgressIndicator determinateIndicator = view.findViewById(R.id.custom_determinate_indicator);

    indeterminateIndicator.initializeDrawables(
        new IndeterminateDrawable(
            indeterminateIndicator.getSpec(),
            new WavyDrawingDelegate(),
            new LinearIndeterminateNonSeamlessAnimatorDelegate(getContext())),
        null);
    determinateIndicator.initializeDrawables(
        null, new DeterminateDrawable(determinateIndicator.getSpec(), new WavyDrawingDelegate()));

    Slider slider = view.findViewById(R.id.slider);
    Button showButton = view.findViewById(R.id.show_button);
    Button hideButton = view.findViewById(R.id.hide_button);

    slider.addOnChangeListener(
        (sliderObj, value, fromUser) -> {
          determinateIndicator.setProgressCompat((int) value, true);
        });
    showButton.setOnClickListener(
        v -> {
          indeterminateIndicator.show();
          determinateIndicator.show();
        });
    hideButton.setOnClickListener(
        v -> {
          indeterminateIndicator.hide();
          determinateIndicator.hide();
        });
  }

  static class WavyDrawingDelegate implements DrawingDelegate {

    private static final int WAVE_ARC_RADIUS_MULTIPLIER = 5;
    // The x coordinates of the centers of all arcs forming the wave. Y coordinates are always 0.
    int[] arcCentersX;
    int arcRadius;
    RectF arcPatternBound;

    @Override
    public int getPreferredWidth(@NonNull ProgressIndicatorSpec spec) {
      return -1;
    }

    @Override
    public int getPreferredHeight(@NonNull ProgressIndicatorSpec spec) {
      // The radius of the outer edge of each arc is 10 times of the indicator's width, so that the
      // arc has a radius of 5 times (large enough) of indicator's width.
      return spec.indicatorWidth
          * (WAVE_ARC_RADIUS_MULTIPLIER * 2 + 1 /*Half width on the top and the bottom.*/);
    }

    @Override
    public void adjustCanvas(
        @NonNull Canvas canvas, @NonNull ProgressIndicatorSpec spec, float widthFraction) {
      // Calculates how many semi-circles are needed.
      Rect clipBounds = canvas.getClipBounds();
      int arcCount =
          (clipBounds.width() - spec.indicatorWidth)
              / (2 * WAVE_ARC_RADIUS_MULTIPLIER * spec.indicatorWidth);
      arcRadius = spec.indicatorWidth * WAVE_ARC_RADIUS_MULTIPLIER;
      // Calculates the x coordinate of the centers of circles.
      arcCentersX = new int[arcCount];
      for (int arcIndex = 0; arcIndex < arcCount; arcIndex++) {
        arcCentersX[arcIndex] = -(arcCount - 1) * arcRadius + 2 * arcIndex * arcRadius;
      }
      // Initializes the rect bounds of a single arc.
      arcPatternBound = new RectF(-arcRadius, -arcRadius, arcRadius, arcRadius);

      // Positions the canvas to the center of the clip bounds.
      canvas.translate(clipBounds.width() / 2f, clipBounds.height() / 2f);
      // Flips the canvas horizontally if inverse.
      if (spec.inverse) {
        canvas.scale(-1f, 1f);
      }
    }

    @Override
    public void fillTrackWithColor(
        @NonNull Canvas canvas,
        @NonNull Paint paint,
        @ColorInt int color,
        @FloatRange(from = 0.0, to = 1.0) float startFraction,
        @FloatRange(from = 0.0, to = 1.0) float endFraction,
        float trackWidth,
        float cornerRadius) {
      // No need to draw if startFraction and endFraction are same.
      if (startFraction == endFraction) {
        return;
      }

      // Initializes Paint object.
      paint.setStyle(Style.STROKE);
      paint.setAntiAlias(true);
      paint.setColor(color);
      paint.setStrokeWidth(trackWidth);

      // Draws the stroke arcs without rounded corners.
      float fractionPerArc = 1f / arcCentersX.length;
      for (int arcCenterIndex = 0; arcCenterIndex < arcCentersX.length; arcCenterIndex++) {
        float smallestFractionInThisArc = arcCenterIndex * fractionPerArc;
        float biggestFractionInThisArc = smallestFractionInThisArc + fractionPerArc;
        if (startFraction > biggestFractionInThisArc || endFraction < smallestFractionInThisArc) {
          continue;
        }
        float startFractionInThisArc =
            max(smallestFractionInThisArc, startFraction) - smallestFractionInThisArc;
        float endFractionInThisArc =
            min(biggestFractionInThisArc, endFraction) - smallestFractionInThisArc;
        // Since 0 degree is 3 o'clock, all circles should be drawn from 9 o'clock (180 degree).
        int startAngle = 180 + ((int) (startFractionInThisArc / fractionPerArc * 180));
        int endAngle = 180 + ((int) (endFractionInThisArc / fractionPerArc * 180));
        canvas.save();
        canvas.translate(arcCentersX[arcCenterIndex], 0);
        if (arcCenterIndex % 2 == 0) {
          canvas.scale(1f, -1f);
        }
        canvas.drawArc(arcPatternBound, startAngle, endAngle - startAngle, false, paint);
        canvas.restore();
      }
    }
  }
}
