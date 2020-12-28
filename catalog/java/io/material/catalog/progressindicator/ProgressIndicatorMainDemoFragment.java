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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import io.material.catalog.feature.DemoFragment;

/**
 * This is the fragment to demo simple use cases of {@link LinearProgressIndicator} and {@link
 * CircularProgressIndicator}.
 */
public class ProgressIndicatorMainDemoFragment extends DemoFragment {
  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_main_fragment, viewGroup, false /* attachToRoot */);

    initialize(view);

    return view;
  }

  public void initialize(@NonNull View view) {
    LinearProgressIndicator linearDeterminate = view.findViewById(R.id.linear_determinate);
    CircularProgressIndicator circularDeterminate = view.findViewById(R.id.circular_determinate);
    EditText progressInput = view.findViewById(R.id.progress_input);
    Button updateButton = view.findViewById(R.id.update_button);

    updateButton.setOnClickListener(
        v -> {
          int progress;
          try {
            progress = Integer.parseInt(progressInput.getEditableText().toString());
          } catch (Exception e) {
            progress = 0;
          }
          linearDeterminate.setProgressCompat(progress, true);
          circularDeterminate.setProgressCompat(progress, true);
        });
  }
}
