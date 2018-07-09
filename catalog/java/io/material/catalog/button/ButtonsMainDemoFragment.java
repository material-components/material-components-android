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

package io.material.catalog.button;

import io.material.catalog.R;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays main button demos for the Catalog app. */
public class ButtonsMainDemoFragment extends DemoFragment {

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getButtonsContent(), viewGroup, false /* attachToRoot */);

    List<MaterialButton> buttons = DemoUtils.findViewsWithType(view, MaterialButton.class);

    for (MaterialButton button : buttons) {
      button.setOnClickListener(
          v -> {
            // Show a Snackbar with an action button, which should also have a MaterialButton style
            Snackbar snackbar =
                Snackbar.make(v, R.string.cat_button_clicked, BaseTransientBottomBar.LENGTH_LONG);
            snackbar.setAction(
                R.string.cat_snackbar_action_button_text,
                new OnClickListener() {
                  @Override
                  public void onClick(View v) {}
                });
            snackbar.show();
          });
    }

    return view;
  }

  @LayoutRes
  protected int getButtonsContent() {
    return R.layout.cat_buttons_fragment;
  }
}
