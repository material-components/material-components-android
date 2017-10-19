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
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.design.theme.ThemeUtils;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.CompoundButton;

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

  @Nullable private ChipDrawable chipDrawable;
  private boolean deferredCheckedValue;

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

    initOutlineProvider();

    setChecked(deferredCheckedValue);
  }

  private void initOutlineProvider() {
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              Drawable button = CompoundButtonCompat.getButtonDrawable((CompoundButton) view);
              if (button != null) {
                button.getOutline(outline);
              } else {
                outline.setRect(0, 0, view.getWidth(), view.getHeight());
                outline.setAlpha(0.0f);
              }
            }
          });
    }
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

  @Override
  public void setChecked(boolean checked) {
    if (chipDrawable == null) {
      // Defer the setChecked() call until after initialization.
      deferredCheckedValue = checked;
    } else if (chipDrawable.isCheckable()) {
      super.setChecked(checked);
    }
  }
}
