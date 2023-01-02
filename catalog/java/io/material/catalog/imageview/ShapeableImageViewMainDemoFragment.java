/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.material.catalog.imageview;

import io.material.catalog.R;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import io.material.catalog.feature.DemoFragment;
import java.util.Random;

/** A fragment that displays the main {@link} demo for the Catalog app. */
public class ShapeableImageViewMainDemoFragment extends DemoFragment {

  /**
   * Creates a demo fragment with an image that can change shape and randomely makes the dog image
   * wink.
   */
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.catalog_imageview, viewGroup, false /* attachToRoot */);
    MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.togglegroup);
    ShapeableImageView imageView = view.findViewById(R.id.image_view);
    ShapeableImageView iconView = view.findViewById(R.id.icon_view);

    SparseArray<ShapeAppearanceModel> shapes = new SparseArray<>();
    shapes.put(
        R.id.button_diamond,
        ShapeAppearanceModel.builder()
            .setAllCorners(CornerFamily.CUT, /*cornerSize=*/0f)
            .setAllCornerSizes(ShapeAppearanceModel.PILL)
            .build());
    shapes.put(
        R.id.button_circle,
        ShapeAppearanceModel.builder().setAllCornerSizes(ShapeAppearanceModel.PILL).build());
    shapes.put(R.id.button_square, ShapeAppearanceModel.builder().build());

    Random random = new Random();

    toggleGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) {
            return;
          }

          ShapeAppearanceModel shape = shapes.get(checkedId);

          // Randomly makes dog wink.
          imageView.setImageResource(
              random.nextBoolean() ? R.drawable.dog_image : R.drawable.dog_image_wink);
          imageView.setShapeAppearanceModel(shape);

          iconView.setShapeAppearanceModel(shape);
        });

    return view;
  }
}
