/*
 * Copyright 2024 The Android Open Source Project
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

package io.material.catalog.button;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialSplitButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a split button demo for the Catalog app. */
public class SplitButtonDemoFragment extends DemoFragment {

  /** Create a Demo View with different types of {@link MaterialSplitButton} */
  @Nullable
  @Override
  public View onCreateDemoView(
      @Nullable LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(getSplitButtonContent(), viewGroup, /* attachToRoot= */ false);
    List<MaterialSplitButton> splitButtons =
        DemoUtils.findViewsWithType(view, MaterialSplitButton.class);
    MaterialSwitch enabledToggle = view.findViewById(R.id.switch_enable);
    enabledToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialSplitButton splitButton : splitButtons) {
            // Enable the SplitButton if enable toggle is checked.
            splitButton.setEnabled(isChecked);
          }
        });

    // Popup menu demo for split button
    MaterialButton button =
        (MaterialButton) view.findViewById(R.id.expand_more_or_less_filled_icon_popup);
    button.addOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            showMenu(button, R.menu.split_button_menu);
          }
        });

    return view;
  }

  @SuppressWarnings("RestrictTo")
  private void showMenu(View v, @MenuRes int menuRes) {
    PopupMenu popup = new PopupMenu(getContext(), v);
    // Inflating the Popup using XML file
    popup.getMenuInflater().inflate(menuRes, popup.getMenu());
    popup.setOnMenuItemClickListener(
        menuItem -> {
          Snackbar.make(
                  getActivity().findViewById(android.R.id.content),
                  menuItem.getTitle(),
                  Snackbar.LENGTH_LONG)
              .show();
          return true;
        });
    popup.setOnDismissListener(
        popupMenu -> {
          MaterialButton button =
              (MaterialButton) v.findViewById(R.id.expand_more_or_less_filled_icon_popup);
          button.setChecked(false);
        });
    popup.show();
  }

  @LayoutRes
  protected int getSplitButtonContent() {
    return R.layout.cat_split_button_fragment;
  }
}
