/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.transformation;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transformation.TransformationChildCard;
import io.material.catalog.draggable.DraggableCoordinatorLayout;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.OnBackPressedHandler;

/** A fragment that displays the main transformation demos for the Catalog app. */
public class TransformationMainDemoFragment extends DemoFragment implements OnBackPressedHandler {

  private FloatingActionButton fab;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_transformation_fragment, viewGroup, false /* attachToRoot */);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    fab = view.findViewById(R.id.fab);
    View closeButton = view.findViewById(R.id.close_button);
    TransformationChildCard sheet = view.findViewById(R.id.sheet);
    View scrim = view.findViewById(R.id.scrim);

    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

    fab.setOnClickListener(v -> fab.setExpanded(!fab.isExpanded()));
    closeButton.setOnClickListener(v -> fab.setExpanded(false));
    scrim.setOnClickListener(v -> fab.setExpanded(false));

    DraggableCoordinatorLayout container = (DraggableCoordinatorLayout) view;
    container.setViewDragListener(new DraggableCoordinatorLayout.ViewDragListener() {
      @Override
      public void onViewCaptured(@NonNull View view, int i) {
        sheet.setDragged(true);
      }

      @Override
      public void onViewReleased(@NonNull View view, float v, float v1) {
        sheet.setDragged(false);
      }
    });
    
    container.addDraggableChild(fab);
    container.addDraggableChild(sheet);

    return view;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @Override
  public boolean onBackPressed() {
    if (fab.isExpanded()) {
      fab.setExpanded(false);
      return true;
    }
    return false;
  }
}
