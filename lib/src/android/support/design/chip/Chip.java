/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.chip;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.theme.ThemeUtils;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

/**
 * Chips are compact elements that represent an attribute, text, entity, or action. They allow users
 * to enter information, select a choice, filter content, or trigger an action.
 *
 * <p>The Chip widget is a thin view wrapper around the {@link ChipDrawable}, which contains all of
 * the layout and draw logic.
 *
 * <p>Do not use the {@code android:button} attribute. It will be ignored because Chip manages its
 * own button Drawable.
 *
 * @see ChipDrawable
 */
public class Chip extends AppCompatCheckBox {

  private ChipDrawable chipDrawable;

  public Chip(Context context) {
    this(context, null);
  }

  public Chip(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.chipStyle);
  }

  public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ThemeUtils.checkAppCompatTheme(context);

    ChipDrawable drawable =
        ChipDrawable.createFromAttributes(context, attrs, defStyleAttr, R.style.Widget_Design_Chip);
    setButtonDrawable(drawable);
  }

  @Override
  public void setButtonDrawable(Drawable buttonDrawable) {
    super.setButtonDrawable(buttonDrawable);

    if ((buttonDrawable instanceof ChipDrawable)) {
      chipDrawable = (ChipDrawable) buttonDrawable;
    } else {
      throw new IllegalArgumentException("Button drawable must be an instance of ChipDrawable.");
    }
  }
}
