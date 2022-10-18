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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.R;
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
    int maxMeasuredWidth = 0;
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    for (MaterialButton button : buttons) {
      button.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
      maxMeasuredWidth = Math.max(maxMeasuredWidth, button.getMeasuredWidth());
      button.setOnClickListener(
          v -> {
            // Show a Snackbar with an action button, which should also have a MaterialButton style
            Snackbar snackbar =
                Snackbar.make(v, R.string.cat_button_clicked, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.cat_snackbar_action_button_text, v1 -> {});
            snackbar.show();
          });
    }

    // Using SwitchCompat here to avoid class cast issues in derived demos.
    SwitchCompat enabledSwitch = view.findViewById(R.id.cat_button_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          CharSequence updatedText =
              getText(
                  isChecked
                      ? R.string.cat_button_label_enabled
                      : R.string.cat_button_label_disabled);
          for (MaterialButton button : buttons) {
            if (!TextUtils.isEmpty(button.getText())) {
              // Do not update icon only button.
              button.setText(updatedText);
            }
            button.setEnabled(isChecked);
            button.setFocusable(isChecked);
          }
        });

    ViewGroup iconOnlyButtonsView = view.findViewById(R.id.material_icon_only_buttons_view);
    // Icon only buttons demo may not be there in derived demos.
    if (iconOnlyButtonsView != null) {
      List<MaterialButton> iconButtons =
          DemoUtils.findViewsWithType(iconOnlyButtonsView, MaterialButton.class);
      // using SwitchCompat here to avoid class cast issues in derived demos.
      SwitchCompat toggleableSwitch = view.findViewById(R.id.cat_button_toggleable_icon_buttons);
      toggleableSwitch.setOnCheckedChangeListener(
          (buttonView, isCheckable) -> {
            for (MaterialButton button : iconButtons) {
              button.setCheckable(isCheckable);
              button.setChecked(false);
            }
          });
    }

    SpannableStringBuilder ssb = new SpannableStringBuilder("⌧Hello");
    ssb.setSpan(new MySpan(), 1, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    TextView spannedButton = view.findViewById(R.id.material_replaced_text_button);
    spannedButton.setText(ssb);

    Drawable check = requireActivity().getResources().getDrawable(R.drawable.ic_dialogs_24px);
    MaterialButtonToggleGroup grp = view.findViewById(R.id.btnGrp);
    for (int i : grp.getCheckedButtonIds()) {
      MaterialButton btn = grp.findViewById(i);
      btn.setIcon(check);
    }
    int singleId = grp.getCheckedButtonId();
    if (singleId > 0) {
      MaterialButton btn = grp.findViewById(singleId);
      btn.setIcon(check);
    }
    grp.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
      MaterialButton btn = g.findViewById(checkedId);
      btn.setIcon(isChecked ? check : null);
    });
    MaterialButton spanned = grp.findViewById(R.id.btn);
    spanned.setText(ssb);

    return view;
  }

  class MySpan extends ReplacementSpan {
    int width = 50;

    @Override
    public void draw(
        @NonNull Canvas canvas,
        CharSequence charSequence,
        int i, int i1, float v, int i2, int i3, int i4,
        @NonNull Paint paint) {
      canvas.drawRect(v, i2, v + width, i4, paint);
    }

    @Override
    public int getSize(Paint a, CharSequence b, int c, int d, Paint.FontMetricsInt e) {
      return width;
    }
  }

  @LayoutRes
  protected int getButtonsContent() {
    return R.layout.cat_buttons_fragment;
  }
}
