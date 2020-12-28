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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/**
 * This is the fragment to demo in details of different indeterminate progress indicators.
 *
 * <p>This demo includes multiple examples of indeterminate {@link LinearProgressIndicator} and
 * {@link CircularProgressIndicator} and the ability to:
 *
 * <ul>
 *   <li>Update the indicator with a specified progress (which will change the mode to be
 *       determinate).
 *   <li>Hide the indicator
 *   <li>Show the indicator (this will reset the indicator to indeterminate mode)
 * </ul>
 */
public class ProgressIndicatorIndeterminateDemoFragment extends DemoFragment {

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_indeterminate_fragment,
            viewGroup,
            false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.content);
    content.addView(layoutInflater.inflate(getIndicatorsContent(), content, false), 0);

    initialize(view);

    return view;
  }

  /** Layout resource containing the progress indicator examples. */
  @LayoutRes
  protected int getIndicatorsContent() {
    return R.layout.cat_progress_indicator_indeterminate_indicators;
  }

  /**
   * Updates the linear progress indicator to show the specified progress. This is called every time
   * the "update" button is pressed.
   */
  protected void updateLinearProgressIndicator(
      @NonNull LinearProgressIndicator linearProgressIndicator, int progress) {
    linearProgressIndicator.setProgressCompat(progress, /*animated=*/ true);
  }

  /**
   * Updates the circular progress indicator to show the specified progress. This is called every
   * time the "update" button is pressed.
   */
  protected void updateCircularProgressIndicator(
      @NonNull CircularProgressIndicator circularProgressIndicator, int progress) {
    circularProgressIndicator.setProgressCompat(progress, /*animated=*/ true);
  }

  /**
   * Resets the linear progress indicator to its initial state (indeterminate). This is called every
   * time the "show" button is pressed.
   */
  protected void resetLinearProgressIndicator(
      @NonNull LinearProgressIndicator linearProgressIndicator) {
    // Reset to indeterminate if it was changed to determinate.
    if (!linearProgressIndicator.isIndeterminate()) {
      // Cannot set to indeterminate if the indicator is visible. Immediately set to
      // INVISIBLE instead of waiting for animation from calling hide().
      linearProgressIndicator.setVisibility(View.INVISIBLE);
      linearProgressIndicator.setIndeterminate(true);
    }
  }

  /**
   * Resets the circular progress indicator to its initial state (indeterminate). This is called
   * every time the "show" button is pressed.
   */
  protected void resetCircularProgressIndicator(
      @NonNull CircularProgressIndicator circularProgressIndicator) {
    // Reset to indeterminate if it was changed to determinate.
    if (!circularProgressIndicator.isIndeterminate()) {
      // Cannot set to indeterminate if the indicator is visible. Immediately set to
      // INVISIBLE instead of waiting for animation from calling hide().
      circularProgressIndicator.setVisibility(View.INVISIBLE);
      circularProgressIndicator.setIndeterminate(true);
    }
  }

  private void initialize(View view) {
    List<LinearProgressIndicator> linearProgressIndicatorList =
        DemoUtils.findViewsWithType(view, LinearProgressIndicator.class);
    List<CircularProgressIndicator> circularProgressIndicatorList =
        DemoUtils.findViewsWithType(view, CircularProgressIndicator.class);

    EditText progressInput = view.findViewById(R.id.progress_input);
    Button updateButton = view.findViewById(R.id.update_button);
    Button showButton = view.findViewById(R.id.show_button);
    Button hideButton = view.findViewById(R.id.hide_button);

    updateButton.setOnClickListener(
        v -> {
          int progress;
          try {
            progress = Integer.parseInt(progressInput.getEditableText().toString());
          } catch (NumberFormatException e) {
            progress = 0;
          }
          for (LinearProgressIndicator indicator : linearProgressIndicatorList) {
            updateLinearProgressIndicator(indicator, progress);
          }
          for (CircularProgressIndicator indicator : circularProgressIndicatorList) {
            updateCircularProgressIndicator(indicator, progress);
          }
        });
    showButton.setOnClickListener(
        v -> {
          for (LinearProgressIndicator indicator : linearProgressIndicatorList) {
            resetLinearProgressIndicator(indicator);
            indicator.show();
          }
          for (CircularProgressIndicator indicator : circularProgressIndicatorList) {
            resetCircularProgressIndicator(indicator);
            indicator.show();
          }
        });
    hideButton.setOnClickListener(
        v -> {
          for (LinearProgressIndicator indicator : linearProgressIndicatorList) {
            indicator.hide();
          }
          for (CircularProgressIndicator indicator : circularProgressIndicatorList) {
            indicator.hide();
          }
        });
  }
}
