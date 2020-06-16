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
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.progressindicator.ProgressIndicator;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/**
 * This is the fragment to demo in details of different indeterminate types of {@link
 * ProgressIndicator}.
 *
 * <p>This demo includes multiple examples of indeterminate ProgressIndicators and the ability to:
 *
 * <ul>
 *   <li>Update the ProgressIndicator with a specified progress (which will change the mode to be
 *       determinate).
 *   <li>Hide the ProgressIndicator
 *   <li>Show the ProgressIndicator (this will reset the ProgressIndicator to indeterminate mode)
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

  /** Layout resource containing {@link ProgressIndicator} examples. */
  @LayoutRes
  protected int getIndicatorsContent() {
    return R.layout.cat_progress_indicator_indeterminate_indicators;
  }

  /**
   * Updates the progress indicator to show the specified progress. This is called every time the
   * "update" button is pressed.
   */
  protected void updateProgressIndicator(
      @NonNull ProgressIndicator progressIndicator, int progress) {
    progressIndicator.setProgressCompat(progress, /*animated=*/ true);
  }

  /**
   * Resets the progress indicator to its initial state (indeterminate). This is called every time
   * the "show" button is pressed.
   */
  protected void resetProgressIndicator(@NonNull ProgressIndicator progressIndicator) {
    // Reset to indeterminate if it was changed to determinate.
    if (!progressIndicator.isIndeterminate()) {
      // Cannot set to indeterminate if the indicator is visible. Immediately set to
      // INVISIBLE instead of waiting for animation from calling hide().
      progressIndicator.setVisibility(View.INVISIBLE);
      progressIndicator.setIndeterminate(true);
    }
  }

  private void initialize(View view) {
    List<ProgressIndicator> indicatorList =
        DemoUtils.findViewsWithType(view, ProgressIndicator.class);

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
          for (ProgressIndicator progressIndicator : indicatorList) {
            updateProgressIndicator(progressIndicator, progress);
          }
        });
    showButton.setOnClickListener(
        v -> {
          for (ProgressIndicator progressIndicator : indicatorList) {
            resetProgressIndicator(progressIndicator);
            progressIndicator.show();
            progressIndicator.hide();
          }
        });
    hideButton.setOnClickListener(
        v -> {
          for (ProgressIndicator progressIndicator : indicatorList) {
            progressIndicator.hide();
          }
        });
  }
}
