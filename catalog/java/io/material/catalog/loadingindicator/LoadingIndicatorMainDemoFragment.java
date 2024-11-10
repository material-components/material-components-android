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
package io.material.catalog.loadingindicator;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.loadingindicator.LoadingIndicatorDrawable;
import com.google.android.material.loadingindicator.LoadingIndicatorSpec;
import io.material.catalog.feature.DemoFragment;

/** This is the fragment to demo simple use cases of {@link LoadingIndicator}. */
public class LoadingIndicatorMainDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_loading_indicator_fragment, viewGroup, false /* attachToRoot */);

    LoadingIndicatorSpec spec =
        new LoadingIndicatorSpec(getContext(), null, 0, com.google.android.material.R.style.Widget_Material3_LoadingIndicator);
    spec.setScaleToFit(true);
    LoadingIndicatorDrawable loadingIndicatorDrawable =
        LoadingIndicatorDrawable.create(getContext(), spec);
    MaterialButton loadingButton = view.findViewById(R.id.loading_btn);
    loadingButton.setIcon(loadingIndicatorDrawable);

    return view;
  }
}
