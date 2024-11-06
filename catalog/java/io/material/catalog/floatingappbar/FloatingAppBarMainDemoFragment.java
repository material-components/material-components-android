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
package io.material.catalog.floatingappbar;

import io.material.catalog.R;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import io.material.catalog.feature.DemoFragment;
import java.util.Random;

/** A fragment that displays the main Floating App Bar demo for the Catalog app. */
public class FloatingAppBarMainDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {

    View view =
        layoutInflater.inflate(
            R.layout.cat_floating_app_bar_fragment, viewGroup, /* attachToRoot= */ false);

    TextView bodyText = (TextView) view.findViewById(R.id.body_text);

    // Initialize bold format button.
    MaterialButton boldButton = view.findViewById(R.id.floating_toolbar_button_bold);
    boldButton.addOnCheckedChangeListener(
        (button, isChecked) -> {
          Typeface typeface = bodyText.getTypeface();
          if (isChecked) {
            bodyText.setTypeface(
                typeface, typeface.isItalic() ? Typeface.BOLD_ITALIC : Typeface.BOLD);
          } else {
            bodyText.setTypeface(
                Typeface.create(
                    bodyText.getTypeface(),
                    typeface.isItalic() ? Typeface.ITALIC : Typeface.NORMAL));
          }
        });

    // Initialize italics format button.
    MaterialButton italicsButton = view.findViewById(R.id.floating_toolbar_button_italic);
    italicsButton.addOnCheckedChangeListener(
        (button, isChecked) -> {
          Typeface typeface = bodyText.getTypeface();
          if (isChecked) {
            bodyText.setTypeface(
                typeface, typeface.isBold() ? Typeface.BOLD_ITALIC : Typeface.ITALIC);
          } else {
            bodyText.setTypeface(
                Typeface.create(
                    bodyText.getTypeface(), typeface.isBold() ? Typeface.BOLD : Typeface.NORMAL));
          }
        });

    // Initialize underlined format button.
    MaterialButton underlinedButton = view.findViewById(R.id.floating_toolbar_button_underlined);
    underlinedButton.addOnCheckedChangeListener(
        (button, isChecked) -> {
          int paintFlags = bodyText.getPaintFlags();
          if (isChecked) {
            bodyText.setPaintFlags(paintFlags | Paint.UNDERLINE_TEXT_FLAG);
          } else {
            bodyText.setPaintFlags(paintFlags & ~Paint.UNDERLINE_TEXT_FLAG);
          }
        });

    // Initialize color text format button.
    MaterialButton colorTextButton = view.findViewById(R.id.floating_toolbar_button_color_text);
    colorTextButton.setOnClickListener(v -> bodyText.setTextColor(getRandomColor()));

    // Initialize color fill format button.
    MaterialButton colorFillButton = view.findViewById(R.id.floating_toolbar_button_color_fill);
    colorFillButton.setOnClickListener(v -> view.setBackgroundColor(getRandomColor()));

    return view;
  }

  @ColorInt
  private int getRandomColor() {
    Random random = new Random();
    final int bound = 256;
    return Color.rgb(random.nextInt(bound), random.nextInt(bound), random.nextInt(bound));
  }
}
