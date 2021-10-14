/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.adaptive;

import io.material.catalog.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.util.Consumer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.FoldingFeature.Orientation;
import androidx.window.layout.WindowInfoRepository;
import androidx.window.layout.WindowLayoutInfo;
import io.material.catalog.feature.DemoFragment;
import java.util.List;
import java.util.concurrent.Executor;

/** A Fragment that displays a multi column expansion Adaptive demo. */
public class AdaptiveMultiColumnExpansionFragment extends DemoFragment {

  @Nullable private WindowInfoRepositoryCallbackAdapter windowInfoRepo;
  private final Consumer<WindowLayoutInfo> stateContainer = new StateContainer();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Executor executor = command -> handler.post(() -> handler.post(command));

  private ConstraintLayout constraintLayout;
  private ConstraintSet closedLayout;
  private ConstraintSet openLayout;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_adaptive_multi_column_expansion, viewGroup, false);
    constraintLayout = view.findViewById(R.id.constraint_layout);
    closedLayout = new ConstraintSet();
    closedLayout.clone(constraintLayout);
    openLayout = new ConstraintSet();
    openLayout.clone(constraintLayout);
    openLayout.connect(R.id.text_content, ConstraintSet.START, R.id.fold, ConstraintSet.END, 0);
    openLayout.connect(R.id.text_content, ConstraintSet.TOP, R.id.image, ConstraintSet.TOP, 0);
    windowInfoRepo =
        new WindowInfoRepositoryCallbackAdapter(
            WindowInfoRepository.getOrCreate(requireActivity()));
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (windowInfoRepo != null) {
      windowInfoRepo.addWindowLayoutInfoListener(executor, stateContainer);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (windowInfoRepo != null) {
      windowInfoRepo.removeWindowLayoutInfoListener(stateContainer);
    }
  }

  private class StateContainer implements Consumer<WindowLayoutInfo> {

    public StateContainer() {}

    @Override
    public void accept(WindowLayoutInfo windowLayoutInfo) {
      List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
      boolean isClosed = true;

      for (DisplayFeature displayFeature : displayFeatures) {
        if (displayFeature instanceof FoldingFeature) {
          FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
          if (foldingFeature.getState().equals(FoldingFeature.State.HALF_OPENED)
              || foldingFeature.getState().equals(FoldingFeature.State.FLAT)) {
            openLayout.applyTo(constraintLayout);
            Orientation orientation = foldingFeature.getOrientation();
            if (orientation.equals(FoldingFeature.Orientation.VERTICAL)) {
              // Device is open and fold is vertical.
              ConstraintLayout.getSharedValues()
                  .fireNewValue(
                      R.id.fold,
                      AdaptiveUtils.getFoldPosition(constraintLayout, foldingFeature, orientation));
            } else {
              // Device is open and fold is horizontal.
              ConstraintLayout.getSharedValues()
                  .fireNewValue(R.id.fold, constraintLayout.getWidth() / 2);
            }
            isClosed = false;
          }
        }
      }
      if (isClosed) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
          // Device is closed and in portrait.
          ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, 0);
          closedLayout.applyTo(constraintLayout);
        } else {
          // Device is closed and in landscape.
          openLayout.applyTo(constraintLayout);
          ConstraintLayout.getSharedValues()
              .fireNewValue(R.id.fold, constraintLayout.getWidth() / 2);
        }
      }
    }
  }
}
