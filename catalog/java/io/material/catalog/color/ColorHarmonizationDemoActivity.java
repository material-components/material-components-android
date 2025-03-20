/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.color;

import io.material.catalog.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.HarmonizedColorAttributes;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoActivity;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays the Color Harmonization demo for the Catalog app. */
public class ColorHarmonizationDemoActivity extends DemoActivity {

  private static final HarmonizableButtonData[] HARMONIZABLE_BUTTON_DATA_LIST =
      new HarmonizableButtonData[] {
        new HarmonizableButtonData(
            R.id.red_button_dark, R.color.error_reference, /* isLightButton= */ false),
        new HarmonizableButtonData(
            R.id.red_button_light, R.color.error_reference, /* isLightButton= */ true),
        new HarmonizableButtonData(
            R.id.yellow_button_dark, R.color.yellow_reference, /* isLightButton= */ false),
        new HarmonizableButtonData(
            R.id.yellow_button_light, R.color.yellow_reference, /* isLightButton= */ true),
        new HarmonizableButtonData(
            R.id.green_button_dark, R.color.green_reference, /* isLightButton= */ false),
        new HarmonizableButtonData(
            R.id.green_button_light, R.color.green_reference, /* isLightButton= */ true),
        new HarmonizableButtonData(
            R.id.blue_button_dark, R.color.blue_reference, /* isLightButton= */ false),
        new HarmonizableButtonData(
            R.id.blue_button_light, R.color.blue_reference, /* isLightButton= */ true),
      };

  private static final ColorHarmonizationGridRowData[] HARMONIZATION_GRID_ROW_DATA_LIST =
      new ColorHarmonizationGridRowData[] {
        new ColorHarmonizationGridRowData(
            R.id.cat_colors_error,
            R.id.cat_colors_harmonized_error,
            new int[] {
              androidx.appcompat.R.attr.colorError,
              com.google.android.material.R.attr.colorOnError,
              com.google.android.material.R.attr.colorErrorContainer,
              com.google.android.material.R.attr.colorOnErrorContainer
            },
            R.array.cat_error_strings),
        new ColorHarmonizationGridRowData(
            R.id.cat_colors_yellow,
            R.id.cat_colors_harmonized_yellow,
            R.color.yellow_reference,
            R.array.cat_yellow_strings),
        new ColorHarmonizationGridRowData(
            R.id.cat_colors_green,
            R.id.cat_colors_harmonized_green,
            R.color.green_reference,
            R.array.cat_green_strings),
        new ColorHarmonizationGridRowData(
            R.id.cat_colors_blue,
            R.id.cat_colors_harmonized_blue,
            R.color.blue_reference,
            R.array.cat_blue_strings)
      };

  private Context dynamicColorsContext;
  private Context harmonizedContext;
  private View demoView;

  private final List<HarmonizableButton> harmonizableButtonList = new ArrayList<>();

  @Nullable
  @Override
  public View onCreateDemoView(
      @Nullable LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    demoView =
        layoutInflater.inflate(
            R.layout.cat_colors_harmonization_fragment, viewGroup, false /* attachToRoot */);

    dynamicColorsContext = DynamicColors.wrapContextIfAvailable(this);
    HarmonizedColorsOptions options =
        new HarmonizedColorsOptions.Builder()
            .setColorResourceIds(
                new int[] {
                  R.color.yellow_reference, R.color.blue_reference, R.color.green_reference,
                })
            .setColorAttributes(HarmonizedColorAttributes.createMaterialDefaults())
            .build();
    harmonizedContext = HarmonizedColors.wrapContextIfAvailable(dynamicColorsContext, options);

    for (ColorHarmonizationGridRowData colorHarmonizationGridRowData :
        HARMONIZATION_GRID_ROW_DATA_LIST) {
      createColorGridAndPopulateLayout(
          dynamicColorsContext,
          colorHarmonizationGridRowData,
          colorHarmonizationGridRowData.getLeftLayoutId());
      createColorGridAndPopulateLayout(
          harmonizedContext,
          colorHarmonizationGridRowData,
          colorHarmonizationGridRowData.getRightLayoutId());
    }
    // Setup buttons text color based on current theme.
    for (HarmonizableButtonData harmonizableButtonData : HARMONIZABLE_BUTTON_DATA_LIST) {
      harmonizableButtonList.add(HarmonizableButton.create(demoView, harmonizableButtonData));
    }
    updateButtons(/* harmonize= */ false);
    MaterialSwitch enabledSwitch = demoView.findViewById(R.id.cat_color_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtons(isChecked));
    return demoView;
  }

  // This will disable app-wide color harmonization, to not have conflicts with the harmonization
  // logic within this demo.
  @Override
  public boolean isColorHarmonizationEnabled() {
    return false;
  }

  private void createColorGridAndPopulateLayout(
      Context context,
      ColorHarmonizationGridRowData colorHarmonizationGridRowData,
      @IdRes int layoutId) {
    ColorGrid colorGrid = createColorGrid(context, colorHarmonizationGridRowData);
    LinearLayout layout = demoView.findViewById(layoutId);
    layout.addView(colorGrid.renderView(getLayoutInflater(), layout));
  }

  private ColorGrid createColorGrid(
      Context context, ColorHarmonizationGridRowData colorHarmonizationGridRowData) {
    if (colorHarmonizationGridRowData.getColorAttributeResIds().length > 0
        && colorHarmonizationGridRowData.getColorResId() == 0) {
      return ColorGrid.createFromAttrResId(
          context,
          getResources().getStringArray(colorHarmonizationGridRowData.getColorNameIds()),
          colorHarmonizationGridRowData.getColorAttributeResIds());
    } else {
      return ColorGrid.createFromColorGridData(
          ColorGridData.createFromColorResId(
              context,
              colorHarmonizationGridRowData.getColorResId(),
              colorHarmonizationGridRowData.getColorNameIds()));
    }
  }

  private void updateButtons(boolean harmonize) {
    for (HarmonizableButton button : harmonizableButtonList) {
      button.updateColors(harmonize);
    }
  }
}
